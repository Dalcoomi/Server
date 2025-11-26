package com.dalcoomi.member.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.encryption.HashService;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

	private final MemberJpaRepository memberJpaRepository;
	private final HashService hashService;

	@Override
	public Member save(Member member) {
		MemberJpaEntity entity = MemberJpaEntity.from(member);

		entity = MemberJpaEntity.builder()
			.id(entity.getId())
			.email(entity.getEmail())
			.emailHash(hashService.hash(member.getEmail()))
			.name(entity.getName())
			.nameHash(hashService.hash(member.getName()))
			.nickname(entity.getNickname())
			.birthday(entity.getBirthday())
			.birthdayHash(member.getBirthday() != null ? hashService.hash(member.getBirthday().toString()) : null)
			.gender(entity.getGender())
			.genderHash(hashService.hash(member.getGender()))
			.profileImageUrl(entity.getProfileImageUrl())
			.serviceAgreement(entity.getServiceAgreement())
			.collectionAgreement(entity.getCollectionAgreement())
			.aiLearningAgreement(entity.getAiLearningAgreement())
			.lastLoginAt(entity.getLastLoginAt())
			.deletedAt(entity.getDeletedAt())
			.build();

		return memberJpaRepository.save(entity).toModel();
	}

	@Override
	public boolean existsByEmail(String email) {
		String emailHash = hashService.hash(email);

		return memberJpaRepository.existsByEmailHash(emailHash);
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
		String emailHash = hashService.hash(email);

		return memberJpaRepository.findByEmailHashAndDeletedAtIsNull(emailHash)
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
		List<MemberJpaEntity> entities = members.stream().map(MemberJpaEntity::from).toList();

		memberJpaRepository.deleteAll(entities);
	}
}
