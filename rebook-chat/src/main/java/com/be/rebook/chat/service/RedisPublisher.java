package com.be.rebook.chat.service;

import com.be.rebook.chat.dto.ChatMessageDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(ChannelTopic topic, ChatMessageDTO message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
