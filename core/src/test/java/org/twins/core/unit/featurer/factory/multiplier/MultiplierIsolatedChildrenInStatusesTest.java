package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedChildrenInStatuses;
import org.twins.core.service.twin.TwinSearchService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedChildrenInStatusesTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private MultiplierIsolatedChildrenInStatuses multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedChildrenInStatuses();
        setField(multiplier, "twinSearchService", twinSearchService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private Properties buildProperties(String statusIdsCsv, boolean exclude) {
        var props = new Properties();
        if (statusIdsCsv != null) {
            props.put("statusIds", statusIdsCsv);
        }
        props.put("exclude", Boolean.toString(exclude));
        return props;
    }

    private FactoryItem buildInputItem(UUID headTwinId) {
        var twin = new TwinEntity()
                .setId(headTwinId)
                .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var twinUpdate = new TwinUpdate().setDbTwinEntity(twin);
        return new FactoryItem().setOutput(twinUpdate);
    }

    // relatives are cloned along the multiply path; clone() triggers isSketch() which reads
    // twinStatus.getType(), so every relative twin must carry a non-null status.
    private TwinEntity buildRelative(UUID headTwinId) {
        var relative = new TwinEntity();
        relative.setId(UUID.randomUUID());
        relative.setHeadTwinId(headTwinId);
        relative.setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        return relative;
    }

    @Nested
    class Multiply {

        @Test
        void multiply_childrenByHead_producesOneOutputPerChildPerInput() throws ServiceException {
            // contract: "Output list of twin relatives for each input. Output twin will be loaded
            // by head and filtered by statusIds" — count == total relatives across inputs.
            var statusId = UUID.randomUUID();
            var inputHead1 = UUID.randomUUID();
            var inputHead2 = UUID.randomUUID();
            var props = buildProperties(statusId.toString(), false);

            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            var rel1 = buildRelative(inputHead1);
            var rel2 = buildRelative(inputHead1);
            var rel3 = buildRelative(inputHead2);

            when(twinSearchService.findTwins(any())).thenReturn(List.of(rel1, rel2, rel3));

            var input = List.of(buildInputItem(inputHead1), buildInputItem(inputHead2));

            var result = multiplier.multiply(props, input, ctx);

            assertEquals(3, result.size());

            var out0 = (TwinUpdate) result.get(0).getOutput();
            assertSame(rel1, out0.getDbTwinEntity());
            // each output item is wired to exactly its single source input (isolated scope)
            assertEquals(1, result.get(0).getContextFactoryItemList().size());
            assertSame(input.get(0), result.get(0).getContextFactoryItemList().get(0));

            var out2 = (TwinUpdate) result.get(2).getOutput();
            assertSame(rel3, out2.getDbTwinEntity());
            assertSame(input.get(1), result.get(2).getContextFactoryItemList().get(0));
        }

        @Test
        void multiply_inputWithoutRelatives_skipped() throws ServiceException {
            var statusId = UUID.randomUUID();
            var head1 = UUID.randomUUID();
            var head2NoChildren = UUID.randomUUID();
            var props = buildProperties(statusId.toString(), false);

            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            var rel1 = buildRelative(head1);
            when(twinSearchService.findTwins(any())).thenReturn(List.of(rel1));

            var input = List.of(buildInputItem(head1), buildInputItem(head2NoChildren));

            var result = multiplier.multiply(props, input, ctx);

            // only head1 contributed; the second input had no relatives and was skipped
            assertEquals(1, result.size());
            assertSame(rel1, ((TwinUpdate) result.get(0).getOutput()).getDbTwinEntity());
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var statusId = UUID.randomUUID();
            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            when(twinSearchService.findTwins(any())).thenReturn(List.of());

            var result = multiplier.multiply(
                    buildProperties(statusId.toString(), false),
                    List.of(),
                    ctx);

            assertTrue(result.isEmpty());
        }

        @Test
        void multiply_statusFilterAppliedToSearch() throws ServiceException {
            // The statusIds param must propagate to the BasicSearch used to find relatives.
            var status1 = UUID.randomUUID();
            var status2 = UUID.randomUUID();
            var head1 = UUID.randomUUID();
            var props = buildProperties(status1 + "," + status2, false);

            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            var rel = buildRelative(head1);
            when(twinSearchService.findTwins(any())).thenReturn(List.of(rel));

            multiplier.multiply(props, List.of(buildInputItem(head1)), ctx);

            var captor = org.mockito.ArgumentCaptor.forClass(BasicSearch.class);
            verify(twinSearchService).findTwins(captor.capture());
            // both status ids flowed into the include-side of the search
            assertNotNull(captor.getValue().getStatusIdList());
            assertTrue(captor.getValue().getStatusIdList().contains(status1));
            assertTrue(captor.getValue().getStatusIdList().contains(status2));
            // headTwinIdList must contain the input twin id
            assertTrue(captor.getValue().getHeadTwinIdList().contains(head1));
            // view-permission check is intentionally disabled by this multiplier
            assertFalse(captor.getValue().isCheckViewPermission());
        }
    }
}
