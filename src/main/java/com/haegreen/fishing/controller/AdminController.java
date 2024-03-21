package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.Reservation;
import com.haegreen.fishing.entitiy.ReservationDate;
import com.haegreen.fishing.repository.ReservationDateRepository;
import com.haegreen.fishing.repository.ReservationRepository;
import com.haegreen.fishing.service.ReservationDateService;
import com.haegreen.fishing.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("admin")
@Log4j2
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final ReservationService reservationService;
    private final ReservationDateService reservationDateService;
    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository reservationDateRepository;

    public String numberFormat(int money) {
        NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(money);
    }

    private Boolean sendSms(String regName, String to, LocalDate regDate, int mebmber) {
        String api_key = "NCS04BSHXUV9PONO";
        String api_secret = "I60QMWVQ0AZLLANTGFNHSQ64RI9EJERL";
        Message coolsms = new Message(api_key, api_secret);
        HashMap<String, String> params = new HashMap<>();
        String from = "010-4421-2628";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 ");
        String formattedDate = regDate.format(formatter);
        String text = regName + "님 " + formattedDate + mebmber + "명 예약확인이 완료되었습니다.(해그린피싱 거북선호)";
        params.put("to", to);
        params.put("from", from);
        params.put("type", "SMS");
        params.put("text", text);
        params.put("app_version", "test app 1.2");
        try {
            JSONObject obj = coolsms.send(params);
            System.out.println(obj.toString());
            return Boolean.TRUE;
        } catch (CoolsmsException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
            return Boolean.FALSE;
        }
    }

    @GetMapping("reservationList")
    public String list(ReservationDTO reservationDTO, Model model) {
        if (reservationDTO.getRegDate() == null)
            reservationDTO.setRegDate(String.valueOf(LocalDate.now()));
        List<ReservationDTO> reservationDTOS = reservationService.getDateUserReservation(reservationDTO.getRegDate());
        int confirmMember = 0;
        for (ReservationDTO r : reservationDTOS) {
            r.setFormatMoney(numberFormat(r.getMoney()));
            if (r.isState())
                confirmMember += r.getMember();
        }
        model.addAttribute("confirmMember", confirmMember);
        model.addAttribute("reservationDTOS", reservationDTOS);
        model.addAttribute("reservationDTO", reservationDTO);

        ReservationDate reservationDate = reservationDateRepository.findReservationDateByRegDate(reservationDTO.getRegDate());
        boolean available = reservationDateService.isReservable(reservationDate);
        int extras = reservationDate.getExtrasMembers() - confirmMember;

        model.addAttribute("fishingMoney", reservationDate.getFishingMoney());
        model.addAttribute("available", available);
        model.addAttribute("extras", extras);
        return "admin/reservationList";
    }

    @GetMapping("searchList")
    public String getSearchList(PageRequestDTO pageRequestDTO, ReservationDTO reservationDTO, Model model) {
        pageRequestDTO.setSize(15);
        PageResultDTO<ReservationDTO, Object[]> reservation = reservationService.getSearchList(pageRequestDTO, reservationDTO.getRvno(), reservationDTO.getRegName(), reservationDTO
                .getDepositName(), reservationDTO.getTel(), reservationDTO.getRegDate());

        int confirmMember = 0;
        int extras = 0;
        for (ReservationDTO r : reservation.getDtoList()) {
            r.setFormatMoney(numberFormat(r.getMoney()));
            if (r.isState())
                confirmMember += r.getMember();
            extras = r.getExtraMembers() - confirmMember;
        }

        if (reservation.getTotalPage() == 0)
            reservation.setTotalPage(1);

        model.addAttribute("extras", extras);
        model.addAttribute("confirmMember", confirmMember);
        model.addAttribute("result", reservation);
        return "admin/searchList";
    }

    @PostMapping("register")
    public ResponseEntity<Map<String, Object>> register(ReservationDTO dto) {
        String tel = dto.getTel1() + "-" + dto.getTel2() + "-" + dto.getTel3();
        dto.setTel(tel);
        Long rvno = reservationService.register(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", rvno + "번 예약 등록이 성공적으로 완료되었습니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("reservationState")
    public ResponseEntity<?> modifyState(@RequestBody ReservationDTO reservationDTO) {
        ResponseDTO responseDTO = new ResponseDTO();
        Optional<Reservation> reservation = reservationService.modifyState(reservationDTO.getRvno(), reservationDTO);
        if (reservation.isEmpty()) {
            responseDTO.setError("조회에 실패하였습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        if (sendSms(reservation.get().getRegName(), reservation.get().getTel(), reservation.get().getRegDate(), reservation.get().getMember())) {
            responseDTO.setSuccess("예약 확정 및 문자가 발송되었습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
        responseDTO.setError("발송에 실패하였습니다.");
        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("modifyDate")
    public String List(ReservationDateDTO reservationDateDTO, Model model) {
        if (reservationDateDTO.getRegDate() == null)
            reservationDateDTO.setRegDate(LocalDate.now());
        reservationDateDTO = reservationDateService.getReservationDate(reservationDateDTO.getRegDate());
        model.addAttribute("reservationDateDTO", reservationDateDTO);
        return "admin/modifyDate";
    }

    @PostMapping("modifyAvailable")
    public String modifyAvailable(ReservationDateDTO reservationDateDTO, RedirectAttributes redirectAttributes) {
        boolean isModified = reservationDateService.modifyDateAvailable(reservationDateDTO.getRdate(), reservationDateDTO);
        if (isModified)
            return "redirect:/admin/modifyDate";
        redirectAttributes.addFlashAttribute("message", "변경에 실패하였습니다.");
        return "redirect:/admin/modifyDate";
    }

    @GetMapping("reservationInfo")
    public String reservationInfo(@RequestParam("rvno") Long rvno, @RequestParam("search") boolean search, Model model, @ModelAttribute("message") String message) {
        model.addAttribute("message", message);
        ReservationDTO reservationDTO = reservationService.get(rvno);
        reservationDTO.setFormatMoney(numberFormat(reservationDTO.getMoney()));
        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("search", search);
        return "admin/reservationInfo";
    }

    @GetMapping("changeregdate")
    public ResponseEntity<?> changeregDate(@RequestParam("regDate") LocalDate regDate){

        Integer confirmedMembers = reservationRepository.findConfirmedReservationsOnDate(regDate);
        int extras = 16 - (confirmedMembers != null ? confirmedMembers : 0);
        Map<String, Integer> responseMap = new HashMap<>();
        responseMap.put("extras", extras);
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

    @GetMapping("reservationModify")
    public String showReservationModifyForm(@RequestParam("rvno") Long rvno, @RequestParam("search") Boolean search, Model model) {
        ReservationDTO reservationDTO = reservationService.get(rvno);
        ReservationDate reservationDate = reservationDateRepository.findReservationDateByRegDate(reservationDTO.getRegDate());
        Integer confirmedMembers = reservationRepository.findConfirmedReservationsOnDate(reservationDTO.getRegDate());
        int extras = reservationDate.getExtrasMembers() - (confirmedMembers != null ? confirmedMembers : 0);
        extras += reservationDTO.getMember();
        model.addAttribute("extras", extras);
        String[] parts = reservationDTO.getTel().split("-");
        reservationDTO.setTel1(parts[0]);
        reservationDTO.setTel2(parts[1]);
        reservationDTO.setTel3(parts[2]);
        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("search", search);
        return "admin/reservationModify";
    }

    @PostMapping("moneyChange")
    public ResponseEntity<?> moneyChange(ReservationDTO reservationDTO) {
        ResponseDTO responseDTO = new ResponseDTO();
        if (reservationService.modifyMoney(reservationDTO)) {
            responseDTO.setSuccess("입금금액이 수정되었습니다.");
            return new ResponseEntity(responseDTO, HttpStatus.OK);
        }
        responseDTO.setError("수정 실패");
        return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("reservationModify")
    public String reservationModify(ReservationDTO reservationDTO, RedirectAttributes redirectAttributes, @RequestParam("search") boolean search,
                                    @RequestParam String tel1, @RequestParam String tel2, @RequestParam String tel3) {
        String tel = tel1 + "-" + tel2 + "-" + tel3;
        reservationDTO.setTel(tel);
        reservationService.modify(reservationDTO);
        redirectAttributes.addFlashAttribute("message", "수정이 되었습니다.");
        return "redirect:/admin/reservationInfo?search=" + search + "&rvno=" + reservationDTO.getRvno();
    }

    @PostMapping("reservationDelete")
    public ResponseEntity<?> reservationDelete(@RequestParam("rvno") Long rvno) {
        ResponseDTO responseDTO = new ResponseDTO();
        if (reservationService.remove(rvno)) {
            responseDTO.setSuccess("삭제 성공");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
        responseDTO.setError("삭제 실패");
        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("modifySort")
    public String modifySortsGet(Model model, ReservationDateDTO reservationDateDTO){
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(1);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        if(reservationDateDTO.getRegDate()==null){
            reservationDateDTO.setRegDate(LocalDate.now());
        }
        reservationDateDTO = reservationDateService.getReservationDate(reservationDateDTO.getRegDate());
        model.addAttribute("reservationDateDTO", reservationDateDTO);
        return "admin/modifySort";
    }

    @GetMapping("modifySort2") //  선택 날짜 갱신용
    public ResponseEntity<?> modifySortsGet2(@RequestParam("regDate") LocalDate regDate){
        ReservationDateDTO reservationDateDTO = reservationDateService.getReservationDate(regDate);
        return new ResponseEntity<>(reservationDateDTO, HttpStatus.OK);
    }

    @PostMapping("modifySort")
    public ResponseEntity<ResponseDTO> modifySortPost(ReservationDateDTO reservationDateDTO) {

        ResponseDTO responseDTO = new ResponseDTO();

        if (reservationDateDTO.getFishingSort() == null || reservationDateDTO.getFishingSort().trim().isEmpty()) {
            responseDTO.setError("낚시 종류를 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateDTO.getExtrasMembers() == null) {
            responseDTO.setError("낚시 인원을 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateDTO.getFishingMoney() == null) {
            responseDTO.setError("금액을 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateService.modifySort(reservationDateDTO.getRdate(), reservationDateDTO)) {
            responseDTO.setSuccess("적용되었습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            responseDTO.setError("수정 중 문제가 발생했습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("modifySort2")
    public ResponseEntity<ResponseDTO> modifySortsPost(ReservationDateDTO reservationDateDTO,
            @RequestParam(name="startDate")  LocalDate startDate,
            @RequestParam(name="endDate")  LocalDate endDate) {

        ResponseDTO responseDTO = new ResponseDTO();

        if (startDate == null || endDate == null) {
            responseDTO.setError("시작날짜와 종료날짜를 모두 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateDTO.getFishingSort() == null || reservationDateDTO.getFishingSort().trim().isEmpty()) {
            responseDTO.setError("낚시 종류를 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateDTO.getExtrasMembers() == null) {
            responseDTO.setError("낚시 인원을 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateDTO.getFishingMoney() == null) {
            responseDTO.setError("금액을 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateService.modifySorts(startDate, endDate, reservationDateDTO.getFishingSort(),
                reservationDateDTO.getExtrasMembers(), reservationDateDTO.getFishingMoney())) {
            responseDTO.setSuccess("적용되었습니다. 예약현황보기로 이동 하시겠습니까?");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            responseDTO.setError("수정 중 문제가 발생했습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
