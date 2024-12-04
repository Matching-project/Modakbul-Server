package com.modakbul.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.modakbul.domain.board.entity.Board;
import com.modakbul.domain.board.repository.BoardRepository;
import com.modakbul.domain.notification.dto.DeleteNotificationsReqDto;
import com.modakbul.domain.notification.dto.FcmNotificationDto;
import com.modakbul.domain.notification.dto.NotificationDto;
import com.modakbul.domain.notification.dto.NotificationListResDto;
import com.modakbul.domain.notification.dto.NotificationReqDto;
import com.modakbul.domain.notification.entity.Notification;
import com.modakbul.domain.notification.repository.NotificationRepository;
import com.modakbul.domain.user.entity.User;
import com.modakbul.domain.user.repository.UserRepository;
import com.modakbul.global.common.response.BaseException;
import com.modakbul.global.common.response.BaseResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
	private final FirebaseMessaging firebaseMessaging;
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	private final BoardRepository boardRepository;

	@Transactional
	public void sendAndSaveNotification(User user, Long targetId, NotificationReqDto notificationDto) throws
			FirebaseMessagingException {
		log.info("targetId: {}", targetId);
		log.info("boardId: {}, title: {}, subtitle: {}, type: {}", notificationDto.getBoardId(),
				notificationDto.getTitle(), notificationDto.getSubtitle(), notificationDto.getType());

		User findTargetUser = userRepository.findById(targetId)
				.orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_EXIST));
		Board findBoard = boardRepository.findById(notificationDto.getBoardId())
				.orElseThrow(() -> new BaseException(BaseResponseStatus.BOARD_NOT_FOUND));

		FcmNotificationDto fcmDto = FcmNotificationDto.builder()
				.title(notificationDto.getTitle())
				.subtitle(notificationDto.getSubtitle())
				.build();

		Message message = Message.builder()
				.setToken(findTargetUser.getFcmToken())
				.setNotification(fcmDto.toNotification())
				.putData("type", notificationDto.getType())
				.putData("boardId", String.valueOf(notificationDto.getBoardId()))
				//.putData("sound", "bingbong.aiff")
				//.putData("badge", "1")
				.build();
		firebaseMessaging.send(message);

		Notification notification = Notification.builder()
				.receiver(findTargetUser)
				.sender(user)
				.board(findBoard)
				.title(notificationDto.getTitle())
				.subtitle(notificationDto.getSubtitle())
				.type(notificationDto.getType())
				.isRead(false)
				.build();
		notificationRepository.save(notification);
	}

	@Transactional(readOnly = true)
	public List<NotificationListResDto> getNotificationList(User user) {
		List<NotificationDto> findNotificationList = notificationRepository.findByUserWithBoard(user.getId());

		return findNotificationList.stream()
				.map(notification -> NotificationListResDto.builder()
						.id(notification.getId())
						.boardId(notification.getBoardId())
						.title(notification.getTitle())
						.thumbnail(notification.getSender().getImage())
						.type(notification.getType())
						.content(notification.getContent())
						.isRead(notification.getIsRead())
						.createdAt(notification.getCreatedAt())
						.build()).toList();
	}

	@Transactional
	public void deleteNotifications(DeleteNotificationsReqDto deleteNotificationsReqDto) {
		if (deleteNotificationsReqDto.getNotificationIds() != null && !deleteNotificationsReqDto.getNotificationIds()
				.isEmpty()) {
			notificationRepository.deleteAllById(deleteNotificationsReqDto.getNotificationIds());
		}
	}

	@Transactional
	public void readNotification(Long id) {
		Notification findNotification = notificationRepository.findById(id)
				.orElseThrow(() -> new BaseException(BaseResponseStatus.NOTIFICATION_NOT_EXIST));

		findNotification.readNotification();
	}
}
