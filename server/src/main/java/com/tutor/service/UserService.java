package com.tutor.service;

import com.tutor.dto.UserDTO;
import com.tutor.dto.UserProfileDTO;
import com.tutor.entity.Teacher;
import com.tutor.entity.TeacherProfile;
import com.tutor.entity.User;
import com.tutor.entity.UserRole;
import com.tutor.repository.TeacherProfileRepository;
import com.tutor.repository.TeacherRepository;
import com.tutor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeacherProfileRepository teacherProfileRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
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
            // 保存教师详细资料
            TeacherProfile teacherProfile = new TeacherProfile();
            teacherProfile.setUser(savedUser);
            teacherProfile.setEducation(userDTO.getEducation());
            teacherProfile.setMajor(userDTO.getMajor());
            
            // 将科目列表转换为逗号分隔的字符串
            String subjects = "";
            if (userDTO.getSubjects() != null && !userDTO.getSubjects().isEmpty()) {
                subjects = String.join(",", userDTO.getSubjects());
                teacherProfile.setSubjects(subjects);
            }
            
            teacherProfile.setExperience(userDTO.getExperience());
            teacherProfile.setBio(userDTO.getBio());
            
            teacherProfileRepository.save(teacherProfile);
            
            // 同时创建Teacher记录
            Teacher teacher = new Teacher();
            teacher.setUser(savedUser);
            teacher.setEducation(userDTO.getEducation());
            teacher.setMajor(userDTO.getMajor());
            teacher.setExperience(userDTO.getExperience() != null ? userDTO.getExperience() : 0);
            teacher.setSubjects(subjects);
            teacher.setStatus("active");
            
            teacherRepository.save(teacher);
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

    /**
     * 获取用户个人资料
     */
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setId(user.getId());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setRealName(user.getRealName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhone(user.getPhone());
        profileDTO.setRole(user.getRole().toString());
        profileDTO.setRegisterTime(user.getCreateTime());
        
        return profileDTO;
    }
    
    /**
     * 更新用户个人资料
     */
    @Transactional
    public UserProfileDTO updateUserProfile(Long userId, UserProfileDTO profileDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 检查邮箱是否已被其他用户使用
        if (profileDTO.getEmail() != null && !profileDTO.getEmail().equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(profileDTO.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(profileDTO.getEmail());
        }
        
        // 检查手机号是否已被其他用户使用
        if (profileDTO.getPhone() != null && !profileDTO.getPhone().equals(user.getPhone())) {
            Optional<User> existingUser = userRepository.findByPhone(profileDTO.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("Phone number already in use");
            }
            user.setPhone(profileDTO.getPhone());
        }
        
        // 更新真实姓名
        if (profileDTO.getRealName() != null) {
            user.setRealName(profileDTO.getRealName());
        }
        
        User updatedUser = userRepository.save(user);
        
        // 返回更新后的用户资料
        profileDTO.setId(updatedUser.getId());
        profileDTO.setUsername(updatedUser.getUsername());
        profileDTO.setRealName(updatedUser.getRealName());
        profileDTO.setEmail(updatedUser.getEmail());
        profileDTO.setPhone(updatedUser.getPhone());
        profileDTO.setRole(updatedUser.getRole().toString());
        profileDTO.setRegisterTime(updatedUser.getCreateTime());
        
        return profileDTO;
    }
    
    /**
     * 修改用户密码
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 验证当前密码是否正确
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
} 