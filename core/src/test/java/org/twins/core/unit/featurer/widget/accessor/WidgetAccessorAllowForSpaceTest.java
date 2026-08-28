package org.twins.core.featurer.widget.accessor;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


class WidgetAccessorAllowForSpaceTest extends BaseUnitTest {

    private final WidgetAccessorAllowForSpace accessor = new WidgetAccessorAllowForSpace();

    @Nested
    class IsAvailableForClass {

        @Test
        void isAvailableForClass_permissionSchemaSpaceTrue_returnsTrue() {
            var twinClass = new TwinClassEntity();
            twinClass.setPermissionSchemaSpace(true);
            twinClass.setTwinflowSchemaSpace(false);
            twinClass.setTwinClassSchemaSpace(false);
            twinClass.setAliasSpace(false);

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertTrue(result);
        }

        @Test
        void isAvailableForClass_twinflowSchemaSpaceTrue_returnsTrue() {
            var twinClass = new TwinClassEntity();
            twinClass.setPermissionSchemaSpace(false);
            twinClass.setTwinflowSchemaSpace(true);
            twinClass.setTwinClassSchemaSpace(false);
            twinClass.setAliasSpace(false);

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertTrue(result);
        }

        @Test
        void isAvailableForClass_twinClassSchemaSpaceTrue_returnsTrue() {
            var twinClass = new TwinClassEntity();
            twinClass.setPermissionSchemaSpace(false);
            twinClass.setTwinflowSchemaSpace(false);
            twinClass.setTwinClassSchemaSpace(true);
            twinClass.setAliasSpace(false);

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertTrue(result);
        }

        @Test
        void isAvailableForClass_aliasSpaceTrue_returnsTrue() {
            var twinClass = new TwinClassEntity();
            twinClass.setPermissionSchemaSpace(false);
            twinClass.setTwinflowSchemaSpace(false);
            twinClass.setTwinClassSchemaSpace(false);
            twinClass.setAliasSpace(true);

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertTrue(result);
        }

        @Test
        void isAvailableForClass_allSpaceFlagsFalse_returnsFalse() {
            var twinClass = new TwinClassEntity();
            twinClass.setPermissionSchemaSpace(false);
            twinClass.setTwinflowSchemaSpace(false);
            twinClass.setTwinClassSchemaSpace(false);
            twinClass.setAliasSpace(false);

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertFalse(result);
        }

        @Test
        void isAvailableForClass_allSpaceFlagsNull_returnsFalse() {
            var twinClass = new TwinClassEntity();
            twinClass.setPermissionSchemaSpace(false);
            twinClass.setTwinflowSchemaSpace(false);
            twinClass.setTwinClassSchemaSpace(false);
            twinClass.setAliasSpace(false);

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertFalse(result);
        }

        @Test
        void isAvailableForClass_multipleSpaceFlagsTrue_returnsTrue() {
            var twinClass = new TwinClassEntity();
            twinClass.setPermissionSchemaSpace(true);
            twinClass.setTwinflowSchemaSpace(false);
            twinClass.setTwinClassSchemaSpace(false);
            twinClass.setAliasSpace(true);

            var result = accessor.isAvailableForClass(new Properties(), twinClass);

            assertTrue(result);
        }
    }
}
