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

@RequiredArgsConstructor
@Service
public class ImgServiceImpl implements ImgService{

    private final NoticeBoardImgRepository noticeBoardImgRepository;
    private final FileManager fileManager;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public void remove(Long nbno) {
        String filename = noticeBoardImgRepository.findById(nbno).get().getImgfile(); //select 하고
        //fil name 불러오고 file 삭제하고 db 삭제하고 처리!
        fileManager.remove(filename); //파일삭제
        noticeBoardImgRepository.deleteById(nbno); //이런식 //db삭제
    }

    @Override
    public ImgDTO get(Long nbno) {
        NoticeBoardImg noticeBoardImg =  noticeBoardImgRepository.findById(nbno).get();
        ImgDTO imgDTO = modelMapper.map(noticeBoardImg,ImgDTO.class);
        return imgDTO;
    }

    @Override
    public void update(Long nbno,MultipartFile file) {
        String filename = get(nbno).getImgfile();
        fileManager.remove(filename); //파일 삭제
        String uuid = filename.split("_")[0]; //전에 사용됬던 이미지 UUID
        String realfilename = uuid + file.getOriginalFilename();
        fileManager.add(file,realfilename); //파일을 생성

        NoticeBoardImg sec_entity = noticeBoardImgRepository.findById(nbno).get();
        NoticeBoardImg entity = NoticeBoardImg.builder().nino(sec_entity.getNino()).Imgfile(realfilename).noticeBoard(sec_entity.getNoticeBoard()).build();
        noticeBoardImgRepository.save(entity);
    }

    @Override
    public void noticeBoardRegister(MultipartFile file, Long nbno){
        String origin = file.getOriginalFilename(); //사용자가 업로드한 파일 이름 가져오기 xxxx.png xxxx.jpg
        String realname = fileManager.UUIDMaker(origin); //UUID 생성
        boolean is_add =fileManager.add(file,realname); //파일 생성
        if(is_add) //파일 생성 여부에 따라서 Db에 저장한다.
        {
            // db 저장코드
            NoticeBoard noticeBoard = NoticeBoard.builder().nbno(nbno).build();
            NoticeBoardImg noticeBoardImg = NoticeBoardImg
                    .builder().Imgfile(realname).noticeBoard(noticeBoard).build();
            noticeBoardImgRepository.save(noticeBoardImg);
        }
    }


}
