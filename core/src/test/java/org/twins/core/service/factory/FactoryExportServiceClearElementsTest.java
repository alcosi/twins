package org.twins.core.service.factory;

import org.cambium.common.StringList;
import org.cambium.common.sql.SqlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.service.EntityExportService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * Isolated unit tests for the clearElements logic of {@link FactoryExportService}: the FK-safe
 * DELETE ordering, the twin_factory_condition_set skip rule (only cleared when all its RESTRICT
 * referencers are in clear scope), and selective clearing.
 * <p>
 * The private {@code appendClearElementsSql} is invoked directly via reflection; {@link SqlBuilder}
 * is mocked to emit a marker per entity class so ordering can be asserted by substring position.
 * Constructor dependencies (the per-element export services) are unused by the method under test and
 * left as plain mocks.
 */
@ExtendWith(MockitoExtension.class)
class FactoryExportServiceClearElementsTest {

    private static final UUID FACTORY_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    private static final String M_PIPELINE      = "DELETE FROM TwinFactoryPipelineEntity";
    private static final String M_BRANCH        = "DELETE FROM TwinFactoryBranchEntity";
    private static final String M_ERASER        = "DELETE FROM TwinFactoryEraserEntity";
    private static final String M_MULTIPLIER    = "DELETE FROM TwinFactoryMultiplierEntity";
    private static final String M_TRIGGER       = "DELETE FROM TwinFactoryTriggerEntity";
    private static final String M_CONDITION_SET = "DELETE FROM TwinFactoryConditionSetEntity";

    @Mock private FactoryService factoryService;
    @Mock private FactoryBranchExportService branchExportService;
    @Mock private FactoryMultiplierExportService multiplierExportService;
    @Mock private FactoryPipelineExportService pipelineExportService;
    @Mock private FactoryEraserExportService eraserExportService;
    @Mock private FactoryTriggerExportService triggerExportService;
    @Mock private FactoryConditionSetExportService factoryConditionSetExportService;
    @Mock private FactoryConditionSetService factoryConditionSetService;
    @Mock private SqlBuilder sqlBuilder;

    private FactoryExportService service;
    private Method appendClearElementsSql;

    @BeforeEach
    void setUp() throws Exception {
        service = new FactoryExportService(
                factoryService, branchExportService, multiplierExportService,
                pipelineExportService, eraserExportService, triggerExportService,
                factoryConditionSetExportService, factoryConditionSetService);

        // EntityExportService.sqlBuilder is field-injected (@Autowired); set it manually for the test.
        Field sqlBuilderField = EntityExportService.class.getDeclaredField("sqlBuilder");
        sqlBuilderField.setAccessible(true);
        sqlBuilderField.set(service, sqlBuilder);

        appendClearElementsSql = FactoryExportService.class.getDeclaredMethod(
                "appendClearElementsSql",
                StringList.class, Collection.class,
                boolean.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class);
        appendClearElementsSql.setAccessible(true);

        // Marker per entity class so DELETE order is assertable by substring position.
        lenient().when(sqlBuilder.buildDeleteByColumn(any(), any(), any()))
                .thenAnswer(inv -> "DELETE FROM " + ((Class<?>) inv.getArgument(0)).getSimpleName());
    }

    @Test
    void allIncludes_emitsSixDeletesInFkSafeOrder() throws Exception {
        String sql = run(true, true, true, true, true, true);

        assertTrue(sql.contains("-- clearElements: factories = "), "header missing; got:\n" + sql);
        assertTrue(sql.contains(FACTORY_ID.toString()), "factory id missing in header; got:\n" + sql);

        // FK-safe order: pipeline -> branch -> eraser -> multiplier -> trigger -> condition_set.
        assertBefore(sql, M_PIPELINE, M_BRANCH);
        assertBefore(sql, M_BRANCH, M_ERASER);
        assertBefore(sql, M_ERASER, M_MULTIPLIER);
        assertBefore(sql, M_MULTIPLIER, M_TRIGGER);
        assertBefore(sql, M_TRIGGER, M_CONDITION_SET);
        assertFalse(sql.contains("SKIPPED"), "no skip comment when everything is in scope; got:\n" + sql);
    }

