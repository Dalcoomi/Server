package com.dalcoomi.batch;

import static org.springframework.util.StringUtils.hasText;

import java.util.Base64;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dalcoomi.common.encryption.HashService;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
import com.dalcoomi.member.infrastructure.MemberJpaRepository;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationBatchService {

	private static final int BATCH_SIZE = 500;
	private final MemberJpaRepository memberJpaRepository;
	private final SocialConnectionRepository socialConnectionRepository;
	private final TransactionRepository transactionRepository;
	private final HashService hashService;

	public void migratePlainTextData() {
		log.info("===== 평문 데이터 암호화 마이그레이션 시작 =====");

		try {
			int totalProcessed = 0;

			log.info("==== 회원 데이터 마이그레이션 시작 ====");
			totalProcessed += migrateMemberData();

			log.info("==== 소셜 연결 데이터 마이그레이션 시작 ====");
			totalProcessed += migrateSocialConnectionData();

			log.info("==== 거래 내역 데이터 마이그레이션 시작 ====");
			totalProcessed += migrateTransactionData();

			log.info("==== 평문 데이터 암호화 마이그레이션 완료. 총 {} 건 처리 ====", totalProcessed);
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
			List<SocialConnection> plainTextConnections = socialConnectionRepository.findAll(pageable).getContent();
			processedCount = plainTextConnections.size();

			if (processedCount > 0) {
				migrateSocialConnectionBatch(plainTextConnections);

				totalProcessed += processedCount;
			}
		} while (processedCount == BATCH_SIZE);
		log.info("소셜 연결 데이터 마이그레이션 완료: {} 건", totalProcessed);

		return totalProcessed;
	}

	private int migrateTransactionData() {
		int processedCount;
		int totalProcessed = 0;
		int page = 0;

		do {
			Pageable pageable = PageRequest.of(page, BATCH_SIZE);
			List<Transaction> plainTextTransactions = transactionRepository.findAll(pageable).getContent();
			processedCount = plainTextTransactions.size();

			if (processedCount > 0) {
				migrateTransactionBatch(plainTextTransactions);

				totalProcessed += processedCount;
				page++;
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

				if (isPlainText(member.getEmail()) && !hasText(member.getEmailHash())) {
					String originalEmail = member.getEmail();

					member.updateEmailForEncryption(originalEmail, hashService.hash(originalEmail));

					needsUpdate = true;
				}

				if (isPlainText(member.getName()) && !hasText(member.getNameHash())) {
					String originalName = member.getName();

					member.updateNameForEncryption(originalName, hashService.hash(originalName));

					needsUpdate = true;
				}

				if (!hasText(member.getBirthdayHash())) {
					String birthdayStr = member.getBirthday().toString();

					member.updateBirthdayHashForEncryption(hashService.hash(birthdayStr));

					needsUpdate = true;
				}

				if (!hasText(member.getGenderHash())) {
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

	private void migrateSocialConnectionBatch(List<SocialConnection> socialConnections) {
		for (SocialConnection socialConnection : socialConnections) {
			try {
				boolean needsUpdate = false;

				if (isPlainText(socialConnection.getSocialEmail()) && !hasText(socialConnection.getSocialEmailHash())) {
					String originalEmail = socialConnection.getSocialEmail();

					socialConnection.updateSocialEmailForEncryption(originalEmail, hashService.hash(originalEmail));

					needsUpdate = true;
				}

				if (isPlainText(socialConnection.getSocialId()) && !hasText(socialConnection.getSocialIdHash())) {
					String originalId = socialConnection.getSocialId();

					socialConnection.updateSocialIdForEncryption(hashService.hash(originalId));

					needsUpdate = true;
				}

				if (needsUpdate) {
					socialConnectionRepository.save(socialConnection);

					log.info("소셜 연결 ID {} 마이그레이션 완료", socialConnection.getId());
				}
			} catch (Exception e) {
				log.error("소셜 연결 ID {} 마이그레이션 실패", socialConnection.getId(), e);
			}
		}
	}

	private void migrateTransactionBatch(List<Transaction> transactions) {
		for (Transaction transaction : transactions) {
			try {
				boolean needsUpdate = transaction.getContent() != null && isPlainText(transaction.getContent());

				if (transaction.getAmount() != null && isPlainText(String.valueOf(transaction.getAmount()))) {
					needsUpdate = true;
				}

				if (needsUpdate) {
					transactionRepository.save(transaction);

					log.debug("거래 내역 ID {} 마이그레이션 완료", transaction.getId());
				}
			} catch (Exception e) {
				log.error("거래 내역 ID {} 마이그레이션 실패", transaction.getId(), e);
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
			return true;
		}
	}
}
