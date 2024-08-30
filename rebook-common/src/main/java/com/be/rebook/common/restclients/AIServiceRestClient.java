package com.be.rebook.common.restclients;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import com.be.rebook.common.dto.InternalAdviceDTO.InternalAdviceRequestDTO;
import com.be.rebook.common.dto.InternalAdviceDTO.InternalAdviceResponseDTO;

public interface AIServiceRestClient {

  @PostExchange(value = "/predict", contentType = MediaType.APPLICATION_JSON_VALUE, accept = MediaType.APPLICATION_JSON_VALUE)
  InternalAdviceResponseDTO predictMessage(@RequestBody InternalAdviceRequestDTO internalAdviceRequestDTO);
}
