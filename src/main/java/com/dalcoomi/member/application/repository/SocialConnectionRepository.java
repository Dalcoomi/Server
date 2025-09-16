package com.dalcoomi.member.application.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;

public interface SocialConnectionRepository {

	SocialConnection save(SocialConnection socialConnection);

	void saveAll(List<SocialConnection> socialConnections);

	Boolean existsMemberBySocialIdAndSocialType(String socialId, SocialType socialType);

	List<SocialConnection> findBySocialEmailOrSocialId(String socialEmail, String socialId);

	List<SocialConnection> findByMemberId(Long memberId);

	List<SocialConnection> findExpiredSoftDeletedWithMember(LocalDateTime cutoffDate);

	void deleteByMemberId(Long memberId);

	void deleteAll(List<SocialConnection> socialConnections);
}
