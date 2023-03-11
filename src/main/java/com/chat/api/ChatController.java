package com.chat.api;

import com.chat.exception.AppException;
import com.chat.model.*;
import com.chat.payload.Converter;
import com.chat.payload.MessageDto;
import com.chat.payload.MessagePayload;
import com.chat.repository.ConversationRepository;
import com.chat.repository.GroupRepository;
import com.chat.repository.MessageRepository;
import com.chat.repository.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate,
                          ConversationRepository conversationRepository,
                          UserRepository userRepository,
                          MessageRepository messageRepository,
                          GroupRepository groupRepository) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
    }

    @MessageMapping("/chat.user/{userId}")
    public void sendMessageToUser(@Payload MessagePayload messagePayload,
                                  @DestinationVariable("userId") Long userId,
                                  Principal principal) {
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Date date = new Date();
        User receiver = userRepository.findById(userId).orElseThrow(() -> new AppException("user not found.."));

        Conversation receiverConversation = createConversation(receiver, currentUser, messagePayload, date);
        Conversation senderConversation = createConversation(currentUser, receiver, messagePayload, date);

        Group group = senderConversation.getGroup();
        if (group == null) group = receiverConversation.getGroup();
        if (group == null) {
            group = new Group();
            group.setUser(currentUser);
            groupRepository.save(group);
        }
        receiverConversation.setGroup(group);
        senderConversation.setGroup(group);
        conversationRepository.save(senderConversation);
        conversationRepository.save(receiverConversation);

        Message message = new Message();
        message.setContent(messagePayload.getContent());
        message.setType(MessageType.valueOf(messagePayload.getType()));
        message.setStatus(MessageStatus.SENT);
        message.setUser(currentUser);
        message.setGroup(group);
        messageRepository.save(message);

        MessageDto messageDto = new MessageDto();
        messageDto.setUuid(messagePayload.getUuid());
        messageDto.setId(message.getId());
        messageDto.setContent(message.getContent());
        messageDto.setType(message.getType().name());
        messageDto.setConversation(Converter.convertToList(receiverConversation));
        messageDto.setUser(Converter.convertTo(currentUser));
        messageDto.setStatus(message.getStatus());
        messageDto.setCreatedAt(message.getCreatedAt());

        Map<String, Object> headers = new HashMap<>();
        headers.put("messageType", "MESSAGE");
        simpMessagingTemplate.convertAndSend("/chat/" + userId, messageDto, headers);
    }

    @MessageMapping("/chat.group/{groupId}")
    public void sendMessageToGroup(@Payload MessagePayload messagePayload,
                                   @DestinationVariable("groupId") Long groupId,
                                   Principal principal) {
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Date date = new Date();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException("not a valid group id"));

        List<Conversation> conversations = conversationRepository.findAllByGroupId(groupId);

        Message message = new Message();
        message.setContent(messagePayload.getContent());
        message.setType(MessageType.valueOf(messagePayload.getType()));
        message.setStatus(MessageStatus.SENT);
        message.setUser(currentUser);
        message.setGroup(group);
        messageRepository.save(message);


        for (Conversation conversation : conversations) {
            User owner = conversation.getOwner();
            conversation.setMessageAt(date);
            conversation.setMessage(message.getContent());
            conversationRepository.save(conversation);

            MessageDto messageDto = new MessageDto();
            messageDto.setUuid(messagePayload.getUuid());
            messageDto.setId(message.getId());
            messageDto.setContent(message.getContent());
            messageDto.setType(message.getType().name());
            messageDto.setConversation(Converter.convertToList(conversation));
            messageDto.setUser(Converter.convertTo(currentUser));
            messageDto.setStatus(message.getStatus());
            messageDto.setCreatedAt(message.getCreatedAt());

            if (owner.getId().longValue() == currentUser.getId().longValue()) continue;

            Map<String, Object> headers = new HashMap<>();
            headers.put("messageType", "MESSAGE");
            simpMessagingTemplate.convertAndSend("/chat/" + owner.getId(), messageDto, headers);
        }
    }

    @MessageMapping("/chat.user/{userId}/typing")
    public void sendMessageTypingToUser(@DestinationVariable("userId") Long userId,
                                        @Payload Map<String, String> item,
                                        Principal principal) {
        String messageType = item.get("messageType");
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Map<String, Object> headers = new HashMap<>();
        headers.put("messageType", messageType);
        Conversation conversation = conversationRepository.findByUserIdAndOwnerIdAndType(currentUser.getId(), userId, ConversationType.USER)
                .orElseThrow(() -> new AppException("conversation not found"));
        simpMessagingTemplate.convertAndSend("/chat/" + userId, Converter.convertToList(conversation), headers);
    }

    @MessageMapping("/chat.group/{groupId}/typing")
    public void sendMessageTypingToGroup(@DestinationVariable("groupId") Long groupId,
                                         @Payload Map<String, String> item,
                                         Principal principal) {
        String messageType = item.get("messageType");
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Map<String, Object> headers = new HashMap<>();
        headers.put("messageType", messageType);

        List<Conversation> conversations = conversationRepository.findAllByGroupId(groupId);
        for (Conversation conversation : conversations) {
            User user = conversation.getOwner();
            if (user.getId().longValue() == currentUser.getId().longValue()) continue;
            conversation.setUser(currentUser);
            simpMessagingTemplate.convertAndSend("/chat/" + user.getId(), Converter.convertToList(conversation), headers);
        }
    }

    @MessageMapping("/chat.message/{messageId}/status")
    public void updateMessageStatus(@DestinationVariable("messageId") Long messageId,
                                    @Payload Map<String, String> item,
                                    Principal principal) {
        String value = item.get("status");
        String uuid = item.get("uuid");
        MessageStatus status = MessageStatus.valueOf(value);
        Message message = messageRepository.findByIdWithAssociations(messageId);
        Group group = message.getGroup();
        User groupOwner = group.getUser();

        User user = message.getUser();
        message.setStatus(status);
        messageRepository.save(message);
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        Map<String, Object> headers = new HashMap<>();
        headers.put("messageType", "MESSAGE");

        MessageDto messageDto = Converter.convertToList(message);
        messageDto.setUuid(uuid);

        Conversation conversation;
        if (groupOwner.getId().longValue() == user.getId().longValue()) {
            conversation = conversationRepository.findByUserIdAndOwnerIdAndType(currentUser.getId(), user.getId(), ConversationType.USER)
                    .orElseThrow(() -> new AppException("not found"));
        } else {
            conversation = conversationRepository.findByUserIdAndOwnerIdAndType(user.getId(), currentUser.getId(), ConversationType.USER)
                    .orElseThrow(() -> new AppException("not found"));
        }
        messageDto.setConversation(Converter.convertToList(conversation));
        simpMessagingTemplate.convertAndSend("/chat/" + user.getId(), messageDto, headers);
    }

    private Conversation createConversation(User owner, User user, MessagePayload payload, Date date) {
        Conversation conversation = conversationRepository.findByUserIdAndOwnerIdAndType(user.getId(), owner.getId(), ConversationType.USER)
                .orElse(null);
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setOwner(owner);
            conversation.setUser(user);
            conversation.setType(ConversationType.USER);
        }
        conversation.setMessage(payload.getContent());
        conversation.setMessageAt(date);
        return conversation;
    }
}