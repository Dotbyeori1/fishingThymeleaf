package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.ImgDTO;
import com.haegreen.fishing.entitiy.NoticeBoard;
import com.haegreen.fishing.entitiy.NoticeBoardImg;
import com.haegreen.fishing.manager.FileManager;
import com.haegreen.fishing.repository.NoticeBoardImgRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NoticeBoardImgServiceImpl implements NoticeBoardImgService {

    private final NoticeBoardImgRepository noticeBoardImgRepository;
    private final FileManager fileManager;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public void remove(Long nbno) {
        List<NoticeBoardImg> noticeBoardImgs = noticeBoardImgRepository.findByNoticeBoardNbno(nbno);
        noticeBoardImgs.forEach(i -> {
            fileManager.remove(i.getUuidfileName());
            noticeBoardImgRepository.deleteById(i.getNino());
        });
    }

    @Override
    public void deleteImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;

        for (Long id : imageIds) {
            NoticeBoardImg img = noticeBoardImgRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found: " + id));

            // 파일 시스템에서 이미지 삭제
            fileManager.remove(img.getUuidfileName());

            // DB에서 이미지 정보 삭제
            noticeBoardImgRepository.deleteById(id);
        }
    }

    @Override
    public ImgDTO get(Long nbno) {
        NoticeBoardImg noticeBoardImg = noticeBoardImgRepository.findById(nbno).get();
        ImgDTO imgDTO = modelMapper.map(noticeBoardImg, ImgDTO.class);
        return imgDTO;
    }

    @Override
    public void update(Long nino, MultipartFile file) {
        NoticeBoardImg noticeBoardImg = noticeBoardImgRepository.findById(nino).orElseThrow(() -> new IllegalArgumentException("Image not found: " + nino));

        String origin = file.getOriginalFilename(); // 사용자가 업로드한 파일 이름

        if (origin.equals(noticeBoardImg.getRealfileName())) {
            return;
        }
        String uuidFileName = fileManager.UUIDMaker(origin); // 새 UUID 생성

        boolean is_add = fileManager.add(file, uuidFileName); // 새 파일 저장
        if (is_add) {
            String oldFilename = noticeBoardImg.getUuidfileName();
            fileManager.remove(oldFilename); // 기존 파일 삭제

            noticeBoardImg.setUuidfileName(uuidFileName);
            noticeBoardImg.setRealfileName(origin);

            noticeBoardImgRepository.save(noticeBoardImg); // 업데이트된 정보로 DB에 저장
        } else {
            System.out.println("파일 업데이트 실패");
        }
    }

    @Override
    public void updateImages(List<Long> ninoList, List<MultipartFile> images) {
        int imgSize = images.size();
        for (int i = 0; i < ninoList.size(); i++) {
            Long nino = ninoList.get(i);
            if (i < imgSize && !images.get(i).isEmpty()) {  // 이미지가 존재하고 비어 있지 않을 때만 업데이트
                MultipartFile file = images.get(i);
                update(nino, file);
            }
        }
    }

    @Override
    public void noticeBoardRegister(MultipartFile file, Long nbno) {
        if (file == null || file.isEmpty()) {
            return;  // 파일이 제공되지 않았다면, 아무것도 하지 않고 메서드를 종료합니다.
        }
        String origin = file.getOriginalFilename(); //사용자가 업로드한 파일 이름 가져오기 xxxx.png xxxx.jpg
        String uuidFileName = fileManager.UUIDMaker(origin); //UUID 생성
        boolean is_add = fileManager.add(file, uuidFileName); //파일 생성
        if (is_add) //파일 생성 여부에 따라서 Db에 저장한다.
        {
            // db 저장코드
            NoticeBoard noticeBoard = NoticeBoard.builder().nbno(nbno).build();
            NoticeBoardImg noticeBoardImg = NoticeBoardImg
                    .builder().uuidfileName(uuidFileName).realfileName(origin).noticeBoard(noticeBoard).build();
            noticeBoardImgRepository.save(noticeBoardImg);
        }
    }
}
