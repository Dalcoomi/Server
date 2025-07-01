package com.dalcoomi.transaction.domain.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.dalcoomi.transaction.domain.Transaction;

import lombok.Getter;

@Getter
public class TransactionCreatedEvent extends ApplicationEvent {

	private final String taskId;
	private final transient List<Transaction> transactions;

	public TransactionCreatedEvent(Object source, String taskId, List<Transaction> transactions) {
		super(source);
		this.taskId = taskId;
		this.transactions = transactions;
	}
}
