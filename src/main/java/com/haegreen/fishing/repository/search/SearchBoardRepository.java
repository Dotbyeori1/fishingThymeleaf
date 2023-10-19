package com.haegreen.fishing.repository.search;

import java.time.LocalDate;

import com.haegreen.fishing.entitiy.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchBoardRepository {
    Page<Object[]> searchPage(Long paramLong, String paramString1, String paramString2, String paramString3, LocalDate paramLocalDate, Pageable paramPageable);

    Page<Object[]> searchPageByWriter(String[] type, String keyword, Pageable pageable);
}
