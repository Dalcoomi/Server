package com.dalcoomi.member.application.repository;

import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;

public interface SocialConnectionRepository {

	SocialConnection save(SocialConnection socialConnection);

	Boolean existsMemberBySocialIdAndSocialType(String socialId, SocialType socialType);

	SocialConnection findByMemberId(Long memberId);

	Long findMemberIdBySocialIdAndSocialType(String socialId, SocialType socialType);

	void deleteByMemberId(Long memberId);
}
