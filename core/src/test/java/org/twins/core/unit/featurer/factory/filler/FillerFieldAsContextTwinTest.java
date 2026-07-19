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
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldAsContextTwin;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldAsContextTwinTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FillerFieldAsContextTwin filler;

    private static final UUID LINK_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldAsContextTwin();
        inject(filler, "twinService", twinService);
        inject(filler, "twinClassFieldService", twinClassFieldService);
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
        return p;
    }

    private FactoryItem buildFactoryItem(TwinEntity contextTwin) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(contextTwin);
        var contextItem = new FactoryItem().setOutput(contextOutput);
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    @Nested
    class Fill {

        @Test
        void fill_writesSingleContextTwinIdAsLinkFieldValue() throws ServiceException {
            // NAME promises: fill LINK field on output with the SINGLE CONTEXT TWIN's id.
            var contextTwinId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(contextTwinId);
            var factoryItem = buildFactoryItem(contextTwin);
            var linkFieldEntity = new TwinClassFieldEntity().setId(LINK_FIELD_ID);
            var createdLink = new FieldValueLink(linkFieldEntity);
            when(twinClassFieldService.findEntitySafe(LINK_FIELD_ID)).thenReturn(linkFieldEntity);
            when(twinService.createFieldValue(linkFieldEntity, contextTwinId.toString())).thenReturn(createdLink);

            filler.fill(props(), List.of(factoryItem), null, false);

            assertSame(createdLink, factoryItem.getOutput().getField(LINK_FIELD_ID));
            verify(twinService).createFieldValue(linkFieldEntity, contextTwinId.toString());
        }

        @Test
        void fill_noContext_throwsStepError() {
            // NAME promises: requires a single context twin.
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var factoryItem = new FactoryItem().setOutput(output).setContextFactoryItemList(Collections.emptyList());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_multipleContexts_throwsStepError() {
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var c1Output = new TwinCreate();
            c1Output.setTwinEntity(new TwinEntity());
            var c1 = new FactoryItem().setOutput(c1Output);
            var c2Output = new TwinCreate();
            c2Output.setTwinEntity(new TwinEntity());
            var c2 = new FactoryItem().setOutput(c2Output);
            var factoryItem = new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(c1, c2));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }
    }
}
