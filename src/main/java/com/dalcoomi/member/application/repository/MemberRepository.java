package com.dalcoomi.member.application.repository;

import java.util.List;

import com.dalcoomi.member.domain.Member;

public interface MemberRepository {

	Member save(Member member);

	Member findById(Long memberId);

	List<Member> findByIds(List<Long> memberIds);

	Member findByNickname(String nickname);
}
