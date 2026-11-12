package org.twins.core.unit.service.twin;

import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.service.twin.TwinGroupAssembler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TwinGroupAssemblerTest {
    private static final UUID ANN = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    @Test
    void keepsLeafOrderAndCountsEveryTwinOnce() throws Exception {
        TwinEntity first = task(uuid(1), BOB);
        TwinEntity second = task(uuid(2), ANN);
        TwinEntity third = task(uuid(3), BOB);
        List<TwinGroup> groups = TwinGroupAssembler.assemble(List.of(first, second, first, third),
                twin -> Collections.singletonList(twin.getAssignerUserId()));

        assertEquals(2, groups.size());
        assertEquals(List.of(first.getId(), third.getId()), ids(groups.get(0)));
        assertEquals(2, groups.get(0).getTwins().size());
        assertEquals(List.of(second.getId()), ids(groups.get(1)));
        assertEquals(3, groups.stream().mapToInt(group -> group.getTwins().size()).sum());
    }

    @Test
    void putsNullKeyLastForKeyAndNameSortInBothDirections() throws Exception {
        TwinEntity unassigned = task(uuid(1), null);
        TwinEntity ann = task(uuid(2), ANN);
        TwinEntity bob = task(uuid(3), BOB);
        List<TwinGroup> groups = TwinGroupAssembler.assemble(List.of(unassigned, ann, bob),
                twin -> Collections.singletonList(twin.getAssignerUserId()));

        groups.sort(TwinGroupAssembler.keys(true));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
        groups.sort(TwinGroupAssembler.keys(false));
        assertEquals(Arrays.asList(BOB, ANN, null), keys(groups));

        Map<UUID, String> names = Map.of(ANN, "Ann", BOB, "Bob");
        groups.sort(TwinGroupAssembler.names(names, true));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
        groups.sort(TwinGroupAssembler.names(names, false));
        assertEquals(Arrays.asList(BOB, ANN, null), keys(groups));

        groups.sort(TwinGroupAssembler.names(Map.of(ANN, "Ann", BOB, "Ann"), false));
        assertEquals(Arrays.asList(ANN, BOB, null), keys(groups));
    }

    @Test
    void countSortUsesSizeThenKeyAndKeepsALargerEmptyGroupFirst() throws Exception {
        TwinEntity unassigned = task(uuid(1), null);
        TwinEntity alsoUnassigned = task(uuid(4), null);
        TwinEntity ann = task(uuid(2), ANN);
        List<TwinGroup> groups = TwinGroupAssembler.assemble(List.of(unassigned, ann, alsoUnassigned),
                twin -> Collections.singletonList(twin.getAssignerUserId()));
        groups.sort(TwinGroupAssembler.counts(false));
        assertEquals(Arrays.asList(null, ANN), keys(groups));

        TwinEntity bob = task(uuid(3), BOB);
        groups = TwinGroupAssembler.assemble(List.of(ann, bob), twin -> List.of(twin.getAssignerUserId()));
        groups.sort(TwinGroupAssembler.counts(false));
        assertEquals(List.of(ANN, BOB), keys(groups));
    }

    @Test
    void multipleValuesPutTheTwinInEachGroupOnce() throws Exception {
        TwinEntity task = task(uuid(1), ANN);
        List<TwinGroup> groups = TwinGroupAssembler.assemble(List.of(task),
                twin -> List.of(ANN, BOB, ANN));
        assertEquals(2, groups.size());
        assertEquals(1, groups.get(0).getTwins().size());
        assertEquals(1, groups.get(1).getTwins().size());
        assertEquals(2, groups.stream().mapToInt(group -> group.getTwins().size()).sum());
    }

    @Test
    void missingValueIsOneNullGroup() throws Exception {
        TwinEntity task = task(uuid(1), ANN);
        List<TwinGroup> groups = TwinGroupAssembler.assemble(List.of(task), twin -> List.of());
        assertEquals(1, groups.size());
        assertEquals(null, groups.get(0).getKey());
        assertEquals(List.of(task.getId()), ids(groups.get(0)));
    }

    private static TwinEntity task(UUID id, UUID assignerUserId) {
        return new TwinEntity().setId(id).setAssignerUserId(assignerUserId);
    }

    private static UUID uuid(int suffix) {
        return UUID.fromString("00000000-0000-0000-0000-00000000000" + suffix);
    }

    private static List<UUID> ids(TwinGroup group) {
        return group.getTwins().stream().map(TwinEntity::getId).toList();
    }

    private static List<UUID> keys(List<TwinGroup> groups) {
        return groups.stream().map(group -> (UUID) group.getKey()).toList();
    }
}
