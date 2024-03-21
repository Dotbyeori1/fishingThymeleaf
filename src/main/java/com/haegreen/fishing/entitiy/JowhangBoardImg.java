package com.haegreen.fishing.entitiy;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;

@Entity
@Table(name = "JowhangBoardImg")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "jowhangBoard")
public class JowhangBoardImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jino;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE) //delete 옵션을 cascade로 설정하는것! room이 삭제되면 img는 쓰레기가 되디때문에 지워줘야됨
    private JowhangBoard jowhangBoard;

    @Column(length = 200)
    private String uuidfileName;

    @Column(length = 200)
    private String realfileName;

}