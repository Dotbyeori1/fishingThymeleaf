package com.haegreen.fishing.dto;

import com.haegreen.fishing.entitiy.NoticeBoard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImgDTO {
    private Long ino;
    private NoticeBoard noticeBoard;
    private String Imgfile;
}
