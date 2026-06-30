package org.twins.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.enums.consts.SystemIds;

import java.util.*;
import java.util.function.Function;

import static org.twins.bootstrap.SystemEntityBootstrapData.*;

/**
 * Two-phase bootstrap pipeline that converts parsed glossary markdown into TWINS_GLOSSARY Twins.
 *
 * <ul>
 *   <li><b>DISCOVERY</b> — parse all .md files, load existing glossary Twins from DB, classify
 *       each parsed DTO into one of: CREATE / SKIP / UPDATE / RESTORE / MARK_DELETED.</li>
 *   <li><b>EXECUTE — PHASE 2a</b> — for each DTO, build a {@link SystemTwin} record and delegate
 *       to {@link SystemEntityBootstrapService#saveSystemTwin}. Persistence lives there so the
 *       logic is shared with the static {@code SYSTEM_TEMPLATE_TWINS} bootstrap.</li>
 *   <li><b>EXECUTE — PHASE 2b</b> — after every Twin is in DB, reconcile outgoing see_also
 *       TwinLinks via {@link SystemEntityBootstrapService#saveSystemTwinLinks}. Deferred to a
 *       second pass so see_also forward references resolve (a Twin may point to another that is
 *       itself being created in the same pass).</li>
 * </ul>
 *
 * <p>This service owns the markdown → record mapping; the service layer owns the TwinEntity +
 * per-type-field + TwinLink routing. Glossary Twins bypass TwinService on purpose — they are
 * system metadata, not user content, and TwinService drags in permission checks, validation,
 * aliases, search index, history, and a request-scoped ApiUser that is not available on
 * ApplicationReadyEvent.</p>
 * <p>
 * See {@code ai/plans/glossary-as-twins.md} §15.3 / §16 for the full design.
 *
 * <p><b>Current scope:</b> TwinEntity + 13 TwinField values + status transitions + see_also
 * TwinLink reconciliation. TwinTag (category) reconciliation is still a stub.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlossaryBootstrapService {

    private final GlossaryMarkdownParser parser;
    private final TwinRepository twinRepository;
    private final TwinClassRepository twinClassRepository;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;  // read-only — for stored markdown_hash lookup
    private final SystemEntityBootstrapService systemEntityBootstrapService;

    /**
     * Field IDs from SystemIds constants.
     */
    private static final UUID FIELD_PURPOSE = SystemIds.TwinClassField.Glossary.PURPOSE;
    private static final UUID FIELD_FIELDS = SystemIds.TwinClassField.Glossary.FIELDS;
    private static final UUID FIELD_RELATIONS_OVERVIEW = SystemIds.TwinClassField.Glossary.RELATIONS_OVERVIEW;
    private static final UUID FIELD_API = SystemIds.TwinClassField.Glossary.API;
    private static final UUID FIELD_API_DEPRECATED = SystemIds.TwinClassField.Glossary.API_DEPRECATED;
    private static final UUID FIELD_EXAMPLES = SystemIds.TwinClassField.Glossary.EXAMPLES;
    private static final UUID FIELD_DEV_NOTES = SystemIds.TwinClassField.Glossary.DEV_NOTES;
    private static final UUID FIELD_JPA_CLASS = SystemIds.TwinClassField.Glossary.JPA_CLASS;
    private static final UUID FIELD_DB_TABLE = SystemIds.TwinClassField.Glossary.DB_TABLE;
    private static final UUID FIELD_MARKDOWN_SOURCE = SystemIds.TwinClassField.Glossary.MARKDOWN_SOURCE;
    private static final UUID FIELD_MARKDOWN_HASH = SystemIds.TwinClassField.Glossary.MARKDOWN_HASH;
    private static final UUID FIELD_IS_SYSTEM = SystemIds.TwinClassField.Glossary.IS_SYSTEM;
    private static final UUID FIELD_ACTUALIZED_AT = SystemIds.TwinClassField.Glossary.ACTUALIZED_AT;

    private static final UUID STATUS_ACTUAL = SystemIds.TwinStatus.Glossary.INIT;
    private static final UUID STATUS_DELETED = SystemIds.TwinStatus.Glossary.DELETED;
    private static final UUID GLOSSARY_CLASS_ID = SystemIds.TwinClass.TWINS_GLOSSARY;
    private static final UUID LINK_SEE_ALSO = SystemIds.Link.GLOSSARY_SEE_ALSO;
    private static final UUID USER_SYSTEM = SystemIds.User.SYSTEM;

    /**
     * Declarative mapping: glossary DTO field → TwinClassField UUID + destination table.
     * {@code indexed=true} → {@code twin_field_simple} (fieldTyper 1301),
     * {@code indexed=false} → {@code twin_field_simple_non_indexed} (fieldTyper 1336).
     * Destination is decided here, at mapping construction time — SystemEntityBootstrapService
     * just writes each list to its table.
     */
    private record TextFieldMapping(UUID fieldId, boolean indexed, Function<GlossaryEntityDto, String> extractor) {
    }

    private static final List<TextFieldMapping> TEXT_FIELD_MAPPINGS = List.of(
            // Non-indexed (fieldTyper 1336): long-form markdown sections
            new TextFieldMapping(FIELD_PURPOSE, false, GlossaryEntityDto::sectionPurpose),
            new TextFieldMapping(FIELD_FIELDS, false, GlossaryEntityDto::sectionFields),
            new TextFieldMapping(FIELD_RELATIONS_OVERVIEW, false, GlossaryEntityDto::sectionRelations),
            new TextFieldMapping(FIELD_API, false, GlossaryEntityDto::sectionApi),
            new TextFieldMapping(FIELD_API_DEPRECATED, false, GlossaryEntityDto::sectionApiDeprecated),
            new TextFieldMapping(FIELD_EXAMPLES, false, GlossaryEntityDto::sectionExamples),
            new TextFieldMapping(FIELD_DEV_NOTES, false, GlossaryEntityDto::sectionDevNotes),
            // Indexed (fieldTyper 1301): short identifier-like values
            new TextFieldMapping(FIELD_JPA_CLASS, true, GlossaryEntityDto::jpaClass),
            new TextFieldMapping(FIELD_DB_TABLE, true, GlossaryEntityDto::dbTable),
            new TextFieldMapping(FIELD_MARKDOWN_SOURCE, true, GlossaryEntityDto::markdownSource),
            new TextFieldMapping(FIELD_MARKDOWN_HASH, true, GlossaryEntityDto::markdownHash)
    );

    /**
     * Run the full pipeline. Single {@code @Transactional} — DB error anywhere rolls back the
     * whole batch. Invalid .md files were already dropped by the parser (logged WARN).
     */
    @Transactional(rollbackFor = Throwable.class)
    public GlossaryBootstrapResult bootstrap() {
        log.info("Glossary bootstrap starting");
        List<GlossaryEntityDto> dtos = parser.parseAll();
        if (dtos.isEmpty()) {
            log.warn("Glossary bootstrap: no valid markdown files found — nothing to do");
            return GlossaryBootstrapResult.empty(List.of());
        }
        TwinClassEntity glossaryClass = twinClassRepository.findById(GLOSSARY_CLASS_ID).orElse(null);
        if (glossaryClass == null) {
            log.error("Glossary bootstrap: TWINS_GLOSSARY class {} not found in DB — SystemEntityBootstrapService did not run? Skipping bootstrap", GLOSSARY_CLASS_ID);
            return GlossaryBootstrapResult.empty(List.of());
        }

        // Pre-compute the set of valid source slugs so dangling see_also references can be filtered
        // out before they would FK-violate on link insert. Markdown is the curated source of truth,
        // so unresolved slugs point to Twins that simply do not exist (yet).
        Set<String> knownSlugs = new HashSet<>(dtos.size());
        for (GlossaryEntityDto dto : dtos) {
            knownSlugs.add(dto.slug());
        }

        BootstrapPlan plan = discover(dtos);
        Executed executed = execute(plan, knownSlugs);

        log.info("Glossary bootstrap done: created={}, updated={}, skipped={}, restored={}, markedDeleted={}, linksAdded={}, linksRemoved={}",
                executed.created, executed.updated, plan.skips(), executed.restored, executed.markDeleted,
                executed.linksAdded, executed.linksRemoved);
        return new GlossaryBootstrapResult(
                executed.created,
                executed.updated,
                plan.skips(),
                executed.linksAdded,
                executed.linksRemoved,
                executed.markDeleted,
                executed.restored,
                List.of()       // invalidFiles — parser already logged them; not currently propagated
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PHASE 1: DISCOVERY
    // ──────────────────────────────────────────────────────────────────────────

    private BootstrapPlan discover(List<GlossaryEntityDto> dtos) {
        List<TwinEntity> existingTwins = twinRepository.findByTwinClassId(GLOSSARY_CLASS_ID);
        Map<UUID, TwinEntity> existingById = new HashMap<>();
        for (TwinEntity t : existingTwins) {
            existingById.put(t.getId(), t);
        }
        Map<UUID, String> storedHashByTwinId = loadStoredHashes(existingById.keySet());

        List<GlossaryEntityDto> creates = new ArrayList<>();
        List<BootstrapPlan.Update> updates = new ArrayList<>();
        List<BootstrapPlan.Update> restores = new ArrayList<>();
        int skips = 0;

        for (GlossaryEntityDto dto : dtos) {
            TwinEntity existing = existingById.get(dto.twinId());
            if (existing == null) {
                creates.add(dto);
                continue;
            }
            String storedHash = storedHashByTwinId.get(dto.twinId());
            boolean hashMatches = dto.markdownHash().equals(storedHash);
            boolean isDeleted = STATUS_DELETED.equals(existing.getTwinStatusId());
            if (hashMatches && !isDeleted) {
                skips++;
            } else if (hashMatches && isDeleted) {
                restores.add(new BootstrapPlan.Update(dto, existing));
            } else {
                updates.add(new BootstrapPlan.Update(dto, existing));
            }
        }

        // Orphans: existing Twins with ACTUAL status whose markdown_source file is gone
        List<TwinEntity> markDeletes = new ArrayList<>();
        Set<UUID> dtoIds = new HashSet<>();
        for (GlossaryEntityDto dto : dtos) {
            dtoIds.add(dto.twinId());
        }
        for (TwinEntity existing : existingTwins) {
            if (!dtoIds.contains(existing.getId()) && STATUS_ACTUAL.equals(existing.getTwinStatusId())) {
                markDeletes.add(existing);
            }
        }

        log.info("Glossary discovery: {} create(s), {} update(s), {} restore(s), {} skip(s), {} markDelete(s)",
                creates.size(), updates.size(), restores.size(), skips, markDeletes.size());
        return new BootstrapPlan(creates, updates, restores, markDeletes, skips);
    }

    /**
     * Batch-load markdown_hash values for all existing glossary Twins.
     * Returns Map<twinId, hashValue> — Twins without a stored hash are absent from the map.
     */
    private Map<UUID, String> loadStoredHashes(Collection<UUID> twinIds) {
        if (twinIds.isEmpty()) return Map.of();
        List<TwinFieldSimpleEntity> fields = twinFieldSimpleRepository
                .findByTwinIdInAndTwinClassFieldIdIn(twinIds, Set.of(FIELD_MARKDOWN_HASH));
        Map<UUID, String> result = new HashMap<>(fields.size());
        for (TwinFieldSimpleEntity f : fields) {
            result.put(f.getTwinId(), f.getValue());
        }
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PHASE 2: EXECUTE — delegate persistence to SystemEntityBootstrapService
    // ──────────────────────────────────────────────────────────────────────────

    private Executed execute(BootstrapPlan plan, Set<String> knownSlugs) {
        int created = 0;
        for (GlossaryEntityDto dto : plan.creates()) {
            try {
                SystemTwin twin = toSystemTwin(dto, STATUS_ACTUAL, knownSlugs);
                systemEntityBootstrapService.saveSystemTwin(twin, EntitySmartService.SaveMode.ifNotPresentCreate, false);
                created++;
            } catch (Exception e) {
                log.error("Glossary CREATE failed for {}: {}", dto.slug(), e.getMessage(), e);
            }
        }
        int updated = 0;
        for (BootstrapPlan.Update u : plan.updates()) {
            try {
                SystemTwin twin = toSystemTwin(u.dto(), STATUS_ACTUAL, knownSlugs);
                systemEntityBootstrapService.saveSystemTwin(twin, EntitySmartService.SaveMode.saveAndLogOnException, true);
                updated++;
            } catch (Exception e) {
                log.error("Glossary UPDATE failed for {}: {}", u.dto().slug(), e.getMessage(), e);
            }
        }
        int restored = 0;
        for (BootstrapPlan.Update u : plan.restores()) {
            try {
                SystemTwin twin = toSystemTwin(u.dto(), STATUS_ACTUAL, knownSlugs);
                systemEntityBootstrapService.saveSystemTwin(twin, EntitySmartService.SaveMode.saveAndLogOnException, true);
                restored++;
            } catch (Exception e) {
                log.error("Glossary RESTORE failed for {}: {}", u.dto().slug(), e.getMessage(), e);
            }
        }
        // MARK_DELETE — single bulk UPDATE for all orphans at once.
        int markDeleted = 0;
        if (!plan.markDeletes().isEmpty()) {
            Set<UUID> orphanIds = new HashSet<>(plan.markDeletes().size());
            for (TwinEntity orphan : plan.markDeletes()) {
                orphanIds.add(orphan.getId());
            }
            try {
                markDeleted = systemEntityBootstrapService.setTwinStatusForTwins(orphanIds, STATUS_DELETED);
            } catch (Exception e) {
                log.error("Glossary MARK_DELETE batch failed for {} twin(s): {}",
                        orphanIds.size(), e.getMessage(), e);
            }
        }

        // PHASE 2b — link reconciliation. Runs AFTER all Twins are persisted so see_also forward
        // references resolve. SKIPs are excluded (hash match → seeAlso unchanged). MARK_DELETEDs
        // are excluded — soft-deleted Twins keep their outgoing links for referential integrity.
        // Batched: one bulk DELETE per linkId across all participating Twins + one bulk INSERT,
        // regardless of how many Twins are in the pass.
        Map<UUID, List<SystemTwinLink>> linkBatch = new LinkedHashMap<>();
        for (GlossaryEntityDto dto : plan.creates()) {
            linkBatch.put(dto.twinId(), buildSeeAlsoLinks(dto, knownSlugs));
        }
        for (BootstrapPlan.Update u : plan.updates()) {
            linkBatch.put(u.dto().twinId(), buildSeeAlsoLinks(u.dto(), knownSlugs));
        }
        for (BootstrapPlan.Update u : plan.restores()) {
            linkBatch.put(u.dto().twinId(), buildSeeAlsoLinks(u.dto(), knownSlugs));
        }
        int linksAdded = 0;
        int linksRemoved = 0;
        if (!linkBatch.isEmpty()) {
            try {
                SystemEntityBootstrapService.LinkReconcileResult r =
                        systemEntityBootstrapService.saveSystemTwinLinksBatch(linkBatch);
                linksAdded = r.inserted();
                linksRemoved = r.deleted();
            } catch (Exception e) {
                log.error("Glossary link reconciliation batch failed: {}", e.getMessage(), e);
            }
        }
        return new Executed(created, updated, restored, markDeleted, linksAdded, linksRemoved);
    }

    /**
     * Convert a parsed markdown DTO into a {@link SystemTwin} record. Text fields are split into
     * indexed vs non-indexed lists at this layer (we know each glossary field's typer statically);
     * each list then maps 1:1 to its destination table in {@link SystemEntityBootstrapService},
     * with no fieldTyper routing needed at save time. The Twin also carries its desired see_also
     * link set, persisted separately in PHASE 2b.
     */
    private SystemTwin toSystemTwin(GlossaryEntityDto dto, UUID statusId, Set<String> knownSlugs) {
        List<SystemTwinFieldSimple> indexed = new ArrayList<>();
        List<SystemTwinFieldSimpleNonIndexed> nonIndexed = new ArrayList<>();
        for (TextFieldMapping m : TEXT_FIELD_MAPPINGS) {
            String value = m.extractor().apply(dto);
            if (value == null || value.isBlank()) continue;
            if (m.indexed()) {
                indexed.add(new SystemTwinFieldSimple(m.fieldId(), value));
            } else {
                nonIndexed.add(new SystemTwinFieldSimpleNonIndexed(m.fieldId(), value));
            }
        }
        List<SystemTwinFieldBoolean> booleanFields = List.of(
                new SystemTwinFieldBoolean(FIELD_IS_SYSTEM, dto.isSystem()));
        List<SystemTwinFieldTimestamp> timestampFields = dto.actualizedAt() == null
                ? List.of()
                : List.of(new SystemTwinFieldTimestamp(FIELD_ACTUALIZED_AT, dto.actualizedAt().atStartOfDay()));
        return new SystemTwin(
                dto.twinId(),
                GLOSSARY_CLASS_ID,
                statusId,
                dto.title(),
                dto.sectionSummary(),
                "glossary:" + dto.slug(),
                USER_SYSTEM,
                indexed,
                nonIndexed,
                booleanFields,
                timestampFields,
                buildSeeAlsoLinks(dto, knownSlugs));
    }

    /**
     * Map the DTO's {@code see_also} slug set to {@link SystemTwinLink} records. Slugs that do not
     * resolve to a known parsed file are dropped (with WARN) — otherwise the link insert would
     * FK-violate against {@code twin.id}.
     */
    private List<SystemTwinLink> buildSeeAlsoLinks(GlossaryEntityDto dto, Set<String> knownSlugs) {
        if (dto.seeAlso() == null || dto.seeAlso().isEmpty()) return List.of();
        List<SystemTwinLink> links = new ArrayList<>(dto.seeAlso().size());
        for (String slug : dto.seeAlso()) {
            if (!knownSlugs.contains(slug)) {
                log.warn("Glossary {}: see_also references unknown slug '{}' — dropping link", dto.slug(), slug);
                continue;
            }
            links.add(new SystemTwinLink(LINK_SEE_ALSO, GlossaryEntityDto.computeTwinId(slug)));
        }
        return links;
    }

    /**
     * Internal execute counters.
     */
    private record Executed(int created, int updated, int restored, int markDeleted,
                            int linksAdded, int linksRemoved) {
    }
}
