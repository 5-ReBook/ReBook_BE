package com.be.rebook.chat.controller;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.be.rebook.chat.dto.ChatMessageDTO;
import com.be.rebook.chat.dto.ChatRoomDto;
import com.be.rebook.chat.repository.ChatRoomRepository;
import com.be.rebook.chat.service.ChatService;
import com.be.rebook.chat.service.ChatSocketService;
import com.be.rebook.chat.service.RedisPublisher;
import com.be.rebook.chat.service.RedisSubscriber;
import com.be.rebook.common.exception.BaseException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ChatSocketController {

    private final ChatSocketService chatSocketService;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/message")
    public void message(ChatMessageDTO message) { // TODO: Redis로 성능 향상 시킬수 있음
        try {
            switch (message.getType()) {
                case TALK:
                    chatSocketService.sendMessage(message);
                    break;
                case ENTER:
                    chatSocketService.enterChatRoom(message.getRoomId());
                    break;
                case READ:
                    // TODO : 읽음 처리
                    chatSocketService.readMessage(message.getChatMessageId());
                case ERROR:
                    // TODO : 현재는 에러 메시지 요청에 대한 응답이 없음
                    break;
                default:
                    break;
            }
        } catch (BaseException e) {
            e.printStackTrace(); // TODO : 에러 응답 주기
        }
    }
}