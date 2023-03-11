package com.chat.payload;

import com.chat.model.Conversation;
import com.chat.model.Message;
import com.chat.model.User;

import java.util.stream.Collectors;

public class Converter {
    public static UserDto convertTo(User user) {
        return UserDto
                .builder()
                .id(user.getId())
                .name(user.getProfile().getName())
                .username(user.getUsername())
                .role(user.getRoles().stream().map(x -> x.getName().name()).collect(Collectors.joining(",")))
                .build();
    }

    public static ConversationDto convertToList(Conversation conversation) {
        return ConversationDto.builder().id(conversation.getId())
                .user(convertTo(conversation.getUser()))
                .group(conversation.getGroup())
                .type(conversation.getType().name())
                .message(conversation.getMessage())
                .messageAt(conversation.getMessageAt())
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    public static MessageDto convertToList(Message message) {
        MessageDto messageDto = new MessageDto();
        messageDto.setId(message.getId());
        messageDto.setContent(message.getContent());
        messageDto.setType(message.getType().name());
        messageDto.setStatus(message.getStatus());
        messageDto.setUser(Converter.convertTo(message.getUser()));
        messageDto.setCreatedAt(message.getCreatedAt());
        return messageDto;
    }
}
