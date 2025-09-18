package com.dalcoomi.member.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

	private final MemberJpaRepository memberJpaRepository;

	@Override
	public Member save(Member member) {
		return memberJpaRepository.save(MemberJpaEntity.from(member)).toModel();
	}

	@Override
	public boolean existsByEmail(String email) {
		return memberJpaRepository.existsByEmail(email);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return memberJpaRepository.existsByNickname(nickname);
	}

	@Override
	public Member findById(Long memberId) {
		return memberJpaRepository.findByIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND)).toModel();
	}

	@Override
	public Member findByNickname(String nickname) {
		return memberJpaRepository.findByNicknameAndDeletedAtIsNull(nickname)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND)).toModel();
	}

	@Override
	public Member findByEmail(String email) {
		return memberJpaRepository.findByEmailAndDeletedAtIsNull(email)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND)).toModel();
	}

	@Override
	public List<Member> findAll() {
		return memberJpaRepository.findAll().stream().map(MemberJpaEntity::toModel).toList();
	}

	@Override
	public void deleteById(Long memberId) {
		memberJpaRepository.deleteById(memberId);
	}

	@Override
	public void deleteAll(List<Member> members) {
		List<MemberJpaEntity> entities = members.stream()
			.map(MemberJpaEntity::from)
			.toList();

		memberJpaRepository.deleteAll(entities);
	}
}
