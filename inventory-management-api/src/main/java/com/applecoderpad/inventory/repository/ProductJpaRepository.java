package com.applecoderpad.inventory.repository;

import com.applecoderpad.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, String> {}
