package com.be.rebook.chat.entity;

import com.be.rebook.common.config.BaseEntity;
import com.be.rebook.common.type.AdviceType;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ChatAdvice extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "advice_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "result")
  private AdviceType result;

  @Column(name = "warning_message")
  private String warningMessage;

  @OneToOne
  @JoinColumn(name = "chat_message_id") // 외래 키 컬럼을 Advice 테이블에 생성
  private ChatMessage chatMessage;

  public ChatAdvice(AdviceType result, String warningMessage, ChatMessage chatMessage) {
    this.result = result;
    this.warningMessage = warningMessage;
    this.chatMessage = chatMessage;
  }
}
