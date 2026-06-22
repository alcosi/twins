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
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedRelativesByHead;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedRelativesByHeadTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinSearchService twinSearchService;

    private MultiplierIsolatedRelativesByHead multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedRelativesByHead();
        setField(multiplier, "twinService", twinService);
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

    private FactoryItem buildInputItem(TwinEntity twin) {
        var twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twin);
        return new FactoryItem().setOutput(twinCreate);
    }

    @Nested
    class Multiply {

        @Test
        void multiply_excludesInputTwinFromRelatives() throws ServiceException {
            var head = new TwinEntity();
            head.setId(UUID.randomUUID());
            var input = new TwinEntity();
            input.setId(UUID.randomUUID());
            input.setTwinClassId(UUID.randomUUID());
            input.setHeadTwinId(head.getId());

            // a relative that shares the head, plus the input twin itself in the search result.
            // the relative is cloned along the multiply path; clone triggers isSketch() which reads
            // twinStatus.getType(), so the relative must carry a non-null status.
            var relative = new TwinEntity();
            relative.setId(UUID.randomUUID());
            relative.setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));

            doAnswer(inv -> {
                ((TwinEntity) inv.getArgument(0)).setHeadTwin(head);
                return head;
            }).when(twinService).loadHeadForTwin(any(TwinEntity.class));
            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of(input, relative));

            var result = multiplier.multiply(
                    new Properties(),
                    List.of(buildInputItem(input)),
                    mock(FactoryContext.class));

            // head loaded before read
            verify(twinService).loadHeadForTwin(input);
            // input twin must be filtered out; only the true relative remains
            assertEquals(1, result.size());
            var out = (TwinUpdate) result.get(0).getOutput();
            assertSame(relative, out.getDbTwinEntity());
        }

        @Test
        void multiply_noHead_inputSkipped() throws ServiceException {
            var input = new TwinEntity();
            input.setId(UUID.randomUUID());
            input.setTwinClassId(UUID.randomUUID());

            // loadHeadForTwin returns a (non-void) TwinEntity; stubbing it to leave head unset
            // (returns null, no head wired onto the input) makes the multiplier skip this input.
            when(twinService.loadHeadForTwin(any(TwinEntity.class))).thenReturn(null);

            var result = multiplier.multiply(
                    new Properties(),
                    List.of(buildInputItem(input)),
                    mock(FactoryContext.class));

            assertTrue(result.isEmpty());
            verify(twinSearchService, never()).findTwins(any(BasicSearch.class));
        }

        @Test
        void multiply_emptyRelatives_inputSkipped() throws ServiceException {
            var head = new TwinEntity();
            head.setId(UUID.randomUUID());
            var input = new TwinEntity();
            input.setId(UUID.randomUUID());
            input.setTwinClassId(UUID.randomUUID());

            doAnswer(inv -> {
                ((TwinEntity) inv.getArgument(0)).setHeadTwin(head);
                return head;
            }).when(twinService).loadHeadForTwin(any(TwinEntity.class));
            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of());

            var result = multiplier.multiply(
                    new Properties(),
                    List.of(buildInputItem(input)),
                    mock(FactoryContext.class));

            assertTrue(result.isEmpty());
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var result = multiplier.multiply(new Properties(), List.of(), mock(FactoryContext.class));

            assertTrue(result.isEmpty());
            verify(twinService, never()).loadHeadForTwin(any(TwinEntity.class));
        }
    }
}
