package com.haegreen.fishing.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeBoardDTO {

    private Long nbno;
    private String title;
    private String content;
    private String writerEmail; //작성자 이메일
    private String writerName;
    private LocalDateTime regTime;
    private LocalDateTime modTime;
    private String createdBy;
    private String modifiedBy;
    private int replyCount; //해당 게시글 댓글 수
}
