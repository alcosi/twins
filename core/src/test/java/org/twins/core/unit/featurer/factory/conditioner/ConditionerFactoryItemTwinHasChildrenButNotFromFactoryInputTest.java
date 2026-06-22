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
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinHasChildrenButNotFromFactoryInput;
import org.twins.core.service.twin.TwinSearchService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class ConditionerFactoryItemTwinHasChildrenButNotFromFactoryInputTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private ConditionerFactoryItemTwinHasChildrenButNotFromFactoryInput conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerFactoryItemTwinHasChildrenButNotFromFactoryInput();
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

    private Properties props(Set<UUID> statuses) {
        var p = new Properties();
        p.put("statusIds", String.join(",", statuses.stream().map(UUID::toString).toList()));
        return p;
    }

    private FactoryItem item(UUID headTwinId, TwinEntity... inputTwins) {
        var twin = new TwinEntity().setId(headTwinId);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        var ctx = mock(FactoryContext.class);
        when(ctx.getInputTwinList()).thenReturn(Arrays.asList(inputTwins));
        return new FactoryItem().setOutput(output).setFactoryContext(ctx);
    }

    @Nested
    class Check {

        @Test
        void check_childrenOutsideInputExist_returnsTrue() throws ServiceException {
            // contract: count of children (in given statuses, excluding input twins) > 0 -> true.
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(2L);

            assertTrue(conditioner.check(props(statuses),
                    item(UUID.randomUUID(), new TwinEntity().setId(UUID.randomUUID()))));
        }

        @Test
        void check_noChildrenOutsideInput_returnsFalse() throws ServiceException {
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            assertFalse(conditioner.check(props(statuses),
                    item(UUID.randomUUID(), new TwinEntity().setId(UUID.randomUUID()))));
        }

        @Test
        void check_searchScopedToOutputTwinAsHead() throws Exception {
            var headId = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            conditioner.check(props(statuses), item(headId, new TwinEntity().setId(UUID.randomUUID())));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).count(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getHeadTwinIdList() != null && used.getHeadTwinIdList().contains(headId));
        }

        @Test
        void check_inputTwinIdsAlwaysExcluded() throws Exception {
            // contract (per name "...but NOT from factory input"): input twins are excluded regardless.
            var input1 = new TwinEntity().setId(UUID.randomUUID());
            var input2 = new TwinEntity().setId(UUID.randomUUID());
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            conditioner.check(props(statuses), item(UUID.randomUUID(), input1, input2));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).count(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getTwinIdExcludeList() != null
                    && used.getTwinIdExcludeList().contains(input1.getId())
                    && used.getTwinIdExcludeList().contains(input2.getId()));
        }

        @Test
        void check_statusIdsPropagatedToSearch() throws ServiceException {
            var s1 = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(s1);
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            conditioner.check(props(statuses),
                    item(UUID.randomUUID(), new TwinEntity().setId(UUID.randomUUID())));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).count(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getStatusIdList() != null && used.getStatusIdList().contains(s1));
        }
    }
}
