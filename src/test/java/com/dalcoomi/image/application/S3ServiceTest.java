package com.dalcoomi.image.application;

import static com.dalcoomi.image.constant.ImageConstants.S3_PROFILE_IMAGE_PATH;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.TIFF_TAG_ORIENTATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.dalcoomi.common.error.exception.ImageException;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.image.infrastructure.S3Adapter;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.dto.AvatarInfo;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

	private static final String IMAGE_BASE_URL = "https://bucket.s3.amazonaws.com/";

	@InjectMocks
	private S3Service s3Service;

	@Mock
	private S3Adapter s3Adapter;

	private BufferedImage createTestImage() {
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 100, 50);
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 50, 100, 50);
		graphics.dispose();

		return image;
	}

	private byte[] createDummyImageData(int orientation) throws IOException {
		BufferedImage image = createTestImage();

		// 생성된 이미지를 JPG 형식의 바이트 배열로 변환
		ByteArrayOutputStream initialBaos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", initialBaos);
		byte[] imageBytes = initialBaos.toByteArray();

		// EXIF orientation 추가
		ByteArrayOutputStream finalBaos = new ByteArrayOutputStream();
		TiffOutputSet outputSet = new TiffOutputSet();
		TiffOutputDirectory rootDirectory = outputSet.getOrCreateRootDirectory();

		// 3 = 180도 회전
		rootDirectory.add(TIFF_TAG_ORIENTATION, (short)orientation);

		// 기존 이미지에 EXIF 메타데이터를 추가하여 새로운 이미지 생성
		new ExifRewriter().updateExifMetadataLossless(imageBytes, finalBaos, outputSet);

		return finalBaos.toByteArray();
	}

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(s3Service, "imageBaseUrl", IMAGE_BASE_URL);
	}

	@Test
	@DisplayName("회원 프로필 사진 수정 성공")
	void update_profile_image_success() throws Exception {
		// given
		String currentImageUrl = IMAGE_BASE_URL + "profile/old-image.jpg";
		Member member = spy(MemberFixture.getMember1());
		when(member.getProfileImageUrl()).thenReturn(currentImageUrl);

		AvatarInfo avatarInfo = AvatarInfo.builder()
			.member(member)
			.defaultImage(false)
			.build();

		MockMultipartFile newImage = new MockMultipartFile("image", "new.jpg", IMAGE_JPEG_VALUE,
			createDummyImageData(1));
		String expectedNewUrl = IMAGE_BASE_URL + "profile/new-123.jpg";

		given(s3Adapter.uploadFile(any(byte[].class), anyString(), anyString())).willReturn(expectedNewUrl);
		doNothing().when(s3Adapter).deleteFile(anyString());

		// when
		String result = s3Service.updateAvatar(false, avatarInfo, newImage);

		// then
		assertThat(result).isEqualTo(expectedNewUrl);
		verify(s3Adapter).deleteFile("profile/old-image.jpg");
		verify(s3Adapter).uploadFile(any(byte[].class), eq(S3_PROFILE_IMAGE_PATH), eq("jpg"));
	}

	@Test
	@DisplayName("회원 프로필 사진 삭제 성공")
	void delete_profile_image_success() {
		// given
		String currentImageUrl = IMAGE_BASE_URL + "profile/current-image.jpg";
		Member member = spy(MemberFixture.getMember1());
		when(member.getProfileImageUrl()).thenReturn(currentImageUrl);

		AvatarInfo avatarInfo = AvatarInfo.builder()
			.member(member)
			.defaultImage(false)
			.build();

		doNothing().when(s3Adapter).deleteFile(anyString());

		// when
		String result = s3Service.updateAvatar(true, avatarInfo, null);

		// then
		assertThat(result).isNull();
		verify(s3Adapter).deleteFile("profile/current-image.jpg");
		verify(s3Adapter, never()).uploadFile(any(), anyString(), anyString());
	}

	@Test
	@DisplayName("기본 이미지 상태에서 삭제 요청 시 현재 URL 반환 성공")
	void delete_default_image_returns_current_url_success() {
		// given
		Member member = MemberFixture.getMember1();
		AvatarInfo avatarInfo = AvatarInfo.builder()
			.member(member)
			.defaultImage(true)
			.build();

		String currentUrl = member.getProfileImageUrl();

		// when
		String result = s3Service.updateAvatar(true, avatarInfo, null);

		// then
		assertThat(result).isEqualTo(currentUrl);
		verify(s3Adapter, never()).deleteFile(anyString());
	}

	@Test
	@DisplayName("지원하지 않는 이미지 형식으로 업로드 실패")
	void upload_profile_image_unsupported_format_error() throws IOException {
		// given
		Member member = MemberFixture.getMember1();
		AvatarInfo avatarInfo = AvatarInfo.builder().member(member).defaultImage(false).build();
		MockMultipartFile image = new MockMultipartFile("image", "test.gif", "image/gif", createDummyImageData(1));

		// when & then
		assertThrows(ImageException.class, () -> s3Service.updateAvatar(false, avatarInfo, image));
	}

	@Test
	@DisplayName("orientation이 3일 때 이미지 180도 회전 성공")
	void rotate_image_orientation_3_success() throws IOException {
		// given
		BufferedImage originalImage = createTestImage();
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", IMAGE_JPEG_VALUE, createDummyImageData(3));

		// when
		BufferedImage rotatedImage = ReflectionTestUtils.invokeMethod(s3Service, "rotateImageIfRequired", originalImage,
			image);

		// then
		assertThat(requireNonNull(rotatedImage).getRGB(50, 25)).isEqualTo(Color.BLACK.getRGB());
		assertThat(requireNonNull(rotatedImage).getRGB(50, 75)).isEqualTo(Color.WHITE.getRGB());
	}

	@Test
	@DisplayName("orientation이 6일 때 이미지 90도 회전 성공")
	void rotate_image_orientation_6_success() throws IOException {
		// given
		BufferedImage originalImage = createTestImage();
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", IMAGE_JPEG_VALUE, createDummyImageData(6));

		// when
		BufferedImage rotatedImage = ReflectionTestUtils.invokeMethod(s3Service, "rotateImageIfRequired",
			originalImage, image);

		// then
		assertThat(requireNonNull(rotatedImage).getRGB(25, 50)).isEqualTo(Color.BLACK.getRGB());
		assertThat(requireNonNull(rotatedImage).getRGB(75, 50)).isEqualTo(Color.WHITE.getRGB());
	}
}
