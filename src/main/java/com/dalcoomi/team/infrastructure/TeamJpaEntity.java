package com.dalcoomi.team.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;

import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
import com.dalcoomi.team.domain.Team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "team")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamJpaEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "leader_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity leader;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "invitation_code", nullable = false)
	private String invitationCode;

	@Column(name = "member_limit", nullable = false)
	private Integer memberLimit;

	@Column(name = "purpose", nullable = true)
	private String purpose;

	@Builder
	public TeamJpaEntity(Long id, MemberJpaEntity leader, String title, String invitationCode, Integer memberLimit,
		String purpose) {
		this.id = id;
		this.leader = leader;
		this.title = title;
		this.invitationCode = invitationCode;
		this.memberLimit = memberLimit;
		this.purpose = purpose;
	}

	public static TeamJpaEntity from(Team team) {
		return TeamJpaEntity.builder()
			.id(team.getId())
			.leader(MemberJpaEntity.from(team.getLeader()))
			.title(team.getTitle())
			.invitationCode(team.getInvitationCode())
			.memberLimit(team.getMemberLimit())
			.purpose(team.getPurpose())
			.build();
	}

	public Team toModel() {
		return Team.builder()
			.id(this.id)
			.leader(this.leader.toModel())
			.title(this.title)
			.invitationCode(this.invitationCode)
			.memberLimit(this.memberLimit)
			.purpose(this.purpose)
			.build();
	}
}
