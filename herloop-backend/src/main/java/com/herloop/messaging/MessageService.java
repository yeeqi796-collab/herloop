package com.herloop.messaging;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.BusinessException;
import com.herloop.common.PageResult;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    /**
     * 获取或创建会话
     */
    @Transactional
    public ConversationVO getOrCreateConversation(Long userId, Long targetUserId, Long productId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(400, "不能和自己聊天");
        }

        // 查找已有会话 (user1 < user2 保证唯一性)
        Long smallId = Math.min(userId, targetUserId);
        Long bigId = Math.max(userId, targetUserId);

        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUser1Id, smallId)
                .eq(Conversation::getUser2Id, bigId);
        if (productId != null) {
            wrapper.eq(Conversation::getProductId, productId);
        } else {
            wrapper.isNull(Conversation::getProductId);
        }

        Conversation conv = conversationMapper.selectOne(wrapper);

        if (conv == null) {
            conv = new Conversation();
            conv.setUser1Id(smallId);
            conv.setUser2Id(bigId);
            conv.setProductId(productId);
            conv.setLastMessage(null);
            conv.setLastMessageAt(null);
            conversationMapper.insert(conv);
        }

        return toConversationVO(conv, userId);
    }

    /**
     * 获取用户会话列表
     */
    public List<ConversationVO> listConversations(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUser1Id, userId)
                .or()
                .eq(Conversation::getUser2Id, userId);
        wrapper.orderByDesc(Conversation::getLastMessageAt);

        List<Conversation> convs = conversationMapper.selectList(wrapper);
        return convs.stream()
                .map(c -> toConversationVO(c, userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取会话消息列表
     */
    public PageResult<MessageVO> listMessages(Long userId, Long conversationId, int page, int pageSize) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || !isParticipant(conv, userId)) {
            throw new BusinessException(403, "无权访问此会话");
        }

        Page<Message> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .orderByDesc(Message::getCreatedAt);

        messageMapper.selectPage(p, wrapper);

        List<MessageVO> list = p.getRecords().stream()
                .map(this::toMessageVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, p.getTotal(), page, pageSize);
    }

    /**
     * 发送消息
     */
    @Transactional
    public MessageVO sendMessage(Long userId, Long conversationId, String content) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || !isParticipant(conv, userId)) {
            throw new BusinessException(403, "无权访问此会话");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException(400, "消息内容不能为空");
        }

        // 插入消息
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(userId);
        msg.setContent(content);
        msg.setIsRead(0);
        messageMapper.insert(msg);

        // 更新会话最后消息
        conv.setLastMessage(content.length() > 100 ? content.substring(0, 100) + "..." : content);
        conv.setLastMessageAt(LocalDateTime.now());
        conversationMapper.updateById(conv);

        return toMessageVO(msg);
    }

    /**
     * 标记会话消息为已读
     */
    @Transactional
    public void markAsRead(Long userId, Long conversationId) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || !isParticipant(conv, userId)) {
            throw new BusinessException(403, "无权访问此会话");
        }

        Message update = new Message();
        update.setIsRead(1);
        messageMapper.update(update,
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .eq(Message::getIsRead, 0)
                        .ne(Message::getSenderId, userId));
    }

    /**
     * 获取未读消息总数
     */
    public long getUnreadCount(Long userId) {
        // 获取用户所有会话 ID
        List<Conversation> convs = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUser1Id, userId)
                        .or()
                        .eq(Conversation::getUser2Id, userId));
        if (convs.isEmpty()) return 0;

        List<Long> convIds = convs.stream().map(Conversation::getId).collect(Collectors.toList());

        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .in(Message::getConversationId, convIds)
                        .eq(Message::getIsRead, 0)
                        .ne(Message::getSenderId, userId));
    }

    private boolean isParticipant(Conversation conv, Long userId) {
        return userId.equals(conv.getUser1Id()) || userId.equals(conv.getUser2Id());
    }

    private ConversationVO toConversationVO(Conversation conv, Long currentUserId) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());

        // 确定对方用户
        Long otherUserId = currentUserId.equals(conv.getUser1Id()) ? conv.getUser2Id() : conv.getUser1Id();
        User other = userMapper.selectById(otherUserId);
        vo.setOtherUserId(otherUserId);
        vo.setOtherNickname(other != null ? other.getNickname() : "未知用户");
        vo.setOtherAvatar(other != null ? other.getAvatar() : null);
        vo.setOtherVerified(other != null && Boolean.TRUE.equals(other.getVerified()));

        // 关联商品
        vo.setProductId(conv.getProductId());
        if (conv.getProductId() != null) {
            Product product = productMapper.selectById(conv.getProductId());
            vo.setProductTitle(product != null ? product.getTitle() : null);
        }

        vo.setLastMessage(conv.getLastMessage());
        vo.setLastMessageAt(conv.getLastMessageAt());

        // 未读数
        long unread = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conv.getId())
                        .eq(Message::getIsRead, 0)
                        .ne(Message::getSenderId, currentUserId));
        vo.setUnreadCount(unread);

        return vo;
    }

    private MessageVO toMessageVO(Message msg) {
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setConversationId(msg.getConversationId());
        vo.setSenderId(msg.getSenderId());
        vo.setContent(msg.getContent());
        vo.setIsRead(msg.getIsRead() == 1);
        vo.setCreatedAt(msg.getCreatedAt());

        // 发送者信息
        User sender = userMapper.selectById(msg.getSenderId());
        if (sender != null) {
            vo.setSenderNickname(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        return vo;
    }
}
