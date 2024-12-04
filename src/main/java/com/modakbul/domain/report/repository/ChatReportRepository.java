package com.modakbul.domain.report.repository;

import com.modakbul.domain.chat.chatroom.entity.ChatRoom;
import com.modakbul.domain.user.entity.User;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.modakbul.domain.report.entity.ChatReport;
import com.modakbul.domain.report.entity.UserReport;

@Repository
public interface ChatReportRepository extends JpaRepository<ChatReport, Long> {
	@Query("select c from ChatReport c where c.reporter.id=:reporterId")
	List<ChatReport> findByReporterId(@Param("reporterId") long reporterId);

	void deleteAllByChatRoom(ChatRoom chatRoom);

	void deleteAllByReported(User reported);

	void deleteAllByReporter(User reporter);
}
