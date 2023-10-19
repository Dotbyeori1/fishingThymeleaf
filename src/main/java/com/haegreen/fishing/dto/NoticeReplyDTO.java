package com.haegreen.fishing.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoticeReplyDTO {

    private Long nrno;
    private String text;
    private String replyer;
    private Long nbno;
    private LocalDateTime regDate, modDate;

}
