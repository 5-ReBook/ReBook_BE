package com.be.rebook.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalMemberInfoDTO {
  private Long memberId;
  private String username;
  private String nickname;
  private String university;
  private String majors;
  private String storedFileName;

  public InternalMemberInfoDTO(Long memberId, String username, String nickname, String university,
      String majors,
      String storedFileName) {
    this.memberId = memberId;
    this.username = username;
    this.nickname = nickname;
    this.university = university;
    this.majors = majors;
    this.storedFileName = storedFileName;
  }

  public String toString() {
    return "memberId: " + memberId + ", username: " + username + ", nickname: " + nickname + ", university: "
        + university + ", majors: " + majors + ", storedFileName: " + storedFileName;
  }
}
