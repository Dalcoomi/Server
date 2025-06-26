package com.dalcoomi.transaction.application;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_NOT_FOUND;
import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_CREATOR_INCONSISTENCY;
import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_TEAM_INCONSISTENCY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.common.error.exception.BadRequestException;
import com.dalcoomi.common.error.exception.DalcoomiException;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.ReceiptInfo;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;
import com.dalcoomi.transaction.dto.TransactionsInfo;
import com.dalcoomi.transaction.dto.request.SendBulkToAiServerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	@Value("${ai.server.url}")
	private String aiServerUrl;

	@Transactional
	public void create(Long memberId, Long categoryId, Transaction transaction) {
		validateTeamMember(transaction.getTeamId(), memberId);

		Member member = memberRepository.findById(memberId);
		Category category = categoryRepository.findById(categoryId);

		transaction.updateCreator(member);
		transaction.updateCategory(category);

		transactionRepository.save(transaction);
	}

	@Transactional
	public List<Transaction> create(Long memberId, List<Long> categoryIds, List<Transaction> transactions) {
		validateTeamMember(transactions.getFirst().getTeamId(), memberId);

		Member member = memberRepository.findById(memberId);

		List<Category> categories = categoryRepository.findAllById(categoryIds);

		if (categories.size() != transactions.size()) {
			throw new IllegalArgumentException("카테고리와 거래 내역의 개수가 일치하지 않습니다.");
		}

		for (int i = 0; i < transactions.size(); i++) {
			Transaction transaction = transactions.get(i);
			Category category = categories.get(i);

			transaction.updateCreator(member);
			transaction.updateCategory(category);
		}

		return transactionRepository.saveAll(transactions);
	}

	@Transactional(readOnly = true)
	public TransactionsInfo get(TransactionSearchCriteria criteria) {
		validateTeamMember(criteria.teamId(), criteria.requesterId());

		List<Transaction> transactions = transactionRepository.findTransactions(criteria);

		return TransactionsInfo.from(transactions);
	}

	@Transactional(readOnly = true)
	public Transaction get(Long memberId, Long transactionId, @Nullable Long teamId) {
		validateTeamMember(teamId, memberId);

		Transaction transaction = transactionRepository.findById(transactionId);

		validateTransactionTeam(transaction, teamId);

		if (teamId == null) {
			validateTransactionCreator(transaction, memberId);
		}

		return transaction;
	}

	@Transactional
	public void update(Long memberId, Long transactionId, Long categoryId, Transaction transaction) {
		Long teamId = transaction.getTeamId();

		validateTeamMember(teamId, memberId);

		Transaction currentTransaction = transactionRepository.findById(transactionId);

		validateTransactionTeam(currentTransaction, teamId);

		validateTransactionCreator(currentTransaction, memberId);

		Category category = categoryRepository.findById(categoryId);

		currentTransaction.updateCategory(category);
		currentTransaction.updateAmount(transaction.getAmount());
		currentTransaction.updateContent(transaction.getContent());
		currentTransaction.updateTransactionDate(transaction.getTransactionDate());
		currentTransaction.updateTransactionType(transaction.getTransactionType());

		transactionRepository.save(currentTransaction);
	}

	@Transactional
	public void delete(Long memberId, Long transactionId, @Nullable Long teamId) {
		validateTeamMember(teamId, memberId);

		Transaction transaction = transactionRepository.findById(transactionId);

		validateTransactionTeam(transaction, teamId);

		validateTransactionCreator(transaction, memberId);

		transaction.softDelete();

		transactionRepository.save(transaction);
	}

	public List<ReceiptInfo> analyseReceipt(MultipartFile receipt, List<String> categoryNames) {
		try {
			MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

			parts.add("receipt", receipt.getResource());
			parts.add("categories", objectMapper.writeValueAsString(categoryNames));

			String response = webClient.post()
				.uri(aiServerUrl + "/receipt")
				.contentType(MULTIPART_FORM_DATA)
				.bodyValue(parts)
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse -> {
					log.error("AI 서버 요청 실패: status={}", clientResponse.statusCode());

					return Mono.error(new RuntimeException("AI 서버 처리 중 오류가 발생했습니다."));
				})
				.bodyToMono(String.class)
				.block();

			log.info("AI 서버 응답: {}", response);

			CollectionType listType = objectMapper.getTypeFactory()
				.constructCollectionType(List.class, ReceiptInfo.class);

			return objectMapper.readValue(response, listType);
		} catch (Exception e) {
			log.error("데이터 매핑 오류", e);

			throw new DalcoomiException("영수증 처리 중 오류가 발생했습니다.", e);
		}
	}

	public void sendToAiServer(String taskId, List<Transaction> transactions) {
		String updatedTaskId = incrementTaskId(taskId);

		List<ReceiptInfo> transactionData = transactions.stream()
			.map(transaction -> ReceiptInfo.builder()
				.date(transaction.getTransactionDate().toLocalDate())
				.categoryName(transaction.getCategory().getName())
				.content(transaction.getContent())
				.amount(transaction.getAmount())
				.build())
			.toList();

		SendBulkToAiServerRequest request = SendBulkToAiServerRequest.builder()
			.taskId(updatedTaskId)
			.transactions(transactionData)
			.build();

		try {
			String response = webClient.post()
				.uri(aiServerUrl + "/transactions")
				.contentType(APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse -> {
					log.error("AI 서버 전송 실패: status={}", clientResponse.statusCode());

					return Mono.error(new RuntimeException("AI 서버 전송 중 오류가 발생했습니다."));
				})
				.bodyToMono(String.class)
				.block();

			log.info("AI 서버 전송 성공: taskId={}, response={}", updatedTaskId, response);
		} catch (Exception e) {
			log.error("AI 서버 전송 중 오류 발생: taskId={}", updatedTaskId, e);

			throw new DalcoomiException("AI 서버 전송 중 오류가 발생했습니다.", e);
		}
	}

	private void validateTeamMember(@Nullable Long teamId, Long memberId) {
		if (teamId == null) {
			return;
		}

		if (!teamMemberRepository.existsByTeamIdAndMemberId(teamId, memberId)) {
			throw new NotFoundException(TEAM_MEMBER_NOT_FOUND);
		}
	}

	private void validateTransactionTeam(Transaction transaction, @Nullable Long teamId) {
		if (teamId == null) {
			return;
		}

		if (!transaction.getTeamId().equals(teamId)) {
			throw new BadRequestException(TRANSACTION_TEAM_INCONSISTENCY);
		}
	}

	private void validateTransactionCreator(Transaction transaction, Long memberId) {
		if (!transaction.getCreator().getId().equals(memberId)) {
			throw new BadRequestException(TRANSACTION_CREATOR_INCONSISTENCY);
		}
	}

	private String incrementTaskId(String taskId) {
		int lastDashIndex = taskId.lastIndexOf('-');

		if (lastDashIndex == -1) {
			throw new IllegalArgumentException("잘못된 taskId 형식입니다: " + taskId);
		}

		String prefix = taskId.substring(0, lastDashIndex + 1);
		String numberPart = taskId.substring(lastDashIndex + 1);

		try {
			int currentNumber = Integer.parseInt(numberPart);
			int nextNumber = currentNumber + 1;

			return prefix + nextNumber;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("taskId의 숫자 부분을 파싱할 수 없습니다: " + taskId, e);
		}
	}
}
