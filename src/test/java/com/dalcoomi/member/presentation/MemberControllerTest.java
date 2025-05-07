package com.dalcoomi.member.presentation;

import static com.dalcoomi.member.domain.SocialType.KAKAO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

		mockMvc.perform(post("/api/member/sign-up")
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

		mockMvc.perform(post("/api/member/sign-up")
				.contentType("application/json")
				.content(json))
			.andExpect(status().isConflict())
			.andDo(print());
	}
}
