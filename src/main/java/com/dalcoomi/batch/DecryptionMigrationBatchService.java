package com.dalcoomi.batch;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.encryption.EncryptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecryptionMigrationBatchService {

	private final JdbcTemplate jdbcTemplate;
	private final EncryptionService encryptionService;

	@Transactional
	public void decryptAllData() {
		log.info("===== 암호화 데이터 → 평문 복호화 마이그레이션 시작 =====");

		int memberCount = decryptMemberData();
		int socialCount = decryptSocialConnectionData();
		int transactionCount = decryptTransactionData();

		log.info("===== 복호화 마이그레이션 완료. member={}, social={}, transaction={} =====",
			memberCount, socialCount, transactionCount);
	}

	private int decryptMemberData() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
			"SELECT id, email, name, birthday, gender FROM member"
		);

		int count = 0;

		for (Map<String, Object> row : rows) {
			try {
				Long id = ((Number)row.get("id")).longValue();
				String email = decryptSafe((String)row.get("email"));
				String name = decryptSafe((String)row.get("name"));
				String birthday = decryptSafe((String)row.get("birthday"));
				String gender = decryptSafe((String)row.get("gender"));

				jdbcTemplate.update(
					"UPDATE member SET email = ?, name = ?, birthday = ?, gender = ?, "
						+ "email_hash = '', name_hash = '', birthday_hash = '', gender_hash = '' "
						+ "WHERE id = ?",
					email, name, birthday, gender, id
				);

				count++;
			} catch (Exception e) {
				log.error("회원 ID {} 복호화 실패", row.get("id"), e);
			}
		}

		log.info("회원 데이터 복호화 완료: {} 건", count);

		return count;
	}

	private int decryptSocialConnectionData() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
			"SELECT id, social_email, social_id FROM social_connection"
		);

		int count = 0;

		for (Map<String, Object> row : rows) {
			try {
				Long id = ((Number)row.get("id")).longValue();
				String socialEmail = decryptSafe((String)row.get("social_email"));
				String socialId = decryptSafe((String)row.get("social_id"));

				jdbcTemplate.update(
					"UPDATE social_connection SET social_email = ?, social_id = ?, social_refresh_token = '', "
						+ "social_email_hash = '', social_id_hash = '' "
						+ "WHERE id = ?",
					socialEmail, socialId, id
				);

				count++;
			} catch (Exception e) {
				log.error("소셜 연결 ID {} 복호화 실패", row.get("id"), e);
			}
		}

		log.info("소셜 연결 데이터 복호화 완료: {} 건", count);

		return count;
	}

	private int decryptTransactionData() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
			"SELECT id, content, amount FROM transaction WHERE deleted_at IS NULL"
		);

		int count = 0;

		for (Map<String, Object> row : rows) {
			try {
				Long id = ((Number)row.get("id")).longValue();
				String content = decryptSafe((String)row.get("content"));
				Object amountRaw = row.get("amount");
				String amount = amountRaw instanceof Number
					? amountRaw.toString()
					: decryptSafe((String)amountRaw);

				jdbcTemplate.update(
					"UPDATE transaction SET content = ?, amount = ? WHERE id = ?",
					content, amount, id
				);

				count++;
			} catch (Exception e) {
				log.error("거래 내역 ID {} 복호화 실패", row.get("id"), e);
			}
		}

		log.info("거래 내역 데이터 복호화 완료: {} 건", count);

		return count;
	}

	private String decryptSafe(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}

		try {
			return encryptionService.decrypt(value);
		} catch (Exception e) {
			return value;
		}
	}
}
