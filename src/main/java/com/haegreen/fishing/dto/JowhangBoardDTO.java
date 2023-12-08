package com.haegreen.fishing.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JowhangBoardDTO {

    private Long jbno;
    private String title;
    private String content;
    private String writerEmail; //작성자 이메일
    private String writerName;
    private LocalDateTime regTime;
    private LocalDateTime modTime;
    private String createdBy;
    private String modifiedBy;
    private int replyCount; //해당 게시글 댓글 수

    private List<ImgDTO> imgDTOList;// imgDTO list
    public void setImgDTOList(List<ImgDTO> imgDTOList) {
        this.imgDTOList = imgDTOList;
    } // 이미지 리스트화
}
