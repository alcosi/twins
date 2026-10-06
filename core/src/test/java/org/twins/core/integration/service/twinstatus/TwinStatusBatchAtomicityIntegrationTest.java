package org.twins.core.integration.service.twinstatus;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.twins.core.base.BaseIntegrationTest;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.dao.idp.IdentityProviderRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
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
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinstatus.TwinStatusService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TWINS-934 batch contract detectors:
 * <ul>
 *     <li>all-or-nothing: a failure on the k-th element rolls back the whole batch</li>
 *     <li>happy path: a valid batch persists every element with its own name i18n</li>
 * </ul>
 * Deliberately NOT {@code @Transactional}: the service must open and roll back its own transaction,
 * otherwise the rollback assertions would read uncommitted rows of the joined test transaction.
 */
public class TwinStatusBatchAtomicityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TwinStatusService twinStatusService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IdentityProviderRepository identityProviderRepository;
    @Autowired
    private PermissionSchemaRepository permissionSchemaRepository;
    @Autowired
    private TwinflowSchemaRepository twinflowSchemaRepository;
    @Autowired
    private TwinClassSchemaRepository twinClassSchemaRepository;
    @Autowired
    private TwinClassRepository twinClassRepository;
    @Autowired
    private DomainRepository domainRepository;

    @MockitoBean
    private AuthService authService;

    private UUID domainId;
    private UUID userId;
    private UUID twinClassId;
    private UUID idpId;
    private UUID permSchemaId;
    private UUID flowSchemaId;
    private UUID classSchemaId;

    @BeforeEach
    public void setupData() throws ServiceException {
        userId = UUID.randomUUID();
        userRepository.save(new UserEntity()
                .setId(userId)
                .setName("Test User")
                .setEmail("test_" + userId + "@example.com")
                .setUserStatusId(UserStatus.ACTIVE));

        idpId = UUID.randomUUID();
        identityProviderRepository.save(new IdentityProviderEntity()
                .setId(idpId)
                .setName("Test IDP")
                .setStatus(IdentityProviderEntity.IdentityProviderStatus.ACTIVE)
                .setTrustorFeaturerId(3501));

        permSchemaId = UUID.fromString("00000000-0000-0000-0012-000000000001");
        permissionSchemaRepository.save(new PermissionSchemaEntity().setId(permSchemaId).setCreatedByUserId(userId).setName("System Perm"));

        flowSchemaId = UUID.fromString("00000000-0000-0000-0013-000000000001");
        twinflowSchemaRepository.save(new TwinflowSchemaEntity().setId(flowSchemaId).setCreatedByUserId(userId).setName("System Flow"));

        classSchemaId = UUID.fromString("00000000-0000-0000-0014-000000000001");
        twinClassSchemaRepository.save(new TwinClassSchemaEntity().setId(classSchemaId).setCreatedByUserId(userId).setName("System Class Schema"));

        twinClassId = UUID.randomUUID();
        TwinClassEntity twinClass = new TwinClassEntity()
                .setId(twinClassId)
                .setDomainId(null) // system class: domain check passes for null domain
                .setKey("TEST_STATUS_CLASS_" + twinClassId)
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

        domainId = UUID.randomUUID();
        domainRepository.save(new DomainEntity()
                .setId(domainId)
                .setKey("TEST_STATUS_DOMAIN_" + domainId)
                .setName("Test Domain")
                .setDomainType(DomainType.basic)
                .setDomainStatusId(DomainStatus.ACTIVE)
                .setIdentityProviderId(idpId)
                .setPermissionSchemaId(permSchemaId)
                .setTwinflowSchemaId(flowSchemaId)
                .setTwinClassSchemaId(classSchemaId)
                .setAncestorTwinClassId(twinClassId)
                .setAttachmentsStorageUsedCount(0L)
                .setAttachmentsStorageUsedSize(0L)
                .setDomainUserInitiatorFeaturerId(3401)
                .setBusinessAccountInitiatorFeaturerId(3401));

        stubApiUser();
    }

    @AfterEach
    public void cleanup() {
        jdbcTemplate.update("DELETE FROM i18n WHERE id IN (SELECT name_i18n_id FROM twin_status WHERE twin_class_id = ?)", twinClassId);
        jdbcTemplate.update("DELETE FROM i18n WHERE id IN (SELECT description_i18n_id FROM twin_status WHERE twin_class_id = ?)", twinClassId);
        jdbcTemplate.update("DELETE FROM i18n WHERE domain_id = ?", domainId);
        jdbcTemplate.update("DELETE FROM twin_status WHERE twin_class_id = ?", twinClassId);
        domainRepository.deleteById(domainId);
        twinClassRepository.deleteById(twinClassId);
        twinClassSchemaRepository.deleteById(classSchemaId);
        twinflowSchemaRepository.deleteById(flowSchemaId);
        permissionSchemaRepository.deleteById(permSchemaId);
        identityProviderRepository.deleteById(idpId);
        userRepository.deleteById(userId);
    }

    @Test
    public void batchFailureOnSecondElementRollsBackWholeBatch() {
        TwinStatusEntity valid = status("st_a");
        TwinStatusEntity invalid = status(null); // null key -> TWIN_STATUS_KEY_INCORRECT inside the service
        ServiceException se = assertThrows(ServiceException.class, () ->
                twinStatusService.createStatuses(
                        List.of(valid, invalid),
                        List.of((I18nEntity) null, null),
                        List.of((I18nEntity) null, null),
                        List.of((FileData) null, null),
                        List.of((FileData) null, null)));
        assertEquals(ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT.getCode(), se.getErrorCode());
        // the first element must NOT survive: the batch is all-or-nothing
        assertEquals(0, countStatuses(), "valid first element must be rolled back with the failed batch");
    }

    @Test
    public void validBatchPersistsAllElements() throws ServiceException {
        List<TwinStatusEntity> created = twinStatusService.createStatuses(
                List.of(status("st_a"), status("st_b")),
                List.of((I18nEntity) null, null),
                List.of((I18nEntity) null, null),
                List.of((FileData) null, null),
                List.of((FileData) null, null));
        assertEquals(2, created.size());
        assertEquals(2, countStatuses());
        for (TwinStatusEntity status : created) {
            assertNotNull(status.getNameI18nId(), "each element must get its own name i18n");
        }
    }

    private TwinStatusEntity status(String key) {
        return new TwinStatusEntity()
                .setId(UuidUtils.generate())
                .setTwinClassId(twinClassId)
                .setKey(key);
    }

    private void stubApiUser() throws ServiceException {
        ApiUser apiUser = Mockito.mock(ApiUser.class);
        Mockito.when(apiUser.getDomainId()).thenReturn(domainId);
        Mockito.when(apiUser.getDomain()).thenReturn(new DomainEntity().setId(domainId));
        Mockito.when(apiUser.getUserId()).thenReturn(userId);
        Mockito.when(authService.getApiUser()).thenReturn(apiUser);
    }

    private int countStatuses() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM twin_status WHERE twin_class_id = ?", Integer.class, twinClassId);
        return count == null ? 0 : count;
    }
}
