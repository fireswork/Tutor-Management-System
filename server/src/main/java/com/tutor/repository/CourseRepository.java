package com.tutor.repository;

import com.tutor.entity.Course;
import com.tutor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Page<Course> findByTeacher(User teacher, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:keyword IS NULL OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%)")
    Page<Course> findByFilters(@Param("category") String category, 
                              @Param("keyword") String keyword, 
                              Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.teacher = :teacher AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:keyword IS NULL OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%)")
    Page<Course> findByTeacherAndFilters(@Param("teacher") User teacher,
                                        @Param("category") String category, 
                                        @Param("keyword") String keyword, 
                                        Pageable pageable);
} 