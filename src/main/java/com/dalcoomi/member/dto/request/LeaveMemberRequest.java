package com.dalcoomi.member.dto.request;

import java.util.List;

import com.dalcoomi.member.dto.LeaderTransferInfo;

public record LeaveMemberRequest(
	List<LeaderTransferInfo> leaderTransferInfos
) {

}
