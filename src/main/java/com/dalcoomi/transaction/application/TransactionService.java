package com.dalcoomi.transaction.application;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_NOT_FOUND;
import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_CREATOR_INCONSISTENCY;
import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_TEAM_INCONSISTENCY;
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
import com.fasterxml.jackson.core.JsonProcessingException;
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
	public void createTransaction(Long memberId, Long categoryId, Transaction transaction) {
		validateTeamMember(transaction.getTeamId(), memberId);

		Member member = memberRepository.findById(memberId);
		Category category = categoryRepository.findById(categoryId);

		transaction.updateCreator(member);
		transaction.updateCategory(category);

		transactionRepository.save(transaction);
	}

	@Transactional
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
		} catch (JsonProcessingException je) {
			log.error("데이터 매핑 오류", je);

			throw new DalcoomiException("영수증 처리 중 오류가 발생했습니다.", je);
		}
	}

	@Transactional(readOnly = true)
	public TransactionsInfo getTransactions(TransactionSearchCriteria criteria) {
		validateTeamMember(criteria.teamId(), criteria.requesterId());

		List<Transaction> transactions = transactionRepository.findTransactions(criteria);

		return TransactionsInfo.from(transactions);
	}

	@Transactional(readOnly = true)
	public Transaction getTransaction(Long memberId, Long transactionId, @Nullable Long teamId) {
		validateTeamMember(teamId, memberId);

		Transaction transaction = transactionRepository.findById(transactionId);

		validateTransactionTeam(transaction, teamId);

		if (teamId == null) {
			validateTransactionCreator(transaction, memberId);
		}

		return transaction;
	}

	@Transactional
	public void updateTransaction(Long memberId, Long transactionId, Long categoryId, Transaction transaction) {
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
	public void deleteTransaction(Long memberId, Long transactionId, @Nullable Long teamId) {
		validateTeamMember(teamId, memberId);

		Transaction transaction = transactionRepository.findById(transactionId);

		validateTransactionTeam(transaction, teamId);

		validateTransactionCreator(transaction, memberId);

		transaction.softDelete();

		transactionRepository.save(transaction);
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
}
