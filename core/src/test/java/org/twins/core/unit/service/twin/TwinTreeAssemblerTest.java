package org.twins.core.unit.service.twin;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueStatus;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;
import org.twins.core.service.twin.TwinTreeAssembler;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TwinTreeAssemblerTest {
    private static final UUID STATUS = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID OTHER_STATUS = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
    private static final UUID ANN = UUID.fromString("00000000-0000-0000-0000-0000000000b1");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final UUID LINK = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    private static final UUID DST_1 = UUID.fromString("00000000-0000-0000-0000-0000000000d1");
    private static final UUID DST_2 = UUID.fromString("00000000-0000-0000-0000-0000000000d2");

    private static final TwinClassFieldEntity STATUS_FIELD = new TwinClassFieldEntity().setId(UUID.randomUUID());
    private static final TwinClassFieldEntity ASSIGNEE_FIELD = new TwinClassFieldEntity().setId(UUID.randomUUID());
    private static final TwinClassFieldEntity LINK_FIELD = new TwinClassFieldEntity().setId(LINK);

    @Test
    void nestsStatusAssigneeAndLinkAndCountsALeafOnceInTheParents() throws Exception {
        TwinEntity task = withLinks(task(1, STATUS, ANN), DST_1, DST_2);
        TwinEntity other = task(2, OTHER_STATUS, BOB);
        List<TwinGroup> roots = TwinTreeAssembler.assemble(List.of(task, other), levels(), values(Map.of())).roots();

        roots.sort(TwinTreeAssembler.keys(true));
        assertEquals(List.of(STATUS, OTHER_STATUS), keys(roots));
        assertEquals(1, roots.get(0).getCount());
        assertEquals(2, roots.get(0).getGroups().stream().flatMap(group -> group.getGroups().stream()).mapToInt(TwinGroup::getCount).sum());
        assertEquals(List.of(task.getId(), task.getId()), roots.get(0).getGroups().get(0).getGroups().stream()
                .flatMap(group -> group.getTwins().stream()).map(TwinEntity::getId).toList());
        assertNull(roots.get(1).getGroups().get(0).getGroups().get(0).getKey());
    }

    @Test
    void oneCriterionPutsTwinsOnTheRoot() throws Exception {
        TwinEntity ann = task(1, STATUS, ANN);
        TwinEntity unassigned = task(2, STATUS, null);
        List<TwinGroupLevel> levels = List.of(assigneeLevel());
        List<TwinGroup> roots = TwinTreeAssembler.assemble(List.of(ann, unassigned), levels, values(Map.of())).roots();

        TwinTreeAssembler.sort(roots, levels);
        assertEquals(Arrays.asList(ANN, null), keys(roots));
        assertEquals(List.of(ann.getId()), roots.get(0).getTwins().stream().map(TwinEntity::getId).toList());
        assertNull(roots.get(0).getGroups());
        assertEquals(1, roots.get(1).getCount());
    }

    @Test
    void rootLinkCountsTheSameLeafInEachRoot() throws Exception {
        TwinEntity task = withLinks(task(1, STATUS, ANN), DST_1, DST_2);
        List<TwinGroup> roots = TwinTreeAssembler.assemble(List.of(task), List.of(linkLevel()), values(Map.of())).roots();

        assertEquals(2, roots.size());
        assertEquals(1, roots.get(0).getCount());
        assertEquals(1, roots.get(1).getCount());
        assertEquals(2, roots.stream().mapToInt(TwinGroup::getCount).sum());
    }

    @Test
    void duplicateLeavesAreKeptOnceAndPreserveMemberOrder() throws Exception {
        TwinEntity first = task(1, STATUS, BOB);
        TwinEntity second = task(2, STATUS, ANN);
        TwinEntity third = task(3, STATUS, BOB);
        List<TwinGroup> roots = TwinTreeAssembler.assemble(
                List.of(first, second, first, third), List.of(assigneeLevel()), values(Map.of())).roots();

        assertEquals(2, roots.size());
        assertEquals(BOB, roots.get(0).getKey());
        assertEquals(List.of(first.getId(), third.getId()),
                roots.get(0).getTwins().stream().map(TwinEntity::getId).toList());
        assertEquals(2, roots.get(0).getCount());
        assertEquals(List.of(second.getId()),
                roots.get(1).getTwins().stream().map(TwinEntity::getId).toList());
    }

    @Test
    void nullKeyIsLastForKeyAndNameSortInBothDirections() throws Exception {
        TwinEntity unassigned = task(1, STATUS, null);
        TwinEntity ann = task(2, STATUS, ANN);
        TwinEntity bob = task(3, STATUS, BOB);
        List<TwinGroupLevel> levels = List.of(assigneeLevel());
        List<TwinGroup> groups = TwinTreeAssembler.assemble(List.of(unassigned, ann, bob), levels,
                values(Map.of(ANN, "Ann", BOB, "Bob"))).roots();

        TwinTreeAssembler.sort(groups, List.of(assigneeLevel().setSort(TwinGroupSort.KEY_ASC)));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
        TwinTreeAssembler.sort(groups, List.of(assigneeLevel().setSort(TwinGroupSort.KEY_DESC)));
        assertEquals(Arrays.asList(BOB, ANN, null), keys(groups));
        TwinTreeAssembler.sort(groups, List.of(assigneeLevel().setSort(TwinGroupSort.NAME_ASC)));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
        TwinTreeAssembler.sort(groups, List.of(assigneeLevel().setSort(TwinGroupSort.NAME_DESC)));
        assertEquals(Arrays.asList(BOB, ANN, null), keys(groups));

        // equal names fall back to the key
        groups = TwinTreeAssembler.assemble(List.of(unassigned, ann, bob), levels,
                values(Map.of(ANN, "Ann", BOB, "Ann"))).roots();
        TwinTreeAssembler.sort(groups, List.of(assigneeLevel().setSort(TwinGroupSort.NAME_DESC)));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
    }

    @Test
    void countSortUsesSizeThenKeyAndKeepsALargerEmptyGroupFirst() throws Exception {
        TwinEntity unassigned1 = task(1, STATUS, null);
        TwinEntity unassigned2 = task(4, STATUS, null);
        TwinEntity ann = task(2, STATUS, ANN);
        List<TwinGroup> groups = TwinTreeAssembler.assemble(
                List.of(unassigned1, ann, unassigned2), List.of(assigneeLevel()), values(Map.of())).roots();
        groups.sort(TwinTreeAssembler.counts(false));
        assertEquals(Arrays.asList(null, ANN), keys(groups));

        TwinEntity bob = task(3, STATUS, BOB);
        groups = TwinTreeAssembler.assemble(List.of(ann, bob), List.of(assigneeLevel()), values(Map.of())).roots();
        groups.sort(TwinTreeAssembler.counts(false));
        assertEquals(List.of(ANN, BOB), keys(groups));
    }

    private static List<TwinGroupLevel> levels() {
        return List.of(statusLevel(), assigneeLevel(), linkLevel());
    }

    private static TwinGroupLevel statusLevel() {
        return new TwinGroupLevel()
                .setField(STATUS_FIELD)
                .setSort(TwinGroupSort.KEY_ASC);
    }

    private static TwinGroupLevel assigneeLevel() {
        return new TwinGroupLevel()
                .setField(ASSIGNEE_FIELD)
                .setSort(TwinGroupSort.NAME_ASC);
    }

    private static TwinGroupLevel linkLevel() {
        return new TwinGroupLevel()
                .setField(LINK_FIELD)
                .setSort(TwinGroupSort.NAME_ASC);
    }

    /**
     * Stands in for TwinFieldService.getFieldValue: base columns become single values (the user
     * names simulate the loaded key entities), the link field reads the kit.
     */
    private static TwinTreeAssembler.FieldValueGetter values(Map<UUID, String> userNames) {
        return (twin, field) -> {
            if (field == STATUS_FIELD) {
                return twin.getTwinStatusId() == null
                        ? new FieldValueStatus(field)
                        : new FieldValueStatus(field).setValue(new TwinStatusEntity().setId(twin.getTwinStatusId()));
            }
            if (field == ASSIGNEE_FIELD) {
                return twin.getAssignerUserId() == null
                        ? new FieldValueUserSingle(field)
                        : new FieldValueUserSingle(field).setValue(
                        new UserEntity().setId(twin.getAssignerUserId()).setName(userNames.get(twin.getAssignerUserId())));
            }
            return twin.getFieldValuesKit() == null ? new FieldValueLink(field) : twin.getFieldValuesKit().get(field.getId());
        };
    }

    private static TwinEntity withLinks(TwinEntity twin, UUID... dstIds) {
        List<TwinEntity> items = new ArrayList<>();
        for (UUID dstId : dstIds) {
            items.add(new TwinEntity().setId(dstId));
        }
        twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
        twin.getFieldValuesKit().add(new FieldValueLink(LINK_FIELD)
                .setForwardLink(true)
                .setItems(items));
        return twin;
    }

    private static TwinEntity task(int suffix, UUID statusId, UUID assignerUserId) {
        return new TwinEntity()
                .setId(UUID.fromString("00000000-0000-0000-0000-00000000000" + suffix))
                .setTwinStatusId(statusId)
                .setAssignerUserId(assignerUserId);
    }

    private static List<UUID> keys(List<TwinGroup> groups) {
        return groups.stream().map(group -> (UUID) group.getKey()).toList();
    }
}
