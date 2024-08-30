package com.modakbul.domain.information.dto;

import com.modakbul.domain.cafe.entity.Address;
import com.modakbul.domain.cafe.enums.GroupSeat;
import com.modakbul.domain.cafe.enums.Outlet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InformationReqDto {
	private String name;
	private Address location;
	private Outlet outlet;
	private GroupSeat groupSeat;
}

