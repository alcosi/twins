package org.twins.core.integration.service.twin;

import jakarta.persistence.EntityManager;
import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import org.twins.core.enums.domain.DomainStatus;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.enums.status.StatusType;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.service.MapperModesResolveService;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinHeadService;
import org.twins.core.service.twinclass.TwinClassService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shared fixture for the head_twin_id FK-ordering tests. Builds a single self-referential twin class and
 * exposes helpers to assemble a head-chain and assert it persists in head-first order. Subclasses pin the
 * Hibernate configuration under test (e.g. {@code hibernate.order_inserts}); the chain-building and
 * assertion logic is identical so both configs exercise the same {@link TwinChangesService#applyChanges}
 * path.
 */
public abstract class AbstractTwinHeadFkOrderingTest extends BaseIntegrationTest {

    @Autowired
    protected TwinChangesService twinChangesService;
    @Autowired
    protected TwinRepository twinRepository;
    @Autowired
    protected TwinStatusRepository twinStatusRepository;
    @Autowired
    protected TwinClassRepository twinClassRepository;
    @Autowired
    protected DomainRepository domainRepository;
    @Autowired
    protected IdentityProviderRepository identityProviderRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected PermissionSchemaRepository permissionSchemaRepository;
    @Autowired
    protected TwinflowSchemaRepository twinflowSchemaRepository;
    @Autowired
    protected TwinClassSchemaRepository twinClassSchemaRepository;
    @Autowired
    protected EntityManager entityManager;

    @MockitoBean
    protected TwinClassService twinClassService;
    @MockitoBean
    protected AuthService authService;
    @MockitoBean
    protected MapperModesResolveService mapperModesResolveService;
    @MockitoBean
    protected I18nService i18nService;

    protected TwinClassEntity twinClass;
    protected UUID twinStatusId;
    protected UUID userId;

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

    protected TwinEntity baseTwin(UUID id, String name) {
        return new TwinEntity()
                .setId(id)
                .setTwinClassId(twinClass.getId())
                .setTwinStatusId(twinStatusId)
                .setName(name)
                .setCreatedByUserId(userId)
                .setOwnerUserId(userId)
                .setCreatedAt(Timestamp.from(Instant.now()));
    }

    /**
     * Builds a head-chain of {@code chainLength} twins where node {@code i} has node {@code i+1} as its
     * head (root = last node), wiring hierarchyTree through the canonical {@link TwinHeadService} rule
     * (setHead for a child, initRootHierarchy for the root) — exactly what the multipliers and
     * TwinService.createTwinEntity do after the fix. chain[0] is the deepest leaf.
     */
    protected void buildHeadChain(int chainLength, UUID[] ids, TwinEntity[] chain) {
        for (int i = chainLength - 1; i >= 0; i--) {
            TwinEntity t = baseTwin(ids[i], "chain-node-" + i);
            if (i < chainLength - 1) {
                TwinHeadService.setHead(t, chain[i + 1]);
            } else {
                TwinHeadService.initRootHierarchy(t);
            }
            chain[i] = t;
        }
    }

    /**
     * Asserts every node of the chain was persisted and that each twin's hierarchyTree depth matches the
     * in-memory tree built by setHead / initRootHierarchy — the very depth key the head-first sort relies
     * on. (The DB trigger hierarchyprocesstreeupdate recomputes hierarchy_tree AFTER INSERT, so this checks
     * the Java-side invariant that makes the sort correct, not the trigger output.)
     */
    protected void assertChainPersistedHeadFirst(UUID[] ids, TwinEntity[] chain, int chainLength) {
        for (int i = 0; i < chainLength; i++) {
            assertTrue(twinRepository.findById(ids[i]).isPresent(),
                    "twin node " + i + " should be persisted");
            int expectedDepth = chainLength - i; // root (last) = depth 1, leaf (first) = depth chainLength
            String hierarchyTree = chain[i].getHierarchyTree();
            assertEquals(expectedDepth, hierarchyTree.split("\\.").length,
                    "twin node " + i + " hierarchyTree depth must match the head-first order key");
        }
    }
}
