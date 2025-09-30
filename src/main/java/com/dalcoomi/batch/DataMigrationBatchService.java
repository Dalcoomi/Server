package com.dalcoomi.batch;

import java.util.Base64;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

	public void migratePlainTextData() {
		log.info("===== 평문 데이터 암호화 마이그레이션 시작 =====");

		try {
			int totalProcessed = 0;

			log.info("회원 데이터 마이그레이션 시작");
			totalProcessed += migrateMemberData();

			log.info("소셜 연결 데이터 마이그레이션 시작");
			totalProcessed += migrateSocialConnectionData();

			log.info("거래 내역 데이터 마이그레이션 시작");
			totalProcessed += migrateTransactionData();

			log.info("평문 데이터 암호화 마이그레이션 완료. 총 {} 건 처리", totalProcessed);
		} catch (Exception e) {
			log.error("평문 데이터 마이그레이션 중 오류 발생", e);

			throw e;
		}
	}

	private int migrateMemberData() {
		int processedCount;
		int totalProcessed = 0;

		do {
			Pageable pageable = PageRequest.of(0, BATCH_SIZE);
			List<MemberJpaEntity> plainTextMembers = memberJpaRepository.findAll(pageable).getContent();
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
		int processedCount;
		int totalProcessed = 0;

		do {
			Pageable pageable = PageRequest.of(0, BATCH_SIZE);
			List<SocialConnectionJpaEntity> plainTextConnections
				= socialConnectionJpaRepository.findAll(pageable).getContent();
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
		int processedCount;
		int totalProcessed = 0;
		int maxIterations = 2;
		int iteration = 0;

		do {
			Pageable pageable = PageRequest.of(0, BATCH_SIZE);
			List<TransactionJpaEntity> plainTextTransactions = transactionJpaRepository.findAll(pageable).getContent();
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
				var member = memberEntity.toModel();
				boolean needsUpdate = false;

				if (isPlainText(member.getEmail()) && (member.getEmailHash().isBlank())) {
					String originalEmail = member.getEmail();

					member.updateEmailForEncryption(originalEmail, hashService.hash(originalEmail));

					needsUpdate = true;
				}

				if (isPlainText(member.getName()) && (member.getNameHash().isBlank())) {
					String originalName = member.getName();

					member.updateNameForEncryption(originalName, hashService.hash(originalName));

					needsUpdate = true;
				}

				if (member.getBirthdayHash().isBlank()) {
					String birthdayStr = member.getBirthday().toString();

					member.updateBirthdayHashForEncryption(hashService.hash(birthdayStr));

					needsUpdate = true;
				}

				if (member.getGenderHash().isBlank()) {
					String originalGender = member.getGender();

					member.updateGenderForEncryption(originalGender, hashService.hash(originalGender));

					needsUpdate = true;
				}

				if (needsUpdate) {
					memberJpaRepository.save(MemberJpaEntity.from(member));

					log.debug("회원 ID {} 마이그레이션 완료", member.getId());
				}
			} catch (Exception e) {
				log.error("회원 ID {} 마이그레이션 실패", memberEntity.getId(), e);
			}
		}
	}

	private void migrateSocialConnectionBatch(List<SocialConnectionJpaEntity> socialConnectionEntities) {
		for (SocialConnectionJpaEntity connectionEntity : socialConnectionEntities) {
			try {
				var socialConnection = connectionEntity.toModel();
				boolean needsUpdate = false;

				if (isPlainText(socialConnection.getSocialEmail()) && (socialConnection.getSocialEmailHash()
					.isBlank())) {
					String originalEmail = socialConnection.getSocialEmail();

					socialConnection.updateSocialEmailForEncryption(originalEmail, hashService.hash(originalEmail));

					needsUpdate = true;
				}

				if (isPlainText(socialConnection.getSocialId()) && (socialConnection.getSocialIdHash().isBlank())) {
					String originalId = socialConnection.getSocialId();

					socialConnection.updateSocialIdForEncryption(hashService.hash(originalId));

					needsUpdate = true;
				}

				if (needsUpdate) {
					socialConnectionJpaRepository.save(SocialConnectionJpaEntity.from(socialConnection));

					log.debug("소셜 연결 ID {} 마이그레이션 완료", socialConnection.getId());
				}
			} catch (Exception e) {
				log.error("소셜 연결 ID {} 마이그레이션 실패", connectionEntity.getId(), e);
			}
		}
	}

	private void migrateTransactionBatch(List<TransactionJpaEntity> transactionEntities) {
		for (TransactionJpaEntity transactionEntity : transactionEntities) {
			try {
				var transaction = transactionEntity.toModel();
				boolean needsUpdate = transaction.getContent() != null && isPlainText(transaction.getContent());

				if (transaction.getAmount() != null && isPlainText(String.valueOf(transaction.getAmount()))) {
					needsUpdate = true;
				}

				if (needsUpdate) {
					transactionJpaRepository.save(TransactionJpaEntity.from(transaction));

					log.debug("거래 내역 ID {} 마이그레이션 완료", transaction.getId());
				}
			} catch (Exception e) {
				log.error("거래 내역 ID {} 마이그레이션 실패", transactionEntity.getId(), e);
			}
		}
	}

	private boolean isPlainText(String value) {
		if (value == null) {
			return true;
		}

		try {
			Base64.getDecoder().decode(value);

			return false;
		} catch (Exception e) {
			log.error("복호화 실패", e);

			return true;
		}
	}
}
