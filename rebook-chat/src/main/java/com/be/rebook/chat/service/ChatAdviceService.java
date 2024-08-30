package com.be.rebook.chat.service;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.be.rebook.chat.dto.ChatMessageDTO;
import com.be.rebook.chat.entity.ChatAdvice;
import com.be.rebook.chat.entity.ChatMessage;
import com.be.rebook.chat.entity.ChatRoom;
import com.be.rebook.chat.repository.ChatAdviceRepository;
import com.be.rebook.chat.repository.ChatMessageRepository;
import com.be.rebook.chat.repository.ChatRoomRepository;
import com.be.rebook.common.dto.InternalAdviceDTO.InternalAdviceRequestDTO;
import com.be.rebook.common.dto.InternalAdviceDTO.InternalAdviceResponseDTO;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;
import com.be.rebook.common.config.BaseResponse;
import com.be.rebook.common.dto.InternalMemberInfoDTO;
import com.be.rebook.common.restclients.AIServiceRestClient;
import com.be.rebook.common.restclients.MemberServiceRestClient;
import com.be.rebook.common.restclients.RestClientFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChatAdviceService {

  // 채팅 DB 레포지터리
  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ChatAdviceRepository chatAdviceRepository;

  // REST API 호출을 위한 Factory
  private final RestClientFactory restClientFactory;

  // 발행 처리 서비스
  private final RedisPublisher redisPublisher;

  // TODO : 유저 식절자 id로 바꾸며 리펙토링 필요
  // @Async <- 비동기 처리를 하게되면 트랜잭션 처리가 제대로 되지 않을 수 있음
  @Transactional
  public void generateAdvice(ChatMessageDTO message) {
    try {

      MemberServiceRestClient memberClient = restClientFactory.createMemberServiceRestClient(null);
      AIServiceRestClient aiServiceRestClient = restClientFactory.createAIServiceRestClient(null);
      ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
          .orElseThrow(() -> new BaseException(ErrorCode.CHAT_ROOM_NOT_FOUND));
      ChatMessage chatMessage = chatMessageRepository.findById(message.getRoomId())
          .orElseThrow(() -> new BaseException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

      ResponseEntity<BaseResponse<InternalMemberInfoDTO>> userInfoRes = memberClient
          .getMemberInfoByUsername(message.getSenderUsername());

      if (userInfoRes.getBody().getStatus() != HttpStatus.OK)
        return;

      InternalMemberInfoDTO userInfo = userInfoRes.getBody().getResult();
      InternalAdviceRequestDTO internalAdviceRequestDTO = new InternalAdviceRequestDTO(message.getRoomId(),
          userInfo.getMemberId(), message.getMessage()); // TODO : 추가적인 에러 핸들링 필요

      InternalAdviceResponseDTO adviceRes = aiServiceRestClient
          .predictMessage(internalAdviceRequestDTO);

      ChatAdvice chatAdvice = new ChatAdvice(adviceRes.getResult(), adviceRes.getWarningMessage(), chatMessage);
      chatAdviceRepository.save(chatAdvice);

      ChatMessageDTO adviceMessage = ChatMessageDTO.createAdviceMessage(chatRoom.getId(), chatMessage.getId(),
          message.getSenderUsername(), adviceRes.getWarningMessage());
      ChannelTopic topic = new ChannelTopic(message.getRoomId().toString());
      redisPublisher.publish(topic, adviceMessage);
    } catch (Exception e) {
      log.error("Failed to generate advice for message: {}", message);
    }
  }
}
