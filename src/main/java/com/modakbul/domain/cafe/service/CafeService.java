package com.modakbul.domain.cafe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.modakbul.domain.board.entity.Board;
import com.modakbul.domain.board.enums.BoardStatus;
import com.modakbul.domain.board.repository.BoardRepository;
import com.modakbul.domain.cafe.dto.CafeListResDto;
import com.modakbul.domain.cafe.entity.Cafe;
import com.modakbul.domain.cafe.repository.CafeRepository;
import com.modakbul.domain.match.entity.Matches;
import com.modakbul.domain.match.repository.MatchRepository;
import com.modakbul.global.common.response.BaseException;
import com.modakbul.global.common.response.BaseResponseStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CafeService {

	private final CafeRepository cafeRepository;
	private final BoardRepository boardRepository;
	private final MatchRepository matchRepository;

	public List<CafeListResDto> getCafeListSortByDistance(double latitude, double longitude) {
		List<Cafe> findCafes = cafeRepository.findAllByDistance(latitude, longitude);

		return findCafes.stream()
			.map(findCafe -> CafeListResDto.builder()
				.id(findCafe.getId())
				.image(findCafe.getImageUrls())
				.name(findCafe.getName())
				.meetingCount(findCafe.getBoards().stream()
					.filter(board -> (BoardStatus.CONTINUE).equals(board.getStatus()))
					.count())
				.location(findCafe.getAddress())
				.openingHour(findCafe.getOpeningHours())
				.outlet(findCafe.getOutlet())
				.groupSeat(findCafe.getGroupSeat())
				.build())
			.collect(Collectors.toList());
	}

	public List<CafeListResDto> getCafeListSortByMeetingCount(double latitude, double longitude) {
		List<Cafe> findCafes = cafeRepository.findAllByMeetingCount(latitude, longitude);

		return findCafes.stream()
			.map(findCafe -> CafeListResDto.builder()
				.id(findCafe.getId())
				.image(findCafe.getImageUrls())
				.name(findCafe.getName())
				.meetingCount(findCafe.getBoards().stream()
					.filter(board -> (BoardStatus.CONTINUE).equals(board.getStatus()))
					.count())
				.location(findCafe.getAddress())
				.openingHour(findCafe.getOpeningHours())
				.outlet(findCafe.getOutlet())
				.groupSeat(findCafe.getGroupSeat())
				.build())
			.collect(Collectors.toList());
	}

	public List<CafeListResDto> searchCafeList(String cafeName, double latitude, double longitude) {
		List<Cafe> findCafes = cafeRepository.findAllBySearch(latitude, longitude);

		return findCafes.stream()
			.filter(findCafe -> findCafe.getName().contains(cafeName))
			.map(findCafe -> CafeListResDto.builder()
				.id(findCafe.getId())
				.image(findCafe.getImageUrls())
				.name(findCafe.getName())
				.meetingCount(findCafe.getBoards().stream()
					.filter(board -> (BoardStatus.CONTINUE).equals(board.getStatus()))
					.count())
				.location(findCafe.getAddress())
				.openingHour(findCafe.getOpeningHours())
				.outlet(findCafe.getOutlet())
				.groupSeat(findCafe.getGroupSeat())
				.build())
			.collect(Collectors.toList());
	}

	public void deleteCafe(Long cafeId) {
		Cafe cafe = cafeRepository.findById(cafeId)
			.orElseThrow(() -> new BaseException(BaseResponseStatus.CAFE_NOT_FOUND));
		List<Board> findBoards = boardRepository.findAllByCafe(cafe);

		findBoards.forEach(findBoard -> {
			List<Matches> findMatches = matchRepository.findAllByBoard(findBoard);

			if (!findMatches.isEmpty()) {
				matchRepository.deleteAll(findMatches);
			}
		});

		if (!findBoards.isEmpty()) {
			boardRepository.deleteAll(findBoards);
		}
		cafeRepository.delete(cafe);
	}
}
