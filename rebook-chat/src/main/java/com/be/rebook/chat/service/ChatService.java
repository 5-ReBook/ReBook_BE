package com.be.rebook.chat.service;

import java.nio.channels.Channel;
import java.util.List;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.be.rebook.chat.dto.ChatMessageDTO;
import com.be.rebook.chat.dto.ChatRoomDto;
import com.be.rebook.chat.dto.CreateChatRoomDto;
import com.be.rebook.chat.entity.ChatMessage;
import com.be.rebook.chat.entity.ChatRoom;
import com.be.rebook.chat.repository.ChatRoomRepository;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public ChatRoomDto findChatRoomById(Long id) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND)); // TODO : 적절한 예외처리 변경
        return new ChatRoomDto(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatRoomHistory(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(roomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        List<ChatMessage> messages = chatRoom.getMessages();
        List<ChatMessageDTO> messageDtos = messages.stream().map(ChatMessageDTO::new).toList();
        return messageDtos;
    }

    @Transactional
    public Long createChatRoom(CreateChatRoomDto createChatRoomDto) {
        ChatRoom newChatRoom = new ChatRoom(createChatRoomDto);
        newChatRoom = chatRoomRepository.save(newChatRoom);
        return newChatRoom.getId();
    }

    @Transactional
    public Long addMessageToChatRoom(ChatMessageDTO message) {
        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Long senderId = message.getSenderId();
        if (senderId != chatRoom.getBuyerId() && senderId != chatRoom.getSellerId()) {
            throw new BaseException(ErrorCode.CHAT_SENDER_NOT_EIXIST);
        }

        chatRoom.addMessage(message);
        chatRoomRepository.save(chatRoom);
        return 0L;
    }
}
