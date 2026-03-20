package com.nilesh.cym.review.service;

import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.ReviewEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.enums.BookingStatus;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.ReviewRepository;
import com.nilesh.cym.review.dto.ReviewRequestDto;
import com.nilesh.cym.token.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private MechanicRepository mechanicRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_rejectsIncompleteBooking() {
        BookingEntity booking = completedBooking();
        booking.setStatus(BookingStatus.STARTED);

        when(bookingRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(booking));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(1L, new AuthenticatedUser(1L, UserRole.USER, "+919900000001"), new ReviewRequestDto(5, "Good"))
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(reviewRepository, never()).save(any(ReviewEntity.class));
    }

    @Test
    void createReview_persistsReviewForCompletedBooking() {
        BookingEntity booking = completedBooking();
        ReviewEntity saved = new ReviewEntity();
        saved.setId(10L);
        saved.setBooking(booking);
        saved.setMechanic(booking.getMechanic());
        saved.setUser(booking.getUser());
        saved.setRating(5);
        saved.setReview("Great job");

        when(bookingRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking_Id(1L)).thenReturn(false);
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(saved);
        when(mechanicRepository.findById(1L)).thenReturn(Optional.of(booking.getMechanic()));
        when(reviewRepository.findAverageRatingByMechanicId(1L)).thenReturn(5.0);

        var response = reviewService.createReview(1L, new AuthenticatedUser(1L, UserRole.USER, "+919900000001"), new ReviewRequestDto(5, "Great job"));

        assertEquals(5, response.rating());
        verify(mechanicRepository).save(any(MechanicEntity.class));
    }

    private BookingEntity completedBooking() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(UserRole.USER);

        UserEntity mechanicUser = new UserEntity();
        mechanicUser.setId(2L);
        mechanicUser.setRole(UserRole.MECHANIC);

        MechanicEntity mechanic = new MechanicEntity();
        mechanic.setId(1L);
        mechanic.setUser(mechanicUser);

        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setUser(user);
        booking.setMechanic(mechanic);
        booking.setStatus(BookingStatus.COMPLETED);
        return booking;
    }
}
