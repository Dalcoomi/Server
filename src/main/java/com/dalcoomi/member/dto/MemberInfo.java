package com.dalcoomi.member.dto;

import java.time.LocalDate;
import java.util.List;

import com.dalcoomi.member.domain.SocialType;

import lombok.Builder;

@Builder
public record MemberInfo(
	String socialId,
	List<SocialType> socialTypes,
	String email,
	String name,
	String nickname,
	LocalDate birthday,
	String gender,
	String profileImageUrl,
	Boolean serviceAgreement,
	Boolean collectionAgreement,
	Boolean aiLearningAgreement
) {

}
