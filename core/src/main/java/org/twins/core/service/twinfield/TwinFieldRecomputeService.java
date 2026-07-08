package org.twins.core.service.twinfield;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldRecomputeOnActionEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldRecomputeOnFieldEntity;
import org.twins.core.domain.EntityKey;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperRecomputed;
import org.twins.core.featurer.pointer.Pointer;
import org.twins.core.service.twin.TwinPointerService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
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
 *   <li>Walk collector: changed publisher fields + twin actions + referenced TwinClassField IDs</li>
 *   <li>Batch-load TwinClassFieldEntity for changed IDs, populate
 *       {@link TwinClassFieldEntity#getRecomputeOnField()} via
 *       {@link TwinClassFieldService#loadRecomputeOnField(Collection)} — cached lookup</li>
 *   <li>Batch-load TwinClassEntity for touched publisher classes, populate
 *       {@link TwinClassEntity#getRecomputeOnAction()} via
 *       {@link TwinClassService#loadRecomputeOnAction(Collection)} — cached lookup</li>
 *   <li>Batch-load TwinClassFieldEntity for subscriber side</li>
 *   <li>Build {@code (subscriberField, trigger)} pairs grouped by pointerId</li>
 *   <li>{@link #resolveToRequests} — one {@link Pointer#load} batch per pointerId, group by
 *       {@code (subscriberTwin, subscriberField)} → one {@link FieldRecomputeRequest} with N triggers</li>
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

    private final TwinPointerService twinPointerService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassService twinClassService;
    private final FeaturerService featurerService;
    private final TwinFieldDecimalService twinFieldDecimalService;
    private final TwinClassFieldRecomputeOnFieldService twinClassFieldRecomputeOnFieldService;

    @Value("${twins.mater.max-depth:5}")
    private int maxDepth;

    public void triggerAffected(TwinChangesCollector collector) throws ServiceException {
        triggerAffected(collector, new HashSet<>(), 0);
    }

    private void triggerAffected(TwinChangesCollector collector, Set<SubscriberKey> visited, int depth) throws ServiceException {
        if (depth > maxDepth) {
            log.warn("TwinFieldRecompute cascade depth {} exceeded max {}, skipping remaining", depth, maxDepth);
            return;
        }
        var decimalFieldKit = collectFieldWithRecompute(collector);
        Map<ActionKey, Set<TwinEntity>> twinsByAction = collectTwinActions(collector);

        if (decimalFieldKit.isEmpty() && twinsByAction.isEmpty()) return;

        // TODO TWINS-868 §4.2: bulk detection — if total touched twins > bulkThreshold,
        //   schedule consolidated async TwinChangeTaskEntity and return. Sync recompute on >50 twins
        //   risks tx timeout / pool exhaustion.


        // === Batch-load TwinClassEntity for touched publisher classes + populate recomputeOnAction kits ===
        Set<UUID> publisherClassIds = new HashSet<>();
        for (ActionKey ak : twinsByAction.keySet()) publisherClassIds.add(ak.twinClassId());
        Map<UUID, TwinClassEntity> publisherClassById = new HashMap<>();
        if (!publisherClassIds.isEmpty()) {
            for (TwinClassEntity c : twinClassService.findEntitiesSafe(publisherClassIds)) {
                publisherClassById.put(c.getId(), c);
            }
            twinClassService.loadRecomputeOnAction(publisherClassById.values());
        }

        if (changedFieldById.values().stream().noneMatch(this::hasOnFieldRules)
                && publisherClassById.values().stream().noneMatch(this::hasOnActionRules)) return;

        // === Batch-load TwinClassFieldEntity for subscriber side ===
        Set<UUID> subscriberFieldIds = new HashSet<>();
        for (TwinClassFieldEntity field : changedFieldById.values()) {
            if (field.getRecomputeOnField() == null) continue;
            for (TwinClassFieldRecomputeOnFieldEntity rule : field.getRecomputeOnField()) {
                subscriberFieldIds.add(rule.getSubscriberTwinClassFieldId());
            }
        }
        for (TwinClassEntity c : publisherClassById.values()) {
            if (c.getRecomputeOnAction() == null) continue;
            for (TwinClassFieldRecomputeOnActionEntity rule : c.getRecomputeOnAction()) {
                subscriberFieldIds.add(rule.getSubscriberTwinClassFieldId());
            }
        }
        Kit<TwinClassFieldEntity, UUID> subscriberFieldById = twinClassFieldService.findEntitiesSafe(subscriberFieldIds);

        // === Build pending (subscriberField, trigger) by pointerId ===
        Map<UUID, List<PendingPointerResolve>> pendingByPointer = new HashMap<>();
        collectOnFieldRecomputes(changedFieldById, publishersByChangedField, subscriberFieldById, pendingByPointer);
        collectOnActionRecomputes(publisherClassById, twinsByAction, subscriberFieldById, pendingByPointer);
        if (pendingByPointer.isEmpty()) return;

        // === Resolve pointers → build requests → dispatch ===
        List<FieldRecomputeRequest> requests = resolveToRequests(pendingByPointer, visited);
        for (FieldRecomputeRequest request : requests) {
            dispatchRecompute(request, collector, visited, depth);
        }
    }

    @NotNull
    private KitGroupedObj<TwinFieldDecimalEntity, UUID, UUID, TwinClassFieldEntity> collectFieldWithRecompute(TwinChangesCollector collector) throws ServiceException {
        var decimalFields = new ArrayList<TwinFieldDecimalEntity>();
        decimalFields.addAll(collector.getSaveEntities(TwinFieldDecimalEntity.class));
        decimalFields.addAll(collector.getDeletes(TwinFieldDecimalEntity.class));
        twinFieldDecimalService.loadTwinClassField(decimalFields);
        twinFieldDecimalService.loadTwin(decimalFields);
        var decimalFieldsKit = new KitGroupedObj<>(
                decimalFields,
                TwinFieldDecimalEntity::getId,
                TwinFieldDecimalEntity::getTwinClassFieldId,
                TwinFieldDecimalEntity::getTwinClassField);
        twinClassFieldService.loadRecomputeOnField(decimalFieldsKit.getGroupingObjectMap().values());
        List<TwinFieldDecimalEntity> hasRecomputes = null;
        List<TwinClassFieldRecomputeOnFieldEntity> recomputeOnFields = null;
        for (var groupedField : decimalFieldsKit.getGroupedList()) {
            var twinClassField = groupedField.left;
            var twinFieldsDecimal = groupedField.right;
            if (KitUtils.isNotEmpty(twinClassField.getRecomputeOnField())) {
                hasRecomputes = CollectionUtils.safeAdd(hasRecomputes, twinFieldsDecimal);
                recomputeOnFields = CollectionUtils.safeAdd(recomputeOnFields, twinClassField.getRecomputeOnField().getCollection());
            }
        }
        decimalFieldsKit.clear();
        if (hasRecomputes != null) {
            decimalFieldsKit.addAll(hasRecomputes);
            twinClassFieldRecomputeOnFieldService.loadSubscriberTwinPointer(recomputeOnFields);
            twinClassFieldRecomputeOnFieldService.loadSubscriberTwinClassField(recomputeOnFields);
        }
        return decimalFieldsKit;
    }

    private void registerChangedField(TwinFieldDecimalEntity field,
                                      Map<UUID, Set<TwinEntity>> publishersByChangedField,
                                      Set<UUID> changedFieldIds) {
        TwinEntity twin = field.getTwin();
        UUID fieldId = field.getTwinClassFieldId();
        if (twin == null || fieldId == null) return;
        changedFieldIds.add(fieldId);
        publishersByChangedField.computeIfAbsent(fieldId, k -> new HashSet<>()).add(twin);
    }

    private boolean hasOnFieldRules(TwinClassFieldEntity field) {
        return field.getRecomputeOnField() != null && !field.getRecomputeOnField().isEmpty();
    }

    private boolean hasOnActionRules(TwinClassEntity twinClass) {
        return twinClass.getRecomputeOnAction() != null && !twinClass.getRecomputeOnAction().isEmpty();
    }

    /**
     * Walks TwinEntity saves/deletes and groups them by {@code (twinClassId, action)}.
     * <p>
     * CREATE-vs-EDIT heuristic: brand-new twins are added via {@code collector.add(entity)} with no
     * field-level changes; updates go through {@code collector.add(entity, field, old, new)}. So an
     * empty {@link ChangesHelper} in the save map means CREATE. Fragile if a future caller adds field
     * changes during create — flagged for follow-up.
     */
    private Map<ActionKey, Set<TwinEntity>> collectTwinActions(TwinChangesCollector collector) {
        Map<ActionKey, Set<TwinEntity>> twinsByAction = new HashMap<>();
        Map<EntityKey, ChangesHelper> twinSaveHelpers = collector.getSaveEntityMap().get(TwinEntity.class);
        if (twinSaveHelpers != null) {
            for (Map.Entry<EntityKey, ChangesHelper> entry : twinSaveHelpers.entrySet()) {
                if (!(entry.getKey().entity() instanceof TwinEntity twin)) continue;
                if (twin.getTwinClassId() == null) continue;
                ChangesHelper helper = entry.getValue();
                TwinAction action = (helper != null && helper.hasChanges()) ? TwinAction.EDIT : TwinAction.CREATE;
                twinsByAction.computeIfAbsent(new ActionKey(twin.getTwinClassId(), action), k -> new HashSet<>()).add(twin);
            }
        }
        for (TwinEntity twin : collector.getDeletes(TwinEntity.class)) {
            if (twin.getTwinClassId() == null) continue;
            twinsByAction.computeIfAbsent(new ActionKey(twin.getTwinClassId(), TwinAction.DELETE), k -> new HashSet<>()).add(twin);
        }
        return twinsByAction;
    }

    private void collectOnFieldRecomputes(Map<UUID, TwinClassFieldEntity> changedFieldById,
                                          Map<UUID, Set<TwinEntity>> publishersByChangedField,
                                          Map<UUID, TwinClassFieldEntity> subscriberFieldById,
                                          Map<UUID, List<PendingPointerResolve>> pendingByPointer) {
        for (TwinClassFieldEntity changedField : changedFieldById.values()) {
            if (!hasOnFieldRules(changedField)) continue;
            Set<TwinEntity> publishers = publishersByChangedField.get(changedField.getId());
            if (publishers == null || publishers.isEmpty()) continue;
            for (TwinClassFieldRecomputeOnFieldEntity rule : changedField.getRecomputeOnField()) {
                TwinClassFieldEntity subscriberField = subscriberFieldById.get(rule.getSubscriberTwinClassFieldId());
                for (TwinEntity publisher : publishers) {
                    RecomputeTriggerOnField trigger = new RecomputeTriggerOnField(publisher, changedField, rule.isAsync());
                    pendingByPointer
                            .computeIfAbsent(rule.getSubscriberTwinPointerId(), k -> new ArrayList<>())
                            .add(new PendingPointerResolve(subscriberField, trigger));
                }
            }
        }
    }

    private void collectOnActionRecomputes(Map<UUID, TwinClassEntity> publisherClassById,
                                           Map<ActionKey, Set<TwinEntity>> twinsByAction,
                                           Map<UUID, TwinClassFieldEntity> subscriberFieldById,
                                           Map<UUID, List<PendingPointerResolve>> pendingByPointer) throws ServiceException {
        for (Map.Entry<ActionKey, Set<TwinEntity>> entry : twinsByAction.entrySet()) {
            ActionKey actionKey = entry.getKey();
            TwinClassEntity publisherClass = publisherClassById.get(actionKey.twinClassId());
            if (publisherClass == null || !hasOnActionRules(publisherClass)) continue;
            List<TwinClassFieldRecomputeOnActionEntity> rules = publisherClass.getRecomputeOnAction().getGrouped(actionKey.action());
            if (rules == null || rules.isEmpty()) continue;
            Set<TwinEntity> publishers = entry.getValue();
            if (publishers == null || publishers.isEmpty()) continue;
            for (TwinClassFieldRecomputeOnActionEntity rule : rules) {
                if (!shouldFireByValidators(rule, publishers)) continue;
                TwinClassFieldEntity subscriberField = subscriberFieldById.get(rule.getSubscriberTwinClassFieldId());
                for (TwinEntity publisher : publishers) {
                    RecomputeTriggerOnAction trigger = new RecomputeTriggerOnAction(publisher, actionKey.action(), rule.isAsync());
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
                if (subscriber == null || p.subscriberField() == null) continue;
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
     *
     * TODO TWINS-868: implement. For each active validator rule on this recompute rule, validate
     * the publisher twin; skip recompute if any rule rejects (e.g. child with status=DRAFT excluded
     * from parent's sum).
     */
    private boolean shouldFireByValidators(TwinClassFieldRecomputeOnActionEntity rule,
                                           Collection<TwinEntity> publisherTwins) {
        return true;
    }

    // === Inner data carriers ===

    /** (twinClassId, TwinAction) identity for grouping OnAction lookups. */
    private record ActionKey(UUID twinClassId, TwinAction action) {}

    /**
     * One pending trigger before pointer resolution: which subscriber field to recompute,
     * and the trigger that caused it. Subscriber twin is unknown until the pointer resolves.
     */
    private record PendingPointerResolve(
            TwinClassFieldEntity subscriberField,
            RecomputeTrigger trigger
    ) {}

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
