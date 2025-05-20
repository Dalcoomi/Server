package com.dalcoomi.group.presentation;

import static com.dalcoomi.common.error.model.ErrorMessage.GROUP_MEMBER_ALREADY_EXISTS;
import static com.dalcoomi.common.error.model.ErrorMessage.GROUP_MEMBER_COUNT_EXCEEDED;
import static com.dalcoomi.common.error.model.ErrorMessage.GROUP_NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.dalcoomi.fixture.GroupFixture;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.group.application.repository.GroupMemberRepository;
import com.dalcoomi.group.application.repository.GroupRepository;
import com.dalcoomi.group.domain.Group;
import com.dalcoomi.group.domain.GroupMember;
import com.dalcoomi.group.dto.request.GroupRequest;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class GroupControllerTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private GroupMemberRepository groupMemberRepository;

	@Test
	@DisplayName("통합 테스트 - 그룹 생성 성공")
	void create_group_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();

		member = memberRepository.save(member);

		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String title = "엥엥";
		Integer count = 2;
		String goal = "에엥";

		GroupRequest request = new GroupRequest(title, count, goal);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		String result = mockMvc.perform(post("/api/group")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andDo(print())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Group group = groupRepository.findByInvitationCode(result);

		assertThat(group.getMember().getId()).isEqualTo(member.getId());
		assertThat(group.getTitle()).isEqualTo(title);
		assertThat(group.getCount()).isEqualTo(count);
		assertThat(group.getGoal()).isEqualTo(goal);
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 가입 성공")
	void join_group_success() throws Exception {
		// given
		Member leaderMember = MemberFixture.getMember1();
		leaderMember = memberRepository.save(leaderMember);

		Group group = GroupFixture.getGroup1(leaderMember);
		group = groupRepository.save(group);

		Member newMember = MemberFixture.getMember2();
		newMember = memberRepository.save(newMember);

		CustomUserDetails memberUserDetails = new CustomUserDetails(newMember.getId(),
			newMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String invitationCode = "12345678";

		// when & then
		mockMvc.perform(post("/api/group/join/{invitationCode}", invitationCode)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());

		boolean joined = groupMemberRepository.existsByGroupIdAndMemberId(group.getId(), newMember.getId());

		assertThat(joined).isTrue();
	}

	@Test
	@DisplayName("통합 테스트 - 이미 가입한 경우 그룹 가입 실패")
	void already_joined_fail() throws Exception {
		// given
		Member leaderMember = MemberFixture.getMember1();
		leaderMember = memberRepository.save(leaderMember);

		Group group = GroupFixture.getGroup1(leaderMember);
		group = groupRepository.save(group);

		Member member = MemberFixture.getMember2();
		member = memberRepository.save(member);

		GroupMember groupMember = GroupMember.of(group, member);
		groupMemberRepository.save(groupMember);

		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String invitationCode = "12345678";

		// when & then
		mockMvc.perform(post("/api/group/join/{invitationCode}", invitationCode)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(GROUP_MEMBER_ALREADY_EXISTS.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 인원 제한 초과할 경우 그룹 가입 실패")
	void member_count_exceeded_fail() throws Exception {
		// given
		Member leaderMember = MemberFixture.getMember1();
		leaderMember = memberRepository.save(leaderMember);

		Group group = GroupFixture.getGroup1(leaderMember);
		group = groupRepository.save(group);

		GroupMember leaderGroupMember = GroupMember.of(group, leaderMember);
		groupMemberRepository.save(leaderGroupMember);

		Member newMember = MemberFixture.getMember2();
		newMember = memberRepository.save(newMember);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(newMember.getId(),
			newMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String invitationCode = "12345678";

		// when & then
		mockMvc.perform(post("/api/group/join/{invitationCode}", invitationCode)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(GROUP_MEMBER_COUNT_EXCEEDED.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 존재하지 않는 코드일 경우 그룹 가입 실패")
	void invalid_invitation_code_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String invalidCode = "NOTEXIST";

		// when & then
		mockMvc.perform(post("/api/group/join/{invitationCode}", invalidCode)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value(GROUP_NOT_FOUND.getMessage()))
			.andDo(print());
	}
}
