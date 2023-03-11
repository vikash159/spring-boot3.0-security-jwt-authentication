package com.chat.api;

import com.chat.exception.AppException;
import com.chat.model.Conversation;
import com.chat.model.ConversationType;
import com.chat.model.Group;
import com.chat.model.User;
import com.chat.payload.GroupPayload;
import com.chat.payload.Result;
import com.chat.repository.ConversationRepository;
import com.chat.repository.GroupRepository;
import com.chat.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupApi {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    public GroupApi(GroupRepository groupRepository,
                    UserRepository userRepository,
                    ConversationRepository conversationRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
    }

    @PostMapping("/api/v1/groups")
    public ResponseEntity<?> create(@AuthenticationPrincipal User currentUser,
                                    @Valid @RequestBody GroupPayload payload) {
        Group group = new Group();
        group.setName(payload.getName());
        group.setUser(currentUser);
        groupRepository.save(group);

        List<Long> userIds = payload.getUserIds();
        if (userIds == null || userIds.size() == 0) throw new AppException("please select at least 1 user");

        List<User> users = userRepository.findAllById(userIds);
        users.add(currentUser);
        List<Conversation> conversations = new ArrayList<>();
        for (User user : users) {
            Conversation conversation = new Conversation();
            conversation.setOwner(user);
            conversation.setUser(currentUser);
            conversation.setGroup(group);
            conversation.setType(ConversationType.GROUP);
            conversations.add(conversation);
        }
        conversationRepository.saveAll(conversations);

        Result result = new Result();
        result.setSuccess(true);
        result.setMessage("successfully created");
        return ResponseEntity.ok(result);
    }
}
