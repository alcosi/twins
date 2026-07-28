package org.twins.core.service.recompute;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.recompute.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.recomputer.Recomputer;
import org.twins.core.service.twin.TwinPointerService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinValidatorSetService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinfield.TwinFieldDecimalService;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Parallel recompute engine (TWINS-893) that dispatches through a pluggable {@link Recomputer} featurer.
 * Works on the new {@code twin_recompute_*} tables; the legacy {@code TwinFieldRecomputeService} and its
 * {@code twin_class_field_recompute_*} tables are left untouched. The recomputer config lives on the
 * subscriber row and becomes part of the recompute target identity, so different recomputers for the same
 * (subscriber twin, field) split into separate requests automatically.
 */
@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinRecomputeService {

    private final TwinPointerService twinPointerService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassService twinClassService;
    private final FeaturerService featurerService;
    private final TwinFieldDecimalService twinFieldDecimalService;
    private final TwinService twinService;
    private final TwinRecomputeOnFieldService twinRecomputeOnFieldService;
    private final TwinRecomputeOnActionService twinRecomputeOnActionService;
    private final TwinValidatorSetService twinValidatorSetService;

    @Value("${twins.mater.max-depth:5}")
    private int maxDepth;

    public void triggerAffected(TwinChangesCollector collector) throws ServiceException {
        triggerAffected(collector, new RecomputePlan());
    }

    private void triggerAffected(TwinChangesCollector collector, RecomputePlan recomputePlan) throws ServiceException {
        recomputePlan.newLoop();
        if (recomputePlan.getCurrentLoop() > maxDepth) {
            log.warn("TwinRecompute cascade depth {} exceeded max {}, skipping remaining", recomputePlan.getCurrentLoop(), maxDepth);
            return;
        }
        if (recomputePlan.getCurrentLoop() == 1 || recomputePlan.hasRecomputedFields(collector)) {
            collectFieldWithRecompute(collector, recomputePlan);
        }
        if (recomputePlan.getCurrentLoop() == 1) { // no twins CUD can be done on 2+ loop
            collectTwinActions(collector, recomputePlan);
        }

        if (recomputePlan.getLoop().isEmpty()) return;

        resolvePointers(recomputePlan);
        List<DispatchTask> tasks = buildRecomputeRequests(recomputePlan);
        if (tasks.isEmpty()) return; // nothing dispatched -> no new writes -> no cascade needed
        for (DispatchTask task : tasks) {
            dispatchRecompute(task.request(), collector, task.recomputerFeaturerId(), task.recomputerParams());
        }
        triggerAffected(collector, recomputePlan);
    }

    private void collectFieldWithRecompute(TwinChangesCollector collector, RecomputePlan recomputePlan) throws ServiceException {
        if (collector.getSaveEntities(TwinFieldDecimalEntity.class).isEmpty() && collector.getDeletes(TwinFieldDecimalEntity.class).isEmpty())
            return;
        var unprocessedDecimalFields = new ArrayList<TwinFieldDecimalEntity>();
        for (var decimalField : collector.getSaveEntities(TwinFieldDecimalEntity.class)) {
            if (recomputePlan.isVisitedPublisher(toKey(decimalField))) {
                continue; //circle protection
            }
            unprocessedDecimalFields.add(decimalField);
        }
        for (var decimalField : collector.getDeletes(TwinFieldDecimalEntity.class)) {
            if (recomputePlan.isVisitedPublisher(toKey(decimalField))) {
                continue; //circle protection
            }
            unprocessedDecimalFields.add(decimalField);
        }
        if (unprocessedDecimalFields.isEmpty()) {
            return;
        }
        twinFieldDecimalService.loadTwinClassField(unprocessedDecimalFields);
        twinFieldDecimalService.loadTwin(unprocessedDecimalFields);
        var decimalFieldsKit = new KitGroupedObj<>(
                unprocessedDecimalFields,
                TwinFieldDecimalEntity::getId,
                TwinFieldDecimalEntity::getTwinClassFieldId,
                TwinFieldDecimalEntity::getTwinClassField);
        twinClassFieldService.loadRecomputeOnFieldV2(decimalFieldsKit.getGroupingObjectMap().values());
        List<TwinFieldDecimalEntity> hasRecomputes = null;
        List<TwinRecomputeOnFieldEntity> recomputeOnFields = null;
        for (var groupedField : decimalFieldsKit.getGroupedList()) {
            var twinClassField = groupedField.left;
            var twinFieldsDecimal = groupedField.right;
            if (KitUtils.isNotEmpty(twinClassField.getRecomputeOnFieldV2())) {
                hasRecomputes = CollectionUtils.safeAdd(hasRecomputes, twinFieldsDecimal);
                recomputeOnFields = CollectionUtils.safeAdd(recomputeOnFields, twinClassField.getRecomputeOnFieldV2().getCollection());
            }
        }
        if (hasRecomputes == null)
            return;
        decimalFieldsKit.clear();
        decimalFieldsKit.addAll(hasRecomputes);
        twinRecomputeOnFieldService.loadSubscriber(recomputeOnFields);
        twinRecomputeOnFieldService.loadValidators(recomputeOnFields);
        for (var triggerField : decimalFieldsKit.getCollection()) {
            TwinEntity publisherTwin = triggerField.getTwin();
            for (var recomputeOnField : triggerField.getTwinClassField().getRecomputeOnFieldV2()) {
                if (!passesCondition(recomputeOnField, triggerField)
                        || !passesValidatorRules(publisherTwin, recomputeOnField.getValidatorRulesKit(), TwinRecomputeOnFieldValidatorRuleEntity::isActive)) {
                    continue; // publisher twin failed every active validator set for this rule
                }
                recomputePlan.add(triggerField, recomputeOnField);
            }
        }
    }

    private void collectTwinActions(TwinChangesCollector collector, RecomputePlan recomputePlan) throws ServiceException {
        if (collector.getSaveEntities(TwinEntity.class).isEmpty() && collector.getDeletes(TwinEntity.class).isEmpty())
            return;
        var twins = new ArrayList<TwinEntity>();
        var twinActionMap = new HashMap<UUID, TwinAction>();
        for (var twin : collector.getSaveEntities(TwinEntity.class)) {
            twins.add(twin);
            twinActionMap.put(twin.getId(), twin.isCreateElseUpdate() ? TwinAction.CREATE : TwinAction.EDIT);
        }
        for (var twin : collector.getDeletes(TwinEntity.class)) {
            twins.add(twin);
            twinActionMap.put(twin.getId(), TwinAction.DELETE);
        }
        twinService.loadClass(twins);
        var twinKit = new KitGroupedObj<>(
                twins,
                TwinEntity::getId,
                TwinEntity::getTwinClassId,
                TwinEntity::getTwinClass);
        twinClassService.loadRecomputeOnActionV2(twinKit.getGroupingObjectMap().values());
        List<TwinEntity> hasRecomputes = null;
        List<TwinRecomputeOnActionEntity> recomputeOnActions = null;
        for (var groupedTwin : twinKit.getGroupedList()) {
            var twinClass = groupedTwin.left;
            var twinsByTwinClass = groupedTwin.right;
            if (KitUtils.isNotEmpty(twinClass.getRecomputeOnActionV2())) {
                hasRecomputes = CollectionUtils.safeAdd(hasRecomputes, twinsByTwinClass);
                recomputeOnActions = CollectionUtils.safeAdd(recomputeOnActions, twinClass.getRecomputeOnActionV2().getCollection());
            }
        }
        if (hasRecomputes == null)
            return;
        twinKit.clear();
        twinKit.addAll(hasRecomputes);
        twinRecomputeOnActionService.loadSubscriber(recomputeOnActions);
        twinRecomputeOnActionService.loadValidators(recomputeOnActions);
        for (var twin : twinKit.getCollection()) {
            for (var recomputeOnAction : twin.getTwinClass().getRecomputeOnActionV2()) {
                if (!passesValidatorRules(twin, recomputeOnAction.getValidatorRulesKit(), TwinRecomputeOnActionValidatorRuleEntity::isActive)) {
                    continue; // publisher twin failed every active validator set for this rule
                }
                recomputePlan.add(twin, twinActionMap.get(twin.getId()), recomputeOnAction);
            }
        }
    }

    /**
     * Returns true if the publisher field's current value passes the rule's {@link ConditionEvaluator}.
     */
    private boolean passesCondition(TwinRecomputeOnFieldEntity recomputeOnField, TwinFieldDecimalEntity triggerField) throws ServiceException {
        FieldValueText currentValue = new FieldValueText(triggerField.getTwinClassField())
                .setValue(triggerField.getValue() != null ? triggerField.getValue().toPlainString() : null);
        ConditionEvaluator evaluator = featurerService.getFeaturer(recomputeOnField.getConditionEvaluatorFeaturerId(), ConditionEvaluator.class);
        return evaluator.evaluate(recomputeOnField.getConditionEvaluatorParams(), currentValue);
    }

    /**
     * Returns true if the twin passes the rule's validator sets (OR-ed): no rules → pass; else pass if any
     * active validator set validates. {@code activePredicate} selects active rule-containers (the {@code active}
     * flag lives on the concrete rule entity, not on {@link ContainsTwinValidatorSet}).
     */
    private <R extends ContainsTwinValidatorSet> boolean passesValidatorRules(TwinEntity twin, Kit<R, UUID> rules, Predicate<R> activePredicate) throws ServiceException {
        if (KitUtils.isEmpty(rules)) {
            return true;
        }
        var activeRules = rules.getCollection().stream().filter(activePredicate).toList();
        return activeRules.isEmpty() || twinValidatorSetService.isValid(List.of(twin), activeRules).get(twin.getId()).isValid();
    }

    private static String toKey(TwinFieldDecimalEntity twinFieldDecimal) {
        return toKey(twinFieldDecimal.getTwinId(), twinFieldDecimal.getTwinClassFieldId());
    }

    private static String toKey(UUID twinId, UUID twinClassFieldId) {
        return twinId + ":" + twinClassFieldId;
    }

    @Data
    private static class RecomputePlan {
        private Kit<TwinRecomputeSubscriberEntity, UUID> subscriberKit;
        private Kit<TwinPointerEntity, UUID> subscriberTwinPointerKit;
        private Kit<TwinClassFieldEntity, UUID> subscriberTwinClassFieldKit;
        private Kit<TwinEntity, UUID> publisherTwinsKit;
        private Kit<TwinEntity, UUID> subscriberTwinsKit;
        private Map<UUID, List<TwinEntity>> publisherTwinsByPointerId;
        private Map<UnresolvedSubscriber, TwinEntity> subscriberTwinByUnresolvedSubscriber;
        private int currentLoop = 0;
        private Map<RecomputeTarget, Set<String>> appliedPublishersByTarget; // target -> publisher keys already applied
        private Set<String> visitedPublishers;
        private boolean initialized = false;
        private List<RecomputePlanLoop> loops;

        private void init() {
            if (!initialized) {
                subscriberKit = new Kit<>(TwinRecomputeSubscriberEntity::getId);
                subscriberTwinPointerKit = new Kit<>(TwinPointerEntity::getId);
                subscriberTwinClassFieldKit = new Kit<>(TwinClassFieldEntity::getId);
                publisherTwinsKit = new Kit<>(TwinEntity::getId);
                subscriberTwinsKit = new Kit<>(TwinEntity::getId);
                subscriberTwinByUnresolvedSubscriber = new HashMap<>();
                publisherTwinsByPointerId = new HashMap<>();
                appliedPublishersByTarget = new HashMap<>();
                visitedPublishers = new HashSet<>();
                initialized = true;
            }
            getLoop().init();
        }

        public RecomputePlanLoop getLoop() {
            if (currentLoop == 0) {
                newLoop();
            }
            return loops.get(currentLoop - 1);
        }

        public void add(TwinFieldDecimalEntity triggerField, TwinRecomputeOnFieldEntity recomputeOnField) {
            init();
            visitedPublishers.add(toKey(triggerField));
            TwinRecomputeSubscriberEntity subscriber = recomputeOnField.getSubscriber();
            if (subscriber == null || subscriber.getSubscriberTwinPointer() == null || subscriber.getSubscriberTwinClassField() == null) {
                log.warn("Recompute rule {} has unresolved subscriber, skipping", recomputeOnField.logShort());
                return;
            }
            subscriberKit.add(subscriber);
            subscriberTwinPointerKit.add(subscriber.getSubscriberTwinPointer());
            subscriberTwinClassFieldKit.add(subscriber.getSubscriberTwinClassField());
            publisherTwinsKit.add(triggerField.getTwin());
            publisherTwinsByPointerId
                    .computeIfAbsent(subscriber.getSubscriberTwinPointerId(), _ -> new ArrayList<>())
                    .add(triggerField.getTwin());
            var unresolvedPointer = new UnresolvedSubscriber(triggerField.getTwinId(), subscriber.getId());
            getLoop().recomputeTriggersByUnresolvedPointer
                    .computeIfAbsent(unresolvedPointer, _ -> new HashMap<>())
                    .computeIfAbsent(subscriber.getSubscriberTwinClassFieldId(), _ -> new ArrayList<>())
                    .add(new RecomputeTriggerOnField(triggerField.getTwin(), triggerField.getTwinClassField(), recomputeOnField.isAsync()));
        }

        public void add(TwinEntity twin, TwinAction twinAction, TwinRecomputeOnActionEntity recomputeOnAction) {
            init();
            TwinRecomputeSubscriberEntity subscriber = recomputeOnAction.getSubscriber();
            if (subscriber == null || subscriber.getSubscriberTwinPointer() == null || subscriber.getSubscriberTwinClassField() == null) {
                log.warn("Recompute rule {} has unresolved subscriber, skipping", recomputeOnAction.logShort());
                return;
            }
            subscriberKit.add(subscriber);
            subscriberTwinPointerKit.add(subscriber.getSubscriberTwinPointer());
            subscriberTwinClassFieldKit.add(subscriber.getSubscriberTwinClassField());
            publisherTwinsKit.add(twin);
            publisherTwinsByPointerId
                    .computeIfAbsent(subscriber.getSubscriberTwinPointerId(), _ -> new ArrayList<>())
                    .add(twin);
            var unresolvedPointer = new UnresolvedSubscriber(twin.getId(), subscriber.getId());
            getLoop().recomputeTriggersByUnresolvedPointer
                    .computeIfAbsent(unresolvedPointer, _ -> new HashMap<>())
                    .computeIfAbsent(subscriber.getSubscriberTwinClassFieldId(), _ -> new ArrayList<>())
                    .add(new RecomputeTriggerOnAction(twin, twinAction, recomputeOnAction.isAsync()));
        }

        public void resolveSubscriberTwins() {
            for (var unresolvedPointer : getLoop().recomputeTriggersByUnresolvedPointer.keySet()) {
                var subscriber = subscriberKit.get(unresolvedPointer.subscriberId());
                if (subscriber == null) {
                    continue;
                }
                var subscriberTwin = publisherTwinsKit.get(unresolvedPointer.publisherTwinId()).getPointer(subscriber.getSubscriberTwinPointerId());
                if (subscriberTwin == null) {
                    log.warn("No subscriber was detected by {}", unresolvedPointer);
                    continue;
                }
                subscriberTwinByUnresolvedSubscriber.put(unresolvedPointer, subscriberTwin);
                subscriberTwinsKit.add(subscriberTwin);
            }
        }

        public void groupTriggersBySubscriber() {
            for (var entry : getLoop().recomputeTriggersByUnresolvedPointer.entrySet()) {
                var unresolvePointer = entry.getKey();
                var fieldTriggersMap = entry.getValue();
                var subscriberTwin = subscriberTwinByUnresolvedSubscriber.get(unresolvePointer);
                if (subscriberTwin == null) {
                    log.warn("No subscriber was detected by {}", unresolvePointer);
                    continue;
                }
                var subscriber = subscriberKit.get(unresolvePointer.subscriberId());
                for (var fieldTrigger : fieldTriggersMap.entrySet()) {
                    var subscriberFieldId = fieldTrigger.getKey();
                    var triggers = fieldTrigger.getValue();
                    var target = new RecomputeTarget(
                            subscriberTwin.getId(),
                            subscriberFieldId,
                            subscriber.getRecomputerFeaturerId(),
                            subscriber.getRecomputerParams());
                    getLoop().recomputeTriggersByTarget
                            .computeIfAbsent(target, _ -> new ArrayList<>())
                            .addAll(triggers);
                }
            }
        }

        public boolean isVisitedPublisher(String key) {
            return visitedPublishers != null && visitedPublishers.contains(key);
        }

        /** true if any of publisherKeys is new for this target (and then all of them are recorded). */
        public boolean markAppliedPublishers(RecomputeTarget target, Set<String> publisherKeys) {
            return appliedPublishersByTarget
                    .computeIfAbsent(target, k -> new HashSet<>())
                    .addAll(publisherKeys);
        }

        public boolean isEmpty() {
            return !initialized;
        }

        public void newLoop() {
            this.currentLoop++;
            if (this.loops == null) {
                loops = new ArrayList<>();
            }
            this.loops.add(new RecomputePlanLoop());
        }

        public boolean hasRecomputedFields(TwinChangesCollector collector) {
            if (visitedPublishers == null) {
                return true;
            }
            int collectedDecimalFieldsCount = collector.getSaveEntities(TwinFieldDecimalEntity.class).size() + collector.getDeletes(TwinFieldDecimalEntity.class).size();
            if (collectedDecimalFieldsCount == 0) {
                return false;
            }
            if (collectedDecimalFieldsCount != visitedPublishers.size()) { //some new fields were added
                return true;
            }
            return false;
        }

        private record UnresolvedSubscriber(UUID publisherTwinId, UUID subscriberId) {
            @Override
            public String toString() {
                return "subscriber[fromTwinId:" + publisherTwinId + ", subscriberId:" + subscriberId + "]";
            }
        }

        /** Unit of recompute work: same target merges triggers; different recomputer -> separate target. */
        private record RecomputeTarget(UUID subscriberTwinId, UUID subscriberFieldId,
                                       Integer recomputerFeaturerId, HashMap<String, String> recomputerParams) {
        }

        @Data
        private static class RecomputePlanLoop {
            private Map<UnresolvedSubscriber, Map<UUID, List<RecomputeTrigger>>> recomputeTriggersByUnresolvedPointer;
            private Map<RecomputeTarget, List<RecomputeTrigger>> recomputeTriggersByTarget;

            public void init() {
                if (recomputeTriggersByUnresolvedPointer == null) {
                    recomputeTriggersByUnresolvedPointer = new HashMap<>();
                    recomputeTriggersByTarget = new HashMap<>();
                }
            }

            public boolean isEmpty() {
                return MapUtils.isEmpty(recomputeTriggersByUnresolvedPointer);
            }
        }
    }

    private void resolvePointers(RecomputePlan recomputePlan) throws ServiceException {
        for (var entry : recomputePlan.getPublisherTwinsByPointerId().entrySet()) {
            var pointerId = entry.getKey();
            var publisherTwins = entry.getValue();
            var twinPointer = recomputePlan.getSubscriberTwinPointerKit().get(pointerId);
            twinPointerService.loadPointer(publisherTwins, twinPointer); //todo support pointers merge
        }
        recomputePlan.resolveSubscriberTwins();
        recomputePlan.groupTriggersBySubscriber();
    }

    private List<DispatchTask> buildRecomputeRequests(RecomputePlan recomputePlan) {
        List<DispatchTask> tasks = new ArrayList<>();
        for (var entry : recomputePlan.getLoop().getRecomputeTriggersByTarget().entrySet()) {
            var target = entry.getKey();
            var triggers = entry.getValue();
            Set<String> publisherKeys = triggers.stream()
                    .map(RecomputeTrigger::publisherKey)
                    .collect(Collectors.toSet());
            if (!recomputePlan.markAppliedPublishers(target, publisherKeys)) {
                continue; // all publishers already applied for this target — duplicate, skip
            }
            tasks.add(new DispatchTask(
                    new FieldRecomputeRequest(
                            recomputePlan.getSubscriberTwinsKit().get(target.subscriberTwinId()),
                            recomputePlan.getSubscriberTwinClassFieldKit().get(target.subscriberFieldId()),
                            triggers),
                    target.recomputerFeaturerId(),
                    target.recomputerParams()));
        }
        return tasks;
    }

    private void dispatchRecompute(FieldRecomputeRequest request, TwinChangesCollector collector,
                                   Integer recomputerFeaturerId, HashMap<String, String> recomputerParams) throws ServiceException {
        if (request.triggers().stream().anyMatch(RecomputeTrigger::async)) {
            //TODO
            log.warn("Async Mater recompute not yet implemented, falling back to sync for {} on twin {}",
                    request.subscriberField().getId(), request.subscriberTwin().getId());
        }
        Recomputer recomputer = featurerService.getFeaturer(recomputerFeaturerId, Recomputer.class);
        recomputer.recompute(request, collector, recomputerParams);
    }

    private record DispatchTask(FieldRecomputeRequest request, Integer recomputerFeaturerId,
                                HashMap<String, String> recomputerParams) {
    }
}
