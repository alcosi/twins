package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.permission.PermissionService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldService {
    final TwinClassFieldRepository twinClassFieldRepository;
    final PermissionService permissionService;

    public List<TwinClassFieldEntity> findTwinClassFields(ApiUser apiUser, UUID twinClassId) {
        permissionService.checkTwinClassPermission(apiUser, twinClassId);
        return findTwinClassFields(twinClassId);
    }

    public List<TwinClassFieldEntity> findTwinClassFields(UUID twinClassId) {
        return twinClassFieldRepository.findByTwinClassId(twinClassId);
    }
}
