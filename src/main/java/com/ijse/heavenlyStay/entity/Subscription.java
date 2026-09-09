package com.ijse.heavenlyStay.entity;

import com.ijse.heavenlyStay.enumeration.SubcriptionStatus;
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
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal listingFee;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubcriptionStatus status = SubcriptionStatus.ACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boarding_id", nullable = false, unique = true)
    private Boarding boarding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @PrePersist
    public void prePersist() {
        if (this.startDate == null) this.startDate = LocalDateTime.now();
        if (this.expiryDate == null) this.expiryDate = this.startDate.plusMonths(3);
        if (this.status == null) this.status = SubcriptionStatus.ACTIVE;
    }
}
