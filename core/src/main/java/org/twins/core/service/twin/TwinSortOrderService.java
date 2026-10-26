package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.DuplicateKeyMode;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.UuidUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.twinlink.TwinLinkUpdate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * User defined order (drag and drop, TWINS-727): lexicographic sort keys.
 * twin level        - siblings of one head_twin_id (twin.sort_order)
 * twin_link level   - src twins within one (dst_twin_id, link_id) (twin_link.sort_order)
 * <p>
 * Key rules: anchor afterTwinId = "place after this sibling", key = midpoint(anchor key, next key);
 * null anchor = skip (only update flow passes moves), {@link UuidUtils#NULLIFY_MARKER} = place first.
 * Ordering is update-only: create flow never reorders. The whole level is renumbered only on demand
 * ({@link Level#renumber}): unordered anchor, no key below the minimum, or a key longer than
 * {@link #MAX_KEY_LENGTH}. Renumber spreads keys evenly over a fixed-width base-62 space, so the key
 * order is correct for any level size and every gap has room for midpoint inserts. Unordered siblings
 * keep null keys and sort last. Alphabet index order == byte order (digits < upper < lower), equal
 * length keys compare numerically - column COLLATE "C" is required.
 * Concurrency: advisory lock per level; level sort_order index is deliberately not unique - writers
 * of one level are serialized by the lock and renumbering rewrites keys in one flush, which would
 * violate a non-deferrable unique index on rotated keys.
 * Entry points ({@link #reorderTwins}, {@link #reorderTwinLinks}) take a {@link Kit} of moves in
 * request order: levels are locked in a deterministic (sorted) order first, all twin levels are
 * loaded by one query, then all moves are applied in memory (sequential semantics - each move sees
 * the keys set by the previous ones). Must be invoked inside the caller transaction.
 */
@Service
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
    /** Kit key of a link reorder move: id of the updated twin_link */
    public static final Function<TwinLinkUpdate, UUID> TWIN_LINK_UPDATE_KEY = move -> move.getTwinLink().getId();

    /**
     * Batch move: TwinUpdates carrying the afterTwinId anchor ({@link UuidUtils#NULLIFY_MARKER} = first).
     * Moves are applied in kit iteration order (sequential semantics): each move sees the keys set by
     * the previous ones. Levels are locked in a deterministic (sorted) order, so two batches touching
     * the same levels can not deadlock each other, and all levels are loaded by a single query;
     * processing cost per batch is O(1) queries regardless of the number of moved twins.
     */
    public void reorderTwins(Kit<TwinUpdate, UUID> moves) throws ServiceException {
        if (moves == null || moves.isEmpty())
            return;
        for (TwinUpdate move : moves) {
            if (move.getDbTwinEntity().getHeadTwinId() == null)
                throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "headless twin can not be reordered, headTwinId is null");
        }
        KitGrouped<TwinUpdate, UUID, UUID> byLevel = new KitGrouped<>(
                moves.getCollection(), TWIN_UPDATE_KEY, move -> move.getDbTwinEntity().getHeadTwinId(), DuplicateKeyMode.REPLACE);
        Map<UUID, List<TwinUpdate>> grouped = byLevel.getGroupedMap();
        List<UUID> levelIds = grouped.keySet().stream().sorted().toList();
        for (UUID levelId : levelIds)
            lockLevel("twin:" + levelId);
        Map<UUID, List<TwinEntity>> siblingsByLevel = new LinkedHashMap<>();
        for (TwinEntity sibling : twinRepository.findHeadSiblingsOrderedIn(levelIds))
            siblingsByLevel.computeIfAbsent(sibling.getHeadTwinId(), k -> new ArrayList<>()).add(sibling);
        for (UUID levelId : levelIds) {
            List<TwinEntity> siblings = siblingsByLevel.get(levelId);
            if (siblings == null) // unreachable: the moved twin itself belongs to the level
                throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "level[" + levelId + "] has no siblings");
            Level<TwinEntity> level = new Level<>(siblings,
                    TwinEntity::getSortOrder, TwinEntity::setSortOrder, TwinEntity::getId);
            List<TwinUpdate> levelMoves = grouped.get(levelId);
            List<TwinEntity> moved = new ArrayList<>(levelMoves.size());
            for (TwinUpdate move : levelMoves) {
                if (move.getAfterTwinId() == null)
                    continue;
                level.move(move.getDbTwinEntity(), move.getAfterTwinId(), TwinSortOrderService::findAnchorTwinIndex);
                moved.add(move.getDbTwinEntity());
            }
            twinRepository.saveAll(moved);
        }
    }

    /**
     * Batch move: TwinLinkUpdates carrying the afterTwinId anchor ({@link UuidUtils#NULLIFY_MARKER} =
     * first). Anchor is a src twin id, not a twin_link id: src twins are what users reorder within
     * one dstTwin + link type. Moves are applied in kit iteration order (sequential semantics).
     * Levels are locked in a deterministic (sorted) order, so two batches touching the same levels
     * can not deadlock each other; the level load stays per level - JPQL has no portable tuple IN
     * for the (dstTwinId, linkId) pair, and a batch rarely spans many levels.
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
            Level<TwinLinkEntity> level = new Level<>(twinLinkRepository.findDstLinkLevelOrdered(any.getDstTwinId(), any.getLinkId()),
                    TwinLinkEntity::getSortOrder, TwinLinkEntity::setSortOrder, TwinLinkEntity::getId);
            List<TwinLinkEntity> moved = new ArrayList<>(levelMoves.size());
            for (TwinLinkUpdate move : levelMoves) {
                if (move.getAfterTwinId() == null)
                    continue;
                level.move(move.getTwinLink(), move.getAfterTwinId(), TwinSortOrderService::findAnchorLinkIndex);
                moved.add(move.getTwinLink());
            }
            twinLinkRepository.saveAll(moved);
        }
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
        final List<T> siblings;
        final Function<T, String> getSortOrder;
        final BiConsumer<T, String> setSortOrder;
        final Function<T, UUID> getId;

        Level(List<T> siblings, Function<T, String> getSortOrder, BiConsumer<T, String> setSortOrder, Function<T, UUID> getId) {
            this.siblings = siblings;
            this.getSortOrder = getSortOrder;
            this.setSortOrder = setSortOrder;
            this.getId = getId;
        }

        /**
         * Place moved after afterTwinId anchor ({@link UuidUtils#NULLIFY_MARKER} = first); renumbers
         * the whole level on demand - when the anchor is unordered, no key fits or the key is too
         * long. Re-sorts siblings afterwards, so the next move in a batch sees index order == key order.
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
                    key = midpointAfter(anchorIdx, movedId); // renumbered gaps guarantee a short midpoint
                }
                setSortOrder.accept(moved, key);
            }
            resort();
        }

        /** re-sorts siblings by current keys (still unordered stay last), so index order matches key order for the next move */
        private void resort() {
            siblings.sort(Comparator.comparing(getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(getId));
        }

        /**
         * Evenly spreads siblings over the smallest fixed-width key space with
         * {@link TwinSortOrderService#RENUMBER_MIN_GAP} free slots between neighbors: keys along the
         * current order grow strictly (monotonic for any level size), every gap fits a midpoint.
         */
        private void renumber() {
            if (siblings.isEmpty())
                return;
            int depth = 1;
            long capacity = ALPHABET_SIZE;
            while (capacity / (siblings.size() + 1) < RENUMBER_MIN_GAP) {
                depth++;
                capacity *= ALPHABET_SIZE;
            }
            long step = capacity / (siblings.size() + 1);
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
