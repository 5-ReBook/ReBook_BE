package com.be.rebook.chat.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.be.rebook.chat.dto.ChatMessageDTO;
import com.be.rebook.chat.dto.ChatRoomDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatSocketService {
    // 채팅방(topic)에 발행되는 메시지를 처리할 Listner
    private final RedisMessageListenerContainer redisMessageListener;
    // 구독 처리 서비스
    private final RedisSubscriber redisSubscriber;
    // 발행 처리 서비스
    private final RedisPublisher redisPublisher;
    // 채팅방 DB 레포지터리
    private final ChatService chatService;

    // AI advice 서비스
    private final ChatAdviceService chatAdviceService;

    // Topic을 추적하기 위한 Map<채팅방ID, Topic>
    // 메모리에 부담이 갈 수 있음.
    private final Map<Long, ChannelTopic> topics = new HashMap<Long, ChannelTopic>();

    /**
     * 채팅방 입장 : redis에 topic을 만들고 pub/sub 통신을 하기 위해 리스너를 설정한다.
     */
    public void enterChatRoom(Long roomId) {
        ChatRoomDto chatRoom = chatService.findChatRoomById(roomId);
        ChannelTopic topic = topics.get(chatRoom.getRoomId());
        if (topic == null) {
            topic = new ChannelTopic(roomId.toString());
            redisMessageListener.addMessageListener(redisSubscriber, topic); // 추후 채팅방 삭제시 리스너도 삭제해야함
        }
    }

    @Transactional
    public void sendMessage(ChatMessageDTO message) {
        message = chatService.addMessageToChatRoom(message);

        chatAdviceService.generateAdvice(message);

        // 채팅방에 발행된 메시지를 전송한다.
        ChannelTopic topic = new ChannelTopic(message.getRoomId().toString());
        redisPublisher.publish(topic, message);
    }

    @Transactional
    public void readMessage(Long chatMessageId) {
        chatService.patchMessageRead(chatMessageId);
    }
}
