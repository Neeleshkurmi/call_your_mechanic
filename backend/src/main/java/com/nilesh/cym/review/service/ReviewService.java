package com.nilesh.cym.review.service;

import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.ReviewEntity;
import com.nilesh.cym.entity.enums.BookingStatus;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.ReviewRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.review.dto.MechanicReviewsResponseDto;
import com.nilesh.cym.review.dto.ReviewRequestDto;
import com.nilesh.cym.review.dto.ReviewResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final MechanicRepository mechanicRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            MechanicRepository mechanicRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.mechanicRepository = mechanicRepository;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "mechanics:reviews", key = "#result.mechanicId()", condition = "#result != null"),
            @CacheEvict(cacheNames = "mechanics:detail", key = "#result.mechanicId()", condition = "#result != null")
    })
    @Transactional
    public ReviewResponseDto createReview(Long bookingId, AuthenticatedUser authenticatedUser, ReviewRequestDto request) {
        Long userId = requireUser(authenticatedUser);
        BookingEntity booking = bookingRepository.findByIdAndUser_Id(bookingId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed bookings can be reviewed");
        }
        if (reviewRepository.existsByBooking_Id(bookingId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking review already exists");
        }

        ReviewEntity review = new ReviewEntity();
        review.setBooking(booking);
        review.setMechanic(booking.getMechanic());
        review.setUser(booking.getUser());
        review.setRating(request.rating());
        review.setReview(request.review() == null ? null : request.review().trim());

        ReviewEntity saved = reviewRepository.save(review);
        refreshMechanicRating(booking.getMechanic().getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getBookingReview(Long bookingId, AuthenticatedUser authenticatedUser) {
        Long userId = requireUser(authenticatedUser);
        BookingEntity booking = bookingRepository.findByIdAndUser_Id(bookingId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        ReviewEntity review = reviewRepository.findByBooking_Id(booking.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        return toResponse(review);
    }

    @Cacheable(cacheNames = "mechanics:reviews", key = "#mechanicId")
    @Transactional(readOnly = true)
    public MechanicReviewsResponseDto getMechanicReviews(Long mechanicId) {
        mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found"));
        List<ReviewResponseDto> reviews = reviewRepository.findByMechanic_IdOrderByCreatedAtDesc(mechanicId).stream()
                .map(this::toResponse)
                .toList();
        Double average = reviewRepository.findAverageRatingByMechanicId(mechanicId);
        BigDecimal averageRating = BigDecimal.valueOf(average == null ? 0D : average).setScale(2, RoundingMode.HALF_UP);
        return new MechanicReviewsResponseDto(mechanicId, averageRating, reviews.size(), reviews);
    }

    private void refreshMechanicRating(Long mechanicId) {
        MechanicEntity mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found"));
        Double average = reviewRepository.findAverageRatingByMechanicId(mechanicId);
        mechanic.setRating(BigDecimal.valueOf(average == null ? 0D : average).setScale(2, RoundingMode.HALF_UP));
        mechanicRepository.save(mechanic);
    }

    private Long requireUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.role() != UserRole.USER) {
            log.warn("review_role_rejected principal={}", LogSanitizer.summarizePrincipal(authenticatedUser));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
        return authenticatedUser.userId();
    }

    private ReviewResponseDto toResponse(ReviewEntity review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getBooking().getId(),
                review.getMechanic().getId(),
                review.getUser().getId(),
                review.getRating(),
                review.getReview(),
                review.getCreatedAt()
        );
    }
}
