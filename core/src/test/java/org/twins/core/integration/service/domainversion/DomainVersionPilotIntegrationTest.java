package org.twins.core.integration.service.domainversion;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.twins.core.base.BaseIntegrationTest;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.dao.idp.IdentityProviderRepository;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TWINS-719 domain configuration versioning pilot (model v2) against real PostgreSQL.
 * migration_backlog is not scanned by flyway, so the pilot scripts are applied manually inside
 * the test (the reference pattern: DataListSubsetMigrationAndCrudIntegrationTest). Each test is
 * self-contained (no class-level transaction): the operation functions manage their own
 * transactions and the concurrency cases need committed data.
 */
public class DomainVersionPilotIntegrationTest extends BaseIntegrationTest {

    private static final String[] BACKLOG_SCRIPTS = {
            "db/migration_backlog/V1.4.381.01__TWINS-719_domain_versioning_schema.sql",
            "db/migration_backlog/V1.4.381.02__TWINS-719_domain_versioning_functions.sql",
            "db/migration_backlog/V1.4.381.03__TWINS-719_domain_versioning_backfill.sql",
            "db/migration_backlog/V1.4.381.04__TWINS-719_domain_versioning_triggers.sql"
    };

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
    private DomainRepository domainRepository;

    @MockitoBean
    private AuthService authService;

    private UUID domainId;
    private UUID userId;
    private UUID twinClassId;
    private UUID dataListId;

    private void stubApiUser() throws ServiceException {
        ApiUser apiUser = Mockito.mock(ApiUser.class);
        Mockito.when(apiUser.getDomainId()).thenReturn(domainId);
        Mockito.when(apiUser.getUserId()).thenReturn(userId);
        Mockito.when(authService.getApiUser()).thenReturn(apiUser);
    }

    @BeforeEach
    public void setupData() throws ServiceException {
        userId = UUID.randomUUID();
        userRepository.save(new org.twins.core.dao.user.UserEntity()
                .setId(userId)
                .setName("DV Test User")
                .setEmail("dv_" + userId + "@example.com")
                .setUserStatusId(org.twins.core.enums.user.UserStatus.ACTIVE));

        UUID idpId = UUID.randomUUID();
        identityProviderRepository.save(new IdentityProviderEntity()
                .setId(idpId)
                .setName("DV Test IDP")
                .setStatus(IdentityProviderEntity.IdentityProviderStatus.ACTIVE)
                .setTrustorFeaturerId(3501));

        UUID permSchemaId = UUID.fromString("00000000-0000-0000-0012-0000000000d1");
        permissionSchemaRepository.save(new org.twins.core.dao.permission.PermissionSchemaEntity()
                .setId(permSchemaId).setCreatedByUserId(userId).setName("DV System Perm"));
        UUID flowSchemaId = UUID.fromString("00000000-0000-0000-0013-0000000000d1");
        twinflowSchemaRepository.save(new org.twins.core.dao.twinflow.TwinflowSchemaEntity()
                .setId(flowSchemaId).setCreatedByUserId(userId).setName("DV System Flow"));
        UUID classSchemaId = UUID.fromString("00000000-0000-0000-0014-0000000000d1");
        twinClassSchemaRepository.save(new org.twins.core.dao.twinclass.TwinClassSchemaEntity()
                .setId(classSchemaId).setCreatedByUserId(userId).setName("DV System Class Schema"));

        domainId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO domain (id, key, domain_type_id, ancestor_twin_class_id, identity_provider_id," +
                "     permission_schema_id, twinflow_schema_id, twin_class_schema_id, domain_user_initiator_featurer_id)" +
                " VALUES (?, ?, 'basic', (SELECT id FROM twin_class WHERE domain_id IS NULL LIMIT 1), ?, ?, ?, ?, 3401)",
                domainId, "DV_TEST_" + domainId, idpId, permSchemaId, flowSchemaId, classSchemaId);

        // pilot rows created BEFORE the pilot migrations: the backfill must stamp them
        twinClassId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO twin_class (id, key, twin_class_owner_type_id, created_by_user_id, domain_id)" +
                " VALUES (?, ?, 'system', ?, ?)",
                twinClassId, "DV_CLASS_" + twinClassId, userId, domainId);

