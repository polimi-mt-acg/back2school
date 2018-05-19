package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

public class SecurityContextPriority {
  // Note that Priority.AUTHORIZATION == 2000

  public static final int PARENT_TEACHER_ADMINISTRATOR = 2001;
  public static final int PARENT_ADMINISTRATOR = 2002;
  public static final int TEACHER_ADMINISTRATOR = 2003;
  public static final int ADMINISTRATOR = 2010;
  public static final int PARENT_OF_STUDENT = 2030;
  public static final int TEACHER_OF_STUDENT = 2031;
}
