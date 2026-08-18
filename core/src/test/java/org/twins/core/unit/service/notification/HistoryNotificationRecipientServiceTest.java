package org.twins.core.unit.service.notification;

import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.notificator.recipient.RecipientResolveBatch;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.notification.HistoryNotificationChunk;
import org.twins.core.service.notification.HistoryNotificationRecipientCollectorService;
import org.twins.core.service.notification.HistoryNotificationRecipientService;
import org.twins.core.service.user.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Chunk-level recipient precompute over OVERLAPPING data: several configs / recipients / collectors
 * sharing one chunk, collectors reusing the same resolver featurer with same or different params.
 * Verifies the grouping (one resolveBatch per unique (featurerId, params)) and the per-recipient
 * include/exclude reassembly in Stage 3.
 */
class HistoryNotificationRecipientServiceTest extends BaseUnitTest {

    private static final int RESOLVER_FEATURER_ID = FeaturerTwins.ID_4702;

    @Mock
    private HistoryNotificationRecipientRepository repository;
    @Mock
    private I18nService i18nService;
    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private HistoryNotificationRecipientCollectorService recipientCollectorService;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private RecipientResolver resolver;

    private HistoryNotificationRecipientService service;
    private UUID domainId;

    @BeforeEach
    void setUp() throws Exception {
        service = new HistoryNotificationRecipientService(
                repository, i18nService, authService, userService, recipientCollectorService, featurerService);
        domainId = UUID.randomUUID();
        when(featurerService.getFeaturer(eq(RESOLVER_FEATURER_ID), eq(RecipientResolver.class))).thenReturn(resolver);
    }

    // ---------- test data builders ----------

    private TwinEntity twin() {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setOwnerBusinessAccountId(UUID.randomUUID());
        return twin;
    }

    private HistoryEntity history(TwinEntity twin) {
        var history = new HistoryEntity();
        history.setTwinId(twin.getId());
        history.setTwin(twin);
        return history;
    }

    private HistoryNotificationTaskEntity task(HistoryEntity history) {
        // id matters: entities are used as map keys and @Data equals collapses id-less instances
        return new HistoryNotificationTaskEntity()
                .setId(UUID.randomUUID())
                .setHistory(history);
    }

    /** Collector resolving users of a "group" (params = userGroupIds) via the mocked resolver. */
    private HistoryNotificationRecipientCollectorEntity resolverCollector(String group, boolean exclude) {
        var collector = new HistoryNotificationRecipientCollectorEntity();
        collector.setId(UUID.randomUUID());
        collector.setRecipientResolverFeaturerId(RESOLVER_FEATURER_ID);
        collector.setRecipientResolverParams(new HashMap<>(Map.of("userGroupIds", group)));
        collector.setExclude(exclude);
        return collector;
    }

    private HistoryNotificationRecipientEntity recipient(HistoryNotificationRecipientCollectorEntity... collectors) {
        var recipient = new HistoryNotificationRecipientEntity();
        recipient.setId(UUID.randomUUID());
        recipient.setCollectors(new Kit<>(HistoryNotificationRecipientCollectorEntity::getId));
        for (var collector : collectors) {
            recipient.getCollectors().add(collector);
        }
        return recipient;
    }

    private HistoryNotificationEntity config(HistoryNotificationRecipientEntity recipient) {
        var config = new HistoryNotificationEntity();
        config.setHistoryNotificationRecipientId(recipient.getId());
        config.setHistoryNotificationRecipient(recipient);
        return config;
    }

    private HistoryNotificationChunk chunkOf(HistoryNotificationTaskEntity task) {
        return new HistoryNotificationChunk(domainId, List.of(task));
    }

    private void wire(HistoryNotificationChunk chunk, HistoryNotificationEntity config, HistoryNotificationTaskEntity... tasks) {
        chunk.getTasksByConfig().put(config, new LinkedHashSet<>(List.of(tasks)));
        for (HistoryNotificationTaskEntity task : tasks) {
            chunk.getConfigsByTask().computeIfAbsent(task, _ -> new ArrayList<>()).add(config);
        }
    }

    /** Mocked resolver adds the given userIds per userGroupIds param value. */
    private void resolverAddsByGroup(Map<String, Set<UUID>> idsByGroup) throws Exception {
        doAnswer(invocation -> {
            RecipientResolveBatch context = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            HashMap<String, String> params = invocation.getArgument(1);
            Set<UUID> ids = idsByGroup.getOrDefault(params.get("userGroupIds"), Set.of());
            for (var entry : context.getRecipientIdsByHistory().entrySet()) {
                entry.getValue().addAll(ids);
            }
            return null;
        }).when(resolver).resolveBatch(any(RecipientResolveBatch.class), any(HashMap.class));
    }

    // ---------- scenarios ----------

    @Nested
    class OverlappingConfigs {

        @Test
        void twoConfigsSameResolverDifferentParams_separateRecipientSetsPerConfig() throws Exception {
            // one task matched by a manager config and a worker config — same resolver featurer,
            // DIFFERENT params → two groups, two resolveBatch calls, per-recipient results
            var managerIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
            var workerIds = Set.of(UUID.randomUUID());
            var managers = recipient(resolverCollector("managers", false));
            var workers = recipient(resolverCollector("workers", false));
            var configManagers = config(managers);
            var configWorkers = config(workers);
            var task = task(history(twin()));
            var chunk = chunkOf(task);
            wire(chunk, configManagers, task);
            wire(chunk, configWorkers, task);
            resolverAddsByGroup(Map.of("managers", managerIds, "workers", workerIds));

            service.resolveRecipientsBatch(chunk);

            var byRecipient = task.getResolvedRecipientsByRecipientId();
            assertEquals(2, byRecipient.size());
            assertEquals(managerIds, byRecipient.get(managers.getId()));
            assertEquals(workerIds, byRecipient.get(workers.getId()));
            verify(resolver, times(2)).resolveBatch(any(RecipientResolveBatch.class), any(HashMap.class));
        }

