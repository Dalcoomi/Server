package com.dalcoomi.common.util.lock;

import java.security.MessageDigest;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ReceiptLockKeyGenerator {

	public String generateUploadLockKey(Long memberId, Long teamId, MultipartFile receipt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(receipt.getBytes());
			StringBuilder result = new StringBuilder();

			for (byte b : hashBytes) {
				result.append(String.format("%02x", b));
			}

			String fileHash = result.toString();
			String teamKey = teamId != null ? teamId.toString() : "personal";

			return String.format("receipt:upload:%d:%s:%s", memberId, teamKey, fileHash);
		} catch (Exception e) {
			// 파일 해시 생성 실패 시 파일명과 크기로 대체
			return String.format("receipt:upload:%d:%s:%s:%d",
				memberId,
				teamId != null ? teamId.toString() : "personal",
				receipt.getOriginalFilename(),
				receipt.getSize());
		}
	}

	public String generateSaveLockKey(Long memberId, String taskId) {
		return String.format("receipt:save:%d:%s", memberId, taskId);
	}
}
