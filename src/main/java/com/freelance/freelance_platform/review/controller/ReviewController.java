package com.freelance.freelance_platform.review.controller;

import com.freelance.freelance_platform.review.dto.CreateReviewRequest;
import com.freelance.freelance_platform.review.dto.ReviewDto;
import com.freelance.freelance_platform.review.service.ReviewService;
import com.freelance.freelance_platform.shared.response.ApiResponse;
import com.freelance.freelance_platform.shared.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Gestion des évaluations")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Créer une évaluation")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        reviewService.createReview(request),
                        "Évaluation créée avec succès"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtenir les évaluations d'un utilisateur")
    public ResponseEntity<ApiResponse<PageResponse<ReviewDto>>> getReviewsForUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getReviewsForUser(userId, pageable)));
    }

    @GetMapping("/user/{userId}/rating")
    @Operation(summary = "Obtenir la note moyenne d'un utilisateur")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getAverageRating(userId)));
    }
}