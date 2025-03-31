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

import java.util.List;
import java.util.Map;
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
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 检查手机号是否已存在
        if (userDTO.getPhone() != null && userRepository.existsByPhone(userDTO.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRealName(userDTO.getRealName());
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
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码不正确");
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
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
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
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查邮箱是否已被其他用户使用
        if (profileDTO.getEmail() != null && !profileDTO.getEmail().equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(profileDTO.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(profileDTO.getEmail());
        }
        
        // 检查手机号是否已被其他用户使用
        if (profileDTO.getPhone() != null && !profileDTO.getPhone().equals(user.getPhone())) {
            Optional<User> existingUser = userRepository.findByPhone(profileDTO.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("该手机号已被使用");
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
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证当前密码是否正确
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("当前密码不正确");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 获取所有用户列表 (管理员功能)
     */
    public List<UserProfileDTO> getAllUsers() {
        List<User> users = userRepository.findByRole(UserRole.USER);
        return users.stream()
                .map(this::convertToUserProfileDTO)
                .toList();
    }
    
    /**
     * 创建新用户 (管理员功能)
     */
    @Transactional
    public UserProfileDTO createUser(Map<String, String> userData) {
        String email = userData.get("email");
        String realName = userData.get("realName");
        String phone = userData.get("phone");
        
        // 基本验证
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        
        if (realName == null || realName.trim().isEmpty()) {
            throw new RuntimeException("真实姓名不能为空");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("该邮箱已被注册");
        }
        
        // 如果提供了手机号，检查是否已存在
        if (phone != null && !phone.trim().isEmpty() && userRepository.existsByPhone(phone)) {
            throw new RuntimeException("该手机号已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(email); // 使用邮箱作为用户名
        user.setPassword(passwordEncoder.encode("123456")); // 初始密码设置为123456并加密
        user.setEmail(email);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole(UserRole.USER); // 只能创建普通用户
        
        User savedUser = userRepository.save(user);
        
        return convertToUserProfileDTO(savedUser);
    }
    
    /**
     * 管理员更新用户信息
     */
    @Transactional
    public UserProfileDTO updateUserByAdmin(Long userId, Map<String, String> userData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("未找到该用户"));
        
        String email = userData.get("email");
        String realName = userData.get("realName");
        String phone = userData.get("phone");
        
        // 检查邮箱是否已被其他用户使用
        if (email != null && !email.equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("该邮箱已被其他用户使用");
            }
            user.setEmail(email);
            user.setUsername(email); // 同时更新用户名为邮箱
        }
        
        // 更新真实姓名
        if (realName != null) {
            user.setRealName(realName);
        }
        
        // 检查手机号是否已被其他用户使用
        if (phone != null && !phone.equals(user.getPhone())) {
            Optional<User> existingUser = userRepository.findByPhone(phone);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("该手机号已被其他用户使用");
            }
            user.setPhone(phone);
        }
        
        User updatedUser = userRepository.save(user);
        
        return convertToUserProfileDTO(updatedUser);
    }
    
    /**
     * 删除用户 (管理员功能)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("未找到该用户"));
        
        // 检查是否为管理员
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("不能删除管理员账户");
        }
        
        // TODO: 根据业务需求，可能需要检查用户是否有关联数据，如订单等
        
        userRepository.delete(user);
    }
    
    /**
     * 转换User实体为UserProfileDTO
     */
    private UserProfileDTO convertToUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().toString());
        dto.setRegisterTime(user.getCreateTime());
        return dto;
    }
} 