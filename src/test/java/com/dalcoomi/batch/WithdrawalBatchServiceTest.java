package com.dalcoomi.batch;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.dalcoomi.fixture.SocialConnectionFixture;
import com.dalcoomi.fixture.TransactionFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class WithdrawalBatchServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private WithdrawalBatchService withdrawalBatchService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private SocialConnectionRepository socialConnectionRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	@DisplayName("90일 경과한 휴면 탈퇴 데이터 정리 - 데이터 보존 동의한 개인 거래 내역 익명화 성공")
	void cleanup_expired_withdrawal_data_with_consent_anonymized_success() {
		// given
		Member member = MemberFixture.getSoftDeletedMember1(91);
		member = memberRepository.save(member);
		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);
		SocialConnection socialConnection = SocialConnectionFixture.getSoftDeletedSocialConnection1(member, 91);
		socialConnectionRepository.save(socialConnection);
		Transaction personalTransaction = TransactionFixture.getSoftDeletedPersonalTransaction1(member, category, 91,
			true);
		personalTransaction = transactionRepository.save(personalTransaction);

		// when
		withdrawalBatchService.cleanupExpiredWithdrawalData();

		// then
		Transaction updatedTransaction = transactionRepository.findById(personalTransaction.getId());
		assertThat(updatedTransaction.getCreator()).isNull();
		assertThat(updatedTransaction.getDataRetentionConsent()).isTrue();

		List<SocialConnection> remainingSocialConnections = socialConnectionRepository.findByMemberId(member.getId());
		assertThat(remainingSocialConnections).isEmpty();

		Long memberId = member.getId();
		List<Member> remainingMembers = memberRepository.findAll();
		boolean memberExists = remainingMembers.stream().anyMatch(m -> m.getId().equals(memberId));
		assertThat(memberExists).isFalse();
	}

	@Test
	@DisplayName("90일 경과한 휴면 탈퇴 데이터 정리 - 데이터 보존 비동의한 개인 거래 내역 완전 삭제 성공")
	void cleanup_expired_withdrawal_data_without_consent_deleted_success() {
		// given
		Member member = MemberFixture.getSoftDeletedMember1(91);
		member = memberRepository.save(member);
		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);
		SocialConnection socialConnection = SocialConnectionFixture.getSoftDeletedSocialConnection1(member, 91);
		socialConnectionRepository.save(socialConnection);
		Transaction personalTransaction = TransactionFixture.getSoftDeletedPersonalTransaction1(member, category, 91,
			false);
		personalTransaction = transactionRepository.save(personalTransaction);

		// when
		withdrawalBatchService.cleanupExpiredWithdrawalData();

		// then
		Long transactionId = personalTransaction.getId();
		List<Transaction> remainingTransactions = transactionRepository.findAll();
		boolean transactionExists = remainingTransactions.stream().anyMatch(t -> t.getId().equals(transactionId));
		assertThat(transactionExists).isFalse();

		List<SocialConnection> remainingSocialConnections = socialConnectionRepository.findByMemberId(member.getId());
		assertThat(remainingSocialConnections).isEmpty();

		Long memberId = member.getId();
		List<Member> remainingMembers = memberRepository.findAll();
		boolean memberExists = remainingMembers.stream().anyMatch(m -> m.getId().equals(memberId));
		assertThat(memberExists).isFalse();
	}

	@Test
	@DisplayName("90일 미만 경과한 데이터는 처리하지 않기 성공")
	void cleanup_expired_withdrawal_data_not_expired_data_not_processed_success() {
		// given
		Member member = MemberFixture.getSoftDeletedMember1(89);
		member = memberRepository.save(member);
		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);
		SocialConnection socialConnection = SocialConnectionFixture.getSoftDeletedSocialConnection1(member, 89);
		socialConnectionRepository.save(socialConnection);
		Transaction personalTransaction = TransactionFixture.getSoftDeletedPersonalTransaction1(member, category, 89,
			true);
		personalTransaction = transactionRepository.save(personalTransaction);

		// when
		withdrawalBatchService.cleanupExpiredWithdrawalData();

		// then
		Transaction unchangedTransaction = transactionRepository.findById(personalTransaction.getId());
		assertThat(unchangedTransaction.getCreator()).isNotNull();
		assertThat(unchangedTransaction.getDeletedAt()).isNotNull();

		List<SocialConnection> unchangedSocialConnections = socialConnectionRepository.findByMemberId(member.getId());
		assertThat(unchangedSocialConnections).hasSize(1);

		Long memberId = member.getId();
		List<Member> unchangedMembers = memberRepository.findAll();
		boolean memberExists = unchangedMembers.stream().anyMatch(m -> m.getId().equals(memberId));
		assertThat(memberExists).isTrue();
	}
}
