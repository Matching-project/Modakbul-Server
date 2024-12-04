package com.modakbul.domain.notification.repository;

import com.modakbul.domain.board.entity.Board;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.modakbul.domain.notification.dto.NotificationDto;
import com.modakbul.domain.notification.entity.Notification;
import com.modakbul.domain.user.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	@Query(
		"SELECT new com.modakbul.domain.notification.dto.NotificationDto(n.id, b.id, n.title, n.type, n.subtitle, n.isRead, n.createdAt, n.sender) "
			+ "FROM Notification n JOIN n.board b "
			+ "WHERE n.receiver.id = :userId "
			+ "AND n.type != 'chat'")
	List<NotificationDto> findByUserWithBoard(Long userId);

	void deleteAllBySender(User user);
	void deleteAllByReceiver(User user);

	void deleteAllByBoard(Board board);
}
