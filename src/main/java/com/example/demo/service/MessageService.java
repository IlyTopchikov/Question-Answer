package com.example.demo.service;

import com.example.demo.domain.AppUser;
import com.example.demo.domain.DirectMessage;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.DirectMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final DirectMessageRepository messageRepo;
    private final AppUserRepository userRepo;

    public MessageService(DirectMessageRepository messageRepo, AppUserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public DirectMessage sendMessage(String senderUsername, String receiverUsername, String body) {
        AppUser sender = userRepo.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("Отправитель не найден"));
        AppUser receiver = userRepo.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь «" + receiverUsername + "» не найден"));
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Нельзя написать самому себе");
        }
        DirectMessage msg = new DirectMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setBody(body.trim());
        return messageRepo.save(msg);
    }

    @Transactional
    public List<DirectMessage> getConversation(String myUsername, String otherUsername) {
        AppUser me = userRepo.findByUsername(myUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        AppUser other = userRepo.findByUsername(otherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь «" + otherUsername + "» не найден"));
        List<DirectMessage> msgs = messageRepo.findConversation(me, other);
        // Помечаем прочитанными
        msgs.stream()
            .filter(m -> m.getReceiver().getId().equals(me.getId()) && !m.isRead())
            .forEach(m -> m.setRead(true));
        return msgs;
    }

    @Transactional(readOnly = true)
    public List<AppUser> getContacts(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return messageRepo.findContacts(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        AppUser user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return 0;
        return messageRepo.countByReceiverAndReadFalse(user);
    }
}
