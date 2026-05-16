package com.freelance.freelance_platform.review.service;

import com.freelance.freelance_platform.project.repository.ContractRepository;
import com.freelance.freelance_platform.review.domain.Review;
import com.freelance.freelance_platform.review.dto.CreateReviewRequest;
import com.freelance.freelance_platform.review.dto.ReviewDto;
import com.freelance.freelance_platform.review.mapper.ReviewMapper;
import com.freelance.freelance_platform.review.repository.ReviewRepository;
import com.freelance.freelance_platform.shared.enums.ContractStatus;
import com.freelance.freelance_platform.shared.exception.BusinessException;
import com.freelance.freelance_platform.shared.exception.ErrorCode;
import com.freelance.freelance_platform.shared.response.PageResponse;
import com.freelance.freelance_platform.shared.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContractRepository contractRepository;
    private final ReviewMapper reviewMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReviewDto createReview(CreateReviewRequest request) {
        var reviewerId = SecurityUtils.getCurrentUserId();

        // Récupérer le contrat
        var contract = contractRepository.findById(request.contractId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTRACT_NOT_FOUND));

        // Vérifier que le contrat est terminé
        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // Vérifier que le reviewer fait partie du contrat
        boolean isClient = contract.getClient().getId().equals(reviewerId);
        boolean isFreelancer = contract.getFreelancer().getId().equals(reviewerId);

        if (!isClient && !isFreelancer) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // Vérifier qu'il n'a pas déjà évalué ce contrat
        if (reviewRepository.existsByContractIdAndReviewerId(
                request.contractId(), reviewerId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // Déterminer qui est évalué
        var reviewee = isClient
                ? contract.getFreelancer()
                : contract.getClient();

        var reviewer = isClient
                ? contract.getClient()
                : contract.getFreelancer();

        // Créer la review
        var review = new Review();
        review.setContract(contract);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(request.rating());
        review.setComment(request.comment());

        review = reviewRepository.save(review);
        log.info("Review créée par {} pour {}", reviewerId, reviewee.getId());

        return reviewMapper.toDto(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDto> getReviewsForUser(UUID userId, Pageable pageable) {
        var page = reviewRepository.findByRevieweeId(userId, pageable);
        return PageResponse.from(page.map(reviewMapper::toDto));
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(UUID userId) {
        return reviewRepository.findAverageRatingByUserId(userId);
    }
}