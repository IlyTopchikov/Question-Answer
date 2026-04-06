package com.example.demo.repository;

import com.example.demo.domain.AppUser;
import com.example.demo.domain.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Query("""
        SELECT m FROM DirectMessage m
        WHERE (m.sender = :a AND m.receiver = :b)
           OR (m.sender = :b AND m.receiver = :a)
        ORDER BY m.createdAt ASC
    """)
    List<DirectMessage> findConversation(@Param("a") AppUser a, @Param("b") AppUser b);

    @Query("""
        SELECT DISTINCT m.receiver FROM DirectMessage m WHERE m.sender = :user
        UNION
        SELECT DISTINCT m.sender FROM DirectMessage m WHERE m.receiver = :user
    """)
    List<AppUser> findContacts(@Param("user") AppUser user);

    long countByReceiverAndReadFalse(AppUser receiver);
}
