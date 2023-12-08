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
public class JowhangReplyDTO {

    private Long jrno;
    private String text;
    private String replyer;
    private Long jbno;
    private LocalDateTime regDate, modDate;
    private boolean memberState;

}
