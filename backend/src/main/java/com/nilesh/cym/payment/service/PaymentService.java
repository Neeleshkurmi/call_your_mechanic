package com.nilesh.cym.payment.service;

import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.PaymentEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.payment.dto.PaymentCreateRequestDto;
import com.nilesh.cym.payment.dto.PaymentResponseDto;
import com.nilesh.cym.payment.dto.PaymentVerifyRequestDto;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.PaymentRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public PaymentResponseDto createPayment(AuthenticatedUser authenticatedUser, PaymentCreateRequestDto request) {
        Long userId = requireUser(authenticatedUser);
        BookingEntity booking = bookingRepository.findByIdAndUser_Id(request.bookingId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        PaymentEntity payment = new PaymentEntity();
        payment.setBooking(booking);
        payment.setReference("PAY-" + booking.getId() + "-" + Instant.now().toEpochMilli());
        payment.setAmount(499D);
        payment.setStatus("PENDING");
        return toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponseDto verifyPayment(AuthenticatedUser authenticatedUser, PaymentVerifyRequestDto request) {
        Long userId = requireUser(authenticatedUser);
        bookingRepository.findByIdAndUser_Id(request.bookingId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        PaymentEntity payment = paymentRepository.findByReference(request.reference())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        if (!payment.getBooking().getId().equals(request.bookingId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment reference does not match booking");
        }
        payment.setStatus(request.status().trim().toUpperCase());
        return toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentForBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }
        BookingEntity booking = switch (authenticatedUser.role()) {
            case USER -> bookingRepository.findByIdAndUser_Id(bookingId, authenticatedUser.userId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
            case MECHANIC -> {
                Long mechanicId = bookingRepository.findWithParticipantsById(bookingId)
                        .filter(b -> b.getMechanic().getUser().getId().equals(authenticatedUser.userId()))
                        .map(b -> b.getMechanic().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
                yield bookingRepository.findByIdAndMechanic_Id(bookingId, mechanicId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
            }
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        };

        PaymentEntity payment = paymentRepository.findTopByBooking_IdOrderByCreatedAtDesc(booking.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        return toResponse(payment);
    }

    private Long requireUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.role() != UserRole.USER) {
            log.warn("payment_role_rejected principal={}", LogSanitizer.summarizePrincipal(authenticatedUser));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
        return authenticatedUser.userId();
    }

    private PaymentResponseDto toResponse(PaymentEntity payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getReference(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
