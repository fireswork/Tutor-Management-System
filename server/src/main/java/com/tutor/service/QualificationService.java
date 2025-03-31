package com.tutor.service;

import com.tutor.dto.QualificationDTO;
import com.tutor.dto.QualificationReviewDTO;
import com.tutor.entity.Qualification;
import com.tutor.entity.User;
import com.tutor.repository.QualificationRepository;
import com.tutor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QualificationService {

    @Autowired
    private QualificationRepository qualificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 获取用户的所有资质证书
     */
    public List<QualificationDTO> getUserQualifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return qualificationRepository.findByUserOrderByUploadTimeDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户指定状态的资质证书
     */
    public List<QualificationDTO> getUserQualificationsByStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return qualificationRepository.findByUserAndStatusOrderByUploadTimeDesc(user, status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 添加资质证书
     */
    @Transactional
    public QualificationDTO addQualification(Long userId, QualificationDTO qualificationDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Qualification qualification = new Qualification();
        qualification.setUser(user);
        qualification.setName(qualificationDTO.getName());
        qualification.setType(qualificationDTO.getType());
        qualification.setIssuer(qualificationDTO.getIssuer());
        qualification.setDescription(qualificationDTO.getDescription());
        qualification.setFileUrl(qualificationDTO.getFileUrl());
        qualification.setStatus("pending"); // 默认状态为待审核
        
        qualification = qualificationRepository.save(qualification);
        
        return convertToDTO(qualification);
    }
    
    /**
     * 删除资质证书
     */
    @Transactional
    public void deleteQualification(Long userId, Long qualificationId) {
        Qualification qualification = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new RuntimeException("资质未找到"));
        
        // 验证资质证书属于指定用户
        if (!qualification.getUser().getId().equals(userId)) {
            throw new RuntimeException("你没有权限删除此资质证书");
        }
        
        // 只有待审核或被拒绝的资质可以删除
        if ("approved".equals(qualification.getStatus())) {
            throw new RuntimeException("无法删除已批准的资质证书");
        }
        
        qualificationRepository.delete(qualification);
    }
    
    /**
     * 获取待审核的资质列表
     */
    public List<QualificationDTO> getPendingQualifications() {
        List<Qualification> pendingQualifications = qualificationRepository.findByStatus("PENDING");
        return pendingQualifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取已审核的资质列表
     */
    public List<QualificationDTO> getReviewedQualifications() {
        List<Qualification> reviewedQualifications = qualificationRepository.findByStatusNot("PENDING");
        return reviewedQualifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 审核资质
     */
    public QualificationDTO reviewQualification(Long id, QualificationReviewDTO reviewDTO) {
        Qualification qualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资质不存在"));
                
        qualification.setStatus(reviewDTO.getStatus());
        qualification.setReviewComment(reviewDTO.getComment());
        qualification.setReviewDate(new Date());
        
        Qualification savedQualification = qualificationRepository.save(qualification);
        return convertToDTO(savedQualification);
    }
    
    /**
     * 批量审核资质
     */
    public Map<Long, Object> batchReviewQualifications(Map<Long, QualificationReviewDTO> reviewsMap) {
        Map<Long, Object> results = new HashMap<>();
        
        for (Map.Entry<Long, QualificationReviewDTO> entry : reviewsMap.entrySet()) {
            Long qualificationId = entry.getKey();
            QualificationReviewDTO reviewDTO = entry.getValue();
            
            try {
                QualificationDTO reviewedQualification = reviewQualification(qualificationId, reviewDTO);
                results.put(qualificationId, reviewedQualification);
            } catch (Exception e) {
                Map<String, String> errorInfo = new HashMap<>();
                errorInfo.put("error", e.getMessage());
                results.put(qualificationId, errorInfo);
            }
        }
        
        return results;
    }
    
    /**
     * 将实体转换为DTO
     */
    public QualificationDTO convertToDTO(Qualification qualification) {
        QualificationDTO dto = new QualificationDTO();
        dto.setId(qualification.getId());
        dto.setUserId(qualification.getUser().getId());
        dto.setName(qualification.getName());
        dto.setType(qualification.getType());
        dto.setIssuer(qualification.getIssuer());
        dto.setDescription(qualification.getDescription());
        dto.setFileUrl(qualification.getFileUrl());
        dto.setStatus(qualification.getStatus());
        dto.setComment(qualification.getComment());
        dto.setUploadTime(qualification.getUploadTime().format(formatter));
        
        return dto;
    }
} 