package com.dalcoomi.auth.presentation;

import static com.dalcoomi.common.constant.TokenConstants.REFRESH_TOKEN_REDIS_KEY_SUFFIX;
import static com.dalcoomi.common.error.model.ErrorMessage.TOKEN_NOT_FOUND;
import static com.dalcoomi.member.domain.SocialType.KAKAO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.annotation.WithMockCustomUser;
import com.dalcoomi.auth.dto.request.LoginRequest;
import com.dalcoomi.auth.filter.CustomUserDetails;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.fixture.SocialConnectionFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest extends AbstractContainerBaseTest {

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
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("통합 테스트 - 로그인 실패")
	void login_fail() throws Exception {
		// given
		LoginRequest request = new LoginRequest("123", KAKAO);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/auth/login")
				.contentType("application/json")
				.content(json))
			.andExpect(status().isNotFound())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 로그인 성공")
	void login_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		SocialConnection socialConnection = SocialConnectionFixture.getSocialConnection1(member);
		socialConnection = socialConnectionRepository.save(socialConnection);

		LoginRequest request = new LoginRequest(socialConnection.getSocialId(), socialConnection.getSocialType());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/auth/login")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.refreshToken").exists())
			.andDo(print());

		String savedRefreshToken = redisTemplate.opsForValue().get(member.getId() + REFRESH_TOKEN_REDIS_KEY_SUFFIX);

		assertThat(savedRefreshToken).isNotNull();
	}

	@Test
	@DisplayName("통합 테스트 - 로그아웃 성공")
	void logout_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		SocialConnection socialConnection = SocialConnectionFixture.getSocialConnection1(member);
		socialConnection = socialConnectionRepository.save(socialConnection);

		// 로그인하여 refresh token을 Redis에 저장
		LoginRequest loginRequest = new LoginRequest(socialConnection.getSocialId(), socialConnection.getSocialType());
		String loginJson = objectMapper.writeValueAsString(loginRequest);

		mockMvc.perform(post("/api/auth/login")
				.contentType(APPLICATION_JSON)
				.content(loginJson))
			.andExpect(status().isOk());

		// 인증 설정
		setAuthentication(member.getId());

		// when & then
		mockMvc.perform(post("/api/auth/logout")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());

		String savedRefreshTokenAfterLogout = redisTemplate.opsForValue()
			.get(member.getId() + REFRESH_TOKEN_REDIS_KEY_SUFFIX);

		assertThat(savedRefreshTokenAfterLogout).isNull();
	}

	@Test
	@DisplayName("통합 테스트 - 이미 로그아웃된 상태이므로 로그아웃 실패")
	void already_logged_out_logout_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		setAuthentication(member.getId());

		// when & then
		mockMvc.perform(post("/api/auth/logout")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 토큰 재발급 성공")
	@WithMockCustomUser()
	void reissue_token_success() throws Exception {
		// given
		Long memberId = 1L;
		String key = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX;
		String dummyRefreshToken = "dummy-refresh-token";

		redisTemplate.opsForValue().set(key, dummyRefreshToken);

		// when & then
		mockMvc.perform(post("/api/auth/reissue")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.refreshToken").exists())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 토큰 재발급 실패")
	@WithMockCustomUser()
	void reissue_token_fail() throws Exception {
		// given
		Long memberId = 1L;
		String key = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX;

		redisTemplate.delete(key);

		// when & then
		mockMvc.perform(post("/api/auth/reissue")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(result -> {
				String content = result.getResponse().getContentAsString();

				assertThat(content).contains(TOKEN_NOT_FOUND.getMessage());
			})
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 테스트 토큰 발급 성공")
	void create_test_token_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// when & then
		mockMvc.perform(get("/api/auth/test/token")
				.queryParam("memberId", String.valueOf(member.getId()))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.refreshToken").exists())
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
