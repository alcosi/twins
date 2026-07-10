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
import org.twins.core.domain.TwinField;
import org.twins.core.enums.link.LinkType;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperLinkTest extends BaseUnitTest {

    @Mock
    private LinkService linkService;

    @Mock
    private TwinLinkService twinLinkService;

    private FieldTyperLink fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperLink();
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

    // FindTwinLinksResult.forwardLinks/backwardLinks are package-private (org.twins.core.service.link);
    // populate them via reflection from this package.
    @SuppressWarnings("unchecked")
    private void addForwardLink(TwinLinkService.FindTwinLinksResult result, TwinLinkEntity link) throws Exception {
        var f = TwinLinkService.FindTwinLinksResult.class.getDeclaredField("forwardLinks");
        f.setAccessible(true);
        ((org.cambium.common.kit.KitGrouped<TwinLinkEntity, UUID, UUID>) f.get(result)).add(link);
    }

    @SuppressWarnings("unchecked")
    private void addBackwardLink(TwinLinkService.FindTwinLinksResult result, TwinLinkEntity link) throws Exception {
        var f = TwinLinkService.FindTwinLinksResult.class.getDeclaredField("backwardLinks");
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
        void deserializeValue_forwardLink_collectsForwardLinksForLink() throws Exception {
            // Intended: for a forward link, deserialization reads the twin's forward links for this link id
            // into the value and marks the value as a forward link.
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.OneToOne);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            var link1 = new TwinLinkEntity()
                    .setId(UUID.randomUUID())
                    .setLinkId(linkId)
                    .setSrcTwinId(twin.getId())
                    .setDstTwinId(UUID.randomUUID());
            var linksResult = new TwinLinkService.FindTwinLinksResult();
            addForwardLink(linksResult, link1);
            twin.setTwinLinks(linksResult);
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            when(linkService.detectLinkDirection(link, classField.getTwinClass()))
                    .thenReturn(LinkService.LinkDirection.forward);

            FieldValueLink result = fieldTyper.deserializeValue(properties(linkId), twinField(twin, classField));

            assertEquals(1, result.getItems().size());
            assertSame(link1, result.getItems().get(0));
            assertTrue(result.isForwardLink());
        }

        @Test
        void deserializeValue_backwardLink_collectsBackwardLinksForLink() throws Exception {
            // Intended: for a backward link, deserialization reads the twin's backward links and flags
            // the value as NOT a forward link.
            var linkId = UUID.randomUUID();
            var link = new LinkEntity().setId(linkId).setType(LinkType.ManyToMany);
            var classField = classFieldWithTwinClass();
            var twin = new TwinEntity().setId(UUID.randomUUID()).setTwinClass(classField.getTwinClass());
            var link1 = new TwinLinkEntity()
                    .setId(UUID.randomUUID())
                    .setLinkId(linkId)
                    .setSrcTwinId(UUID.randomUUID())
                    .setDstTwinId(twin.getId());
            var linksResult = new TwinLinkService.FindTwinLinksResult();
            addBackwardLink(linksResult, link1);
            twin.setTwinLinks(linksResult);
            when(linkService.findEntitySafe(linkId)).thenReturn(link);
            when(linkService.detectLinkDirection(link, classField.getTwinClass()))
                    .thenReturn(LinkService.LinkDirection.backward);

            FieldValueLink result = fieldTyper.deserializeValue(properties(linkId), twinField(twin, classField));

            assertEquals(1, result.getItems().size());
            assertSame(link1, result.getItems().get(0));
            assertFalse(result.isForwardLink());
        }
    }
}
