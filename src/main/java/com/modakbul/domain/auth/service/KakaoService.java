package com.modakbul.domain.auth.service;

import com.modakbul.domain.chat.chatroom.entity.ChatRoom;
import com.modakbul.domain.chat.chatroom.repository.ChatRoomRepository;
import com.modakbul.domain.chat.chatroom.repository.UserChatRoomRepository;
import com.modakbul.domain.report.repository.ChatReportRepository;
import java.util.List;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.modakbul.domain.auth.dto.AuthResDto;
import com.modakbul.domain.auth.dto.KakaoLoginReqDto;
import com.modakbul.domain.auth.dto.KakaoSignUpReqDto;
import com.modakbul.domain.auth.entity.LogoutToken;
import com.modakbul.domain.auth.entity.RefreshToken;
import com.modakbul.domain.auth.repository.LogoutTokenRepository;
import com.modakbul.domain.auth.repository.RefreshTokenRepository;
import com.modakbul.domain.block.repository.BlockRepository;
import com.modakbul.domain.board.entity.Board;
import com.modakbul.domain.board.repository.BoardRepository;
import com.modakbul.domain.information.repository.InformationRepository;
import com.modakbul.domain.match.entity.Matches;
import com.modakbul.domain.match.repository.MatchRepository;
import com.modakbul.domain.notification.repository.NotificationRepository;
import com.modakbul.domain.report.repository.UserReportRepository;
import com.modakbul.domain.review.repository.ReviewRepository;
import com.modakbul.domain.user.entity.User;
import com.modakbul.domain.user.entity.UserCategory;
import com.modakbul.domain.user.enums.Provider;
import com.modakbul.domain.user.enums.UserRole;
import com.modakbul.domain.user.enums.UserStatus;
import com.modakbul.domain.user.repository.CategoryRepository;
import com.modakbul.domain.user.repository.UserCategoryRepository;
import com.modakbul.domain.user.repository.UserRepository;
import com.modakbul.global.auth.jwt.JwtProvider;
import com.modakbul.global.common.response.BaseException;
import com.modakbul.global.common.response.BaseResponse;
import com.modakbul.global.common.response.BaseResponseStatus;
import com.modakbul.global.s3.service.S3ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

	private final UserRepository userRepository;
	private final UserCategoryRepository userCategoryRepository;
	private final CategoryRepository categoryRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProvider jwtProvider;
	@Value("${jwt.refresh-token.expiration-time}")
	private Long refreshTokenExpirationTime;
	private final S3ImageService s3ImageService;
	private final LogoutTokenRepository logoutTokenRepository;

	private final BoardRepository boardRepository;
	private final MatchRepository matchRepository;
	private final InformationRepository informationRepository;
	private final BlockRepository blockRepository;
	private final ReviewRepository reviewRepository;
	private final UserReportRepository userReportRepository;
	private final NotificationRepository notificationRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatReportRepository chatReportRepository;
	private final UserChatRoomRepository userChatRoomRepository;

	@Transactional
	public ResponseEntity<BaseResponse<AuthResDto>> login(KakaoLoginReqDto request) {
		HttpHeaders httpHeaders = new HttpHeaders();
		User findUser = userRepository.findByProvideIdAndProvider(request.getEmail(),
				Provider.KAKAO).orElse(null);

		if (findUser == null) {
			AuthResDto authResDto = AuthResDto.builder().userId(-1L).build();
			return new ResponseEntity<>(new BaseResponse<>(BaseResponseStatus.USER_NOT_EXIST, authResDto),
					httpHeaders, HttpStatus.OK);
		} else if ((findUser.getUserStatus()).equals(UserStatus.DELETED)) {
			throw new BaseException(BaseResponseStatus.WITHDRAWAL_USER);
		} else {
			findUser.updateFcmToken(request.getFcm());

			String accessToken = jwtProvider.createAccessToken(findUser.getProvider(), findUser.getProvideId(),
					findUser.getNickname());
			String refreshToken = jwtProvider.createRefreshToken(findUser.getProvider(), findUser.getProvideId(),
					findUser.getNickname());

			RefreshToken addRefreshToken = new RefreshToken(findUser.getId(), refreshToken, refreshTokenExpirationTime);
			refreshTokenRepository.save(addRefreshToken);

			httpHeaders.set("Authorization", "Bearer " + accessToken);
			httpHeaders.set("Authorization_refresh", "Bearer " + refreshToken);

			AuthResDto authResDto = AuthResDto.builder().userId(findUser.getId()).build();
			return new ResponseEntity<>(new BaseResponse<>(BaseResponseStatus.LOGIN_SUCCESS, authResDto),
					httpHeaders, HttpStatus.OK);
		}
	}

	public ResponseEntity<BaseResponse<AuthResDto>> signUp(MultipartFile image, KakaoSignUpReqDto request) {
		User findUser = userRepository.findByProvideIdAndProvider(request.getEmail(),
				Provider.KAKAO).orElse(null);

		if (findUser != null) {
			throw new BaseException(BaseResponseStatus.USER_EXIST);
		}

		String accessToken = jwtProvider.createAccessToken(Provider.KAKAO, request.getEmail(),
				request.getNickname());
		String refreshToken = jwtProvider.createRefreshToken(Provider.KAKAO, request.getEmail(),
				request.getNickname());

		User addUser = User.builder()
				.provideId(request.getEmail())
				.provider(Provider.KAKAO)
				.name(request.getName())
				.nickname(request.getNickname())
				.gender(request.getGender())
				.userJob(request.getJob())
				.isVisible(true)
				.image(s3ImageService.upload(image))
				.userRole(UserRole.NORMAL)
				.userStatus(UserStatus.ACTIVE)
				.fcmToken(request.getFcm())
				.build();
		userRepository.save(addUser);

		request.getCategories().forEach(categoryName ->
				categoryRepository.findByCategoryName(categoryName)
						.ifPresentOrElse(
								category -> userCategoryRepository.save(
										UserCategory.builder()
												.user(addUser)
												.category(category)
												.build()
								),
								() -> {
									throw new BaseException(BaseResponseStatus.CATEGORY_NOT_EXIST);
								}
						)
		);

		RefreshToken addRefreshToken = new RefreshToken(addUser.getId(), refreshToken, refreshTokenExpirationTime);
		refreshTokenRepository.save(addRefreshToken);

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer " + accessToken);
		httpHeaders.set("Authorization_refresh", "Bearer " + refreshToken);

		AuthResDto authResDto = AuthResDto.builder().userId(addUser.getId()).build();
		return new ResponseEntity<>(new BaseResponse<>(BaseResponseStatus.REGISTER_SUCCESS, authResDto),
				httpHeaders, HttpStatus.OK);
	}

