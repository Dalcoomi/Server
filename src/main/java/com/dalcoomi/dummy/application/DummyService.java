package com.dalcoomi.dummy.application;

import static com.dalcoomi.category.domain.OwnerType.ADMIN;
import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static com.dalcoomi.transaction.domain.TransactionType.INCOME;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.dummy.dto.DummyDataResponse;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile({"local", "dev"})
@Service
@RequiredArgsConstructor
public class DummyService {

	private static final int BATCH_SIZE = 1000;
	private final Random random = new SecureRandom();

	private final TransactionRepository transactionRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	public DummyDataResponse generateDummyData(String type, int count) {
		try {
			return switch (type.toLowerCase()) {
				case "member" -> generateMember();
				case "transaction" -> generateTransactions(count);
				default -> new DummyDataResponse(false,
					"지원하지 않는 타입입니다. member, transaction 중 하나를 선택하세요.", type, 0, null);
			};
		} catch (Exception e) {
			log.error("더미 데이터 생성 실패 - type: {}, count: {}", type, count, e);

			return new DummyDataResponse(false, "더미 데이터 생성 실패: " + e.getMessage(), type, 0, null);
		}
	}

	private DummyDataResponse generateMember() {
		log.info("더미 회원 1명 생성 시작");

		// 중복되지 않는 인덱스 찾기
		int index = 0;

		while (memberRepository.existsByNickname("유저" + index)) {
			index++;
		}

		Member member = Member.builder()
			.email(String.format("test-user-%d@dalcoomi.com", index))
			.name("테스트" + index)
			.collectionAgreement(true)
			.nickname("유저" + index)
			.birthday(LocalDate.of(2000, 1, 1).plusDays(index))
			.profileImageUrl("https://dalcoomi.com/default.jpg")
			.serviceAgreement(true)
			.build();

		member = memberRepository.save(member);

		Map<String, Object> data = new HashMap<>();
		data.put("memberId", member.getId());
		data.put("email", member.getEmail());
		data.put("name", member.getName());
		data.put("nickname", member.getNickname());

		log.info("더미 회원 생성 완료 - nickname: {}", member.getNickname());

		return new DummyDataResponse(true, "회원 생성 완료", "member", 1, data);
	}

	private void generateCategory() {
		log.info("더미 카테고리 1개 생성 시작");

		// 회원이 없으면 먼저 생성
		List<Member> members = memberRepository.findAll();

		if (members.isEmpty()) {
			log.warn("회원이 없어서 먼저 회원을 생성합니다.");

			generateMember();

			members = memberRepository.findAll();
		}

		Member member = members.get(random.nextInt(members.size()));

		// 중복되지 않는 인덱스 찾기
		int index = 0;

		while (categoryRepository.existsByName("카테고리" + index)) {
			index++;
		}

		String categoryName = "카테고리" + index;

		Category category = Category.builder()
			.creator(member)
			.teamId(null)
			.name(categoryName)
			.iconUrl("https://dalcoomi.com/icons/test.png")
			.isActive(true)
			.transactionType(EXPENSE)
			.ownerType(ADMIN)
			.build();

		category = categoryRepository.save(category);

		log.info("더미 카테고리 생성 완료 - name: {}", category.getName());
	}

	private DummyDataResponse generateTransactions(int count) {
		log.info("더미 거래 내역 {}개 생성 시작", count);

		// 회원이 없으면 먼저 생성
		List<Member> members = memberRepository.findAll();

		if (members.isEmpty()) {
			log.warn("회원이 없어서 먼저 회원을 생성합니다.");

			generateMember();

			members = memberRepository.findAll();
		}

		// ADMIN 오너타입 카테고리 가져오기
		List<Category> categories = categoryRepository.findAdminCategories(EXPENSE);

		if (categories.isEmpty()) {
			log.warn("카테고리가 없어서 먼저 카테고리를 생성합니다.");

			generateCategory();

			categories = categoryRepository.findAdminCategories(EXPENSE);

		}

		long startTime = System.currentTimeMillis();
		int totalCreated = 0;

		// 배치 처리
		int batches = (count + BATCH_SIZE - 1) / BATCH_SIZE;

		for (int batch = 0; batch < batches; batch++) {
			int batchSize = Math.min(BATCH_SIZE, count - totalCreated);
			List<Transaction> transactions = new ArrayList<>();

			for (int i = 0; i < batchSize; i++) {
				Member member = members.get(random.nextInt(members.size()));
				Category category = categories.get(random.nextInt(categories.size()));
				Transaction transaction = createRandomTransaction(member, category);

				transactions.add(transaction);
			}

			transactionRepository.saveAll(transactions);
			totalCreated += batchSize;

			if (totalCreated % 1000 == 0 || totalCreated == count) {
				log.info("진행 상황: {}/{} ({}%)", totalCreated, count, (totalCreated * 100 / count));
			}
		}

		long elapsed = System.currentTimeMillis() - startTime;

		Map<String, Object> data = new HashMap<>();
		data.put("elapsedTimeMs", elapsed);
		data.put("transactionsPerSecond", count * 1000L / Math.max(elapsed, 1));

		log.info("더미 거래 내역 생성 완료 - 총 {}개, 소요시간: {}ms", totalCreated, elapsed);

		return new DummyDataResponse(true, "거래 내역 생성 완료", "transaction", totalCreated, data);
	}

	private Transaction createRandomTransaction(Member member, Category category) {
		// 랜덤 금액 (1,000 ~ 100,000원)
		long amount = (long)1000 + random.nextInt(99000);

		// 랜덤 날짜 (최근 2년)
		LocalDateTime randomDate = LocalDateTime.now()
			.minusDays(ThreadLocalRandom.current().nextInt(0, 730))
			.minusHours(ThreadLocalRandom.current().nextInt(0, 24))
			.minusMinutes(ThreadLocalRandom.current().nextInt(0, 60));

		// 랜덤 타입 (70% EXPENSE, 30% INCOME)
		TransactionType type = random.nextDouble() > 0.3 ? EXPENSE : INCOME;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.teamId(null)
			.transactionDate(randomDate)
			.content("더미 거래")
			.amount(amount)
			.transactionType(type)
			.dataRetentionConsent(false)
			.build();
	}
}
