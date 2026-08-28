package org.twins.core.featurer.widget.accessor;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class WidgetAccessorAllowForKeysTest extends BaseUnitTest {

    private final WidgetAccessorAllowForKeys accessor = new WidgetAccessorAllowForKeys();

    @Nested
    class IsAvailableForClass {

        @Test
        void isAvailableForClass_classIdInList_returnsTrue() {
            var classId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var twinClass = buildTwinClass(classId);
            var properties = buildPropertiesWithClassIds(classId);

            var result = accessor.isAvailableForClass(properties, twinClass);

            assertTrue(result);
        }

        @Test
        void isAvailableForClass_classIdNotInList_returnsFalse() {
            var classId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var otherId = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var twinClass = buildTwinClass(classId);
            var properties = buildPropertiesWithClassIds(otherId);

            var result = accessor.isAvailableForClass(properties, twinClass);

            assertFalse(result);
        }

        @Test
        void isAvailableForClass_emptyList_returnsFalse() {
            var classId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var twinClass = buildTwinClass(classId);
            var properties = new Properties();
            properties.setProperty("twinClassIdList", "");

            var result = accessor.isAvailableForClass(properties, twinClass);

            assertFalse(result);
        }

        @Test
        void isAvailableForClass_multipleIds_classInList_returnsTrue() {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var classId2 = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var classId3 = UUID.fromString("99999999-8888-7777-6666-555555555555");
            var twinClass = buildTwinClass(classId2);
            var properties = new Properties();
            properties.setProperty("twinClassIdList", classId1 + ", " + classId2 + ", " + classId3);

            var result = accessor.isAvailableForClass(properties, twinClass);

            assertTrue(result);
        }

        @Test
        void isAvailableForClass_multipleIds_classNotInList_returnsFalse() {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var classId2 = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var otherId = UUID.fromString("99999999-8888-7777-6666-555555555555");
            var twinClass = buildTwinClass(otherId);
            var properties = new Properties();
            properties.setProperty("twinClassIdList", classId1 + ", " + classId2);

            var result = accessor.isAvailableForClass(properties, twinClass);

            assertFalse(result);
        }

        @Test
        void isAvailableForClass_nullClassId_returnsFalse() {
            var classId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var twinClass = new TwinClassEntity();
            var properties = buildPropertiesWithClassIds(classId);

            var result = accessor.isAvailableForClass(properties, twinClass);

            assertFalse(result);
        }
    }

    private TwinClassEntity buildTwinClass(UUID id) {
        var twinClass = new TwinClassEntity();
        twinClass.setId(id);
        return twinClass;
    }

    private Properties buildPropertiesWithClassIds(UUID... ids) {
        var properties = new Properties();
        var sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ids[i]);
        }
        properties.setProperty("twinClassIdList", sb.toString());
        return properties;
    }
}
