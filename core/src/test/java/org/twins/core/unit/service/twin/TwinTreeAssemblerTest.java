package org.twins.core.unit.service.twin;

import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.domain.twin.TwinGroupKeyKind;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.service.twin.TwinTreeAssembler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Test
    void nestsStatusAssigneeAndLinkAndCountsALeafOnceInTheParents() {
        TwinEntity task = task(1, STATUS, ANN);
        TwinEntity other = task(2, OTHER_STATUS, BOB);
        List<TwinGroup> roots = TwinTreeAssembler.assemble(List.of(
                leaf(task, List.of(STATUS), List.of(ANN), List.of(DST_1, DST_2)),
                leaf(other, List.of(OTHER_STATUS), List.of(BOB), List.of())
        ), levels());

        roots.sort(TwinTreeAssembler.keys(true));
        assertEquals(List.of(STATUS, OTHER_STATUS), keys(roots));
        assertEquals(1, roots.get(0).getCount());
        assertEquals(2, roots.get(0).getGroups().stream().flatMap(group -> group.getGroups().stream()).mapToInt(TwinGroup::getCount).sum());
        assertEquals(List.of(task.getId(), task.getId()), roots.get(0).getGroups().get(0).getGroups().stream()
                .flatMap(group -> group.getTwins().stream()).map(TwinEntity::getId).toList());
        assertNull(roots.get(1).getGroups().get(0).getGroups().get(0).getKey());
    }

    @Test
    void oneCriterionPutsTwinsOnTheRoot() {
        TwinEntity ann = task(1, STATUS, ANN);
        TwinEntity unassigned = task(2, STATUS, null);
        TwinGroupLevel assignee = new TwinGroupLevel()
                .setFieldId(ANN)
                .setKeyKind(TwinGroupKeyKind.ASSIGNEE_USER)
                .setSort(TwinGroupSort.NAME_ASC);
        List<TwinGroup> roots = TwinTreeAssembler.assemble(List.of(
                leaf(ann, List.of(ANN)),
                leaf(unassigned, List.of())
        ), List.of(assignee));
        roots.sort(TwinTreeAssembler.names(Map.of(ANN, "Ann"), true));
        assertEquals(Arrays.asList(ANN, null), keys(roots));
        assertEquals(List.of(ann.getId()), roots.get(0).getTwins().stream().map(TwinEntity::getId).toList());
        assertNull(roots.get(0).getGroups());
        assertEquals(1, roots.get(1).getCount());
    }

    @Test
    void rootLinkCountsTheSameLeafInEachRoot() {
        TwinEntity task = task(1, STATUS, ANN);
        TwinGroupLevel link = new TwinGroupLevel()
                .setFieldId(LINK)
                .setKeyKind(TwinGroupKeyKind.DYNAMIC)
                .setSort(TwinGroupSort.KEY_ASC);
        List<TwinGroup> roots = TwinTreeAssembler.assemble(List.of(
                leaf(task, List.of(DST_1, DST_2))
        ), List.of(link));
        assertEquals(2, roots.size());
        assertEquals(1, roots.get(0).getCount());
        assertEquals(1, roots.get(1).getCount());
        assertEquals(2, roots.stream().mapToInt(TwinGroup::getCount).sum());
    }

    @Test
    void duplicateLeavesAreKeptOnceAndPreserveMemberOrder() {
        TwinEntity first = task(1, STATUS, BOB);
        TwinEntity second = task(2, STATUS, ANN);
        TwinEntity third = task(3, STATUS, BOB);
        List<TwinGroup> roots = TwinTreeAssembler.assemble(List.of(
                leaf(first, List.of(BOB)),
                leaf(second, List.of(ANN)),
                leaf(first, List.of(BOB)),
                leaf(third, List.of(BOB))
        ), List.of(new TwinGroupLevel().setFieldId(ANN).setKeyKind(TwinGroupKeyKind.ASSIGNEE_USER)));

        assertEquals(2, roots.size());
        assertEquals(BOB, roots.get(0).getKey());
        assertEquals(List.of(first.getId(), third.getId()),
                roots.get(0).getTwins().stream().map(TwinEntity::getId).toList());
        assertEquals(2, roots.get(0).getCount());
        assertEquals(List.of(second.getId()),
                roots.get(1).getTwins().stream().map(TwinEntity::getId).toList());
    }

    @Test
    void nullKeyIsLastForKeyAndNameSortInBothDirections() {
        TwinEntity unassigned = task(1, STATUS, null);
        TwinEntity ann = task(2, STATUS, ANN);
        TwinEntity bob = task(3, STATUS, BOB);
        List<TwinGroup> groups = TwinTreeAssembler.assemble(List.of(
                leaf(unassigned, List.of()),
                leaf(ann, List.of(ANN)),
                leaf(bob, List.of(BOB))
        ), List.of(new TwinGroupLevel().setFieldId(ANN).setKeyKind(TwinGroupKeyKind.ASSIGNEE_USER)));

        groups.sort(TwinTreeAssembler.keys(true));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
        groups.sort(TwinTreeAssembler.keys(false));
        assertEquals(Arrays.asList(BOB, ANN, null), keys(groups));

        Map<UUID, String> names = Map.of(ANN, "Ann", BOB, "Bob");
        groups.sort(TwinTreeAssembler.names(names, true));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
        groups.sort(TwinTreeAssembler.names(names, false));
        assertEquals(Arrays.asList(BOB, ANN, null), keys(groups));

        groups.sort(TwinTreeAssembler.names(Map.of(ANN, "Ann", BOB, "Ann"), false));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
    }

    @Test
    void countSortUsesSizeThenKeyAndKeepsALargerEmptyGroupFirst() {
        TwinGroupLevel assignee = new TwinGroupLevel().setFieldId(ANN).setKeyKind(TwinGroupKeyKind.ASSIGNEE_USER);
        TwinEntity unassigned1 = task(1, STATUS, null);
        TwinEntity unassigned2 = task(4, STATUS, null);
        TwinEntity ann = task(2, STATUS, ANN);
        List<TwinGroup> groups = TwinTreeAssembler.assemble(List.of(
                leaf(unassigned1, List.of()),
                leaf(ann, List.of(ANN)),
                leaf(unassigned2, List.of())
        ), List.of(assignee));
        groups.sort(TwinTreeAssembler.counts(false));
        assertEquals(Arrays.asList(null, ANN), keys(groups));

        TwinEntity bob = task(3, STATUS, BOB);
        groups = TwinTreeAssembler.assemble(List.of(
                leaf(ann, List.of(ANN)),
                leaf(bob, List.of(BOB))
        ), List.of(assignee));
        groups.sort(TwinTreeAssembler.counts(false));
        assertEquals(List.of(ANN, BOB), keys(groups));
    }

    private static List<TwinGroupLevel> levels() {
        return List.of(
                new TwinGroupLevel().setFieldId(STATUS).setKeyKind(TwinGroupKeyKind.STATUS).setSort(TwinGroupSort.KEY_ASC),
                new TwinGroupLevel().setFieldId(ANN).setKeyKind(TwinGroupKeyKind.ASSIGNEE_USER).setSort(TwinGroupSort.NAME_ASC),
                new TwinGroupLevel().setFieldId(LINK).setKeyKind(TwinGroupKeyKind.DYNAMIC).setValueType(org.twins.core.enums.twin.TwinGroupValueType.TWIN).setSort(TwinGroupSort.NAME_ASC)
        );
    }

    @SafeVarargs
    private static TwinTreeAssembler.Leaf leaf(TwinEntity twin, List<?>... levels) {
        List<List<Object>> keys = new java.util.ArrayList<>();
        for (List<?> level : levels) {
            keys.add(new java.util.ArrayList<>(level));
        }
        return new TwinTreeAssembler.Leaf(twin, keys);
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
