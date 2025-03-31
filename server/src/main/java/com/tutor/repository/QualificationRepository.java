package com.tutor.repository;

import com.tutor.entity.Qualification;
import com.tutor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QualificationRepository extends JpaRepository<Qualification, Long> {
    List<Qualification> findByUser(User user);
    List<Qualification> findByUserOrderByUploadTimeDesc(User user);
    List<Qualification> findByUserAndStatusOrderByUploadTimeDesc(User user, String status);
    List<Qualification> findByStatus(String status);
    List<Qualification> findByStatusNot(String status);
    
    @Modifying
    @Query("DELETE FROM Qualification q WHERE q.user = :user")
    void deleteByUser(@Param("user") User user);
} 