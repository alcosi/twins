package org.twins.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/**
 * Two-phase bootstrap pipeline that converts parsed glossary markdown into TWINS_GLOSSARY Twins.
 *
 * <ul>
 *   <li><b>DISCOVERY</b> — parse all .md files, load existing glossary Twins from DB, classify
 *       each parsed DTO into one of: CREATE / SKIP / UPDATE / RESTORE / MARK_DELETED.</li>
 *   <li><b>EXECUTE</b> — apply the plan via {@link TwinService#createTwin(TwinCreate)} /
 *       {@link TwinService#updateTwin(TwinUpdate)}. The 13 TwinField values are passed inside the
 *       {@code TwinCreate}/{@code TwinUpdate} payload as {@link FieldValue} objects — TwinService
 *       routes them to the correct per-type table (twin_field_simple / _non_indexed / _boolean /
 *       _timestamp) via the fieldTyper featurer on each {@link TwinClassFieldEntity}, applies
 *       validation, updates the search index, and records history.</li>
 * </ul>
 *
 * See {@code ai/plans/glossary-as-twins.md} §15.3 / §16 for the full design.
 *
 * <p><b>MVP scope (current):</b> TwinEntity + 13 TwinField values + status transitions
 * are implemented via TwinService. TwinLink (see_also) and TwinTag (category) reconciliation
 * are stubbed as TODO — they will be added as {@code setLinksEntityList} / {@code setTagsAddExisted}
 * on TwinCreate and {@code setTwinLinkCUD} / {@code setTagsDelete} on TwinUpdate in a follow-up.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlossaryBootstrapService {

    private final GlossaryMarkdownParser parser;
    private final TwinService twinService;
    private final TwinRepository twinRepository;
    private final TwinClassRepository twinClassRepository;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;  // read-only — for stored markdown_hash lookup

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
     * Declarative mapping: glossary DTO field → TwinClassField UUID, for all text-typed fields.
     * Order = definition order; iterated once per Twin in {@link #buildFieldValues}.
     * TwinService routes each value to the right per-type table via the fieldTyper featurer
     * on the TwinClassFieldEntity, so both indexed (1301) and non-indexed (1336) text share
     * {@link FieldValueText} here.
     */
    private record TextFieldMapping(UUID fieldId, Function<GlossaryEntityDto, String> extractor) {}

    private static final List<TextFieldMapping> TEXT_FIELD_MAPPINGS = List.of(
            new TextFieldMapping(FIELD_PURPOSE,            GlossaryEntityDto::sectionPurpose),
            new TextFieldMapping(FIELD_FIELDS,             GlossaryEntityDto::sectionFields),
            new TextFieldMapping(FIELD_RELATIONS_OVERVIEW, GlossaryEntityDto::sectionRelations),
            new TextFieldMapping(FIELD_API,                GlossaryEntityDto::sectionApi),
            new TextFieldMapping(FIELD_API_DEPRECATED,     GlossaryEntityDto::sectionApiDeprecated),
            new TextFieldMapping(FIELD_EXAMPLES,           GlossaryEntityDto::sectionExamples),
            new TextFieldMapping(FIELD_DEV_NOTES,          GlossaryEntityDto::sectionDevNotes),
            new TextFieldMapping(FIELD_JPA_CLASS,          GlossaryEntityDto::jpaClass),
            new TextFieldMapping(FIELD_DB_TABLE,           GlossaryEntityDto::dbTable),
            new TextFieldMapping(FIELD_MARKDOWN_SOURCE,    GlossaryEntityDto::markdownSource),
            new TextFieldMapping(FIELD_MARKDOWN_HASH,      GlossaryEntityDto::markdownHash)
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
            log.error("Glossary bootstrap: TWINS_GLOSSARY class {} not found in DB — migration V1.4.100.01 not applied? Skipping bootstrap", GLOSSARY_CLASS_ID);
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

    private Executed execute(BootstrapPlan plan, TwinClassEntity glossaryClass) {
        int created = executeCreates(plan.creates(), glossaryClass);

        // updates, restores, and markDeletes are all TwinUpdate under the hood — bundle into one batch.
        List<TwinUpdate> updateBatch = new ArrayList<>(
                plan.updates().size() + plan.restores().size() + plan.markDeletes().size());
        for (BootstrapPlan.Update u : plan.updates()) {
            updateBatch.add(buildTwinUpdate(u, glossaryClass));
        }
        for (BootstrapPlan.Update u : plan.restores()) {
            // RESTORE is an UPDATE that flips status DELETED → ACTUAL and refreshes fields.
            updateBatch.add(buildTwinUpdate(u, glossaryClass));
        }
        for (TwinEntity orphan : plan.markDeletes()) {
            updateBatch.add(buildMarkDeleteUpdate(orphan));
        }

        if (updateBatch.isEmpty()) {
            return new Executed(created, 0, 0, 0);
        }
        int updated  = plan.updates().size();
        int restored = plan.restores().size();
        int markDel  = plan.markDeletes().size();
        try {
            twinService.updateTwin(updateBatch, false);
        } catch (Exception e) {
            log.error("Glossary UPDATE batch failed ({} updates incl. {} restores, {} markDeletes): {}",
                    updateBatch.size(), restored, markDel, e.getMessage(), e);
            return new Executed(created, 0, 0, 0);
        }
        return new Executed(created, updated, restored, markDel);
    }

    /**
     * Single batched {@link TwinService#createTwinsAsync(java.util.List)} call for all new Twins.
     * Whole batch is atomic — one failure rolls back every Twin in the list.
     */
    private int executeCreates(List<GlossaryEntityDto> dtos, TwinClassEntity glossaryClass) {
        if (dtos.isEmpty()) return 0;
        List<TwinCreate> creates = new ArrayList<>(dtos.size());
        for (GlossaryEntityDto dto : dtos) {
            creates.add(buildTwinCreate(dto, glossaryClass));
        }
        try {
            twinService.createTwinsAsync(creates);
        } catch (Exception e) {
            log.error("Glossary CREATE batch failed ({} twins): {}", creates.size(), e.getMessage(), e);
            return 0;
        }
        return creates.size();
    }

    private TwinCreate buildTwinCreate(GlossaryEntityDto dto, TwinClassEntity glossaryClass) {
        TwinEntity twinEntity = new TwinEntity()
                .setId(dto.twinId())
                .setName(dto.title())
                .setDescription(dto.sectionSummary())
                .setTwinClassId(GLOSSARY_CLASS_ID)
                .setTwinStatusId(STATUS_ACTUAL)
                .setExternalId("glossary:" + dto.slug())
                .setCreatedByUserId(USER_SYSTEM);
        TwinCreate twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twinEntity);
        twinCreate.setCheckCreatePermission(false);
        for (FieldValue fv : buildFieldValues(dto, glossaryClass)) {
            twinCreate.addField(fv);
        }
        return twinCreate;
    }

    private TwinUpdate buildTwinUpdate(BootstrapPlan.Update u, TwinClassEntity glossaryClass) {
        TwinEntity dbTwin = u.dbTwin();
        TwinEntity updatedEntity = dbTwin.clone()
                .setName(u.dto().title())
                .setDescription(u.dto().sectionSummary())
                .setTwinStatusId(STATUS_ACTUAL);
        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate.setDbTwinEntity(dbTwin);
        twinUpdate.setTwinEntity(updatedEntity);
        twinUpdate.setCheckEditPermission(false);
        for (FieldValue fv : buildFieldValues(u.dto(), glossaryClass)) {
            twinUpdate.addField(fv);
        }
        return twinUpdate;
    }

    private TwinUpdate buildMarkDeleteUpdate(TwinEntity orphan) {
        TwinEntity updatedEntity = orphan.clone()
                .setTwinStatusId(STATUS_DELETED);
        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate.setDbTwinEntity(orphan);
        twinUpdate.setTwinEntity(updatedEntity);
        twinUpdate.setCheckEditPermission(false);
        return twinUpdate;
    }

    /**
     * Build the 13 FieldValue objects for a parsed DTO. Iterates the declarative
     * {@link #TEXT_FIELD_MAPPINGS} for text values; boolean and date are added separately
     * (different FieldValue subtypes). TwinService routes each value to the correct per-type
     * table based on the fieldTyper featurer ID on the TwinClassFieldEntity.
     */
    private List<FieldValue> buildFieldValues(GlossaryEntityDto dto, TwinClassEntity glossaryClass) {
        List<FieldValue> list = new ArrayList<>(13);
        for (TextFieldMapping m : TEXT_FIELD_MAPPINGS) {
            String value = m.extractor().apply(dto);
            if (value == null || value.isBlank()) continue;
            TwinClassFieldEntity field = glossaryClass.getTwinClassFieldKit().get(m.fieldId());
            if (field == null) {
                log.warn("Glossary field {} not in class kit — skipping value assignment", m.fieldId());
                continue;
            }
            list.add(new FieldValueText(field).setValue(value));
        }
        // Boolean (featurer 1306)
        TwinClassFieldEntity boolField = glossaryClass.getTwinClassFieldKit().get(FIELD_IS_SYSTEM);
        if (boolField != null) {
            list.add(new FieldValueBoolean(boolField).setValue(dto.isSystem()));
        }
        // Date (featurer 1302)
        TwinClassFieldEntity dateField = glossaryClass.getTwinClassFieldKit().get(FIELD_ACTUALIZED_AT);
        if (dateField != null && dto.actualizedAt() != null) {
            LocalDate date = dto.actualizedAt();
            list.add(new FieldValueDate(dateField, null).setDate(date.atStartOfDay()));
        }
        return list;
    }

    /** Internal execute counters. */
    private record Executed(int created, int updated, int restored, int markDeleted) {}
}
