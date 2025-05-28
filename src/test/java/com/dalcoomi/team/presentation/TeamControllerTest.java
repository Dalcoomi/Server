package com.dalcoomi.team.presentation;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_COUNT_EXCEEDED;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_INVALID_MEMBER_LIMIT;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_ALREADY_EXISTS;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_COUNT_EXCEEDED;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_NOT_FOUND;
import static com.dalcoomi.team.domain.Team.MAX_MEMBER_LIMIT;
import static java.lang.String.format;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
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
import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.fixture.CategoryFixture;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.fixture.TeamFixture;
import com.dalcoomi.fixture.TransactionFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;
import com.dalcoomi.team.dto.request.LeaveTeamRequest;
import com.dalcoomi.team.dto.request.TeamRequest;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Test
	@DisplayName("통합 테스트 - 그룹 생성 성공")
	void create_team_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String title = "엥엥";
		Integer memberLimit = 2;
		String purpose = "에엥";

		TeamRequest request = new TeamRequest(title, memberLimit, purpose);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		String result = mockMvc.perform(post("/api/team")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andDo(print())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Team team = teamRepository.findByInvitationCode(result);

		assertThat(team.getLeader().getId()).isEqualTo(member.getId());
		assertThat(team.getTitle()).isEqualTo(title);
		assertThat(team.getMemberLimit()).isEqualTo(memberLimit);
		assertThat(team.getPurpose()).isEqualTo(purpose);
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 인원 제한 10명 초과 시 그룹 생성 실패")
	void create_team_limit_exceeded_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String title = "엥엥";
		Integer memberLimit = 12;
		String purpose = "에엥";

		TeamRequest request = new TeamRequest(title, memberLimit, purpose);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/team")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().is5xxServerError())
			.andExpect(jsonPath("$.message").value(format(TEAM_INVALID_MEMBER_LIMIT.getMessage(), MAX_MEMBER_LIMIT)))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 최대 그룹 수 초과 시 팀 생성 실패")
	void create_team_max_team_count_exceeded_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Team team1 = TeamFixture.getTeam1(member);
		team1 = teamRepository.save(team1);
		TeamMember teamMember1 = TeamMember.of(team1, member);
		teamMemberRepository.save(teamMember1);

		Team team2 = TeamFixture.getTeam2(member);
		team2 = teamRepository.save(team2);
		TeamMember teamMember2 = TeamMember.of(team2, member);
		teamMemberRepository.save(teamMember2);

		Team team3 = TeamFixture.getTeam3(member);
		team3 = teamRepository.save(team3);
		TeamMember teamMember3 = TeamMember.of(team3, member);
		teamMemberRepository.save(teamMember3);

		Team team4 = TeamFixture.getTeam4(member);
		team4 = teamRepository.save(team4);
		TeamMember teamMember4 = TeamMember.of(team4, member);
		teamMemberRepository.save(teamMember4);

		Team team5 = TeamFixture.getTeam5(member);
		team5 = teamRepository.save(team5);
		TeamMember teamMember5 = TeamMember.of(team5, member);
		teamMemberRepository.save(teamMember5);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		TeamRequest request = new TeamRequest("새 팀", 3, "새 목표");
		String json = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(post("/api/team")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(TEAM_COUNT_EXCEEDED.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 가입 성공")
	void join_team_success() throws Exception {
		// given
		Member leaderMember = MemberFixture.getMember1();
		leaderMember = memberRepository.save(leaderMember);

		Team team = TeamFixture.getTeam1(leaderMember);
		team = teamRepository.save(team);

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
		mockMvc.perform(post("/api/team/join/{invitationCode}", invitationCode)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());

		boolean joined = teamMemberRepository.existsByTeamIdAndMemberId(team.getId(), newMember.getId());

		assertThat(joined).isTrue();
	}

	@Test
	@DisplayName("통합 테스트 - 이미 가입한 경우 그룹 가입 실패")
	void already_joined_fail() throws Exception {
		// given
		Member leaderMember = MemberFixture.getMember1();
		leaderMember = memberRepository.save(leaderMember);

		Team team = TeamFixture.getTeam1(leaderMember);
		team = teamRepository.save(team);

		Member member = MemberFixture.getMember2();
		member = memberRepository.save(member);

		TeamMember teamMember = TeamMember.of(team, member);
		teamMemberRepository.save(teamMember);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String invitationCode = "12345678";

		// when & then
		mockMvc.perform(post("/api/team/join/{invitationCode}", invitationCode)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(TEAM_MEMBER_ALREADY_EXISTS.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 인원 제한 초과할 경우 그룹 가입 실패")
	void member_count_exceeded_fail() throws Exception {
		// given
		Member leaderMember = MemberFixture.getMember1();
		leaderMember = memberRepository.save(leaderMember);

		Team team = TeamFixture.getTeam1(leaderMember);
		team = teamRepository.save(team);

		TeamMember leaderTeamMember = TeamMember.of(team, leaderMember);
		teamMemberRepository.save(leaderTeamMember);

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
		mockMvc.perform(post("/api/team/join/{invitationCode}", invitationCode)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(TEAM_MEMBER_COUNT_EXCEEDED.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 존재하지 않는 코드일 경우 그룹 가입 실패")
	void invalid_invitation_code_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String invalidCode = "NOTEXIST";

		// when & then
		mockMvc.perform(post("/api/team/join/{invitationCode}", invalidCode)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value(TEAM_NOT_FOUND.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 내 그룹 목록 조회 성공")
	void get_my_teams_success() throws Exception {
		// given
		Member member1 = MemberFixture.getMember1();
		member1 = memberRepository.save(member1);

		Team team1 = TeamFixture.getTeam1(member1);
		team1 = teamRepository.save(team1);

		TeamMember teamMember1 = TeamMember.of(team1, member1);
		teamMemberRepository.save(teamMember1);

		Member member2 = MemberFixture.getMember2();
		member2 = memberRepository.save(member2);

		Team team2 = TeamFixture.getTeam2(member2);
		team2 = teamRepository.save(team2);

		TeamMember teamMember2 = TeamMember.of(team2, member1);
		teamMemberRepository.save(teamMember2);

		Member member3 = MemberFixture.getMember3();
		member3 = memberRepository.save(member3);

		TeamMember additionalTeamMember = TeamMember.of(team1, member3);
		teamMemberRepository.save(additionalTeamMember);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member1.getId(),
			member1.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		// when & then
		mockMvc.perform(get("/api/team")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.groups").isArray())
			.andExpect(jsonPath("$.groups").value(hasSize(2)))
			.andExpect(jsonPath("$.groups[0].teamId").value(team1.getId()))
			.andExpect(jsonPath("$.groups[0].title").value(team1.getTitle()))
			.andExpect(jsonPath("$.groups[0].memberCount").value(2))
			.andExpect(jsonPath("$.groups[0].memberLimit").value(team1.getMemberLimit()))
			.andExpect(jsonPath("$.groups[1].teamId").value(team2.getId()))
			.andExpect(jsonPath("$.groups[1].title").value(team2.getTitle()))
			.andExpect(jsonPath("$.groups[1].memberCount").value(1))
			.andExpect(jsonPath("$.groups[1].memberLimit").value(team2.getMemberLimit()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 가입한 그룹이 없을 경우 빈 목록 반환 성공")
	void get_my_teams_empty_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		// when & then
		mockMvc.perform(get("/api/team")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.groups").isArray())
			.andExpect(jsonPath("$.groups").value(hasSize(0)))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 특정 팀 조회 성공")
	void get_team_success() throws Exception {
		// given
		Member leader = MemberFixture.getMember1();
		leader = memberRepository.save(leader);

		Team team = TeamFixture.getTeam1(leader);
		team = teamRepository.save(team);

		Member member2 = MemberFixture.getMember2();
		member2 = memberRepository.save(member2);

		Member member3 = MemberFixture.getMember3();
		member3 = memberRepository.save(member3);

		TeamMember teamMember1 = TeamMember.of(team, leader);
		teamMemberRepository.save(teamMember1);

		TeamMember teamMember2 = TeamMember.of(team, member2);
		teamMemberRepository.save(teamMember2);

		TeamMember teamMember3 = TeamMember.of(team, member3);
		teamMemberRepository.save(teamMember3);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(leader.getId(),
			leader.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		// when & then
		mockMvc.perform(get("/api/team/{teamId}", team.getId())
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.teamId").value(team.getId()))
			.andExpect(jsonPath("$.title").value(team.getTitle()))
			.andExpect(jsonPath("$.memberLimit").value(team.getMemberLimit()))
			.andExpect(jsonPath("$.purpose").value(team.getPurpose()))
			.andExpect(jsonPath("$.leaderNickname").value(team.getLeader().getNickname()))
			.andExpect(jsonPath("$.members").isArray())
			.andExpect(jsonPath("$.members").value(hasSize(3)))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 존재하지 않는 팀 조회 실패")
	void team_not_found_fail() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Long nonExistentTeamId = 999L;

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		// when & then
		mockMvc.perform(get("/api/team/{teamId}", nonExistentTeamId)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value(TEAM_NOT_FOUND.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 떠나기 성공")
	void leave_team_success() throws Exception {
		// given
		Member leader = MemberFixture.getMember1();
		leader = memberRepository.save(leader);

		Team team = TeamFixture.getTeam1(leader);
		team = teamRepository.save(team);

		Member memberToLeave = MemberFixture.getMember2();
		memberToLeave = memberRepository.save(memberToLeave);

		Member otherMember = MemberFixture.getMember3();
		otherMember = memberRepository.save(otherMember);

		TeamMember leaderTeamMember = TeamMember.of(team, leader);
		teamMemberRepository.save(leaderTeamMember);

		TeamMember memberToLeaveTeamMember = TeamMember.of(team, memberToLeave);
		teamMemberRepository.save(memberToLeaveTeamMember);

		TeamMember otherTeamMember = TeamMember.of(team, otherMember);
		teamMemberRepository.save(otherTeamMember);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberToLeave.getId(),
			memberToLeave.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		LeaveTeamRequest request = new LeaveTeamRequest(team.getId(), null);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(delete("/api/team/leave")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());

		boolean memberExists = teamMemberRepository.existsByTeamIdAndMemberId(team.getId(), memberToLeave.getId());
		boolean leaderExists = teamMemberRepository.existsByTeamIdAndMemberId(team.getId(), leader.getId());
		boolean otherMemberExists = teamMemberRepository.existsByTeamIdAndMemberId(team.getId(), otherMember.getId());

		assertThat(memberExists).isFalse();
		assertThat(leaderExists).isTrue();
		assertThat(otherMemberExists).isTrue();
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 리더가 팀을 떠나면서 새 리더 지정 성공")
	void leave_team_leader_with_next_leader_success() throws Exception {
		// given
		Member leader = MemberFixture.getMember1();
		leader = memberRepository.save(leader);

		Team team = TeamFixture.getTeam1(leader);
		team = teamRepository.save(team);

		Member nextLeader = MemberFixture.getMember2();
		nextLeader = memberRepository.save(nextLeader);

		Member otherMember = MemberFixture.getMember3();
		otherMember = memberRepository.save(otherMember);

		TeamMember leaderTeamMember = TeamMember.of(team, leader);
		teamMemberRepository.save(leaderTeamMember);

		TeamMember nextLeaderTeamMember = TeamMember.of(team, nextLeader);
		teamMemberRepository.save(nextLeaderTeamMember);

		TeamMember otherTeamMember = TeamMember.of(team, otherMember);
		teamMemberRepository.save(otherTeamMember);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(leader.getId(),
			leader.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		LeaveTeamRequest request = new LeaveTeamRequest(team.getId(), nextLeader.getNickname());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(delete("/api/team/leave")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());

		Team updatedTeam = teamRepository.findById(team.getId());
		assertThat(updatedTeam.getLeader().getId()).isEqualTo(nextLeader.getId());

		int memberCount = teamMemberRepository.countByTeamId(team.getId());
		assertThat(memberCount).isEqualTo(2);
	}

	@Test
	@DisplayName("통합 테스트 - 마지막 멤버가 팀을 떠나면 팀 삭제 성공")
	void leave_team_last_member_team_deleted() throws Exception {
		// given
		Member lastMember = MemberFixture.getMember1();
		lastMember = memberRepository.save(lastMember);

		Team team = TeamFixture.getTeam1(lastMember);
		team = teamRepository.save(team);

		TeamMember lastTeamMember = TeamMember.of(team, lastMember);
		teamMemberRepository.save(lastTeamMember);

		Category category = CategoryFixture.getCategory1(lastMember);
		category = categoryRepository.save(category);

		Transaction transaction1 = TransactionFixture.getTeamTransactionWithExpense1(lastMember, team.getId(),
			category);
		Transaction transaction2 = TransactionFixture.getTeamTransactionWithExpense2(lastMember, team.getId(),
			category);
		Transaction transaction3 = TransactionFixture.getTeamTransactionWithExpense3(lastMember, team.getId(),
			category);
		Transaction transaction4 = TransactionFixture.getTeamTransactionWithExpense4(lastMember, team.getId(),
			category);
		List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3, transaction4);
		transactionRepository.saveAll(transactions);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(lastMember.getId(),
			lastMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Long teamId = team.getId();
		LeaveTeamRequest request = new LeaveTeamRequest(teamId, null);

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(delete("/api/team/leave")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());

		int memberCount = teamMemberRepository.countByTeamId(teamId);
		assertThat(memberCount).isZero();

		assertThatThrownBy(() -> teamRepository.findById(teamId))
			.isInstanceOf(NotFoundException.class)
			.hasMessageContaining(TEAM_NOT_FOUND.getMessage());

		TransactionSearchCriteria criteria = TransactionSearchCriteria.of(lastTeamMember.getId(), teamId, null, null);

		List<Transaction> teamTransactions = transactionRepository.findTransactions(criteria);
		assertThat(teamTransactions).isEmpty();
	}
}
