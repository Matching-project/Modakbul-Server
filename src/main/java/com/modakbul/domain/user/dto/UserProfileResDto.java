package com.modakbul.domain.user.dto;

import java.util.List;

import com.modakbul.domain.user.enums.CategoryName;
import com.modakbul.domain.user.enums.Gender;
import com.modakbul.domain.user.enums.UserJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResDto {
	private Long id;
	private String image;
	private String nickname;
	private Gender gender;
	private UserJob job;
	private List<CategoryName> categories;
}
