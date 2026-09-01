package com.tangan.glucose.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangan.glucose.common.ApiException;
import com.tangan.glucose.dto.FamilyDtos;
import com.tangan.glucose.entity.FamilyConnection;
import com.tangan.glucose.repository.*;
import com.tangan.glucose.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

@Service @RequiredArgsConstructor
public class FamilyConnectionServiceImpl implements FamilyConnectionService {
    private final FamilyConnectionRepository repository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override @Transactional(readOnly = true)
    public List<FamilyDtos.Response> list(UUID userId) { return repository.findByUserId(userId).stream().map(this::toResponse).toList(); }

    @Override @Transactional(rollbackFor = Exception.class)
    public FamilyDtos.Response create(UUID userId, FamilyDtos.Request request) {
        FamilyConnection connection = new FamilyConnection(); connection.setUser(userService.require(userId));
        connection.setContact(request.contact()); connection.setRelationship(request.relationship());
        try { connection.setPermissionsJson(objectMapper.writeValueAsString(request.permissions())); }
        catch (Exception ex) { throw ApiException.badRequest("permissions 格式无效"); }
        connection.setExpiresAt(OffsetDateTime.now().plusDays(7));
        return toResponse(repository.save(connection));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, UUID id) {
        if (repository.deleteOwned(id, userId) == 0) throw ApiException.notFound("家属共享关系不存在");
    }

    private FamilyDtos.Response toResponse(FamilyConnection c) {
        List<String> permissions;
        try { permissions = objectMapper.readValue(c.getPermissionsJson(), new TypeReference<>() { }); }
        catch (Exception ex) { permissions = List.of(); }
        return new FamilyDtos.Response(c.getId(), c.getContact(), c.getRelationship(), permissions, c.getStatus(), c.getExpiresAt(), c.getCreatedAt());
    }
}
