package com.be.rebook.common.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AdviceType {
  SAFE(0),
  PHISHING(1),
  EXTERNAL_MESSENGER(2),
  ACCOUNT_NUM(3);

  private final int label;

  AdviceType(int label) {
    this.label = label;
  }

  @JsonValue
  public int getLabel() {
    return label;
  }

  @JsonCreator
  public static AdviceType fromLabel(int label) {
    for (AdviceType type : AdviceType.values()) {
      if (type.getLabel() == label) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid label: " + label);
  }
}
