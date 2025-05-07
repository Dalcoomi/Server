package com.dalcoomi.member.application.repository;

import com.dalcoomi.member.domain.Member;

public interface MemberRepository {

	Member save(Member member);

	Member findById(Long memberId);
}
