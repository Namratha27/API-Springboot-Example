package com.applecoderpad.support.repository;

import com.applecoderpad.support.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentJpaRepository extends JpaRepository<Agent, String> {}
