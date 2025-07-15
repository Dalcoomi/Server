package com.dalcoomi.member.presentation;

import static com.dalcoomi.member.domain.SocialType.KAKAO;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.auth.filter.CustomUserDetails;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.fixture.SocialConnectionFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.dto.request.SignUpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

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
	private StringRedisTemplate redisTemplate;

	@AfterEach
	void cleanUp() {
		// Redis 정리
		// 모든 키를 검색하고 삭제
		Set<String> keys = redisTemplate.keys("*");

		if (!keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}

	@Test
	@DisplayName("통합 테스트 - 회원가입 성공")
	void sign_up_success() throws Exception {
		// given
		String socialId = "12345";
		String email = "test@example.com";
		String name = "테스트";
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
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.refreshToken").exists())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 이미 존재하는 회원은 회원가입 실패")
	void already_exists_sign_up_failure() throws Exception {
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

		// 인증 설정
		setAuthentication(member.getId());

		// when & then
		mockMvc.perform(get("/api/members")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value(member.getEmail()))
			.andExpect(jsonPath("$.name").value(member.getName()))
			.andExpect(jsonPath("$.nickname").value(member.getNickname()))
			.andExpect(jsonPath("$.profileImageUrl").value(member.getProfileImageUrl()))
			.andDo(print());
	}

	private void setAuthentication(Long memberId) {
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId, memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
