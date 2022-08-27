package com.example;

import de.captaingoldfish.scim.sdk.common.constants.HttpHeader;
import de.captaingoldfish.scim.sdk.common.constants.enums.HttpMethod;
import de.captaingoldfish.scim.sdk.common.response.ScimResponse;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceEndpoint;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Pascal Knueppel
 * @since 22.01.2021
 */
@Path("/scim/v2")
@Blocking
public class ScimController
{

  @Inject
  private ScimConfig scimConfig;

  @GET
  @Path("/{s:.*}")
  @Produces(HttpHeader.SCIM_CONTENT_TYPE)
  public Response handleScimRequest(@Context HttpServerRequest request, String body)
  {
    ResourceEndpoint resourceEndpoint = scimConfig.getResourceEndpoint();

    ScimAuthentication scimAuthentication = new ScimAuthentication();
    de.captaingoldfish.scim.sdk.server.endpoints.Context context =
    // @formatter:off
                  new de.captaingoldfish.scim.sdk.server.endpoints.Context(scimAuthentication);
    // @formatter:on
    ScimResponse scimResponse = resourceEndpoint.handleRequest(request.absoluteURI(),
                                                               HttpMethod.valueOf(request.method().name()),
                                                               body,
                                                               getHttpHeaders(request),
                                                               context);
    return scimResponse.buildResponse();
  }

  /**
   * extracts the http headers from the request and puts them into a map
   *
   * @param request the request object
   * @return a map with the http-headers
   */
  public Map<String, String> getHttpHeaders(HttpServerRequest request)
  {
    Map<String, String> httpHeaders = new HashMap<>();
    MultiMap headerMap = request.headers();
    for ( Map.Entry<String, String> headerEntry : headerMap )
    {
      httpHeaders.put(headerEntry.getKey(), headerEntry.getValue());

    }
    return httpHeaders;
  }
}
