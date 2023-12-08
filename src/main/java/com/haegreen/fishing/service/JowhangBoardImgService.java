package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.ImgDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface JowhangBoardImgService {
    public void remove(Long nbno);

    public ImgDTO get(Long nbno);

    public void update(Long ino, MultipartFile file);

    public void jowhangBoardRegister(MultipartFile file, Long jbno);

    public void deleteImages(List<Long> imageIds);
    public void updateImages(List<Long> ninoList, List<MultipartFile> images);
}

