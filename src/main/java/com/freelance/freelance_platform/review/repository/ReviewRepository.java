package com.freelance.freelance_platform.review.repository;

import com.freelance.freelance_platform.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByRevieweeId(UUID revieweeId, Pageable pageable);

    Page<Review> findByReviewerId(UUID reviewerId, Pageable pageable);

    boolean existsByContractIdAndReviewerId(UUID contractId, UUID reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double findAverageRatingByUserId(UUID userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewee.id = :userId")
    long countByRevieweeId(UUID userId);
}