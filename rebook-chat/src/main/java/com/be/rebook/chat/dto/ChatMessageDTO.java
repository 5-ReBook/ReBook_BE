package com.be.rebook.chat.dto;

import com.be.rebook.chat.entity.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatMessageDTO { // TODO : ChatMessage 추상화
    private String senderUsername;
    private MessageType type;
    private String message;
    private Long roomId;

    private Long chatMessageId;
    private boolean isRead;

    private String createdAt;

    private ChatAdviceDTO chatAdvice;

    public enum MessageType {
        JOIN, TALK, ENTER, READ, ADVICE, ERROR
    }

    public ChatMessageDTO(ChatMessage message) {
        this.senderUsername = message.getSenderUsername();
        this.type = ChatMessageDTO.MessageType.TALK;
        this.message = message.getMessage();
        this.roomId = message.getChatRoom().getId();
        this.isRead = message.getIsRead();
        this.chatMessageId = message.getId();
        this.createdAt = message.getCreatedAt().toString();
        if (message.getAdvice() != null)
            this.chatAdvice = new ChatAdviceDTO(message.getAdvice());
        else
            this.chatAdvice = null;
    }

    static public ChatMessageDTO createAdviceMessage(Long roomId, Long ChatmessageId, String senderUsername,
            String message) {
        ChatMessageDTO chatMessageDTO = ChatMessageDTO.builder().roomId(roomId).chatMessageId(ChatmessageId)
                .senderUsername(senderUsername).message(message).type(MessageType.ADVICE).build();
        return chatMessageDTO;
    }

    @Override
    public String toString() {
        return "ChatMessageDTO [senderUsername=" + senderUsername + ", type=" + type + ", message=" + message
                + ", roomId=" + roomId + ", chatMessageId=" + chatMessageId + ", isRead=" + isRead + ", createdAt="
                + createdAt + "]";
    }
}
