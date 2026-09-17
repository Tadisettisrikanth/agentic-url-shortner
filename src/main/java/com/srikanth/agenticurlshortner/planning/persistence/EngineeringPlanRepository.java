package com.srikanth.agenticurlshortner.planning.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineeringPlanRepository extends JpaRepository<EngineeringPlanEntity, UUID> {
    Optional<EngineeringPlanEntity> findByRevisionId(UUID revisionId);
}

