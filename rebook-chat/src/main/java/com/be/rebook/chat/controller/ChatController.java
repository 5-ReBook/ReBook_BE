package com.be.rebook.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.UnknownContentTypeException;

import com.be.rebook.chat.dto.ChatMessageDTO;
import com.be.rebook.chat.dto.ChatRoomDto;
import com.be.rebook.chat.dto.CreateChatRoomDto;
import com.be.rebook.chat.service.ChatService;
import com.be.rebook.common.argumentresolver.auth.Auth;
import com.be.rebook.common.argumentresolver.auth.MemberLoginInfo;
import com.be.rebook.common.config.BaseResponse;
import com.be.rebook.common.dto.InternalAdviceDTO.InternalAdviceRequestDTO;
import com.be.rebook.common.dto.InternalAdviceDTO.InternalAdviceResponseDTO;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;
import com.be.rebook.common.restclients.AIServiceRestClient;
import com.be.rebook.common.restclients.RestClientFactory;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    private final RestClientFactory restClientFactory;

    @GetMapping("/test")
    public String test() {
        InternalAdviceRequestDTO requestDTO = new InternalAdviceRequestDTO(1L, 1L, "110-437-303283");

        AIServiceRestClient client = restClientFactory.createAIServiceRestClient(null);
        InternalAdviceResponseDTO res;
        try {
            res = client.predictMessage(requestDTO);
            // 응답 처리 로직
        } catch (UnknownContentTypeException ex) {
            // HTML이나 예상치 못한 응답이 왔을 때 처리하는 로직
            System.err.println("Unexpected content type: " + ex.getResponseBodyAsString());
            throw new RuntimeException("Failed to parse response from AI service", ex);
        }

        System.out.println(res.toString());
        return res.toString();
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<BaseResponse<List<ChatMessageDTO>>> getChatRoomHistory(
            @Auth MemberLoginInfo memberLoginInfo, // TODO : AUTH 인증
            @NotNull(message = "roomId must not be null") @PathVariable Long roomId) {
        return ResponseEntity.ok().body(new BaseResponse<List<ChatMessageDTO>>(
                chatService.getChatRoomHistory(roomId, memberLoginInfo.getUsername())));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<BaseResponse<ChatRoomDto>> getChatRoomById(
            @NotNull(message = "roomId must not be null") @PathVariable Long roomId) {
        return ResponseEntity.ok().body(new BaseResponse<ChatRoomDto>(chatService.findChatRoomById(roomId)));
    }

    public String getMethodName(@RequestParam String param) {
        return new String();
    }

    @GetMapping("/me/rooms")
    public ResponseEntity<BaseResponse<List<ChatRoomDto>>> getMethodName(@Auth MemberLoginInfo memberLoginInfo) {
        if (memberLoginInfo == null) { // TODO : AUTH 비동기 문제 해결되면 빼도됨
            throw new BaseException(ErrorCode.UNAUTHORIZED); // TODO : 적절한 예외처리 변경
        }
        List<ChatRoomDto> chatRoomDtos = chatService.findChatRoomsByUsername(memberLoginInfo.getUsername());

        return ResponseEntity.ok()
                .body(new BaseResponse<List<ChatRoomDto>>(chatRoomDtos));
    }

    // @DeleteMapping("/rooms/{roomId}") // TODO : 채팅방 삭제 구현 (listener도 삭제해야함)
    // public ResponseEntity<BaseResponse<Long>> deleteChatRoom( // TODO : AUTH 인증
    // @Auth MemberLoginInfo memberLoginInfo,
    // @NotNull(message = "roomId must not be null") @PathVariable Long roomId) {
    // return ResponseEntity.ok().body(new BaseResponse<Long>(deletedRoomId));
    // }

    // TODO : 서버간 통신 인터페이스 작성 완료시 상품의 판매자와 요청이 일치하는지 검사해야함
    @PostMapping("/rooms")
    public ResponseEntity<BaseResponse<ChatRoomDto>> createChatRoom(@Auth MemberLoginInfo memberLoginInfo,
            @RequestBody CreateChatRoomDto createChatRoomDto) {
        if (memberLoginInfo == null) { // TODO : AUTH 비동기 문제 해결되면 빼도됨
            throw new BaseException(ErrorCode.UNAUTHORIZED); // TODO : 적절한 예외처리 변경
        }

        return ResponseEntity.ok().body(new BaseResponse<ChatRoomDto>(chatService.createChatRoom(createChatRoomDto)));
    }
}
