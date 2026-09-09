package com.ijse.heavenlyStay.entity;

import com.ijse.heavenlyStay.enumeration.BoardingStatus;
import com.ijse.heavenlyStay.enumeration.GenderPreference;
import com.ijse.heavenlyStay.enumeration.RoomCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "boardings")
public class Boarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardingId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(length = 100)
    private String province;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Column(precision = 12, scale = 2)
    private BigDecimal keyMoney;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenderPreference genderPreference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomCategory roomCategory = RoomCategory.SINGLE;

    @Column(nullable = false)
    private Integer bedsPerRoom = 1;

    @Column(nullable = false)
    private Integer totalRooms = 1;

    @Column(nullable = false)
    private Integer availableRooms = 1;

    @Column(nullable = false)
    private Integer totalBeds = 1;

    @Column(nullable = false)
    private Integer availableBeds = 1;

    // GPS coordinates for 5km radius search
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // Cover image URL (first/main image)
    @Column(length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardingStatus status = BoardingStatus.PENDING_APPROVAL;

    @Column(columnDefinition = "TEXT")
    private String adminFeedback;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = BoardingStatus.PENDING_APPROVAL;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}