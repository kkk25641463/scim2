package com.example.handler;

import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.ConflictException;
import de.captaingoldfish.scim.sdk.common.exceptions.ResourceNotFoundException;
import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;


/**
 * author Pascal Knueppel <br>
 * created at: 03.11.2019 - 00:14 <br>
 * <br>
 * a simple handler implementation for managing users
 */
public class UserHandler extends ResourceHandler<User>
{

  /**
   * an in memory map that holds our 5000 users
   */
  @Getter
  private Map<String, User> inMemoryMap = new HashMap<>();

  /**
   * adds approximately 5000 users into the in memory map
   */
  public UserHandler()
  {
    try (InputStream inputStream = getClass().getResourceAsStream("/firstnames.txt");
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
      BufferedReader reader = new BufferedReader(inputStreamReader))
    {

      String name;
      while ((name = reader.readLine()) != null)
      {
        String id = UUID.randomUUID().toString();
        Meta meta = Meta.builder().created(LocalDateTime.now()).lastModified(LocalDateTime.now()).build();
        inMemoryMap.put(id, User.builder().id(id).userName(name).nickName(name).meta(meta).build());
      }
    }
    catch (IOException e)
    {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User createResource(User resource, Context context)
  {
    final String userId = UUID.randomUUID().toString();
    if (inMemoryMap.containsKey(userId))
    {
      throw new ConflictException("resource with id '" + userId + "' does already exist");
    }
    resource.setId(userId);
    inMemoryMap.put(userId, resource);
    resource.getMeta().ifPresent(meta -> {
      meta.setCreated(Instant.now());
      meta.setLastModified(Instant.now());
    });
    return resource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User getResource(String id,
                          List<SchemaAttribute> attributes,
                          List<SchemaAttribute> excludedAttributes,
                          Context context)
  {
    return inMemoryMap.get(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PartialListResponse<User> listResources(long startIndex,
                                                 int count,
                                                 FilterNode filter,
                                                 SchemaAttribute sortBy,
                                                 SortOrder sortOrder,
                                                 List<SchemaAttribute> attributes,
                                                 List<SchemaAttribute> excludedAttributes,
                                                 Context context)
  {
    List<User> resourceNodes = new ArrayList<>(inMemoryMap.values());
    return PartialListResponse.<User> builder().resources(resourceNodes).totalResults(resourceNodes.size()).build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User updateResource(User resource, Context context)
  {
    String userId = resource.getId().get();
    User oldUser = inMemoryMap.get(userId);
    if (oldUser == null)
    {
      throw new ResourceNotFoundException("resource with id '" + userId + "' does not exist", null, null);
    }
    inMemoryMap.put(userId, resource);
    resource.getMeta().ifPresent(meta -> {
      meta.setCreated(oldUser.getMeta().get().getCreated().get());
      meta.setLastModified(Instant.now());
    });
    return resource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    if (inMemoryMap.containsKey(id))
    {
      inMemoryMap.remove(id);
    }
    else
    {
      throw new ResourceNotFoundException("resource with id '" + id + "' does not exist", null, null);
    }
  }
}
