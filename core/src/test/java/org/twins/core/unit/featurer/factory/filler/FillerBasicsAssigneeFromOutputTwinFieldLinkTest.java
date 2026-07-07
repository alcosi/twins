package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromOutputTwinFieldLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerBasicsAssigneeFromOutputTwinFieldLinkTest extends BaseUnitTest {

    private FillerBasicsAssigneeFromOutputTwinFieldLink filler;

    @Mock
    private TwinLinkService twinLinkService;

    private static final UUID FIELD_ID = UUID.randomUUID();
    private static final UUID LINK_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBasicsAssigneeFromOutputTwinFieldLink();
        setField(filler, "twinLinkService", twinLinkService);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                var f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("twinClassFieldId", FIELD_ID.toString());
        p.setProperty("linkId", LINK_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem(FieldValueLink fieldValue) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        output.addField(fieldValue);
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity buildField() {
        var field = new TwinClassFieldEntity();
        field.setId(FIELD_ID);
        field.setKey("link");
        return field;
    }

    @Nested
    class Fill {

        @Test
        void fill_matchingLink_setsDstTwinAssignee() throws ServiceException {
            var assigneeId = UUID.randomUUID();
            var assignee = new UserEntity().setId(assigneeId);
            var dstTwin = new TwinEntity()
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assigneeId);
            var twinLink = new TwinLinkEntity()
                    .setLinkId(LINK_ID)
                    .setDstTwin(dstTwin);
            var fieldValue = new FieldValueLink(buildField());
            fieldValue.add(twinLink);
            var factoryItem = buildFactoryItem(fieldValue);

            filler.fill(props(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: assignee FROM the output twin's linked dst twin (matched by linkId).
            assertSame(assignee, outputTwin.getAssignerUser());
            assertEquals(assigneeId, outputTwin.getAssignerUserId());
        }

        @Test
        void fill_missingField_throwsStepError() {
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var factoryItem = new FactoryItem().setOutput(output); // field not added

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_emptyLink_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem(new FieldValueLink(buildField())); // undefined

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_noLinkWithMatchingLinkId_throwsStepError() throws ServiceException {
            var twinLink = new TwinLinkEntity()
                    .setLinkId(UUID.randomUUID()) // different link id
                    .setDstTwin(new TwinEntity());
            var fieldValue = new FieldValueLink(buildField());
            fieldValue.add(twinLink);
            var factoryItem = buildFactoryItem(fieldValue);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_nonLinkField_doesNothing() throws ServiceException {
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            output.addField(new FieldValueText(buildField()));
            var factoryItem = new FactoryItem().setOutput(output);

            // field present but not a FieldValueLink -> silently skips (no throw, no set).
            filler.fill(props(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertNull(outputTwin.getAssignerUser());
        }
    }
}
