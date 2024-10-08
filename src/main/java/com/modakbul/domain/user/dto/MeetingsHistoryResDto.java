package com.modakbul.domain.user.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.modakbul.domain.board.enums.BoardStatus;
import com.modakbul.domain.user.enums.CategoryName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingsHistoryResDto {
	private Long boardId;
	private String title;
	private CategoryName categoryName;
	private LocalDate meetingDate;
	private LocalTime startTime;
	private LocalTime endTime;
	private BoardStatus boardStatus;
	private Long cafeId;
	private String cafeName;
	private String roadName;
}
