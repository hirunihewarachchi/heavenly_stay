package com.ijse.heavenlyStay.dto;

import com.ijse.heavenlyStay.enumeration.BoardingStatus;
import com.ijse.heavenlyStay.enumeration.GenderPreference;
import com.ijse.heavenlyStay.enumeration.RoomCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardingDTO {
    private Long boardingId;
    private String name;
    private String description;
    private String address;
    private String district;
    private String province;
    private BigDecimal monthlyRent;
    private BigDecimal keyMoney;
    private GenderPreference genderPreference;
    private RoomCategory roomCategory;
    private Integer bedsPerRoom;
    private Integer totalRooms;
    private Integer availableRooms;
    private Integer totalBeds;
    private Integer availableBeds;
    private Double latitude;
    private Double longitude;
    private String coverImageUrl;
    private BoardingStatus status;
    private String adminFeedback;
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
    private List<String> facilities;
    private List<String> imageUrls;
    private Double distanceKm; // Computed dynamically for location search
}
