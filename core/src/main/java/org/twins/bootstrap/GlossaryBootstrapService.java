package org.twins.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;
import java.util.function.Function;

import static org.twins.bootstrap.SystemEntityBootstrapData.*;

/**
 * Two-phase bootstrap pipeline that converts parsed glossary markdown into TWINS_GLOSSARY Twins.
 *
 * <ul>
 *   <li><b>DISCOVERY</b> — parse all .md files, load existing glossary Twins from DB, classify
 *       each parsed DTO into one of: CREATE / SKIP / UPDATE / RESTORE / MARK_DELETED.</li>
 *   <li><b>EXECUTE</b> — for each DTO, build a {@link SystemTwin} record and delegate to
 *       {@link SystemEntityBootstrapService#saveSystemTwin}. Persistence lives there so the
 *       logic is shared with the static {@code SYSTEM_TEMPLATE_TWINS} bootstrap.</li>
 * </ul>
 *
 * <p>This service owns the markdown → record mapping; the service layer owns the TwinEntity +
 * per-type-field routing. Glossary Twins bypass TwinService on purpose — they are system
 * metadata, not user content, and TwinService drags in permission checks, validation, aliases,
 * search index, history, and a request-scoped ApiUser that is not available on
 * ApplicationReadyEvent.</p>
 * <p>
 * See {@code ai/plans/glossary-as-twins.md} §15.3 / §16 for the full design.
 *
 * <p><b>MVP scope (current):</b> TwinEntity + 13 TwinField values + status transitions.
 * TwinLink (see_also) and TwinTag (category) reconciliation are stubbed as TODO — they will be
 * added in a follow-up.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlossaryBootstrapService {

    private final GlossaryMarkdownParser parser;
    private final TwinRepository twinRepository;
    private final TwinClassRepository twinClassRepository;
    private final TwinClassFieldService twinClassFieldService;
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
        twinClassFieldService.loadTwinClassFields(glossaryClass);

        BootstrapPlan plan = discover(dtos);
        Executed executed = execute(plan, glossaryClass);

        log.info("Glossary bootstrap done: created={}, updated={}, skipped={}, restored={}, markedDeleted={}",
                executed.created, executed.updated, plan.skips(), executed.restored, executed.markDeleted);
        return new GlossaryBootstrapResult(
                executed.created,
                executed.updated,
                plan.skips(),
                0,              // linksAdded — TODO MVP-2 (TwinLink reconciliation)
                0,              // linksRemoved — TODO MVP-2
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
                restores.add(new BootstrapPlan.Update(dto, existing, List.of()));
            } else {
                updates.add(new BootstrapPlan.Update(dto, existing, List.of()));
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

    private Executed execute(BootstrapPlan plan, TwinClassEntity glossaryClass) {
        int created = 0;
        for (GlossaryEntityDto dto : plan.creates()) {
            try {
                SystemTwin twin = toSystemTwin(dto, STATUS_ACTUAL);
                systemEntityBootstrapService.saveSystemTwin(twin, glossaryClass, EntitySmartService.SaveMode.ifNotPresentCreate, false);
                created++;
            } catch (Exception e) {
                log.error("Glossary CREATE failed for {}: {}", dto.slug(), e.getMessage(), e);
            }
        }
        int updated = 0;
        for (BootstrapPlan.Update u : plan.updates()) {
            try {
                SystemTwin twin = toSystemTwin(u.dto(), STATUS_ACTUAL);
                systemEntityBootstrapService.saveSystemTwin(twin, glossaryClass, EntitySmartService.SaveMode.saveAndLogOnException, true);
                updated++;
            } catch (Exception e) {
                log.error("Glossary UPDATE failed for {}: {}", u.dto().slug(), e.getMessage(), e);
            }
        }
        int restored = 0;
        for (BootstrapPlan.Update u : plan.restores()) {
            try {
                SystemTwin twin = toSystemTwin(u.dto(), STATUS_ACTUAL);
                systemEntityBootstrapService.saveSystemTwin(twin, glossaryClass, EntitySmartService.SaveMode.saveAndLogOnException, true);
                restored++;
            } catch (Exception e) {
                log.error("Glossary RESTORE failed for {}: {}", u.dto().slug(), e.getMessage(), e);
            }
        }
        int markDeleted = 0;
        for (TwinEntity orphan : plan.markDeletes()) {
            try {
                systemEntityBootstrapService.saveSystemTwinStatus(orphan.getId(), STATUS_DELETED);
                markDeleted++;
            } catch (ServiceException e) {
                log.error("Glossary MARK_DELETE failed for {}: {}", orphan.getId(), e.getMessage(), e);
            }
        }
        return new Executed(created, updated, restored, markDeleted);
    }

    /**
     * Convert a parsed markdown DTO into a {@link SystemTwin} record. Text fields are split into
     * indexed vs non-indexed lists at this layer (we know each glossary field's typer statically);
     * each list then maps 1:1 to its destination table in {@link SystemEntityBootstrapService},
     * with no fieldTyper routing needed at save time.
     */
    private SystemTwin toSystemTwin(GlossaryEntityDto dto, UUID statusId) {
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
                timestampFields);
    }

    /**
     * Internal execute counters.
     */
    private record Executed(int created, int updated, int restored, int markDeleted) {
    }
}
