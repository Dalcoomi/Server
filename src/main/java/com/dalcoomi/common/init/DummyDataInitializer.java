package com.dalcoomi.common.init;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.category.domain.OwnerType;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 성능 테스트를 위한 더미 데이터 자동 생성
 * 애플리케이션 시작 시 자동으로 10만 건의 거래 내역을 생성합니다.
 * local 프로파일에서만 동작합니다.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DummyDataInitializer implements ApplicationRunner {

	private static final int TOTAL_COUNT = 100_000;
	private static final int BATCH_SIZE = 1000;
	private static final int MEMBER_COUNT = 100; // 100명의 회원 생성

	private final TransactionRepository transactionRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		// 이미 더미 데이터가 있으면 스킵
		if (true) {
			log.info("더미 데이터가 이미 존재합니다. 생성을 건너뜁니다.");
			return;
		}

		log.info("=== 성능 테스트용 더미 데이터 생성 시작 ===");
		log.info("생성할 데이터: {}건", TOTAL_COUNT);

		long startTime = System.currentTimeMillis();

		try {
			// 1. 여러 더미 회원 생성 (100명)
			List<Member> dummyMembers = new ArrayList<>();
			List<Category> dummyCategories = new ArrayList<>();

			for (int i = 0; i < MEMBER_COUNT; i++) {
				Member member = createDummyMember(i);
				dummyMembers.add(member);

				Category category = createDummyCategory(member, i);
				dummyCategories.add(category);
			}
			log.info("더미 회원 {}명 생성 완료", MEMBER_COUNT);

			// 2. 대량 거래 내역 생성 (배치 처리 - 회원별로 분산)
			Random random = new SecureRandom();

			for (int batch = 0; batch < TOTAL_COUNT / BATCH_SIZE; batch++) {
				List<Transaction> transactions = new ArrayList<>();

				for (int i = 0; i < BATCH_SIZE; i++) {
					// 랜덤하게 회원 선택
					int memberIndex = random.nextInt(MEMBER_COUNT);
					Member member = dummyMembers.get(memberIndex);
					Category category = dummyCategories.get(memberIndex);

					Transaction transaction = createRandomTransaction(member, category, random);
					transactions.add(transaction);
				}

				// 배치 저장
				transactionRepository.saveAll(transactions);

				int progress = (batch + 1) * BATCH_SIZE;
				if (progress % 10000 == 0) {
					log.info("진행 상황: {}/{} ({}%)", progress, TOTAL_COUNT, (progress * 100 / TOTAL_COUNT));
				}
			}

			long elapsed = System.currentTimeMillis() - startTime;
			log.info("=== 더미 데이터 생성 완료 ===");
			log.info("소요 시간: {}ms ({}초)", elapsed, elapsed / 1000);
			log.info("초당 처리: {}건/s", TOTAL_COUNT * 1000 / elapsed);

		} catch (Exception e) {
			log.error("더미 데이터 생성 실패", e);
		}
	}

	private Member createDummyMember(int index) {
		Member member = Member.builder()
			.email(String.format("test-user-%d@dalcoomi.com", index))
			.name("테스트" + index)
			.nickname("유저" + index)
			.birthday(LocalDate.of(1990, 1, 1).plusDays(index))
			.profileImageUrl("https://dalcoomi.com/default.jpg")
			.serviceAgreement(true)
			.collectionAgreement(true)
			.build();

		return memberRepository.save(member);
	}

	private Category createDummyCategory(Member member, int index) {
		Category category = Category.builder()
			.creator(member)
			.teamId(null)
			.name("카테고리" + (index % 10)) // 카테고리는 10개만 순환
			.iconUrl("https://dalcoomi.com/icons/test.png")
			.isActive(true)
			.transactionType(TransactionType.EXPENSE)
			.ownerType(OwnerType.MEMBER)
			.build();

		return categoryRepository.save(category);
	}

	private Transaction createRandomTransaction(Member member, Category category, Random random) {
		// 랜덤 금액 (1,000 ~ 100,000원)
		long amount = (long)1000 + random.nextInt(99000);

		// 랜덤 날짜 (최근 2년)
		LocalDateTime randomDate = LocalDateTime.now()
			.minusDays(ThreadLocalRandom.current().nextInt(0, 730))
			.minusHours(ThreadLocalRandom.current().nextInt(0, 24))
			.minusMinutes(ThreadLocalRandom.current().nextInt(0, 60));

		// 랜덤 타입 (70% EXPENSE, 30% INCOME)
		TransactionType type = random.nextDouble() > 0.3
			? TransactionType.EXPENSE
			: TransactionType.INCOME;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.teamId(null)
			.transactionDate(randomDate)
			.content("성능테스트 거래")
			.amount(amount)
			.transactionType(type)
			.dataRetentionConsent(false)
			.build();
	}
}
