package org.twins.core.featurer.factory.multiplier;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamMap;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchService;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deep-copies a twin hierarchy with class and link replacement.
 *
 * <h3>Overview</h3>
 * For every input twin this multiplier collects the full subtree (children up to
 * a configurable depth) plus any twins reachable via configured links, then
 * produces a mirrored copy where every twin gets a new class (from
 * {@code twinClassReplaceMap}) and every link gets a new type (from
 * {@code linksReplaceMap}). Twins whose class is NOT present in the replace map
 * are silently skipped.
 *
 * <h3>Copy algorithm (high level)</h3>
 * <pre>
 *  1. Collect originals
 *     ┌─────────────────────┐
 *     │  input twins        │  ← from factory pipeline
 *     │  + hierarchy search │  ← children up to `childrenDepth`
 *     │  + linked twins     │  ← via `linkedTwinsByLinkIdMap`
 *     └─────────────────────┘
 *
 *  2. Filter by classReplaceMap
 *     Only twins whose twinClassId is a KEY in the map are eligible for copy.
 *
 *  3. Sort by hierarchy depth (ascending), then links-last
 *     This guarantees that when we process a twin, its parent copy already
 *     exists in copyContextMap, and when we process links, the dst twin copy
 *     already exists too.
 *
 *  4. Create copies
 *     For each orig twin (in sorted order):
 *       - allocate a new TwinEntity with a replaced class
 *       - point headTwinId to the COPY of the parent (or keep original if
 *         the parent is outside the copy scope)
 *       - copy forward links, remapping both src/dst to their copies and
 *         replacing the link type via linksReplaceMap
 *
 *  5. Wrap each copy into a FactoryItem(TwinCreate) for downstream processing
 * </pre>
 *
 * <h3>Example</h3>
 * <pre>
 *  Original hierarchy:              Copied hierarchy:
 *
 *  TwinA (classX)                   TwinA' (classY)
 *  ├── TwinB (classX)              ├── TwinB' (classY)
 *  │   └── TwinC (classZ)         │   (skipped — classZ not in map)
 *  └── TwinD (classX) ──L1──→ E   └── TwinD' (classY) ──L2──→ E'
 *
 *  classReplaceMap:  { classX → classY }
 *  linksReplaceMap:  { L1 → L2 }
 * </pre>
 */
@Component
@Featurer(
        id = FeaturerTwins.ID_2213,
        name = "Isolated copy with depth and class change",
        description = "New output twin with children for each input. Output class will be taken from twinClassReplaceMap."
)
@Slf4j
@RequiredArgsConstructor
public class MultiplierIsolatedCopyWithDepthAndClassChange extends Multiplier {

    /** How many levels of children to collect below each input twin. 0 = input twins only. */
    @FeaturerParam(name = "Children Depth", description = "Level of depth in twin hierarchy tree", optional = true, defaultValue = "0")
    public static final FeaturerParamInt childrenDepth = new FeaturerParamInt("childrenDepth");

    /** Optional status filter — only children with one of these statuses will be included. */
    @FeaturerParam(name = "Children statuses", description = "Statuses that are used to filter twin children", optional = true)
    public static final FeaturerParamUUIDSetTwinsStatusId childrenStatuses = new FeaturerParamUUIDSetTwinsStatusId("childrenStatuses");

    /**
     * Mapping of original twinClassId → new twinClassId.
     * Twins whose class is NOT a key in this map will be excluded from copying entirely.
     */
    @FeaturerParam(name = "Twin class replace map", description = "Map of old twin class ID to new twin class ID. Twins with classes not in map keys will be skipped.")
    public static final FeaturerParamMap twinClassReplaceMap = new FeaturerParamMap("twinClassReplaceMap");

    /**
     * Mapping of original linkId → new linkId.
     * Forward links between copied twins will be recreated with the replaced link type.
     * If empty, no links are copied.
     */
    @FeaturerParam(name = "Link replace map", description = "Map of old link ID to new link ID.", optional = true)
    public static final FeaturerParamMap linksReplaceMap = new FeaturerParamMap("linksReplaceMap");

