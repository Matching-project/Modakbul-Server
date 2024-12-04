package com.modakbul.domain.chat.chatroom.repository;

import com.modakbul.domain.board.entity.Board;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.modakbul.domain.chat.chatroom.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	Optional<ChatRoom> findByRoomHashCode(int hashCode);
	List<ChatRoom> findAllByBoard(Board board);

	void deleteAllByBoard(Board board);
}
