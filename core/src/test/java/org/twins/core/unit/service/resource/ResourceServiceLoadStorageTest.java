package org.twins.core.unit.service.resource;

import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.ResourceRepository;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.storage.StorageService;
import org.twins.core.service.user.UserService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Storage loading for resources must not run the read-permission (domain) check: a public/anonymous
 * request has no domain at all (browser {@code <img>} tags cannot send the DomainId header), and the
 * storageId is resolved from the resource row itself, access being decided at the resource level
 * (domain check on private APIs, resource.public flag on the public download path).
 * Regression: TWINS-942 moved storage loading onto the checked load pattern and every public
 * download started failing with "resourceStorage[...] is not allowed in current domain".
 */
class ResourceServiceLoadStorageTest extends BaseUnitTest {

    @Mock
    private StorageService storageService;

    private ResourceService service;

    @BeforeEach
    void setUp() {
        service = new ResourceService(
                mock(FeaturerService.class),
                mock(ResourceRepository.class),
                storageService,
                mock(AuthService.class),
                mock(UserService.class));
    }

    @Test
    void loadStorage_loadsWithoutReadPermissionCheck() throws Exception {
        UUID storageId = UUID.randomUUID();
        StorageEntity storage = new StorageEntity().setId(storageId);
        ResourceEntity resource = new ResourceEntity().setId(UUID.randomUUID()).setStorageId(storageId);
        when(storageService.findEntities(anyCollection(),
                eq(EntitySmartService.ListFindMode.ifMissedThrows),
                eq(EntitySmartService.ReadPermissionCheckMode.none),
                eq(EntitySmartService.EntityValidateMode.none)))
                .thenReturn(new Kit<>(List.of(storage), StorageEntity::getId));

        service.loadStorage(List.of(resource));

        assertEquals(storage, resource.getStorage());
        verify(storageService).findEntities(anyCollection(),
                eq(EntitySmartService.ListFindMode.ifMissedThrows),
                eq(EntitySmartService.ReadPermissionCheckMode.none),
                eq(EntitySmartService.EntityValidateMode.none));
    }

    @Test
    void loadStorage_missingStorageIdLeftUnloaded() throws Exception {
        ResourceEntity resource = new ResourceEntity().setId(UUID.randomUUID()).setStorageId(null);
        when(storageService.findEntities(anyCollection(),
                eq(EntitySmartService.ListFindMode.ifMissedThrows),
                eq(EntitySmartService.ReadPermissionCheckMode.none),
                eq(EntitySmartService.EntityValidateMode.none)))
                .thenReturn(new Kit<>(List.of(), StorageEntity::getId));

        service.loadStorage(List.of(resource));

        assertNull(resource.getStorage());
    }
}
