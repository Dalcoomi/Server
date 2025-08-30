package com.dalcoomi.member.dto;

import com.dalcoomi.member.domain.Member;

import lombok.Builder;

@Builder
public record AvatarInfo(
	Member member,
	boolean defaultImage
) {

}
