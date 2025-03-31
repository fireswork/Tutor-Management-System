package com.tutor.repository;

import com.tutor.entity.TeacherProfile;
import com.tutor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    Optional<TeacherProfile> findByUser(User user);
    
    @Modifying
    @Query("DELETE FROM TeacherProfile tp WHERE tp.user = :user")
    void deleteByUser(@Param("user") User user);
} 