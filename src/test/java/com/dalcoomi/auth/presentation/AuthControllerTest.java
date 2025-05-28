package com.dalcoomi.auth.presentation;

import static com.dalcoomi.common.constant.TokenConstants.REFRESH_TOKEN_REDIS_KEY_SUFFIX;
import static com.dalcoomi.common.error.model.ErrorMessage.TOKEN_NOT_FOUND;
import static com.dalcoomi.member.domain.SocialType.KAKAO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.annotation.WithMockCustomUser;
import com.dalcoomi.auth.dto.request.LoginRequest;
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
class AuthControllerTest {

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
	void login_failure() throws Exception {
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
				.contentType("application/json")
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.refreshToken").exists())
			.andDo(print());

		String savedRefreshToken = redisTemplate.opsForValue().get(member.getId() + REFRESH_TOKEN_REDIS_KEY_SUFFIX);

		assertThat(savedRefreshToken).isNotNull();
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
				.contentType("application/json"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").exists())
			.andExpect(jsonPath("$.refreshToken").exists())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 토큰 재발급 실패")
	@WithMockCustomUser()
	void reissue_token_failure() throws Exception {
		// given
		Long memberId = 1L;
		String key = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX;

		redisTemplate.delete(key);

		// when & then
		mockMvc.perform(post("/api/auth/reissue")
				.contentType("application/json"))
			.andExpect(status().isNotFound())
			.andExpect(result -> {
				String content = result.getResponse().getContentAsString();

				assertThat(content).contains(TOKEN_NOT_FOUND.getMessage());
			})
			.andDo(print());
	}
}
