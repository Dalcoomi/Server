package com.dalcoomi.group.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;

import java.time.LocalDateTime;

import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.group.domain.Group;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
@Table(name = "groups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupJpaEntity extends BaseTimeEntity {

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reader_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity member;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "invitation_code", nullable = false)
	private String invitationCode;

	@Column(name = "count", nullable = false)
	private Integer count;

	@Column(name = "goal", nullable = true)
	private String goal;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public GroupJpaEntity(Long id, MemberJpaEntity member, String title, String invitationCode, Integer count,
		String goal, LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.title = title;
		this.invitationCode = invitationCode;
		this.count = count;
		this.goal = goal;
		this.deletedAt = deletedAt;
	}

	public static GroupJpaEntity from(Group group) {
		return GroupJpaEntity.builder()
			.id(group.getId())
			.member(MemberJpaEntity.from(group.getMember()))
			.title(group.getTitle())
			.invitationCode(group.getInvitationCode())
			.count(group.getCount())
			.goal(group.getGoal())
			.deletedAt(group.getDeletedAt())
			.build();
	}

	public Group toModel() {
		return Group.builder()
			.id(this.id)
			.member(this.member.toModel())
			.title(this.title)
			.invitationCode(this.invitationCode)
			.count(this.count)
			.goal(this.goal)
			.deletedAt(this.deletedAt)
			.build();
	}
}
