package com.applecoderpad.meetingrooms.repository;

import com.applecoderpad.meetingrooms.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRoomJpaRepository extends JpaRepository<MeetingRoom, String> {}
