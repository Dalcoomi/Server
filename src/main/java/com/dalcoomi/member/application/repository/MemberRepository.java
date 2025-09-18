package com.dalcoomi.member.application.repository;

import java.util.List;

import com.dalcoomi.member.domain.Member;

public interface MemberRepository {

	Member save(Member member);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	Member findById(Long memberId);

	Member findByNickname(String nickname);

	Member findByEmail(String email);

	List<Member> findAll();

	void deleteById(Long memberId);

	void deleteAll(List<Member> members);
}
