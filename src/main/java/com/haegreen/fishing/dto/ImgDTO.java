package com.haegreen.fishing.dto;

import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.NoticeBoard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImgDTO {
    private Long ino; // 공통 이미지 번호
    private NoticeBoard noticeBoard;
    private JowhangBoard jowhangBoard;
    private String uuidfileName;
    private String realfileName;
}
