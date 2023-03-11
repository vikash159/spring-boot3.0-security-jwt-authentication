package com.chat.api;

import com.chat.exception.AppException;
import com.chat.model.*;
import com.chat.payload.*;
import com.chat.repository.ConversationRepository;
import com.chat.repository.GroupRepository;
import com.chat.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ConversationApi {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    private final GroupRepository groupRepository;

    public ConversationApi(ConversationRepository conversationRepository,
                           MessageRepository messageRepository,
                           GroupRepository groupRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
    }

    @GetMapping("/api/v1/conversations")
    public ResponseEntity<?> findAll(@AuthenticationPrincipal User currentUser,
                                     Pageable pageable) {
        Page<Conversation> page = conversationRepository.findAllByOwnerId(currentUser.getId(), pageable);
        List<ConversationDto> list = page.getContent().stream().map(Converter::convertToList).collect(Collectors.toList());
        return ResponseEntity.ok(ConversationListResult.builder().success(true).body(list).build());
    }

    @GetMapping("/api/v1/conversations/users/{userId}/messages")
    public ResponseEntity<?> findAllMessagesByUserId(@AuthenticationPrincipal User currentUser,
                                                     @PathVariable("userId") Long userId,
                                                     Pageable pageable) {
        Conversation conversation = conversationRepository.findByUserIdAndOwnerIdAndType(userId, currentUser.getId(), ConversationType.USER)
                .orElseThrow(() -> new AppException("please provide valid id"));
        Group group = conversation.getGroup();
        Page<Message> page = messageRepository.findAllByGroupId(group.getId(), pageable);
        List<MessageDto> list = page.getContent().stream().map(Converter::convertToList).collect(Collectors.toList());
        return ResponseEntity.ok(MessageResult.builder().success(true).body(list).build());
    }

    @GetMapping("/api/v1/conversations/groups/{groupId}/messages")
    public ResponseEntity<?> findAllMessagesByGroupId(@AuthenticationPrincipal User currentUser,
                                                      @PathVariable("groupId") Long groupId,
                                                      Pageable pageable) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException("not a valid group id"));
        Page<Message> page = messageRepository.findAllByGroupId(group.getId(), pageable);
        List<MessageDto> list = page.getContent().stream().map(Converter::convertToList).collect(Collectors.toList());
        return ResponseEntity.ok(MessageResult.builder().success(true).body(list).build());
    }
}
