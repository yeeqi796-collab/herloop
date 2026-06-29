package com.herloop.messaging;

import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 创建/获取会话
     */
    @PostMapping("/conversations")
    public Result<ConversationVO> getOrCreateConversation(@RequestBody @Valid CreateConversationRequest req) {
        Long userId = CurrentUser.getId();
        ConversationVO vo = messageService.getOrCreateConversation(userId, req.getTargetUserId(), req.getProductId());
        return Result.success(vo);
    }

    /**
     * 会话列表
     */
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> listConversations() {
        Long userId = CurrentUser.getId();
        List<ConversationVO> list = messageService.listConversations(userId);
        return Result.success(list);
    }

    /**
     * 消息列表
     */
    @GetMapping("/conversations/{id}/messages")
    public Result<PageResult<MessageVO>> listMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        Long userId = CurrentUser.getId();
        PageResult<MessageVO> result = messageService.listMessages(userId, id, page, pageSize);
        return Result.success(result);
    }

    /**
     * 发送消息
     */
    @PostMapping("/conversations/{id}/messages")
    public Result<MessageVO> sendMessage(@PathVariable Long id, @RequestBody @Valid SendMessageRequest req) {
        Long userId = CurrentUser.getId();
        MessageVO vo = messageService.sendMessage(userId, id, req.getContent());
        return Result.success(vo);
    }

    /**
     * 标记会话已读
     */
    @PutMapping("/conversations/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = CurrentUser.getId();
        messageService.markAsRead(userId, id);
        return Result.success();
    }

    /**
     * 未读消息总数
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        Long userId = CurrentUser.getId();
        long count = messageService.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }

    @Data
    public static class CreateConversationRequest {
        private Long targetUserId;
        private Long productId;
    }

    @Data
    public static class SendMessageRequest {
        private String content;
    }
}