    /**
     * Additional twins to include via link traversal (beyond the hierarchy search).
     * Map of linkId → boolean: true means "find twins where hierarchy twins are src",
     * false means "find twins where hierarchy twins are dst".
     */
    @FeaturerParam(name = "Linked twins by link id", description = "Map of link id and boolean param (True -> search twins as src. False -> search twins as dst)", optional = true)
    public static final FeaturerParamMap linkedTwinsByLinkIdMap = new FeaturerParamMap("linkedTwinsByLinkIdMap");

    private final TwinSearchService twinSearchService;
    private final TwinLinkService twinLinkService;
    private final LinkService linkService;

    /**
     * Holds the state for a single twin being copied:
     *  - twinCopy:         the newly created TwinEntity (null until the copy is materialized)
     *  - linksCopy:        forward links from this copy to other copies
     *  - origFactoryItem:  reference to the original FactoryItem (input or synthesized for children)
     */
    @Data
    @Accessors(chain = true)
    private static class CopyContext {
        private TwinEntity twinCopy;
        private List<TwinLinkEntity> linksCopy;
        private FactoryItem origFactoryItem;
    }

    /**
     * Pre-loaded link data: original links within the hierarchy and their replacement LinkEntities.
     * EMPTY sentinel avoids null checks when linksReplaceMap is not configured.
     */
    private record LinksData(Set<TwinLinkEntity> origTwinLinks, Kit<LinkEntity, UUID> newLinks) {
        static final LinksData EMPTY = new LinksData(Collections.emptySet(), new Kit<>(LinkEntity::getId));
    }

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        var user = authService.getApiUser().getUser();
        var childrenStatusIds = childrenStatuses.extract(properties);
        var depth = childrenDepth.extract(properties);
        var classReplaceMap = twinClassReplaceMap.extract(properties);
        var linkReplaceMap = extractLinkReplaceMap(properties);

        // ── Step 1: Seed copyContextMap with input twins ──────────────────
        // copyContextMap  : origTwinId → CopyContext  (tracks every twin involved in copying)
        // copyTwinIdsMap  : origTwinId → copyTwinId   (quick ID remapping for head references)
        var copyContextMap = new LinkedHashMap<UUID, CopyContext>();
        var copyTwinIdsMap = new HashMap<UUID, UUID>();
        var origTwins = new HashSet<TwinEntity>(inputFactoryItemList.size());

        for (var factoryItem : inputFactoryItemList) {
            var twin = factoryItem.getTwin();
            origTwins.add(twin);
            copyContextMap.put(twin.getId(), new CopyContext().setOrigFactoryItem(factoryItem));
        }

        // ── Step 2: Collect the full set of original twins ────────────────
        // 2a. Children via hierarchy search (ltree descendant query, bounded by depth)
        var search = new BasicSearch().setCheckViewPermission(false);
        search.setHierarchyChildrenSearch(
                new HierarchySearch()
                        .setDepth(depth)
                        .setIdList(copyContextMap.keySet())
        );

        if (!childrenStatusIds.isEmpty()) {
            search.addStatusId(childrenStatusIds, false);
        }

        // 2b. Links within the hierarchy (pre-loaded to avoid N+1 later)
        var linksData = findLinksData(copyContextMap.keySet(), linkReplaceMap, childrenStatusIds);
        var origTwinLinksGrouped = new KitGrouped<>(linksData.origTwinLinks(), TwinLinkEntity::getId, TwinLinkEntity::getSrcTwinId);

        // 2c. Hierarchy children
        var origTwinsChildren = twinSearchService.findTwins(search);
        origTwins.addAll(origTwinsChildren);

        // 2d. Twins reachable via configured links (outside the hierarchy tree)
        origTwins.addAll(findLinkedTwins(origTwins, extractLinkedTwinsByLinkIdMap(properties), childrenStatusIds));

