package com.freelance.freelance_platform.identity.repository;


import com.freelance.freelance_platform.identity.domain.FreelancerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, UUID> {

    Optional<FreelancerProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Query("""
            SELECT fp FROM FreelancerProfile fp
            JOIN FETCH fp.user u
            JOIN FETCH fp.skills s
            WHERE fp.user.id = :userId
            """)
    Optional<FreelancerProfile> findByUserIdWithSkills(UUID userId);
}
