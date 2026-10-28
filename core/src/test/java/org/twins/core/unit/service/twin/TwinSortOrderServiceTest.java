package org.twins.core.unit.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.dao.twin.TwinLinkRepository.TwinLinkSortOrderView;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinRepository.SortOrderBounds;
import org.twins.core.dao.twin.TwinRepository.TwinRootSortOrderView;
import org.twins.core.dao.twin.TwinRepository.TwinSortOrderView;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.twinlink.TwinLinkUpdate;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.status.StatusType;
import org.twins.core.service.twin.TwinSortOrderService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Algorithm checks for the lexicographic sort keys (docs/backlog/067):
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

    /** created twin: transient, no db row yet (no createdAt, no status - what the create flow builds) */
    private static TwinEntity created(int i) {
        TwinEntity twin = new TwinEntity();
        twin.setId(UUID.nameUUIDFromBytes(("created-" + i).getBytes()));
        twin.setHeadTwinId(HEAD);
        return twin;
    }

    private static final UUID ROOT_CLASS = UUID.nameUUIDFromBytes("root-class".getBytes());
    private static final UUID ROOT_BA = UUID.nameUUIDFromBytes("root-ba".getBytes());

    /** headless root twin of the (ROOT_CLASS, ROOT_BA) level */
    private static TwinEntity root(int i, String sortOrder) {
        TwinEntity twin = twin(i, sortOrder);
        twin.setHeadTwinId(null);
        twin.setTwinClassId(ROOT_CLASS);
        twin.setOwnerBusinessAccountId(ROOT_BA);
        return twin;
    }

    /** transient created headless root */
    private static TwinEntity rootCreated(int i) {
        TwinEntity twin = created(i);
        twin.setHeadTwinId(null);
        twin.setTwinClassId(ROOT_CLASS);
        twin.setOwnerBusinessAccountId(ROOT_BA);
        return twin;
    }

    private static TwinCreate createMove(TwinEntity created, UUID afterTwinId) {
        TwinCreate twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(created);
        twinCreate.setAfterTwinId(afterTwinId);
        return twinCreate;
    }

    private static Kit<TwinCreate, UUID> createMovesKit(TwinCreate... moves) {
        Kit<TwinCreate, UUID> kit = new Kit<>(TwinSortOrderService.TWIN_CREATE_KEY, DuplicateKeyMode.REPLACE);
        kit.addAll(Arrays.asList(moves));
        return kit;
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

    /** emulates the db for both reorder mechanisms: full level load, anchor projections, min/next bounds */
    private void stubLevel(List<TwinEntity> siblings) {
        when(twinRepository.findHeadSiblingsOrderedIn(any())).thenReturn(new ArrayList<>(siblings));
        when(twinRepository.findSortOrderViewByIdIn(any())).thenAnswer(inv -> {
            java.util.Collection<UUID> ids = inv.getArgument(0);
            return ids.stream()
                    .flatMap(id -> siblings.stream().filter(t -> t.getId().equals(id)))
                    .<TwinSortOrderView>map(TwinSortOrderViewImpl::new)
                    .toList();
        });
        when(twinRepository.findSortOrderBounds(any(), any(), any())).thenAnswer(inv -> {
            UUID head = inv.getArgument(0);
            String afterKey = inv.getArgument(1);
            java.util.Collection<UUID> excludeIds = inv.getArgument(2);
            String minKey = null, nextKey = null;
            for (TwinEntity t : siblings) {
                if (!head.equals(t.getHeadTwinId()) || t.getSortOrder() == null || excludeIds.contains(t.getId()))
                    continue;
                if (minKey == null || t.getSortOrder().compareTo(minKey) < 0)
                    minKey = t.getSortOrder();
                if (t.getSortOrder().compareTo(afterKey) > 0 && (nextKey == null || t.getSortOrder().compareTo(nextKey) < 0))
                    nextKey = t.getSortOrder();
            }
            return new SortOrderBoundsImpl(minKey, nextKey);
        });
    }

    private record TwinSortOrderViewImpl(TwinEntity t) implements TwinRepository.TwinSortOrderView {
        @Override
        public UUID getId() {
            return t.getId();
        }

        @Override
        public UUID getHeadTwinId() {
            return t.getHeadTwinId();
        }

        @Override
        public String getSortOrder() {
            return t.getSortOrder();
        }
    }

    private record TwinRootSortOrderViewImpl(TwinEntity t) implements TwinRepository.TwinRootSortOrderView {
        @Override
        public UUID getId() {
            return t.getId();
        }

        @Override
        public UUID getTwinClassId() {
            return t.getTwinClassId();
        }

        @Override
        public UUID getOwnerBusinessAccountId() {
            return t.getOwnerBusinessAccountId();
        }

        @Override
        public String getSortOrder() {
            return t.getSortOrder();
        }
    }

    /** emulates the db for the (ROOT_CLASS, ROOT_BA) root level: full load, anchor projections, bounds */
    private void stubRootLevel(List<TwinEntity> siblings) {
        when(twinRepository.findRootSiblingsOrdered(ROOT_CLASS, ROOT_BA)).thenReturn(new ArrayList<>(siblings));
        when(twinRepository.findRootSortOrderViewByIdIn(any())).thenAnswer(inv -> {
            java.util.Collection<UUID> ids = inv.getArgument(0);
            return ids.stream()
                    .flatMap(id -> siblings.stream().filter(t -> t.getId().equals(id)))
                    .<TwinRootSortOrderView>map(TwinRootSortOrderViewImpl::new)
                    .toList();
        });
        when(twinRepository.findRootSortOrderBounds(org.mockito.ArgumentMatchers.eq(ROOT_CLASS), org.mockito.ArgumentMatchers.eq(ROOT_BA), any(), any())).thenAnswer(inv -> {
            String afterKey = inv.getArgument(2);
            java.util.Collection<UUID> excludeIds = inv.getArgument(3);
            String minKey = null, nextKey = null;
            for (TwinEntity t : siblings) {
                if (t.getSortOrder() == null || excludeIds.contains(t.getId()))
                    continue;
                if (minKey == null || t.getSortOrder().compareTo(minKey) < 0)
                    minKey = t.getSortOrder();
                if (t.getSortOrder().compareTo(afterKey) > 0 && (nextKey == null || t.getSortOrder().compareTo(nextKey) < 0))
                    nextKey = t.getSortOrder();
            }
            return new SortOrderBoundsImpl(minKey, nextKey);
        });
    }

    private record SortOrderBoundsImpl(String minKey, String nextKey) implements TwinRepository.SortOrderBounds {
        @Override
        public String getMinKey() {
            return minKey;
        }

        @Override
        public String getNextKey() {
            return nextKey;
        }
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
    void partialLightKeysSurviveFullFallback() throws Exception {
        // move 1 succeeds on the light path (x got midpoint "2"), move 2 hits an exhausted bottom
        // edge ("0" has no predecessor) - the level falls back to the full path, which reloads it
        // (the stub emulates auto-flush: the reloaded rows are the same mutated instances) and
        // reapplies both moves before the renumber
        TwinEntity a = twin(0, "0"), b = twin(1, "5"), x = twin(2, "2"), y = twin(3, null);
        stubLevel(Arrays.asList(a, b, x, y));
        service.reorderTwins(movesKit(move(x, a.getId()), move(y, UuidUtils.NULLIFY_MARKER)));
        assertEquals(List.of(y.getId(), a.getId(), x.getId(), b.getId()), orderedIds(Arrays.asList(a, b, x, y)));
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
        when(twinLinkRepository.findSortOrderViewByLevelAndSrcIds(org.mockito.ArgumentMatchers.eq(HEAD), org.mockito.ArgumentMatchers.eq(l1.getLinkId()), any())).thenAnswer(inv -> {
            java.util.Collection<UUID> srcIds = inv.getArgument(2);
            return srcIds.stream()
                    .flatMap(src -> Arrays.asList(l1, l2).stream().filter(l -> l.getSrcTwinId().equals(src)))
                    .<TwinLinkSortOrderView>map(l -> new TwinLinkSortOrderView() {
                        @Override
                        public UUID getId() {
                            return l.getId();
                        }

                        @Override
                        public UUID getSrcTwinId() {
                            return l.getSrcTwinId();
                        }

                        @Override
                        public String getSortOrder() {
                            return l.getSortOrder();
                        }
                    })
                    .toList();
        });
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

    @Test
    void createIntoBrandNewLevelPlacesFirst() throws Exception {
        stubLevel(new ArrayList<>()); // no committed siblings yet
        TwinEntity x = created(0);
        service.reorderCreatedTwins(createMovesKit(createMove(x, UuidUtils.NULLIFY_MARKER)));
        // level initialization renumber, then the key strictly below the renumbered minimum (place-first
        // semantics - same as the update flow on an exhausted bottom edge)
        assertEquals("U", x.getSortOrder());
    }

    @Test
    void createAfterAnchorMidpointWithoutRenumber() throws Exception {
        TwinEntity a = twin(0, "1"), b = twin(1, "3");
        stubLevel(Arrays.asList(a, b));
        TwinEntity x = created(0);
        service.reorderCreatedTwins(createMovesKit(createMove(x, a.getId())));
        assertEquals("2", x.getSortOrder()); // midpoint between "1" and "3", committed keys untouched
        assertEquals("1", a.getSortOrder());
        assertEquals("3", b.getSortOrder());
        verify(twinRepository, never()).saveAll(any()); // light path: nothing to persist
    }

    @Test
    void createInsertFirstUsesLexicographicPredecessor() throws Exception {
        TwinEntity a = twin(0, "04");
        stubLevel(Arrays.asList(a));
        TwinEntity x = created(0);
        service.reorderCreatedTwins(createMovesKit(createMove(x, UuidUtils.NULLIFY_MARKER)));
        assertEquals("03", x.getSortOrder());
        assertEquals("04", a.getSortOrder());
        verify(twinRepository, never()).saveAll(any());
    }

    @Test
    void createBatchMovesSeeEachOtherKeys() throws Exception {
        TwinEntity a = twin(0, "1");
        stubLevel(Arrays.asList(a));
        TwinEntity x = created(0), y = created(1);
        service.reorderCreatedTwins(createMovesKit(
                createMove(x, UuidUtils.NULLIFY_MARKER), // x first, before "1"
                createMove(y, x.getId())));              // y right after x
        assertEquals("0", x.getSortOrder());
        assertEquals("0V", y.getSortOrder());
        assertEquals(List.of(x.getId(), y.getId(), a.getId()), orderedIds(Arrays.asList(a, x, y)));
        verify(twinRepository, never()).saveAll(any());
    }

    @Test
    void createOnUnorderedLevelRenumbersCommittedAndCreated() throws Exception {
        TwinEntity a = twin(0, null), b = twin(1, null), c = twin(2, null);
        stubLevel(Arrays.asList(a, b, c));
        TwinEntity x = created(0);
        service.reorderCreatedTwins(createMovesKit(createMove(x, b.getId())));
        // full path: whole level (committed + created) renumbered, x placed right after b
        assertEquals(List.of(a.getId(), b.getId(), x.getId(), c.getId()), orderedIds(Arrays.asList(a, b, c, x)));
        assertNotNull(a.getSortOrder());
        assertNotNull(c.getSortOrder());
        verify(twinRepository).saveAll(any()); // renumbered committed siblings are persisted
    }

    @Test
    void rootMovePlacesAfterAnchorOnLightPath() throws Exception {
        TwinEntity a = root(0, "1"), b = root(1, "3");
        stubRootLevel(Arrays.asList(a, b));
        service.reorderTwins(movesKit(move(a, b.getId())));
        assertEquals("3V", a.getSortOrder()); // append after the last root, no renumber
        assertEquals(List.of(b.getId(), a.getId()), orderedIds(Arrays.asList(a, b)));
        verify(twinRepository).saveAll(any()); // the moved root itself is persisted, siblings untouched
    }

    @Test
    void rootPlaceFirstUsesLexicographicPredecessor() throws Exception {
        TwinEntity x = root(0, "04"), y = root(1, "1a"), moved = root(2, null);
        stubRootLevel(Arrays.asList(x, y, moved));
        service.reorderTwins(movesKit(move(moved, UuidUtils.NULLIFY_MARKER)));
        assertEquals("03", moved.getSortOrder());
        assertEquals(List.of(moved.getId(), x.getId(), y.getId()), orderedIds(Arrays.asList(x, y, moved)));
    }

    @Test
    void rootUnorderedAnchorFallsBackToFullRenumber() throws Exception {
        TwinEntity a = root(0, null), b = root(1, null), c = root(2, null);
        stubRootLevel(Arrays.asList(a, b, c));
        service.reorderTwins(movesKit(move(a, b.getId())));
        // full path: the (class, BA) root level is initialized by a renumber, a lands right after b
        assertEquals(List.of(b.getId(), a.getId(), c.getId()), orderedIds(Arrays.asList(a, b, c)));
        assertNotNull(a.getSortOrder());
        assertNotNull(c.getSortOrder());
        verify(twinRepository).saveAll(any());
    }

    @Test
    void headlessRootWithoutBusinessAccountCanNotBeReordered() {
        TwinEntity x = new TwinEntity()
                .setId(UUID.nameUUIDFromBytes("root-no-ba".getBytes()))
                .setTwinClassId(ROOT_CLASS) // headless, no owner BA
                .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        assertThrows(ServiceException.class, () -> service.reorderTwins(movesKit(move(x, UuidUtils.NULLIFY_MARKER))));
    }

    @Test
    void createdHeadlessRootWithoutBusinessAccountIsSkipped() throws Exception {
        TwinEntity x = new TwinEntity()
                .setId(UUID.nameUUIDFromBytes("created-no-ba".getBytes()))
                .setTwinClassId(ROOT_CLASS); // headless, no owner BA
        service.reorderCreatedTwins(createMovesKit(createMove(x, UuidUtils.NULLIFY_MARKER)));
        assertNull(x.getSortOrder()); // the move is skipped, the key stays null
        verifyNoInteractions(twinRepository);
    }

    @Test
    void createIntoRootLevelAfterAnchorRidesWithInsert() throws Exception {
        TwinEntity a = root(0, "1"), b = root(1, "3");
        stubRootLevel(Arrays.asList(a, b));
        TwinEntity x = rootCreated(0);
        service.reorderCreatedTwins(createMovesKit(createMove(x, b.getId())));
        assertEquals("3V", x.getSortOrder()); // key assigned to the transient entity, INSERT carries it
        assertEquals("1", a.getSortOrder());
        verify(twinRepository, never()).saveAll(any());
    }

    @Test
    void mixedHeadAndRootBatchLocksAndMovesBothLevels() throws Exception {
        TwinEntity a = twin(0, "1"), b = twin(1, "3");   // head level
        TwinEntity r1 = root(2, "1"), r2 = root(3, "3"); // (class, BA) root level
        stubLevel(Arrays.asList(a, b));
        stubRootLevel(Arrays.asList(r1, r2));
        service.reorderTwins(movesKit(move(a, b.getId()), move(r1, r2.getId())));
        assertEquals("3V", a.getSortOrder());
        assertEquals("3V", r1.getSortOrder());
        // both levels of the batch are locked (deterministic order, no cross-level deadlock)
        verify(lockQuery).setParameter("levelKey", "twin:" + HEAD);
        verify(lockQuery).setParameter("levelKey", "twin_root:" + ROOT_CLASS + ":" + ROOT_BA);
    }
}
