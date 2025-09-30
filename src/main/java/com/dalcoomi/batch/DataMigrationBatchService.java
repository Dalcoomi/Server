package com.dalcoomi.batch;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.encryption.HashService;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
import com.dalcoomi.member.infrastructure.MemberJpaRepository;
import com.dalcoomi.member.infrastructure.SocialConnectionJpaEntity;
import com.dalcoomi.member.infrastructure.SocialConnectionJpaRepository;
import com.dalcoomi.transaction.infrastructure.TransactionJpaEntity;
import com.dalcoomi.transaction.infrastructure.TransactionJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationBatchService {

	private static final int BATCH_SIZE = 500;
	private final MemberJpaRepository memberJpaRepository;
	private final SocialConnectionJpaRepository socialConnectionJpaRepository;
	private final TransactionJpaRepository transactionJpaRepository;
	private final HashService hashService;

	@Transactional
	public void migratePlainTextData() {
		log.info("===== 평문 데이터 암호화 마이그레이션 시작 =====");

		try {
			int totalProcessed = 0;

			// Member 마이그레이션
			log.info("회원 데이터 마이그레이션 시작");
			totalProcessed += migrateMemberData();

			// SocialConnection 마이그레이션
			log.info("소셜 연결 데이터 마이그레이션 시작");
			totalProcessed += migrateSocialConnectionData();

			// Transaction 마이그레이션 (해시 없이 암호화만)
			log.info("거래 내역 데이터 마이그레이션 시작");
			totalProcessed += migrateTransactionData();

			log.info("평문 데이터 암호화 마이그레이션 완료. 총 {} 건 처리", totalProcessed);
		} catch (Exception e) {
			log.error("평문 데이터 마이그레이션 중 오류 발생", e);
			throw e;
		}
	}

	private int migrateMemberData() {
		int processedCount = 0;
		int totalProcessed = 0;

		do {
			Pageable pageable = PageRequest.of(0, BATCH_SIZE);
			List<MemberJpaEntity> plainTextMembers = memberJpaRepository.findPlainTextMembers(pageable);
			processedCount = plainTextMembers.size();

			if (processedCount > 0) {
				migrateMemberBatch(plainTextMembers);
				totalProcessed += processedCount;
				log.info("회원 배치 처리 완료: {} 건, 총 처리: {} 건", processedCount, totalProcessed);
			}
		} while (processedCount == BATCH_SIZE);

		log.info("회원 데이터 마이그레이션 완료: {} 건", totalProcessed);
		return totalProcessed;
	}

	private int migrateSocialConnectionData() {
		int processedCount = 0;
		int totalProcessed = 0;

		do {
			Pageable pageable = PageRequest.of(0, BATCH_SIZE);
			List<SocialConnectionJpaEntity> plainTextConnections
				= socialConnectionJpaRepository.findPlainTextConnections(pageable);
			processedCount = plainTextConnections.size();

			if (processedCount > 0) {
				migrateSocialConnectionBatch(plainTextConnections);
				totalProcessed += processedCount;
				log.info("소셜 연결 배치 처리 완료: {} 건, 총 처리: {} 건", processedCount, totalProcessed);
			}
		} while (processedCount == BATCH_SIZE);

		log.info("소셜 연결 데이터 마이그레이션 완료: {} 건", totalProcessed);
		return totalProcessed;
	}

	private int migrateTransactionData() {
		int processedCount = 0;
		int totalProcessed = 0;
		int maxIterations = 5;
		int iteration = 0;

		do {
			Pageable pageable = PageRequest.of(0, BATCH_SIZE);
			List<TransactionJpaEntity> plainTextTransactions = transactionJpaRepository.findPlainTextTransactions(
				pageable);
			processedCount = plainTextTransactions.size();

			if (processedCount > 0) {
				migrateTransactionBatch(plainTextTransactions);
				totalProcessed += processedCount;
				log.info("거래 내역 배치 처리 완료: {} 건, 총 처리: {} 건", processedCount, totalProcessed);
			}

			iteration++;
			if (iteration >= maxIterations) {
				log.error("거래 내역 마이그레이션 최대 반복 횟수({}) 초과. 무한 루프 가능성. 중단합니다.", maxIterations);
				break;
			}
		} while (processedCount == BATCH_SIZE);

		log.info("거래 내역 데이터 마이그레이션 완료: {} 건", totalProcessed);
		return totalProcessed;
	}

	private void migrateMemberBatch(List<MemberJpaEntity> memberEntities) {
		for (MemberJpaEntity memberEntity : memberEntities) {
			try {
				migrateSingleMember(memberEntity);
			} catch (Exception e) {
				log.error("회원 ID {} 마이그레이션 실패", memberEntity.getId(), e);
			}
		}
	}

	private void migrateSingleMember(MemberJpaEntity memberEntity) {
		var member = memberEntity.toModel();
		boolean needsUpdate = false;

		if (isPlainText(member.getEmail()) && (member.getEmailHash() == null || member.getEmailHash().isEmpty())) {
			String originalEmail = member.getEmail();
			member.updateEmailForEncryption(
				originalEmail,
				hashService.hash(originalEmail)
			);
			needsUpdate = true;
		}

		if (isPlainText(member.getName()) && (member.getNameHash() == null || member.getNameHash().isEmpty())) {
			String originalName = member.getName();
			member.updateNameForEncryption(
				originalName,
				hashService.hash(originalName)
			);
			needsUpdate = true;
		}

		if (member.getBirthday() != null && (member.getBirthdayHash() == null || member.getBirthdayHash().isEmpty())) {
			String birthdayStr = member.getBirthday().toString();
			member.updateBirthdayHashForEncryption(hashService.hash(birthdayStr));
			needsUpdate = true;
		}

		if (member.getGender() != null && isPlainText(member.getGender()) && (member.getGenderHash() == null
			|| member.getGenderHash().isEmpty())) {
			String originalGender = member.getGender();
			member.updateGenderForEncryption(
				originalGender,
				hashService.hash(originalGender)
			);
			needsUpdate = true;
		}

		if (needsUpdate) {
			memberJpaRepository.save(MemberJpaEntity.from(member));
			log.debug("회원 ID {} 마이그레이션 완료", member.getId());
		}
	}

	private void migrateSocialConnectionBatch(List<SocialConnectionJpaEntity> socialConnectionEntities) {
		for (SocialConnectionJpaEntity connectionEntity : socialConnectionEntities) {
			try {
				migrateSingleSocialConnection(connectionEntity);
			} catch (Exception e) {
				log.error("소셜 연결 ID {} 마이그레이션 실패", connectionEntity.getId(), e);
			}
		}
	}

	private void migrateSingleSocialConnection(SocialConnectionJpaEntity connectionEntity) {
		var socialConnection = connectionEntity.toModel();
		boolean needsUpdate = false;

		if (isPlainText(socialConnection.getSocialEmail()) && (socialConnection.getSocialEmailHash() == null
			|| socialConnection.getSocialEmailHash().isEmpty())) {
			String originalEmail = socialConnection.getSocialEmail();
			socialConnection.updateSocialEmailForEncryption(
				originalEmail,
				hashService.hash(originalEmail)
			);
			needsUpdate = true;
		}

		if (isPlainText(socialConnection.getSocialId()) && (socialConnection.getSocialIdHash() == null
			|| socialConnection.getSocialIdHash().isEmpty())) {
			String originalId = socialConnection.getSocialId();
			socialConnection.updateSocialIdForEncryption(hashService.hash(originalId));
			needsUpdate = true;
		}

		if (needsUpdate) {
			socialConnectionJpaRepository.save(SocialConnectionJpaEntity.from(socialConnection));
			log.debug("소셜 연결 ID {} 마이그레이션 완료", socialConnection.getId());
		}
	}

	private void migrateTransactionBatch(List<TransactionJpaEntity> transactionEntities) {
		for (TransactionJpaEntity transactionEntity : transactionEntities) {
			try {
				migrateSingleTransaction(transactionEntity);
			} catch (Exception e) {
				log.error("거래 내역 ID {} 마이그레이션 실패", transactionEntity.getId(), e);
			}
		}
	}

	private void migrateSingleTransaction(TransactionJpaEntity transactionEntity) {
		var transaction = transactionEntity.toModel();
		boolean needsUpdate = transaction.getContent() != null && isPlainText(transaction.getContent());

		if (transaction.getAmount() != null && isPlainText(String.valueOf(transaction.getAmount()))) {
			needsUpdate = true;
		}

		if (needsUpdate) {
			transactionJpaRepository.save(TransactionJpaEntity.from(transaction));
			log.debug("거래 내역 ID {} 마이그레이션 완료", transaction.getId());
		}
	}

	private boolean isPlainText(String value) {
		if (value == null || value.length() < 150) {
			return true;
		}

		try {
			java.util.Base64.getDecoder().decode(value);

			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

}
