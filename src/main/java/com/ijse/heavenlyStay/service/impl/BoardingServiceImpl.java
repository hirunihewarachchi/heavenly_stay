package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.entity.*;
import com.ijse.heavenlyStay.enumeration.BoardingStatus;
import com.ijse.heavenlyStay.enumeration.GenderPreference;
import com.ijse.heavenlyStay.enumeration.RoomCategory;
import com.ijse.heavenlyStay.exception.CustomerException;
import com.ijse.heavenlyStay.repository.*;
import com.ijse.heavenlyStay.service.BoardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardingServiceImpl implements BoardingService {

    private final BoardingRepository boardingRepository;
    private final UserRepository userRepository;
    private final BoardingImageRepository boardingImageRepository;
    private final FacilityRepository facilityRepository;

    @Override
    @Transactional
    public BoardingDTO createBoarding(BoardingDTO dto, Long ownerId) {
        log.info("Executing createBoarding() for ownerId: {}, dto: {}", ownerId, dto);
        try {
            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new CustomerException(404, "Owner not found"));

            Boarding b = new Boarding();
            b.setName(dto.getName());
            b.setDescription(dto.getDescription());
            b.setAddress(dto.getAddress());
            b.setDistrict(dto.getDistrict());
            b.setProvince(dto.getProvince() != null ? dto.getProvince() : getProvinceByDistrict(dto.getDistrict()));
            b.setMonthlyRent(dto.getMonthlyRent());
            b.setKeyMoney(dto.getKeyMoney() != null ? dto.getKeyMoney() : BigDecimal.ZERO);
            b.setGenderPreference(dto.getGenderPreference() != null ? dto.getGenderPreference() : GenderPreference.ANY);
            RoomCategory category = dto.getRoomCategory() != null ? dto.getRoomCategory() : RoomCategory.SINGLE;
            int bedsPerRoom = (dto.getBedsPerRoom() != null && dto.getBedsPerRoom() > 0) ? dto.getBedsPerRoom() : (category == RoomCategory.SHARED ? 2 : 1);
            int totalRooms = dto.getTotalRooms() != null ? dto.getTotalRooms() : 1;
            int availableRooms = dto.getAvailableRooms() != null ? dto.getAvailableRooms() : totalRooms;
            int totalBeds = dto.getTotalBeds() != null ? dto.getTotalBeds() : (totalRooms * bedsPerRoom);
            int availableBeds = dto.getAvailableBeds() != null ? dto.getAvailableBeds() : (availableRooms * bedsPerRoom);

            b.setRoomCategory(category);
            b.setBedsPerRoom(bedsPerRoom);
            b.setTotalRooms(totalRooms);
            b.setAvailableRooms(availableRooms);
            b.setTotalBeds(totalBeds);
            b.setAvailableBeds(availableBeds);
            b.setLatitude(dto.getLatitude());
            b.setLongitude(dto.getLongitude());
            b.setCoverImageUrl(dto.getCoverImageUrl());
            b.setStatus(BoardingStatus.PENDING_APPROVAL); // Requires admin approval & fee payment
            b.setOwner(owner);

            Boarding saved = boardingRepository.save(b);

            // Save Facilities
            if (dto.getFacilities() != null) {
                for (String facName : dto.getFacilities()) {
                    Facility f = new Facility();
                    f.setFacilityName(facName);
                    f.setBoarding(saved);
                    facilityRepository.save(f);
                }
            }

            // Save Image URLs
            if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
                boolean first = true;
                for (String url : dto.getImageUrls()) {
                    BoardingImage img = new BoardingImage();
                    img.setImageUrl(url);
                    img.setIsCover(first);
                    img.setBoarding(saved);
                    boardingImageRepository.save(img);

                    if (first && (saved.getCoverImageUrl() == null || saved.getCoverImageUrl().isEmpty())) {
                        saved.setCoverImageUrl(url);
                        boardingRepository.save(saved);
                    }
                    first = false;
                }
            }

            return mapToDTO(saved);
        } catch (Exception e) {
            log.error("Error in createBoarding() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public BoardingDTO updateBoarding(Long boardingId, BoardingDTO dto) {
        log.info("Executing updateBoarding() for boardingId: {}, dto: {}", boardingId, dto);
        try {
            Boarding b = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding not found"));

            if (dto.getName() != null) b.setName(dto.getName());
            if (dto.getDescription() != null) b.setDescription(dto.getDescription());
            if (dto.getAddress() != null) b.setAddress(dto.getAddress());
            if (dto.getDistrict() != null) b.setDistrict(dto.getDistrict());
            if (dto.getMonthlyRent() != null) b.setMonthlyRent(dto.getMonthlyRent());
            if (dto.getKeyMoney() != null) b.setKeyMoney(dto.getKeyMoney());
            if (dto.getGenderPreference() != null) b.setGenderPreference(dto.getGenderPreference());
            if (dto.getRoomCategory() != null) b.setRoomCategory(dto.getRoomCategory());
            if (dto.getBedsPerRoom() != null) b.setBedsPerRoom(dto.getBedsPerRoom());
            if (dto.getTotalRooms() != null) b.setTotalRooms(dto.getTotalRooms());
            if (dto.getAvailableRooms() != null) b.setAvailableRooms(dto.getAvailableRooms());
            if (dto.getTotalBeds() != null) b.setTotalBeds(dto.getTotalBeds());
            if (dto.getAvailableBeds() != null) b.setAvailableBeds(dto.getAvailableBeds());

            // Sync beds & rooms capacity if either is updated
            int bedsPerRoom = (b.getBedsPerRoom() != null && b.getBedsPerRoom() > 0) ? b.getBedsPerRoom() : 1;
            if (dto.getAvailableBeds() != null && dto.getAvailableRooms() == null) {
                b.setAvailableRooms((int) Math.ceil((double) dto.getAvailableBeds() / bedsPerRoom));
            } else if (dto.getAvailableRooms() != null && dto.getAvailableBeds() == null) {
                b.setAvailableBeds(dto.getAvailableRooms() * bedsPerRoom);
            }

            if (dto.getLatitude() != null) b.setLatitude(dto.getLatitude());
            if (dto.getLongitude() != null) b.setLongitude(dto.getLongitude());

            // Update Image URLs if provided
            if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
                List<BoardingImage> oldImages = boardingImageRepository.findByBoardingId(boardingId);
                boardingImageRepository.deleteAll(oldImages);

                boolean first = true;
                for (String url : dto.getImageUrls()) {
                    BoardingImage img = new BoardingImage();
                    img.setImageUrl(url);
                    img.setIsCover(first);
                    img.setBoarding(b);
                    boardingImageRepository.save(img);

                    if (first) {
                        b.setCoverImageUrl(url);
                    }
                    first = false;
                }
            } else if (dto.getCoverImageUrl() != null) {
                b.setCoverImageUrl(dto.getCoverImageUrl());
            }

            Boarding updated = boardingRepository.save(b);
            return mapToDTO(updated);
        } catch (Exception e) {
            log.error("Error in updateBoarding() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteBoarding(Long boardingId) {
        log.info("Executing deleteBoarding() for boardingId: {}", boardingId);
        try {
            Boarding b = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding not found"));

            List<BoardingImage> images = boardingImageRepository.findByBoardingId(boardingId);
            if (images != null && !images.isEmpty()) {
                boardingImageRepository.deleteAll(images);
            }

            List<Facility> facilities = facilityRepository.findByBoardingId(boardingId);
            if (facilities != null && !facilities.isEmpty()) {
                facilityRepository.deleteAll(facilities);
            }

            boardingRepository.delete(b);
        } catch (Exception e) {
            log.error("Error in deleteBoarding() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<BoardingDTO> getApprovedBoardings() {
        log.info("Executing getApprovedBoardings()");
        try {
            return boardingRepository.findByStatus(BoardingStatus.APPROVED)
                    .stream().map(this::mapToDTO).toList();
        } catch (Exception e) {
            log.error("Error in getApprovedBoardings() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<BoardingDTO> getBoardingsByOwner(Long ownerId) {
        log.info("Executing getBoardingsByOwner() for ownerId: {}", ownerId);
        try {
            return boardingRepository.findByOwnerUserId(ownerId)
                    .stream().map(this::mapToDTO).toList();
        } catch (Exception e) {
            log.error("Error in getBoardingsByOwner() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public BoardingDTO getBoardingById(Long boardingId) {
        log.info("Executing getBoardingById() for boardingId: {}", boardingId);
        try {
            Boarding b = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding ad not found"));
            return mapToDTO(b);
        } catch (Exception e) {
            log.error("Error in getBoardingById() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<BoardingDTO> searchBoardings(String district, GenderPreference gender, BigDecimal maxRent, Double userLat, Double userLng, Double radiusKm) {
        log.info("Executing searchBoardings() for district: {}, gender: {}, maxRent: {}, lat: {}, lng: {}, radius: {}", district, gender, maxRent, userLat, userLng, radiusKm);
        try {
            List<Boarding> boardings = boardingRepository.searchApprovedBoardings(
                    (district != null && !district.trim().isEmpty()) ? district.trim() : null,
                    gender,
                    maxRent
            );

            List<BoardingDTO> result = new ArrayList<>();
            double radius = (radiusKm != null && radiusKm > 0) ? radiusKm : 5.0; // Default 5 km

            for (Boarding b : boardings) {
                BoardingDTO dto = mapToDTO(b);
                if (userLat != null && userLng != null) {
                    if (b.getLatitude() != null && b.getLongitude() != null) {
                        double distance = calculateHaversineDistance(userLat, userLng, b.getLatitude(), b.getLongitude());
                        if (distance <= radius) {
                            dto.setDistanceKm(Math.round(distance * 100.0) / 100.0);
                            result.add(dto);
                        }
                    }
                } else {
                    result.add(dto);
                }
            }

            if (userLat != null && userLng != null) {
                result.sort((a, b) -> Double.compare(
                        a.getDistanceKm() != null ? a.getDistanceKm() : Double.MAX_VALUE,
                        b.getDistanceKm() != null ? b.getDistanceKm() : Double.MAX_VALUE
                ));
            }

            return result;
        } catch (Exception e) {
            log.error("Error in searchBoardings() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<BoardingDTO> findNearbyBoardings(Double userLat, Double userLng, Double radiusKm) {
        log.info("Executing findNearbyBoardings() for lat: {}, lng: {}, radius: {}", userLat, userLng, radiusKm);
        try {
            return searchBoardings(null, null, null, userLat, userLng, radiusKm);
        } catch (Exception e) {
            log.error("Error in findNearbyBoardings() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void addImageToBoarding(Long boardingId, String imageUrl, boolean isCover) {
        log.info("Executing addImageToBoarding() for boardingId: {}, isCover: {}", boardingId, isCover);
        try {
            Boarding b = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding not found"));

            BoardingImage img = new BoardingImage();
            img.setImageUrl(imageUrl);
            img.setIsCover(isCover);
            img.setBoarding(b);
            boardingImageRepository.save(img);

            if (isCover || b.getCoverImageUrl() == null || b.getCoverImageUrl().isEmpty()) {
                b.setCoverImageUrl(imageUrl);
                boardingRepository.save(b);
            }
        } catch (Exception e) {
            log.error("Error in addImageToBoarding() " + e.getMessage());
            throw e;
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    private BoardingDTO mapToDTO(Boarding b) {
        BoardingDTO dto = new BoardingDTO();
        dto.setBoardingId(b.getBoardingId());
        dto.setName(b.getName());
        dto.setDescription(b.getDescription());
        dto.setAddress(b.getAddress());
        dto.setDistrict(b.getDistrict());
        dto.setProvince(b.getProvince());
        dto.setMonthlyRent(b.getMonthlyRent());
        dto.setKeyMoney(b.getKeyMoney());
        dto.setGenderPreference(b.getGenderPreference());
        dto.setRoomCategory(b.getRoomCategory() != null ? b.getRoomCategory() : RoomCategory.SINGLE);
        dto.setBedsPerRoom(b.getBedsPerRoom() != null ? b.getBedsPerRoom() : 1);
        dto.setTotalRooms(b.getTotalRooms() != null ? b.getTotalRooms() : 1);
        dto.setAvailableRooms(b.getAvailableRooms() != null ? b.getAvailableRooms() : 1);
        int bedsPerRoom = dto.getBedsPerRoom();
        dto.setTotalBeds(b.getTotalBeds() != null ? b.getTotalBeds() : (dto.getTotalRooms() * bedsPerRoom));
        dto.setAvailableBeds(b.getAvailableBeds() != null ? b.getAvailableBeds() : (dto.getAvailableRooms() * bedsPerRoom));
        dto.setLatitude(b.getLatitude());
        dto.setLongitude(b.getLongitude());
        dto.setCoverImageUrl(b.getCoverImageUrl());
        dto.setStatus(b.getStatus());
        dto.setAdminFeedback(b.getAdminFeedback());

        if (b.getOwner() != null) {
            dto.setOwnerId(b.getOwner().getUserId());
            dto.setOwnerName(b.getOwner().getUserName());
            dto.setOwnerPhone(b.getOwner().getPhone());
        }

        List<Facility> facs = facilityRepository.findByBoardingId(b.getBoardingId());
        dto.setFacilities(facs.stream().map(Facility::getFacilityName).toList());

        List<BoardingImage> imgs = boardingImageRepository.findByBoardingId(b.getBoardingId());
        dto.setImageUrls(imgs.stream().map(BoardingImage::getImageUrl).toList());

        return dto;
    }

    private String getProvinceByDistrict(String district) {
        if (district == null) return "Western";
        String d = district.toLowerCase();
        if (d.contains("colombo") || d.contains("gampaha") || d.contains("kalutara")) return "Western";
        if (d.contains("kandy") || d.contains("matale") || d.contains("nuwara eliya")) return "Central";
        if (d.contains("galle") || d.contains("matara") || d.contains("hambantota")) return "Southern";
        if (d.contains("jaffna") || d.contains("kilinochchi") || d.contains("mannar") || d.contains("mullaitivu") || d.contains("vavuniya")) return "Northern";
        if (d.contains("batticaloa") || d.contains("ampara") || d.contains("trincomalee")) return "Eastern";
        if (d.contains("kurunegala") || d.contains("puttalam")) return "North Western";
        if (d.contains("anuradhapura") || d.contains("polonnaruwa")) return "North Central";
        if (d.contains("badulla") || d.contains("moneragala")) return "Uva";
        if (d.contains("ratnapura") || d.contains("kegalle")) return "Sabaragamuwa";
        return "Western";
    }
}
