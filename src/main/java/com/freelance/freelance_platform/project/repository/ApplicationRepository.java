package com.freelance.freelance_platform.project.repository;


import com.freelance.freelance_platform.project.domain.Application;
import com.freelance.freelance_platform.shared.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Page<Application> findByProjectId(UUID projectId, Pageable pageable);

    Page<Application> findByFreelancerId(UUID freelancerId, Pageable pageable);

    Optional<Application> findByProjectIdAndFreelancerId(UUID projectId, UUID freelancerId);

    boolean existsByProjectIdAndFreelancerId(UUID projectId, UUID freelancerId);

    Page<Application> findByFreelancerIdAndStatus(
            UUID freelancerId, ApplicationStatus status, Pageable pageable);
}