        // ── Step 3: Filter — only twins whose class is in the replace map are eligible ──
        var twinsToCopyIds = origTwins.stream()
                .filter(twin -> classReplaceMap.containsKey(twin.getTwinClassId().toString()))
                .map(TwinEntity::getId)
                .collect(Collectors.toSet());

        // ── Step 4: Sort originals for correct processing order ───────────
        // Primary:   ascending hierarchy depth (parents before children)
        // Secondary: twins WITHOUT outgoing links first, so that when we process
        //            a twin WITH links, the dst twin's copy already exists in copyContextMap
        var origTwinsSorted = origTwins.stream()
                .sorted((t1, t2) -> {
                    var h1 = StringUtils.countMatches(t1.getHierarchyTree(), '.');
                    var h2 = StringUtils.countMatches(t2.getHierarchyTree(), '.');

                    var depthComparison = Integer.compare(h1, h2);
                    if (depthComparison != 0) {
                        return depthComparison;
                    }

                    boolean t1HasLinks = origTwinLinksGrouped.containsGroupedKey(t1.getId());
                    boolean t2HasLinks = origTwinLinksGrouped.containsGroupedKey(t2.getId());

                    return Boolean.compare(t1HasLinks, t2HasLinks);
                })
                .toList();

        // ── Step 5: Create copies and replicate links ─────────────────────
        for (var origTwin : origTwinsSorted) {
            if (!twinsToCopyIds.contains(origTwin.getId())) {
                continue;
            }

            var ctx = createCopyContext(origTwin, user, copyContextMap, copyTwinIdsMap, classReplaceMap);

            // Copy forward links (src=this twin) — dst copies are already in copyContextMap
            // thanks to the sorting order above
            if (origTwinLinksGrouped.containsGroupedKey(origTwin.getId())) {
                ctx.setLinksCopy(
                        copyForwardLinks(ctx.getTwinCopy(), origTwinLinksGrouped.getGrouped(origTwin.getId()), copyContextMap, twinsToCopyIds, linksData.newLinks(), linkReplaceMap)
                );
            }
        }

        // ── Step 6: Wrap each copy into a FactoryItem for downstream processing ──
        var ret = new ArrayList<FactoryItem>(copyContextMap.size());
        for (var ctx : copyContextMap.values()) {
            if (ctx.getTwinCopy() == null) {
                continue;
            }
            var twinCreate = new TwinCreate();
            twinCreate
                    .setLinksEntityList(ctx.getLinksCopy())
                    .setTwinEntity(ctx.getTwinCopy());
            ret.add(
                    new FactoryItem()
                            .setOutput(twinCreate)
                            .setContextFactoryItemList(List.of(ctx.getOrigFactoryItem()))
            );
        }

