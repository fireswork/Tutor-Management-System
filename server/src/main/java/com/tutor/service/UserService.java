package com.tutor.service;

import com.tutor.dto.UserDTO;
import com.tutor.entity.TeacherProfile;
import com.tutor.entity.User;
import com.tutor.entity.UserRole;
import com.tutor.repository.TeacherProfileRepository;
import com.tutor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeacherProfileRepository teacherProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User register(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // 检查邮箱是否已存在
        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // 检查手机号是否已存在
        if (userDTO.getPhone() != null && userRepository.existsByPhone(userDTO.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRole(UserRole.USER); // 默认注册为普通用户
        
        // 如果提供了教师信息，则设置为教师角色
        boolean isTeacher = userDTO.getEducation() != null && !userDTO.getEducation().isEmpty();
        if (isTeacher) {
            user.setRole(UserRole.TEACHER);
        }

        User savedUser = userRepository.save(user);
        
        // 如果是教师，保存教师详细信息
        if (isTeacher) {
            TeacherProfile teacherProfile = new TeacherProfile();
            teacherProfile.setUser(savedUser);
            teacherProfile.setEducation(userDTO.getEducation());
            
            // 将科目列表转换为逗号分隔的字符串
            if (userDTO.getSubjects() != null && !userDTO.getSubjects().isEmpty()) {
                String subjects = String.join(",", userDTO.getSubjects());
                teacherProfile.setSubjects(subjects);
            }
            
            teacherProfile.setExperience(userDTO.getExperience());
            teacherProfile.setBio(userDTO.getBio());
            
            teacherProfileRepository.save(teacherProfile);
        }

        return savedUser;
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    @Transactional
    public void createAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
        }
    }
} 