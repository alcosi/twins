package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class FieldTyperForwardLinkTest extends BaseUnitTest {

    @Mock
    private LinkService linkService;

    @Mock
    private TwinLinkService twinLinkService;

    private FieldTyperForwardLink fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperForwardLink();
        setField(fieldTyper, "linkService", linkService);
        setField(fieldTyper, "twinLinkService", twinLinkService);
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

    // FindTwinLinksResult.forwardLinks is package-private (org.twins.core.service.link);
    // populate it via reflection from this package.
    @SuppressWarnings("unchecked")
    private void addForwardLink(TwinLinkService.FindTwinLinksResult result, TwinLinkEntity link) throws Exception {
        var f = TwinLinkService.FindTwinLinksResult.class.getDeclaredField("forwardLinks");
        f.setAccessible(true);
        ((org.cambium.common.kit.KitGrouped<TwinLinkEntity, UUID, UUID>) f.get(result)).add(link);
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
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
    class AllowMultiply {

        @Test
        void allowMultiply_manyTypeAndBackwardLink_isTrue() throws ServiceException {
            // Intended: a link may carry multiple values only when its type is "many" AND it is a backward link
            // for this twin class.
            var link = new LinkEntity().setId(UUID.randomUUID()).setType(LinkType.ManyToMany);
            var classField = classFieldWithTwinClass();
            when(linkService.isBackwardLink(link, classField.getTwinClass())).thenReturn(true);

            assertTrue(fieldTyper.allowMultiply(link, classField));
        }

        @Test
        void allowMultiply_oneToOneType_isFalse() throws ServiceException {
            // Intended: a single-valued link type can never be multi, regardless of direction.
            var link = new LinkEntity().setId(UUID.randomUUID()).setType(LinkType.OneToOne);
            var classField = classFieldWithTwinClass();

            assertFalse(fieldTyper.allowMultiply(link, classField));
        }

        @Test
        void allowMultiply_forwardLink_isFalse() throws ServiceException {
            // Intended: even a many-typed link is single-valued when viewed from the forward direction.
            var link = new LinkEntity().setId(UUID.randomUUID()).setType(LinkType.ManyToMany);
            var classField = classFieldWithTwinClass();
            when(linkService.isBackwardLink(link, classField.getTwinClass())).thenReturn(false);

            assertFalse(fieldTyper.allowMultiply(link, classField));
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_propagatesLinkIdAndMultiplicity() throws ServiceException {
            // Intended: the descriptor carries the resolved link id and the allowMultiply verdict.
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.ManyToMany);
            var classField = classFieldWithTwinClass();
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            when(linkService.isBackwardLink(link, classField.getTwinClass())).thenReturn(true);

            var descriptor = (FieldDescriptorLink) fieldTyper.getFieldDescriptor(classField, properties(linkId));

            assertEquals(linkId, descriptor.linkId());
            assertTrue(descriptor.multiple());
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_forwardLink_itemsAreFarTwins() throws Exception {
            // Intended: for a forward link, deserialization reads the twin's forward links for this link id
            // and maps them to the FAR TWINS — the value carries twins, not twin_links.
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.OneToOne);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var link1 = new TwinLinkEntity()
                    .setId(UUID.randomUUID())
                    .setLinkId(linkId)
                    .setSrcTwinId(twin.getId())
                    .setDstTwinId(dstTwin.getId())
                    .setDstTwin(dstTwin); // loadDstTwin is mocked — pre-wire the far twin
            var linksResult = new TwinLinkService.FindTwinLinksResult();
            addForwardLink(linksResult, link1);
            twin.setTwinLinks(linksResult);
            when(linkService.findEntitySafe(linkId)).thenReturn(link);

            FieldValueLink result = fieldTyper.deserializeValue(properties(linkId), twinField(twin, classField));

            assertEquals(1, result.getItems().size());
            assertSame(dstTwin, result.getItems().get(0));
            assertTrue(result.isForwardLink());
        }

        @Test
        void deserializeValue_noStoredLinks_undefined() throws Exception {
            // Intended: no stored forward links -> the value is undefined
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.OneToOne);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            twin.setTwinLinks(new TwinLinkService.FindTwinLinksResult());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);

            FieldValueLink result = fieldTyper.deserializeValue(properties(linkId), twinField(twin, classField));

            assertTrue(result.isUndefined());
            assertTrue(result.isForwardLink());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_delegatesDesiredSetToReconcileLinks() throws ServiceException {
            // Intended: the field value is a DESIRED link set — the whole write goes through the standard
            // link pipeline (reconcileLinks); the typer carries no twin_link logic of its own.
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.ManyToMany);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            var farTwin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueLink(classField);
            value.setItems(List.of(farTwin));
            var collector = new TwinChangesCollector();

            fieldTyper.serializeValue(properties(linkId), twin, value, collector);

            @SuppressWarnings("unchecked")
            var captor = ArgumentCaptor.forClass((Class<List<TwinEntity>>) (Class<?>) List.class);
            verify(twinLinkService).reconcileLinks(org.mockito.ArgumentMatchers.same(twin),
                    org.mockito.ArgumentMatchers.same(link), org.mockito.ArgumentMatchers.eq(LinkService.LinkDirection.forward),
                    captor.capture(), org.mockito.ArgumentMatchers.same(collector));
            assertEquals(1, captor.getValue().size());
            assertSame(farTwin, captor.getValue().get(0));
        }

        @Test
        void serializeValue_linkWithRelationTwinClass_allowed() throws ServiceException {
            // Intended (supersedes the old fail-fast): a link with relation_twin_class_id is written the
            // same way — reconcileLinks creates an empty AUTO relation twin; the field value carries no
            // relation attributes.
            var linkId = UUID.randomUUID();
            var link = new LinkEntity()
                    .setId(linkId)
                    .setType(LinkType.OneToOne)
                    .setRelationTwinClassId(UUID.randomUUID());
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            var value = new FieldValueLink(classField);
            value.setItems(List.of(new TwinEntity().setId(UUID.randomUUID())));

            assertDoesNotThrow(() -> fieldTyper.serializeValue(properties(linkId), twin, value, new TwinChangesCollector()));
            verify(twinLinkService).reconcileLinks(org.mockito.ArgumentMatchers.same(twin),
                    org.mockito.ArgumentMatchers.same(link), org.mockito.ArgumentMatchers.eq(LinkService.LinkDirection.forward),
                    anyList(), any(TwinChangesCollector.class));
        }

        @Test
        void serializeValue_emptyValue_delegatesEmptySet() throws ServiceException {
            // Intended: an empty value is a pure delete-all — delegated as an empty desired set
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.OneToOne);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            var value = new FieldValueLink(classField);
            value.setItems(null);

            fieldTyper.serializeValue(properties(linkId), twin, value, new TwinChangesCollector());

            @SuppressWarnings("unchecked")
            var captor = ArgumentCaptor.forClass((Class<List<TwinEntity>>) (Class<?>) List.class);
            verify(twinLinkService).reconcileLinks(any(), any(), any(), captor.capture(), any(TwinChangesCollector.class));
            assertTrue(captor.getValue().isEmpty());
        }

        @Test
        void serializeValue_multiplyNotAllowed_throws() throws ServiceException {
            // Intended: the field-level multiplicity guard stays in the typer — two items on a
            // single-valued link type are rejected before any write
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.OneToOne);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            var value = new FieldValueLink(classField);
            value.setItems(List.of(
                    new TwinEntity().setId(UUID.randomUUID()),
                    new TwinEntity().setId(UUID.randomUUID())));

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(linkId), twin, value, new TwinChangesCollector()));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED.getCode(), ex.getErrorCode());
            verify(twinLinkService, never()).reconcileLinks(any(), any(), any(), anyList(), any(TwinChangesCollector.class));
        }
    }
}
