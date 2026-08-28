package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldAsFoundTwinOfClass;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldAsFoundTwinOfClassTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    @Mock
    private TwinService twinService;

    private FillerFieldAsFoundTwinOfClass filler;

    private static final UUID LINK_FIELD_ID = UUID.randomUUID();
    private static final UUID TWIN_CLASS_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldAsFoundTwinOfClass();
        inject(filler, "twinSearchService", twinSearchService);
        inject(filler, "twinService", twinService);
    }

    private void inject(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("field not found: " + name);
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("twinClassFieldLinkId", LINK_FIELD_ID.toString());
        p.setProperty("twinClassId", TWIN_CLASS_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Fill {

        @Test
        void fill_exactlyOneTwinFound_writesItsIdAsLinkFieldValue() throws ServiceException {
            // NAME promises: fill LINK field with the id of the single FOUND TWIN OF CLASS.
            var foundTwinId = UUID.randomUUID();
            var found = new TwinEntity().setId(foundTwinId);
            var factoryItem = buildFactoryItem();
            when(twinSearchService.findTwins(any(BasicSearch.class))).thenReturn(List.of(found));
            var createdLink = new FieldValueLink(new TwinClassFieldEntity().setId(LINK_FIELD_ID));
            when(twinService.createFieldValue(LINK_FIELD_ID, foundTwinId.toString())).thenReturn(createdLink);

            filler.fill(props(), factoryItem, null);

            assertSame(createdLink, factoryItem.getOutput().getField(LINK_FIELD_ID));
        }

        @Test
        void fill_noTwinFound_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(twinSearchService.findTwins(any(BasicSearch.class))).thenReturn(Collections.emptyList());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_moreThanOneTwinFound_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(twinSearchService.findTwins(any(BasicSearch.class))).thenReturn(List.of(
                    new TwinEntity().setId(UUID.randomUUID()),
                    new TwinEntity().setId(UUID.randomUUID())));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }
    }
}
