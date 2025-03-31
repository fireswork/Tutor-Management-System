package com.tutor.dto;

import lombok.Data;

/**
 * 资质审核数据传输对象
 */
@Data
public class QualificationReviewDTO {
    
    /**
     * 审核状态：APPROVED（通过）或 REJECTED（拒绝）
     */
    private String status;
    
    /**
     * 审核意见或拒绝理由
     */
    private String comment;
} 