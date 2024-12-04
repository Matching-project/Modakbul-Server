package com.modakbul.domain.cafe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.modakbul.domain.cafe.entity.Cafe;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, Long> {

	@Query(value = "SELECT c.*, "
			+ "(6371 * ACOS(COS(RADIANS(:userLatitude)) * COS(RADIANS(latitude)) * COS(RADIANS(longitude) - RADIANS(:userLongitude)) + SIN(RADIANS(:userLatitude)) * SIN(RADIANS(latitude)))) AS distance "
			+ "FROM cafe c "
			+ "LEFT JOIN board b ON c.cafe_id = b.cafe_id "
			+ "GROUP BY c.cafe_id "
			+ "HAVING distance <= 2 "
			+ "ORDER BY distance ASC;",
			nativeQuery = true)
	List<Cafe> findAllByDistance(@Param("userLatitude") double userLatitude,
								 @Param("userLongitude") double userLongitude);

	@Query(value = "SELECT c.*, "
			+ "(6371 * ACOS(COS(RADIANS(:userLatitude)) * COS(RADIANS(latitude)) * COS(RADIANS(longitude) - RADIANS(:userLongitude)) + SIN(RADIANS(:userLatitude)) * SIN(RADIANS(latitude)))) AS distance "
			+ "FROM cafe c "
			+ "LEFT JOIN board b ON c.cafe_id = b.cafe_id "
			+ "GROUP BY c.cafe_id "
			+ "ORDER BY distance ASC;",
			nativeQuery = true)
	List<Cafe> findAllBySearch(@Param("userLatitude") double userLatitude,
										   @Param("userLongitude") double userLongitude);

	@Query(value = "SELECT c.*, "
			+ "(6371 * ACOS(COS(RADIANS(:userLatitude)) * COS(RADIANS(latitude)) * COS(RADIANS(longitude) - RADIANS(:userLongitude)) + SIN(RADIANS(:userLatitude)) * SIN(RADIANS(latitude)))) AS distance, "
			+ "(SELECT COUNT(b.board_id) "
			+ "FROM board b "
			+ "WHERE b.cafe_id = c.cafe_id "
			+ "AND b.status = 'CONTINUE') AS meetingCount "
			+ "FROM cafe c "
			+ "HAVING distance <= 2 "
			+ "ORDER BY meetingCount DESC, distance ASC; ",
			nativeQuery = true)
	List<Cafe> findAllByMeetingCount(@Param("userLatitude") double userLatitude,
									 @Param("userLongitude") double userLongitude);
}
