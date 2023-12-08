package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.ImgDTO;
import com.haegreen.fishing.dto.JowhangBoardDTO;
import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.JowhangBoardImg;
import com.haegreen.fishing.manager.FileManager;
import com.haegreen.fishing.repository.JowhangBoardImgRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class JowhangBoardImgServiceImpl implements JowhangBoardImgService {

    private final JowhangBoardImgRepository jowhangBoardImgRepository;
    private final FileManager fileManager;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public void remove(Long jbno) {
        List<JowhangBoardImg> jowhangBoardImgs = jowhangBoardImgRepository.findByJowhangBoardJbno(jbno);
        jowhangBoardImgs.forEach(i -> {
            fileManager.remove(i.getUuidfileName());
            jowhangBoardImgRepository.deleteById(i.getJino());
        });
    }

    @Override
    public void deleteImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;

        for (Long id : imageIds) {
            JowhangBoardImg img = jowhangBoardImgRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found: " + id));

            // 파일 시스템에서 이미지 삭제
            fileManager.remove(img.getUuidfileName());

            // DB에서 이미지 정보 삭제
            jowhangBoardImgRepository.deleteById(id);
        }
    }

    @Override
    public ImgDTO get(Long jbno) {
        JowhangBoardImg jowhangBoardImg = jowhangBoardImgRepository.findById(jbno).orElse(null);
        ImgDTO imgDTO = modelMapper.map(jowhangBoardImg, ImgDTO.class);
        return imgDTO;
    }

    @Override
    public void update(Long jino, MultipartFile file) {
        JowhangBoardImg jowhangBoardImg = jowhangBoardImgRepository.findById(jino).orElseThrow(() -> new IllegalArgumentException("Image not found: " + jino));

        String origin = file.getOriginalFilename();

        if (origin.equals(jowhangBoardImg.getRealfileName())) {
            return;
        }
        String uuidFileName = fileManager.UUIDMaker(origin);

        boolean is_add = fileManager.add(file, uuidFileName);
        if (is_add) {
            String oldFilename = jowhangBoardImg.getUuidfileName();
            fileManager.remove(oldFilename);

            jowhangBoardImg.setUuidfileName(uuidFileName);
            jowhangBoardImg.setRealfileName(origin);

            jowhangBoardImgRepository.save(jowhangBoardImg);
        } else {
            System.out.println("파일 업데이트 실패");
        }
    }

    @Override
    public void updateImages(List<Long> jinoList, List<MultipartFile> images) {
        int imgSize = images.size();
        for (int i = 0; i < jinoList.size(); i++) {
            Long jino = jinoList.get(i);
            if (i < imgSize && !images.get(i).isEmpty()) {  // 이미지가 존재하고 비어 있지 않을 때만 업데이트
                MultipartFile file = images.get(i);
                update(jino, file);
            }
        }
    }

    @Override
    public void jowhangBoardRegister(MultipartFile file, Long jbno) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String origin = file.getOriginalFilename();
        String uuidFileName = fileManager.UUIDMaker(origin);
        boolean is_add = fileManager.add(file, uuidFileName);
        if (is_add)
        {
            JowhangBoard jowhangBoard = JowhangBoard.builder().jbno(jbno).build();
            JowhangBoardImg jowhangBoardImg = JowhangBoardImg
                    .builder().uuidfileName(uuidFileName).realfileName(origin).jowhangBoard(jowhangBoard).build();
            jowhangBoardImgRepository.save(jowhangBoardImg);
        }
    }
}
