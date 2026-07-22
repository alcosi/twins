package org.twins.core.integration.service.twin;

import jakarta.persistence.EntityManager;
import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.base.BaseIntegrationTest;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.dao.idp.IdentityProviderRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesApplyResult;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.enums.domain.DomainStatus;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.enums.status.StatusType;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.service.MapperModesResolveService;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twinclass.TwinClassService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reproduces the head_twin_id FK ordering bug: when a batch of new twins reference each other via
 * head_twin_id and are flushed in arbitrary order, the non-deferrable twin_head_twin_id_fk can be
 * violated because a child is INSERTed before its head parent.
 *
 * <p>With the fix, {@link TwinChangesService} sorts the batch by hierarchyTree depth (head-first) so a
 * parent is always persisted before its children. The hierarchyTree is populated here through the
 * canonical {@link org.twins.core.service.twin.TwinHeadService} rule (setHead / initRootHierarchy),
 * mirroring what the multipliers and TwinService.createTwinEntity now do.
 *
 * <p>A single deep chain (length 20) is used because there is exactly one correct head-first
 * persistence order; without the sort the probability of a random iteration matching it is ~1/20!,
 * so the FK violation is effectively deterministic.
 */
@Transactional
public class TwinChangesHeadFkOrderingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TwinChangesService twinChangesService;
    @Autowired
    private TwinRepository twinRepository;
    @Autowired
    private TwinStatusRepository twinStatusRepository;
    @Autowired
    private TwinClassRepository twinClassRepository;
    @Autowired
    private DomainRepository domainRepository;
    @Autowired
    private IdentityProviderRepository identityProviderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PermissionSchemaRepository permissionSchemaRepository;
    @Autowired
    private TwinflowSchemaRepository twinflowSchemaRepository;
    @Autowired
    private TwinClassSchemaRepository twinClassSchemaRepository;
    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private TwinClassService twinClassService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private MapperModesResolveService mapperModesResolveService;
    @MockitoBean
    private I18nService i18nService;

    private TwinClassEntity twinClass;
    private UUID twinStatusId;
    private UUID userId;

    private void stubApiUser(UUID domainId, UserEntity user) throws ServiceException {
        ApiUser apiUser = Mockito.mock(ApiUser.class);
        Mockito.when(apiUser.getDomain()).thenReturn(new DomainEntity().setId(domainId));
        Mockito.when(apiUser.getUser()).thenReturn(user);
        Mockito.when(apiUser.getDomainId()).thenReturn(domainId);
        Mockito.when(authService.getApiUser()).thenReturn(apiUser);
    }

    @BeforeEach
    public void setupData() {
        userId = UUID.randomUUID();
        UserEntity user = new UserEntity()
                .setId(userId)
                .setName("Test User")
                .setEmail("test_" + userId + "@example.com")
                .setUserStatusId(UserStatus.ACTIVE);
        userRepository.save(user);

        UUID idpId = UUID.randomUUID();
        IdentityProviderEntity idp = new IdentityProviderEntity()
                .setId(idpId)
                .setName("Test IDP")
                .setStatus(IdentityProviderEntity.IdentityProviderStatus.ACTIVE)
                .setTrustorFeaturerId(3501);
        identityProviderRepository.save(idp);

        UUID permSchemaId = UUID.fromString("00000000-0000-0000-0012-000000000001");
        permissionSchemaRepository.save(new PermissionSchemaEntity().setId(permSchemaId).setCreatedByUserId(userId).setName("System Perm"));

        UUID flowSchemaId = UUID.fromString("00000000-0000-0000-0013-000000000001");
        twinflowSchemaRepository.save(new TwinflowSchemaEntity().setId(flowSchemaId).setCreatedByUserId(userId).setName("System Flow"));

        UUID classSchemaId = UUID.fromString("00000000-0000-0000-0014-000000000001");
        twinClassSchemaRepository.save(new TwinClassSchemaEntity().setId(classSchemaId).setCreatedByUserId(userId).setName("System Class Schema"));

        UUID domainId = UUID.randomUUID();

        // Twin class with domainId = null first to break circularity
        twinClass = new TwinClassEntity()
                .setId(UUID.randomUUID())
                .setDomainId(null)
                .setKey("TEST_HEAD_CHAIN_CLASS")
                .setOwnerType(OwnerType.SYSTEM)
                .setCreatedByUserId(userId)
                .setAssigneeRequired(false)
                .setSegment(false)
                .setAbstractt(false)
                .setHasSegment(false)
                .setHasDynamicMarkers(false)
                .setUniqueName(false)
                .setHeadHierarchyCounterDirectChildren(0)
                .setExtendsHierarchyCounterDirectChildren(0)
                .setTwinCounter(0)
                .setPermissionSchemaSpace(false)
                .setTwinflowSchemaSpace(false)
                .setTwinClassSchemaSpace(false)
                .setAliasSpace(false);
        twinClassRepository.save(twinClass);
        entityManager.flush();

        DomainEntity domain = new DomainEntity()
                .setId(domainId)
                .setKey("TEST_DOMAIN_" + domainId)
                .setName("Test Domain")
                .setDomainType(DomainType.basic)
                .setDomainStatusId(DomainStatus.ACTIVE)
                .setIdentityProviderId(idpId)
                .setPermissionSchemaId(permSchemaId)
                .setTwinflowSchemaId(flowSchemaId)
                .setTwinClassSchemaId(classSchemaId)
                .setAncestorTwinClassId(twinClass.getId())
                .setAttachmentsStorageUsedCount(0L)
                .setAttachmentsStorageUsedSize(0L)
                .setDomainUserInitiatorFeaturerId(3401)
                .setBusinessAccountInitiatorFeaturerId(3401);
        domainRepository.save(domain);
        entityManager.flush();

        twinClass.setDomainId(domainId);
        twinClassRepository.save(twinClass);

        try {
            stubApiUser(domainId, user);
            Mockito.when(twinClassService.isOwnerSystemType(Mockito.any())).thenReturn(false);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        // Twin status (FK target for twin.twin_status_id)
        twinStatusId = UUID.randomUUID();
        twinStatusRepository.save(new TwinStatusEntity()
                .setId(twinStatusId)
                .setTwinClassId(twinClass.getId())
                .setKey("TEST_STATUS")
                .setType(StatusType.BASIC));
        entityManager.flush();
    }

    @AfterEach
    public void cleanAuth() {
        authService.removeThreadLocalApiUser();
    }

    @Test
    public void applyChanges_headChainInArbitraryOrder_persistsAllWithoutFkViolation() throws ServiceException {
        int chainLength = 20;
        UUID[] ids = new UUID[chainLength];
        for (int i = 0; i < chainLength; i++) {
            ids[i] = UUID.randomUUID();
        }
        TwinEntity[] chain = new TwinEntity[chainLength];
        // Build hierarchyTree parent-first (root = last node) via the canonical TwinHeadService rule,
        // exactly as the multipliers / createTwinEntity do after the fix.
        for (int i = chainLength - 1; i >= 0; i--) {
            TwinEntity t = baseTwin(ids[i], "chain-node-" + i);
            if (i < chainLength - 1) {
                org.twins.core.service.twin.TwinHeadService.setHead(t, chain[i + 1]);
            } else {
                org.twins.core.service.twin.TwinHeadService.initRootHierarchy(t);
            }
            chain[i] = t;
        }

        TwinChangesCollector collector = new TwinChangesCollector();
        // Add child-first into the collector; the collector's ConcurrentHashMap iterates in
        // non-deterministic order, which is precisely why the depth sort is required.
        for (int i = 0; i < chainLength; i++) {
            collector.add(chain[i]);
        }

        TwinChangesApplyResult result = twinChangesService.applyChanges(collector); // must not throw

        for (int i = 0; i < chainLength; i++) {
            assertTrue(twinRepository.findById(ids[i]).isPresent(),
                    "twin node " + i + " should be persisted");
        }
        assertNotNull(result);
    }

    private TwinEntity baseTwin(UUID id, String name) {
        return new TwinEntity()
                .setId(id)
                .setTwinClassId(twinClass.getId())
                .setTwinStatusId(twinStatusId)
                .setName(name)
                .setCreatedByUserId(userId)
                .setOwnerUserId(userId)
                .setCreatedAt(Timestamp.from(Instant.now()));
    }
}
