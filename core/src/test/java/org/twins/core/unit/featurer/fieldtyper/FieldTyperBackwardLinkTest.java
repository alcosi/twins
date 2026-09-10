package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.enums.link.LinkType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.same;

class FieldTyperBackwardLinkTest extends BaseUnitTest {

    @Mock
    private LinkService linkService;

    @Mock
    private TwinLinkService twinLinkService;

    private FieldTyperBackwardLink fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperBackwardLink();
        setField(fieldTyper, "linkService", linkService);
        setField(fieldTyper, "twinLinkService", twinLinkService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private Properties properties(UUID linkId) {
        var props = new Properties();
        props.setProperty("linkUUID", linkId.toString());
        return props;
    }

    private TwinClassFieldEntity classFieldWithTwinClass() {
        var twinClass = new TwinClassEntity().setId(UUID.randomUUID());
        return new TwinClassFieldEntity()
                .setId(UUID.randomUUID())
                .setTwinClass(twinClass)
                .setTwinClassId(twinClass.getId());
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_nonOneToOneLink_throws() throws ServiceException {
            // Intended: the backward side of a many-typed link is unbounded (hundreds+ of sources may point
            // at one twin) — only OneToOne keeps the state-based diff bounded, everything else is rejected
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.ManyToMany);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            var value = new FieldValueLink(classField);
            value.setItems(List.of(new TwinEntity().setId(UUID.randomUUID())));

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(linkId), twin, value, new TwinChangesCollector()));

            assertEquals(ErrorCodeTwins.TWIN_LINK_INCORRECT.getCode(), ex.getErrorCode());
            verify(twinLinkService, never()).reconcileLinks(any(), any(), any(), anyList(), any(TwinChangesCollector.class));
        }

        @Test
        void serializeValue_oneToOneLink_delegatesWithBackwardDirection() throws ServiceException {
            // Intended: the backward typer sets the direction EXPLICITLY — class-based detection cannot be
            // used (a link between the same twin class on both ends is ambiguous)
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.OneToOne);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            var farTwin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueLink(classField);
            value.setItems(List.of(farTwin));

            fieldTyper.serializeValue(properties(linkId), twin, value, new TwinChangesCollector());

            verify(twinLinkService).reconcileLinks(same(twin), same(link), eq(LinkService.LinkDirection.backward),
                    eq(List.of(farTwin)), any(TwinChangesCollector.class));
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_backwardLink_itemsAreSrcTwins() throws Exception {
            // Intended: the backward value carries the SRC twins of the twin's (OneToOne, at most one)
            // incoming links
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.OneToOne);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            var stored = new TwinLinkEntity()
                    .setId(UUID.randomUUID())
                    .setLinkId(linkId)
                    .setSrcTwinId(srcTwin.getId())
                    .setSrcTwin(srcTwin) // loadSrcTwin is mocked — pre-wire the far twin
                    .setDstTwinId(twin.getId());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            when(twinLinkService.findTwinLinks(link, twin, LinkService.LinkDirection.backward))
                    .thenReturn(List.of(stored));

            FieldValueLink result = fieldTyper.deserializeValue(properties(linkId), new TwinField(twin, classField));

            assertEquals(1, result.getItems().size());
            assertSame(srcTwin, result.getItems().get(0));
            assertFalse(result.isForwardLink());
        }

        @Test
        void deserializeValue_nonOneToOneLink_undefined() throws Exception {
            // Intended: a legacy misconfigured field (non-OneToOne backward link) renders as undefined
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.ManyToMany);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);

            FieldValueLink result = fieldTyper.deserializeValue(properties(linkId), new TwinField(twin, classField));

            assertTrue(result.isUndefined());
            assertFalse(result.isForwardLink());
            verify(twinLinkService, never()).findTwinLinks(any(), any(), any());
        }
    }
}
