package com.modakbul.domain.auth.dto;

import java.util.List;

import com.modakbul.domain.user.enums.Gender;
import com.modakbul.domain.user.enums.Job;
import com.modakbul.domain.user.enums.Provider;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequestDto {
	@Builder
	@Getter
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class loginDto {
		private String email;
		@Enumerated(EnumType.STRING)
		private Provider provider;
	}

	@Builder
	@Getter
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class signUpDto {
		private String email;
		@Enumerated(EnumType.STRING)
		private Provider provider;
		private String name;
		private String birth;
		private Gender gender;
		private String image;
		private String nickname;
		private Job job;
		private List<String> categoryNames;
	}
}