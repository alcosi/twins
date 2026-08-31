package org.twins.core.unit.service.twinlink;

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
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinCreateStage;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.enums.link.LinkType;
import org.twins.core.enums.twin.TwinCreateStrategy;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryCollectorMultiTwin;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinlink.TwinLinkService;
import org.twins.core.service.user.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        return java.util.Arrays.stream(twinLinks)
                .map(tl -> new TwinLinkCreate().setTwinLink(tl))
                .toList();
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
    void shouldSkipCreationWhenRelationTwinAlreadyExists() throws Exception {
        // given: relink — processAlreadyExisted reuses an existing twin_link id whose relation twin exists
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        twinLink.setId(UuidUtils.generate()); // relink path: id is already assigned before createRelationTwins runs
        when(twinService.findExistingIds(any())).thenReturn(Set.of(twinLink.getId()));

        // when
        twinLinkService.addLinks(srcTwin, creates(twinLink), new TwinChangesCollector());

        // then
        verify(twinService, never()).createTwins(any(TwinCreateStage.class), any(TwinChangesCollector.class));
        assertNull(twinLink.getRelationTwinId(), "idempotency guard must leave relation_twin_id untouched");
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
    void shouldSeedPreconvertedRelationTwinFieldsOnCreate() throws Exception {
        // given: relationTwinFields arrive already converted (List<FieldValue>) on the TwinLinkCreate composition object
        LinkEntity link = linkEntity(relationTwinClass.getId());
        TwinLinkEntity twinLink = twinLinkEntity(link);
        FieldValue fieldValue = mock(FieldValue.class);
        when(fieldValue.getTwinClassField()).thenReturn(new org.twins.core.dao.twinclass.TwinClassFieldEntity().setId(UuidUtils.generate()));
        TwinLinkCreate twinLinkCreate = new TwinLinkCreate()
                .setTwinLink(twinLink)
                .setRelationTwinFields(List.of(fieldValue));

        // when: the composition entry (2-arg addLinks unwraps + carries fields by identity)
        twinLinkService.addLinks(srcTwin, List.of(twinLinkCreate));

        // then
        ArgumentCaptor<TwinCreateStage> captor = ArgumentCaptor.forClass(TwinCreateStage.class);
        verify(twinService).createTwins(captor.capture(), any(TwinChangesCollector.class));
        TwinCreate twinCreate = captor.getValue().getTwinCreates().iterator().next();
        assertNotNull(twinCreate.getFields(), "pre-converted fields must be seeded on the TwinCreate as-is");
        assertSame(fieldValue, twinCreate.getFields().values().iterator().next());
    }
}
