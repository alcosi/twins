package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCopyWithDepth;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedCopyWithDepthTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private AuthService authService;

    @Mock
    private TwinService twinService;

    private MultiplierIsolatedCopyWithDepth multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedCopyWithDepth(twinSearchService, twinLinkService, twinService);
        setField(multiplier, "authService", authService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private Properties buildProperties(int depth, String statusesCsv) {
        var props = new Properties();
        props.put("childrenDepth", Integer.toString(depth));
        if (statusesCsv != null) {
            props.put("childrenStatuses", statusesCsv);
        }
        return props;
    }

    private FactoryItem buildInputItem(UUID twinId, UUID classId, String hierarchyTree) {
        var twin = new TwinEntity()
                .setId(twinId)
                .setTwinClassId(classId)
                .setHierarchyTree(hierarchyTree)
                .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twin);
        return new FactoryItem().setOutput(twinCreate);
    }

    private TwinEntity buildChild(UUID id, UUID headId, UUID classId, String hierarchyTree) {
        return new TwinEntity()
                .setId(id)
                .setHeadTwinId(headId)
                .setTwinClassId(classId)
                .setHierarchyTree(hierarchyTree)
                .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
    }

    private ApiUser stubApiUser() throws ServiceException {
        var apiUser = mock(ApiUser.class);
        when(apiUser.getUser()).thenReturn(new UserEntity().setId(UUID.randomUUID()));
        return apiUser;
    }

    @Nested
    class Multiply {

        @Test
        void multiply_depthZero_noChildren_searchReturnsEmpty_onlyInputCopied() throws ServiceException {
            // depth=0 -> HierarchySearch depth 0 means "input twins only"; search yields no extra children.
            var classId = UUID.randomUUID();
            var inputId = UUID.randomUUID();
            var props = buildProperties(0, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinSearchService.findTwins(any())).thenReturn(List.of());
            when(twinLinkService.findAllBetweenTwinsIn(any())).thenReturn(new HashSet<>());

            var input = List.of(buildInputItem(inputId, classId, inputId.toString()));

            var result = multiplier.multiply(props, input, mock(FactoryContext.class));

            // exactly one output per input (no children)
            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertEquals(classId, out.getTwinEntity().getTwinClassId());
            // copy must have a fresh id (not the input id)
            assertNotEquals(inputId, out.getTwinEntity().getId());
            assertNotNull(out.getTwinEntity().getId());
        }

        @Test
        void multiply_descendsIntoChildren_eachChildGetsItsOwnCopy() throws ServiceException {
            // contract: "CopyWithDepth descends N levels" — depth=1 collects direct children,
            // and each collected child is itself wrapped in a FactoryItem output.
            var classId = UUID.randomUUID();
            var rootId = UUID.randomUUID();
            var childId = UUID.randomUUID();
            var props = buildProperties(1, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            // search returns the direct child
            when(twinSearchService.findTwins(any())).thenReturn(List.of(
                    buildChild(childId, rootId, classId, rootId + "." + childId)));
            when(twinLinkService.findAllBetweenTwinsIn(any())).thenReturn(new HashSet<>());

            var input = List.of(buildInputItem(rootId, classId, rootId.toString()));

            var result = multiplier.multiply(props, input, mock(FactoryContext.class));

            // 1 root copy + 1 child copy
            assertEquals(2, result.size());

            // one of the outputs must be the child copy, with its head pointing to the root's COPY
            var copies = new HashMap<UUID, TwinEntity>();
            for (var item : result) {
                var t = ((TwinCreate) item.getOutput()).getTwinEntity();
                copies.put(t.getId(), t);
            }
            assertEquals(2, copies.size());

            // find root copy (the one whose origFactoryItem is the input)
            var rootCopyOpt = result.stream()
                    .filter(i -> ((TwinCreate) i.getOutput()).getTwinEntity().getHeadTwinId() == null)
                    .findFirst();
            // root has no head in input, so its copy has no head either
            assertTrue(rootCopyOpt.isPresent(), "expected a copy with null head (the root)");
            var rootCopy = ((TwinCreate) rootCopyOpt.get().getOutput()).getTwinEntity();

            // the other copy is the child; its headTwinId must equal the root copy's NEW id
            var childCopyOpt = copies.values().stream()
                    .filter(t -> !t.getId().equals(rootCopy.getId()))
                    .findFirst();
            assertTrue(childCopyOpt.isPresent());
            assertEquals(rootCopy.getId(), childCopyOpt.get().getHeadTwinId());
        }

        @Test
        void multiply_forwardLinksReplicated_linkSrcAndDstRemappedToCopies() throws ServiceException {
            // When twins in the collected set are linked, the link is copied forward with
            // src/dst remapped to the copy twins. The original linkId is preserved.
            var classId = UUID.randomUUID();
            var rootId = UUID.randomUUID();
            var dstId = UUID.randomUUID();
            var linkId = UUID.randomUUID();
            var props = buildProperties(0, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinSearchService.findTwins(any())).thenReturn(List.of());
            when(twinLinkService.findAllBetweenTwinsIn(any())).thenReturn(new HashSet<>(Set.of(
                    new TwinLinkEntity()
                            .setId(UUID.randomUUID())
                            .setSrcTwinId(rootId)
                            .setSrcTwin(new TwinEntity()
                                    .setId(rootId)
                                    .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC)))
                            .setDstTwinId(dstId)
                            // dst twin's headTwinId MUST point to a twin already in copyContextMap
                            // (here the src input) — CopyWithDepth.createCopyContext looks up
                            // copyContextMap.get(headTwinId).getOrigFactoryItem() for on-demand children.
                            .setDstTwin(new TwinEntity()
                                    .setId(dstId)
                                    .setHeadTwinId(rootId)
                                    .setHierarchyTree(rootId + "." + dstId)
                                    .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC)))
                            .setLinkId(linkId)
            )));

            var input = List.of(buildInputItem(rootId, classId, rootId.toString()));

            var result = multiplier.multiply(props, input, mock(FactoryContext.class));

            // The dst twin was not in the input set, but it is pulled in via the link.
            // Both src (root) and dst twins get copies; the link is replicated forward.
            assertEquals(2, result.size());

            // one of the copies must carry a non-empty linksEntityList with the replicated link
            var replicatedLink = result.stream()
                    .map(i -> ((TwinCreate) i.getOutput()).getLinksEntityList())
                    .filter(java.util.Objects::nonNull)
                    .filter(list -> !list.isEmpty())
                    .flatMap(List::stream)
                    .findFirst();
            assertTrue(replicatedLink.isPresent(), "expected at least one replicated forward link");
            assertEquals(linkId, replicatedLink.get().getLinkId());
            // src and dst of the replicated link must be the COPY twins (new ids, not originals)
            assertNotEquals(rootId, replicatedLink.get().getSrcTwinId());
            assertNotEquals(dstId, replicatedLink.get().getDstTwinId());
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var apiUser = stubApiUser();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinSearchService.findTwins(any())).thenReturn(List.of());
            when(twinLinkService.findAllBetweenTwinsIn(any())).thenReturn(new HashSet<>());

            var result = multiplier.multiply(buildProperties(1, null), List.of(), mock(FactoryContext.class));

            assertTrue(result.isEmpty());
        }

        @Test
        void multiply_statusFilterPropagatedToBothSearchAndLinkLookup() throws ServiceException {
            // When childrenStatuses is set, the status filter flows into BOTH the twin search AND
            // the link lookup (which uses the status-aware variant).
            var classId = UUID.randomUUID();
            var inputId = UUID.randomUUID();
            var status1 = UUID.randomUUID();
            var props = buildProperties(1, status1.toString());

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinSearchService.findTwins(any())).thenReturn(List.of());
            when(twinLinkService.findAllBetweenTwinsInAndTwinsInStatusIds(any(), any())).thenReturn(new HashSet<>());

            multiplier.multiply(props, List.of(buildInputItem(inputId, classId, inputId.toString())), mock(FactoryContext.class));

            // status-aware link variant used
            verify(twinLinkService).findAllBetweenTwinsInAndTwinsInStatusIds(any(), any());
            verify(twinLinkService, never()).findAllBetweenTwinsIn(any());
        }
    }
}
