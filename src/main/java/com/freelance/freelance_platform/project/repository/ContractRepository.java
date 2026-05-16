package com.freelance.freelance_platform.project.repository;


import com.freelance.freelance_platform.project.domain.Contract;
import com.freelance.freelance_platform.shared.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Page<Contract> findByClientId(UUID clientId, Pageable pageable);

    Page<Contract> findByFreelancerId(UUID freelancerId, Pageable pageable);

    Optional<Contract> findByProjectId(UUID projectId);

    Page<Contract> findByClientIdAndStatus(
            UUID clientId, ContractStatus status, Pageable pageable);

    Page<Contract> findByFreelancerIdAndStatus(
            UUID freelancerId, ContractStatus status, Pageable pageable);
}