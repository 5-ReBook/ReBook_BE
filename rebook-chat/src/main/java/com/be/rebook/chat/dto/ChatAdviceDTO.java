package com.be.rebook.chat.dto;

import com.be.rebook.chat.entity.ChatAdvice;
import com.be.rebook.chat.entity.ChatMessage;
import com.be.rebook.common.type.AdviceType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatAdviceDTO {
  private Long id;

  private AdviceType result;

  private String warningMessage;

  private ChatMessage chatMessage;

  public ChatAdviceDTO(Long id, AdviceType result, String warningMessage, ChatMessage chatMessage) {
    this.id = id;
    this.result = result;
    this.warningMessage = warningMessage;
    this.chatMessage = chatMessage;
  }

  public ChatAdviceDTO(ChatAdvice chatAdvice) {
    this.id = chatAdvice.getId();
    this.result = chatAdvice.getResult();
    this.warningMessage = chatAdvice.getWarningMessage();
    this.chatMessage = chatAdvice.getChatMessage();
  }
}
