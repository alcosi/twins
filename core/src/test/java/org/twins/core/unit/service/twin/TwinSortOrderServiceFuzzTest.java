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
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.status.StatusType;
import org.twins.core.service.twin.TwinSortOrderService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Property-based invariant check (docs/backlog/067): random move sequences over one level must
 * always keep the produced key order equal to the expected element order, with distinct keys
 * within {@link TwinSortOrderService} length cap. Fixed seed - reproducible.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TwinSortOrderServiceFuzzTest {
    private static final int ROUNDS = 25;
    private static final int ELEMENTS = 40;
    private static final int MOVES_PER_ROUND = 500;
    private static final int MAX_KEY_LENGTH = 8;

    @Mock
    TwinRepository twinRepository;
    @Mock
    TwinLinkRepository twinLinkRepository;
    @Mock
    EntityManager entityManager;
    @Mock
    Query lockQuery;

    TwinSortOrderService service;
    /** current fuzz level, swapped per round; the single-shot stubs read it */
    private final List<TwinEntity> levelHolder = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new TwinSortOrderService(twinRepository, twinLinkRepository);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(lockQuery);
        when(lockQuery.setParameter(anyString(), any())).thenReturn(lockQuery);
        when(twinRepository.findHeadSiblingsOrderedIn(any())).thenAnswer(inv -> levelHolder);
        when(twinRepository.findSortOrderViewByIdIn(any())).thenAnswer(inv -> {
            java.util.Collection<UUID> ids = inv.getArgument(0);
            return ids.stream()
                    .flatMap(id -> levelHolder.stream().filter(t -> t.getId().equals(id)))
                    .<TwinRepository.TwinSortOrderView>map(t -> new TwinRepository.TwinSortOrderView() {
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
                    })
                    .toList();
        });
        when(twinRepository.findSortOrderBounds(any(), any(), any())).thenAnswer(inv -> {
            UUID head = inv.getArgument(0);
            String afterKey = inv.getArgument(1);
            java.util.Collection<UUID> excludeIds = inv.getArgument(2);
            String minKey = null, nextKey = null;
            for (TwinEntity t : levelHolder) {
                if (!head.equals(t.getHeadTwinId()) || t.getSortOrder() == null || excludeIds.contains(t.getId()))
                    continue;
                if (minKey == null || t.getSortOrder().compareTo(minKey) < 0)
                    minKey = t.getSortOrder();
                if (t.getSortOrder().compareTo(afterKey) > 0 && (nextKey == null || t.getSortOrder().compareTo(nextKey) < 0))
                    nextKey = t.getSortOrder();
            }
            final String fMinKey = minKey, fNextKey = nextKey;
            return new TwinRepository.SortOrderBounds() {
                @Override
                public String getMinKey() {
                    return fMinKey;
                }

                @Override
                public String getNextKey() {
                    return fNextKey;
                }
            };
        });
    }

    private static TwinEntity twin(int i) {
        TwinEntity twin = new TwinEntity();
        twin.setId(UUID.nameUUIDFromBytes(("fuzz-" + i).getBytes()));
        twin.setHeadTwinId(UUID.nameUUIDFromBytes("fuzz-head".getBytes()));
        // newest first (mirror of the repository ORDER BY): lower index sorts first
        twin.setCreatedAt(new java.sql.Timestamp(2_000_000_000L - i));
        twin.setTwinStatusId(UUID.nameUUIDFromBytes(("fs" + i).getBytes()));
        twin.setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        return twin;
    }

    @Test
    void randomMovesKeepKeysConsistentWithExpectedOrder() throws Exception {
        Random rnd = new Random(42); // fixed seed: reproducible failures
        for (int round = 0; round < ROUNDS; round++) {
            List<TwinEntity> siblings = new ArrayList<>();
            for (int i = 0; i < ELEMENTS; i++)
                siblings.add(twin(round * 1000 + i));
            levelHolder.clear();
            levelHolder.addAll(siblings);

            // expected element order: DB initial order (all null keys, position as loaded)
            List<TwinEntity> expected = new ArrayList<>(siblings);
            for (int op = 0; op < MOVES_PER_ROUND; op++) {
                TwinEntity moved = expected.get(rnd.nextInt(expected.size()));
                boolean first = rnd.nextInt(6) == 0; // every ~6th move is "to top"
                UUID anchorId = UuidUtils.NULLIFY_MARKER;
                int insertPos = 0;
                if (!first) {
                    int movedPos = expected.indexOf(moved);
                    int anchorPos = rnd.nextInt(expected.size() - 1);
                    if (anchorPos >= movedPos)
                        anchorPos++; // skip moved itself when picking the anchor position
                    anchorId = expected.get(anchorPos).getId();
                    // after removing the moved element, "right after the anchor" is:
                    // anchorPos itself when the anchor was after it, anchorPos+1 when before
                    insertPos = anchorPos < movedPos ? anchorPos + 1 : anchorPos;
                }
                expected.remove(moved);
                expected.add(insertPos, moved);

                Kit<TwinUpdate, UUID> moves = new Kit<>(TwinSortOrderService.TWIN_UPDATE_KEY, DuplicateKeyMode.REPLACE);
                moves.add(new TwinUpdate().setDbTwinEntity(moved).setAfterTwinId(anchorId));
                service.reorderTwins(moves);

                assertInvariants(siblings, expected);
            }
        }
    }

    /** keys of ordered elements must be distinct, <= cap, and sorted exactly like the expected order */
    private void assertInvariants(List<TwinEntity> siblings, List<TwinEntity> expected) {
        List<TwinEntity> actual = siblings.stream()
                .sorted(Comparator
                        .comparing(TwinEntity::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TwinEntity::getId))
                .toList();
        // null-keyed tail (elements never touched and no renumber yet) must match the expected tail exactly
        assertEquals(expected, actual, "element order diverged from the expected one");
        String prev = null;
        for (TwinEntity t : actual) {
            String key = t.getSortOrder();
            if (key == null)
                break; // unordered tail reached
            assertTrue(key.length() <= MAX_KEY_LENGTH, "key exceeds the cap: " + key);
            if (prev != null)
                assertTrue(prev.compareTo(key) < 0, "keys not strictly increasing: " + prev + " vs " + key);
            prev = key;
        }
    }
}
