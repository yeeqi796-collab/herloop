package com.herloop.messaging;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;

    private Long conversationId;

    private Long senderId;
    private String senderNickname;
    private String senderAvatar;

    private String content;

    private Boolean isRead;

    private LocalDateTime createdAt;
}
