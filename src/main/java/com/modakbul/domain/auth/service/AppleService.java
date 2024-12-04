package com.modakbul.domain.auth.service;

import com.modakbul.domain.auth.entity.LogoutToken;
import com.modakbul.domain.auth.repository.LogoutTokenRepository;
import com.modakbul.domain.block.repository.BlockRepository;
import com.modakbul.domain.board.entity.Board;
import com.modakbul.domain.board.repository.BoardRepository;
import com.modakbul.domain.chat.chatroom.entity.ChatRoom;
import com.modakbul.domain.chat.chatroom.repository.ChatRoomRepository;
import com.modakbul.domain.chat.chatroom.repository.UserChatRoomRepository;
import com.modakbul.domain.information.repository.InformationRepository;
import com.modakbul.domain.match.repository.MatchRepository;
import com.modakbul.domain.notification.repository.NotificationRepository;
import com.modakbul.domain.report.repository.ChatReportRepository;
import com.modakbul.domain.report.repository.UserReportRepository;
import com.modakbul.domain.review.repository.ReviewRepository;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modakbul.domain.auth.dto.AppleLoginReqDto;
import com.modakbul.domain.auth.dto.ApplePublicKeyDto;
import com.modakbul.domain.auth.dto.AppleSignUpReqDto;
import com.modakbul.domain.auth.dto.AuthResDto;
import com.modakbul.domain.auth.entity.AppleRefreshToken;
import com.modakbul.domain.auth.entity.RefreshToken;
import com.modakbul.domain.auth.repository.AppleRefreshTokenRepository;
import com.modakbul.domain.auth.repository.RefreshTokenRepository;
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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleService {
	@Value("${apple.team.id}")
	private String APPLE_TEAM_ID;

	@Value("${apple.login.key}")
	private String APPLE_LOGIN_KEY;

	@Value("${apple.client.id}")
	private String APPLE_CLIENT_ID;

	@Value("${apple.key.path}")
	private String APPLE_KEY_PATH;

	private final static String APPLE_AUTH_URL = "https://appleid.apple.com";

	private final UserRepository userRepository;
	private final UserCategoryRepository userCategoryRepository;
	private final CategoryRepository categoryRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProvider jwtProvider;
	@Value("${jwt.refresh-token.expiration-time}")
	private Long refreshTokenExpirationTime;
	private final S3ImageService s3ImageService;
	private final AppleRefreshTokenRepository appleRefreshTokenRepository;
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
	public ResponseEntity<BaseResponse<AuthResDto>> login(AppleLoginReqDto request) {
		HttpHeaders httpHeaders = new HttpHeaders();
		User findUser = userRepository.findByProvideIdAndProvider(request.getProvideId(), Provider.APPLE).orElse(null);

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

	public ResponseEntity<BaseResponse<AuthResDto>> signUp(MultipartFile image, AppleSignUpReqDto request) throws
			IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		JsonNode node = getNode(request.getAuthorizationCode());

		log.info("authorizationCode: {}", request.getAuthorizationCode());
		log.info("refreshToken: {}", node.path("refresh_token").asText());

		String provideId = (String) getClaims(node.path("id_token").asText()).get("sub");
		Provider provider = Provider.APPLE;

		log.info("provideId: {}", provideId);

		User findUser = userRepository.findByProvideIdAndProvider(provideId, provider).orElse(null);

		if (findUser != null) {
			throw new BaseException(BaseResponseStatus.USER_EXIST);
		}

		String accessToken = jwtProvider.createAccessToken(provider, provideId, request.getNickname());
		String refreshToken = jwtProvider.createRefreshToken(provider, provideId, request.getNickname());

		User addUser = User.builder()
				.provideId(provideId)
				.provider(provider)
				.birth(request.getBirth())
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

		AppleRefreshToken addAppleRefreshToken = new AppleRefreshToken(addUser.getId(),
				node.path("refresh_token").asText());
		appleRefreshTokenRepository.save(addAppleRefreshToken);

		RefreshToken addRefreshToken = new RefreshToken(addUser.getId(), refreshToken, refreshTokenExpirationTime);
		refreshTokenRepository.save(addRefreshToken);

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer " + accessToken);
		httpHeaders.set("Authorization_refresh", "Bearer " + refreshToken);

		AuthResDto authResDto = AuthResDto.builder().userId(addUser.getId()).build();
		return new ResponseEntity<>(new BaseResponse<>(BaseResponseStatus.LOGIN_SUCCESS, authResDto),
				httpHeaders, HttpStatus.OK);
	}

/*	public void withdrawal(User user, String accessToken) throws IOException {
		revoke(user);

		user.updateUserStatus(UserStatus.DELETED);
		userRepository.save(user);

		Long expiration = jwtProvider.getExpiration(accessToken);

		refreshTokenRepository.deleteById(user.getId());
		logoutTokenRepository.save(new LogoutToken(accessToken, expiration / 1000));
	}*/

	@Transactional
	public void withdrawal(User user, String accessToken) throws IOException {
		revoke(user);

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

	public void revoke(User user) throws IOException {
		AppleRefreshToken findAppleRefreshToken = appleRefreshTokenRepository.findById(user.getId())
				.orElseThrow(() -> new BaseException(BaseResponseStatus.REFRESHTOKEN_EXPIRED));

		RestTemplate restTemplate = new RestTemplateBuilder().build();
		String revokeUrl = APPLE_AUTH_URL + "/auth/revoke";

		LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", APPLE_CLIENT_ID);
		params.add("client_secret", createClientSecret());
		params.add("token", findAppleRefreshToken.getRefreshToken());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

		restTemplate.postForEntity(revokeUrl, httpEntity, String.class);
	}

	public Claims getClaims(String idToken) throws //idToken 정보
			JsonProcessingException,
			UnsupportedEncodingException,
			InvalidKeySpecException,
			NoSuchAlgorithmException {
		List<ApplePublicKeyDto> applePublicKeys = getPublicKey();

		String headerOfIdToken = idToken.substring(0, idToken.indexOf("."));
		Map<String, String> header = new ObjectMapper().readValue(
				new String(Base64.getDecoder().decode(headerOfIdToken), "UTF-8"), Map.class);
		ApplePublicKeyDto applePublicKey = applePublicKeys.stream()
				.filter(key -> key.getKid().equals(header.get("kid")) && key.getAlg().equals(header.get("alg")))
				.findFirst()
				.orElseThrow(() -> new BaseException(BaseResponseStatus.SEARCH_APPLE_PUBLIC_KEY_FAILED));

		byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.getN());
		byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.getE());

		BigInteger n = new BigInteger(1, nBytes);
		BigInteger e = new BigInteger(1, eBytes);

		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
		KeyFactory keyFactory = KeyFactory.getInstance(applePublicKey.getKty());
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

		return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(idToken).getBody();
	}

	public List<ApplePublicKeyDto> getPublicKey() throws JsonProcessingException {
		RestTemplate restTemplate = new RestTemplateBuilder().build();
		String authUrl = APPLE_AUTH_URL + "/auth/keys";

		ResponseEntity<String> response = restTemplate.getForEntity(authUrl, String.class);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode publicKeys = mapper.readTree(response.getBody()).get("keys");

		List<ApplePublicKeyDto> applePublicKeys = new ArrayList<>();
		for (JsonNode publicKey : publicKeys) {
			ApplePublicKeyDto applePublicKey = ApplePublicKeyDto.builder()
					.kty(publicKey.get("kty").asText())
					.kid(publicKey.get("kid").asText())
					.use(publicKey.get("use").asText())
					.alg(publicKey.get("alg").asText())
					.n(publicKey.get("n").asText())
					.e(publicKey.get("e").asText())
					.build();
			applePublicKeys.add(applePublicKey);
		}

		return applePublicKeys;
	}

	private String createClientSecret() throws IOException {
		Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());

		return Jwts.builder()
				.setHeaderParam("kid", APPLE_LOGIN_KEY)
				.setHeaderParam("alg", "ES256")
				.setIssuer(APPLE_TEAM_ID) //애플 개발자 Team ID
				.setIssuedAt(new Date(System.currentTimeMillis())) //발급 시각 (issued at)
				.setExpiration(expirationDate) //만료 시각 (발급으로부터 6개월 미만)
				.setAudience(APPLE_AUTH_URL) //"https://appleid.apple.com/"
				.setSubject(APPLE_CLIENT_ID) //App bundle ID
				.signWith(SignatureAlgorithm.ES256, getPrivateKey())
				.compact();
	}

	private PrivateKey getPrivateKey() throws IOException {
		Resource resource = new FileSystemResource(APPLE_KEY_PATH);
		if (!resource.exists()) {
			System.out.println("파일이 존재하지 않습니다.");
		}
		String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
		Reader pemReader = new StringReader(privateKey);
		PEMParser pemParser = new PEMParser(pemReader);
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
		PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
		return converter.getPrivateKey(object);
	}

	public JsonNode getNode(String code) throws IOException { //회원 정보 얻을 수 있는 함수
		RestTemplate restTemplate = new RestTemplateBuilder().build();
		String authUrl = APPLE_AUTH_URL + "/auth/token";

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("code", code);
		params.add("client_id", APPLE_CLIENT_ID);
		params.add("client_secret", createClientSecret());
		params.add("grant_type", "authorization_code");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(authUrl, httpEntity, String.class);

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(response.getBody());
	}
}
