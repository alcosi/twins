package org.twins.core.service.twinfield;

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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldRecomputeOnActionEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldRecomputeOnFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperRecomputed;
import org.twins.core.service.twin.TwinPointerService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.recompute.*;

import java.util.*;

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
    private final TwinClassFieldRecomputeOnActionService twinClassFieldRecomputeOnActionService;
    private final TwinService twinService;

    @Value("${twins.mater.max-depth:5}")
    private int maxDepth;

    public void triggerAffected(TwinChangesCollector collector) throws ServiceException {
        triggerAffected(collector, new RecomputePlan());
    }

    private void triggerAffected(TwinChangesCollector collector, RecomputePlan recomputePlan) throws ServiceException {
        recomputePlan.newLoop();
        if (recomputePlan.getCurrentLoop() > maxDepth) {
            log.warn("TwinFieldRecompute cascade depth {} exceeded max {}, skipping remaining", recomputePlan.getCurrentLoop(), maxDepth);
            return;
        }
        if (recomputePlan.getCurrentLoop() == 1 || recomputePlan.hasRecomputedFields(collector)) {
            collectFieldWithRecompute(collector, recomputePlan);
        }
        if (recomputePlan.getCurrentLoop() == 1) { // no twins CUD can be done on 2+ loop
            collectTwinActions(collector, recomputePlan);
        }

        if (recomputePlan.getLoop().isEmpty()) return;

        // TODO TWINS-868 §4.2: bulk detection — if total touched twins > bulkThreshold,
        //   schedule consolidated async TwinChangeTaskEntity and return. Sync recompute on >50 twins
        //   risks tx timeout / pool exhaustion.

        resolvePointers(recomputePlan);
        // === Resolve pointers → build requests → dispatch ===
        List<FieldRecomputeRequest> requests = buildRecomputeRequests(recomputePlan);
        if (requests.isEmpty()) return;  // nothing dispatched → no new writes → no cascade needed
        for (FieldRecomputeRequest request : requests) {
            dispatchRecompute(request, collector);
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
        if (hasRecomputes == null)
            return;
        decimalFieldsKit.clear();
        decimalFieldsKit.addAll(hasRecomputes);
        twinClassFieldRecomputeOnFieldService.loadSubscriberTwinPointer(recomputeOnFields);
        twinClassFieldRecomputeOnFieldService.loadSubscriberTwinClassField(recomputeOnFields);
        for (var triggerField : decimalFieldsKit.getCollection()) {
            for (var recomputeOnField : triggerField.getTwinClassField().getRecomputeOnField()) {
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
        twinClassService.loadRecomputeOnAction(twinKit.getGroupingObjectMap().values());
        List<TwinEntity> hasRecomputes = null;
        List<TwinClassFieldRecomputeOnActionEntity> recomputeOnActions = null;
        for (var groupedTwin : twinKit.getGroupedList()) {
            var twinClass = groupedTwin.left;
            var twinsByTwinClass = groupedTwin.right;
            if (KitUtils.isNotEmpty(twinClass.getRecomputeOnAction())) {
                hasRecomputes = CollectionUtils.safeAdd(hasRecomputes, twinsByTwinClass);
                recomputeOnActions = CollectionUtils.safeAdd(recomputeOnActions, twinClass.getRecomputeOnAction().getCollection());
            }
        }
        if (hasRecomputes == null)
            return;
        twinKit.clear();
        twinKit.addAll(hasRecomputes);
        twinClassFieldRecomputeOnActionService.loadSubscriberTwinPointer(recomputeOnActions);
        twinClassFieldRecomputeOnActionService.loadSubscriberTwinClassField(recomputeOnActions);

        for (var twin : twinKit.getCollection()) {
            for (var recomputeOnAction : twin.getTwinClass().getRecomputeOnAction()) {
                recomputePlan.add(twin, twinActionMap.get(twin.getId()), recomputeOnAction);
            }
        }
    }

    private static String toKey(TwinFieldDecimalEntity twinFieldDecimal) {
        return toKey(twinFieldDecimal.getTwinId(), twinFieldDecimal.getTwinClassFieldId());
    }

    private static String toKey(UUID twinId, UUID twinClassFieldId) {
        return twinId + ":" + twinClassFieldId;
    }

    @Data
    private static class RecomputePlan {
        private Set<UUID> subscriberTwinPointerIds;
        private Set<UUID> subscriberTwinClassFieldIds;
        private Kit<TwinPointerEntity, UUID> subscriberTwinPointerKit;
        private Kit<TwinClassFieldEntity, UUID> subscriberTwinClassFieldKit;
        private Kit<TwinEntity, UUID> publisherTwinsKit;
        private Kit<TwinEntity, UUID> subscriberTwinsKit;
        private Map<UUID, List<TwinEntity>> publisherTwinsByPointerId;
        private Map<UnresolvedPointer, TwinEntity> subscriberTwinByUnresolvedPointer;
        private int currentLoop = 0;
        private Set<String> visitedSubscribers;
        private Set<String> visitedPublishers;
        private List<RecomputePlanLoop> loops;

        private void init() {
            if (subscriberTwinPointerIds == null) {
                subscriberTwinPointerIds = new HashSet<>();
                subscriberTwinClassFieldIds = new HashSet<>();
                subscriberTwinPointerKit = new Kit<>(TwinPointerEntity::getId);
                subscriberTwinClassFieldKit = new Kit<>(TwinClassFieldEntity::getId);
                publisherTwinsKit = new Kit<>(TwinEntity::getId);
                subscriberTwinsKit = new Kit<>(TwinEntity::getId);
                subscriberTwinByUnresolvedPointer = new HashMap<>();
                publisherTwinsByPointerId = new HashMap<>();
                visitedSubscribers = new HashSet<>();
                visitedPublishers = new HashSet<>();
            }
            getLoop().init();
        }

        public RecomputePlanLoop getLoop() {
            if (currentLoop == 0)
                newLoop();
            return loops.get(currentLoop - 1);
        }

        public void add(TwinFieldDecimalEntity triggerField, TwinClassFieldRecomputeOnFieldEntity recomputeOnField) {
            init();
            visitedPublishers.add(toKey(triggerField));
            subscriberTwinPointerIds.add(recomputeOnField.getSubscriberTwinPointerId());
            subscriberTwinClassFieldIds.add(recomputeOnField.getSubscriberTwinClassFieldId());
            subscriberTwinPointerKit.add(recomputeOnField.getSubscriberTwinPointer());
            subscriberTwinClassFieldKit.add(recomputeOnField.getSubscriberTwinClassField());
            publisherTwinsKit.add(triggerField.getTwin());
            publisherTwinsByPointerId
                    .computeIfAbsent(recomputeOnField.getSubscriberTwinPointerId(), _ -> new ArrayList<>())
                    .add(triggerField.getTwin());
            var unresolvedPointer = new UnresolvedPointer(triggerField.getTwinId(), recomputeOnField.getSubscriberTwinPointerId());
            getLoop().recomputeTriggersByUnresolvedPointer
                    .computeIfAbsent(unresolvedPointer, _ -> new HashMap<>())
                    .computeIfAbsent(recomputeOnField.getSubscriberTwinClassFieldId(), _ -> new ArrayList<>())
                    .add(new RecomputeTriggerOnField(triggerField.getTwin(), triggerField.getTwinClassField(), recomputeOnField.isAsync()));
        }

        public void add(TwinEntity twin, TwinAction twinAction, TwinClassFieldRecomputeOnActionEntity recomputeOnAction) {
            init();
            subscriberTwinPointerIds.add(recomputeOnAction.getSubscriberTwinPointerId());
            subscriberTwinClassFieldIds.add(recomputeOnAction.getSubscriberTwinClassFieldId());
            subscriberTwinPointerKit.add(recomputeOnAction.getSubscriberTwinPointer());
            subscriberTwinClassFieldKit.add(recomputeOnAction.getSubscriberTwinClassField());
            publisherTwinsKit.add(twin);
            publisherTwinsByPointerId
                    .computeIfAbsent(recomputeOnAction.getSubscriberTwinPointerId(), _ -> new ArrayList<>())
                    .add(twin);
            var unresolvedPointer = new UnresolvedPointer(twin.getId(), recomputeOnAction.getSubscriberTwinPointerId());
            getLoop().recomputeTriggersByUnresolvedPointer
                    .computeIfAbsent(unresolvedPointer, _ -> new HashMap<>())
                    .computeIfAbsent(recomputeOnAction.getSubscriberTwinClassFieldId(), _ -> new ArrayList<>())
                    .add(new RecomputeTriggerOnAction(twin, twinAction, recomputeOnAction.isAsync()));
        }

        public void resolveSubscriberTwins() {
            for (var unresolvedPointer : getLoop().recomputeTriggersByUnresolvedPointer.keySet()) {
                var subscriberTwin = publisherTwinsKit.get(unresolvedPointer.publisherTwinId).getPointer(unresolvedPointer.twinPointerId);
                if (subscriberTwin == null) {
                    log.warn("No subscriber was detected by {}", unresolvedPointer);
                    continue;
                }
                subscriberTwinByUnresolvedPointer.put(unresolvedPointer, subscriberTwin);
                subscriberTwinsKit.add(subscriberTwin);
            }
        }

        public void groupTriggersBySubscriber() {
            for (var entry : getLoop().recomputeTriggersByUnresolvedPointer.entrySet()) {
                var unresolvePointer = entry.getKey();
                var fieldTriggersMap = entry.getValue();
                var subscriberTwin = subscriberTwinByUnresolvedPointer.get(unresolvePointer);
                if (subscriberTwin == null) {
                    log.warn("No subscriber was detected by {}", unresolvePointer);
                    continue;
                }
                for (var fieldTrigger : fieldTriggersMap.entrySet()) {
                    var subscriberFieldId = fieldTrigger.getKey();
                    var triggers = fieldTrigger.getValue();
                    getLoop().recomputeTriggersBySubscriberTwinId
                            .computeIfAbsent(subscriberTwin.getId(), _ -> new HashMap<>())
                            .computeIfAbsent(subscriberFieldId, _ -> new ArrayList<>())
                            .addAll(triggers);
                }
            }
        }

        public boolean isVisitedPublisher(String key) {
            return visitedPublishers != null && visitedPublishers.contains(key);
        }

        public boolean addVisitedSubscriber(String key) {
            if (visitedSubscribers == null)
                visitedSubscribers = new HashSet<>();
            return visitedSubscribers.add(key);
        }

        public boolean isEmpty() {
            return subscriberTwinPointerIds == null;
        }

        public void newLoop() {
            this.currentLoop++;
            if (this.loops == null)
                loops = new ArrayList<>();
            this.loops.add(new RecomputePlanLoop());
        }

        public boolean hasRecomputedFields(TwinChangesCollector collector) {
            if (visitedPublishers == null)
                return true;
            int collectedDecimalFieldsCount = collector.getSaveEntities(TwinFieldDecimalEntity.class).size() + collector.getDeletes(TwinFieldDecimalEntity.class).size();
            if (collectedDecimalFieldsCount == 0)
                return false;
            if (collectedDecimalFieldsCount != visitedPublishers.size()) //some new fields were added
                return true;
            return false;
        }

        private record UnresolvedPointer(UUID publisherTwinId, UUID twinPointerId) {
            @Override
            public String toString() {
                return "pointer[fromTwinId:" + publisherTwinId + ", twinPointerId:" + twinPointerId + "]";
            }
        }

        @Data
        private static class RecomputePlanLoop {
            private Map<UnresolvedPointer, Map<UUID, List<RecomputeTrigger>>> recomputeTriggersByUnresolvedPointer;
            private Map<UUID, Map<UUID, List<RecomputeTrigger>>> recomputeTriggersBySubscriberTwinId;

            public void init() {
                if (recomputeTriggersByUnresolvedPointer == null) {
                    recomputeTriggersByUnresolvedPointer = new HashMap<>();
                    recomputeTriggersBySubscriberTwinId = new HashMap<>();
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


    private List<FieldRecomputeRequest> buildRecomputeRequests(RecomputePlan recomputePlan) throws ServiceException {
        List<FieldRecomputeRequest> requests = new ArrayList<>();
        for (var entry : recomputePlan.getLoop().getRecomputeTriggersBySubscriberTwinId().entrySet()) {
            var subscriberTwinId = entry.getKey();
            var subscriberFieldTriggers = entry.getValue();

            for (var fieldTriggers : subscriberFieldTriggers.entrySet()) {
                var subscriberFieldId = fieldTriggers.getKey();
                var triggers = fieldTriggers.getValue();
                if (!recomputePlan.addVisitedSubscriber(toKey(subscriberTwinId, subscriberFieldId))) {
                    log.warn("subscriber[twinId:{}, twinClassFieldId:{}] is already visited", subscriberTwinId, subscriberFieldId);
                    continue;
                }
                requests.add(new FieldRecomputeRequest(
                        recomputePlan.getSubscriberTwinsKit().get(subscriberTwinId),
                        recomputePlan.getSubscriberTwinClassFieldKit().get(subscriberFieldId),
                        triggers));
            }

        }
        return requests;
    }

    private void dispatchRecompute(FieldRecomputeRequest request, TwinChangesCollector collector) throws ServiceException {
        if (request.triggers().stream().anyMatch(RecomputeTrigger::async)) {
            //TODO
            log.warn("Async Mater recompute not yet implemented, falling back to sync for {} on twin {}",
                    request.subscriberField().getId(), request.subscriberTwin().getId());
        }

        FieldTyper fieldTyper = featurerService.getFeaturer(request.subscriberField().getFieldTyperFeaturerId(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperRecomputed subscriber)) {
            log.warn("FieldTyper {} for field {} is not a FieldTyperRecomputed, skipping",
                    fieldTyper.getClass().getSimpleName(), request.subscriberField().getId());
            return;
        }
        subscriber.recompute(request, collector);
    }
}
