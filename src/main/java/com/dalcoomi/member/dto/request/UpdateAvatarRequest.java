package com.dalcoomi.member.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.dalcoomi.common.validator.ValidProfileImage;

import jakarta.validation.constraints.NotNull;

public record UpdateAvatarRequest(
	@NotNull(message = "프로필 사진 삭제 여부는 필수입니다.")
	Boolean removeAvatar,

	@ValidProfileImage
	MultipartFile profileImage
) {

}
