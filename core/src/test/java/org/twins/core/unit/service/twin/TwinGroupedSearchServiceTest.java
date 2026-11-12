package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupCriterion;
import org.twins.core.domain.twin.TwinGroupedSearch;
import org.twins.core.domain.twin.TwinGroupedSearchResult;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupCriterionType;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.enums.twin.TwinLinkNodeEnd;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinGroupedSearchService;
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.user.UserService;
import org.cambium.featurer.FeaturerService;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwinGroupedSearchServiceTest {
    @Mock
    private TwinSearchServiceV2 twinSearchServiceV2;
    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private UserService userService;
    @Mock
    private TwinService twinService;
    @Mock
    private LinkService linkService;
    @Mock
    private TwinLinkRepository twinLinkRepository;
    @Mock
    private AuthService authService;
    @Mock
    private ApiUser apiUser;
    @Mock
    private JpaSpecificationExecutor<TwinEntity> executor;

    private TwinGroupedSearchService service;

    @BeforeEach
    void setUp() {
        service = new TwinGroupedSearchService(twinSearchServiceV2, twinClassFieldService, featurerService,
                userService, twinService, linkService, twinLinkRepository, authService);
    }

    @Test
    void rejectsTwoClassesBeforeTheQuery() {
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID(), UUID.randomUUID()))
                .setCriteria(List.of(assignee(TwinGroupSort.KEY_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void rejectsAFourthCriterion() {
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setCriteria(List.of(assignee(TwinGroupSort.KEY_ASC), assignee(TwinGroupSort.KEY_ASC), assignee(TwinGroupSort.KEY_ASC), assignee(TwinGroupSort.KEY_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void rejectsNameSortForStatus() throws Exception {
        UUID statusId = SystemIds.TwinClassField.Base.STATUS_ID;
        when(twinSearchServiceV2.resolveGroupProvider(statusId)).thenReturn(GroupExpressionProvider.ofField("twinStatusId"));
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setCriteria(List.of(new TwinGroupCriterion()
                        .setType(TwinGroupCriterionType.FIELD)
                        .setFieldId(statusId)
                        .setSort(TwinGroupSort.NAME_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void assigneeAndLinkBuildTheTreeFromTheFlatRows() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        UUID linkId = UUID.randomUUID();
        UUID ann = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        UUID dst = UUID.fromString("00000000-0000-0000-0000-0000000000d1");
        TwinEntity task = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(ann);
        TwinEntity unassigned = new TwinEntity().setId(UUID.randomUUID());
        when(twinSearchServiceV2.resolveGroupProvider(assigneeId)).thenReturn(GroupExpressionProvider.ofField("assignerUserId"));
        when(linkService.findEntitySafe(linkId)).thenReturn(new LinkEntity().setId(linkId));
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getDomainId()).thenReturn(UUID.randomUUID());
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(twinSearchServiceV2.createFilterSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.jpaSpecificationExecutor()).thenReturn(executor);
        when(executor.findAll(any(Specification.class))).thenReturn(List.of(task, unassigned));
        when(userService.findEntities(any(), any(), any(), any())).thenReturn(new Kit<>(
                List.of(new UserEntity().setId(ann).setName("Ann")), UserEntity::getId));
        when(twinService.findEntities(any(), any(), any(), any())).thenReturn(new Kit<>(
                List.of(new TwinEntity().setId(dst).setName("Work")), TwinEntity::getId));
        when(twinLinkRepository.findByLinkIdInAndEndpointIn(anyCollection(), anyCollection())).thenReturn(List.of(
                new TwinLinkEntity().setLinkId(linkId).setSrcTwinId(task.getId()).setDstTwinId(dst)));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(classId))
                .setCriteria(List.of(
                        assignee(TwinGroupSort.NAME_ASC),
                        new TwinGroupCriterion().setType(TwinGroupCriterionType.LINK).setLinkId(linkId).setNodeEnd(TwinLinkNodeEnd.DST).setSort(TwinGroupSort.NAME_ASC)
                )));

        assertEquals(Arrays.asList(ann, null), result.getGroups().stream().map(TwinGroup::getKey).toList());
        TwinGroup annNode = result.getGroups().get(0);
        assertEquals(1, annNode.getCount());
        assertEquals(dst, annNode.getGroups().get(0).getKey());
        assertEquals(linkId, annNode.getGroups().get(0).getLinkId());
        assertEquals(List.of(task.getId()), annNode.getGroups().get(0).getTwins().stream().map(TwinEntity::getId).toList());
        assertNull(result.getGroups().get(1).getGroups().get(0).getKey());
        assertEquals(linkId, result.getGroups().get(1).getGroups().get(0).getLinkId());
    }

    private static BasicSearch classes(UUID... classIds) {
        BasicSearch search = new BasicSearch();
        search.setTwinClassIdList(Set.of(classIds));
        return search;
    }

    private static TwinGroupCriterion assignee(TwinGroupSort sort) {
        return new TwinGroupCriterion()
                .setType(TwinGroupCriterionType.FIELD)
                .setFieldId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID)
                .setSort(sort);
    }
}
