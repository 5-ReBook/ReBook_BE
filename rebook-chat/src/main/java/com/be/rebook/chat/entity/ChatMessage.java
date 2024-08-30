package com.be.rebook.chat.entity;

import com.be.rebook.chat.dto.ChatMessageDTO;
import com.be.rebook.common.config.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "sender_username")
    private String senderUsername;

    @Column(name = "is_read")
    private Boolean isRead;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @OneToOne(mappedBy = "chatMessage") // Advice 엔티티의 chatMessage 필드에 의해 매핑됨
    private ChatAdvice advice;

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public ChatMessage(ChatMessageDTO message, ChatRoom chatRoom) {
        this.message = message.getMessage();
        this.senderUsername = message.getSenderUsername();
        this.isRead = false;
        this.chatRoom = chatRoom;
    }
}
