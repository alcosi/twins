package org.twins.core.service.twinfield;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclassfieldrecompute.TwinClassFieldRecomputeOnActionEntity;
import org.twins.core.dao.twinclassfieldrecompute.TwinClassFieldRecomputeOnFieldEntity;
import org.twins.core.domain.EntityKey;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperRecomputed;
import org.twins.core.featurer.pointer.Pointer;
import org.twins.core.service.twin.TwinPointerService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclassfield.recompute.*;

import java.util.*;

/**
 * Orchestrates Mater-field recomputes triggered by changes flowing through a {@link TwinChangesCollector}.
 *
 * <p>Hook point: called between {@code TwinService.validateAndCollect} and {@code TwinService.applyChanges}
 * so that recompute writes land in the same collector — atomicity with the publisher tx (sync flow).
 *
 * <p>Flow:
 * <ol>
 *   <li>{@link #extractAffected} — read changed fields and twin actions out of the collector</li>
 *   <li>{@link #preloadEntities} — one batch SQL for TwinClassFieldEntity metadata</li>
 *   <li>{@link #collectOnFieldRecomputes} / {@link #collectOnActionRecomputes} — build pending
 *       {@code (subscriberField, trigger)} pairs grouped by pointerId</li>
 *   <li>{@link #resolveToRequests} — for each pointerId one {@link Pointer#load} batch, then
 *       group by {@code (subscriberTwin, subscriberField)} → one {@link FieldRecomputeRequest} with N triggers</li>
 *   <li>{@link #dispatchRecompute} — locate FieldTyper, call {@link FieldTyperRecomputed#recompute}</li>
 * </ol>
 *
 * <p>Cycle protection via {@link SubscriberKey} visited-set; cascade via recursive
 * {@link #triggerAffected(TwinChangesCollector, Set, int)} after each dispatch — subscriber changes
 * land in the same collector and trigger a new wave up to {@code twins.mater.max-depth}.
 *
 * <p>MVP gaps (see ai/plans/field-typer-mater-listeners.md §7): validator rules, async outbox,
 * consolidated bulk task. All marked with {@code TODO} inline.
 */
