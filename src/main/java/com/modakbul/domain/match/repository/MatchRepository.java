package com.modakbul.domain.match.repository;

import com.modakbul.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.modakbul.domain.board.entity.Board;
import com.modakbul.domain.match.entity.Matches;
import com.modakbul.domain.match.enums.MatchStatus;

@Repository
public interface MatchRepository extends JpaRepository<Matches, Long> {
	int countAllByBoardAndMatchStatus(Board board, MatchStatus matchStatus);

	@Query("SELECT m FROM Matches m "
		+ "JOIN FETCH m.sender s "
		+ "WHERE m.board.id = :boardId "
		+ "AND m.matchStatus = :matchStatus")
	List<Matches> findByBoardWithUser(@Param("boardId") Long boardId, @Param("matchStatus") MatchStatus matchStatus);

	@Query("SELECT m FROM Matches m "
		+ "JOIN FETCH m.board b "
		+ "JOIN FETCH b.cafe c "
		+ "JOIN FETCH b.category cat "
		+ "WHERE m.sender.id = :userId "
		+ "AND m.matchStatus = :status")
	List<Matches> findAllByParticipantIdWithBoard(@Param("userId") Long userId, @Param("status") MatchStatus status);

	@Query("SELECT COUNT(m) FROM Matches m "
		+ "WHERE m.board.id = :boardId "
		+ "AND m.matchStatus = :status")
	Integer countByBoardIdAndStatus(@Param("boardId") Long boardId, @Param("status") MatchStatus status);

	@Query("SELECT m FROM Matches m "
		+ "JOIN FETCH m.board b "
		+ "JOIN FETCH b.cafe c "
		+ "JOIN FETCH b.category cat "
		+ "WHERE m.sender.id = :userId "
		+ "AND m.isDeleted = :isDeleted")
	List<Matches> findAllMatchesByUserIdWithBoardDetails(@Param("userId") Long userId,
		@Param("isDeleted") boolean isDeleted);

	@Query("SELECT DISTINCT m FROM Matches m "
		+ "JOIN FETCH m.board b "
		+ "JOIN FETCH b.cafe c "
		+ "JOIN FETCH c.imageUrls i "
		+ "WHERE m.sender.id = :userId "
		+ "AND m.matchStatus = :status "
		+ "AND m.board.meetingDate < :currentDate ")
	List<Matches> findAllByParticipantIdWithCafe(@Param("userId") Long userId,
		@Param("status") MatchStatus status,
		@Param("currentDate") LocalDate currentDate);

	List<Matches> findAllByBoard(Board board);

	List<Matches> findAllBySenderId(Long senderId);

	void deleteAllByBoard(Board board);

	void deleteAllById(Long id);

	void deleteAllBySender(User user);
}
