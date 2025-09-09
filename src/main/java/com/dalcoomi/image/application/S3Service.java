package com.dalcoomi.image.application;

import static com.dalcoomi.common.error.model.ErrorMessage.IMAGE_NOT_FOUND;
import static com.dalcoomi.common.error.model.ErrorMessage.IMAGE_NOT_SUPPORT;
import static com.dalcoomi.image.constant.ImageConstants.IMAGE_QUALITY;
import static com.dalcoomi.image.constant.ImageConstants.MAX_HEIGHT;
import static com.dalcoomi.image.constant.ImageConstants.MAX_WIDTH;
import static com.dalcoomi.image.constant.ImageConstants.S3_PROFILE_IMAGE_PATH;
import static com.drew.metadata.exif.ExifDirectoryBase.TAG_ORIENTATION;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

import com.dalcoomi.common.error.exception.ImageException;
import com.dalcoomi.image.infrastructure.S3Adapter;
import com.dalcoomi.member.dto.AvatarInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	protected static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "svg");

	private final S3Adapter s3Adapter;

	@Value("${aws.s3.image-base-url}")
	private String imageBaseUrl;

	public String uploadImage(String defaultImage, String folderPath, @Nullable MultipartFile image) {
		if (image == null) {
			return defaultImage;
		}

		String extension = validateImageFormat(image);

		try {
			StopWatch sw = new StopWatch();
			sw.start();
			byte[] processedImage = processImage(image, extension);
			sw.stop();

			log.debug("========== 이미지 처리 시간: {}ms ==========", sw.getTotalTimeMillis());

			return s3Adapter.uploadFile(processedImage, folderPath, extension);
		} catch (Exception e) {
			return defaultImage;
		}
	}

	public void deleteImage(String imageUrl) {
		try {
			String s3Key = imageUrl.replace(imageBaseUrl, "");

			s3Adapter.deleteFile(s3Key);
		} catch (Exception e) {
			log.error("이미지 삭제 실패: {}", imageUrl, e);
		}
	}

	public String updateAvatar(boolean removeAvatar, AvatarInfo avatarInfo, @Nullable MultipartFile multipartFile) {
		String currentAvatarUrl = avatarInfo.member().getProfileImageUrl();

		// 프사 등록 or 수정
		if (!removeAvatar && multipartFile != null) {
			// 수정 시
			if (!avatarInfo.defaultImage()) {
				// s3에서 현재 url 삭제
				deleteImage(currentAvatarUrl);
			}

			// s3에 저장 후 링크 리턴
			return uploadImage(currentAvatarUrl, S3_PROFILE_IMAGE_PATH, multipartFile);
		}

		// 프사 삭제
		if (removeAvatar && !avatarInfo.defaultImage()) {
			deleteImage(currentAvatarUrl);

			return null;
		} else {
			return currentAvatarUrl;
		}
	}

	private String validateImageFormat(MultipartFile image) {
		String originalFilename = image.getOriginalFilename();

		if (originalFilename == null) {
			throw new ImageException(IMAGE_NOT_FOUND);
		}

		String extension = getExtension(originalFilename).toLowerCase();

		if (!SUPPORTED_FORMATS.contains(extension)) {
			throw new ImageException(IMAGE_NOT_SUPPORT);
		}

		return extension;
	}

	private String getExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');

		if (lastDotIndex == -1) {
			throw new ImageException(IMAGE_NOT_FOUND);
		}

		return filename.substring(lastDotIndex + 1);
	}

	private byte[] processImage(MultipartFile image, String extension) throws IOException {
		if ("svg".equals(extension)) {
			return image.getBytes();
		}

		// 원본 이미지 읽기
		BufferedImage originalImage = ImageIO.read(image.getInputStream());

		// 리사이징
		BufferedImage resizedImage = Thumbnails.of(originalImage)
			.size(MAX_WIDTH, MAX_HEIGHT)
			.keepAspectRatio(true)
			.asBufferedImage();

		// 리사이즈된 이미지를 회전
		resizedImage = rotateImageIfRequired(resizedImage, image);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Thumbnails.of(resizedImage)
			.scale(1.0)  // 크기는 그대로
			.outputQuality(IMAGE_QUALITY)
			.outputFormat(extension)
			.toOutputStream(outputStream);

		return outputStream.toByteArray();
	}

	private BufferedImage rotateImageIfRequired(BufferedImage image, MultipartFile multipartFile) throws IOException {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(multipartFile.getInputStream());
			Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

			if (directory != null && directory.containsTag(TAG_ORIENTATION)) {
				int orientation = directory.getInt(TAG_ORIENTATION);

				return switch (orientation) {
					case 3 -> Thumbnails.of(image).scale(1.0).rotate(180).asBufferedImage();
					case 6 -> Thumbnails.of(image).scale(1.0).rotate(90).asBufferedImage();
					case 8 -> Thumbnails.of(image).scale(1.0).rotate(270).asBufferedImage();
					default -> image;
				};
			}
		} catch (ImageProcessingException | MetadataException e) {
			log.warn("이미지 방향 정보 읽기 실패", e);
		}

		return image;
	}
}
