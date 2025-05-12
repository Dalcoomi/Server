package com.dalcoomi.group.infrastructure;

import org.springframework.stereotype.Repository;

import com.dalcoomi.group.application.repository.GroupRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepository {

	private final GroupJpaRepository groupJpaRepository;
}
