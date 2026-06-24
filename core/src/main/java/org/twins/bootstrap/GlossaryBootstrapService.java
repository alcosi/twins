package org.twins.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Two-phase bootstrap pipeline that converts parsed glossary markdown into TWINS_GLOSSARY Twins.
 *
 * <ul>
 *   <li><b>DISCOVERY</b> — parse all .md files, load existing glossary Twins from DB, classify
 *       each parsed DTO into one of: CREATE / SKIP / UPDATE / RESTORE / MARK_DELETED.</li>
 *   <li><b>EXECUTE</b> — apply the plan via {@link TwinService#createTwin(TwinEntity)} /
 *       {@link TwinService#updateTwin(TwinUpdate)} for the TwinEntity itself, then write the
 *       13 {@code TwinClassField} values directly through the per-type TwinField repositories
 *       (text / non-indexed text / boolean / timestamp).</li>
 * </ul>
 *
 * See {@code ai/plans/glossary-as-twins.md} §15.3 / §16 for the full design.
 *
 * <p><b>MVP scope (current):</b> TwinEntity creation + 13 TwinField values + status transitions
 * are implemented. TwinLink (see_also) and TwinTag (category) reconciliation are stubbed as
 * TODO — they require additional exploration of {@code TwinLinkEntity} / {@code TwinTagEntity}
 * construction APIs and will be added in a follow-up.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlossaryBootstrapService {

    private final GlossaryMarkdownParser parser;
    private final TwinService twinService;
    private final TwinRepository twinRepository;
    private final TwinClassRepository twinClassRepository;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final TwinFieldSimpleNonIndexedRepository twinFieldNonIndexedRepository;
    private final TwinFieldBooleanRepository twinFieldBooleanRepository;
    private final TwinFieldTimestampRepository twinFieldTimestampRepository;

    /** Field IDs from SystemEntityService constants. */
    private static final UUID FIELD_PURPOSE            = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_PURPOSE;
    private static final UUID FIELD_FIELDS             = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_FIELDS;
    private static final UUID FIELD_RELATIONS_OVERVIEW = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_RELATIONS_OVERVIEW;
    private static final UUID FIELD_API                = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_API;
    private static final UUID FIELD_API_DEPRECATED     = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_API_DEPRECATED;
    private static final UUID FIELD_EXAMPLES           = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_EXAMPLES;
    private static final UUID FIELD_DEV_NOTES          = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_DEV_NOTES;
    private static final UUID FIELD_JPA_CLASS          = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_JPA_CLASS;
    private static final UUID FIELD_DB_TABLE           = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_DB_TABLE;
    private static final UUID FIELD_MARKDOWN_SOURCE    = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_MARKDOWN_SOURCE;
    private static final UUID FIELD_MARKDOWN_HASH      = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_MARKDOWN_HASH;
    private static final UUID FIELD_IS_SYSTEM          = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_IS_SYSTEM;
    private static final UUID FIELD_ACTUALIZED_AT      = SystemEntityService.TWIN_CLASS_FIELD_GLOSSARY_ACTUALIZED_AT;

    private static final UUID STATUS_ACTUAL  = SystemEntityService.TWIN_STATUS_GLOSSARY_ACTUAL;
    private static final UUID STATUS_DELETED = SystemEntityService.TWIN_STATUS_GLOSSARY_DELETED;
    private static final UUID GLOSSARY_CLASS_ID = SystemEntityService.TWIN_CLASS_TWINS_GLOSSARY;
    private static final UUID USER_SYSTEM = SystemEntityService.USER_SYSTEM;

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
        if (!twinClassRepository.existsById(GLOSSARY_CLASS_ID)) {
            log.error("Glossary bootstrap: TWINS_GLOSSARY class {} not found in DB — migration V1.4.100.01 not applied? Skipping bootstrap", GLOSSARY_CLASS_ID);
            return GlossaryBootstrapResult.empty(List.of());
        }

        BootstrapPlan plan = discover(dtos);
        Executed executed = execute(plan);

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
        Set<UUID> dtoIds = new java.util.HashSet<>();
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
    //  PHASE 2: EXECUTE
    // ──────────────────────────────────────────────────────────────────────────

    private Executed execute(BootstrapPlan plan) {
        int created = 0;
        int updated = 0;
        int restored = 0;
        int markDeleted = 0;

        for (GlossaryEntityDto dto : plan.creates()) {
            try {
                createGlossaryTwin(dto);
                created++;
            } catch (Exception e) {
                log.error("Glossary CREATE failed for slug '{}' ({}): {}", dto.slug(), dto.markdownSource(), e.getMessage(), e);
            }
        }
        for (BootstrapPlan.Update u : plan.updates()) {
            try {
                updateGlossaryTwin(u);
                updated++;
            } catch (Exception e) {
                log.error("Glossary UPDATE failed for slug '{}' ({}): {}", u.dto().slug(), u.dto().markdownSource(), e.getMessage(), e);
            }
        }
        for (BootstrapPlan.Update u : plan.restores()) {
            try {
                restoreGlossaryTwin(u);
                restored++;
            } catch (Exception e) {
                log.error("Glossary RESTORE failed for slug '{}' ({}): {}", u.dto().slug(), u.dto().markdownSource(), e.getMessage(), e);
            }
        }
        for (TwinEntity orphan : plan.markDeletes()) {
            try {
                markDeleted(orphan);
                markDeleted++;
            } catch (Exception e) {
                log.error("Glossary MARK_DELETED failed for twin {}: {}", orphan.getId(), e.getMessage(), e);
            }
        }
        return new Executed(created, updated, restored, markDeleted);
    }

    private void createGlossaryTwin(GlossaryEntityDto dto) throws Exception {
        TwinEntity twinEntity = baseTwinEntity(dto)
                .setId(dto.twinId())
                .setTwinClassId(GLOSSARY_CLASS_ID)
                .setTwinStatusId(STATUS_ACTUAL)
                .setExternalId("glossary:" + dto.slug());
        TwinEntity saved = twinService.createTwin(twinEntity);
        writeFields(saved.getId(), dto);
    }

    private void updateGlossaryTwin(BootstrapPlan.Update u) throws Exception {
        TwinEntity dbTwin = u.dbTwin();
        TwinEntity updatedEntity = dbTwin.clone()
                .setName(u.dto().title())
                .setDescription(u.dto().sections().get("Summary"))
                .setTwinStatusId(STATUS_ACTUAL);
        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate.setDbTwinEntity(dbTwin);
        twinUpdate.setTwinEntity(updatedEntity);
        twinUpdate.setCheckEditPermission(false);
        twinService.updateTwin(twinUpdate);
        deleteExistingFields(dbTwin.getId());
        writeFields(dbTwin.getId(), u.dto());
    }

    private void restoreGlossaryTwin(BootstrapPlan.Update u) throws Exception {
        // RESTORE is just an UPDATE that flips status DELETED → ACTUAL and refreshes fields.
        updateGlossaryTwin(u);
    }

    private void markDeleted(TwinEntity orphan) throws Exception {
        TwinEntity updatedEntity = orphan.clone()
                .setTwinStatusId(STATUS_DELETED);
        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate.setDbTwinEntity(orphan);
        twinUpdate.setTwinEntity(updatedEntity);
        twinUpdate.setCheckEditPermission(false);
        twinService.updateTwin(twinUpdate);
    }

    private TwinEntity baseTwinEntity(GlossaryEntityDto dto) {
        return new TwinEntity()
                .setName(dto.title())
                .setDescription(dto.sections().get("Summary"))
                .setCreatedByUserId(USER_SYSTEM);
    }

    /**
     * Write all 13 TwinField values for the given Twin + DTO. Replaces existing values —
     * caller is expected to have deleted old values first (see {@link #deleteExistingFields}).
     */
    private void writeFields(UUID twinId, GlossaryEntityDto dto) {
        // Long-text (non-indexed, featurer 1336)
        List<TwinFieldSimpleNonIndexedEntity> longFields = new ArrayList<>();
        addNonIndexed(longFields, twinId, FIELD_PURPOSE, dto.sections().get("Purpose"));
        addNonIndexed(longFields, twinId, FIELD_FIELDS, dto.sections().get("Fields"));
        addNonIndexed(longFields, twinId, FIELD_RELATIONS_OVERVIEW, dto.sections().get("Relations"));
        addNonIndexed(longFields, twinId, FIELD_API, dto.sections().get("API"));
        addNonIndexed(longFields, twinId, FIELD_API_DEPRECATED, dto.sections().get("API (deprecated)"));
        addNonIndexed(longFields, twinId, FIELD_EXAMPLES, dto.sections().get("Examples"));
        addNonIndexed(longFields, twinId, FIELD_DEV_NOTES, dto.sections().get("Dev notes"));
        if (!longFields.isEmpty()) {
            twinFieldNonIndexedRepository.saveAll(longFields);
        }

        // Short indexed-text (featurer 1301)
        List<TwinFieldSimpleEntity> simpleFields = new ArrayList<>();
        addSimple(simpleFields, twinId, FIELD_JPA_CLASS, dto.jpaClass());
        addSimple(simpleFields, twinId, FIELD_DB_TABLE, dto.dbTable());
        addSimple(simpleFields, twinId, FIELD_MARKDOWN_SOURCE, dto.markdownSource());
        addSimple(simpleFields, twinId, FIELD_MARKDOWN_HASH, dto.markdownHash());
        if (!simpleFields.isEmpty()) {
            twinFieldSimpleRepository.saveAll(simpleFields);
        }

        // Boolean (featurer 1306)
        TwinFieldBooleanEntity boolField = new TwinFieldBooleanEntity()
                .setTwinId(twinId)
                .setTwinClassFieldId(FIELD_IS_SYSTEM)
                .setValue(dto.isSystem());
        twinFieldBooleanRepository.save(boolField);

        // Timestamp (featurer 1302) — store actualized_at as start-of-day UTC
        LocalDate date = dto.actualizedAt();
        Timestamp ts = Timestamp.from(date.atStartOfDay(ZoneId.of("UTC")).toInstant());
        TwinFieldTimestampEntity dateField = new TwinFieldTimestampEntity()
                .setTwinId(twinId)
                .setTwinClassFieldId(FIELD_ACTUALIZED_AT)
                .setValue(ts);
        twinFieldTimestampRepository.save(dateField);
    }

    private static void addNonIndexed(List<TwinFieldSimpleNonIndexedEntity> list, UUID twinId, UUID fieldId, String value) {
        if (value == null || value.isBlank()) return;
        list.add(new TwinFieldSimpleNonIndexedEntity()
                .setTwinId(twinId)
                .setTwinClassFieldId(fieldId)
                .setValue(value));
    }

    private static void addSimple(List<TwinFieldSimpleEntity> list, UUID twinId, UUID fieldId, String value) {
        if (value == null) return;
        list.add(new TwinFieldSimpleEntity()
                .setTwinId(twinId)
                .setTwinClassFieldId(fieldId)
                .setValue(value));
    }

    private void deleteExistingFields(UUID twinId) {
        // No `deleteByTwinId(UUID)` on these repositories — load by twinId, then deleteAll.
        // Each call is a small bounded list (one Twin → at most 13 fields of each type).
        twinFieldSimpleRepository.deleteAll(twinFieldSimpleRepository.findByTwinId(twinId));
        twinFieldNonIndexedRepository.deleteAll(twinFieldNonIndexedRepository.findByTwinId(twinId));
        twinFieldBooleanRepository.deleteAll(twinFieldBooleanRepository.findByTwinId(twinId));
        twinFieldTimestampRepository.deleteAll(twinFieldTimestampRepository.findByTwinId(twinId));
    }

    /** Internal execute counters. */
    private record Executed(int created, int updated, int restored, int markDeleted) {}
}
