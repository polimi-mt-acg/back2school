package com.github.polimi_mt_acg.back2school;

import com.github.polimi_mt_acg.back2school.utils.JacksonCustomMapper;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("v1")
public class RESTApplication extends ResourceConfig {

  public RESTApplication() {
    packages("com.github.polimi_mt_acg.back2school");
    register(JacksonCustomMapper.class);
    register(JacksonFeature.class);
  }
}
