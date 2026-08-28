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
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinHasChildrenOfClasses;
import org.twins.core.service.twin.TwinSearchService;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerFactoryItemTwinHasChildrenOfClassesTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private ConditionerFactoryItemTwinHasChildrenOfClasses conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerFactoryItemTwinHasChildrenOfClasses();
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

    private Properties props(Set<UUID> classIds, boolean excludeFactoryInput) {
        var p = new Properties();
        p.put("classIds", String.join(",", classIds.stream().map(UUID::toString).toList()));
        p.put("excludeFactoryInput", Boolean.toString(excludeFactoryInput));
        return p;
    }

    private FactoryItem item(UUID headTwinId, FactoryContext ctx) {
        var twin = new TwinEntity().setId(headTwinId);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output).setFactoryContext(ctx);
    }

    @Nested
    class Check {

        @Test
        void check_childrenExist_returnsTrue() throws ServiceException {
            var classIds = new LinkedHashSet<UUID>();
            classIds.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(3L);

            assertTrue(conditioner.check(props(classIds, false),
                    item(UUID.randomUUID(), mock(FactoryContext.class))));
        }

        @Test
        void check_noChildren_returnsFalse() throws ServiceException {
            var classIds = new LinkedHashSet<UUID>();
            classIds.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            assertFalse(conditioner.check(props(classIds, false),
                    item(UUID.randomUUID(), mock(FactoryContext.class))));
        }

        @Test
        void check_searchScopedToOutputTwinAsHead() throws ServiceException {
            // contract: children are scoped by head twin = the output twin.
            var headId = UUID.randomUUID();
            var classIds = new LinkedHashSet<UUID>();
            classIds.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            conditioner.check(props(classIds, false), item(headId, mock(FactoryContext.class)));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).count(captor.capture());
            assertTrue(captor.getValue().getHeadTwinIdList() != null
                    && captor.getValue().getHeadTwinIdList().contains(headId));
        }

        @Test
        void check_classIdsPropagatedToExtendsHierarchy() throws ServiceException {
            var c1 = UUID.randomUUID();
            var classIds = new LinkedHashSet<UUID>();
            classIds.add(c1);
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            conditioner.check(props(classIds, false),
                    item(UUID.randomUUID(), mock(FactoryContext.class)));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).count(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getTwinClassExtendsHierarchyContainsIdList() != null
                    && used.getTwinClassExtendsHierarchyContainsIdList().contains(c1));
        }

        @Test
        void check_excludeFactoryInput_appliesInputTwinIdsAsExclusion() throws Exception {
            var headId = UUID.randomUUID();
            var input = new TwinEntity().setId(UUID.randomUUID());
            var ctx = mock(FactoryContext.class);
            when(ctx.getInputTwinList()).thenReturn(java.util.Collections.singletonList(input));

            var classIds = new LinkedHashSet<UUID>();
            classIds.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            conditioner.check(props(classIds, true), item(headId, ctx));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).count(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getTwinIdExcludeList() != null
                    && used.getTwinIdExcludeList().contains(input.getId()));
        }

        @Test
        void check_excludeFactoryInputFalse_doesNotSetExclusionList() throws ServiceException {
            var classIds = new LinkedHashSet<UUID>();
            classIds.add(UUID.randomUUID());
            when(twinSearchService.count(org.mockito.ArgumentMatchers.any(BasicSearch.class)))
                    .thenReturn(0L);

            conditioner.check(props(classIds, false),
                    item(UUID.randomUUID(), mock(FactoryContext.class)));

            var captor = ArgumentCaptor.forClass(BasicSearch.class);
            org.mockito.Mockito.verify(twinSearchService).count(captor.capture());
            var used = captor.getValue();
            assertTrue(used.getTwinIdExcludeList() == null || used.getTwinIdExcludeList().isEmpty());
        }
    }
}
