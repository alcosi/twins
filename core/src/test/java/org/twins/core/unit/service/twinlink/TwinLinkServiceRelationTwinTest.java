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
import org.twins.core.domain.twinlink.TwinLinkUpdate;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinCreateStage;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.link.LinkType;
import org.twins.core.enums.twin.TwinCreateStrategy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the shadow "relation twin" creation hook (mirrors TwinTriggerServiceJobTwinTest):
 * a twin_link whose link has relation_twin_class_id set gets an auto-created twin of that class
 * with id == twin_link.id (ID equality), batched into ONE createTwins call via the shared collector.
 */
@ExtendWith(MockitoExtension.class)
class TwinLinkServiceRelationTwinTest {

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
    private TwinClassEntity relationTwinClass;
    private TwinEntity srcTwin;
    private TwinEntity dstTwin;

    @BeforeEach
    void setUp() throws Exception {
        srcClass = classEntity();
        dstClass = classEntity();
        relationTwinClass = classEntity();

        srcTwin = new TwinEntity()
                .setId(UuidUtils.generate())
                .setTwinClassId(srcClass.getId())
                .setTwinClass(srcClass);
        dstTwin = new TwinEntity()
                .setId(UuidUtils.generate())
                .setTwinClassId(dstClass.getId())
                .setTwinClass(dstClass);

        ApiUser apiUser = mock(ApiUser.class);
        lenient().when(apiUser.getUser()).thenReturn(new UserEntity().setId(UuidUtils.generate()));
        lenient().when(authService.getApiUser()).thenReturn(apiUser);
        // linkCreated must return a real (empty) collector — add(null) would NPE
        lenient().when(historyService.linkCreated(any(TwinLinkEntity.class))).thenReturn(new HistoryCollectorMultiTwin());
        // @InjectMocks uses constructor injection (RequiredArgsConstructor) and does NOT fill the base class'
        // private entitySmartService — inject it manually so the inherited findEntitiesSafe path works
        injectSuperclassField(twinLinkService, "entitySmartService", entitySmartService);
    }

