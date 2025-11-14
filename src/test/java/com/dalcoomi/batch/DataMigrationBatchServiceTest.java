package com.dalcoomi.batch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.common.encryption.EncryptionService;

import jakarta.persistence.EntityManager;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class DataMigrationBatchServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private DataMigrationBatchService dataMigrationBatchService;

	@Autowired
	private EncryptionService encryptionService;

	@Autowired
	private EntityManager entityManager;

	@Test
	@DisplayName("평문 회원 데이터 암호화 마이그레이션 성공")
	void migrate_plain_text_data_member_data_encrypted_success() {
		// given
		Long memberId = 99999999L;
		String plainEmail = "test@example.com";
		String plainName = "testuser";
		String plainGender = "M";
		String plainBirthday = "2000-01-01";

		entityManager.createNativeQuery("""
				INSERT INTO member (id, email, email_hash, name, name_hash, nickname, birthday, birthday_hash,
				gender, gender_hash, profile_image_url, service_agreement, collection_agreement,
				ai_learning_agreement, created_at, updated_at)
				VALUES (?, ?, '', ?, '', ?, ?, '', ?, '', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
				""")
			.setParameter(1, memberId)
			.setParameter(2, plainEmail)
			.setParameter(3, plainName)
			.setParameter(4, "testuser")
			.setParameter(5, plainBirthday)
			.setParameter(6, plainGender)
			.setParameter(7, "https://example.com/profile.jpg")
			.setParameter(8, true)
			.setParameter(9, true)
			.setParameter(10, false)
			.executeUpdate();

		// when
		dataMigrationBatchService.migratePlainTextData();

		// 배치 실행 후 DB에서 암호화된 값 직접 확인
		String encryptedName = (String)entityManager.createNativeQuery("SELECT name FROM member WHERE id = ?")
			.setParameter(1, memberId)
			.getSingleResult();

		// then
		assertThat(encryptionService.decrypt(encryptedName)).isEqualTo(plainName);
	}

	@Test
	@DisplayName("평문 소셜 연결 데이터 암호화 마이그레이션 성공")
	void migratePlainTextData_social_connection_data_encrypted_success() {
		// given
		Long memberId = 99999998L;
		Long socialConnectionId = 99999997L;
		String plainSocialEmail = "social@example.com";
		String plainSocialId = "social123";

		entityManager.createNativeQuery("""
				INSERT INTO member (id, email, email_hash, name, name_hash, nickname, birthday, birthday_hash,
				gender, gender_hash, profile_image_url, service_agreement, collection_agreement,
				ai_learning_agreement, created_at, updated_at)
				VALUES (?, ?, '', ?, '', ?, ?, '', ?, '', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
				""")
			.setParameter(1, memberId)
			.setParameter(2, "test@example.com")
			.setParameter(3, "testu")
			.setParameter(4, "test")
			.setParameter(5, "2000-01-01")
			.setParameter(6, "M")
			.setParameter(7, "https://example.com/profile.jpg")
			.setParameter(8, true)
			.setParameter(9, true)
			.setParameter(10, false)
			.executeUpdate();

		entityManager.createNativeQuery("""
				INSERT INTO social_connection (id, member_id, social_type, social_id, social_id_hash,
				social_email, social_email_hash, social_refresh_token ,created_at, updated_at)
				VALUES (?, ?, ?, ?, '', ?, '', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
				""")
			.setParameter(1, socialConnectionId)
			.setParameter(2, memberId)
			.setParameter(3, "NAVER")
			.setParameter(4, plainSocialId)
			.setParameter(5, plainSocialEmail)
			.setParameter(6, "eqwewq")
			.executeUpdate();

		// when
		dataMigrationBatchService.migratePlainTextData();

		// then
		String encryptedSocialEmail = (String)entityManager.createNativeQuery(
				"SELECT social_email FROM social_connection WHERE id = ?")
			.setParameter(1, socialConnectionId)
			.getSingleResult();

		assertThat(encryptionService.decrypt(encryptedSocialEmail)).isEqualTo(plainSocialEmail);
	}

	@Test
	@DisplayName("평문 거래 내역 데이터 암호화 마이그레이션 성공")
	void migratePlainTextData_transaction_data_encrypted_success() {
		// given
		Long memberId = 99999996L;
		Long categoryId = 99999995L;
		Long transactionId = 99999994L;
		String plainContent = "test transaction";

		entityManager.createNativeQuery("""
				INSERT INTO member (id, email, email_hash, name, name_hash, nickname, birthday, birthday_hash,
				gender, gender_hash,profile_image_url, service_agreement, collection_agreement,
				ai_learning_agreement, created_at, updated_at)
				VALUES (?, ?, '', ?, '', ?, ?, '', ?, '', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
				""")
			.setParameter(1, memberId)
			.setParameter(2, "test@example.com")
			.setParameter(3, "testuser")
			.setParameter(4, "testuser")
			.setParameter(5, "2000-01-01")
			.setParameter(6, "M")
			.setParameter(7, "https://example.com/profile.jpg")
			.setParameter(8, true)
			.setParameter(9, true)
			.setParameter(10, false)
			.executeUpdate();

		entityManager.createNativeQuery("""
				INSERT INTO category (id, creator_id, name, icon_url, is_active, owner_type, transaction_type,
				created_at, updated_at)
				VALUES (?, ?, ?, ?, ?,?,?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
				""")
			.setParameter(1, categoryId)
			.setParameter(2, memberId)
			.setParameter(3, "category")
			.setParameter(4, "icon")
			.setParameter(5, true)
			.setParameter(6, "ADMIN")
			.setParameter(7, "EXPENSE")
			.executeUpdate();

		entityManager.createNativeQuery("""
				INSERT INTO transaction (id, creator_id, category_id, transaction_type, amount, content,
				transaction_date, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
				""")
			.setParameter(1, transactionId)
			.setParameter(2, memberId)
			.setParameter(3, categoryId)
			.setParameter(4, "EXPENSE")
			.setParameter(5, "1000")
			.setParameter(6, plainContent)
			.executeUpdate();

		// when
		dataMigrationBatchService.migratePlainTextData();

		// then
		String encryptedContent = (String)entityManager.createNativeQuery(
				"SELECT content FROM transaction WHERE id = ?")
			.setParameter(1, transactionId)
			.getSingleResult();

		assertThat(encryptionService.decrypt(encryptedContent)).isEqualTo(plainContent);
	}
}
