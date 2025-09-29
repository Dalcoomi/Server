package com.dalcoomi.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.fixture.CategoryFixture;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.fixture.TransactionFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class TransactionBatchServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private TransactionBatchService transactionBatchService;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	@DisplayName("5년 경과한 익명화된 개인 거래 내역 삭제")
	void delete_expired_anonymized_data_5_years_expired_transactions_deleted() {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);
		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Transaction anonymizedTransaction = TransactionFixture.getAnonymizedPersonalTransaction1(category, 5 * 365 + 1);
		anonymizedTransaction = transactionRepository.save(anonymizedTransaction);
		Long transactionId = anonymizedTransaction.getId();

		// updatedAt을 5년 전으로 설정 (익명화된 시점을 5년 전으로 시뮬레이션)
		LocalDateTime fiveYearsAgo = LocalDateTime.now().minusYears(5).minusDays(1);
		updateTransactionUpdatedAt(transactionId, fiveYearsAgo);

		// when
		transactionBatchService.deleteExpiredAnonymizedData();

		// then
		List<Transaction> remainingTransactions = transactionRepository.findAll();
		boolean exists = remainingTransactions.stream().anyMatch(t -> t.getId().equals(transactionId));
		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("5년 미만 경과한 익명화된 거래 내역은 삭제하지 않음")
	void delete_expired_anonymized_data_not_expired_transactions_not_deleted() {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);
		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Transaction anonymizedTransaction = TransactionFixture.getAnonymizedPersonalTransaction1(category, 5 * 365 - 1);
		anonymizedTransaction = transactionRepository.save(anonymizedTransaction);
		Long transactionId = anonymizedTransaction.getId();

		// updatedAt을 4년 전으로 설정 (아직 5년이 지나지 않음)
		LocalDateTime fourYearsAgo = LocalDateTime.now().minusYears(4);
		updateTransactionUpdatedAt(transactionId, fourYearsAgo);

		// when
		transactionBatchService.deleteExpiredAnonymizedData();

		// then
		List<Transaction> remainingTransactions = transactionRepository.findAll();
		boolean exists = remainingTransactions.stream().anyMatch(t -> t.getId().equals(transactionId));
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("비익명화 거래 내역이나 그룹 거래 내역은 삭제하지 않음")
	void delete_expired_anonymized_data_non_anonymized_or_team_transactions_not_deleted() {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);
		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Transaction nonAnonymizedTransaction = TransactionFixture.getNonAnonymizedPersonalTransaction1(member, category,
			5 * 365 + 1);
		nonAnonymizedTransaction = transactionRepository.save(nonAnonymizedTransaction);
		Long nonAnonymizedId = nonAnonymizedTransaction.getId();

		Transaction anonymizedTeamTransaction = TransactionFixture.getAnonymizedTeamTransaction1(category, 5 * 365 + 1);
		anonymizedTeamTransaction = transactionRepository.save(anonymizedTeamTransaction);
		Long teamTransactionId = anonymizedTeamTransaction.getId();

		// 익명화된 그룹 거래도 5년 전으로 설정 (하지만 그룹 거래는 삭제되지 않아야 함)
		LocalDateTime fiveYearsAgo = LocalDateTime.now().minusYears(5).minusDays(1);
		updateTransactionUpdatedAt(teamTransactionId, fiveYearsAgo);

		// when
		transactionBatchService.deleteExpiredAnonymizedData();

		// then
		List<Transaction> remainingTransactions = transactionRepository.findAll();
		boolean nonAnonymizedExists = remainingTransactions.stream()
			.anyMatch(t -> t.getId().equals(nonAnonymizedId));
		boolean teamTransactionExists = remainingTransactions.stream()
			.anyMatch(t -> t.getId().equals(teamTransactionId));

		assertThat(nonAnonymizedExists).isTrue();
		assertThat(teamTransactionExists).isTrue();
	}

	private void updateTransactionUpdatedAt(Long transactionId, LocalDateTime updatedAt) {
		entityManager.createQuery(
				"UPDATE TransactionJpaEntity t SET t.updatedAt = :updatedAt WHERE t.id = :id")
			.setParameter("updatedAt", updatedAt)
			.setParameter("id", transactionId)
			.executeUpdate();
		entityManager.flush();
	}
}
