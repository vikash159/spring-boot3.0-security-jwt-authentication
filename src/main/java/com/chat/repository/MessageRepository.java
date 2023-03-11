package com.chat.repository;

import com.chat.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findAllByGroupId(Long groupId, Pageable pageable);

    @Query("select m from Message m left join fetch m.group left join fetch m.group.user where m.id = ?1")
    Message findByIdWithAssociations(Long id);
}
