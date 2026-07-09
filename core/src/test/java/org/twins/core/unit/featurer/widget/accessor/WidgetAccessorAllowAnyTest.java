package org.twins.core.featurer.widget.accessor;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


class WidgetAccessorAllowAnyTest extends BaseUnitTest {

    private final WidgetAccessorAllowAny accessor = new WidgetAccessorAllowAny();

    @Nested
    class IsAvailableForClass {

        @Test
        void isAvailableForClass_alwaysReturnsTrue() {
            var twinClass = new TwinClassEntity();

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertTrue(result);
        }

        @Test
        void isAvailableForClass_returnsTrueForAnyTwinClass() {
            var twinClass = new TwinClassEntity();
            twinClass.setId(null);

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertTrue(result);
        }

        @Test
        void isAvailableForClass_returnsTrueRegardlessOfProperties() {
            var twinClass = new TwinClassEntity();
            var properties = new Properties();
            properties.setProperty("someKey", "someValue");

            var result = accessor.isAvailableForClass(properties, twinClass);

            assertTrue(result);
        }
    }
}
