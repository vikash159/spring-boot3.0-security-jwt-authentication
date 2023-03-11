package com.chat.repository;

import com.chat.model.Conversation;
import com.chat.model.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUserId(Long userId);

    Optional<Conversation> findByUserIdAndOwnerIdAndType(Long userId, Long ownerId, ConversationType type);

    Page<Conversation> findAllByOwnerId(Long ownerId, Pageable pageable);

    List<Conversation> findAllByGroupId(Long groupId);
}
