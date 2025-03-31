package com.tutor.repository;

import com.tutor.entity.Teacher;
import com.tutor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUser(User user);
    
    @Query("SELECT t FROM Teacher t JOIN t.user u WHERE " +
           "(:name IS NULL OR u.realName LIKE %:name%) AND " +
           "(:subject IS NULL OR t.subjects LIKE %:subject%) AND " +
           "(:status IS NULL OR t.status = :status)")
    List<Teacher> findByFilters(String name, String subject, String status);
} 