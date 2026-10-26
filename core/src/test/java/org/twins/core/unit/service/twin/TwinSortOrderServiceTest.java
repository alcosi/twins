package org.twins.core.unit.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.cambium.common.kit.DuplicateKeyMode;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.twinlink.TwinLinkUpdate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.status.StatusType;
import org.twins.core.service.twin.TwinSortOrderService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Algorithm checks for TWINS-727 lexicographic sort keys (docs/backlog/067):
 * renumber monotonicity for large levels, sequential batch semantics, insert-first on an
 * exhausted bottom edge, key length cap, duplicate keys tolerance, twin_link level.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TwinSortOrderServiceTest {
    private static final UUID HEAD = UUID.nameUUIDFromBytes("head".getBytes());

    @Mock
    TwinRepository twinRepository;
    @Mock
    TwinLinkRepository twinLinkRepository;
    @Mock
    EntityManager entityManager;
    @Mock
    Query lockQuery;

    TwinSortOrderService service;

    @BeforeEach
    void setUp() {
        service = new TwinSortOrderService(twinRepository, twinLinkRepository);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(lockQuery);
        when(lockQuery.setParameter(anyString(), any())).thenReturn(lockQuery);
    }

    private static TwinEntity twin(int i, String sortOrder) {
        TwinEntity twin = new TwinEntity();
        twin.setId(UUID.nameUUIDFromBytes(("twin-" + i).getBytes()));
        twin.setHeadTwinId(HEAD);
        twin.setSortOrder(sortOrder);
        // newest first (mirror of the repository ORDER BY): twin(0) sorts before twin(1) ...
        twin.setCreatedAt(new java.sql.Timestamp(2_000_000_000L - i));
        // TwinUpdate.setDbTwinEntity probes isSketch - keep the status loaded and not a sketch
        twin.setTwinStatusId(UUID.nameUUIDFromBytes(("status-" + i).getBytes()));
        twin.setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        return twin;
    }

    private static TwinUpdate move(TwinEntity moved, UUID afterTwinId) {
        return new TwinUpdate().setDbTwinEntity(moved).setAfterTwinId(afterTwinId);
    }

    private static TwinLinkEntity link(int i, UUID srcTwinId, String sortOrder) {
        TwinLinkEntity link = new TwinLinkEntity();
        link.setId(UUID.nameUUIDFromBytes(("link-" + i).getBytes()));
        link.setSrcTwinId(srcTwinId);
        link.setDstTwinId(HEAD);
        link.setLinkId(UUID.nameUUIDFromBytes("link-type".getBytes()));
        link.setSortOrder(sortOrder);
        return link;
    }

    private static Kit<TwinUpdate, UUID> movesKit(TwinUpdate... moves) {
        Kit<TwinUpdate, UUID> kit = new Kit<>(TwinSortOrderService.TWIN_UPDATE_KEY, DuplicateKeyMode.REPLACE);
        kit.addAll(Arrays.asList(moves));
        return kit;
    }

    /** sibling ids ordered by their current sort key (nulls keep list position at the end) */
    private static List<UUID> orderedIds(List<TwinEntity> siblings) {
        return siblings.stream()
                .sorted((a, b) -> {
                    String ka = a.getSortOrder(), kb = b.getSortOrder();
                    if (ka == null) return kb == null ? 0 : 1;
                    if (kb == null) return -1;
                    int c = ka.compareTo(kb);
                    return c != 0 ? c : a.getId().compareTo(b.getId());
                })
                .map(TwinEntity::getId)
                .collect(Collectors.toList());
    }

    private void stubLevel(List<TwinEntity> siblings) {
        when(twinRepository.findHeadSiblingsOrderedIn(any())).thenReturn(new ArrayList<>(siblings));
    }

    @Test
    void initRenumberIsMonotonicForLevelsOver26() throws Exception {
        List<TwinEntity> siblings = new ArrayList<>();
        for (int i = 0; i < 30; i++)
            siblings.add(twin(i, null));
        stubLevel(siblings);
        // move twin[5] after twin[10]
        service.reorderTwins(movesKit(move(siblings.get(5), siblings.get(10).getId())));
        List<UUID> expected = new ArrayList<>();
        for (int i = 0; i < 30; i++)
            if (i != 5)
                expected.add(siblings.get(i).getId());
        expected.add(expected.indexOf(siblings.get(10).getId()) + 1, siblings.get(5).getId()); // right after twin[10]
        assertEquals(expected, orderedIds(siblings));
        // fixed width renumber for the level; the moved twin got a midpoint key, may be shorter
        for (int i = 0; i < 30; i++)
            if (i != 5)
                assertEquals(2, siblings.get(i).getSortOrder().length());
        verify(twinRepository).saveAll(any());
    }

    @Test
    void batchMovesSeeEachOtherKeysSequentially() throws Exception {
        TwinEntity a = twin(0, "1"), b = twin(1, "2"), c = twin(2, "3");
        stubLevel(Arrays.asList(a, b, c));
        // swap: A after B, then B after A -> final order A, B, C
        service.reorderTwins(movesKit(move(a, b.getId()), move(b, a.getId())));
        assertEquals(List.of(a.getId(), b.getId(), c.getId()), orderedIds(Arrays.asList(a, b, c)));
    }

    @Test
    void insertFirstOnExhaustedBottomEdgeRenumbers() throws Exception {
        TwinEntity x = twin(0, "0"), y = twin(1, "1"), moved = twin(2, null);
        stubLevel(Arrays.asList(x, y, moved));
        service.reorderTwins(movesKit(move(moved, UuidUtils.NULLIFY_MARKER)));
        // level renumbered ("0" min leaves no key below), moved lands strictly before the old minimum
        assertEquals(List.of(moved.getId(), x.getId(), y.getId()), orderedIds(Arrays.asList(x, y, moved)));
    }

    @Test
    void insertFirstUsesLexicographicPredecessor() throws Exception {
        TwinEntity x = twin(0, "04"), y = twin(1, "1a"), moved = twin(2, null);
        stubLevel(Arrays.asList(x, y, moved));
        service.reorderTwins(movesKit(move(moved, UuidUtils.NULLIFY_MARKER)));
        assertEquals("03", moved.getSortOrder()); // predecessor of "04" without touching the level
        assertEquals(List.of(moved.getId(), x.getId(), y.getId()), orderedIds(Arrays.asList(x, y, moved)));
    }

    @Test
    void keyLengthCapTriggersRenumber() throws Exception {
        TwinEntity a = twin(0, "a"), b = twin(1, "aVVVVVVV"), c = twin(2, "b"); // "b" after "aV...V" append would grow to 9
        stubLevel(Arrays.asList(a, b, c));
        service.reorderTwins(movesKit(move(c, b.getId())));
        // append key exceeded MAX_KEY_LENGTH -> level renumbered ("F","U"), c got one-digit midpoint
        assertEquals("UV", c.getSortOrder());
        assertEquals(1, a.getSortOrder().length());
        assertEquals(List.of(a.getId(), b.getId(), c.getId()), orderedIds(Arrays.asList(a, b, c)));
    }

    @Test
    void duplicateKeysAreTolerated() throws Exception {
        TwinEntity a = twin(0, "a"), b = twin(1, "a"), c = twin(2, "b");
        stubLevel(Arrays.asList(a, b, c));
        service.reorderTwins(movesKit(move(c, a.getId()))); // next non-moved after anchor "a" is duplicate "a"
        assertEquals("aV", c.getSortOrder()); // between the duplicates and "b"
    }

    @Test
    void moveOfUnorderedTwinKeepsOrderedKeysUntouched() throws Exception {
        TwinEntity a = twin(0, "b"), b = twin(1, "d"), moved = twin(2, null); // moved: fresh, never ordered
        stubLevel(Arrays.asList(a, b, moved));
        service.reorderTwins(movesKit(move(moved, a.getId())));
        assertEquals("c", moved.getSortOrder()); // plain midpoint, no renumber
        assertEquals("b", a.getSortOrder());
        assertEquals("d", b.getSortOrder());
    }

    @Test
    void twinLinkBatchMove() throws Exception {
        UUID src1 = UUID.nameUUIDFromBytes("src-1".getBytes());
        UUID src2 = UUID.nameUUIDFromBytes("src-2".getBytes());
        TwinLinkEntity l1 = link(0, src1, null), l2 = link(1, src2, null);
        when(twinLinkRepository.findDstLinkLevelOrdered(HEAD, l1.getLinkId())).thenReturn(new ArrayList<>(Arrays.asList(l1, l2)));
        Kit<TwinLinkUpdate, UUID> moves = new Kit<>(TwinSortOrderService.TWIN_LINK_UPDATE_KEY, DuplicateKeyMode.REPLACE);
        TwinLinkUpdate move = new TwinLinkUpdate();
        move.setTwinLink(l1);
        move.setAfterTwinId(l2.getSrcTwinId()); // place src1 after src2 -> l1 goes last
        moves.add(move);
        service.reorderTwinLinks(moves);
        // level init renumber: l1(pos0)->"K", l2(pos1)->"e"; l1 after l2 -> midpoint("e", no next) = "eV"
        assertEquals("eV", l1.getSortOrder());
        assertEquals("e", l2.getSortOrder());
        verify(twinLinkRepository).saveAll(any());
    }
}
