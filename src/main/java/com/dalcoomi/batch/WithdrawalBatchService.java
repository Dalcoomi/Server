package com.dalcoomi.batch;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalBatchService {

	private final SocialConnectionRepository socialConnectionRepository;
	private final TransactionRepository transactionRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void cleanupExpiredWithdrawalData() {
		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
		log.info("휴면 탈퇴 데이터 정리 배치 시작. 기준일: {}", cutoffDate);

		// 90일 경과한 휴면 탈퇴 회원들의 개인 거래 내역 처리
		processExpiredPersonalTransactions(cutoffDate);

		// 90일 경과한 휴면 탈퇴 회원들의 소셜 연결 및 회원 정보 삭제
		processExpiredSocialConnectionsAndMembers(cutoffDate);

		log.info("휴면 탈퇴 데이터 정리 배치 완료");
	}

	private void processExpiredPersonalTransactions(LocalDateTime cutoffDate) {
		// 90일 경과한 소프트 삭제된 개인 거래 내역 조회
		List<Transaction> expiredPersonalTransactions = transactionRepository.findExpiredPersonalTransactions(
			cutoffDate);

		// 동의한 거래 내역: 익명화 처리
		List<Transaction> consentedTransactions = expiredPersonalTransactions.stream()
			.filter(t -> TRUE.equals(t.getDataRetentionConsent()))
			.toList();

		for (Transaction transaction : consentedTransactions) {
			transaction.anonymize();
		}

		if (!consentedTransactions.isEmpty()) {
			transactionRepository.saveAll(consentedTransactions);
		}

		// 동의하지 않은 거래 내역: 완전 삭제
		List<Transaction> nonConsentedTransactions = expiredPersonalTransactions.stream()
			.filter(t -> FALSE.equals(t.getDataRetentionConsent()))
			.toList();

		if (!nonConsentedTransactions.isEmpty()) {
			transactionRepository.deleteAll(nonConsentedTransactions);
		}

		log.info("개인 거래 내역 처리 완료. 익명화: {}건, 삭제: {}건",
			consentedTransactions.size(), nonConsentedTransactions.size());
	}

	private void processExpiredSocialConnectionsAndMembers(LocalDateTime cutoffDate) {
		// 소셜 연결과 회원 정보 조회
		List<SocialConnection> expiredSocialConnections = socialConnectionRepository.findExpiredSoftDeletedWithMember(
			cutoffDate);

		if (!expiredSocialConnections.isEmpty()) {
			// 소셜 연결 삭제
			socialConnectionRepository.deleteAll(expiredSocialConnections);

			// 회원 정보도 함께 삭제
			List<Member> expiredMembers = expiredSocialConnections.stream()
				.map(SocialConnection::getMember)
				.distinct()
				.toList();

			memberRepository.deleteAll(expiredMembers);

			log.info("소셜 연결 및 회원 정보 삭제 완료: 소셜연결 {}건, 회원 {}건",
				expiredSocialConnections.size(), expiredMembers.size());
		}
	}
}
