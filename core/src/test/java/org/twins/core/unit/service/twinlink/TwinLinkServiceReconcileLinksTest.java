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
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.domain.twinoperation.TwinCreateStage;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
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
 * Unit tests for the state-based link write ({@link TwinLinkService#reconcileLinks}): the desired link set
 * is reconciled with the stored links of (twin, link) through the standard CUD pipeline — the same path the
 * links[] API takes (relation twin lifecycle, history, MANDATORY delete guard).
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

    /** A stored twin_link as it comes from the DB: id + endpoints + link wired. */
    private TwinLinkEntity storedLink(UUID farTwinId) {
        return new TwinLinkEntity()
                .setId(UuidUtils.generate())
                .setLinkId(link.getId())
                .setLink(link)
                .setSrcTwinId(srcTwin.getId())
                .setSrcTwin(srcTwin)
                .setDstTwinId(farTwinId);
    }

    /** A desired link as the field path supplies it: raw convention — the far endpoint in dstTwinId. */
    private TwinLinkCreate desired(TwinEntity farTwin) {
        TwinLinkCreate linkCreate = new TwinLinkCreate();
        linkCreate.setTwinLink(new TwinLinkEntity()
                .setLinkId(link.getId())
                .setLink(link)
                .setDstTwinId(farTwin.getId())
                .setDstTwin(farTwin));
        return linkCreate;
    }

    private TwinEntity twinOfClass(TwinClassEntity twinClass) {
        return new TwinEntity().setId(UuidUtils.generate()).setTwinClassId(twinClass.getId()).setTwinClass(twinClass);
    }

    private void stubForward() throws ServiceException {
        when(linkService.detectLinkDirection(link, srcTwin.getTwinClass())).thenReturn(LinkService.LinkDirection.forward);
    }

    @Test
    void noStored_desiredCreated() throws Exception {
        // given: no stored links, one desired
        stubForward();
        when(twinLinkRepository.findBySrcTwinIdAndLinkId(srcTwin.getId(), link.getId(), TwinLinkEntity.class))
                .thenReturn(new ArrayList<>());
        TwinLinkCreate desired = desired(dstTwin);

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, List.of(desired), collector);

        // then: created through the standard addLinks pipeline
        assertTrue(collector.getSaveEntities(TwinLinkEntity.class).contains(desired.getTwinLink()));
        assertEquals(srcTwin.getId(), desired.getTwinLink().getSrcTwinId(), "prepareTwinLinks wires the src end");
        verify(historyService).linkCreated(desired.getTwinLink());
        verify(historyService, never()).linkUpdated(any(), any(), anyBoolean());
        verify(historyService, never()).linkDeleted(any());
    }

    @Test
    void unchangedDesired_noop() throws Exception {
        // given: the desired link's far endpoint already has a stored link
        stubForward();
        TwinLinkEntity stored = storedLink(dstTwin.getId());
        when(twinLinkRepository.findBySrcTwinIdAndLinkId(srcTwin.getId(), link.getId(), TwinLinkEntity.class))
                .thenReturn(new ArrayList<>(List.of(stored)));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, List.of(desired(dstTwin)), collector);

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
        // given: one stored link to an OLD far endpoint, one desired link to a NEW far endpoint
        stubForward();
        TwinEntity oldDstTwin = twinOfClass(dstClass);
        TwinLinkEntity stored = storedLink(oldDstTwin.getId())
                .setDstTwin(oldDstTwin);
        when(twinLinkRepository.findBySrcTwinIdAndLinkId(srcTwin.getId(), link.getId(), TwinLinkEntity.class))
                .thenReturn(new ArrayList<>(List.of(stored)));
        TwinLinkCreate desired = desired(dstTwin);
        // updateTwinLinks loads the db twin_link by the adopted id
        when(entitySmartService.findByIdIn(any(), eq(twinLinkRepository), any(), any()))
                .thenReturn(new Kit<>(List.of(stored), TwinLinkEntity::getId));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, List.of(desired), collector);

        // then: id adoption turns the write into an UPDATE of the stored link (no second link created)
        assertEquals(stored.getId(), desired.getTwinLink().getId(), "desired must adopt the stored twin_link id");
        assertEquals(dstTwin.getId(), stored.getDstTwinId(), "db link repointed to the new far endpoint");
        assertTrue(collector.getSaveEntities(TwinLinkEntity.class).contains(stored));
        verify(historyService).linkUpdated(eq(stored), eq(oldDstTwin), eq(true));
        verify(historyService, never()).linkCreated(any());
        verify(historyService, never()).linkDeleted(any());
    }

    @Test
    void moreDesiredThanStored_updateAndCreate() throws Exception {
        // given: one stored link, TWO desired links — one pairs (update), one is created
        stubForward();
        TwinEntity oldDstTwin = twinOfClass(dstClass);
        TwinLinkEntity stored = storedLink(oldDstTwin.getId()).setDstTwin(oldDstTwin);
        when(twinLinkRepository.findBySrcTwinIdAndLinkId(srcTwin.getId(), link.getId(), TwinLinkEntity.class))
                .thenReturn(new ArrayList<>(List.of(stored)));
        TwinLinkCreate paired = desired(dstTwin);
        TwinLinkCreate created = desired(twinOfClass(dstClass));
        when(entitySmartService.findByIdIn(any(), eq(twinLinkRepository), any(), any()))
                .thenReturn(new Kit<>(List.of(stored), TwinLinkEntity::getId));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, List.of(paired, created), collector);

        // then
        assertEquals(stored.getId(), paired.getTwinLink().getId(), "first desired pairs with the stored link");
        assertNotNull(created.getTwinLink().getId(), "second desired is a fresh create — the collector assigns its id at collect time");
        Set<TwinLinkEntity> saved = collector.getSaveEntities(TwinLinkEntity.class);
        assertEquals(2, saved.size(), "one UPDATE + one CREATE");
        verify(historyService).linkUpdated(any(), any(), anyBoolean());
        verify(historyService).linkCreated(created.getTwinLink());
    }

    @Test
    void leftoverStored_deleted() throws Exception {
        // given: two stored links, desired keeps only one far endpoint — the other must be deleted
        stubForward();
        TwinLinkEntity kept = storedLink(dstTwin.getId()).setDstTwin(dstTwin);
        TwinEntity removedFarTwin = twinOfClass(dstClass);
        TwinLinkEntity removed = storedLink(removedFarTwin.getId()).setDstTwin(removedFarTwin);
        when(twinLinkRepository.findBySrcTwinIdAndLinkId(srcTwin.getId(), link.getId(), TwinLinkEntity.class))
                .thenReturn(new ArrayList<>(List.of(kept, removed)));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, List.of(desired(dstTwin)), collector);

        // then
        assertTrue(collector.getDeletes(TwinLinkEntity.class).contains(removed));
        assertTrue(collector.getSaveEntities(TwinLinkEntity.class).isEmpty());
        verify(historyService).linkDeleted(removed);
        verify(historyService, never()).linkCreated(any());
    }

    @Test
    void mandatoryLeftoverStored_notDeleted() throws Exception {
        // given: an EMPTY desired set (pure delete-all) and the stored link is MANDATORY — the service guard must skip it
        stubForward();
        link.setLinkStrengthId(LinkStrength.MANDATORY);
        TwinLinkEntity removed = storedLink(twinOfClass(dstClass).getId());
        when(twinLinkRepository.findBySrcTwinIdAndLinkId(srcTwin.getId(), link.getId(), TwinLinkEntity.class))
                .thenReturn(new ArrayList<>(List.of(removed)));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, List.of(), collector);

        // then: skipped with no delete and no history (behavior change vs the old field path — accepted)
        assertTrue(collector.getDeletes(TwinLinkEntity.class).isEmpty());
        verify(historyService, never()).linkDeleted(any());
    }

    @Test
    void relationTwinClassLink_reconcileCreatesRelationTwin() throws Exception {
        // given: a link with relation_twin_class_id and one NEW desired link (field path: no attributes)
        stubForward();
        TwinClassEntity relationTwinClass = classEntity();
        link
                .setRelationTwinClassId(relationTwinClass.getId())
                .setRelationTwinClass(relationTwinClass);
        when(twinLinkRepository.findBySrcTwinIdAndLinkId(srcTwin.getId(), link.getId(), TwinLinkEntity.class))
                .thenReturn(new ArrayList<>());
        TwinLinkCreate desired = desired(dstTwin);

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, link, List.of(desired), collector);

        // then: the relation twin is created through the SAME pipeline as the links[] API —
        // empty AUTO twin (no relationTwinFields from a field write), ID equality, relation_twin_id wired
        ArgumentCaptor<TwinCreateStage> captor = ArgumentCaptor.forClass(TwinCreateStage.class);
        verify(twinService).createTwins(captor.capture(), eq(collector));
        var twinCreate = captor.getValue().getTwinCreates().iterator().next();
        assertEquals(desired.getTwinLink().getId(), twinCreate.getTwinEntity().getId(), "ID equality");
        assertEquals(relationTwinClass.getId(), twinCreate.getTwinEntity().getTwinClassId());
        assertTrue(twinCreate.getFields() == null || twinCreate.getFields().isEmpty(), "field write carries no relation attributes");
        assertEquals(desired.getTwinLink().getId(), desired.getTwinLink().getRelationTwinId());
    }

    @Test
    void backwardDirection_storedKeyedBySrcAndSwapApplied() throws Exception {
        // given: a BACKWARD link for srcTwin — srcTwin sits at the link's dst end; stored rows are keyed by
        // their srcTwinId (the far endpoint), desired items carry the far endpoint in dstTwinId (raw convention)
        LinkEntity backwardLink = new LinkEntity()
                .setId(UuidUtils.generate())
                .setSrcTwinClassId(dstClass.getId())
                .setDstTwinClassId(srcClass.getId())
                .setType(LinkType.ManyToMany)
                .setLinkStrengthId(LinkStrength.OPTIONAL);
        when(linkService.detectLinkDirection(backwardLink, srcTwin.getTwinClass())).thenReturn(LinkService.LinkDirection.backward);
        TwinEntity farTwin = twinOfClass(dstClass);
        TwinEntity otherFarTwin = twinOfClass(dstClass);
        TwinLinkEntity stored = new TwinLinkEntity()
                .setId(UuidUtils.generate())
                .setLinkId(backwardLink.getId())
                .setLink(backwardLink)
                .setSrcTwinId(farTwin.getId())
                .setSrcTwin(farTwin)
                .setDstTwinId(srcTwin.getId())
                .setDstTwin(srcTwin);
        when(twinLinkRepository.findByDstTwinIdAndLinkId(srcTwin.getId(), backwardLink.getId(), TwinLinkEntity.class))
                .thenReturn(new ArrayList<>(List.of(stored)));
        // desired: keep farTwin (no-op), add otherFarTwin (fresh create through the direction swap)
        TwinLinkCreate keep = new TwinLinkCreate();
        keep.setTwinLink(new TwinLinkEntity()
                .setLinkId(backwardLink.getId())
                .setLink(backwardLink)
                .setDstTwinId(farTwin.getId())
                .setDstTwin(farTwin));
        TwinLinkCreate create = new TwinLinkCreate();
        create.setTwinLink(new TwinLinkEntity()
                .setLinkId(backwardLink.getId())
                .setLink(backwardLink)
                .setDstTwinId(otherFarTwin.getId())
                .setDstTwin(otherFarTwin));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.reconcileLinks(srcTwin, backwardLink, List.of(keep, create), collector);

        // then: the unchanged link is untouched, the new one is created with the direction applied
        assertTrue(collector.getSaveEntities(TwinLinkEntity.class).contains(create.getTwinLink()));
        assertEquals(otherFarTwin.getId(), create.getTwinLink().getSrcTwinId(), "backward swap: far endpoint becomes src");
        assertEquals(srcTwin.getId(), create.getTwinLink().getDstTwinId(), "backward swap: the field's twin becomes dst");
        verify(historyService, never()).linkDeleted(any());
        verify(historyService, never()).linkUpdated(any(), any(), anyBoolean());
    }
}
