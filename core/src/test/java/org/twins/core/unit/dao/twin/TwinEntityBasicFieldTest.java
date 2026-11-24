package org.twins.core.unit.dao.twin;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.consts.SystemIds;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BasicField contract behind twin grouping: tags and markers are base fields too, but kit-backed
 * (twinColumn = false) — column-based SQL paths must skip them, while getRawValue returns the
 * twin's option kit. Every other member keeps the scalar-column flag.
 */
class TwinEntityBasicFieldTest extends BaseUnitTest {

    @Test
    void tagsAndMarkersAreKitBackedBasicFields() {
        TwinEntity.BasicField tags = TwinEntity.BasicField.convertOrNull(SystemIds.TwinClassField.Base.TAGS);
        TwinEntity.BasicField markers = TwinEntity.BasicField.convertOrNull(SystemIds.TwinClassField.Base.MARKERS);

        assertSame(TwinEntity.BasicField.TAGS, tags);
        assertSame(TwinEntity.BasicField.MARKERS, markers);
        assertFalse(tags.isTwinColumn());
        assertFalse(markers.isTwinColumn());
    }

    @Test
    void kitBackedGetterReadsTheTwinKit() {
        TwinEntity twin = new TwinEntity();
        Kit<DataListOptionEntity, UUID> kit = new Kit<>(
                List.of(new DataListOptionEntity().setId(UUID.randomUUID())), DataListOptionEntity::getId);
        twin.setTwinTagKit(kit);

        assertSame(kit, TwinEntity.BasicField.TAGS.getRawValue(twin));
    }

    @Test
    void everyOtherMemberKeepsTheColumnFlag() {
        for (TwinEntity.BasicField field : TwinEntity.BasicField.values()) {
            boolean kitBacked = field == TwinEntity.BasicField.TAGS || field == TwinEntity.BasicField.MARKERS;
            assertEquals(!kitBacked, field.isTwinColumn(), field.name());
        }
    }

    @Test
    void groupableFlagMarksExactlyTheGroupingLevels() {
        Set<TwinEntity.BasicField> groupable = EnumSet.of(
                TwinEntity.BasicField.OWNER_USER_ID, TwinEntity.BasicField.ASSIGNEE_USER_ID,
                TwinEntity.BasicField.CREATOR_USER_ID, TwinEntity.BasicField.TWIN_STATUS_ID,
                TwinEntity.BasicField.TWIN_CLASS_ID, TwinEntity.BasicField.HEAD_TWIN_ID,
                TwinEntity.BasicField.FLAVOR_DATA_LIST_OPTION_ID,
                TwinEntity.BasicField.TAGS, TwinEntity.BasicField.MARKERS);

        for (TwinEntity.BasicField field : TwinEntity.BasicField.values()) {
            assertEquals(groupable.contains(field), field.isGroupable(), field.name());
        }
    }

    @Test
    void sortableFlagMarksExactlyTheBasicSortFields() {
        Set<TwinEntity.BasicField> sortable = EnumSet.of(
                TwinEntity.BasicField.NAME, TwinEntity.BasicField.DESCRIPTION,
                TwinEntity.BasicField.EXTERNAL_ID, TwinEntity.BasicField.SORT_ORDER,
                TwinEntity.BasicField.CREATED_AT, TwinEntity.BasicField.FLAVOR_DATA_LIST_OPTION_ID,
                TwinEntity.BasicField.TWIN_CLASS_ID, TwinEntity.BasicField.TWIN_STATUS_ID,
                TwinEntity.BasicField.OWNER_USER_ID, TwinEntity.BasicField.ASSIGNEE_USER_ID,
                TwinEntity.BasicField.CREATOR_USER_ID);

        for (TwinEntity.BasicField field : TwinEntity.BasicField.values()) {
            assertEquals(sortable.contains(field), field.isSortable(), field.name());
        }
    }
}
