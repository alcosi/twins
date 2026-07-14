package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinFieldTwinAssignee;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinDbFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FillerBasicsAssigneeFromContextTwinFieldTwinAssigneeTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextTwinDbFields lookuper;

    @Mock
    private TwinService twinService;

    private FillerBasicsAssigneeFromContextTwinFieldTwinAssignee filler;

    private static final UUID LINK_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBasicsAssigneeFromContextTwinFieldTwinAssignee();
        inject(filler, "fieldLookupers", fieldLookupers);
        inject(filler, "twinService", twinService);
        when(fieldLookupers.getFromContextTwinDbFields()).thenReturn(lookuper);
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
        p.setProperty("linkField", LINK_FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(new TwinEntity());
        var contextItem = new FactoryItem().setOutput(contextOutput);
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    private TwinClassFieldEntity buildField() {
        var field = new TwinClassFieldEntity();
        field.setId(UUID.randomUUID());
        field.setKey("link");
        return field;
    }

    @Nested
    class Fill {

        @Test
        void fill_usesContextTwinDbFieldsLookuper_thenLinkedTwinAssignee() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var dstTwinId = UUID.randomUUID();
            var link = new TwinLinkEntity().setDstTwinId(dstTwinId);
            var fieldValue = new FieldValueLink(buildField()).add(link);
            when(lookuper.lookupFieldValue(factoryItem, LINK_FIELD_ID)).thenReturn(fieldValue);

            var assignee = new UserEntity().setId(UUID.randomUUID());
            when(twinService.getTwinAssignee(dstTwinId)).thenReturn(assignee);

            filler.fill(props(), List.of(factoryItem), null, false);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: field resolved from the CONTEXT TWIN (db fields), assignee from linked twin.
            assertSame(assignee, outputTwin.getAssignerUser());
            assertEquals(assignee.getId(), outputTwin.getAssignerUserId());
            verify(lookuper).lookupFieldValue(factoryItem, LINK_FIELD_ID);
        }
    }
}
