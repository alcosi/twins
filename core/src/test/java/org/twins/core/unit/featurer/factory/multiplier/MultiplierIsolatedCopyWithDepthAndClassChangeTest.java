package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCopyWithDepthAndClassChange;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedCopyWithDepthAndClassChangeTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private LinkService linkService;

    @Mock
    private TwinClassService twinClassService;

    @Mock
    private AuthService authService;

    private MultiplierIsolatedCopyWithDepthAndClassChange multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedCopyWithDepthAndClassChange(twinSearchService, twinLinkService, linkService);
        setField(multiplier, "twinClassService", twinClassService);
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

    /**
     * Builds properties for the class+link change multiplier.
     *  - twinClassReplaceMap and linksReplaceMap are JSON maps (string→string)
     *  - childrenStatuses is a csv UUID set (optional)
     *  - childrenDepth is an int (default 0)
     *  - srcTwinsByLinkIds / dstTwinsByLinkIds are csv UUID sets (optional)
     */
    private Properties buildProperties(
            Map<UUID, UUID> classReplaceMap,
            Map<UUID, UUID> linkReplaceMap,
            Integer depth,
            Set<UUID> childrenStatuses,
            Set<UUID> srcTwinsByLinkIds,
            Set<UUID> dstTwinsByLinkIds) {
        var props = new Properties();
        props.put("twinClassReplaceMap", jsonMap(classReplaceMap));
        if (linkReplaceMap != null && !linkReplaceMap.isEmpty()) {
            props.put("linksReplaceMap", jsonMap(linkReplaceMap));
        }
        if (depth != null) {
            props.put("childrenDepth", depth.toString());
        }
        if (childrenStatuses != null && !childrenStatuses.isEmpty()) {
            props.put("childrenStatuses", joinIds(childrenStatuses));
        }
        if (srcTwinsByLinkIds != null && !srcTwinsByLinkIds.isEmpty()) {
            props.put("srcTwinsByLinkIds", joinIds(srcTwinsByLinkIds));
        }
        if (dstTwinsByLinkIds != null && !dstTwinsByLinkIds.isEmpty()) {
            props.put("dstTwinsByLinkIds", joinIds(dstTwinsByLinkIds));
        }
        return props;
    }

    private String jsonMap(Map<UUID, UUID> m) {
        var sb = new StringBuilder("{");
        var first = true;
        for (var e : m.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(e.getKey()).append("\": \"").append(e.getValue()).append("\"");
            first = false;
        }
        return sb.append("}").toString();
    }

    private String joinIds(Set<UUID> ids) {
        var sb = new StringBuilder();
        var first = true;
        for (var id : ids) {
            if (!first) sb.append(",");
            sb.append(id.toString());
            first = false;
        }
        return sb.toString();
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

    private ApiUser stubApiUser() throws ServiceException {
        var apiUser = mock(ApiUser.class);
        when(apiUser.getUser()).thenReturn(new UserEntity().setId(UUID.randomUUID()));
        return apiUser;
    }

    @Nested
    class Multiply {

        @Test
        void multiply_inputClassInMap_classReplacedOnCopy() throws ServiceException {
            // contract: every copied twin gets a new class from twinClassReplaceMap.
            var oldClass = UUID.randomUUID();
            var newClass = UUID.randomUUID();
            var inputId = UUID.randomUUID();
            var props = buildProperties(Map.of(oldClass, newClass), null, 0, null, null, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinClassService.findEntitySafe(newClass)).thenReturn(new TwinClassEntity().setId(newClass));
            when(twinSearchService.findTwins(any())).thenReturn(List.of());

            var result = multiplier.multiply(
                    props,
                    List.of(buildInputItem(inputId, oldClass, inputId.toString())),
                    mock(FactoryContext.class));

            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertEquals(newClass, out.getTwinEntity().getTwinClassId());
            assertNotEquals(inputId, out.getTwinEntity().getId()); // fresh id
        }

        @Test
        void multiply_inputClassNotInMap_twinSkipped() throws ServiceException {
            // contract: "Twins with classes not in map keys will be skipped."
            var inputId = UUID.randomUUID();
            var classNotInMap = UUID.randomUUID();
            var someOtherClass = UUID.randomUUID();
            var props = buildProperties(Map.of(someOtherClass, UUID.randomUUID()), null, 0, null, null, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinSearchService.findTwins(any())).thenReturn(List.of());

            var result = multiplier.multiply(
                    props,
                    List.of(buildInputItem(inputId, classNotInMap, inputId.toString())),
                    mock(FactoryContext.class));

            // no copy produced -> output is empty
            assertTrue(result.isEmpty());
        }

        @Test
        void multiply_descendsIntoChildren_childrenWithMappedClassAlsoCopied() throws ServiceException {
            // depth=1: child found via hierarchy search; child's class is in the map -> child copied too,
            // and its head points to the root COPY.
            var oldClass = UUID.randomUUID();
            var newClass = UUID.randomUUID();
            var rootId = UUID.randomUUID();
            var childId = UUID.randomUUID();
            var props = buildProperties(Map.of(oldClass, newClass), null, 1, null, null, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinClassService.findEntitySafe(newClass)).thenReturn(new TwinClassEntity().setId(newClass));
            when(twinSearchService.findTwins(any())).thenReturn(List.of(
                    new TwinEntity()
                            .setId(childId)
                            .setHeadTwinId(rootId)
                            .setTwinClassId(oldClass)
                            .setHierarchyTree(rootId + "." + childId)
                            .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC))));

            var result = multiplier.multiply(
                    props,
                    List.of(buildInputItem(rootId, oldClass, rootId.toString())),
                    mock(FactoryContext.class));

            // 1 root copy + 1 child copy
            assertEquals(2, result.size());

            // identify root copy (no head) and child copy (head == root copy id)
            var rootCopy = result.stream()
                    .map(i -> ((TwinCreate) i.getOutput()).getTwinEntity())
                    .filter(t -> t.getHeadTwinId() == null)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("root copy with null head expected"));
            var childCopy = result.stream()
                    .map(i -> ((TwinCreate) i.getOutput()).getTwinEntity())
                    .filter(t -> t.getHeadTwinId() != null)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("child copy expected"));
            assertEquals(rootCopy.getId(), childCopy.getHeadTwinId());
            assertEquals(newClass, childCopy.getTwinClassId());
        }

        @Test
        void multiply_childClassNotInMap_childSkippedButRootCopied() throws ServiceException {
            // contract: when child's class is NOT in the map, the child is skipped (no copy),
            // but the input twin is still copied (its class IS in the map).
            var rootOldClass = UUID.randomUUID();
            var rootNewClass = UUID.randomUUID();
            var childClassNotInMap = UUID.randomUUID();
            var rootId = UUID.randomUUID();
            var childId = UUID.randomUUID();
            var props = buildProperties(Map.of(rootOldClass, rootNewClass), null, 1, null, null, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinClassService.findEntitySafe(rootNewClass)).thenReturn(new TwinClassEntity().setId(rootNewClass));
            when(twinSearchService.findTwins(any())).thenReturn(List.of(
                    new TwinEntity()
                            .setId(childId)
                            .setHeadTwinId(rootId)
                            .setTwinClassId(childClassNotInMap)
                            .setHierarchyTree(rootId + "." + childId)));

            var result = multiplier.multiply(
                    props,
                    List.of(buildInputItem(rootId, rootOldClass, rootId.toString())),
                    mock(FactoryContext.class));

            // only the root gets copied
            assertEquals(1, result.size());
            assertEquals(rootNewClass, ((TwinCreate) result.get(0).getOutput()).getTwinEntity().getTwinClassId());
        }

        @Test
        void multiply_linkReplaceMapProvided_forwardLinksRemappedToCopiesAndNewLinkType() throws ServiceException {
            // When both class and link replace maps are provided, a forward link between two
            // collected twins is replicated with src/dst remapped to the copy twins AND the
            // link type replaced via linksReplaceMap.
            // Both twins must be collected into origTwins (here as two inputs at the same depth)
            // and at the SAME hierarchy depth — the multiplier's secondary "links-last" sort
            // only applies within a depth, which is what guarantees the dst copy exists before
            // the src's outgoing link is replicated.
            var oldClass = UUID.randomUUID();
            var newClass = UUID.randomUUID();
            var srcId = UUID.randomUUID();
            var dstId = UUID.randomUUID();
            var oldLink = UUID.randomUUID();
            var newLink = UUID.randomUUID();
            var props = buildProperties(Map.of(oldClass, newClass), Map.of(oldLink, newLink), 0, null, null, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinClassService.findEntitySafe(newClass)).thenReturn(new TwinClassEntity().setId(newClass));
            when(twinSearchService.findTwins(any())).thenReturn(List.of());

            // forward link between the two collected input twins
            when(twinLinkService.findAllBetweenTwinsInAndLinkIdIn(any(), any())).thenReturn(new HashSet<>(Set.of(
                    new TwinLinkEntity()
                            .setId(UUID.randomUUID())
                            .setSrcTwinId(srcId)
                            .setSrcTwin(new TwinEntity().setId(srcId))
                            .setDstTwinId(dstId)
                            .setDstTwin(new TwinEntity().setId(dstId))
                            .setLinkId(oldLink)
            )));
            when(linkService.findEntitiesSafe(any())).thenReturn(
                    new org.cambium.common.kit.Kit<>(List.of(new LinkEntity().setId(newLink)), LinkEntity::getId));

            var result = multiplier.multiply(
                    props,
                    List.of(
                            buildInputItem(srcId, oldClass, srcId.toString()),
                            buildInputItem(dstId, oldClass, dstId.toString())),
                    mock(FactoryContext.class));

            // both src and dst inputs copied
            assertEquals(2, result.size());

            // exactly one forward link replicated, with the NEW link type and remapped endpoints
            var replicatedLinks = result.stream()
                    .map(i -> ((TwinCreate) i.getOutput()).getLinksEntityList())
                    .filter(java.util.Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();
            assertEquals(1, replicatedLinks.size());
            var replicated = replicatedLinks.getFirst();
            assertEquals(newLink, replicated.getLinkId());
            assertNotEquals(srcId, replicated.getSrcTwinId());
            assertNotEquals(dstId, replicated.getDstTwinId());
        }

        @Test
        void multiply_emptyLinkReplaceMap_noLinkLookupAtAll() throws ServiceException {
            // contract: if linksReplaceMap is empty/absent, NO link lookup happens (LinksData.EMPTY).
            var oldClass = UUID.randomUUID();
            var newClass = UUID.randomUUID();
            var inputId = UUID.randomUUID();
            var props = buildProperties(Map.of(oldClass, newClass), null, 0, null, null, null);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinClassService.findEntitySafe(newClass)).thenReturn(new TwinClassEntity().setId(newClass));
            when(twinSearchService.findTwins(any())).thenReturn(List.of());

            var result = multiplier.multiply(
                    props,
                    List.of(buildInputItem(inputId, oldClass, inputId.toString())),
                    mock(FactoryContext.class));

            assertEquals(1, result.size());
            verifyNoInteractions(twinLinkService);
            verifyNoInteractions(linkService);
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var apiUser = stubApiUser();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(twinSearchService.findTwins(any())).thenReturn(List.of());

            var result = multiplier.multiply(
                    buildProperties(Map.of(UUID.randomUUID(), UUID.randomUUID()), null, 0, null, null, null),
                    List.of(),
                    mock(FactoryContext.class));

            assertTrue(result.isEmpty());
        }
    }
}
