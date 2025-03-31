package com.tutor.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class TeacherQualificationsDTO {
    private Long teacherId;
    private String teacherName;
    private String teachingCertificate; // 教师资格证信息
    private String education; // 学历信息
    private String major; // 专业信息
    private List<String> otherCertificates; // 其他证书
    private List<QualificationDTO> qualifications; // 所有资质证书详情
} 