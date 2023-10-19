package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.ImgDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ImgService {
    public void remove(Long nbno);

    public ImgDTO get(Long nbno);

    public void update(Long ino, MultipartFile file);

    public void noticeBoardRegister(MultipartFile file, Long nbno);
}