    @Test
    void conditionSetSkipped_whenPipelineReferencerOutsideScope() throws Exception {
        // pipelines OFF but still a RESTRICT referencer of condition_set -> CS clear must be skipped.
        String sql = run(true, true, true, false, true, true);

        assertFalse(sql.contains(M_CONDITION_SET), "condition_set DELETE must be skipped; got:\n" + sql);
        assertFalse(sql.contains(M_PIPELINE), "pipeline DELETE must be absent (out of scope); got:\n" + sql);
        assertTrue(sql.contains("SKIPPED"), "skip SQL comment expected; got:\n" + sql);
        assertTrue(sql.contains("twin_factory_pipeline"),
                "skip comment must name the out-of-scope referencer; got:\n" + sql);
        // referencers that ARE in scope are still cleared
        assertTrue(sql.contains(M_BRANCH), "branch delete expected; got:\n" + sql);
        assertTrue(sql.contains(M_ERASER), "eraser delete expected; got:\n" + sql);
        assertTrue(sql.contains(M_MULTIPLIER), "multiplier delete expected; got:\n" + sql);
        assertTrue(sql.contains(M_TRIGGER), "trigger delete expected; got:\n" + sql);
    }

    @Test
    void conditionSetCleared_whenAllRestrictReferencersInScope() throws Exception {
        // trigger is NOT a RESTRICT referencer of condition_set, so it can be OFF while CS is cleared.
        String sql = run(true, true, true, true, true, false);

        assertTrue(sql.contains(M_CONDITION_SET),
                "condition_set must be cleared when pipeline/branch/eraser/multiplier are all in scope; got:\n" + sql);
        assertFalse(sql.contains(M_TRIGGER), "trigger was out of scope; got:\n" + sql);
        assertFalse(sql.contains("SKIPPED"), "no skip comment expected here; got:\n" + sql);
        assertBefore(sql, M_MULTIPLIER, M_CONDITION_SET); // CS still deleted last
    }

    @Test
    void selectiveIncludes_emitsOnlySelectedDelete() throws Exception {
        String sql = run(false, false, false, false, false, true);

        assertTrue(sql.contains(M_TRIGGER), "trigger delete expected; got:\n" + sql);
        assertFalse(sql.contains(M_PIPELINE), "got:\n" + sql);
        assertFalse(sql.contains(M_BRANCH), "got:\n" + sql);
        assertFalse(sql.contains(M_ERASER), "got:\n" + sql);
        assertFalse(sql.contains(M_MULTIPLIER), "got:\n" + sql);
        assertFalse(sql.contains(M_CONDITION_SET), "got:\n" + sql);
        assertFalse(sql.contains("SKIPPED"),
                "no CS skip comment expected when CS itself is out of scope; got:\n" + sql);
    }

    /**
     * @param cs    includeConditionSets
     * @param br    includeBranches
     * @param mu    includeMultipliers
     * @param pi    includePipelines
     * @param er    includeErasers
     * @param tr    includeTriggers
     */
    private String run(boolean cs, boolean br, boolean mu, boolean pi, boolean er, boolean tr) throws Exception {
        TwinFactoryEntity factory = new TwinFactoryEntity().setId(FACTORY_ID);
        StringList sqlParts = new StringList();
        appendClearElementsSql.invoke(service, sqlParts, List.of(factory), cs, br, mu, pi, er, tr);
        return String.join("\n", sqlParts);
    }

    private void assertBefore(String sql, String first, String second) {
        int i = sql.indexOf(first);
        int j = sql.indexOf(second);
        assertTrue(i >= 0 && j >= 0, "markers missing: [" + first + "] / [" + second + "]; got:\n" + sql);
        assertTrue(i < j, "[" + first + "] must precede [" + second + "]; got:\n" + sql);
    }
}
