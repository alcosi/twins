package org.twins.core.integration.service.datalist;

import jakarta.persistence.EntityManager;
import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.base.BaseIntegrationTest;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.dao.idp.IdentityProviderRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.datalist.DataListSubsetCreate;
import org.twins.core.enums.domain.DomainStatus;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListSubsetService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the TWINS-923 mandatory review fixes against the real backlog migrations:
 * migration_backlog is not scanned by flyway, so the scripts are applied manually inside the
 * test transaction (postgres ddl is transactional, everything rolls back afterwards).
 */
@Transactional
public class DataListSubsetMigrationAndCrudIntegrationTest extends BaseIntegrationTest {

    private static final String BACKLOG_INDEXES =
            "db/migration_backlog/V1.4.xx.04__TWINS-923_data_list_subset_search_sort_indexes.sql";
    private static final String BACKLOG_I18N_AUDIT =
            "db/migration_backlog/V1.4.xx.05__TWINS-923_data_list_subset_i18n_audit.sql";

    @Autowired
    private DataListSubsetService dataListSubsetService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DomainRepository domainRepository;
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

    @MockitoBean
    private AuthService authService;

    private UUID domainId;
    private UUID userId;
    private UUID dataListId;

    private void stubApiUser() throws ServiceException {
        ApiUser apiUser = Mockito.mock(ApiUser.class);
        Mockito.when(apiUser.getDomainId()).thenReturn(domainId);
        Mockito.when(apiUser.getDomain()).thenReturn(new DomainEntity().setId(domainId));
        Mockito.when(apiUser.getUserId()).thenReturn(userId);
        Mockito.when(authService.getApiUser()).thenReturn(apiUser);
    }

    @BeforeEach
    public void setupData() throws ServiceException {
        userId = UUID.randomUUID();
        userRepository.save(new UserEntity()
                .setId(userId)
                .setName("Test User")
                .setEmail("test_" + userId + "@example.com")
                .setUserStatusId(UserStatus.ACTIVE));

        UUID idpId = UUID.randomUUID();
        identityProviderRepository.save(new IdentityProviderEntity()
                .setId(idpId)
                .setName("Test IDP")
                .setStatus(IdentityProviderEntity.IdentityProviderStatus.ACTIVE)
                .setTrustorFeaturerId(3501));

        UUID permSchemaId = UUID.fromString("00000000-0000-0000-0012-000000000001");
        permissionSchemaRepository.save(new PermissionSchemaEntity().setId(permSchemaId).setCreatedByUserId(userId).setName("System Perm"));

        UUID flowSchemaId = UUID.fromString("00000000-0000-0000-0013-000000000001");
        twinflowSchemaRepository.save(new TwinflowSchemaEntity().setId(flowSchemaId).setCreatedByUserId(userId).setName("System Flow"));

        UUID classSchemaId = UUID.fromString("00000000-0000-0000-0014-000000000001");
        twinClassSchemaRepository.save(new TwinClassSchemaEntity().setId(classSchemaId).setCreatedByUserId(userId).setName("System Class Schema"));

        domainId = UUID.randomUUID();

        TwinClassEntity twinClass = new TwinClassEntity()
                .setId(UUID.randomUUID())
                .setDomainId(null)
                .setKey("TEST_SUBSET_CLASS")
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

        domainRepository.save(new DomainEntity()
                .setId(domainId)
                .setKey("TEST_SUBSET_DOMAIN_" + domainId)
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
                .setBusinessAccountInitiatorFeaturerId(3401));
        entityManager.flush();

        // data_list needs a not-null name_i18n_id — seed the i18n row manually
        UUID nameI18nId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO i18n (id, i18n_type_id, domain_id) VALUES (?, 'dataListName', ?)", nameI18nId, domainId);
        dataListId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO data_list (id, domain_id, key, name_i18n_id) VALUES (?, ?, ?, ?)",
                dataListId, domainId, "test_list", nameI18nId);

