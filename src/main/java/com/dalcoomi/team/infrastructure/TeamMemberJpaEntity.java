package com.dalcoomi.team.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;

import java.time.LocalDateTime;

import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
import com.dalcoomi.team.domain.TeamMember;

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
@Table(name = "team_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMemberJpaEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private TeamJpaEntity team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity member;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public TeamMemberJpaEntity(Long id, TeamJpaEntity team, MemberJpaEntity member, LocalDateTime deletedAt) {
		this.id = id;
		this.team = team;
		this.member = member;
		this.deletedAt = deletedAt;
	}

	public static TeamMemberJpaEntity from(TeamMember teamMember) {
		return TeamMemberJpaEntity.builder()
			.id(teamMember.getId())
			.team(TeamJpaEntity.from(teamMember.getTeam()))
			.member(MemberJpaEntity.from(teamMember.getMember()))
			.deletedAt(teamMember.getDeletedAt())
			.build();
	}

	public TeamMember toModel() {
		return TeamMember.builder()
			.id(this.id)
			.team(this.team.toModel())
			.member(this.member.toModel())
			.deletedAt(this.deletedAt)
			.build();
	}
}
