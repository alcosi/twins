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
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldAsContextFieldHead;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FillerFieldAsContextFieldHeadTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFields fromContextFields;

    private FillerFieldAsContextFieldHead filler;

    private static final UUID SRC_FIELD_ID = UUID.randomUUID();
    private static final UUID DST_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        filler = new FillerFieldAsContextFieldHead();
        inject("twinService", twinService);
        inject("fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromContextFields()).thenReturn(fromContextFields);
    }

    private void inject(String fieldName, Object value) {
        try {
            var f = findField(filler.getClass(), fieldName);
            f.setAccessible(true);
            f.set(filler, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("srcTwinClassFieldId", SRC_FIELD_ID.toString());
        p.setProperty("dstTwinClassFieldId", DST_FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id);
    }

    private void stubLookupValue(FactoryItem factoryItem, FieldValue value) throws ServiceException {
        when(fromContextFields.lookupFieldValue(any(FactoryItemsBatch.class), eq(SRC_FIELD_ID)))
                .thenReturn(new LookupResult(Map.of(factoryItem, value), Map.of()));
    }

    @Nested
    class Fill {

        @Test
        void fill_linkFieldWithDstTwinHead_writesHeadIdToDst() throws ServiceException {
            // NAME promises: take head of the dst twin of the CONTEXT LINK FIELD's value, write head twin to dst field.
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var dstTwin = new TwinEntity().setHeadTwin(headTwin);
            var srcValue = new FieldValueLink(field(SRC_FIELD_ID)).add(dstTwin); // items carry the far twins
            var factoryItem = buildFactoryItem();
            stubLookupValue(factoryItem, srcValue);
            var createdHeadLink = new FieldValueLink(field(DST_FIELD_ID));
            when(twinService.createFieldValue(DST_FIELD_ID, headTwin)).thenReturn(createdHeadLink);

            filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false);

            assertSame(createdHeadLink, factoryItem.getOutput().getField(DST_FIELD_ID));
        }

        @Test
        void fill_nonLinkField_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var srcValue = new FieldValueText(field(SRC_FIELD_ID)).setValue("v");
            stubLookupValue(factoryItem, srcValue);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_emptyLink_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            // FieldValueLink with no items -> isEmpty()==true (isUndefined since collection==null);
            // under the new lookuper contract the filler fails the item itself on an undefined source.
            var srcValue = new FieldValueLink(field(SRC_FIELD_ID));
            stubLookupValue(factoryItem, srcValue);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_dstTwinHasNoHead_throwsStepError() throws ServiceException {
            var dstTwin = new TwinEntity(); // headTwin == null
            var srcValue = new FieldValueLink(field(SRC_FIELD_ID)).add(dstTwin); // items carry the far twins
            var factoryItem = buildFactoryItem();
            stubLookupValue(factoryItem, srcValue);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            // one bulk loadHead covers the whole batch — loaded before the null check throws
            verify(twinService).loadHead(argThat((java.util.Collection<TwinEntity> col) -> col.size() == 1 && col.contains(dstTwin)));
        }
    }
}
