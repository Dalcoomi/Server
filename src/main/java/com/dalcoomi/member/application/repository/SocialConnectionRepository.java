package com.dalcoomi.member.application.repository;

import java.util.List;

import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;

public interface SocialConnectionRepository {

	SocialConnection save(SocialConnection socialConnection);

	Boolean existsMemberBySocialIdAndSocialType(String socialId, SocialType socialType);

	List<SocialConnection> findBySocialEmail(String socialEmail);

	List<SocialConnection> findByMemberId(Long memberId);

	void deleteByMemberId(Long memberId);
}
