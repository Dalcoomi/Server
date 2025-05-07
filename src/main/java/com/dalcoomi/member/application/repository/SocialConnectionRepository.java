package com.dalcoomi.member.application.repository;

import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;

public interface SocialConnectionRepository {

	boolean existsMemberBySocialIdAndSocialType(String socialId, SocialType socialType);

	Long findMemberIdBySocialIdAndSocialType(String socialId, SocialType socialType);

	SocialConnection save(SocialConnection socialConnection);
}