        stubApiUser();
    }

    private void applyBacklogMigrations() {
        executeScript(BACKLOG_INDEXES);
        executeScript(BACKLOG_I18N_AUDIT);
    }

    private void executeScript(String location) {
        String sql;
        try {
            sql = new ClassPathResource(location).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + location, e);
        }
        // one statement per whole script: postgres simple-query protocol handles the DO $$ blocks and
        // splits statements respecting dollar-quoting
        jdbcTemplate.execute(sql);
    }

    private int countRows(String sql, Object... args) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return count != null ? count : 0;
    }

    // lombok chain setters are declared on the parent and return DataListSubsetSave,
    // so fluent building would not type-check as List<DataListSubsetCreate>
    private DataListSubsetCreate subsetCreate(String key) {
        DataListSubsetCreate create = new DataListSubsetCreate();
        create.setDataListId(dataListId);
        create.setKey(key);
        return create;
    }

    // ------------------------------------------------------------------
    // migration: legacy varchar -> i18n transfer, idempotency, constraints
    // ------------------------------------------------------------------

    @Test
    public void migrationTransfersLegacyColumnsToI18nAndIsIdempotent() {
        UUID legacySubsetId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO data_list_subset (id, data_list_id, key, name, description) VALUES (?, ?, 'legacy', 'Legacy name', 'Legacy desc')",
                legacySubsetId, dataListId);

        applyBacklogMigrations();

        // legacy values transferred to i18n + 'en' translation
        assertNotNull(jdbcTemplate.queryForObject(
                "SELECT name_i18n_id FROM data_list_subset WHERE id = ?", UUID.class, legacySubsetId));
        assertNotNull(jdbcTemplate.queryForObject(
                "SELECT description_i18n_id FROM data_list_subset WHERE id = ?", UUID.class, legacySubsetId));
        assertEquals(1, countRows(
                "SELECT count(*) FROM i18n_translation t JOIN data_list_subset s ON t.i18n_id = s.name_i18n_id WHERE s.id = ? AND t.locale = 'en' AND t.translation = 'Legacy name'",
                legacySubsetId));
        assertEquals(1, countRows(
                "SELECT count(*) FROM i18n_translation t JOIN data_list_subset s ON t.i18n_id = s.description_i18n_id WHERE s.id = ? AND t.locale = 'en' AND t.translation = 'Legacy desc'",
                legacySubsetId));

        // legacy columns dropped
        assertEquals(0, countRows(
                "SELECT count(*) FROM information_schema.columns WHERE table_name = 'data_list_subset' AND column_name IN ('name', 'description')"));

        // fk constraints on i18n columns (review fix: "index and fk for every i18n column")
        assertEquals(2, countRows(
                "SELECT count(*) FROM pg_constraint WHERE conname IN ('fk_data_list_subset_name_i18n', 'fk_data_list_subset_description_i18n')"));
        assertEquals(2, countRows(
                "SELECT count(*) FROM pg_indexes WHERE indexname IN ('idx_data_list_subset_name_i18n_id', 'idx_data_list_subset_description_i18n_id')"));

        // unique (data_list_id, key) exists (review fix: db-level uniqueness)
        assertEquals(1, countRows(
                "SELECT count(*) FROM pg_indexes WHERE indexname = 'ux_data_list_subset_data_list_id_key'"));

        // re-run of the whole script must not fail (idempotent DO-block guard) and must not duplicate data
        assertDoesNotThrow(() -> {
            executeScript(BACKLOG_INDEXES);
            executeScript(BACKLOG_I18N_AUDIT);
        });
        assertEquals(1, countRows(
                "SELECT count(*) FROM i18n_translation t JOIN data_list_subset s ON t.i18n_id = s.name_i18n_id WHERE s.id = ? AND t.locale = 'en' AND t.translation = 'Legacy name'",
                legacySubsetId));
    }

    @Test
    public void uniqueIndexRejectsDuplicateDataListIdAndKey() {
        applyBacklogMigrations();
        jdbcTemplate.update(
                "INSERT INTO data_list_subset (id, data_list_id, key) VALUES (?, ?, 'race_duplicate')",
                UUID.randomUUID(), dataListId);
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
                "INSERT INTO data_list_subset (id, data_list_id, key) VALUES (?, ?, 'race_duplicate')",
                UUID.randomUUID(), dataListId));
    }

    @Test
    public void fkRejectsDanglingI18nReference() {
        applyBacklogMigrations();
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
                "INSERT INTO data_list_subset (id, data_list_id, key, name_i18n_id) VALUES (?, ?, 'dangling', ?)",
                UUID.randomUUID(), dataListId, UUID.randomUUID()));
    }

    // ------------------------------------------------------------------
    // service: create/update/delete guards from the review
    // ------------------------------------------------------------------

    @Test
    public void createRejectsDuplicateKeyInsideOneBatch() throws ServiceException {
        applyBacklogMigrations();
        List<DataListSubsetCreate> batch = List.of(
                subsetCreate("same_key"),
                subsetCreate("same_key"));
        ServiceException se = assertThrows(ServiceException.class, () -> dataListSubsetService.createDataListSubsets(batch));
        assertEquals(ErrorCodeTwins.DATALIST_SUBSET_KEY_IS_NOT_UNIQUE.getCode(), se.getErrorCode());
        assertEquals(0, countRows("SELECT count(*) FROM data_list_subset WHERE key = 'same_key'"));
    }

    @Test
    public void createNormalizesKey() throws ServiceException {
        applyBacklogMigrations();
        List<DataListSubsetEntity> created = dataListSubsetService.createDataListSubsets(List.of(
                subsetCreate("My Key")));
        // KeyUtils: trim, spaces -> underscore, lower case
        assertEquals("my_key", created.get(0).getKey());
    }

    @Test
    public void createRejectsAlreadyPersistedKey() throws ServiceException {
        applyBacklogMigrations();
        dataListSubsetService.createDataListSubsets(List.of(
                subsetCreate("dup")));
        entityManager.flush();
        ServiceException se = assertThrows(ServiceException.class, () -> dataListSubsetService.createDataListSubsets(List.of(
                subsetCreate("DUP"))));
        assertEquals(ErrorCodeTwins.DATALIST_SUBSET_KEY_IS_NOT_UNIQUE.getCode(), se.getErrorCode());
    }

    @Test
    public void deleteRejectedWhileOptionsAreLinkedAndWorksAfterCleanup() throws ServiceException {
        applyBacklogMigrations();
        DataListSubsetEntity subset = dataListSubsetService.createDataListSubsets(List.of(
                subsetCreate("in_use"))).get(0);
        entityManager.flush();

        UUID optionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO data_list_option (id, data_list_id, data_list_option_status_id) VALUES (?, ?, 'active')",
                optionId, dataListId);
        jdbcTemplate.update(
                "INSERT INTO data_list_subset_option (data_list_subset_id, data_list_option_id) VALUES (?, ?)",
                subset.getId(), optionId);

        ServiceException se = assertThrows(ServiceException.class,
                () -> dataListSubsetService.deleteDataListSubsets(Set.of(subset.getId())));
        assertEquals(ErrorCodeTwins.DATALIST_SUBSET_IS_ALREADY_IN_USE.getCode(), se.getErrorCode());
        assertEquals(1, countRows("SELECT count(*) FROM data_list_subset WHERE id = ?", subset.getId()));

        jdbcTemplate.update("DELETE FROM data_list_subset_option WHERE data_list_subset_id = ?", subset.getId());
        assertDoesNotThrow(() -> dataListSubsetService.deleteDataListSubsets(Set.of(subset.getId())));
        entityManager.flush();
        assertEquals(0, countRows("SELECT count(*) FROM data_list_subset WHERE id = ?", subset.getId()));
    }
}
