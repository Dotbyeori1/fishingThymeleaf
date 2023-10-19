package com.haegreen.fishing.repository.search;

import com.haegreen.fishing.entitiy.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class SearchBoardRepositoryImpl extends QuerydslRepositorySupport implements SearchBoardRepository {

    public SearchBoardRepositoryImpl() {
        super(Reservation.class);
    }

    @Override
    public Page<Object[]> searchPage(Long rvno, String regName, String depositName, String tel, LocalDate regDate, Pageable pageable) {
        QReservation qReservation = QReservation.reservation;
        QReservationDate qReservationDate = QReservationDate.reservationDate;
        JPQLQuery<Tuple> tuple = from(qReservation)
                .join(qReservationDate).on(qReservationDate.rdate.eq(qReservation.reservationDate.rdate))
                .select(qReservation, qReservationDate.extrasMembers, qReservation.count())
                .where(qReservation.rvno.gt(0L).and(qReservation.regDate.goe(regDate == null ? LocalDate.now() : regDate)));

        if (rvno != null || regName != null || regDate != null || depositName != null || tel != null) {
            BooleanBuilder conditionBuilder = new BooleanBuilder();
            if (rvno != null)
                conditionBuilder.and(qReservation.rvno.eq(rvno));
            if (regName != null)
                conditionBuilder.and(qReservation.regName.contains(regName));
            if (regDate != null)
                conditionBuilder.and(qReservation.regDate.eq(regDate));
            if (depositName != null)
                conditionBuilder.and(qReservation.depositName.contains(depositName));
            if (tel != null)
                conditionBuilder.and(qReservation.tel.contains(tel));
            tuple.where(conditionBuilder);
        }

        Sort sort = pageable.getSort();
        sort.stream().forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            tuple.orderBy(new OrderSpecifier(direction, ExpressionUtils.path(Object.class, qReservation, prop)));
        });

        tuple.groupBy(qReservation);
        tuple.offset(pageable.getOffset());
        tuple.limit(pageable.getPageSize());
        List<Tuple> result = tuple.fetch();
        long count = tuple.fetchCount();

        return (Page<Object[]>) new PageImpl((List) result
                .stream().map(t -> t.toArray()).collect(Collectors.toList()), pageable, count);
    }

    @Override
    public Page<Object[]> searchPageByWriter(String[] type, String keyword, Pageable pageable) {
        QNoticeBoard noticeBoard = QNoticeBoard.noticeBoard;
        QNoticeReply reply = QNoticeReply.noticeReply;
        QMember member = QMember.member;

        // 기존 쿼리문에서
        JPQLQuery<Tuple> tuple = from(noticeBoard)
                .leftJoin(member).on(noticeBoard.member.eq(member))
                .leftJoin(reply).on(reply.noticeBoard.eq(noticeBoard))
                .select(noticeBoard, member, reply.count());

        if (type != null) {
            BooleanBuilder conditionBuilder = new BooleanBuilder();

            for (String t : type) {
                switch (t) {
                    case "t":
                        conditionBuilder.or(noticeBoard.title.contains(keyword));
                        break;
                    case "w":
                        conditionBuilder.or(member.nickName.contains(keyword));
                        break;
                    case "c":
                        conditionBuilder.or(noticeBoard.content.contains(keyword));
                        break;
                }
            }
            tuple.where(conditionBuilder);
        }

        // order by
        Sort sort = pageable.getSort();
        sort.stream().forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();

            tuple.orderBy(new OrderSpecifier(direction, ExpressionUtils.path(Object.class, noticeBoard, prop)));
        });

        tuple.groupBy(noticeBoard);

        // page 처리
        tuple.offset(pageable.getOffset());
        tuple.limit(pageable.getPageSize());

        List<Tuple> result = tuple.fetch();
        long count = tuple.fetchCount();

        return new PageImpl<Object[]>(
                result.stream().map(t -> t.toArray()).collect(Collectors.toList()),
                pageable,
                count);
    }
}
