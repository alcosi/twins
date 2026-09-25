package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextFieldTwinAssignee;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FillerBasicsAssigneeFromContextFieldTwinAssigneeTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFields lookuper;

    @Mock
    private TwinService twinService;

    private FillerBasicsAssigneeFromContextFieldTwinAssignee filler;

    private static final UUID LINK_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBasicsAssigneeFromContextFieldTwinAssignee();
        inject(filler, "fieldLookupers", fieldLookupers);
        inject(filler, "twinService", twinService);
        // the merged filler resolves its link-field source from the fieldLookuper param — the
        // default fromContextFields covers the former raw context-fields read
        when(fieldLookupers.getByType(FieldLookupers.Type.fromContextFields)).thenReturn(lookuper);
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
        p.setProperty("fieldLookuper", "fromContextFields");
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity buildField() {
        var field = new TwinClassFieldEntity();
        field.setId(LINK_FIELD_ID);
        field.setKey("link");
        return field;
    }

    private LookupResult result(FactoryItem item, FieldValue value) {
        LookupResult result = LookupResult.empty(1);
        result.values().put(item, value);
        return result;
    }

    @Nested
    class Fill {

        @Test
        void fill_singleLink_setsAssigneeFromLinkedTwin() throws ServiceException {
            var assignee = new UserEntity().setId(UUID.randomUUID());
            var linkedTwin = new TwinEntity().setId(UUID.randomUUID())
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assignee.getId());
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueLink(buildField()).add(linkedTwin); // items carry the far twins
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(LINK_FIELD_ID)))
                    .thenReturn(result(factoryItem, fieldValue));

            filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: assignee FROM the linked twin's assignee.
            assertSame(assignee, outputTwin.getAssignerUser());
            assertEquals(assignee.getId(), outputTwin.getAssignerUserId());
        }

        @Test
        void fill_linkFieldNotFound_throwsStepError() throws ServiceException {
            // the lookuper contract: a not-found lookup arrives as an undefined value, and this filler
            // has no default for that scenario, so the item fails itself
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(LINK_FIELD_ID)))
                    .thenReturn(result(factoryItem, new FieldValueLink(buildField()))); // undefined

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_linkedTwinHasNoAssignee_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueLink(buildField()).add(new TwinEntity().setId(UUID.randomUUID())); // items carry the far twins
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(LINK_FIELD_ID)))
                    .thenReturn(result(factoryItem, fieldValue));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_nonLinkField_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var fieldValue = new FieldValueText(buildField()).setValue("not-a-link"); // defined — the navigation is what fails
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(LINK_FIELD_ID)))
                    .thenReturn(result(factoryItem, fieldValue));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }
    }
}
