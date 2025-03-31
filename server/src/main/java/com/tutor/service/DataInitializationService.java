package com.tutor.service;

import com.tutor.entity.Teacher;
import com.tutor.entity.User;
import com.tutor.entity.UserRole;
import com.tutor.repository.TeacherRepository;
import com.tutor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 同步教师数据
        syncTeachers();
    }
    
    /**
     * 同步教师数据，确保每个角色为TEACHER的用户在teachers表中有对应记录
     */
    private void syncTeachers() {
        // 获取所有TEACHER角色的用户
        List<User> teacherUsers = userRepository.findByRole(UserRole.TEACHER);
        
        for (User user : teacherUsers) {
            // 检查教师表中是否已有此用户的记录
            Optional<Teacher> existingTeacher = teacherRepository.findByUser(user);
            
            if (!existingTeacher.isPresent()) {
                // 如果没有，则创建新记录
                Teacher teacher = new Teacher();
                teacher.setUser(user);
                teacher.setExperience(0); // 默认经验为0年
                teacher.setSubjects(""); // 默认无教学科目
                teacher.setStatus("active"); // 默认状态为在职
                
                teacherRepository.save(teacher);
                
                System.out.println("已为用户 " + user.getUsername() + " 创建教师记录");
            }
        }
    }
} 