@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinFieldRecomputeService {

    private final TwinClassFieldRecomputeOnFieldService recomputeOnFieldService;
    private final TwinClassFieldRecomputeOnActionService recomputeOnActionService;
    private final TwinClassFieldRecomputeOnActionValidatorRuleService validatorRuleService;
    private final TwinPointerService twinPointerService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinService twinService;
    private final FeaturerService featurerService;

    @Value("${twins.mater.max-depth:5}")
    private int maxDepth;

    public void triggerAffected(TwinChangesCollector collector) throws ServiceException {
        triggerAffected(collector, new HashSet<>(), 0);
    }

    private void triggerAffected(TwinChangesCollector collector, Set<SubscriberKey> visited, int depth) throws ServiceException {
        if (depth > maxDepth) {
            log.warn("TwinClassFieldRecompute cascade depth {} exceeded max {}, skipping remaining", depth, maxDepth);
            return;
        }
        AffectedSnapshot snapshot = extractAffected(collector);
        if (snapshot.isEmpty()) return;

        // TODO TWINS-868 §4.2: bulk detection — if snapshot.touchedTwinCount() > bulkThreshold,
        //   schedule consolidated async TwinChangeTaskEntity and return. Sync recompute on >50 twins
        //   risks tx timeout / pool exhaustion.

        PreloadedEntities preloaded = preloadEntities(snapshot);
        if (preloaded == null) return;

        // Step 1: collect pending (subscriberField, trigger) by pointerId.
        // One pointer may serve both OnField and OnAction rules — merging under one pointerId
        // means Pointer.load runs once for all of them.
        Map<UUID, List<PendingPointerResolve>> pendingByPointer = new HashMap<>();
        collectOnFieldRecomputes(snapshot, preloaded, pendingByPointer);
        collectOnActionRecomputes(snapshot, preloaded, pendingByPointer);
        if (pendingByPointer.isEmpty()) return;

        // Step 2: resolve pointers (one batch SQL per pointerId), group by subscriber → requests.
        List<FieldRecomputeRequest> requests = resolveToRequests(pendingByPointer, visited);
        if (requests.isEmpty()) return;

        // Step 3: dispatch one recompute per request. Cascade via recursion: subscriber writes
        // land in the same collector → the next wave picks up operand changes for chained Mater fields.
        for (FieldRecomputeRequest request : requests) {
            dispatchRecompute(request, collector, visited, depth);
        }
    }

    /**
     * Read publisher-side signals out of the collector:
     * - modified/deleted {@link TwinFieldDecimalEntity} → changed publisher fields (OnField source)
     * - saved {@link TwinEntity} → CREATE (ChangesHelper empty) or EDIT (ChangesHelper has changes)
     * - deleted {@link TwinEntity} → DELETE
     * <p>
     * Heuristic for CREATE-vs-EDIT: in current TwinService flow, brand-new twins are added via
     * {@code collector.add(entity)} with no field-level changes; updates go through
     * {@code collector.add(entity, field, old, new)}. So an empty ChangesHelper in the save map
     * means CREATE. This is fragile if a future caller adds field changes during create — flag
     * for follow-up.
     * <p>
     * TwinEntity publishers are taken directly from the collector — they are already in the
     * persistence context (managed), so no extra SQL is needed for them in {@link #preloadEntities}.
     */
    private AffectedSnapshot extractAffected(TwinChangesCollector collector) {
        Set<UUID> changedFieldClassIds = new HashSet<>();
        Map<UUID, Map<UUID, TwinEntity>> twinsByChangedField = new HashMap<>();
        Map<UUID, TwinEntity> publisherTwinById = new HashMap<>();

        for (TwinFieldDecimalEntity field : collector.getSaveEntities(TwinFieldDecimalEntity.class)) {
            registerChangedField(field, changedFieldClassIds, twinsByChangedField, publisherTwinById);
        }

        for (TwinFieldDecimalEntity field : collector.getDeletes(TwinFieldDecimalEntity.class)) {
            registerChangedField(field, changedFieldClassIds, twinsByChangedField, publisherTwinById);
        }

        Map<ActionKey, Map<UUID, TwinEntity>> twinsByAction = new HashMap<>();

        Map<EntityKey, ChangesHelper> twinSaveHelpers = collector.getSaveEntityMap().get(TwinEntity.class);
        if (twinSaveHelpers != null) {
            for (Map.Entry<EntityKey, ChangesHelper> entry : twinSaveHelpers.entrySet()) {
                if (!(entry.getKey().entity() instanceof TwinEntity twin) || twin.getTwinClassId() == null) {
                    continue;
                }

                TwinAction action = entry.getValue() != null && entry.getValue().hasChanges()
                        ? TwinAction.EDIT
                        : TwinAction.CREATE;

                twinsByAction
                        .computeIfAbsent(new ActionKey(twin.getTwinClassId(), action), k -> new HashMap<>())
                        .put(twin.getId(), twin);

                publisherTwinById.put(twin.getId(), twin);
            }
        }

        for (TwinEntity twin : collector.getDeletes(TwinEntity.class)) {
            if (twin.getTwinClassId() == null) {
                continue;
            }

            twinsByAction
                    .computeIfAbsent(new ActionKey(twin.getTwinClassId(), TwinAction.DELETE), k -> new HashMap<>())
                    .put(twin.getId(), twin);

            publisherTwinById.put(twin.getId(), twin);
        }

        return new AffectedSnapshot(
                changedFieldClassIds,
                twinsByChangedField,
                twinsByAction,
                publisherTwinById
        );
    }

    private void registerChangedField(TwinFieldDecimalEntity field, Set<UUID> changedFieldClassIds, Map<UUID, Map<UUID, TwinEntity>> twinsByChangedField, Map<UUID, TwinEntity> publisherTwinById) {
        TwinEntity twin = field.getTwin();
        UUID fieldId = field.getTwinClassFieldId();
        if (twin == null || fieldId == null) {
            return;
        }

        changedFieldClassIds.add(fieldId);
        twinsByChangedField
                .computeIfAbsent(fieldId, k -> new HashMap<>())
                .put(twin.getId(), twin);
        publisherTwinById.put(twin.getId(), twin);
    }

    /**
     * TwinEntity publishers are already in {@link AffectedSnapshot#publisherTwinById()} — managed
     * by the persistence context, no extra SQL. Here we only batch-load {@link TwinClassFieldEntity}
     * metadata for every field referenced by matched recompute rules (subscriber + operand side),
     * plus look up the recompute rules themselves via the cached services.
     */
    private PreloadedEntities preloadEntities(AffectedSnapshot snapshot) {
        List<TwinClassFieldRecomputeOnFieldEntity> onFieldRules =
                recomputeOnFieldService.findByPublisherTwinClassFieldIdIn(snapshot.changedFieldClassIds());

        Set<UUID> fieldIds = new HashSet<>();
        for (var rule : onFieldRules) {
            fieldIds.add(rule.getSubscriberTwinClassFieldId());
            fieldIds.add(rule.getPublisherTwinClassFieldId());
        }
        Map<ActionKey, List<TwinClassFieldRecomputeOnActionEntity>> onActionRulesByKey = new HashMap<>();
        for (ActionKey ak : snapshot.twinsByAction().keySet()) {
            List<TwinClassFieldRecomputeOnActionEntity> rules =
                    recomputeOnActionService.findByPublisherTwinClassIdAndPublisherTwinAction(ak.twinClassId(), ak.action());
            if (rules.isEmpty()) continue;
            onActionRulesByKey.put(ak, rules);
            for (var rule : rules) fieldIds.add(rule.getSubscriberTwinClassFieldId());
        }

        if (onFieldRules.isEmpty() && onActionRulesByKey.isEmpty()) return null;

        Map<UUID, TwinClassFieldEntity> fieldById = new HashMap<>();
        for (TwinClassFieldEntity field : twinClassFieldService.findTwinClassFields(fieldIds)) {
            fieldById.put(field.getId(), field);
        }

        return new PreloadedEntities(snapshot.publisherTwinById(), fieldById, onFieldRules, onActionRulesByKey);
    }

    private void collectOnFieldRecomputes(AffectedSnapshot snapshot, PreloadedEntities preloaded,
                                          Map<UUID, List<PendingPointerResolve>> pendingByPointer) {
        for (TwinClassFieldRecomputeOnFieldEntity rule : preloaded.onFieldRules()) {
            UUID changedFieldId = rule.getPublisherTwinClassFieldId();
            if (!snapshot.changedFieldClassIds().contains(changedFieldId)) continue;
            Map<UUID, TwinEntity> publishersForField = snapshot.twinsByChangedField().get(changedFieldId);
            if (publishersForField == null || publishersForField.isEmpty()) continue;
            TwinClassFieldEntity changedField = preloaded.fieldById().get(changedFieldId);
            TwinClassFieldEntity subscriberField = preloaded.fieldById().get(rule.getSubscriberTwinClassFieldId());
            for (TwinEntity publisherTwin : publishersForField.values()) {
                RecomputeTriggerOnField trigger = new RecomputeTriggerOnField(publisherTwin, changedField, rule.isAsync());
                pendingByPointer
                        .computeIfAbsent(rule.getSubscriberTwinPointerId(), k -> new ArrayList<>())
                        .add(new PendingPointerResolve(subscriberField, trigger));
            }
        }
    }

    private void collectOnActionRecomputes(AffectedSnapshot snapshot, PreloadedEntities preloaded,
                                           Map<UUID, List<PendingPointerResolve>> pendingByPointer) throws ServiceException {
        for (Map.Entry<ActionKey, List<TwinClassFieldRecomputeOnActionEntity>> entry : preloaded.onActionRulesByKey().entrySet()) {
            ActionKey actionKey = entry.getKey();
            Map<UUID, TwinEntity> publishers = snapshot.twinsByAction().get(actionKey);
            if (publishers == null || publishers.isEmpty()) continue;
            for (TwinClassFieldRecomputeOnActionEntity rule : entry.getValue()) {
                if (!shouldFireByValidators(rule, publishers.values())) continue;
                TwinClassFieldEntity subscriberField = preloaded.fieldById().get(rule.getSubscriberTwinClassFieldId());
                for (TwinEntity publisherTwin : publishers.values()) {
                    RecomputeTriggerOnAction trigger = new RecomputeTriggerOnAction(publisherTwin, actionKey.action(), rule.isAsync());
                    pendingByPointer
                            .computeIfAbsent(rule.getSubscriberTwinPointerId(), k -> new ArrayList<>())
                            .add(new PendingPointerResolve(subscriberField, trigger));
                }
            }
        }
    }

    /**
     * For each pointerId: one {@link TwinPointerService#getPointers} batch over unique publisher twins,
     * then group survivors by {@code (subscriberTwin, subscriberField)} — same subscriber+field fed by
     * multiple publishers collapse into one {@link FieldRecomputeRequest} with N triggers.
     */
    private List<FieldRecomputeRequest> resolveToRequests(Map<UUID, List<PendingPointerResolve>> pendingByPointer,
                                                          Set<SubscriberKey> visited) throws ServiceException {
        Map<SubscriberKey, List<RecomputeTrigger>> grouped = new LinkedHashMap<>();

        for (Map.Entry<UUID, List<PendingPointerResolve>> entry : pendingByPointer.entrySet()) {
            UUID pointerId = entry.getKey();
            List<PendingPointerResolve> pendings = entry.getValue();
            if (pendings.isEmpty()) continue;

            Collection<TwinEntity> publisherTwins = pendings.stream()
                    .map(p -> p.trigger().publisherTwin())
                    .distinct().toList();
            Map<UUID, TwinEntity> subscriberByPublisher;
            try {
                subscriberByPublisher = twinPointerService.getPointers(publisherTwins, pointerId);
            } catch (ServiceException ex) {
                log.warn("Pointer {} resolution failed for {} publishers, skipping pointer (cause: {})",
                        pointerId, publisherTwins.size(), ex.getMessage());
                continue;
            }

            for (PendingPointerResolve p : pendings) {
                TwinEntity subscriber = subscriberByPublisher.get(p.trigger().publisherTwin().getId());
                if (subscriber == null) continue;
                if (p.subscriberField() == null) continue;
                SubscriberKey key = new SubscriberKey(subscriber.getId(), p.subscriberField().getId(), subscriber, p.subscriberField());
                if (visited.contains(key)) continue;
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(p.trigger());
            }
        }

        List<FieldRecomputeRequest> requests = new ArrayList<>(grouped.size());
        for (var g : grouped.entrySet()) {
            requests.add(new FieldRecomputeRequest(
                    g.getKey().subscriberTwin(),
                    g.getKey().subscriberField(),
                    List.copyOf(g.getValue())));
        }
        return requests;
    }

    private void dispatchRecompute(FieldRecomputeRequest request, TwinChangesCollector collector,
                                   Set<SubscriberKey> visited, int depth) throws ServiceException {
        SubscriberKey key = new SubscriberKey(
                request.subscriberTwin().getId(),
                request.subscriberField().getId(),
                request.subscriberTwin(),
                request.subscriberField());
        if (!visited.add(key)) return;

        if (request.triggers().stream().anyMatch(RecomputeTrigger::async)) {
            // TODO TWINS-868 §4.2 / §7.15: async dispatch — serialize to UUID-only payload, push into
            //   TwinChangeTaskEntity via collector.addPostponed(...). Worker re-loads entities in its
            //   own tx and calls subscriber.recompute after the publisher tx commits.
            log.warn("Async Mater recompute not yet implemented, falling back to sync for {} on twin {}",
                    request.subscriberField().getId(), request.subscriberTwin().getId());
        }

        FieldTyper fieldTyper = featurerService.getFeaturer(
                request.subscriberField().getFieldTyperFeaturerId(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperRecomputed subscriber)) {
            log.debug("FieldTyper {} for field {} is not a FieldTyperRecomputed, skipping",
                    fieldTyper.getClass().getSimpleName(), request.subscriberField().getId());
            return;
        }

        subscriber.recompute(request, collector);

        // Cascade: serializeValue above may have written subscriber-field changes into collector.
        // Recurse to trigger the next wave (e.g. another Mater depends on this subscriber field).
        triggerAffected(collector, visited, depth + 1);
    }

    /**
     * Validator gate for OnAction rules. MVP returns true always — wiring the actual
     * {@code TwinValidatorSetService.isValid(...)} requires ContainsTwinValidatorSet plumbing
     * on the validator-rule entity (kit field + EAGER set), see ai/plans/field-typer-mater-listeners.md §3.4.
     * <p>
     * TODO TWINS-868: implement. For each active validator rule on this recompute rule, validate
     * the publisher twin; skip recompute if any rule rejects (e.g. child with status=DRAFT excluded
     * from parent's sum).
     */
    private boolean shouldFireByValidators(TwinClassFieldRecomputeOnActionEntity rule,
                                           Collection<TwinEntity> publisherTwins) {
        return true;
    }

    // === Inner data carriers ===

    /**
     * Publisher-side state extracted from the collector. Carries TwinEntity instances directly — no re-lookup needed.
     */
    private record AffectedSnapshot(
            Set<UUID> changedFieldClassIds,                                  // publisher field IDs touched
            Map<UUID, Map<UUID, TwinEntity>> twinsByChangedField,            // publisher twins per changed field
            Map<ActionKey, Map<UUID, TwinEntity>> twinsByAction,             // publisher twins per (class, action)
            Map<UUID, TwinEntity> publisherTwinById                          // union — used for pointer resolution
    ) {
        boolean isEmpty() {
            return changedFieldClassIds.isEmpty() && twinsByAction.isEmpty();
        }
    }

    /**
     * (twinClassId, TwinAction) identity for grouping OnAction lookups.
     */
    private record ActionKey(UUID twinClassId, TwinAction action) {
    }

    /**
     * Pre-loaded TwinEntity publishers + TwinClassFieldEntity metadata + matched recompute rules.
     * Filled once per {@link #triggerAffected} wave; reused by both collect methods.
     */
    private record PreloadedEntities(
            Map<UUID, TwinEntity> publisherTwinById,
            Map<UUID, TwinClassFieldEntity> fieldById,
            List<TwinClassFieldRecomputeOnFieldEntity> onFieldRules,
            Map<ActionKey, List<TwinClassFieldRecomputeOnActionEntity>> onActionRulesByKey
    ) {
    }

    /**
     * One pending trigger before pointer resolution: which subscriber field to recompute,
     * and the trigger that caused it. Subscriber twin is unknown until the pointer resolves.
     */
    private record PendingPointerResolve(
            TwinClassFieldEntity subscriberField,
            RecomputeTrigger trigger
    ) {
    }

    /**
     * Cycle-protection key + cargo entities. equals/hashCode on UUID pair only — JPA entity
     * hashCode is not stable, so we cannot use TwinEntity as a HashMap key directly. Entity
     * fields are cargo for dispatchRecompute to skip a re-findById.
     */
    private record SubscriberKey(
            UUID subscriberTwinId,
            UUID subscriberFieldId,
            TwinEntity subscriberTwin,             // cargo — not part of identity
            TwinClassFieldEntity subscriberField   // cargo — not part of identity
    ) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SubscriberKey k)) return false;
            return subscriberTwinId.equals(k.subscriberTwinId) && subscriberFieldId.equals(k.subscriberFieldId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subscriberTwinId, subscriberFieldId);
        }
    }
}
