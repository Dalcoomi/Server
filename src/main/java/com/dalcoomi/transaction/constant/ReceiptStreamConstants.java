package com.dalcoomi.transaction.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReceiptStreamConstants {

	public static final String STREAM_KEY = "receipt:tasks";
	public static final String CONSUMER_GROUP = "receipt-processors";
	public static final String CONSUMER_NAME = "processor-1";
	public static final String PROCESSING_KEY = "receipt:processing";

	public static final String FIELD_TASK_ID = "taskId";
	public static final String FIELD_FILE_PATH = "filePath";
	public static final String FIELD_CATEGORY_NAMES = "categoryNames";
}
