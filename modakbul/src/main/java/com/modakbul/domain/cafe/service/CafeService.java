package com.modakbul.domain.cafe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.modakbul.domain.cafe.dto.CafeResDto;
import com.modakbul.domain.cafe.entity.Cafe;
import com.modakbul.domain.cafe.repository.CafeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CafeService {

	private final CafeRepository cafeRepository;

	public List<CafeResDto.CafeListDto> getCafeListSortByDistance(double latitude, double longitude) {
		List<Cafe> findCafes = cafeRepository.findAllByDistance(latitude, longitude);

		return findCafes.stream()
			.map(findCafe -> CafeResDto.CafeListDto.builder()
				.cafeId(findCafe.getId())
				.cafeImage(findCafe.getImageUrls().get(0))
				.cafeName(findCafe.getName())
				.streetAddress(findCafe.getAddress().getStreetAddress())
				.meetingCount(findCafe.getMeetingCount())
				.openingHour(findCafe.getOpeningHours())
				.outlet(findCafe.getOutlet())
				.congestion(findCafe.getCongestion())
				.groupSeat(findCafe.getGroupSeat())
				.build())
			.collect(Collectors.toList());
	}

	public List<CafeResDto.CafeListDto> getCafeListSortByMeetingCount(double latitude, double longitude) {
		List<Cafe> findCafes = cafeRepository.findAllByMeetingCount(latitude, longitude);

		return findCafes.stream()
			.map(findCafe -> CafeResDto.CafeListDto.builder()
				.cafeId(findCafe.getId())
				.cafeImage(findCafe.getImageUrls().get(0))
				.cafeName(findCafe.getName())
				.streetAddress(findCafe.getAddress().getStreetAddress())
				.meetingCount(findCafe.getMeetingCount())
				.openingHour(findCafe.getOpeningHours())
				.outlet(findCafe.getOutlet())
				.congestion(findCafe.getCongestion())
				.groupSeat(findCafe.getGroupSeat())
				.build())
			.collect(Collectors.toList());
	}

	public List<CafeResDto.CafeListDto> searchCafeList(String cafeName, double latitude, double longitude) {
		List<Cafe> findCafes = cafeRepository.findAllByDistance(latitude, longitude);

		return findCafes.stream()
			.filter(findCafe -> findCafe.getName().contains(cafeName))
			.map(findCafe -> CafeResDto.CafeListDto.builder()
				.cafeId(findCafe.getId())
				.cafeImage(findCafe.getImageUrls().get(0))
				.cafeName(findCafe.getName())
				.meetingCount(findCafe.getMeetingCount())
				.streetAddress(findCafe.getAddress().getStreetAddress())
				.openingHour(findCafe.getOpeningHours())
				.outlet(findCafe.getOutlet())
				.congestion(findCafe.getCongestion())
				.groupSeat(findCafe.getGroupSeat())
				.build())
			.collect(Collectors.toList());
	}
}