package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueI18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldValueI18n extends FieldValueStated. Translations live in a Map<Locale,String>.
 * setTranslations transitions to PRESENT or CLEARED (null/empty); addTranslation lazily
 * creates the map; getTranslations returns an unmodifiable view.
 */
class FieldValueI18nTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class SetTranslations {

        @Test
        void setTranslations_nonEmpty_transitionsToPresent() {
            var value = new FieldValueI18n(field);

            var returned = value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            assertSame(value, returned);
            assertEquals(1, value.getTranslations().size());
            assertFalse(value.isUndefined());
            assertFalse(value.isCleared());
        }

        @Test
        void setTranslations_empty_clears() {
            var value = new FieldValueI18n(field);
            value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            value.setTranslations(Map.of());

            assertTrue(value.isCleared());
            assertTrue(value.getTranslations().isEmpty());
        }

        @Test
        void setTranslations_null_clears() {
            var value = new FieldValueI18n(field);
            value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            value.setTranslations(null);

            assertTrue(value.isCleared());
        }

        @Test
        void setTranslations_defensivelyCopies() {
            var value = new FieldValueI18n(field);
            var src = new HashMap<Locale, String>();
            src.put(Locale.ENGLISH, "Hello");

            value.setTranslations(src);
            src.put(Locale.FRENCH, "Bonjour");

            assertEquals(1, value.getTranslations().size());
        }
    }

    @Nested
    class AddTranslation {

        @Test
        void addTranslation_afterCleared_lazilyCreatesMap() {
            var value = new FieldValueI18n(field);
            value.setTranslations(null);

            value.addTranslation(Locale.ENGLISH, "Hello");

            assertEquals("Hello", value.getTranslations().get(Locale.ENGLISH));
        }

        @Test
        void addTranslation_appendsToExistingMap() {
            var value = new FieldValueI18n(field);
            value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            value.addTranslation(Locale.FRENCH, "Bonjour");

            assertEquals(2, value.getTranslations().size());
        }
    }

    @Nested
    class GetTranslations {

        @Test
        void getTranslations_returnsUnmodifiableView() {
            var value = new FieldValueI18n(field);
            value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            var view = value.getTranslations();

            assertThrows(UnsupportedOperationException.class, () -> view.put(Locale.FRENCH, "x"));
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingTranslationValue_returnsTrue() {
            var value = new FieldValueI18n(field);
            value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            assertTrue(value.hasValue("Hello"));
        }

        @Test
        void hasValue_nonMatchingValue_returnsFalse() {
            var value = new FieldValueI18n(field);
            value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            assertFalse(value.hasValue("Goodbye"));
        }

        @Test
        void hasValue_whenTranslationsNull_returnsFalse() {
            var value = new FieldValueI18n(field);
            value.setTranslations(null);

            assertFalse(value.hasValue("Hello"));
        }
    }

    @Nested
    class StateLifecycle {

        @Test
        void clear_wipesTranslations() {
            var value = new FieldValueI18n(field);
            value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            value.clear();

            assertTrue(value.isCleared());
            assertTrue(value.getTranslations().isEmpty());
        }

        @Test
        void undefine_wipesTranslations() {
            var value = new FieldValueI18n(field);
            value.setTranslations(Map.of(Locale.ENGLISH, "Hello"));

            value.undefine();

            assertTrue(value.isUndefined());
            assertTrue(value.getTranslations().isEmpty());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_copiesTranslationsIntoNewMap() {
            var src = new FieldValueI18n(field);
            src.setTranslations(Map.of(Locale.ENGLISH, "Hello"));
            var dst = new FieldValueI18n(field);

            src.copyValueTo(dst);

            assertEquals(1, dst.getTranslations().size());
            assertEquals("Hello", dst.getTranslations().get(Locale.ENGLISH));
        }

        @Test
        void copyValueTo_isIndependentFromSource() {
            var src = new FieldValueI18n(field);
            src.setTranslations(Map.of(Locale.ENGLISH, "Hello"));
            var dst = new FieldValueI18n(field);

            src.copyValueTo(dst);
            src.addTranslation(Locale.FRENCH, "Bonjour");

            assertEquals(1, dst.getTranslations().size());
        }
    }

    @Nested
    class NewInstance {

        @Test
        void newInstance_yieldsFreshI18n() {
            var src = new FieldValueI18n(field);

            var created = src.newInstance(field);

            assertInstanceOf(FieldValueI18n.class, created);
            assertNotSame(src, created);
        }
    }
}
