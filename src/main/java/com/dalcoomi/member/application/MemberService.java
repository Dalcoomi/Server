package com.dalcoomi.member.application;

import static com.dalcoomi.common.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_1;
import static com.dalcoomi.common.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_2;
import static com.dalcoomi.common.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_3;
import static com.dalcoomi.common.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_4;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_CONFLICT;

import java.security.SecureRandom;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.error.exception.ConflictException;
import com.dalcoomi.common.util.provider.NicknameProvider;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.dto.SocialInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final SocialConnectionRepository socialConnectionRepository;

	@Transactional
	public Long signUp(SocialInfo socialInfo) {
		boolean existsMember = socialConnectionRepository.existsMemberBySocialIdAndSocialType(
			socialInfo.socialId(), socialInfo.socialType());

		if (existsMember) {
			throw new ConflictException(MEMBER_CONFLICT);
		}

		String name = socialInfo.memberInfo().name();
		String nickname = new NicknameProvider().generateUniqueNickname(name, 4);
		String randomProfileUrl = getRandomDefaultProfileImage();

		Member member = Member.builder()
			.email(socialInfo.memberInfo().email())
			.name(name)
			.nickname(nickname)
			.birthday(socialInfo.memberInfo().birthday())
			.gender(socialInfo.memberInfo().gender())
			.profileImageUrl(randomProfileUrl)
			.serviceAgreement(socialInfo.memberInfo().serviceAgreement())
			.collectionAgreement(socialInfo.memberInfo().collectionAgreement())
			.build();

		member = memberRepository.save(member);

		SocialConnection socialConnection = SocialConnection.builder()
			.member(member)
			.socialId(socialInfo.socialId())
			.socialType(socialInfo.socialType())
			.build();

		socialConnectionRepository.save(socialConnection);

		return member.getId();
	}

	@Transactional(readOnly = true)
	public Member get(Long memberId) {
		return memberRepository.findById(memberId);
	}

	private String getRandomDefaultProfileImage() {
		String[] defaultImages = {
			DEFAULT_PROFILE_IMAGE_1,
			DEFAULT_PROFILE_IMAGE_2,
			DEFAULT_PROFILE_IMAGE_3,
			DEFAULT_PROFILE_IMAGE_4
		};

		Random random = new SecureRandom();
		int randomIndex = random.nextInt(defaultImages.length);

		return defaultImages[randomIndex];
	}
}