/*	public void withdrawal(User user) {
		user.updateUserStatus(UserStatus.DELETED);
		userRepository.save(user);

		Long expiration = jwtProvider.getExpiration(accessToken);

		refreshTokenRepository.deleteById(user.getId());
		logoutTokenRepository.save(new LogoutToken(accessToken, expiration / 1000));
	}*/

	@Transactional
	public void withdrawal(User user, String accessToken) {
		List<Board> findBoards = boardRepository.findAllByUser(user);
		List<ChatRoom> findChatRooms = findBoards.stream()
				.flatMap(findBoard -> chatRoomRepository.findAllByBoard(findBoard).stream())
				.toList();

		//chatRoom를 참조하는 데이터 삭제
		findChatRooms.forEach(chatRoom -> {
			userChatRoomRepository.deleteAllByChatRoom(chatRoom);
			chatReportRepository.deleteAllByChatRoom(chatRoom);
		});

		chatReportRepository.deleteAllByReported(user);
		chatReportRepository.deleteAllByReporter(user);

		userChatRoomRepository.deleteAllByUser(user);

		findBoards.forEach(board -> {
			chatRoomRepository.deleteAllByBoard(board);
			matchRepository.deleteAllByBoard(board);
			notificationRepository.deleteAllByBoard(board);
		});

		matchRepository.deleteAllBySender(user);
		notificationRepository.deleteAllBySender(user);
		notificationRepository.deleteAllByReceiver(user);
		boardRepository.deleteAllByUser(user);

		informationRepository.deleteAllByUser(user);
		blockRepository.deleteAllByBlockedId(user);
		blockRepository.deleteAllByBlockerId(user);
		reviewRepository.deleteAllByUser(user);
		userReportRepository.deleteAllByReported(user);
		userReportRepository.deleteAllByReporter(user);

		userCategoryRepository.deleteAllByUser(user);
		userRepository.delete(user);

		Long expiration = jwtProvider.getExpiration(accessToken);

		refreshTokenRepository.deleteById(user.getId());
		logoutTokenRepository.save(new LogoutToken(accessToken, expiration / 1000));
	}
}
