package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.enumeration.GenderPreference;
import com.ijse.heavenlyStay.service.BoardingService;
import com.ijse.heavenlyStay.service.impl.FileStorageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/boardings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BoardingController {

    private final BoardingService boardingService;
    private final FileStorageServiceImpl fileStorageService;

    @PostMapping
    public CommonResponse createBoarding(
            @RequestBody BoardingDTO dto,
            @RequestParam(required = false, defaultValue = "1") Long ownerId) {
        Long ownerToUse = dto.getOwnerId() != null ? dto.getOwnerId() : ownerId;
        BoardingDTO created = boardingService.createBoarding(dto, ownerToUse);
        return new CommonResponse(200, created, "Boarding ad submitted for approval");
    }

    @PostMapping("/upload-image")
    public CommonResponse uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new CommonResponse(400, "File cannot be empty");
        }
        String imageUrl = fileStorageService.storeFile(file);
        Map<String, String> data = new HashMap<>();
        data.put("imageUrl", imageUrl);
        return new CommonResponse(200, data, "Image uploaded successfully");
    }

    @PostMapping("/upload-images")
    public CommonResponse uploadImages(@RequestParam("files") MultipartFile[] files) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = fileStorageService.storeFile(file);
                imageUrls.add(url);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("imageUrls", imageUrls);
        return new CommonResponse(200, data, "Images uploaded successfully");
    }

    @GetMapping
    public CommonResponse getAllApprovedBoardings() {
        List<BoardingDTO> list = boardingService.getApprovedBoardings();
        return new CommonResponse(200, list, "Fetched approved boardings");
    }

    @GetMapping("/{id}")
    public CommonResponse getBoardingById(@PathVariable Long id) {
        BoardingDTO dto = boardingService.getBoardingById(id);
        return new CommonResponse(200, dto, "Boarding details fetched");
    }

    @GetMapping("/owner/{ownerId}")
    public CommonResponse getBoardingsByOwner(@PathVariable Long ownerId) {
        List<BoardingDTO> list = boardingService.getBoardingsByOwner(ownerId);
        return new CommonResponse(200, list, "Owner boardings fetched");
    }

    @GetMapping("/search")
    public CommonResponse searchBoardings(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) GenderPreference gender,
            @RequestParam(required = false) BigDecimal maxRent,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "5.0") Double radius) {
        List<BoardingDTO> list = boardingService.searchBoardings(district, gender, maxRent, lat, lng, radius);
        return new CommonResponse(200, list, "Search results fetched");
    }

    @GetMapping("/nearby")
    public CommonResponse findNearbyBoardings(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "5.0") Double radius) {
        List<BoardingDTO> list = boardingService.findNearbyBoardings(lat, lng, radius);
        return new CommonResponse(200, list, "Nearby boardings within " + radius + " km fetched");
    }

    @PutMapping("/{id}")
    public CommonResponse updateBoarding(@PathVariable Long id, @RequestBody BoardingDTO dto) {
        BoardingDTO updated = boardingService.updateBoarding(id, dto);
        return new CommonResponse(200, updated, "Boarding updated successfully");
    }

    @DeleteMapping("/{id}")
    public CommonResponse deleteBoarding(@PathVariable Long id) {
        boardingService.deleteBoarding(id);
        return new CommonResponse(200, "Boarding deleted successfully");
    }
}
