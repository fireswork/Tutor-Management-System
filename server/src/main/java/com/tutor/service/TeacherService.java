package com.tutor.service;

import com.tutor.dto.QualificationDTO;
import com.tutor.dto.TeacherDTO;
import com.tutor.dto.TeacherQualificationsDTO;
import com.tutor.entity.Qualification;
import com.tutor.entity.Teacher;
import com.tutor.entity.User;
import com.tutor.repository.QualificationRepository;
import com.tutor.repository.TeacherRepository;
import com.tutor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QualificationRepository qualificationRepository;
    
    @Autowired
    private QualificationService qualificationService;
    
    /**
     * 获取所有教师列表
     */
    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据条件筛选教师列表
     */
    public List<TeacherDTO> getTeachersByFilters(String name, String subject, String status) {
        return teacherRepository.findByFilters(name, subject, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取单个教师信息
     */
    public TeacherDTO getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return convertToDTO(teacher);
    }
    
    /**
     * 添加教师
     */
    @Transactional
    public TeacherDTO addTeacher(TeacherDTO teacherDTO) {
        // 创建用户
        User user = new User();
        user.setUsername(teacherDTO.getEmail()); // 使用邮箱作为用户名
        user.setPassword("password"); // 应该使用随机密码，并通过邮件发送给用户
        user.setRealName(teacherDTO.getName());
        user.setEmail(teacherDTO.getEmail());
        user.setPhone(teacherDTO.getPhone());
        user.setRole(com.tutor.entity.UserRole.TEACHER);
        
        user = userRepository.save(user);
        
        // 创建教师
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setExperience(teacherDTO.getExperience());
        teacher.setSubjects(String.join(",", teacherDTO.getSubjects()));
        teacher.setStatus(teacherDTO.getStatus());
        
        teacher = teacherRepository.save(teacher);
        
        return convertToDTO(teacher);
    }
    
    /**
     * 更新教师信息
     */
    @Transactional
    public TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        User user = teacher.getUser();
        user.setRealName(teacherDTO.getName());
        user.setEmail(teacherDTO.getEmail());
        user.setPhone(teacherDTO.getPhone());
        
        userRepository.save(user);
        
        teacher.setExperience(teacherDTO.getExperience());
        teacher.setSubjects(String.join(",", teacherDTO.getSubjects()));
        teacher.setStatus(teacherDTO.getStatus());
        
        teacher = teacherRepository.save(teacher);
        
        return convertToDTO(teacher);
    }
    
    /**
     * 删除教师
     */
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        // 先删除教师，再删除用户
        teacherRepository.delete(teacher);
        userRepository.delete(teacher.getUser());
    }
    
    /**
     * 获取教师资质信息
     */
    public TeacherQualificationsDTO getTeacherQualifications(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        // 获取该教师的所有资质
        List<Qualification> qualifications = qualificationRepository.findByUser(teacher.getUser());
        
        TeacherQualificationsDTO dto = new TeacherQualificationsDTO();
        dto.setTeacherId(teacher.getId());
        dto.setTeacherName(teacher.getUser().getRealName());
        
        // 设置Teacher实体中的学历和专业字段
        dto.setEducation(teacher.getEducation());
        dto.setMajor(teacher.getMajor());
        
        // 转换为DTO
        List<QualificationDTO> qualificationDTOs = qualifications.stream()
                .map(q -> qualificationService.convertToDTO(q))
                .collect(Collectors.toList());
        
        dto.setQualifications(qualificationDTOs);
        
        // 提取特定类型的资质信息
        extractQualificationInfo(dto, qualifications);
        
        // 如果从资质中没有获取到学历和专业，则优先使用Teacher实体中的字段
        if (dto.getEducation() == null) {
            dto.setEducation(teacher.getEducation());
        }
        if (dto.getMajor() == null) {
            dto.setMajor(teacher.getMajor());
        }
        
        return dto;
    }
    
    /**
     * 根据用户ID获取教师资质信息
     */
    public TeacherQualificationsDTO getTeacherQualificationsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Teacher teacher = teacherRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Teacher not found for this user"));
        
        return getTeacherQualifications(teacher.getId());
    }
    
    /**
     * 从资质列表中提取特定类型的资质信息
     */
    private void extractQualificationInfo(TeacherQualificationsDTO dto, List<Qualification> qualifications) {
        // 提取教师资格证信息
        qualifications.stream()
                .filter(q -> "teaching".equals(q.getType()) && "approved".equals(q.getStatus()))
                .findFirst()
                .ifPresent(q -> dto.setTeachingCertificate(q.getName()));
        
        // 提取教育信息
        qualifications.stream()
                .filter(q -> "education".equals(q.getType()) && "approved".equals(q.getStatus()))
                .findFirst()
                .ifPresent(q -> {
                    dto.setEducation(q.getName());
                    dto.setMajor(q.getDescription());
                });
        
        // 提取其他证书
        List<String> otherCerts = qualifications.stream()
                .filter(q -> !"teaching".equals(q.getType()) && !"education".equals(q.getType()) && "approved".equals(q.getStatus()))
                .map(Qualification::getName)
                .collect(Collectors.toList());
        
        dto.setOtherCertificates(otherCerts);
    }
    
    /**
     * 将Teacher实体转换为DTO
     */
    private TeacherDTO convertToDTO(Teacher teacher) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setUserId(teacher.getUser().getId());
        dto.setName(teacher.getUser().getRealName());
        dto.setEmail(teacher.getUser().getEmail());
        dto.setPhone(teacher.getUser().getPhone());
        dto.setExperience(teacher.getExperience());
        dto.setStatus(teacher.getStatus());
        
        // 转换科目字符串为列表
        if (teacher.getSubjects() != null && !teacher.getSubjects().isEmpty()) {
            dto.setSubjects(Arrays.asList(teacher.getSubjects().split(",")));
        } else {
            dto.setSubjects(new ArrayList<>());
        }
        
        return dto;
    }
} 