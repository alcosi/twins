package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldI18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorI18n;
import org.twins.core.featurer.fieldtyper.value.FieldValueI18n;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

class FieldTyperI18nTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldTyperI18n fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperI18n(twinService);
        // lenient: deserialize/serialize call loadTwinFields; getFieldDescriptor does not.
        lenient().doNothing().when(twinService).loadTwinFields(any(TwinEntity.class));
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        return new Properties();
    }

    private TwinFieldI18nEntity i18nEntity(TwinClassFieldEntity classField, Locale locale, String translation) {
        return new TwinFieldI18nEntity()
                .setTwinClassFieldId(classField.getId())
                .setTwinClassField(classField)
                .setLocale(locale)
                .setTranslation(translation)
                .setTwin(new TwinEntity().setId(UUID.randomUUID()));
    }

    private void seedI18nKit(TwinEntity twin, List<TwinFieldI18nEntity> entities) {
        // Kit is grouped by TwinClassFieldEntity::getId; getStoredFieldsForTwinAndField calls .getGrouped(classFieldId).
        twin.setTwinFieldI18nKit(new KitGrouped<>(
                entities,
                TwinFieldI18nEntity::getId,
                TwinFieldI18nEntity::getTwinClassFieldId));
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_multipleLocales_returnsAllTranslations() throws ServiceException {
            // Intended: every stored locale->translation pair is copied onto the field value map.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            seedI18nKit(twin, List.of(
                    i18nEntity(classField, Locale.ENGLISH, "Hello"),
                    i18nEntity(classField, Locale.FRENCH, "Bonjour")));

            FieldValueI18n result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals(Map.of(Locale.ENGLISH, "Hello", Locale.FRENCH, "Bonjour"), result.getTranslations());
        }

        @Test
        void deserializeValue_noStoredRows_returnsEmptyMap() throws ServiceException {
            // Intended: no stored entities -> translations map is empty (not null).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            seedI18nKit(twin, List.of());

            FieldValueI18n result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertTrue(result.getTranslations().isEmpty());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_newLocale_addsEntityToCollector() throws ServiceException {
            // Intended: a locale not yet stored is added as a new TwinFieldI18nEntity.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            seedI18nKit(twin, List.of());
            var value = new FieldValueI18n(classField)
                    .setTranslations(Map.of(Locale.ENGLISH, "Hello"));
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            var added = collector.getSaveEntities(TwinFieldI18nEntity.class);
            assertEquals(1, added.size());
            var entity = added.iterator().next();
            assertEquals(Locale.ENGLISH, entity.getLocale());
            assertEquals("Hello", entity.getTranslation());
            assertEquals(classField.getId(), entity.getTwinClassFieldId());
            assertEquals(twin.getId(), entity.getTwinId());
        }

        @Test
        void serializeValue_changedTranslation_updatesExistingEntity() throws ServiceException {
            // Intended: a locale already stored with a different translation is updated in place.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var existing = i18nEntity(classField, Locale.ENGLISH, "Hello");
            seedI18nKit(twin, List.of(existing));
            var value = new FieldValueI18n(classField)
                    .setTranslations(Map.of(Locale.ENGLISH, "Hi"));
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals("Hi", existing.getTranslation());
            // Same instance is registered for save.
            assertTrue(collector.getSaveEntities(TwinFieldI18nEntity.class).contains(existing));
        }

        @Test
        void serializeValue_unchangedTranslation_doesNothing() throws ServiceException {
            // Intended: identical translation -> neither add nor mutate; collector stays empty.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            seedI18nKit(twin, List.of(i18nEntity(classField, Locale.ENGLISH, "Hello")));
            var value = new FieldValueI18n(classField)
                    .setTranslations(Map.of(Locale.ENGLISH, "Hello"));
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertFalse(collector.hasChanges());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsI18nDescriptor() throws ServiceException {
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertInstanceOf(FieldDescriptorI18n.class, descriptor);
        }
    }
}
