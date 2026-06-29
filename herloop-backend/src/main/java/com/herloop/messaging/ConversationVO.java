package com.herloop.messaging;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {

    private Long id;

    /** 对方用户信息 */
    private Long otherUserId;
    private String otherNickname;
    private String otherAvatar;
    private Boolean otherVerified;

    /** 关联商品 */
    private Long productId;
    private String productTitle;

    /** 最后消息 */
    private String lastMessage;
    private LocalDateTime lastMessageAt;

    /** 当前用户未读数 */
    private Long unreadCount;
}
