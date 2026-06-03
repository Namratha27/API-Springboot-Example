package com.applecoderpad.orders.repository;

import com.applecoderpad.orders.model.CustomerOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderJpaRepository extends JpaRepository<CustomerOrder, UUID> {}