        UUID nameI18nId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO i18n (id, i18n_type_id, domain_id) VALUES (?, 'dataListName', ?)", nameI18nId, domainId);
        dataListId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO data_list (id, domain_id, key, name_i18n_id) VALUES (?, ?, ?, ?)",
                dataListId, domainId, "dv_list", nameI18nId);

        applyBacklogMigrations();
        stubApiUser();
    }

    private void applyBacklogMigrations() {
        for (String location : BACKLOG_SCRIPTS) {
            String sql;
            try {
                sql = new ClassPathResource(location).getContentAsString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("cannot read " + location, e);
            }
            jdbcTemplate.execute(sql);
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Map<String, Object> currentVersion() {
        return jdbcTemplate.queryForMap(
                "SELECT dv.id, dv.status, dv.version, dv.\"order\", dv.hash" +
                "  FROM domain d JOIN domain_version dv ON dv.id = d.current_domain_version_id" +
                " WHERE d.id = ?", domainId);
    }

    private int versionCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM domain_version WHERE domain_id = ?", Integer.class, domainId);
        return count != null ? count : 0;
    }

    private Map<String, Object> twinClassRow() {
        return jdbcTemplate.queryForMap(
                "SELECT row_status, domain_version_id FROM twin_class WHERE id = ?", twinClassId);
    }

    private UUID release(String label, String hash, boolean first) {
        UUID window = jdbcTemplate.queryForObject(
                "SELECT domain_operation_release_start(?::uuid)", UUID.class, domainId);
        if (first) {
            jdbcTemplate.update(
                    "SELECT domain_operation_release_end(?::uuid, ?, ?::jsonb, ?, ?::jsonb, ?)",
                    domainId, label, "{\"snapshot\":\"" + label + "\"}", hash, "{\"delta\":\"full\"}", "DH_" + label);
        } else {
            jdbcTemplate.update(
                    "SELECT domain_operation_release_end(?::uuid, ?, ?::jsonb, ?, ?::jsonb, ?, ?::jsonb, ?, ?::jsonb, ?)",
                    domainId, label, "{\"snapshot\":\"" + label + "\"}", hash,
                    "{\"delta\":\"d\"}", "FWD_" + label, "{\"delta\":\"r\"}", "RWD_" + label);
        }
        return window;
    }

    private void editExternalId(String value) {
        jdbcTemplate.update("UPDATE twin_class SET external_id = ? WHERE id = ?", value, twinClassId);
    }

    private void assertForbidden(Runnable action, String messagePart) {
        DataAccessException ex = assertThrows(DataAccessException.class, action::run, messagePart);
        assertTrue(ex.getCause() != null && ex.getCause().getMessage() != null
                        && !ex.getCause().getMessage().contains("SMOKE FAIL"),
                "unexpected exception: " + ex);
    }

    // ------------------------------------------------------------------
    // 1 + 13. backfill + domain birth
    // ------------------------------------------------------------------

    @Test
    public void backfillStampsExistingRowsAndDomainBirthCreatesV1() {
        // existing rows are stamped with the backfill V1
        Map<String, Object> row = twinClassRow();
        assertEquals("ACTIVE", row.get("row_status"));
        assertEquals(currentVersion().get("id"), row.get("domain_version_id"));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM twin_class WHERE domain_id = ? AND domain_version_id IS NULL",
                Integer.class, domainId));

        // a domain created AFTER the migrations is born with V1 (deferrable FK + trigger)
        UUID newDomain = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO domain (id, key, domain_type_id, ancestor_twin_class_id, identity_provider_id," +
                "     permission_schema_id, twinflow_schema_id, twin_class_schema_id, domain_user_initiator_featurer_id)" +
                " VALUES (?, 'DV_BORN', 'basic', (SELECT id FROM twin_class WHERE domain_id IS NULL LIMIT 1)," +
                "     (SELECT id FROM identity_provider LIMIT 1), (SELECT id FROM permission_schema LIMIT 1)," +
                "     (SELECT id FROM twinflow_schema LIMIT 1), (SELECT id FROM twin_class_schema LIMIT 1), 3401)",
                newDomain);
        Map<String, Object> born = jdbcTemplate.queryForMap(
                "SELECT dv.status, dv.version FROM domain d" +
                "  JOIN domain_version dv ON dv.id = d.current_domain_version_id WHERE d.id = ?", newDomain);
        assertEquals("OPEN", born.get("status"));
        assertEquals("draft", born.get("version"));
    }

    // ------------------------------------------------------------------
    // 2. lazy lifecycle
    // ------------------------------------------------------------------

    @Test
    public void lazyLifecycleReleaseOpensNothingFirstEditOpensWindow() {
        UUID v1 = (UUID) currentVersion().get("id");
        editExternalId("edit1");
        assertEquals(v1, twinClassRow().get("domain_version_id"));

        release("1.0", "H1.0", true);
        Map<String, Object> after = currentVersion();
        assertEquals("RELEASED", after.get("status"));
        assertEquals("1.0", after.get("version"));
        assertEquals(1L, after.get("order"));
        assertEquals(v1, after.get("id"));           // current stays on the released version
        assertEquals(1, versionCount());             // no new version after a release

        editExternalId("edit2");                     // first edit opens the lazy window
        Map<String, Object> window = currentVersion();
        assertEquals("OPEN", window.get("status"));
        assertEquals("draft", window.get("version"));
        assertNotEquals(v1, window.get("id"));
        assertEquals(2, versionCount());
        UUID windowId = (UUID) window.get("id");

        editExternalId("edit3");                     // further edits reuse the same window
        assertEquals(windowId, twinClassRow().get("domain_version_id"));
        assertEquals(2, versionCount());
    }

    // ------------------------------------------------------------------
    // 3. two releases: chain, order, deltas required, cleanup
    // ------------------------------------------------------------------

    @Test
    public void twoReleasesBuildChainAndRequireDeltas() {
        release("1.0", "H1.0", true);
        editExternalId("e1");
        // non-first release without both deltas is rejected
        jdbcTemplate.update("SELECT domain_operation_release_start(?::uuid)", domainId);
        jdbcTemplate.update("SELECT domain_operation_abort(?::uuid)", domainId);
        assertForbidden(() -> jdbcTemplate.update(
                        "SELECT domain_operation_release_end(?::uuid, '1.1', ?::jsonb, 'H', NULL, NULL)",
                        domainId, "{\"s\":1}"),
                "non-first release must require forward and rollback deltas");
        release("1.1", "H1.1", false);

        Map<String, Object> v11 = currentVersion();
        assertEquals("RELEASED", v11.get("status"));
        assertEquals(2L, v11.get("order"));
        Map<String, Object> prev = jdbcTemplate.queryForMap(
                "SELECT version FROM domain_version WHERE id = " +
                "  (SELECT previous_domain_version_id FROM domain_version WHERE domain_id = ? AND version = '1.1')",
                domainId);
        assertEquals("1.0", prev.get("version"));
    }

    // ------------------------------------------------------------------
    // 4. deletions: soft delete, born-in-window, resurrection
    // ------------------------------------------------------------------

    @Test
    public void deletionsSoftBornInWindowAndResurrection() {
        release("1.0", "H1.0", true);

        // published row -> soft delete, stays in the DB stamped with the window
        jdbcTemplate.update("DELETE FROM twin_class WHERE id = ?", twinClassId);
        Map<String, Object> soft = twinClassRow();
        assertEquals("DELETED", soft.get("row_status"));
        assertEquals(currentVersion().get("id"), soft.get("domain_version_id"));

        // born-in-window row -> physical delete right away, only audit keeps the trace
        UUID fieldId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO twin_class_field (id, twin_class_id, key, field_typer_featurer_id," +
                "     twin_sorter_featurer_id, field_initializer_featurer_id) VALUES (?, ?, 'dv_field', 1317, 1317, 1317)",
                fieldId, twinClassId);
        jdbcTemplate.update("DELETE FROM twin_class_field WHERE id = ?", fieldId);
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM twin_class_field WHERE id = ?", Integer.class, fieldId));

        // resurrection with different content: window stamp
        jdbcTemplate.update(
                "INSERT INTO twin_class (id, key, twin_class_owner_type_id, created_by_user_id, domain_id, external_id)" +
                " VALUES (?, ?, 'system', ?, ?, 'resurrected')",
                twinClassId, "DV_CLASS_" + twinClassId, userId, domainId);
        Map<String, Object> resurrected = twinClassRow();
        assertEquals("ACTIVE", resurrected.get("row_status"));
        assertEquals(currentVersion().get("id"), resurrected.get("domain_version_id"));

        // resurrection with identical content: the old (1.0) stamp is restored
        jdbcTemplate.update("DELETE FROM twin_class WHERE id = ?", twinClassId);
        jdbcTemplate.update(
                "INSERT INTO twin_class (id, key, twin_class_owner_type_id, created_by_user_id, domain_id, external_id)" +
                " VALUES (?, ?, 'system', ?, ?, NULL)",
                twinClassId, "DV_CLASS_" + twinClassId, userId, domainId);
        assertEquals("ACTIVE", twinClassRow().get("row_status"));
        UUID releasedId = jdbcTemplate.queryForObject(
                "SELECT id FROM domain_version WHERE domain_id = ? AND version = '1.0'", UUID.class, domainId);
        assertEquals(releasedId, twinClassRow().get("domain_version_id"));
    }

    // ------------------------------------------------------------------
    // 5. no-op update
    // ------------------------------------------------------------------

    @Test
    public void noOpUpdateDoesNotStampAndDoesNotAudit() {
        UUID stampBefore = (UUID) twinClassRow().get("domain_version_id");
        jdbcTemplate.update("UPDATE twin_class SET external_id = external_id WHERE id = ?", twinClassId);
        assertEquals(stampBefore, twinClassRow().get("domain_version_id"));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM domain_version_audit WHERE domain_id = ?", Integer.class, domainId));
    }

    // ------------------------------------------------------------------
    // 6. gates: operation status, lock, incremental base
    // ------------------------------------------------------------------

    @Test
    public void gatesBlockEditsDuringOperationsAndLock() {
        jdbcTemplate.update("SELECT domain_operation_release_start(?::uuid)", domainId);
        assertForbidden(() -> editExternalId("blocked"),
                "edits must be blocked while RELEASING");
        jdbcTemplate.update("SELECT domain_operation_abort(?::uuid)", domainId);

        jdbcTemplate.update("UPDATE domain SET locked = true WHERE id = ?", domainId);
        assertForbidden(() -> editExternalId("locked_edit"),
                "edits must be blocked on a locked domain");
        // import is the only door into a locked domain
        jdbcTemplate.update("SELECT domain_operation_import_start(?::uuid, 'FULL')", domainId);
        jdbcTemplate.update("SELECT domain_operation_import_end(?::uuid, 'H_LOCK_IMPORT', NULL)", domainId);
        jdbcTemplate.update("UPDATE domain SET locked = false WHERE id = ?", domainId);
    }

    @Test
    public void incrementalImportRequiresReleasedCurrentAndMatchingBaseHash() {
        release("1.0", "H1.0", true);
        editExternalId("draft_edit"); // open window
        assertForbidden(() -> jdbcTemplate.update(
                        "SELECT domain_operation_import_start(?::uuid, 'INCREMENTAL', 'H1.0')", domainId),
                "incremental import must be rejected while a draft window is open");
        jdbcTemplate.update("SELECT domain_operation_rollback_start(?::uuid)", domainId);
        jdbcTemplate.update("SELECT domain_operation_rollback_end(?::uuid)", domainId);

        assertForbidden(() -> jdbcTemplate.update(
                        "SELECT domain_operation_import_start(?::uuid, 'INCREMENTAL', 'WRONG')", domainId),
                "incremental import must be rejected on a base hash mismatch");
        jdbcTemplate.update("SELECT domain_operation_abort(?::uuid)", domainId);

        jdbcTemplate.update("SELECT domain_operation_import_start(?::uuid, 'INCREMENTAL', 'H1.0')", domainId);
        jdbcTemplate.update("SELECT domain_operation_import_end(?::uuid, 'H_IMPORTED', NULL)", domainId);
        assertEquals("H_IMPORTED", currentVersion().get("hash"));
    }

    // ------------------------------------------------------------------
    // 7. race: two concurrent first edits open exactly one window
    // ------------------------------------------------------------------

    @Test
    public void concurrentFirstEditsOpenSingleWindow() throws Exception {
        release("1.0", "H1.0", true);
        UUID otherClassId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO twin_class (id, key, twin_class_owner_type_id, created_by_user_id, domain_id)" +
                " VALUES (?, ?, 'system', ?, ?)",
                otherClassId, "DV_CLASS2_" + otherClassId, userId, domainId);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Future<?> f1 = pool.submit(() -> {
            try {
                start.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            editExternalId("race1");
        });
        Future<?> f2 = pool.submit(() -> {
            try {
                start.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            jdbcTemplate.update("UPDATE twin_class SET external_id = 'race2' WHERE id = ?", otherClassId);
        });
        start.countDown();
        f1.get(5, TimeUnit.SECONDS);
        f2.get(5, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertEquals(2, versionCount()); // V1 released + exactly one open window
        Map<String, Object> window = currentVersion();
        assertEquals("OPEN", window.get("status"));
        assertEquals(window.get("id"), twinClassRow().get("domain_version_id"));
        assertEquals(window.get("id"), jdbcTemplate.queryForObject(
                "SELECT domain_version_id FROM twin_class WHERE id = ?", UUID.class, otherClassId));
    }

    // ------------------------------------------------------------------
    // 8. rollback restores images and collapses the empty window
    // ------------------------------------------------------------------

    @Test
    public void rollbackRestoresBeforeImagesAndCollapsesEmptyWindow() {
        String externalAtStart = jdbcTemplate.queryForObject(
                "SELECT external_id FROM twin_class WHERE id = ?", String.class, twinClassId);
        release("1.0", "H1.0", true);
        UUID releasedId = (UUID) currentVersion().get("id");

        editExternalId("draft");
        jdbcTemplate.update("DELETE FROM twin_class WHERE id = ?", twinClassId); // soft delete
        assertEquals(2, versionCount());

        jdbcTemplate.update("SELECT domain_operation_rollback_start(?::uuid)", domainId);
        // "java" applies the before-image of the first record of the window
        jdbcTemplate.update("SELECT set_config('twins.import_mode', 'on', true)");
        jdbcTemplate.update(
                "UPDATE twin_class SET external_id = ?, row_status = 'ACTIVE', domain_version_id = ? WHERE id = ?",
                externalAtStart, releasedId, twinClassId);
        jdbcTemplate.update("SELECT set_config('twins.import_mode', '', true)");
        jdbcTemplate.update("SELECT domain_operation_rollback_end(?::uuid)", domainId);

        assertEquals(1, versionCount());                            // window collapsed
        assertEquals("RELEASED", currentVersion().get("status"));   // back on 1.0
        assertEquals("ACTIVE", twinClassRow().get("row_status"));
        assertEquals(releasedId, twinClassRow().get("domain_version_id"));
    }

    // ------------------------------------------------------------------
    // 9. audit: stages, immutability
    // ------------------------------------------------------------------

    @Test
    public void auditRecordsAllPathsWithStageTagsAndIsImmutable() {
        editExternalId("draft_edit");                                   // IDLE edit
        jdbcTemplate.update("DELETE FROM twin_class WHERE id = ?", twinClassId); // IDLE soft delete
        release("1.0", "H1.0", true);                                   // RELEASING cleanup of the DELETED row

        // the edit carries the IDLE tag
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM domain_version_audit WHERE domain_id = ? AND operation = 'UPDATE'" +
                "     AND domain_operation_status = 'IDLE'", Integer.class, domainId));
        // the release cleanup physically removing the DELETED row carries the RELEASING tag
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM domain_version_audit WHERE domain_id = ? AND operation = 'DELETE'" +
                "     AND domain_operation_status = 'RELEASING' AND after IS NULL", Integer.class, domainId));

        assertForbidden(() -> jdbcTemplate.update(
                        "DELETE FROM domain_version_audit WHERE domain_id = ?", domainId),
                "audit history must be immutable");
        assertForbidden(() -> jdbcTemplate.update(
                        "INSERT INTO domain_version_audit (id, domain_id, domain_version_id, table_name, row_id," +
                        "     operation, domain_operation_status)" +
                        " VALUES (?, ?, ?, 'twin_class', ?, 'UPDATE', 'IDLE')",
                        UUID.randomUUID(), domainId, currentVersion().get("id"), twinClassId),
                "manual audit inserts must be forbidden");
    }

    // ------------------------------------------------------------------
    // 10. protection of the versioning columns
    // ------------------------------------------------------------------

    @Test
    public void protectionRejectsDirectManipulation() {
        assertForbidden(() -> jdbcTemplate.update(
                        "UPDATE twin_class SET domain_version_id = ? WHERE id = ?", UUID.randomUUID(), twinClassId),
                "direct stamp change must be forbidden");
        assertForbidden(() -> jdbcTemplate.update(
                        "UPDATE twin_class SET row_status = 'DELETED' WHERE id = ?", twinClassId),
                "direct DELETED must be forbidden");
        assertForbidden(() -> jdbcTemplate.update(
                        "INSERT INTO domain_version (id, domain_id, version) VALUES (?, ?, 'manual')",
                        UUID.randomUUID(), domainId),
                "manual domain_version writes must be forbidden");
        // ACTIVE <-> DISABLED is a legitimate business operation
        jdbcTemplate.update("UPDATE twin_class SET row_status = 'DISABLED' WHERE id = ?", twinClassId);
        assertEquals("DISABLED", twinClassRow().get("row_status"));
    }

    // ------------------------------------------------------------------
    // 11. FULL import with an open draft: window collapses, import version becomes current
    // ------------------------------------------------------------------

    @Test
    public void fullImportWithDraftWindowCollapsesItAndBecomesCurrent() {
        release("1.0", "H1.0", true);
        editExternalId("draft_that_will_be_wiped");

        jdbcTemplate.update("SELECT domain_operation_import_start(?::uuid, 'FULL')", domainId);
        // the importer reconciles: the draft edit is reverted (before-image style, import GUC)
        jdbcTemplate.update("SELECT set_config('twins.import_mode', 'on', true)");
        jdbcTemplate.update(
                "UPDATE twin_class SET external_id = NULL, domain_version_id = " +
                "  (SELECT id FROM domain_version WHERE domain_id = ? AND version = '1.0') WHERE id = ?",
                domainId, twinClassId);
        jdbcTemplate.update("SELECT set_config('twins.import_mode', '', true)");
        jdbcTemplate.update("SELECT domain_operation_import_end(?::uuid, 'H_FULL_IMPORT', 'restore of 1.0')", domainId);

        Map<String, Object> current = currentVersion();
        assertEquals("RELEASED", current.get("status"));
        assertEquals("restore of 1.0", current.get("version"));
        assertEquals("H_FULL_IMPORT", current.get("hash"));
        assertEquals(2, versionCount()); // 1.0 + import version, the window is gone
    }

    // ------------------------------------------------------------------
    // 12. reconcile: stray version outside the chain is detected and removable
    // ------------------------------------------------------------------

    @Test
    public void reconcileDetectsAndRemovesOrphanStamps() {
        UUID stray = UUID.randomUUID();
        jdbcTemplate.update("SELECT set_config('twins.version_op', 'on', true)");
        jdbcTemplate.update(
                "INSERT INTO domain_version (id, domain_id, version, status, \"order\") VALUES (?, ?, 'stray', 'RELEASED', 999)",
                stray, domainId);
        jdbcTemplate.update("SELECT set_config('twins.version_op', '', true)");
        jdbcTemplate.update("SELECT set_config('twins.import_mode', 'on', true)");
        jdbcTemplate.update("UPDATE twin_class SET domain_version_id = ? WHERE id = ?", stray, twinClassId);
        jdbcTemplate.update("SELECT set_config('twins.import_mode', '', true)");

        List<Map<String, Object>> report = jdbcTemplate.queryForList(
                "SELECT table_name FROM domain_version_reconcile(?::uuid, true)", domainId);
        assertEquals(1, report.size());
        assertEquals("twin_class", report.get(0).get("table_name"));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM twin_class WHERE id = ? AND domain_version_id = ?",
                Integer.class, twinClassId, stray), "dry run must remove nothing");

        jdbcTemplate.update("SELECT domain_version_reconcile(?::uuid, false)", domainId);
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM twin_class WHERE id = ?", Integer.class, twinClassId));
    }

    // ------------------------------------------------------------------
    // 13. child domain derivation + consistency
    // ------------------------------------------------------------------

    @Test
    public void childDomainDerivedFromParentAndForeignDomainRejected() {
        UUID fieldId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO twin_class_field (id, twin_class_id, key, field_typer_featurer_id," +
                "     twin_sorter_featurer_id, field_initializer_featurer_id) VALUES (?, ?, 'dv_f', 1317, 1317, 1317)",
                fieldId, twinClassId);
        Map<String, Object> field = jdbcTemplate.queryForMap(
                "SELECT domain_id, domain_version_id, row_status FROM twin_class_field WHERE id = ?", fieldId);
        assertEquals(domainId, field.get("domain_id"));                    // derived from the parent
        assertEquals(currentVersion().get("id"), field.get("domain_version_id")); // stamped

        UUID foreignDomain = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO domain (id, key, domain_type_id, ancestor_twin_class_id, identity_provider_id," +
                "     permission_schema_id, twinflow_schema_id, twin_class_schema_id, domain_user_initiator_featurer_id)" +
                " VALUES (?, 'DV_FOREIGN', 'basic', (SELECT id FROM twin_class WHERE domain_id IS NULL LIMIT 1)," +
                "     (SELECT id FROM identity_provider LIMIT 1), (SELECT id FROM permission_schema LIMIT 1)," +
                "     (SELECT id FROM twinflow_schema LIMIT 1), (SELECT id FROM twin_class_schema LIMIT 1), 3401)",
                foreignDomain);
        assertForbidden(() -> jdbcTemplate.update(
                        "INSERT INTO twin_class_field (id, twin_class_id, key, domain_id," +
                        "     field_typer_featurer_id, twin_sorter_featurer_id, field_initializer_featurer_id)" +
                        " VALUES (?, ?, 'dv_f2', ?, 1317, 1317, 1317)",
                        UUID.randomUUID(), twinClassId, foreignDomain),
                "a child row of a foreign domain must be rejected");
    }
}
