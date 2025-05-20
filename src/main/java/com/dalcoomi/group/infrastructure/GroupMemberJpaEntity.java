package com.dalcoomi.group.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;

import java.time.LocalDateTime;

import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.group.domain.GroupMember;
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
@Table(name = "groups_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMemberJpaEntity extends BaseTimeEntity {

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private GroupJpaEntity group;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity member;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public GroupMemberJpaEntity(Long id, GroupJpaEntity group, MemberJpaEntity member, LocalDateTime deletedAt) {
		this.id = id;
		this.group = group;
		this.member = member;
		this.deletedAt = deletedAt;
	}

	public static GroupMemberJpaEntity from(GroupMember groupMember) {
		return GroupMemberJpaEntity.builder()
			.id(groupMember.getId())
			.group(GroupJpaEntity.from(groupMember.getGroup()))
			.member(MemberJpaEntity.from(groupMember.getMember()))
			.deletedAt(groupMember.getDeletedAt())
			.build();
	}

	public GroupMember toModel() {
		return GroupMember.builder()
			.id(this.id)
			.group(this.group.toModel())
			.member(this.member.toModel())
			.deletedAt(this.deletedAt)
			.build();
	}
}
