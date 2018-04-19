package com.github.polimi_mt_acg.back2school.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JacksonCustomMapper implements ContextResolver<ObjectMapper> {

  private final ObjectMapper defaultObjectMapper;

  public JacksonCustomMapper() {
    this.defaultObjectMapper = createDefaultMapper();
  }

  private static ObjectMapper createDefaultMapper() {
    final ObjectMapper result = new ObjectMapper();
    result.enable(SerializationFeature.INDENT_OUTPUT);

    result.registerModule(new JaxbAnnotationModule());
    return result;
  }

  @Override
  public ObjectMapper getContext(Class<?> aClass) {
    return defaultObjectMapper;
  }
}