        @Test
        void twoRecipientsIdenticalCollectorParams_resolvedOnceBothGetSameSet() throws Exception {
            // two recipients (different configs) with IDENTICAL (featurerId, params) collectors →
            // ONE shared resolveBatch call, both recipients receive the same resolved set
            var employeeIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
            var recipientA = recipient(resolverCollector("employees", false));
            var recipientB = recipient(resolverCollector("employees", false));
            var configA = config(recipientA);
            var configB = config(recipientB);
            var task = task(history(twin()));
            var chunk = chunkOf(task);
            wire(chunk, configA, task);
            wire(chunk, configB, task);
            resolverAddsByGroup(Map.of("employees", employeeIds));

            service.resolveRecipientsBatch(chunk);

            var byRecipient = task.getResolvedRecipientsByRecipientId();
            assertEquals(2, byRecipient.size());
            assertEquals(employeeIds, byRecipient.get(recipientA.getId()));
            assertEquals(employeeIds, byRecipient.get(recipientB.getId()));
            verify(resolver, times(1)).resolveBatch(any(RecipientResolveBatch.class), any(HashMap.class));
        }

        @Test
        void configWithoutRecipient_skippedGracefully() throws Exception {
            var normalRecipient = recipient(resolverCollector("employees", false));
            var configWithRecipient = config(normalRecipient);
            var configWithout = new HistoryNotificationEntity(); // no recipient at all
            var task = task(history(twin()));
            var chunk = chunkOf(task);
            wire(chunk, configWithRecipient, task);
            wire(chunk, configWithout, task);
            resolverAddsByGroup(Map.of("employees", Set.of(UUID.randomUUID())));

            service.resolveRecipientsBatch(chunk);

            var byRecipient = task.getResolvedRecipientsByRecipientId();
            assertEquals(2, byRecipient.size()); // recipient set + null-recipient entry (empty)
            assertEquals(1, byRecipient.get(normalRecipient.getId()).size());
            assertTrue(byRecipient.get(null).isEmpty());
        }
    }

    @Nested
    class Exclude {

        @Test
        void includeMinusExclude_onlyRemainderLeft() throws Exception {
            // recipient: include=employees(3 users), exclude=managers(1 of them) → employees − managers
            var employee1 = UUID.randomUUID();
            var employee2 = UUID.randomUUID();
            var manager = UUID.randomUUID();
            var recipient = recipient(
                    resolverCollector("employees", false),
                    resolverCollector("managers", true));
            var configExclude = config(recipient);
            var task = task(history(twin()));
            var chunk = chunkOf(task);
            wire(chunk, configExclude, task);
            resolverAddsByGroup(Map.of(
                    "employees", Set.of(employee1, employee2, manager),
                    "managers", Set.of(manager)));

            service.resolveRecipientsBatch(chunk);

            assertEquals(Set.of(employee1, employee2), task.getResolvedRecipientsByRecipientId().get(recipient.getId()));
            // include-group and exclude-group are resolved separately (different params)
            verify(resolver, times(2)).resolveBatch(any(RecipientResolveBatch.class), any(HashMap.class));
        }

        @Test
        void excludeGroupReusedAsIncludeOfAnotherRecipient_noExtraResolve() throws Exception {
            // the managers group is an exclude for one recipient and an include for another →
            // two distinct groups total (employees + managers), each resolved once
            var employee = UUID.randomUUID();
            var manager = UUID.randomUUID();
            var employees = Set.of(employee, manager);
            var managers = Set.of(manager);
            var workersRecipient = recipient(
                    resolverCollector("employees", false),
                    resolverCollector("managers", true)); // everyone except managers
            var managersRecipient = recipient(resolverCollector("managers", false));
            var configWorkers = config(workersRecipient);
            var configManagers = config(managersRecipient);
            var task = task(history(twin()));
            var chunk = chunkOf(task);
            wire(chunk, configWorkers, task);
            wire(chunk, configManagers, task);
            resolverAddsByGroup(Map.of("employees", employees, "managers", managers));

            service.resolveRecipientsBatch(chunk);

            var byRecipient = task.getResolvedRecipientsByRecipientId();
            assertEquals(Set.of(employee), byRecipient.get(workersRecipient.getId())); // employees − managers
            assertEquals(managers, byRecipient.get(managersRecipient.getId()));
            verify(resolver, times(2)).resolveBatch(any(RecipientResolveBatch.class), any(HashMap.class));
        }
    }

    @Nested
    class MultipleTasks {

        @Test
        void severalTasksOneConfigGroup_perHistoryResultsSingleResolve() throws Exception {
            // one config matched by two tasks → one group, one resolveBatch, results land per task
            var employeeIds = Set.of(UUID.randomUUID());
            var recipient = recipient(resolverCollector("employees", false));
            var configEmployees = config(recipient);
            var task1 = task(history(twin()));
            var task2 = task(history(twin()));
            var chunk = new HistoryNotificationChunk(domainId, List.of(task1, task2));
            wire(chunk, configEmployees, task1, task2);
            resolverAddsByGroup(Map.of("employees", employeeIds));

            service.resolveRecipientsBatch(chunk);

            assertEquals(employeeIds, task1.getResolvedRecipientsByRecipientId().get(recipient.getId()));
            assertEquals(employeeIds, task2.getResolvedRecipientsByRecipientId().get(recipient.getId()));
            verify(resolver, times(1)).resolveBatch(any(RecipientResolveBatch.class), any(HashMap.class));
        }
    }
}
