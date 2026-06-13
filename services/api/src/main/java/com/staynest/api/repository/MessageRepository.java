package com.staynest.api.repository;

import com.staynest.api.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByBookingIdOrderByCreatedAtAsc(UUID bookingId, Pageable pageable);

    List<Message> findByBookingIdAndReadFalseAndSenderIdNot(UUID bookingId, UUID readerId);

    long countByBookingIdAndReadFalseAndSenderIdNot(UUID bookingId, UUID readerId);
}
