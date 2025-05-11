package com.dalcoomi.member.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NOT_FOUND;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SocialConnectionRepositoryImpl implements SocialConnectionRepository {

	private final SocialConnectionJpaRepository socialConnectionJpaRepository;

	@Override
	public SocialConnection save(SocialConnection socialConnection) {
		return socialConnectionJpaRepository.save(SocialConnectionJpaEntity.from(socialConnection)).toModel();
	}

	@Override
	public Boolean existsMemberBySocialIdAndSocialType(String socialId, SocialType socialType) {
		return socialConnectionJpaRepository.existsBySocialIdAndSocialType(socialId, socialType);
	}

	@Override
	public Long findMemberIdBySocialIdAndSocialType(String socialId, SocialType socialType) {
		return socialConnectionJpaRepository.findBySocialIdAndSocialType(socialId, socialType)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND)).getMember().getId();
	}
}
