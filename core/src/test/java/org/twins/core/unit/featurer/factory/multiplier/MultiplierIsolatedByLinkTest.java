package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedByLink;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedByLinkTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private LinkService linkService;

    @Mock
    private TwinSearchService twinSearchService;

    private MultiplierIsolatedByLink multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedByLink();
        setField(multiplier, "twinService", twinService);
        setField(multiplier, "linkService", linkService);
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

    private Properties buildProperties(UUID linkId) {
        var props = new Properties();
        props.put("linkId", linkId.toString());
        // statusIds / excludeStatuses left unset -> empty set / false
        return props;
    }

    private FactoryItem buildInputItem(TwinEntity twin) {
        var twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twin);
        return new FactoryItem().setOutput(twinCreate);
    }

    // relative twins are cloned along the multiply path; clone triggers isSketch() which reads
    // twinStatus.getType(), so every twin under test must carry a non-null status.
    private TwinEntity buildRelative() {
        var relative = new TwinEntity();
        relative.setId(UUID.randomUUID());
        relative.setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        return relative;
    }

    @Nested
    class Multiply {

        @Test
        void multiply_withLinkAndRelatives_producesOneOutputPerRelative() throws ServiceException {
            var linkId = UUID.randomUUID();
            var props = buildProperties(linkId);
            var input = new TwinEntity();
            input.setId(UUID.randomUUID());

            var link = new LinkEntity();
            link.setId(linkId);
            when(linkService.findEntitySafe(linkId)).thenReturn(link);

            var relative1 = buildRelative();
            var relative2 = buildRelative();
            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of(relative1, relative2));

            var result = multiplier.multiply(
                    props,
                    List.of(buildInputItem(input)),
                    mock(FactoryContext.class));

            // link MUST be loaded (lazy lookup) before it is used
            verify(linkService).findEntitySafe(linkId);
            assertEquals(2, result.size());
            var out0 = (TwinUpdate) result.get(0).getOutput();
            assertSame(relative1, out0.getDbTwinEntity());
            var out1 = (TwinUpdate) result.get(1).getOutput();
            assertSame(relative2, out1.getDbTwinEntity());
        }

        @Test
        void multiply_nullLink_inputSkipped() throws ServiceException {
            var linkId = UUID.randomUUID();
            var props = buildProperties(linkId);
            var input = new TwinEntity();
            input.setId(UUID.randomUUID());

            when(linkService.findEntitySafe(linkId)).thenReturn(null);

            var result = multiplier.multiply(
                    props,
                    List.of(buildInputItem(input)),
                    mock(FactoryContext.class));

            assertTrue(result.isEmpty());
            verify(twinSearchService, never()).findTwins(any(BasicSearch.class));
        }

        @Test
        void multiply_emptyRelatives_inputSkipped() throws ServiceException {
            var linkId = UUID.randomUUID();
            var props = buildProperties(linkId);
            var input = new TwinEntity();
            input.setId(UUID.randomUUID());

            var link = new LinkEntity();
            link.setId(linkId);
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            when(twinSearchService.findTwins(any(BasicSearch.class)))
                    .thenReturn(List.of());

            var result = multiplier.multiply(
                    props,
                    List.of(buildInputItem(input)),
                    mock(FactoryContext.class));

            assertTrue(result.isEmpty());
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var result = multiplier.multiply(buildProperties(UUID.randomUUID()), List.of(), mock(FactoryContext.class));

            assertTrue(result.isEmpty());
        }
    }
}
