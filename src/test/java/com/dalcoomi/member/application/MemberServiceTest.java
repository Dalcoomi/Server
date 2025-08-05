package com.dalcoomi.member.application;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_EMAIL;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_GENDER;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_NAME;
import static com.dalcoomi.member.domain.SocialType.KAKAO;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;
import com.dalcoomi.member.dto.MemberInfo;
import com.dalcoomi.member.dto.SocialInfo;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@InjectMocks
	private MemberService memberService;

	@Mock
	private SocialConnectionRepository socialConnectionRepository;

	@Mock
	private MemberRepository memberRepository;

	@Test
	@DisplayName("이메일 공백으로 인해 회원가입 실패")
	void email_null_sign_up_over_fail() {
		// given
		String socialId = "12345";
		SocialType socialType = KAKAO;
		String email = "";
		String name = "프라이인드로스테쭈젠댄마리소피아수인레나테엘리자벳피아루이제";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "남성";
		boolean serviceAgreement = true;
		boolean collectionAgreement = true;

		MemberInfo memberInfo = MemberInfo.builder()
			.email(email)
			.name(name)
			.birthday(birthday)
			.gender(gender)
			.serviceAgreement(serviceAgreement)
			.collectionAgreement(collectionAgreement)
			.build();

		SocialInfo socialInfo = SocialInfo.builder()
			.socialId(socialId)
			.socialType(socialType)
			.memberInfo(memberInfo)
			.build();

		given(socialConnectionRepository.existsMemberBySocialIdAndSocialType(socialId, socialType)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> memberService.signUp(socialInfo))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(MEMBER_INVALID_EMAIL.getMessage());

		// verify
		then(socialConnectionRepository).should().existsMemberBySocialIdAndSocialType(socialId, socialType);
		then(memberRepository).should(never()).save(any(Member.class));
		then(socialConnectionRepository).should(never()).save(any(SocialConnection.class));
	}

	@Test
	@DisplayName("이메일 길이 초과로 인해 회원가입 실패")
	void email_length_over_sign_up_over_fail() {
		// given
		String socialId = "12345";
		SocialType socialType = KAKAO;
		String email = "testtesttesttesttesttesttesttesttesttesttesttest"
			+ "testtesttesttesttesttesttesttesttesttesttest@example.com";
		String name = "프라이인드로스테쭈젠댄마리소피아수인레나테엘리자벳피아루이제";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "남성";
		boolean serviceAgreement = true;
		boolean collectionAgreement = true;

		MemberInfo memberInfo = MemberInfo.builder()
			.email(email)
			.name(name)
			.birthday(birthday)
			.gender(gender)
			.serviceAgreement(serviceAgreement)
			.collectionAgreement(collectionAgreement)
			.build();

		SocialInfo socialInfo = SocialInfo.builder()
			.socialId(socialId)
			.socialType(socialType)
			.memberInfo(memberInfo)
			.build();

		given(socialConnectionRepository.existsMemberBySocialIdAndSocialType(socialId, socialType)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> memberService.signUp(socialInfo))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(MEMBER_INVALID_EMAIL.getMessage());

		// verify
		then(socialConnectionRepository).should().existsMemberBySocialIdAndSocialType(socialId, socialType);
		then(memberRepository).should(never()).save(any(Member.class));
		then(socialConnectionRepository).should(never()).save(any(SocialConnection.class));
	}

	@Test
	@DisplayName("이름 공백으로 인해 회원가입 실패")
	void name_null_sign_up_over_fail() {
		// given
		String socialId = "12345";
		SocialType socialType = KAKAO;
		String email = "test@example.com";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "남성";
		boolean serviceAgreement = true;
		boolean collectionAgreement = true;

		MemberInfo memberInfo = MemberInfo.builder()
			.email(email)
			.name("")
			.birthday(birthday)
			.gender(gender)
			.serviceAgreement(serviceAgreement)
			.collectionAgreement(collectionAgreement)
			.build();

		SocialInfo socialInfo = SocialInfo.builder()
			.socialId(socialId)
			.socialType(socialType)
			.memberInfo(memberInfo)
			.build();

		given(socialConnectionRepository.existsMemberBySocialIdAndSocialType(socialId, socialType)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> memberService.signUp(socialInfo))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(MEMBER_INVALID_NAME.getMessage());

		// verify
		then(socialConnectionRepository).should().existsMemberBySocialIdAndSocialType(socialId, socialType);
		then(memberRepository).should(never()).save(any(Member.class));
		then(socialConnectionRepository).should(never()).save(any(SocialConnection.class));
	}

	@Test
	@DisplayName("이름 길이 부족으로 인해 회원가입 실패")
	void name_length_less_sign_up_over_fail() {
		// given
		String socialId = "12345";
		SocialType socialType = KAKAO;
		String email = "test@example.com";
		String name = "프";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "남성";
		boolean serviceAgreement = true;
		boolean collectionAgreement = true;

		MemberInfo memberInfo = MemberInfo.builder()
			.email(email)
			.name(name)
			.birthday(birthday)
			.gender(gender)
			.serviceAgreement(serviceAgreement)
			.collectionAgreement(collectionAgreement)
			.build();

		SocialInfo socialInfo = SocialInfo.builder()
			.socialId(socialId)
			.socialType(socialType)
			.memberInfo(memberInfo)
			.build();

		given(socialConnectionRepository.existsMemberBySocialIdAndSocialType(socialId, socialType)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> memberService.signUp(socialInfo))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(MEMBER_INVALID_NAME.getMessage());

		// verify
		then(socialConnectionRepository).should().existsMemberBySocialIdAndSocialType(socialId, socialType);
		then(memberRepository).should(never()).save(any(Member.class));
		then(socialConnectionRepository).should(never()).save(any(SocialConnection.class));
	}

	@Test
	@DisplayName("이름 길이 초과로 인해 회원가입 실패")
	void name_length_over_sign_up_over_fail() {
		// given
		String socialId = "12345";
		SocialType socialType = KAKAO;
		String email = "test@example.com";
		String name = "프라이인드로스테쭈젠댄마리소피아수인레나테엘리자벳피아루이제제";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "남성";
		boolean serviceAgreement = true;
		boolean collectionAgreement = true;

		MemberInfo memberInfo = MemberInfo.builder()
			.email(email)
			.name(name)
			.birthday(birthday)
			.gender(gender)
			.serviceAgreement(serviceAgreement)
			.collectionAgreement(collectionAgreement)
			.build();

		SocialInfo socialInfo = SocialInfo.builder()
			.socialId(socialId)
			.socialType(socialType)
			.memberInfo(memberInfo)
			.build();

		given(socialConnectionRepository.existsMemberBySocialIdAndSocialType(socialId, socialType)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> memberService.signUp(socialInfo))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(MEMBER_INVALID_NAME.getMessage());

		// verify
		then(socialConnectionRepository).should().existsMemberBySocialIdAndSocialType(socialId, socialType);
		then(memberRepository).should(never()).save(any(Member.class));
		then(socialConnectionRepository).should(never()).save(any(SocialConnection.class));
	}

	@Test
	@DisplayName("성별 길이 초과로 인해 회원가입 실패")
	void gender_length_over_sign_up_over_fail() {
		// given
		String socialId = "12345";
		SocialType socialType = KAKAO;
		String email = "test@example.com";
		String name = "프라이인드로스테쭈젠댄마리소피아수인레나테엘리자벳피아루이제";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "밝히고 싶지 않음";
		boolean serviceAgreement = true;
		boolean collectionAgreement = true;

		MemberInfo memberInfo = MemberInfo.builder()
			.email(email)
			.name(name)
			.birthday(birthday)
			.gender(gender)
			.serviceAgreement(serviceAgreement)
			.collectionAgreement(collectionAgreement)
			.build();

		SocialInfo socialInfo = SocialInfo.builder()
			.socialId(socialId)
			.socialType(socialType)
			.memberInfo(memberInfo)
			.build();

		given(socialConnectionRepository.existsMemberBySocialIdAndSocialType(socialId, socialType)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> memberService.signUp(socialInfo))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(MEMBER_INVALID_GENDER.getMessage());

		// verify
		then(socialConnectionRepository).should().existsMemberBySocialIdAndSocialType(socialId, socialType);
		then(memberRepository).should(never()).save(any(Member.class));
		then(socialConnectionRepository).should(never()).save(any(SocialConnection.class));
	}
}
