package com.modakbul.domain.auth.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.modakbul.domain.auth.dto.AppleLoginReqDto;
import com.modakbul.domain.auth.dto.AppleSignUpReqDto;
import com.modakbul.domain.auth.dto.AuthResDto;
import com.modakbul.domain.auth.service.AppleService;
import com.modakbul.domain.user.entity.User;
import com.modakbul.global.common.response.BaseResponse;
import com.modakbul.global.common.response.BaseResponseStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AppleController {

	private final AppleService appleService;

	@PostMapping("/users/login/apple")
	public ResponseEntity<BaseResponse<AuthResDto>> login(@RequestBody AppleLoginReqDto request) throws
			IOException,
			InvalidKeySpecException,
			NoSuchAlgorithmException {
		return appleService.login(request);
	}

	@PostMapping("/users/register/apple")
	public ResponseEntity<BaseResponse<AuthResDto>> signUp(
			@RequestPart(value = "image", required = false) MultipartFile image,
			@RequestPart(value = "user") AppleSignUpReqDto request) throws
			IOException,
			InvalidKeySpecException,
			NoSuchAlgorithmException {
		return appleService.signUp(image, request);
	}

	@DeleteMapping("/users/withdrawal/apple")
	public BaseResponse<Void> withdrawal(@AuthenticationPrincipal User user,
										 @RequestHeader("Authorization") String accessToken) throws IOException {
		appleService.withdrawal(user, accessToken);
		return new BaseResponse<>(BaseResponseStatus.WITHDRAWAL_SUCCESS);
	}
}
