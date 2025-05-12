package com.dalcoomi.group.application;

import org.springframework.stereotype.Service;

import com.dalcoomi.group.application.repository.GroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

	private final GroupRepository groupRepository;
}