        return ret;
    }

    /**
     * Gets or creates a {@link CopyContext} for the given original twin.
     *
     * <p>For <b>input twins</b> the context already exists in copyContextMap (seeded in step 1).
     * For <b>children</b> discovered later, a new context is created on the fly via
     * {@code computeIfAbsent} — its origFactoryItem wraps the original twin in a
     * {@link TwinUpdate} so that downstream fillers can access the source twin's data.</p>
     *
     * <p>Head twin resolution for the copy:</p>
     * <pre>
     *   origTwin.headTwinId ──┬── present in copyTwinIdsMap? ── YES → use copy of head
     *                         └── NO → keep original headTwinId (head is outside copy scope)
     * </pre>
     */
    private CopyContext createCopyContext(TwinEntity origTwin, UserEntity user, Map<UUID, CopyContext> copyContextMap, Map<UUID, UUID> copyTwinIdsMap, Map<String, String> classReplaceMap) throws ServiceException {
        var copyContext = copyContextMap.computeIfAbsent(
                origTwin.getId(),
                k -> {
                    var parentContext = copyContextMap.get(origTwin.getHeadTwinId());
                    var parentContextItems = parentContext != null
                            ? List.of(parentContext.getOrigFactoryItem())
                            : Collections.<FactoryItem>emptyList();
                    return new CopyContext()
                            .setOrigFactoryItem(
                                    new FactoryItem()
                                            .setOutput(
                                                    new TwinUpdate().setDbTwinEntity(origTwin)
                                            )
                                            .setContextFactoryItemList(parentContextItems)
                            );
                }
        );

        if (copyContext.getTwinCopy() != null) {
            return copyContext; // already materialized — nothing to do
        }

        // Materialize the copy: replace the class, wire up head reference
        var newClassId = UUID.fromString(classReplaceMap.get(origTwin.getTwinClassId().toString()));
        var newClass = twinClassService.findEntitySafe(newClassId);

        var twinCopy = new TwinEntity()
                .setId(UuidUtils.generate())
                .setName("")
                .setTwinClass(newClass)
                .setTwinClassId(newClassId)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(user.getId())
                .setCreatedByUser(user);

        var origHeadTwinId = origTwin.getHeadTwinId();
        if (origHeadTwinId != null) {
            if (copyTwinIdsMap.containsKey(origHeadTwinId)) {
                // Parent was copied — point to the copy
                twinCopy.setHeadTwinId(copyTwinIdsMap.get(origHeadTwinId));
                var headContext = copyContextMap.get(origHeadTwinId);
                if (headContext != null && headContext.getTwinCopy() != null) {
                    twinCopy.setHeadTwin(headContext.getTwinCopy());
                }
            } else {
                // Parent is outside the copy scope — keep the original reference
                twinCopy.setHeadTwinId(origHeadTwinId);
            }
        }

        copyContext.setTwinCopy(twinCopy);
        copyTwinIdsMap.put(origTwin.getId(), twinCopy.getId());

        return copyContext;
    }

    /**
     * Replicates forward links (where the given twin is src) to their copy equivalents.
     *
     * <p>For each original link {@code srcOrig ──linkType──→ dstOrig}, produces
     * {@code srcCopy ──newLinkType──→ dstCopy}, where:</p>
     * <ul>
     *   <li>srcCopy  = the already-created copy of the source twin (passed in)</li>
     *   <li>dstCopy  = looked up from copyContextMap by dstTwinId (guaranteed to exist
     *       because of the depth-then-links sorting order)</li>
     *   <li>newLinkType = resolved from linksReplaceMap</li>
     * </ul>
     *
     * <p>Links whose dst twin was excluded from copying (e.g. its class is not in
     * classReplaceMap) are not replicated — there is no copy to point to.</p>
     */
    private List<TwinLinkEntity> copyForwardLinks(TwinEntity srcTwinCopy, List<TwinLinkEntity> origTwinLinks, Map<UUID, CopyContext> copyContextMap, Set<UUID> twinsToCopyIds, Kit<LinkEntity, UUID> newLinks, Map<UUID, UUID> linkReplaceMap) {
        var linksCopy = new ArrayList<TwinLinkEntity>(origTwinLinks.size());

        for (var origTwinLink : origTwinLinks) {
            // dst twin was not selected for copying (class not in classReplaceMap) — no copy to link to
            if (!twinsToCopyIds.contains(origTwinLink.getDstTwinId())) {
                continue;
            }

            var dstCtx = copyContextMap.get(origTwinLink.getDstTwinId());
            if (dstCtx == null || dstCtx.getTwinCopy() == null) {
                continue;
            }

            var dstTwinCopy = dstCtx.getTwinCopy();
            var newLinkId = linkReplaceMap.get(origTwinLink.getLinkId());

            var linkCopy = new TwinLinkEntity()
                    .setId(UuidUtils.generate())
                    .setSrcTwinId(srcTwinCopy.getId())
                    .setSrcTwin(srcTwinCopy)
                    .setDstTwinId(dstTwinCopy.getId())
                    .setDstTwin(dstTwinCopy)
                    .setLinkId(newLinkId)
                    .setLink(newLinks.get(newLinkId))
                    .setCreatedByUserId(origTwinLink.getCreatedByUserId())
                    .setCreatedByUser(origTwinLink.getCreatedByUser())
                    .setCreatedAt(Timestamp.from(Instant.now()));

            linksCopy.add(linkCopy);
        }

        return linksCopy;
    }

    /** Parses {@code linksReplaceMap} param into a typed UUID→UUID map. */
    private Map<UUID, UUID> extractLinkReplaceMap(Properties properties) {
        var extracted = linksReplaceMap.extract(properties);

        if (extracted.isEmpty()) {
            return Collections.emptyMap();
        }

        return extracted.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> UUID.fromString(entry.getKey()),
                        entry -> UUID.fromString(entry.getValue())
                ));
    }

    /** Parses {@code linkedTwinsByLinkIdMap} param into a typed UUID→Boolean map. */
    private Map<UUID, Boolean> extractLinkedTwinsByLinkIdMap(Properties properties) {
        var extracted = linkedTwinsByLinkIdMap.extract(properties);

        if (extracted.isEmpty()) {
            return Collections.emptyMap();
        }

        return extracted.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> UUID.fromString(entry.getKey()),
                        entry -> Boolean.parseBoolean(entry.getValue())
                ));
    }

    /**
     * Batch-loads all original links within the given hierarchies whose link type is in
     * {@code linkReplaceMap}, plus the replacement {@link LinkEntity} objects.
     * This is done upfront to avoid N+1 queries during link copying.
     */
    private LinksData findLinksData(Set<UUID> hierarchies, Map<UUID, UUID> linkReplaceMap, Set<UUID> childrenStatusIds) throws ServiceException {
        if (linkReplaceMap.isEmpty()) {
            return LinksData.EMPTY;
        }

        var origTwinLinks = childrenStatusIds.isEmpty()
                ? twinLinkService.findAllWithinHierarchiesAndLinkIdIn(hierarchies, linkReplaceMap.keySet())
                : twinLinkService.findAllWithinHierarchiesAndLinkIdInAndTwinsInStatusIds(hierarchies, linkReplaceMap.keySet(), childrenStatusIds);

        var newLinks = linkService.findEntitiesSafe(linkReplaceMap.values());

        return new LinksData(origTwinLinks, newLinks);
    }

    /**
     * Discovers additional twins that are connected to the hierarchy twins via configured links
     * (the {@code linkedTwinsByLinkIdMap} parameter), but live outside the hierarchy tree.
     * These twins are candidates for copying alongside the hierarchy.
     * Twins that are already part of the hierarchy are excluded from the result.
     */
    private Set<TwinEntity> findLinkedTwins(Collection<TwinEntity> hierarchyTwins, Map<UUID, Boolean> linkMap, Set<UUID> childrenStatusIds) throws ServiceException {
        if (linkMap == null || hierarchyTwins.isEmpty()) {
            return Collections.emptySet();
        }

        var hierarchyTwinIds = hierarchyTwins.stream()
                .map(TwinEntity::getId)
                .collect(Collectors.toSet());

        var linkedSearch = new BasicSearch().setCheckViewPermission(false);
        for (var link : linkMap.entrySet()) {
            if (link.getValue()) {
                linkedSearch
                        .addLinkSrcTwinsId(link.getKey(), hierarchyTwinIds, false, true);
            } else {
                linkedSearch
                        .addLinkDstTwinsId(link.getKey(), hierarchyTwinIds, false, true);
            }
        }

        if (!childrenStatusIds.isEmpty()) {
            linkedSearch.addStatusId(childrenStatusIds, false);
        }

        var linkedTwins = new HashSet<>(twinSearchService.findTwins(linkedSearch));
        linkedTwins.removeIf(twin -> hierarchyTwinIds.contains(twin.getId()));
        return linkedTwins;
    }
}