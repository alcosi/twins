package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerTwinAssigneeUserIsMemberOfGroupForLinkedTwinContextField;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.usergroup.UserGroupService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConditionerTwinAssigneeUserIsMemberOfGroupForLinkedTwinContextFieldTest extends BaseUnitTest {

    @Mock
    private UserGroupService userGroupService;

    @Mock
    private TwinLinkService twinLinkService;

    private ConditionerTwinAssigneeUserIsMemberOfGroupForLinkedTwinContextField conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerTwinAssigneeUserIsMemberOfGroupForLinkedTwinContextField();
        setField(conditioner, "userGroupService", userGroupService);
        setField(conditioner, "twinLinkService", twinLinkService);
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

    private Properties props(UUID fieldId, UUID groupId) {
        var p = new Properties();
        p.put("twinClassFieldId", fieldId.toString());
        p.put("userGroupIds", groupId.toString());
        return p;
    }

    private FactoryItem itemWithContextField(UUID fieldId, FieldValue value) {
        var ctx = mock(FactoryContext.class);
        when(ctx.getFields()).thenReturn(Map.of(fieldId, value));
        // real FactoryItem so getOutput().getTwinId() (used in the size>1 error message) resolves
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setId(UUID.randomUUID()));
        return new FactoryItem().setOutput(output).setFactoryContext(ctx);
    }

    private UserEntity userWithGroups(Kit<UserGroupEntity, UUID> kit) throws Exception {
        var user = new UserEntity();
        var idField = UserEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, UUID.randomUUID());
        user.setUserGroups(kit);
        return user;
    }

    @Nested
    class Check {

        @Test
        void check_linkedTwinAssignerMemberOfGroup_returnsTrue() throws Exception {
            var fieldId = UUID.randomUUID();
            var groupId = UUID.randomUUID();

            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(groupId);
            kit.add(group);
            var assignerUser = userWithGroups(kit);

            var link = mock(TwinLinkEntity.class);
            var fvl = mock(FieldValueLink.class);
            when(fvl.size()).thenReturn(1);
            when(fvl.getItems()).thenReturn(List.of(link));
            var dstTwin = mock(TwinEntity.class);
            when(dstTwin.getAssignerUser()).thenReturn(assignerUser);
            when(twinLinkService.getDstTwinSafe(link)).thenReturn(dstTwin);

            assertTrue(conditioner.check(props(fieldId, groupId), itemWithContextField(fieldId, fvl)));
            verify(userGroupService).loadGroups(assignerUser);
        }

        @Test
        void check_linkedTwinAssignerNotMemberOfGroup_returnsFalse() throws Exception {
            var fieldId = UUID.randomUUID();
            var userGroupId = UUID.randomUUID();

            var kit = new Kit<>(UserGroupEntity::getId);
            var group = new UserGroupEntity();
            group.setId(userGroupId);
            kit.add(group);
            var assignerUser = userWithGroups(kit);

            var link = mock(TwinLinkEntity.class);
            var fvl = mock(FieldValueLink.class);
            when(fvl.size()).thenReturn(1);
            when(fvl.getItems()).thenReturn(List.of(link));
            var dstTwin = mock(TwinEntity.class);
            when(dstTwin.getAssignerUser()).thenReturn(assignerUser);
            when(twinLinkService.getDstTwinSafe(link)).thenReturn(dstTwin);

            assertFalse(conditioner.check(props(fieldId, UUID.randomUUID()), itemWithContextField(fieldId, fvl)));
        }

        @Test
        void check_assignerUserGroupsEmpty_returnsFalse() throws Exception {
            var fieldId = UUID.randomUUID();

            var assignerUser = userWithGroups(new Kit<>(UserGroupEntity::getId));

            var link = mock(TwinLinkEntity.class);
            var fvl = mock(FieldValueLink.class);
            when(fvl.size()).thenReturn(1);
            when(fvl.getItems()).thenReturn(List.of(link));
            var dstTwin = mock(TwinEntity.class);
            when(dstTwin.getAssignerUser()).thenReturn(assignerUser);
            when(twinLinkService.getDstTwinSafe(link)).thenReturn(dstTwin);

            assertFalse(conditioner.check(props(fieldId, UUID.randomUUID()), itemWithContextField(fieldId, fvl)));
        }

        @Test
        void check_moreThanOneLinkedTwin_throws() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var fvl = mock(FieldValueLink.class);
            when(fvl.size()).thenReturn(2);

            assertThrows(ServiceException.class,
                    () -> conditioner.check(props(fieldId, UUID.randomUUID()), itemWithContextField(fieldId, fvl)));
        }
    }
}
