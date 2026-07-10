package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerContextTwinBasicFieldValueEqualsContextBasics;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerContextTwinBasicFieldValueEqualsContextBasicsTest extends BaseUnitTest {

    private ConditionerContextTwinBasicFieldValueEqualsContextBasics conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerContextTwinBasicFieldValueEqualsContextBasics();
    }

    private Properties props(String basicFieldName) {
        var p = new Properties();
        p.put("field", basicFieldName);
        return p;
    }

    private FactoryItem buildItem(TwinBasicFields basics, TwinEntity contextTwin) throws ServiceException {
        var item = mock(FactoryItem.class);
        var ctx = mock(FactoryContext.class);
        when(item.getFactoryContext()).thenReturn(ctx);
        when(ctx.getBasics()).thenReturn(basics);
        when(item.checkSingleContextTwin()).thenReturn(contextTwin);
        return item;
    }

    private FactoryItem buildItemWithoutContext() {
        var item = mock(FactoryItem.class);
        when(item.getFactoryContext()).thenReturn(null);
        return item;
    }

    @Nested
    class Check {

        @Test
        void check_nullFactoryContext_returnsFalse() throws ServiceException {
            assertFalse(conditioner.check(props("name"), buildItemWithoutContext()));
        }

        @Test
        void check_nullBasics_returnsFalse() throws ServiceException {
            var item = mock(FactoryItem.class);
            var ctx = mock(FactoryContext.class);
            when(item.getFactoryContext()).thenReturn(ctx);
            when(ctx.getBasics()).thenReturn(null);

            assertFalse(conditioner.check(props("name"), item));
        }

        @Test
        void check_createdByUserIdMatches_returnsTrue() throws ServiceException {
            var userId = UUID.randomUUID();
            var basics = new TwinBasicFields().setCreatedByUserId(userId);
            var twin = mock(TwinEntity.class);
            when(twin.getCreatedByUserId()).thenReturn(userId);

            assertTrue(conditioner.check(props("createdByUserId"), buildItem(basics, twin)));
        }

        @Test
        void check_createdByUserIdDiffers_returnsFalse() throws ServiceException {
            var basics = new TwinBasicFields().setCreatedByUserId(UUID.randomUUID());
            var twin = mock(TwinEntity.class);
            when(twin.getCreatedByUserId()).thenReturn(UUID.randomUUID());

            assertFalse(conditioner.check(props("createdByUserId"), buildItem(basics, twin)));
        }

        @Test
        void check_nameMatches_returnsTrue() throws ServiceException {
            var basics = new TwinBasicFields().setName("alpha");
            var twin = mock(TwinEntity.class);
            when(twin.getName()).thenReturn("alpha");

            assertTrue(conditioner.check(props("name"), buildItem(basics, twin)));
        }

        @Test
        void check_nameDiffers_returnsFalse() throws ServiceException {
            var basics = new TwinBasicFields().setName("alpha");
            var twin = mock(TwinEntity.class);
            when(twin.getName()).thenReturn("beta");

            assertFalse(conditioner.check(props("name"), buildItem(basics, twin)));
        }

        @Test
        void check_descriptionMatches_returnsTrue() throws ServiceException {
            var basics = new TwinBasicFields().setDescription("desc");
            var twin = mock(TwinEntity.class);
            when(twin.getDescription()).thenReturn("desc");

            assertTrue(conditioner.check(props("description"), buildItem(basics, twin)));
        }

        @Test
        void check_descriptionDiffers_returnsFalse() throws ServiceException {
            var basics = new TwinBasicFields().setDescription("desc");
            var twin = mock(TwinEntity.class);
            when(twin.getDescription()).thenReturn("other");

            assertFalse(conditioner.check(props("description"), buildItem(basics, twin)));
        }

        @Test
        void check_assigneeUserIdMatchesTwinAssigner_returnsTrue() throws ServiceException {
            // contract per current impl: basics.assigneeUserId is compared against the twin's
            // assignerUserId (TwinEntity exposes no assignee field — see audit note).
            var userId = UUID.randomUUID();
            var basics = new TwinBasicFields().setAssigneeUserId(userId);
            var twin = mock(TwinEntity.class);
            when(twin.getAssignerUserId()).thenReturn(userId);

            assertTrue(conditioner.check(props("assigneeUserId"), buildItem(basics, twin)));
        }

        @Test
        void check_assigneeUserIdDiffersFromTwinAssigner_returnsFalse() throws ServiceException {
            var basics = new TwinBasicFields().setAssigneeUserId(UUID.randomUUID());
            var twin = mock(TwinEntity.class);
            when(twin.getAssignerUserId()).thenReturn(UUID.randomUUID());

            assertFalse(conditioner.check(props("assigneeUserId"), buildItem(basics, twin)));
        }

        @Test
        void check_nullValuesTreatedEqual_returnsTrue() throws ServiceException {
            // Objects.equals semantics: both null counts as equal
            var basics = new TwinBasicFields().setName(null);
            var twin = mock(TwinEntity.class);
            when(twin.getName()).thenReturn(null);

            assertTrue(conditioner.check(props("name"), buildItem(basics, twin)));
        }
    }
}
