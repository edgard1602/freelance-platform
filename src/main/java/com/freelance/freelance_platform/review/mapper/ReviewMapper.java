package com.freelance.freelance_platform.review.mapper;

import com.freelance.freelance_platform.review.domain.Review;
import com.freelance.freelance_platform.review.dto.ReviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "reviewerName", expression = "java(review.getReviewer().getFullName())")
    @Mapping(target = "revieweeId", source = "reviewee.id")
    @Mapping(target = "revieweeName", expression = "java(review.getReviewee().getFullName())")
    ReviewDto toDto(Review review);
}