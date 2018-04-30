package com.github.polimi_mt_acg.back2school.api.v1;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * A class to have Jersey use our customization of Jackson Object Mapper.
 *
 * <p>Features enabled: Indent output JSON text. Allow comments in JSON files.
 */
@Provider
public class JacksonCustomMapper implements ContextResolver<ObjectMapper> {

  private final ObjectMapper defaultObjectMapper;

  public JacksonCustomMapper() {
    this.defaultObjectMapper = createDefaultMapper();
  }

  private static ObjectMapper createDefaultMapper() {
    final ObjectMapper result = new ObjectMapper();

    result.registerModule(new JavaTimeModule());
    result.enable(SerializationFeature.INDENT_OUTPUT);
    result.enable(Feature.ALLOW_COMMENTS);

    return result;
  }

  @Override
  public ObjectMapper getContext(Class<?> aClass) {
    return defaultObjectMapper;
  }
}
