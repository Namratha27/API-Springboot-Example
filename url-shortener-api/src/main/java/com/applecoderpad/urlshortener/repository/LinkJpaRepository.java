package com.applecoderpad.urlshortener.repository;

import com.applecoderpad.urlshortener.model.LinkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkJpaRepository extends JpaRepository<LinkRecord, String> {}
