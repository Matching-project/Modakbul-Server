package com.modakbul.domain.cafe.entity;

import java.util.ArrayList;
import java.util.List;

import com.modakbul.domain.board.entity.Board;
import com.modakbul.domain.cafe.enums.GroupSeat;
import com.modakbul.domain.cafe.enums.Outlet;
import com.modakbul.global.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Builder
public class Cafe extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cafe_id")
	private Long id;

	@Column(nullable = false)
	private String name;

	@Embedded
	private Address address;

	@ElementCollection
	@CollectionTable(name = "cafe_image_url", joinColumns = @JoinColumn(name = "cafe_id"))
	private final List<String> imageUrls = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "cafe_opening_hour", joinColumns = @JoinColumn(name = "cafe_Id"))
	private final List<OpeningHour> openingHours = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Board> boards = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private Outlet outlet; // 콘센트

	@Enumerated(EnumType.STRING)
	private GroupSeat groupSeat; // 단체석
}
