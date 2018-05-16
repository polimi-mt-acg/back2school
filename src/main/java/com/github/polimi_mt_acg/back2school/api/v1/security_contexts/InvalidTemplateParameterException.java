package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import java.io.IOException;

public class InvalidTemplateParameterException extends IOException {

  public InvalidTemplateParameterException() {
  }

  public InvalidTemplateParameterException(String s) {
    super(s);
  }
}
