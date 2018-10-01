package com.github.polimi_mt_acg.back2school.api.v1;

import javax.ws.rs.core.Response;

public interface ValidableRequest {

  boolean isValidForPost();

  Response getInvalidPostResponse();

  boolean isValidForPut(Integer id);

  Response getInvalidPutResponse();

}
