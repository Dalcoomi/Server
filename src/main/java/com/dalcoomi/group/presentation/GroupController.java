package com.dalcoomi.group.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.config.AuthMember;
import com.dalcoomi.group.application.GroupService;
import com.dalcoomi.group.domain.Group;
import com.dalcoomi.group.dto.request.GroupRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

	private final GroupService groupService;

	@PostMapping
	@ResponseStatus(CREATED)
	public String createGroup(@AuthMember Long memberId, @RequestBody GroupRequest request) {
		Group group = Group.from(request);

		return groupService.createGroup(memberId, group);
	}

	@PostMapping("/join/{invitationCode}")
	@ResponseStatus(OK)
	public void joinGroup(@AuthMember Long memberId, @PathVariable("invitationCode") String invitationCode) {
		groupService.joinGroup(memberId, invitationCode);
	}
}
