package com.haegreen.fishing.entitiy;

import lombok.*;

import jakarta.persistence.*;

@Entity(name = "JowhangBoard")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "member") //FK지정(One쪽의 테이블에서는 외래키 연결시킬 자신의 엔티티의 컬럼명을 exclude 지정)
    public class JowhangBoard extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jbno;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 20000, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY) //지연 로딩 지정
    @JoinColumn(name = "id") // // 조인할 칼럼의 이름
    private Member member; // id

    private int replyCount; // 리플 수

    //grade, photo, bad 기능 추가 필요

    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public void changeTitle(String title){
        this.title = title;
    }

    public void changeContent(String content){
        this.content = content;
    }


}