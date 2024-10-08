package com.modakbul.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.modakbul.domain.notification.entity.Notification;
import com.modakbul.domain.user.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findByUserId(Long userId);

	void deleteAllByUser(User user);
}
