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
        SELECT DISTINCT CASE WHEN m.sender = :user THEN m.receiver ELSE m.sender END
        FROM DirectMessage m WHERE m.sender = :user OR m.receiver = :user
    """)
    List<AppUser> findContacts(@Param("user") AppUser user);

    long countByReceiverAndReadFalse(AppUser receiver);
}
