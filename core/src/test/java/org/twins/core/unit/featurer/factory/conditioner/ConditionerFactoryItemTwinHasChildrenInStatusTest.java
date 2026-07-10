package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinHasChildrenInStatus;
import org.twins.core.service.twin.TwinSearchServiceV2;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerFactoryItemTwinHasChildrenInStatusTest extends BaseUnitTest {

    @Mock
    private TwinSearchServiceV2 twinSearchService;

    private ConditionerFactoryItemTwinHasChildrenInStatus conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerFactoryItemTwinHasChildrenInStatus();
        setField(conditioner, "twinSearchService", twinSearchService);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        var field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + name);
    }

    // statusIds param is a FeaturerParamUUIDSet -> CSV under "statusIds".
    private Properties props(Set<UUID> statuses, boolean excludeFactoryInput, Integer depth) {
        var p = new Properties();
        p.put("statusIds", String.join(",", statuses.stream().map(UUID::toString).toList()));
        p.put("excludeFactoryInput", Boolean.toString(excludeFactoryInput));
        if (depth != null) {
            p.put("depth", depth.toString());
        }
        return p;
    }

    private FactoryItem item(UUID headTwinId, FactoryContext ctx) {
        // TwinCreate.setTwinEntity does not trip isSketch; safe without twinStatus.
        var twin = new TwinEntity().setId(headTwinId);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output).setFactoryContext(ctx);
    }

    @Nested
    class Check {

        @Test
        void check_childrenExist_returnsTrue() throws ServiceException {
            // contract: count > 0 -> true.
            var headId = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            when(twinSearchService.exists(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(true);

            assertTrue(conditioner.check(props(statuses, false, null), item(headId, mock(FactoryContext.class))));
        }

        @Test
        void check_noChildren_returnsFalse() throws ServiceException {
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            when(twinSearchService.exists(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(false);

            assertFalse(conditioner.check(props(statuses, false, null),
                    item(UUID.randomUUID(), mock(FactoryContext.class))));
        }

        @Test
        void check_searchScopedToOutputTwinAsHead() throws ServiceException {
            // contract: "twin HAS CHILDREN in status" must scope the search to children of THIS output twin.
            // Scoping is done by adding the output twin id as the head twin id of the search.
            var outputTwinId = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            when(twinSearchService.exists(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(true);

            conditioner.check(props(statuses, false, 3), item(outputTwinId, mock(FactoryContext.class)));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).exists(captor.capture());
            var used = captor.getValue();
            assertNotNull(used.getHeadTwinIdList(), "search must be scoped to the output twin as head");
            assertTrue(used.getHeadTwinIdList().contains(outputTwinId),
                    "head twin id list must contain the output twin id");
        }

        @Test
        void check_excludeFactoryInput_appliesInputTwinIdsAsExclusion() throws Exception {
            // contract: when excludeFactoryInput=true, factory-input twin ids are added to twinIdExcludeList.
            var headId = UUID.randomUUID();
            var input1 = new TwinEntity().setId(UUID.randomUUID());
            var input2 = new TwinEntity().setId(UUID.randomUUID());
            var ctx = mock(FactoryContext.class);
            when(ctx.getInputTwinList()).thenReturn(java.util.Arrays.asList(input1, input2));

            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            when(twinSearchService.exists(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(false);

            conditioner.check(props(statuses, true, null), item(headId, ctx));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).exists(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getTwinIdExcludeList() != null
                    && used.getTwinIdExcludeList().contains(input1.getId())
                    && used.getTwinIdExcludeList().contains(input2.getId()));
        }

        @Test
        void check_statusIdsPropagatedToSearch() throws ServiceException {
            var status1 = UUID.randomUUID();
            var status2 = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(status1);
            statuses.add(status2);
            when(twinSearchService.exists(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(false);

            conditioner.check(props(statuses, false, null),
                    item(UUID.randomUUID(), mock(FactoryContext.class)));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).exists(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getStatusIdList() != null
                    && used.getStatusIdList().contains(status1)
                    && used.getStatusIdList().contains(status2));
        }

        @Test
        void check_emptyStatusSet_doesNotAddStatusFilter() throws ServiceException {
            // contract: with no configured statuses, statusIdList should stay empty (count by children only).
            when(twinSearchService.exists(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(false);

            conditioner.check(props(new LinkedHashSet<>(), false, null),
                    item(UUID.randomUUID(), mock(FactoryContext.class)));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).exists(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getStatusIdList() == null || used.getStatusIdList().isEmpty());
        }
    }
}
