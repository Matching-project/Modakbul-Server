package com.modakbul.domain.user.dto;

import java.util.List;

import com.modakbul.domain.user.enums.CategoryName;
import com.modakbul.domain.user.enums.UserJob;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyProfileReqDto {
	private Boolean isGenderVisible;
	private String nickname;
	private List<CategoryName> categories;
	private UserJob job;
}
