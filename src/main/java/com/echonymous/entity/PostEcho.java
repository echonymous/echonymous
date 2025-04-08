package com.echonymous.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PostEcho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postEchoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")   // FK post_id point to the PK of Post
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")   // FK user_id point to the PK of User
    private User user;

    private LocalDateTime echoedAt;
}
