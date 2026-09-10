package org.twins.core.unit.service.twinlink;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.twinlink.TwinLinkReconcile;
import org.twins.core.domain.twinoperation.TwinCreateStage;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryCollectorMultiTwin;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinHeadService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinlink.TwinLinkService;
import org.twins.core.service.user.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the state-based link write ({@link TwinLinkService#reconcileLinks}): the desired far-twin
 * set of a (twin, link, direction) triple is reconciled with the stored links through the standard CUD
 * pipeline — the same path the links[] API takes (relation twin lifecycle, history, MANDATORY delete guard).
 * The direction is EXPLICIT (the field typer knows it); backward reconcile is OneToOne-only (a twin's
 * backward side must stay bounded).
 */
@ExtendWith(MockitoExtension.class)
class TwinLinkServiceReconcileLinksTest {

    @Mock
    private LinkService linkService;
    @Mock
    private TwinClassService twinClassService;
    @Mock
    private TwinLinkRepository twinLinkRepository;
    @Mock
    private TwinService twinService;
    @Mock
    private TwinSearchService twinSearchService;
    @Mock
    private TwinHeadService twinHeadService;
    @Mock
    private AuthService authService;
    @Mock
    private EntitySmartService entitySmartService;
    @Mock
    private HistoryService historyService;
    @Mock
    private TwinChangesService twinChangesService;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private UserService userService;

    @InjectMocks
    private TwinLinkService twinLinkService;

    private TwinClassEntity srcClass;
    private TwinClassEntity dstClass;
    private TwinEntity srcTwin;
    private TwinEntity dstTwin;
    private LinkEntity link;

    @BeforeEach
    void setUp() throws Exception {
        srcClass = classEntity();
        dstClass = classEntity();
        srcTwin = new TwinEntity()
                .setId(UuidUtils.generate())
                .setTwinClassId(srcClass.getId())
                .setTwinClass(srcClass);
        dstTwin = new TwinEntity()
                .setId(UuidUtils.generate())
                .setTwinClassId(dstClass.getId())
                .setTwinClass(dstClass);
        link = new LinkEntity()
                .setId(UuidUtils.generate())
                .setSrcTwinClassId(srcClass.getId())
                .setDstTwinClassId(dstClass.getId())
                .setType(LinkType.ManyToMany)
                .setLinkStrengthId(LinkStrength.OPTIONAL);

        ApiUser apiUser = mock(ApiUser.class);
        lenient().when(apiUser.getUser()).thenReturn(new UserEntity().setId(UuidUtils.generate()));
        lenient().when(authService.getApiUser()).thenReturn(apiUser);
        // history collectors must be real (empty) — add(null) would NPE
        lenient().when(historyService.linkCreated(any(TwinLinkEntity.class))).thenReturn(new HistoryCollectorMultiTwin());
        lenient().when(historyService.linkUpdated(any(TwinLinkEntity.class), any(TwinEntity.class), anyBoolean()))
                .thenReturn(new HistoryCollectorMultiTwin());
        lenient().when(historyService.linkDeleted(any(TwinLinkEntity.class))).thenReturn(new HistoryCollectorMultiTwin());
        // @InjectMocks uses constructor injection and does NOT fill the base class' private entitySmartService
        injectSuperclassField(twinLinkService, "entitySmartService", entitySmartService);
    }

    private static void injectSuperclassField(Object target, String name, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        boolean injected = false;
        while (clazz != null) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                injected = true;
            } catch (NoSuchFieldException ignored) {
                // walk up
            }
            clazz = clazz.getSuperclass();
        }
        if (!injected)
            throw new RuntimeException("field not found: " + name);
    }

    private TwinClassEntity classEntity() {
        UUID id = UuidUtils.generate();
        TwinClassEntity twinClass = new TwinClassEntity().setId(id);
        twinClass.setExtendedClassIdSet(new HashSet<>(Set.of(id)));
        return twinClass;
    }

    /** A stored twin_link as it comes from the DB: id + endpoints + link wired (forward direction). */
    private TwinLinkEntity storedLink(UUID farTwinId) {
        return new TwinLinkEntity()
                .setId(UuidUtils.generate())
                .setLinkId(link.getId())
                .setLink(link)
                .setSrcTwinId(srcTwin.getId())
                .setSrcTwin(srcTwin)
                .setDstTwinId(farTwinId);
    }

    private TwinEntity twinOfClass(TwinClassEntity twinClass) {
        return new TwinEntity().setId(UuidUtils.generate()).setTwinClassId(twinClass.getId()).setTwinClass(twinClass);
    }

    private void stubNoForwardStored() {
        when(twinLinkRepository.findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection()))
                .thenReturn(new ArrayList<>());
    }

    @Test
    void noStored_desiredTwinCreated() throws Exception {
        // given: no stored links, one desired far twin
        stubNoForwardStored();

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, LinkService.LinkDirection.forward, List.of(dstTwin), collector);

        // then: created through the standard addLinks pipeline
        assertEquals(1, collector.getSaveEntities(TwinLinkEntity.class).size(), "one twin_link created");
        TwinLinkEntity created = collector.getSaveEntities(TwinLinkEntity.class).iterator().next();
        assertEquals(srcTwin.getId(), created.getSrcTwinId(), "forward: the field's twin is src");
        assertEquals(dstTwin.getId(), created.getDstTwinId(), "the desired far twin is dst");
        verify(historyService).linkCreated(created);
        verify(historyService, never()).linkUpdated(any(), any(), anyBoolean());
        verify(historyService, never()).linkDeleted(any());
    }

    @Test
    void unchangedDesiredTwin_noop() throws Exception {
        // given: the desired far twin already has a stored link
        TwinLinkEntity stored = storedLink(dstTwin.getId());
        when(twinLinkRepository.findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection()))
                .thenReturn(new ArrayList<>(List.of(stored)));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, LinkService.LinkDirection.forward, List.of(dstTwin), collector);

        // then: no CUD at all — no events, no writes, no deletes
        assertTrue(collector.getSaveEntities(TwinLinkEntity.class).isEmpty());
        assertTrue(collector.getDeletes(TwinLinkEntity.class).isEmpty());
        verify(historyService, never()).linkCreated(any());
        verify(historyService, never()).linkUpdated(any(), any(), anyBoolean());
        verify(historyService, never()).linkDeleted(any());
        verify(twinService, never()).createTwins(any(TwinCreateStage.class), any(TwinChangesCollector.class));
    }

    @Test
    void outOfDateStored_pairedAsUpdate() throws Exception {
        // given: one stored link to an OLD far twin, one desired NEW far twin
        TwinEntity oldDstTwin = twinOfClass(dstClass);
        TwinLinkEntity stored = storedLink(oldDstTwin.getId())
                .setDstTwin(oldDstTwin);
        when(twinLinkRepository.findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection()))
                .thenReturn(new ArrayList<>(List.of(stored)));
        // updateTwinLinks loads the db twin_link by the adopted id
        when(entitySmartService.findByIdIn(any(), eq(twinLinkRepository), any(), any()))
                .thenReturn(new Kit<>(List.of(stored), TwinLinkEntity::getId));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, LinkService.LinkDirection.forward, List.of(dstTwin), collector);

        // then: id adoption turns the write into an UPDATE of the stored link (no second link created)
        assertEquals(dstTwin.getId(), stored.getDstTwinId(), "db link repointed to the desired far twin");
        assertTrue(collector.getSaveEntities(TwinLinkEntity.class).contains(stored));
        verify(historyService).linkUpdated(eq(stored), eq(oldDstTwin), eq(true));
        verify(historyService, never()).linkCreated(any());
        verify(historyService, never()).linkDeleted(any());
    }

    @Test
    void moreDesiredThanStored_updateAndCreate() throws Exception {
        // given: one stored link, TWO desired far twins — one pairs (update), one is created
        TwinEntity oldDstTwin = twinOfClass(dstClass);
        TwinLinkEntity stored = storedLink(oldDstTwin.getId()).setDstTwin(oldDstTwin);
        TwinEntity secondTwin = twinOfClass(dstClass);
        when(twinLinkRepository.findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection()))
                .thenReturn(new ArrayList<>(List.of(stored)));
        when(entitySmartService.findByIdIn(any(), eq(twinLinkRepository), any(), any()))
                .thenReturn(new Kit<>(List.of(stored), TwinLinkEntity::getId));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, LinkService.LinkDirection.forward, List.of(dstTwin, secondTwin), collector);

        // then
        assertEquals(dstTwin.getId(), stored.getDstTwinId(), "first desired pairs with the stored link");
        assertEquals(2, collector.getSaveEntities(TwinLinkEntity.class).size(), "one UPDATE + one CREATE");
        verify(historyService).linkUpdated(any(), any(), anyBoolean());
        verify(historyService, times(1)).linkCreated(any());
    }

    @Test
    void leftoverStored_deleted() throws Exception {
        // given: two stored links, desired keeps only one far twin — the other must be deleted
        TwinLinkEntity kept = storedLink(dstTwin.getId()).setDstTwin(dstTwin);
        TwinEntity removedFarTwin = twinOfClass(dstClass);
        TwinLinkEntity removed = storedLink(removedFarTwin.getId()).setDstTwin(removedFarTwin);
        when(twinLinkRepository.findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection()))
                .thenReturn(new ArrayList<>(List.of(kept, removed)));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, LinkService.LinkDirection.forward, List.of(dstTwin), collector);

        // then
        assertTrue(collector.getDeletes(TwinLinkEntity.class).contains(removed));
        assertTrue(collector.getSaveEntities(TwinLinkEntity.class).isEmpty());
        verify(historyService).linkDeleted(removed);
        verify(historyService, never()).linkCreated(any());
    }

    @Test
    void mandatoryLeftoverStored_notDeleted() throws Exception {
        // given: an EMPTY desired set (pure delete-all) and the stored link is MANDATORY — the service guard must skip it
        link.setLinkStrengthId(LinkStrength.MANDATORY);
        TwinLinkEntity removed = storedLink(twinOfClass(dstClass).getId());
        when(twinLinkRepository.findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection()))
                .thenReturn(new ArrayList<>(List.of(removed)));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, LinkService.LinkDirection.forward, List.of(), collector);

        // then: skipped with no delete and no history (behavior change vs the old field path — accepted)
        assertTrue(collector.getDeletes(TwinLinkEntity.class).isEmpty());
        verify(historyService, never()).linkDeleted(any());
    }

    @Test
    void relationTwinClassLink_reconcileCreatesRelationTwin() throws Exception {
        // given: a link with relation_twin_class_id and one NEW desired far twin (field path: no attributes)
        TwinClassEntity relationTwinClass = classEntity();
        link
                .setRelationTwinClassId(relationTwinClass.getId())
                .setRelationTwinClass(relationTwinClass);
        stubNoForwardStored();

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, LinkService.LinkDirection.forward, List.of(dstTwin), collector);

        // then: the relation twin is created through the SAME pipeline as the links[] API —
        // empty AUTO twin (no relationTwinFields from a field write), ID equality, relation_twin_id wired
        ArgumentCaptor<TwinCreateStage> captor = ArgumentCaptor.forClass(TwinCreateStage.class);
        verify(twinService).createTwins(captor.capture(), eq(collector));
        var twinCreate = captor.getValue().getTwinCreates().iterator().next();
        TwinLinkEntity created = collector.getSaveEntities(TwinLinkEntity.class).iterator().next();
        assertEquals(created.getId(), twinCreate.getTwinEntity().getId(), "ID equality");
        assertEquals(relationTwinClass.getId(), twinCreate.getTwinEntity().getTwinClassId());
        assertTrue(twinCreate.getFields() == null || twinCreate.getFields().isEmpty(), "field write carries no relation attributes");
        assertEquals(created.getId(), created.getRelationTwinId());
    }

    @Test
    void backwardOneToOne_dstSideStoredAndSwapApplied() throws Exception {
        // given: a BACKWARD pair on a OneToOne link — srcTwin sits at the link's dst end (dst class =
        // srcTwin's class); stored rows live on the dst side and are keyed by their srcTwinId (the far twin)
        LinkEntity backwardLink = new LinkEntity()
                .setId(UuidUtils.generate())
                .setSrcTwinClassId(dstClass.getId()) // the far side
                .setDstTwinClassId(srcClass.getId()) // the field's twin side
                .setType(LinkType.OneToOne)
                .setLinkStrengthId(LinkStrength.OPTIONAL);
        TwinEntity farTwin = twinOfClass(dstClass);
        when(twinLinkRepository.findByDstTwinIdInAndLinkIdIn(anyCollection(), anyCollection()))
                .thenReturn(new ArrayList<>());

        // when: link this far twin (fresh create through the direction-aware backward branch)
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, backwardLink, LinkService.LinkDirection.backward, List.of(farTwin), collector);

        // then
        TwinLinkEntity created = collector.getSaveEntities(TwinLinkEntity.class).iterator().next();
        assertEquals(farTwin.getId(), created.getSrcTwinId(), "backward: the far twin becomes src");
        assertEquals(srcTwin.getId(), created.getDstTwinId(), "backward: the field's twin becomes dst");
        verify(twinLinkRepository, never()).findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection());
    }

    @Test
    void backwardNonOneToOne_throws() throws Exception {
        // given: a backward pair on a many-typed link — a twin's backward side would be unbounded
        // when + then: fail fast, no dst-side query, no CUD
        TwinChangesCollector collector = new TwinChangesCollector();
        ServiceException ex = assertThrows(ServiceException.class,
                () -> twinLinkService.reconcileLinks(srcTwin, link, LinkService.LinkDirection.backward, List.of(dstTwin), collector));
        assertEquals(ErrorCodeTwins.TWIN_LINK_INCORRECT.getCode(), ex.getErrorCode());
        verify(twinLinkRepository, never()).findByDstTwinIdInAndLinkIdIn(anyCollection(), anyCollection());
        assertTrue(collector.getSaveEntities(TwinLinkEntity.class).isEmpty());
    }

    @Test
    void undetectedDirection_throws() {
        // given: a pair without an explicit direction — undetected must fail fast
        TwinChangesCollector collector = new TwinChangesCollector();
        ServiceException ex = assertThrows(ServiceException.class,
                () -> twinLinkService.reconcileLinks(srcTwin, link, null, List.of(dstTwin), collector));
        assertEquals(ErrorCodeTwins.TWIN_LINK_INCORRECT.getCode(), ex.getErrorCode());
        verifyNoInteractions(twinLinkRepository);
    }

    @Test
    void batch_twoPairsSameTwinAndDirection_oneStoredQueryMergedCud() throws Exception {
        // given: ONE twin, TWO forward link pairs in one batch — pair1 creates a link, pair2 deletes its
        // leftover; both pairs' stored links come from ONE repository query and one merged CUD run
        LinkEntity link2 = new LinkEntity()
                .setId(UuidUtils.generate())
                .setSrcTwinClassId(srcClass.getId())
                .setDstTwinClassId(dstClass.getId())
                .setType(LinkType.ManyToMany)
                .setLinkStrengthId(LinkStrength.OPTIONAL);
        TwinEntity far2 = twinOfClass(dstClass);
        TwinLinkEntity stored2 = new TwinLinkEntity()
                .setId(UuidUtils.generate())
                .setLinkId(link2.getId())
                .setLink(link2)
                .setSrcTwinId(srcTwin.getId())
                .setSrcTwin(srcTwin)
                .setDstTwinId(far2.getId())
                .setDstTwin(far2);
        when(twinLinkRepository.findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection()))
                .thenReturn(new ArrayList<>(List.of(stored2))); // only pair2 has stored links
        TwinLinkReconcile pair1 = new TwinLinkReconcile(srcTwin, link, LinkService.LinkDirection.forward, List.of(dstTwin)); // create
        TwinLinkReconcile pair2 = new TwinLinkReconcile(srcTwin, link2, LinkService.LinkDirection.forward, List.of()); // delete-all

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(List.of(pair1, pair2), collector);

        // then: ONE stored query for the whole batch; pair1's desired created, pair2's stored deleted
        verify(twinLinkRepository, times(1)).findBySrcTwinIdInAndLinkIdIn(anyCollection(), anyCollection());
        assertEquals(1, collector.getSaveEntities(TwinLinkEntity.class).size(), "pair1's twin_link created");
        assertTrue(collector.getDeletes(TwinLinkEntity.class).contains(stored2));
        verify(historyService, times(1)).linkCreated(any());
        verify(historyService).linkDeleted(stored2);
    }
}
