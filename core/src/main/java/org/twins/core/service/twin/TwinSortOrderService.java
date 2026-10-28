package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.DuplicateKeyMode;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.UuidUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twin.TwinLinkRepository.TwinLinkSortOrderView;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinRepository.SortOrderBounds;
import org.twins.core.dao.twin.TwinRepository.TwinRootSortOrderView;
import org.twins.core.dao.twin.TwinRepository.TwinSortOrderView;
import org.twins.core.domain.twinlink.TwinLinkUpdate;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * User defined order (drag and drop): lexicographic sort keys.
 * twin level        - siblings of one head_twin_id (twin.sort_order)
 * twin root level   - headless twins of one twin class inside one business account (twin.sort_order)
 * twin_link level   - src twins within one (dst_twin_id, link_id) (twin_link.sort_order)
 * <p>
 * Key rules: anchor afterTwinId = "place after this sibling", key = midpoint(anchor key, next key);
 * null anchor = skip (moves without anchor are ignored), {@link UuidUtils#NULLIFY_MARKER} = place first.
 * Update flow ({@link #reorderTwins}) re-stores existing twins; create flow ({@link #reorderCreatedTwins})
 * assigns keys to the transient created twins, so the INSERT carries them (anchors must already be committed
 * - batch-internal temporal references are not resolved there). The whole level is renumbered only on demand
 * ({@link Level#renumber}): unordered anchor, no key below the minimum, or a key longer than
 * {@link #MAX_KEY_LENGTH}. Renumber spreads keys evenly over a fixed-width base-62 space, so the key
 * order is correct for any level size and every gap has room for midpoint inserts. Unordered siblings
 * keep null keys and sort last. Alphabet index order == byte order (digits < upper < lower), equal
 * length keys compare numerically - column COLLATE "C" is required.
 * Concurrency: advisory lock per level; level sort_order index is deliberately not unique - writers
 * of one level are serialized by the lock and renumbering rewrites keys in one flush, which would
 * violate a non-deferrable unique index on rotated keys.
 * Entry points ({@link #reorderTwins}, {@link #reorderCreatedTwins}, {@link #reorderTwinLinks}) take a {@link Kit} of moves in
 * request order: levels are locked in a deterministic (sorted) order first, then each level is
 * processed by one of two mechanisms - light (anchor keys only, level is not loaded) or full
 * (whole level loaded once, on a renumber trigger); moves are applied in memory (sequential
 * semantics - each move sees the keys set by the previous ones). Must be invoked inside the caller
 * transaction.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TwinSortOrderService {
    static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ALPHABET_SIZE = ALPHABET.length();
    private static final char MID_DIGIT = ALPHABET.charAt(ALPHABET_SIZE / 2); // 'V'
    /**
     * Renumber trigger: a midpoint key longer than this compacts the level back to fixed width.
     * 8 digits give ~5e11 slots per level - unreachable, the cap only bounds key growth at
     * repeatedly-inserted spots.
     */
    private static final int MAX_KEY_LENGTH = 8;
    /**
     * Renumber quality: minimal count of free key slots between neighboring siblings after renumber,
     * so midpoints between renumbered keys always fit without growing the key.
     */
    private static final int RENUMBER_MIN_GAP = 4;

    private final TwinRepository twinRepository;
    private final TwinLinkRepository twinLinkRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /** Kit key of a reorder move: id of the updated twin */
    public static final Function<TwinUpdate, UUID> TWIN_UPDATE_KEY = move -> move.getDbTwinEntity().getId();
    /** Kit key of a create move: id of the created twin */
    public static final Function<TwinCreate, UUID> TWIN_CREATE_KEY = TwinOperation::getTwinId;
    /** Kit key of a link reorder move: id of the updated twin_link */
    public static final Function<TwinLinkUpdate, UUID> TWIN_LINK_UPDATE_KEY = move -> move.getTwinLink().getId();

    /**
     * Order level of a twin move: siblings of one head_twin_id (head level), or headless twins of one
     * twin class inside one business account (root level). Headless without owner business account is
     * not orderable - the update flow rejects it, the create flow skips the move.
     */
    private record TwinLevel(UUID headTwinId, UUID twinClassId, UUID ownerBusinessAccountId) {
        static TwinLevel ofHead(UUID headTwinId) {
            return new TwinLevel(headTwinId, null, null);
        }

        static TwinLevel ofRoot(UUID twinClassId, UUID ownerBusinessAccountId) {
            return new TwinLevel(null, twinClassId, ownerBusinessAccountId);
        }

        boolean root() {
            return headTwinId == null;
        }

        /** advisory lock / grouping key; "twin:<uuid>" and "twin_root:<uuid>:<uuid>" never collide */
        String lockKey() {
            return root() ? "twin_root:" + twinClassId + ":" + ownerBusinessAccountId : "twin:" + headTwinId;
        }
    }

    /** normalized anchor lookup of the light path: anchor key + membership in the level */
    private record AnchorRef(String sortOrder, boolean sibling) {
    }

    /** level of an update move; headless without owner BA (or class) fails the whole batch */
    private static TwinLevel twinMoveLevel(TwinUpdate move) throws ServiceException {
        TwinEntity twin = move.getDbTwinEntity();
        if (twin.getHeadTwinId() != null)
            return TwinLevel.ofHead(twin.getHeadTwinId());
        if (twin.getTwinClassId() == null || twin.getOwnerBusinessAccountId() == null)
            throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID,
                    "headless twin can not be reordered, ownerBusinessAccountId is required for a twin without headTwinId");
        return TwinLevel.ofRoot(twin.getTwinClassId(), twin.getOwnerBusinessAccountId());
    }

    /** level of a create move; null = headless without owner BA (or class) - the move is skipped */
    private static TwinLevel createdMoveLevel(TwinCreate move) {
        TwinEntity twin = move.getTwinEntity();
        if (twin.getHeadTwinId() != null)
            return TwinLevel.ofHead(twin.getHeadTwinId());
        if (twin.getTwinClassId() == null || twin.getOwnerBusinessAccountId() == null)
            return null;
        return TwinLevel.ofRoot(twin.getTwinClassId(), twin.getOwnerBusinessAccountId());
    }

    /** committed rows of the level, in the read order the {@link Level} machinery relies on */
    private List<TwinEntity> loadLevelSiblings(TwinLevel level) {
        return level.root()
                ? twinRepository.findRootSiblingsOrdered(level.twinClassId(), level.ownerBusinessAccountId())
                : twinRepository.findHeadSiblingsOrderedIn(List.of(level.headTwinId()));
    }

    /** level-scoped min/next bounds of the light path */
    private SortOrderBounds sortOrderBounds(TwinLevel level, String afterKey, Set<UUID> excludeIds) {
        return level.root()
                ? twinRepository.findRootSortOrderBounds(level.twinClassId(), level.ownerBusinessAccountId(), afterKey, excludeIds)
                : twinRepository.findSortOrderBounds(level.headTwinId(), afterKey, excludeIds);
    }

    /** mirror of TwinRepository.findHeadSiblingsOrderedIn ORDER BY (without the key, appended in Level) */
    private static final Comparator<TwinEntity> TWIN_DB_ORDER = Comparator
            .comparing(TwinEntity::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()
            .thenComparing(TwinEntity::getId);
    /** mirror of TwinLinkRepository.findDstLinkLevelOrdered ORDER BY (without the key, appended in Level) */
    private static final Comparator<TwinLinkEntity> TWIN_LINK_DB_ORDER = Comparator
            .comparing(TwinLinkEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(TwinLinkEntity::getId);

    /**
     * Batch move: TwinUpdates carrying the afterTwinId anchor ({@link UuidUtils#NULLIFY_MARKER} = first).
     * Moves are applied in kit iteration order (sequential semantics): each move sees the keys set by
     * the previous ones. Duplicate moves of one twin resolve as "last anchor wins, applied at the
     * position of the first occurrence" ({@link DuplicateKeyMode#REPLACE}) - clients are expected to
     * move each twin once per batch. Two mechanisms per level:
     * light - only the anchor keys are fetched (projection + one min/next bounds query per move), the
     * level itself is never loaded;
     * full - on any renumber trigger (unordered anchor, exhausted bottom edge, no room for a midpoint,
     * key length cap) the whole level is loaded once and every move of the level is reapplied.
     * Levels are locked in a deterministic (sorted) order, so two batches touching the same levels
     * can not deadlock each other. A level is a head level (siblings of one head_twin_id) or a root
     * level (headless twins of one class inside one business account); a headless twin without owner
     * business account fails the whole batch.
     */
    public void reorderTwins(Kit<TwinUpdate, UUID> moves) throws ServiceException {
        if (moves == null || moves.isEmpty())
            return;
        // resolve (and validate) levels first: a headless twin without owner BA fails the whole batch
        Map<UUID, TwinLevel> levelByMoveId = new HashMap<>();
        for (TwinUpdate move : moves)
            levelByMoveId.put(move.getDbTwinEntity().getId(), twinMoveLevel(move));
        KitGrouped<TwinUpdate, UUID, TwinLevel> byLevel = new KitGrouped<>(
                moves.getCollection(), TWIN_UPDATE_KEY, move -> levelByMoveId.get(move.getDbTwinEntity().getId()), DuplicateKeyMode.REPLACE);
        Map<TwinLevel, List<TwinUpdate>> grouped = byLevel.getGroupedMap();
        List<TwinLevel> levels = grouped.keySet().stream()
                .sorted(Comparator.comparing(TwinLevel::lockKey)).toList();
        for (TwinLevel level : levels)
            lockLevel(level.lockKey());
        for (TwinLevel level : levels) {
            List<TwinUpdate> levelMoves = grouped.get(level);
            List<TwinEntity> moved;
            if (applyTwinMovesFast(level, levelMoves)) {
                moved = levelMoves.stream().filter(m -> m.getAfterTwinId() != null).map(TwinUpdate::getDbTwinEntity).toList();
            } else {
                // full renumber-capable path: reload the level (auto-flush persists any partially
                // assigned keys of the light path) and reapply every move of the level
                List<TwinEntity> siblings = loadLevelSiblings(level);
                if (siblings.isEmpty()) // unreachable: the moved twin itself belongs to the level
                    throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "level[" + level.lockKey() + "] has no siblings");
                Level<TwinEntity> keyLevel = new Level<>(level.lockKey(), siblings, TWIN_DB_ORDER,
                        TwinEntity::getSortOrder, TwinEntity::setSortOrder, TwinEntity::getId);
                moved = new ArrayList<>(levelMoves.size());
                for (TwinUpdate move : levelMoves) {
                    if (move.getAfterTwinId() == null)
                        continue;
                    keyLevel.move(move.getDbTwinEntity(), move.getAfterTwinId(), TwinSortOrderService::findAnchorTwinIndex);
                    moved.add(move.getDbTwinEntity());
                }
            }
            twinRepository.saveAll(moved);
        }
    }

    /**
     * Light reorder path: computes keys from anchor keys only (projection + bounds queries), never
     * loads the level. Returns false when a renumber trigger fired - the caller falls back to the
     * full path. Keys assigned here are kept in an in-memory overlay, so batch moves see each other;
     * the db bounds queries exclude already-moved twins - their db keys are outdated until flush.
     */
    private boolean applyTwinMovesFast(TwinLevel level, List<TwinUpdate> levelMoves) throws ServiceException {
        Map<UUID, String> assignedKeyById = new HashMap<>(); // keys set by earlier moves of this batch
        TreeSet<String> assignedKeys = new TreeSet<>();
        Set<UUID> staleIds = new HashSet<>(); // moved twins: their db keys are outdated until flush
        Set<UUID> anchorIds = new HashSet<>();
        for (TwinUpdate move : levelMoves)
            if (move.getAfterTwinId() != null && !UuidUtils.isNullifyMarker(move.getAfterTwinId()))
                anchorIds.add(move.getAfterTwinId());
        Map<UUID, AnchorRef> anchorsById = new HashMap<>();
        if (!anchorIds.isEmpty())
            if (level.root())
                for (TwinRootSortOrderView view : twinRepository.findRootSortOrderViewByIdIn(anchorIds))
                    anchorsById.put(view.getId(), new AnchorRef(view.getSortOrder(),
                            level.twinClassId().equals(view.getTwinClassId())
                                    && level.ownerBusinessAccountId().equals(view.getOwnerBusinessAccountId())));
            else
                for (TwinSortOrderView view : twinRepository.findSortOrderViewByIdIn(anchorIds))
                    anchorsById.put(view.getId(), new AnchorRef(view.getSortOrder(), level.headTwinId().equals(view.getHeadTwinId())));
        for (TwinUpdate move : levelMoves) {
            UUID afterTwinId = move.getAfterTwinId();
            if (afterTwinId == null)
                continue;
            UUID movedId = move.getDbTwinEntity().getId();
            Set<UUID> excludeIds = new HashSet<>(staleIds);
            excludeIds.add(movedId);
            String key;
            if (UuidUtils.isNullifyMarker(afterTwinId)) {
                TwinRepository.SortOrderBounds bounds = sortOrderBounds(level, "", excludeIds);
                String minKey = minKey(bounds.getMinKey(), assignedKeys.isEmpty() ? null : assignedKeys.first());
                if (minKey == null)
                    return false; // no ordered sibling: level initialization renumber
                key = before(minKey);
                if (key == null)
                    return false; // bottom edge exhausted: renumber
            } else {
                if (afterTwinId.equals(movedId))
                    throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor and reordered twin are the same twin");
                String anchorKey = assignedKeyById.get(afterTwinId);
                if (anchorKey == null) {
                    AnchorRef anchor = anchorsById.get(afterTwinId);
                    if (anchor == null || !anchor.sibling())
                        throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor twin[" + afterTwinId + "] is not a sibling of the reordered twin");
                    anchorKey = anchor.sortOrder();
                    if (anchorKey == null)
                        return false; // unordered anchor: renumber
                }
                TwinRepository.SortOrderBounds bounds = sortOrderBounds(level, anchorKey, excludeIds);
                String nextKey = minKey(bounds.getNextKey(), assignedKeys.higher(anchorKey));
                key = midpoint(anchorKey, nextKey);
                if (key == null || key.length() > MAX_KEY_LENGTH)
                    return false; // no room for a midpoint: renumber
            }
            assignedKeyById.put(movedId, key);
            assignedKeys.add(key);
            staleIds.add(movedId);
            move.getDbTwinEntity().setSortOrder(key);
        }
        return true;
    }

    private static String minKey(String a, String b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return a.compareTo(b) < 0 ? a : b;
    }

    /**
     * Batch placement of created twins: TwinCreates carrying the afterTwinId anchor
     * ({@link UuidUtils#NULLIFY_MARKER} = first). Unlike {@link #reorderTwins} the moved twins are
     * not stored yet: keys are assigned to the transient entities and ride with the INSERT - the
     * created twins are never saved here, only committed siblings renumbered on the full path are.
     * Anchors must already exist in the db (batch-internal temporal references are not resolved).
     * Same two mechanisms and level locking as {@link #reorderTwins}; on the full path the created
     * twins of the level join the in-memory level, so a brand new level (no committed siblings yet)
     * is initialized by the same renumber instead of failing. Moves are applied in kit iteration
     * order (sequential semantics - each move sees the keys set by the previous ones). Must be
     * invoked inside the caller transaction, before the created rows are flushed. Moves of headless
     * twins without owner business account are skipped - the created twin stays unordered.
     */
    public void reorderCreatedTwins(Kit<TwinCreate, UUID> moves) throws ServiceException {
        if (moves == null || moves.isEmpty())
            return;
        List<TwinCreate> orderable = new ArrayList<>(moves.getCollection().size());
        for (TwinCreate move : moves)
            if (createdMoveLevel(move) != null)
                orderable.add(move); // null level: headless without owner BA - skipped, the key stays null
        if (orderable.isEmpty())
            return;
        KitGrouped<TwinCreate, UUID, TwinLevel> byLevel = new KitGrouped<>(
                orderable, TWIN_CREATE_KEY, TwinSortOrderService::createdMoveLevel, DuplicateKeyMode.REPLACE);
        Map<TwinLevel, List<TwinCreate>> grouped = byLevel.getGroupedMap();
        List<TwinLevel> levels = grouped.keySet().stream()
                .sorted(Comparator.comparing(TwinLevel::lockKey)).toList();
        for (TwinLevel level : levels)
            lockLevel(level.lockKey());
        for (TwinLevel level : levels) {
            List<TwinCreate> levelMoves = grouped.get(level);
            if (applyTwinCreateMovesFast(level, levelMoves))
                continue; // keys are set on the transient entities, the INSERT carries them
            // full renumber-capable path: reload the committed level (created rows are not persisted
            // yet, so auto-flush is a no-op) and join the batch's created twins, reapply every move
            List<TwinEntity> committed = loadLevelSiblings(level);
            List<TwinEntity> siblings = new ArrayList<>(committed);
            for (TwinCreate move : levelMoves)
                siblings.add(move.getTwinEntity());
            Level<TwinEntity> keyLevel = new Level<>(level.lockKey(), siblings, TWIN_DB_ORDER,
                    TwinEntity::getSortOrder, TwinEntity::setSortOrder, TwinEntity::getId);
            for (TwinCreate move : levelMoves) {
                if (move.getAfterTwinId() == null)
                    continue;
                keyLevel.move(move.getTwinEntity(), move.getAfterTwinId(), TwinSortOrderService::findAnchorTwinIndex);
            }
            twinRepository.saveAll(committed); // renumbered keys of committed siblings; created ones ride with the INSERT
        }
    }

    /**
     * Light placement path for created twins: computes keys from anchor keys only (projection +
     * bounds queries), never loads the level. The created twins have no rows in the db yet - there
     * is nothing stale to exclude from the bounds queries, batch moves see each other through the
     * in-memory overlay. Returns false when a renumber trigger fired - the caller falls back to the
     * full path.
     */
    private boolean applyTwinCreateMovesFast(TwinLevel level, List<TwinCreate> levelMoves) throws ServiceException {
        Map<UUID, String> assignedKeyById = new HashMap<>(); // keys set by earlier moves of this batch
        TreeSet<String> assignedKeys = new TreeSet<>();
        Set<UUID> anchorIds = new HashSet<>();
        for (TwinCreate move : levelMoves)
            if (move.getAfterTwinId() != null && !UuidUtils.isNullifyMarker(move.getAfterTwinId()))
                anchorIds.add(move.getAfterTwinId());
        Map<UUID, AnchorRef> anchorsById = new HashMap<>();
        if (!anchorIds.isEmpty())
            if (level.root())
                for (TwinRootSortOrderView view : twinRepository.findRootSortOrderViewByIdIn(anchorIds))
                    anchorsById.put(view.getId(), new AnchorRef(view.getSortOrder(),
                            level.twinClassId().equals(view.getTwinClassId())
                                    && level.ownerBusinessAccountId().equals(view.getOwnerBusinessAccountId())));
            else
                for (TwinSortOrderView view : twinRepository.findSortOrderViewByIdIn(anchorIds))
                    anchorsById.put(view.getId(), new AnchorRef(view.getSortOrder(), level.headTwinId().equals(view.getHeadTwinId())));
        for (TwinCreate move : levelMoves) {
            UUID afterTwinId = move.getAfterTwinId();
            if (afterTwinId == null)
                continue;
            TwinEntity created = move.getTwinEntity();
            String key;
            if (UuidUtils.isNullifyMarker(afterTwinId)) {
                TwinRepository.SortOrderBounds bounds = sortOrderBounds(level, "", Set.of());
                String minKey = minKey(bounds.getMinKey(), assignedKeys.isEmpty() ? null : assignedKeys.first());
                if (minKey == null)
                    return false; // no ordered sibling: level initialization renumber
                key = before(minKey);
                if (key == null)
                    return false; // bottom edge exhausted: renumber
            } else {
                if (afterTwinId.equals(created.getId()))
                    throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor and created twin are the same twin");
                String anchorKey = assignedKeyById.get(afterTwinId);
                if (anchorKey == null) {
                    AnchorRef anchor = anchorsById.get(afterTwinId);
                    if (anchor == null || !anchor.sibling())
                        throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor twin[" + afterTwinId + "] is not a sibling of the created twin");
                    anchorKey = anchor.sortOrder();
                    if (anchorKey == null)
                        return false; // unordered anchor: renumber
                }
                TwinRepository.SortOrderBounds bounds = sortOrderBounds(level, anchorKey, Set.of());
                String nextKey = minKey(bounds.getNextKey(), assignedKeys.higher(anchorKey));
                key = midpoint(anchorKey, nextKey);
                if (key == null || key.length() > MAX_KEY_LENGTH)
                    return false; // no room for a midpoint: renumber
            }
            assignedKeyById.put(created.getId(), key);
            assignedKeys.add(key);
            created.setSortOrder(key);
        }
        return true;
    }

    /**
     * Batch move: TwinLinkUpdates carrying the afterTwinId anchor ({@link UuidUtils#NULLIFY_MARKER} =
     * first). Anchor is a src twin id, not a twin_link id: src twins are what users reorder within
     * one dstTwin + link type. Moves are applied in kit iteration order (sequential semantics).
     * Same two mechanisms as {@link #reorderTwins}: light path (anchor keys + bounds queries, no level
     * load) with full-path fallback on renumber triggers. Levels are locked in a deterministic (sorted)
     * order, so two batches touching the same levels can not deadlock each other.
     */
    public void reorderTwinLinks(Kit<TwinLinkUpdate, UUID> moves) throws ServiceException {
        if (moves == null || moves.isEmpty())
            return;
        Map<String, List<TwinLinkUpdate>> grouped = new KitGrouped<>(
                moves.getCollection(), TWIN_LINK_UPDATE_KEY, TwinSortOrderService::linkMoveLevel, DuplicateKeyMode.REPLACE)
                .getGroupedMap();
        List<String> levelKeys = grouped.keySet().stream().sorted().toList();
        for (String levelKey : levelKeys) {
            List<TwinLinkUpdate> levelMoves = grouped.get(levelKey);
            TwinLinkEntity any = levelMoves.get(0).getTwinLink();
            lockLevel("twin_link:" + any.getDstTwinId() + ":" + any.getLinkId());
            List<TwinLinkEntity> moved;
            if (applyTwinLinkMovesFast(any.getDstTwinId(), any.getLinkId(), levelMoves)) {
                moved = levelMoves.stream().filter(m -> m.getAfterTwinId() != null).map(TwinLinkUpdate::getTwinLink).toList();
            } else {
                // full renumber-capable path: reload the level (auto-flush persists any partially
                // assigned keys of the light path) and reapply every move of the level
                Level<TwinLinkEntity> level = new Level<>("twin_link:" + any.getDstTwinId() + ":" + any.getLinkId(),
                        twinLinkRepository.findDstLinkLevelOrdered(any.getDstTwinId(), any.getLinkId()), TWIN_LINK_DB_ORDER,
                        TwinLinkEntity::getSortOrder, TwinLinkEntity::setSortOrder, TwinLinkEntity::getId);
                moved = new ArrayList<>(levelMoves.size());
                for (TwinLinkUpdate move : levelMoves) {
                    if (move.getAfterTwinId() == null)
                        continue;
                    level.move(move.getTwinLink(), move.getAfterTwinId(), TwinSortOrderService::findAnchorLinkIndex);
                    moved.add(move.getTwinLink());
                }
            }
            twinLinkRepository.saveAll(moved);
        }
    }

    /** light link reorder path, mirror of {@link #applyTwinMovesFast}; anchor is addressed by src twin id */
    private boolean applyTwinLinkMovesFast(UUID dstTwinId, UUID linkId, List<TwinLinkUpdate> levelMoves) throws ServiceException {
        Map<UUID, String> assignedKeyBySrcId = new HashMap<>(); // keys set by earlier moves of this batch
        TreeSet<String> assignedKeys = new TreeSet<>();
        Set<UUID> staleIds = new HashSet<>(); // moved links: their db keys are outdated until flush
        Set<UUID> anchorSrcIds = new HashSet<>();
        for (TwinLinkUpdate move : levelMoves)
            if (move.getAfterTwinId() != null && !UuidUtils.isNullifyMarker(move.getAfterTwinId()))
                anchorSrcIds.add(move.getAfterTwinId());
        Map<UUID, TwinLinkSortOrderView> anchorsBySrcId = new HashMap<>();
        if (!anchorSrcIds.isEmpty())
            for (TwinLinkSortOrderView view : twinLinkRepository.findSortOrderViewByLevelAndSrcIds(dstTwinId, linkId, anchorSrcIds))
                anchorsBySrcId.put(view.getSrcTwinId(), view);
        for (TwinLinkUpdate move : levelMoves) {
            UUID afterTwinId = move.getAfterTwinId();
            if (afterTwinId == null)
                continue;
            TwinLinkEntity movedLink = move.getTwinLink();
            Set<UUID> excludeIds = new HashSet<>(staleIds);
            excludeIds.add(movedLink.getId());
            String key;
            if (UuidUtils.isNullifyMarker(afterTwinId)) {
                TwinRepository.SortOrderBounds bounds = twinLinkRepository.findSortOrderBounds(dstTwinId, linkId, "", excludeIds);
                String minKey = minKey(bounds.getMinKey(), assignedKeys.isEmpty() ? null : assignedKeys.first());
                if (minKey == null)
                    return false; // no ordered sibling: level initialization renumber
                key = before(minKey);
                if (key == null)
                    return false; // bottom edge exhausted: renumber
            } else {
                String anchorKey = assignedKeyBySrcId.get(afterTwinId);
                if (anchorKey == null) {
                    TwinLinkSortOrderView anchor = anchorsBySrcId.get(afterTwinId);
                    if (anchor == null)
                        throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor src twin[" + afterTwinId + "] has no link in the reordered level");
                    if (anchor.getId().equals(movedLink.getId()))
                        throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor and reordered link are the same link");
                    anchorKey = anchor.getSortOrder();
                    if (anchorKey == null)
                        return false; // unordered anchor: renumber
                }
                TwinRepository.SortOrderBounds bounds = twinLinkRepository.findSortOrderBounds(dstTwinId, linkId, anchorKey, excludeIds);
                String nextKey = minKey(bounds.getNextKey(), assignedKeys.higher(anchorKey));
                key = midpoint(anchorKey, nextKey);
                if (key == null || key.length() > MAX_KEY_LENGTH)
                    return false; // no room for a midpoint: renumber
            }
            assignedKeyBySrcId.put(movedLink.getSrcTwinId(), key);
            assignedKeys.add(key);
            staleIds.add(movedLink.getId());
            movedLink.setSortOrder(key);
        }
        return true;
    }

    /** grouping key of a link move: (dstTwinId, linkId) level */
    private static String linkMoveLevel(TwinLinkUpdate move) {
        TwinLinkEntity link = move.getTwinLink();
        return link.getDstTwinId() + ":" + link.getLinkId();
    }

    private static int findAnchorTwinIndex(List<TwinEntity> siblings, UUID afterTwinId, UUID movedTwinId) throws ServiceException {
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(afterTwinId))
                if (siblings.get(i).getId().equals(movedTwinId))
                    throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor and reordered twin are the same twin");
                else
                    return i;
        }
        throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor twin[" + afterTwinId + "] is not a sibling of the reordered twin");
    }

    private static int findAnchorLinkIndex(List<TwinLinkEntity> siblings, UUID afterTwinId, UUID movedLinkId) throws ServiceException {
        for (int i = 0; i < siblings.size(); i++) {
            TwinLinkEntity sibling = siblings.get(i);
            if (sibling.getSrcTwinId().equals(afterTwinId))
                if (sibling.getId().equals(movedLinkId))
                    throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor and reordered link are the same link");
                else
                    return i;
        }
        throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "anchor src twin[" + afterTwinId + "] has no link in the reordered level");
    }

    @FunctionalInterface
    private interface AnchorFinder<T> {
        int indexOfAnchor(List<T> siblings, UUID afterTwinId, UUID movedId) throws ServiceException;
    }

    /**
     * One reorder level: siblings ordered by current keys, uniform access for twin and twin_link moves.
     */
    private static class Level<T> {
        final String label; // for the renumber log line
        final List<T> siblings;
        final Comparator<T> order; // key order (nulls last) + db tie-break - mirror of the repository ORDER BY
        final Function<T, String> getSortOrder;
        final BiConsumer<T, String> setSortOrder;
        final Function<T, UUID> getId;

        Level(String label, List<T> siblings, Comparator<T> dbTieBreak, Function<T, String> getSortOrder, BiConsumer<T, String> setSortOrder, Function<T, UUID> getId) {
            this.label = label;
            this.siblings = siblings;
            this.getSortOrder = getSortOrder;
            this.setSortOrder = setSortOrder;
            this.getId = getId;
            this.order = Comparator
                    .comparing(getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(dbTieBreak);
            // the repository query promises this exact order; sorting here makes the contract local -
            // anchor/next scans below are only correct over key-ordered siblings
            resort();
        }

        /**
         * Place moved after afterTwinId anchor ({@link UuidUtils#NULLIFY_MARKER} = first); renumbers
         * the whole level on demand - when the anchor is unordered, no key fits or the key is too
         * long. Re-sorts siblings afterwards, so the next move in a batch sees index order == key order.
         * Contract: moved MUST be the same managed instance as its counterpart in siblings - guaranteed
         * by the transaction-scoped persistence context (one instance per row per tx); resort() and the
         * "next non-moved" scan rely on the mutation being visible through the siblings list.
         */
        void move(T moved, UUID afterTwinId, AnchorFinder<T> anchorFinder) throws ServiceException {
            UUID movedId = getId.apply(moved);
            if (UuidUtils.isNullifyMarker(afterTwinId)) {
                setSortOrder.accept(moved, keyBefore());
            } else {
                int anchorIdx = anchorFinder.indexOfAnchor(siblings, afterTwinId, movedId);
                String key = midpointAfter(anchorIdx, movedId);
                if (key == null || key.length() > MAX_KEY_LENGTH) {
                    renumber();
                    // renumbered keys are <= MAX_KEY_LENGTH-1 chars with gaps >= RENUMBER_MIN_GAP:
                    // midpoint always fits the cap - the invariant below holds unconditionally
                    key = midpointAfter(anchorIdx, movedId);
                }
                setSortOrder.accept(moved, key);
            }
            resort();
        }

        /** re-sorts siblings by current keys (still unordered stay last), so index order matches key order for the next move */
        private void resort() {
            siblings.sort(order);
        }

        /**
         * Evenly spreads siblings over the smallest fixed-width key space with
         * {@link TwinSortOrderService#RENUMBER_MIN_GAP} free slots between neighbors: keys along the
         * current order grow strictly (monotonic for any level size), every gap fits a midpoint.
         * Depth is capped at {@link TwinSortOrderService#MAX_KEY_LENGTH}-1, so renumbered keys are
         * always short enough for any midpoint over them to fit the cap too.
         */
        private void renumber() {
            if (siblings.isEmpty())
                return;
            log.info("level[{}] sort order renumbered, {} siblings - key space compacted", label, siblings.size());
            int depth = 1;
            long capacity = ALPHABET_SIZE;
            while (depth < MAX_KEY_LENGTH - 1 && capacity / (siblings.size() + 1) < RENUMBER_MIN_GAP) {
                depth++;
                capacity *= ALPHABET_SIZE;
            }
            long step = capacity / (siblings.size() + 1);
            if (step == 0) // unreachable: would need > 62^(MAX_KEY_LENGTH-1) / RENUMBER_MIN_GAP siblings in one level
                throw new IllegalStateException("level of " + siblings.size() + " siblings exceeds the key space");
            for (int i = 0; i < siblings.size(); i++)
                setSortOrder.accept(siblings.get(i), encodeBase62((i + 1) * step, depth));
        }

        /** key strictly before the current minimum; renumbers when the level is unordered or the bottom edge is exhausted */
        private String keyBefore() {
            String minKey = minKey();
            String key = minKey == null ? null : before(minKey);
            if (key == null) { // no ordered sibling yet, or minimum starts with '0...0': compact the level first
                renumber();
                key = before(minKey()); // renumbered minimum has a non-zero digit - predecessor always exists
            }
            return key;
        }

        private String minKey() {
            String minKey = null;
            for (T sibling : siblings) {
                String sortOrder = getSortOrder.apply(sibling);
                if (sortOrder != null && (minKey == null || sortOrder.compareTo(minKey) < 0))
                    minKey = sortOrder;
            }
            return minKey; // null when the level has no ordered sibling at all
        }

        /**
         * Midpoint between anchor key and the next non-moved sibling key; null when no room.
         * Unordered next sibling (null key) sorts last, so "after anchor" append is correct there.
         */
        private String midpointAfter(int anchorIdx, UUID movedId) {
            String anchorKey = getSortOrder.apply(siblings.get(anchorIdx));
            String nextKey = null;
            for (int i = anchorIdx + 1; i < siblings.size(); i++) {
                if (getId.apply(siblings.get(i)).equals(movedId))
                    continue; // moved element does not count as "next"
                nextKey = getSortOrder.apply(siblings.get(i));
                break;
            }
            return midpoint(anchorKey, nextKey); // null anchorKey (unordered anchor) -> null -> renumber
        }
    }

    /** fixed width most-significant-first base-62: equal length keys compare numerically */
    private static String encodeBase62(long value, int depth) {
        char[] digits = new char[depth];
        for (int i = depth - 1; i >= 0; i--) {
            digits[i] = ALPHABET.charAt((int) (value % ALPHABET_SIZE));
            value /= ALPHABET_SIZE;
        }
        return new String(digits); // value fits into depth digits by construction of renumber()
    }

    /**
     * Lexicographic key predecessor: decrement the rightmost non-'0' digit and truncate after it.
     * Returns null when no key exists below (key is all zeros).
     */
    private static String before(String key) {
        StringBuilder sb = new StringBuilder(key);
        for (int i = sb.length() - 1; i >= 0; i--) {
            int d = ALPHABET.indexOf(sb.charAt(i));
            if (d > 0) {
                sb.setCharAt(i, ALPHABET.charAt(d - 1));
                return sb.substring(0, i + 1);
            }
        }
        return null;
    }

    /**
     * Lexicographic midpoint with left < right; right == null means "after left" (append).
     * Returns null when no key can be produced without renumbering.
     */
    private static String midpoint(String left, String right) {
        if (left == null)
            return null;
        if (right == null)
            return left + MID_DIGIT;
        StringBuilder prefix = new StringBuilder();
        int i = 0;
        while (true) {
            int l = i < left.length() ? ALPHABET.indexOf(left.charAt(i)) : -1;
            int r = i < right.length() ? ALPHABET.indexOf(right.charAt(i)) : -1;
            if (l == -1 && r == -1)
                return prefix + String.valueOf(MID_DIGIT); // equal keys (duplicate keys are allowed): append
            if (l == -1) { // left is prefix of right: pick digit right below right's digit
                if (r == 0)
                    return null; // can not go below '0' at this depth -> renumber
                return prefix.append(ALPHABET.charAt(r - 1)).toString();
            }
            if (r == -1) // right is prefix of left: right < left, precondition broken; defensive
                return left + MID_DIGIT;
            if (r - l >= 2)
                return prefix.append(ALPHABET.charAt((l + r) / 2)).toString();
            prefix.append(left.charAt(i)); // adjacent or crossed digits: descend into tails
            i++;
        }
    }

    private void lockLevel(String levelKey) {
        entityManager.createNativeQuery("select pg_advisory_xact_lock(hashtext(:levelKey))")
                .setParameter("levelKey", levelKey)
                .getSingleResult();
    }
}
