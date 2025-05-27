package com.dalcoomi.member.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

	private final MemberJpaRepository memberJpaRepository;
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Member save(Member member) {
		return memberJpaRepository.save(MemberJpaEntity.from(member)).toModel();
	}

	@Override
	public Member findById(Long memberId) {
		return memberJpaRepository.findByIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND)).toModel();
	}

	@Override
	public List<Member> findByIds(List<Long> memberIds) {
		return memberJpaRepository.findAllByIdInAndDeletedAtIsNull(memberIds).stream()
			.map(MemberJpaEntity::toModel).toList();
	}

	@Override
	public Member findByNickname(String nickname) {
		return memberJpaRepository.findByNicknameAndDeletedAtIsNull(nickname)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND)).toModel();
	}
}
