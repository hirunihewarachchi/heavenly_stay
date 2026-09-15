package com.ijse.heavenlyStay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "boarding_images")
public class BoardingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;
    private String imageUrl;
    private Boolean isCover = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boarding_id", nullable = false)
    private Boarding boarding;
}