    private static void injectSuperclassField(Object target, String name, Object value) throws Exception {
        // sets EVERY field with this name along the hierarchy: the subclass field is filled by @InjectMocks
        // already, but the base class' private twin (used by inherited findEntitiesSafe) is not
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

    private LinkEntity linkEntity(UUID relationTwinClassId) {
        LinkEntity link = new LinkEntity()
                .setId(UuidUtils.generate())
                .setSrcTwinClassId(srcClass.getId())
                .setDstTwinClassId(dstClass.getId())
                .setType(LinkType.ManyToMany);
        if (relationTwinClassId != null) {
            link
                    .setRelationTwinClassId(relationTwinClassId)
                    .setRelationTwinClass(relationTwinClass);
        }
        return link;
    }

    private List<TwinLinkCreate> creates(TwinLinkEntity... twinLinks) {
        List<TwinLinkCreate> result = new ArrayList<>();
        for (TwinLinkEntity tl : twinLinks) {
            TwinLinkCreate linkCreate = new TwinLinkCreate();
            linkCreate.setTwinLink(tl);
            result.add(linkCreate);
        }
        return result;
    }

    private TwinLinkEntity twinLinkEntity(LinkEntity link) {
        return new TwinLinkEntity()
                .setLinkId(link.getId())
                .setLink(link)
                .setDstTwinId(dstTwin.getId())
                .setDstTwin(dstTwin);
    }

    @Test
    void shouldCreateRelationTwinWithTwinLinkId() throws Exception {
        // given
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        TwinChangesCollector collector = new TwinChangesCollector();

        // when
        twinLinkService.addLinks(srcTwin, creates(twinLink), collector);

        // then
        ArgumentCaptor<TwinCreateStage> captor = ArgumentCaptor.forClass(TwinCreateStage.class);
        verify(twinService).createTwins(captor.capture(), same(collector));
        TwinCreateStage stage = captor.getValue();
        assertEquals(1, stage.size(), "one relation twin expected in the batch");

        TwinCreate twinCreate = stage.getTwinCreates().iterator().next();
        assertEquals(twinLink.getId(), twinCreate.getTwinEntity().getId(), "relation twin id must equal twin_link id (ID equality)");
        assertEquals(relationTwinClass.getId(), twinCreate.getTwinEntity().getTwinClassId(), "relation twin class must match link.relation_twin_class_id");
        assertEquals(TwinOperation.Launcher.link, twinCreate.getLauncher(), "launcher should be link");
        assertFalse(twinCreate.isCanTriggerAfterOperationFactory(), "recursion guard must be on (same as job_twin)");
        assertEquals(TwinCreateStrategy.AUTO, twinCreate.getCreateStrategy(), "AUTO strategy: sketch iff required attributes missing");
        assertNull(twinCreate.getFields(), "no initial fields passed -> null fields");

        assertEquals(twinLink.getId(), twinLink.getRelationTwinId(), "relation_twin_id must be set (redundant == id, hosts the FK)");
        assertSame(twinCreate.getTwinEntity(), twinLink.getRelationTwin(), "transient relation twin must be wired back");
    }

    @Test
    void shouldNotCreateRelationTwinWhenClassIdIsNull() throws Exception {
        // given
        LinkEntity link = linkEntity(null);
        TwinLinkEntity twinLink = twinLinkEntity(link);

        // when
        twinLinkService.addLinks(srcTwin, creates(twinLink), new TwinChangesCollector());

        // then
        verify(twinService, never()).createTwins(any(TwinCreateStage.class), any(TwinChangesCollector.class));
        assertNull(twinLink.getRelationTwinId());
    }

    @Test
    void shouldReuseSurvivingRelationTwinOnRelinkWithoutFields() throws Exception {
        // given: relink — processAlreadyExisted reuses an existing twin_link id whose relation twin exists
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        twinLink.setId(UuidUtils.generate()); // relink path: id is already assigned before createRelationTwins runs
        twinLink.setRelationTwinId(twinLink.getId()); // adopted from the DB projection in processAlreadyExisted (== id per ID equality)
        TwinEntity survivingRelationTwin = new TwinEntity()
                .setId(twinLink.getId())
                .setTwinClassId(relationTwinClass.getId())
                .setTwinStatus(new org.twins.core.dao.twin.TwinStatusEntity().setType(org.twins.core.enums.status.StatusType.BASIC));
        // loadTwin fetches the surviving relation twin via the relation_twin_id LoadedField —
        // the twinService.load mock emulates that wiring (same as the update-path test below);
        // lenient: prepareTwinLinks also calls the OTHER load overload (loadDstTwin), strict stubbing
        // would flag the varargs stub against it
        lenient().doAnswer(invocation -> {
            Collection<TwinLinkEntity> entities = invocation.getArgument(0);
            for (TwinLinkEntity entity : entities)
                if (survivingRelationTwin.getId().equals(entity.getRelationTwinId()))
                    entity.setRelationTwin(survivingRelationTwin);
            return null;
        }).when(twinService).load(any(), any(org.cambium.service.EntitySecureFindServiceImpl.LoadedField[].class));

        // when
        twinLinkService.addLinks(srcTwin, creates(twinLink), new TwinChangesCollector());

        // then: NO re-creation (merge would silently clobber the living twin) — instead REUSE:
        // the pointer stays wired so the relinked row keeps the FK column (was a null-column bug)
        verify(twinService, never()).createTwins(any(TwinCreateStage.class), any(TwinChangesCollector.class));
        verify(twinService, never()).updateTwin(any(), any(TwinChangesCollector.class), anyBoolean());
        assertEquals(twinLink.getId(), twinLink.getRelationTwinId(), "relink must keep relation_twin_id wired (== id)");
        assertSame(survivingRelationTwin, twinLink.getRelationTwin());
    }

    @Test
    void shouldApplyRelinkFieldsAsUpdateToSurvivingRelationTwin() throws Exception {
        // given: relink carrying fresh relation twin fields
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        twinLink.setId(UuidUtils.generate());
        twinLink.setRelationTwinId(twinLink.getId()); // adopted from the DB projection in processAlreadyExisted
        TwinEntity survivingRelationTwin = new TwinEntity()
                .setId(twinLink.getId())
                .setTwinClassId(relationTwinClass.getId())
                .setTwinStatus(new org.twins.core.dao.twin.TwinStatusEntity().setType(org.twins.core.enums.status.StatusType.BASIC));
        lenient().doAnswer(invocation -> {
            Collection<TwinLinkEntity> entities = invocation.getArgument(0);
            for (TwinLinkEntity entity : entities)
                if (survivingRelationTwin.getId().equals(entity.getRelationTwinId()))
                    entity.setRelationTwin(survivingRelationTwin);
            return null;
        }).when(twinService).load(any(), any(org.cambium.service.EntitySecureFindServiceImpl.LoadedField[].class));
        FieldValue fieldValue = mock(FieldValue.class);
        when(fieldValue.getTwinClassField()).thenReturn(new org.twins.core.dao.twinclass.TwinClassFieldEntity().setId(UuidUtils.generate()));
        TwinLinkCreate twinLinkCreate = new TwinLinkCreate();
        twinLinkCreate.setTwinLink(twinLink);
        twinLinkCreate.setRelationTwinFields(List.of(fieldValue));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.addLinks(srcTwin, List.of(twinLinkCreate), collector);

        // then: fields go as ONE batched field-only update — not through the create pipeline
        ArgumentCaptor<List<TwinUpdate>> captor = ArgumentCaptor.forClass(List.class);
        verify(twinService, never()).createTwins(any(TwinCreateStage.class), any(TwinChangesCollector.class));
        verify(twinService, times(1)).updateTwin(captor.capture(), same(collector), eq(false));
        assertEquals(1, captor.getValue().size());
        TwinUpdate relationTwinUpdate = captor.getValue().get(0);
        assertSame(survivingRelationTwin, relationTwinUpdate.getDbTwinEntity());
        assertSame(fieldValue, relationTwinUpdate.getFields().values().iterator().next());
        assertFalse(relationTwinUpdate.isCanTriggerAfterOperationFactory());
        assertEquals(TwinOperation.Launcher.link, relationTwinUpdate.getLauncher());
    }

    @Test
    void shouldThrowOnRelinkFieldsWhenRelationTwinMissing() {
        // given: late-enabled relation class — the twin_link row predates it, so the DB projection
        // carries no relation_twin_id and loadTwin wires nothing
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        twinLink.setId(UuidUtils.generate()); // relink: id adopted in processAlreadyExisted
        FieldValue fieldValue = mock(FieldValue.class);
        TwinLinkCreate twinLinkCreate = new TwinLinkCreate();
        twinLinkCreate.setTwinLink(twinLink);
        twinLinkCreate.setRelationTwinFields(List.of(fieldValue));

        // when + then: clean ServiceException (same as the update path), not an NPE from setDbTwinEntity(null)
        ServiceException ex = assertThrows(ServiceException.class,
                () -> twinLinkService.addLinks(srcTwin, List.of(twinLinkCreate), new TwinChangesCollector()));
        assertTrue(ex.getMessage().contains("has no relation twin"), "actual: " + ex.getMessage());
    }

    @Test
    void shouldBatchAllRelationTwinsIntoOneCreateCall() throws Exception {
        // given: two links of the same type, both with relation twin classes
        LinkEntity link1 = linkEntity(relationTwinClass.getId());
        LinkEntity link2 = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink1 = twinLinkEntity(link1);
        TwinLinkEntity twinLink2 = twinLinkEntity(link2);
        TwinChangesCollector collector = new TwinChangesCollector();

        // when
        twinLinkService.addLinks(srcTwin, creates(twinLink1, twinLink2), collector);

        // then
        ArgumentCaptor<TwinCreateStage> captor = ArgumentCaptor.forClass(TwinCreateStage.class);
        verify(twinService, times(1)).createTwins(captor.capture(), same(collector));
        assertEquals(2, captor.getValue().size(), "both relation twins must go in ONE batched create call");
        assertNotEquals(twinLink1.getId(), twinLink2.getId());
        assertEquals(twinLink1.getId(), twinLink1.getRelationTwinId());
        assertEquals(twinLink2.getId(), twinLink2.getRelationTwinId());
    }

    @Test
    void shouldBatchRelationTwinFieldUpdatesViaUpdateTwin() throws Exception {
        // given: an existing twin_link whose update carries relation twin fields
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        twinLink.setId(UuidUtils.generate());
        TwinLinkEntity dbTwinLink = twinLinkEntity(link);
        dbTwinLink.setId(twinLink.getId())
                .setSrcTwin(srcTwin)
                .setSrcTwinId(srcTwin.getId());
        when(entitySmartService.findByIdIn(any(), eq(twinLinkRepository), any(), any()))
                .thenReturn(new Kit<>(List.of(dbTwinLink), TwinLinkEntity::getId));
        // relation twin (its id equals the twin_link id by ID equality) is loaded onto the DB entity
        // by loadTwin's relationTwinId LoadedField — the twinService.load mock emulates that wiring
        TwinEntity relationTwin = new TwinEntity()
                .setId(twinLink.getId())
                .setTwinClassId(relationTwinClass.getId())
                .setTwinStatus(new org.twins.core.dao.twin.TwinStatusEntity().setType(org.twins.core.enums.status.StatusType.BASIC));
        dbTwinLink.setRelationTwinId(twinLink.getId()); // DB column value
        doAnswer(invocation -> {
            Collection<TwinLinkEntity> entities = invocation.getArgument(0);
            for (TwinLinkEntity entity : entities)
                if (relationTwin.getId().equals(entity.getRelationTwinId()))
                    entity.setRelationTwin(relationTwin);
            return null;
        }).when(twinService).load(any(), any(org.cambium.service.EntitySecureFindServiceImpl.LoadedField[].class));
        lenient().when(historyService.linkUpdated(any(TwinLinkEntity.class), any(TwinEntity.class), anyBoolean()))
                .thenReturn(new HistoryCollectorMultiTwin());
        FieldValue fieldValue = mock(FieldValue.class);
        when(fieldValue.getTwinClassField()).thenReturn(new org.twins.core.dao.twinclass.TwinClassFieldEntity().setId(UuidUtils.generate()));
        TwinLinkUpdate twinLinkUpdate = new TwinLinkUpdate();
        twinLinkUpdate.setTwinLink(twinLink);
        twinLinkUpdate.setRelationTwinFields(List.of(fieldValue));

        // when
        TwinChangesCollector collector = new TwinChangesCollector();
        twinLinkService.updateTwinLinks(srcTwin, List.of(twinLinkUpdate), collector);

        // then: ONE batched updateTwin call with a properly built field-only TwinUpdate
        ArgumentCaptor<List<TwinUpdate>> captor = ArgumentCaptor.forClass(List.class);
        verify(twinService, times(1)).updateTwin(captor.capture(), same(collector), eq(false));
        org.junit.jupiter.api.Assertions.assertEquals(1, captor.getValue().size());
        TwinUpdate relationTwinUpdate = captor.getValue().get(0);
        assertSame(relationTwin, relationTwinUpdate.getDbTwinEntity(), "db entity must be the loaded relation twin");
        assertEquals(relationTwin.getId(), relationTwinUpdate.getTwinEntity().getId(), "field-only update: clone with same id");
        assertFalse(relationTwinUpdate.isCanTriggerAfterOperationFactory(), "recursion guard must be on");
        assertEquals(TwinOperation.Launcher.link, relationTwinUpdate.getLauncher());
        assertSame(fieldValue, relationTwinUpdate.getFields().values().iterator().next(), "converted fields must be seeded as-is");
    }

    @Test
    void shouldSeedPreconvertedRelationTwinFieldsOnCreate() throws Exception {
        // given: relationTwinFields arrive already converted (List<FieldValue>) on the TwinLinkCreate composition object
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        FieldValue fieldValue = mock(FieldValue.class);
        when(fieldValue.getTwinClassField()).thenReturn(new org.twins.core.dao.twinclass.TwinClassFieldEntity().setId(UuidUtils.generate()));
        TwinLinkCreate twinLinkCreate = new TwinLinkCreate();
        twinLinkCreate.setTwinLink(twinLink);
        twinLinkCreate.setRelationTwinFields(List.of(fieldValue));

        // when: the composition entry (2-arg addLinks unwraps + carries fields by identity)
        twinLinkService.addLinks(srcTwin, List.of(twinLinkCreate));

        // then
        ArgumentCaptor<TwinCreateStage> captor = ArgumentCaptor.forClass(TwinCreateStage.class);
        verify(twinService).createTwins(captor.capture(), any(TwinChangesCollector.class));
        TwinCreate twinCreate = captor.getValue().getTwinCreates().iterator().next();
        assertNotNull(twinCreate.getFields(), "pre-converted fields must be seeded on the TwinCreate as-is");
        assertSame(fieldValue, twinCreate.getFields().values().iterator().next());
    }

    @Test
    void shouldResolveHeadTwinIdForRelationTwinWithHeadClass() throws Exception {
        // given: the relation twin's class demands a head — it must be resolved from the src twin's hierarchy
        UUID headTwinClassId = UuidUtils.generate();
        UUID headTwinId = UuidUtils.generate();
        relationTwinClass.setHeadTwinClassId(headTwinClassId);
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        when(twinHeadService.resolveHeadTwinId(srcTwin, headTwinClassId)).thenReturn(headTwinId);

        // when
        twinLinkService.addLinks(srcTwin, creates(twinLink), new TwinChangesCollector());

        // then
        ArgumentCaptor<TwinCreateStage> captor = ArgumentCaptor.forClass(TwinCreateStage.class);
        verify(twinService).createTwins(captor.capture(), any(TwinChangesCollector.class));
        TwinEntity relationTwin = captor.getValue().getTwinCreates().iterator().next().getTwinEntity();
        assertEquals(headTwinId, relationTwin.getHeadTwinId(),
                "head twin id must be resolved from the src twin's hierarchy (setHeadSafe completes the wiring)");
    }

    @Test
    void shouldResolveHeadTwinIdOncePerDistinctClassInBatch() throws Exception {
        // given: two links sharing the relation twin class — ONE hierarchy resolve for the whole batch
        UUID headTwinClassId = UuidUtils.generate();
        relationTwinClass.setHeadTwinClassId(headTwinClassId);
        LinkEntity link1 = linkEntity(relationTwinClass.getId());
        LinkEntity link2 = linkEntity(relationTwinClass.getId());
        when(twinHeadService.resolveHeadTwinId(srcTwin, headTwinClassId)).thenReturn(UuidUtils.generate());

        // when
        twinLinkService.addLinks(srcTwin, creates(twinLinkEntity(link1), twinLinkEntity(link2)), new TwinChangesCollector());

        // then
        verify(twinHeadService, times(1)).resolveHeadTwinId(srcTwin, headTwinClassId);
    }

    @Test
    void shouldThrowWhenHierarchyHasNoTwinOfRequiredHeadClass() throws Exception {
        // given: no twin of the required class anywhere in the src twin's head hierarchy
        UUID headTwinClassId = UuidUtils.generate();
        relationTwinClass.setHeadTwinClassId(headTwinClassId);
        LinkEntity link = linkEntity(relationTwinClass.getId());
        when(twinHeadService.resolveHeadTwinId(srcTwin, headTwinClassId)).thenReturn(null);

        // when + then: fail fast instead of letting the create pipeline die on a headless twin
        ServiceException ex = assertThrows(ServiceException.class,
                () -> twinLinkService.addLinks(srcTwin, creates(twinLinkEntity(link)), new TwinChangesCollector()));
        assertEquals(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED.getCode(), ex.getErrorCode());
        verify(twinService, never()).createTwins(any(TwinCreateStage.class), any(TwinChangesCollector.class));
    }
}
