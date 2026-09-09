package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.LeaseAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaseAgreementRepository extends JpaRepository<LeaseAgreement, Long> {

    @Query("SELECT l FROM LeaseAgreement l WHERE l.tenant.userId = :tenantId")
    List<LeaseAgreement> findByTenantUserId(@Param("tenantId") Long tenantId);

    @Query("SELECT l FROM LeaseAgreement l WHERE l.owner.userId = :ownerId")
    List<LeaseAgreement> findByOwnerUserId(@Param("ownerId") Long ownerId);
}
