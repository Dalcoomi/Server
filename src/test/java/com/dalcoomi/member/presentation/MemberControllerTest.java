package com.dalcoomi.member.presentation;

import static com.dalcoomi.member.domain.SocialType.KAKAO;
import static com.dalcoomi.member.domain.WithdrawalType.LOW_USAGE_FREQUENCY;
import static com.dalcoomi.member.domain.WithdrawalType.OTHER;
import static com.dalcoomi.member.domain.WithdrawalType.PRIVACY_CONCERN;
import static com.dalcoomi.member.domain.WithdrawalType.USING_OTHER_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.auth.filter.CustomUserDetails;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.fixture.SocialConnectionFixture;
import com.dalcoomi.fixture.TeamFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.application.repository.WithdrawalRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.Withdrawal;
import com.dalcoomi.member.dto.LeaderTransferInfo;
import com.dalcoomi.member.dto.request.SignUpRequest;
import com.dalcoomi.member.dto.request.UpdateProfileRequest;
import com.dalcoomi.member.dto.request.WithdrawRequest;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest extends AbstractContainerBaseTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private SocialConnectionRepository socialConnectionRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private WithdrawalRepository withdrawalRepository;

	@Test
	@DisplayName("통합 테스트 - 회원가입 성공")
	void sign_up_success() throws Exception {
		// given
		String socialId = "12345";
		String email = "test@example.com";
		String name = "테스트";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "";
		boolean serviceAgreement = true;
		boolean collectionAgreement = true;

		SignUpRequest request = new SignUpRequest(socialId, KAKAO, email, name, birthday, gender, serviceAgreement,
			collectionAgreement);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/members/sign-up")
				.contentType("application/json")
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.refreshToken").exists())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 이름 길이 초과 회원가입 실패")
	void name_length_over_sign_up_fail() throws Exception {
		// given
		String socialId = "12345";
		String email = "test@example.com";
		String name = "프라이인드로스테쭈젠댄마리소피아수인레나테엘리자벳피아루이제제";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "남성";
		boolean serviceAgreement = true;
		boolean collectionAgreement = true;

		SignUpRequest request = new SignUpRequest(socialId, KAKAO, email, name, birthday, gender, serviceAgreement,
			collectionAgreement);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/members/sign-up")
				.contentType("application/json")
				.content(json))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 이미 존재하는 회원은 회원가입 실패")
	void already_exists_sign_up_fail() throws Exception {
		// given
		Member testMember = MemberFixture.getMember1();

		testMember = memberRepository.save(testMember);

		SocialConnection socialConnection = SocialConnectionFixture.getSocialConnection1(testMember);

		socialConnection = socialConnectionRepository.save(socialConnection);

		SignUpRequest request = new SignUpRequest(socialConnection.getSocialId(), KAKAO, "another@example.com", "다른이름",
			LocalDate.of(1995, 5, 5), "여성", true, true);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/members/sign-up")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isConflict())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 회원 조회 성공")
	void get_member_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		SocialConnection socialConnection = SocialConnectionFixture.getSocialConnection1(member);
		socialConnection = socialConnectionRepository.save(socialConnection);

		// 인증 설정
		setAuthentication(member.getId());

		// when & then
		mockMvc.perform(get("/api/members")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.socialType").value(socialConnection.getSocialType().toString()))
			.andExpect(jsonPath("$.email").value(member.getEmail()))
			.andExpect(jsonPath("$.name").value(member.getName()))
			.andExpect(jsonPath("$.nickname").value(member.getNickname()))
			.andExpect(jsonPath("$.birthday").value(member.getBirthday().toString()))
			.andExpect(jsonPath("$.gender").value(member.getGender()))
			.andExpect(jsonPath("$.profileImageUrl").value(member.getProfileImageUrl()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 회원 프로필 사진 업로드 성공")
	void upload_member_profile_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		// 테스트용 이미지 파일 생성
		MockMultipartFile profileImage = new MockMultipartFile(
			"profileImage",
			"test-image.jpg",
			"image/jpeg",
			"test image content".getBytes()
		);

		// when & then
		mockMvc.perform(multipart(PATCH, "/api/members/avatar")
				.file(profileImage)
				.param("removeAvatar", "false"))
			.andExpect(status().isOk())
			.andDo(print());

		Member updatedMember = memberRepository.findById(member.getId());
		assertThat(updatedMember).isNotNull();
	}

	@Test
	@DisplayName("통합 테스트 - 회원 프로필 사진 삭제 성공")
	void delete_member_profile_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		// when & then
		mockMvc.perform(multipart(PATCH, "/api/members/avatar")
				.param("removeAvatar", "true"))
			.andExpect(status().isOk())
			.andDo(print());

		Member updateMember = memberRepository.findById(member.getId());
		assertThat(updateMember).isNotNull();
	}

	@Test
	@DisplayName("통합 테스트 - 닉네임 사용 가능 확인 성공")
	void check_nickname_availability_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		String availableNickname = "사용가능한닉";

		// when & then
		mockMvc.perform(get("/api/members/nickname/availability")
				.param("nickname", availableNickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().string("true"))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 닉네임 중복으로 사용 불가능 확인 성공")
	void check_nickname_unavailable_success() throws Exception {
		// given
		Member member1 = MemberFixture.getMember1();
		Member member2 = MemberFixture.getMember2();

		member1 = memberRepository.save(member1);
		member2 = memberRepository.save(member2);

		// 인증 설정
		setAuthentication(member1.getId());

		String duplicatedNickname = member2.getNickname();

		// when & then
		mockMvc.perform(get("/api/members/nickname/availability")
				.param("nickname", duplicatedNickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().string("false"))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 현재 사용 중인 닉네임 확인 성공")
	void check_current_nickname_available_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		String currentNickname = member.getNickname();

		// when & then
		mockMvc.perform(get("/api/members/nickname/availability")
				.param("nickname", currentNickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().string("true"))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 닉네임 길이 초과로 확인 실패")
	void check_nickname_too_long_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		String tooLongNickname = "a".repeat(10);

		// when & then
		mockMvc.perform(get("/api/members/nickname/availability")
				.param("nickname", tooLongNickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 닉네임 길이 부족으로 확인 실패")
	void check_nickname_too_short_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		String tooShortNickname = "a";

		// when & then
		mockMvc.perform(get("/api/members/nickname/availability")
				.param("nickname", tooShortNickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 닉네임 정규표현식 위반으로 확인 실패")
	void check_nickname_invalid_pattern_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		String invalidNickname = "닉네임@#$";

		// when & then
		mockMvc.perform(get("/api/members/nickname/availability")
				.param("nickname", invalidNickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 빈 닉네임으로 확인 실패")
	void check_nickname_empty_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		String emptyNickname = "";

		// when & then
		mockMvc.perform(get("/api/members/nickname/availability")
				.param("nickname", emptyNickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 회원 정보 수정 성공")
	void update_member_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		SocialConnection socialConnection = SocialConnectionFixture.getSocialConnection1(member);
		socialConnectionRepository.save(socialConnection);

		// 인증 설정
		setAuthentication(member.getId());

		String name = "아야어여";
		String nickname = "무요";
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "여성";

		UpdateProfileRequest request = new UpdateProfileRequest(name, nickname, birthday, gender);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(patch("/api/members/profile")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(name))
			.andExpect(jsonPath("$.nickname").value(nickname))
			.andExpect(jsonPath("$.birthday").value(birthday.toString()))
			.andExpect(jsonPath("$.gender").value(gender))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 닉네임 중복 시 회원 정보 수정 실패")
	void update_member_nickname_conflict_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		Member member2 = MemberFixture.getMember2();
		member = memberRepository.save(member);
		member2 = memberRepository.save(member2);

		SocialConnection socialConnection = SocialConnectionFixture.getSocialConnection1(member);
		socialConnectionRepository.save(socialConnection);

		// 인증 설정
		setAuthentication(member.getId());

		String name = "아야어여";
		String nickname = member2.getNickname();
		LocalDate birthday = LocalDate.of(1990, 1, 1);
		String gender = "여성";

		UpdateProfileRequest request = new UpdateProfileRequest(name, nickname, birthday, gender);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(patch("/api/members/profile")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isConflict())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 고정 사유로 회원탈퇴 성공")
	void withdraw_member_with_predefined_reason_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);
		Long memberId = member.getId();

		// 인증 설정
		setAuthentication(memberId);

		WithdrawRequest request = new WithdrawRequest(LOW_USAGE_FREQUENCY, null, Collections.emptyList());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/members")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

		assertThatThrownBy(() -> memberRepository.findById(memberId))
			.isInstanceOf(NotFoundException.class);

		Withdrawal savedWithdrawal = withdrawalRepository.findByMemberId(member.getId());
		assertThat(savedWithdrawal).isNotNull();
		assertThat(savedWithdrawal.getWithdrawalType()).isEqualTo(LOW_USAGE_FREQUENCY);
		assertThat(savedWithdrawal.getOtherReason()).isNull();
		assertThat(savedWithdrawal.getWithdrawalDate()).isNotNull();
	}

	@Test
	@DisplayName("통합 테스트 - 기타 사유로 회원탈퇴 성공")
	void withdraw_member_with_other_reason_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);
		Long memberId = member.getId();

		// 인증 설정
		setAuthentication(memberId);

		String customReason = "앱이 너무 복잡해서 사용하기 어려워요";
		WithdrawRequest request = new WithdrawRequest(OTHER, customReason, Collections.emptyList());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/members")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

		assertThatThrownBy(() -> memberRepository.findById(memberId))
			.isInstanceOf(NotFoundException.class);

		Withdrawal savedWithdrawal = withdrawalRepository.findByMemberId(member.getId());
		assertThat(savedWithdrawal).isNotNull();
		assertThat(savedWithdrawal.getWithdrawalType()).isEqualTo(OTHER);
		assertThat(savedWithdrawal.getOtherReason()).isEqualTo(customReason);
		assertThat(savedWithdrawal.getWithdrawalDate()).isNotNull();
	}

	@Test
	@DisplayName("통합 테스트 - 기타 사유 누락으로 인한 회원탈퇴 실패")
	void withdraw_member_other_reason_missing_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		WithdrawRequest request = new WithdrawRequest(OTHER, null, Collections.emptyList());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/members")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().is5xxServerError())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 기타 사유 초과로 인한 회원탈퇴 실패")
	void withdraw_member_other_reason_too_long_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		String tooLongReason = "a".repeat(100);
		WithdrawRequest request = new WithdrawRequest(OTHER, tooLongReason, Collections.emptyList());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/members")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 팀 리더 회원탈퇴 성공")
	void withdraw_team_leader_with_transfer_success() throws Exception {
		// given
		Member leader = MemberFixture.getMember1();
		Member nextLeader = MemberFixture.getMember2();

		leader = memberRepository.save(leader);
		nextLeader = memberRepository.save(nextLeader);

		// 팀 생성 및 멤버 추가 로직 (TeamFixture 활용)
		Team team = TeamFixture.getTeam1(leader);
		team = teamRepository.save(team);

		TeamMember teamMember1 = TeamMember.of(team, leader);
		TeamMember teamMember2 = TeamMember.of(team, nextLeader);
		teamMemberRepository.saveAll(List.of(teamMember1, teamMember2));

		// 인증 설정
		setAuthentication(leader.getId());

		LeaderTransferInfo transferInfo = new LeaderTransferInfo(team.getId(), nextLeader.getNickname());
		WithdrawRequest request = new WithdrawRequest(USING_OTHER_SERVICE, null, List.of(transferInfo));

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/members")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

		Team updatedTeam = teamRepository.findById(team.getId());
		assertThat(updatedTeam.getLeader().getId()).isEqualTo(nextLeader.getId());

		List<TeamMember> teamMembers = teamMemberRepository.find(team.getId(), null);
		assertThat(teamMembers).hasSize(1);
		assertThat(teamMembers.getFirst().getMember().getId()).isEqualTo(nextLeader.getId());

		Withdrawal savedWithdrawal = withdrawalRepository.findByMemberId(leader.getId());
		assertThat(savedWithdrawal).isNotNull();
		assertThat(savedWithdrawal.getWithdrawalType()).isEqualTo(USING_OTHER_SERVICE);
		assertThat(savedWithdrawal.getOtherReason()).isNull();
		assertThat(savedWithdrawal.getWithdrawalDate()).isNotNull();
	}

	@Test
	@DisplayName("통합 테스트 - 마지막 팀원 탈퇴 시 팀 삭제 성공")
	void withdraw_last_team_member_deletes_team_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Team team = TeamFixture.getTeam1(member);
		team = teamRepository.save(team);

		TeamMember teamMember = TeamMember.of(team, member);
		teamMemberRepository.save(teamMember);

		// 인증 설정
		setAuthentication(member.getId());

		WithdrawRequest request = new WithdrawRequest(PRIVACY_CONCERN, null, Collections.emptyList());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/members")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

		Withdrawal savedWithdrawal = withdrawalRepository.findByMemberId(member.getId());
		assertThat(savedWithdrawal).isNotNull();
		assertThat(savedWithdrawal.getWithdrawalType()).isEqualTo(PRIVACY_CONCERN);
		assertThat(savedWithdrawal.getOtherReason()).isNull();
		assertThat(savedWithdrawal.getWithdrawalDate()).isNotNull();
	}

	private void setAuthentication(Long memberId) {
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId, memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		@Primary
		public WebClient mockWebClient() {
			return WebClient.builder()
				.exchangeFunction(clientRequest -> {
					if (clientRequest.url().toString().contains("unlink")) {
						return Mono.just(ClientResponse.create(HttpStatus.OK)
							.header("content-type", "application/json")
							.body("{\"id\":123456789}")
							.build());
					}
					return Mono.just(ClientResponse.create(HttpStatus.OK).build());
				})
				.build();
		}
	}
}
