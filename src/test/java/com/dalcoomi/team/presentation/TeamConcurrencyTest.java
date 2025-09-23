package com.dalcoomi.team.presentation;

import static com.dalcoomi.common.error.model.ErrorMessage.LOCK_EXIST_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.auth.filter.CustomUserDetails;
import com.dalcoomi.common.util.lock.TeamLockKeyGenerator;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.fixture.TeamFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;
import com.dalcoomi.team.dto.request.LeaveTeamRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class TeamConcurrencyTest extends AbstractContainerBaseTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private TeamLockKeyGenerator teamLockKeyGenerator;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	private ExecutorService executorService;

	@BeforeEach
	void setUp() {
		executorService = Executors.newFixedThreadPool(4);

		// 테스트별 Redis 키 패턴 정리
		Set<String> keys = redisTemplate.keys("team:*");

		if (!keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}

	@AfterEach
	void tearDown() {
		executorService.shutdown();
	}

	@Test
	@Commit
	@DisplayName("동시성 테스트 - 동일한 팀 나가기 요청 시 하나만 성공")
	void leave_one_team_success() {
		// given
		Member leader = MemberFixture.getMember1();
		Member member2 = MemberFixture.getMember2();
		Member member3 = MemberFixture.getMember3();

		leader = memberRepository.save(leader);
		member2 = memberRepository.save(member2);
		member3 = memberRepository.save(member3);

		Team team = TeamFixture.getTeam1(leader);
		team = teamRepository.save(team);

		TeamMember teamMember1 = TeamMember.of(team, leader);
		TeamMember teamMember2 = TeamMember.of(team, member2);
		TeamMember teamMember3 = TeamMember.of(team, member3);
		teamMemberRepository.saveAll(List.of(teamMember1, teamMember2, teamMember3));

		// 인증 설정
		setAuthentication(leader.getId());

		LeaveTeamRequest request = new LeaveTeamRequest(team.getId(), member2.getNickname());

		int threadCount = 3;
		CyclicBarrier barrier = new CyclicBarrier(threadCount);
		List<Future<ResultActions>> futures = new ArrayList<>();

		// when
		for (int i = 0; i < threadCount; i++) {
			Future<ResultActions> future = executorService.submit(() -> {
				try {
					barrier.await(); // 모든 스레드가 이 지점에 도달할 때까지 대기

					return mockMvc.perform(delete("/api/teams/leave")
						.content(objectMapper.writeValueAsString(request))
						.contentType(APPLICATION_JSON));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			futures.add(future);
		}

		// then
		List<Integer> statusCodes = await().atMost(Duration.ofSeconds(60))
			.until(() -> futures.stream()
				.map(future -> {
					try {
						return future.get(2000, TimeUnit.MILLISECONDS).andReturn().getResponse().getStatus();
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull).toList(), hasSize(threadCount));

		long conflictCount = statusCodes.stream().filter(status -> status == 409).count();
		assertThat(conflictCount).isEqualTo(threadCount - 1);
	}

	@Test
	@DisplayName("동시성 테스트 - 일반 멤버 동시 나가기 요청 시 하나만 성공")
	void leave_regular_member_success() {
		// given
		Member leader = MemberFixture.getMember1();
		Member member2 = MemberFixture.getMember2();

		leader = memberRepository.save(leader);
		member2 = memberRepository.save(member2);

		Team team = TeamFixture.getTeam1(leader);
		team = teamRepository.save(team);

		TeamMember teamMember1 = TeamMember.of(team, leader);
		TeamMember teamMember2 = TeamMember.of(team, member2);
		teamMemberRepository.saveAll(List.of(teamMember1, teamMember2));

		LeaveTeamRequest request = new LeaveTeamRequest(team.getId(), null);

		// 일반 멤버가 동시에 여러 번 나가기 요청
		setAuthentication(member2.getId());

		int threadCount = 3;
		CyclicBarrier barrier = new CyclicBarrier(threadCount);
		List<Future<ResultActions>> futures = new ArrayList<>();

		// when
		for (int i = 0; i < threadCount; i++) {
			Future<ResultActions> future = executorService.submit(() -> {
				try {
					barrier.await();

					return mockMvc.perform(delete("/api/teams/leave")
						.content(objectMapper.writeValueAsString(request))
						.contentType(APPLICATION_JSON));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			futures.add(future);
		}

		// then
		List<Integer> statusCodes = await().atMost(Duration.ofSeconds(60))
			.until(() -> futures.stream()
				.map(future -> {
					try {
						return future.get(2000, TimeUnit.MILLISECONDS).andReturn().getResponse().getStatus();
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull).toList(), hasSize(threadCount));

		long conflictCount = statusCodes.stream().filter(status -> status == 409).count();
		assertThat(conflictCount).isEqualTo(threadCount - 1);
	}

	@Test
	@DisplayName("동시성 테스트 - 마지막 멤버 동시 나가기 시 팀 삭제")
	void leave_last_member_delete_team_success() {
		// given
		Member leader = MemberFixture.getMember1();
		leader = memberRepository.save(leader);

		Team team = TeamFixture.getTeam1(leader);
		team = teamRepository.save(team);

		TeamMember teamMember = TeamMember.of(team, leader);
		teamMemberRepository.save(teamMember);

		LeaveTeamRequest request = new LeaveTeamRequest(team.getId(), null);

		// 마지막 멤버가 동시에 여러 번 나가기 요청
		setAuthentication(leader.getId());

		int threadCount = 3;
		CyclicBarrier barrier = new CyclicBarrier(threadCount);
		List<Future<ResultActions>> futures = new ArrayList<>();

		// when
		for (int i = 0; i < threadCount; i++) {
			Future<ResultActions> future = executorService.submit(() -> {
				try {
					barrier.await();

					return mockMvc.perform(delete("/api/teams/leave")
						.content(objectMapper.writeValueAsString(request))
						.contentType(APPLICATION_JSON));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			futures.add(future);
		}

		// then
		List<Integer> statusCodes = await().atMost(Duration.ofSeconds(60))
			.until(() -> futures.stream()
				.map(future -> {
					try {
						return future.get(2000, TimeUnit.MILLISECONDS).andReturn().getResponse().getStatus();
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull).toList(), hasSize(threadCount));

		long conflictCount = statusCodes.stream().filter(status -> status == 409).count();
		assertThat(conflictCount).isEqualTo(threadCount - 1);
	}

	@Test
	@DisplayName("동시성 테스트 - 락 타임아웃 검증 성공")
	void lock_timeout_success() throws Exception {
		// given
		Member leader = MemberFixture.getMember1();
		leader = memberRepository.save(leader);

		Team team = TeamFixture.getTeam1(leader);
		team = teamRepository.save(team);

		TeamMember teamMember = TeamMember.of(team, leader);
		teamMemberRepository.save(teamMember);

		// 인증 설정
		setAuthentication(leader.getId());

		// 먼저 락을 수동으로 획득 (타임아웃 시뮬레이션)
		String lockKey = teamLockKeyGenerator.generateLeaveLockKey(team.getId());
		redisTemplate.opsForValue().set(lockKey, "lock", Duration.ofSeconds(10));

		LeaveTeamRequest request = new LeaveTeamRequest(team.getId(), null);

		// when & then
		mockMvc.perform(delete("/api/teams/leave")
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(LOCK_EXIST_ERROR.getMessage()))
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
