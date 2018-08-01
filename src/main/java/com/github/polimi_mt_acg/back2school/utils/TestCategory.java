package com.github.polimi_mt_acg.back2school.utils;

/**
 * TestCategory is a collection of test categories that are helpful to classify tests according to
 * the goals they aim at.
 */
public class TestCategory {

  /**
   * Class for those tests you want to execute transiently only for development purposes while
   * you're implementing a feature.
   *
   * <p>This class should be used only during the early code development. Afterwards choose any
   * other more appropriate category to categorize the written test.
   */
  public interface Transient {}

  /** Class for unit tests. */
  public interface Unit {}

  /** Class for endpoint tests. */
  public interface Endpoint {}

  /** Class for authenticated endpoint tests. */
  public interface AuthEndpoint {}

  /** Class for /students endpoint tests. */
  public interface StudentsEndpoint {}

  /** Class for integration tests. */
  public interface Integration {}
}
