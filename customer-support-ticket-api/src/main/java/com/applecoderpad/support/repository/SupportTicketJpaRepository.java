package com.applecoderpad.support.repository;

import com.applecoderpad.support.model.SupportTicket;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportTicketJpaRepository extends JpaRepository<SupportTicket, UUID> {}
