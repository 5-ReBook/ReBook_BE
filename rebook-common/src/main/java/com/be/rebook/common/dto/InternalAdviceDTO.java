package com.be.rebook.common.dto;

import com.be.rebook.common.type.AdviceType;

import lombok.Getter;
import lombok.Setter;

public class InternalAdviceDTO {

  @Getter
  @Setter
  public static class InternalAdviceRequestDTO {
    private Long chat_room_id;
    private Long sender_id;
    private String message;

    public InternalAdviceRequestDTO(Long chat_room_id, Long sender_id, String message) {
      this.chat_room_id = chat_room_id;
      this.sender_id = sender_id;
      this.message = message;
    }

    public String toString() {
      return "chat_room_id: " + chat_room_id + ", sender_id: " + sender_id + ", message: " + message;
    }
  }

  @Setter
  public static class InternalAdviceResponseDTO {
    private Long chat_room_id;
    private Long sender_id;
    private AdviceType result;
    private String warning_message;

    public InternalAdviceResponseDTO(Long chat_room_id, Long sender_id, AdviceType result, String warning_message) {
      this.chat_room_id = chat_room_id;
      this.sender_id = sender_id;
      this.result = result;
      this.warning_message = warning_message;
    }

    public Long getChatRoomId() {
      return chat_room_id;
    }

    public Long getSenderId() {
      return sender_id;
    }

    public AdviceType getResult() {
      return result;
    }

    public String getWarningMessage() {
      return warning_message;
    }

    public String toString() {
      return "chat_room_id: " + chat_room_id + ", sender_id: " + sender_id + ", result: " + result
          + ", warning_message: " + warning_message;
    }
    // {
    // "chat_room_id": 1,
    // "sender_id": 2,
    // "result": 0,
    // "warning_message": null
    // }
  }
}
