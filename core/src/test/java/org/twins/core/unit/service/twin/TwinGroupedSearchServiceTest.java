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
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupCriterion;
import org.twins.core.domain.twin.TwinGroupedSearch;
import org.twins.core.domain.twin.TwinMemberSort;
import org.twins.core.domain.twin.TwinGroupedSearchResult;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageLink;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinGroupedSearchService;
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinlink.TwinLinkService;
import org.twins.core.service.user.UserService;
import org.cambium.featurer.FeaturerService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwinGroupedSearchServiceTest {
    @Mock
    private TwinSearchServiceV2 twinSearchServiceV2;
    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private TwinClassService twinClassService;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private UserService userService;
    @Mock
    private TwinService twinService;
    @Mock
    private TwinLinkService twinLinkService;
    @Mock
    private LinkService linkService;
    @Mock
    private AuthService authService;
    @Mock
    private ApiUser apiUser;
    @Mock
    private JpaSpecificationExecutor<TwinEntity> executor;

    private TwinGroupedSearchService service;

    @BeforeEach
    void setUp() {
        service = new TwinGroupedSearchService(twinSearchServiceV2, twinClassFieldService, twinClassService, featurerService,
                userService, twinService, twinLinkService, linkService, authService);
    }

    @Test
    void rejectsTwoClassesBeforeTheQuery() {
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID(), UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.KEY_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void rejectsAFourthCriterion() {
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.KEY_ASC), assignee(TwinGroupSort.KEY_ASC), assignee(TwinGroupSort.KEY_ASC), assignee(TwinGroupSort.KEY_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void rejectsNameSortForStatus() throws Exception {
        UUID statusId = SystemIds.TwinClassField.Base.STATUS_ID;
        when(twinSearchServiceV2.resolveGroupProvider(statusId)).thenReturn(GroupExpressionProvider.ofField("twinStatusId"));
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupCriterion()
                        .setFieldId(statusId)
                        .setSort(TwinGroupSort.NAME_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void assigneeGroupsKeepTheEmptyKeyLast() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        UUID ann = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        TwinEntity task = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(ann);
        TwinEntity unassigned = new TwinEntity().setId(UUID.randomUUID());
        when(twinClassService.findEntitySafe(classId)).thenReturn(new TwinClassEntity().setId(classId));
        when(twinSearchServiceV2.resolveGroupProvider(assigneeId)).thenReturn(GroupExpressionProvider.ofField("assignerUserId"));
        when(twinSearchServiceV2.count(any(Specification.class))).thenReturn(2L);
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getDomainId()).thenReturn(UUID.randomUUID());
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(twinSearchServiceV2.createFilterSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.jpaSpecificationExecutor()).thenReturn(executor);
        when(executor.findAll(any(Specification.class))).thenReturn(List.of(task, unassigned));
        when(userService.findEntities(any(), any(), any(), any())).thenReturn(new Kit<>(
                List.of(new UserEntity().setId(ann).setName("Ann")), UserEntity::getId));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(classId))
                .setLevels(List.of(assignee(TwinGroupSort.NAME_ASC))));

        assertEquals(Arrays.asList(ann, null), result.getGroups().stream().map(TwinGroup::getKey).toList());
        assertEquals(List.of(task.getId()), result.getGroups().get(0).getTwins().stream().map(TwinEntity::getId).toList());
        assertNull(result.getGroups().get(1).getKey());
    }

    @Test
    void selectionAboveTheLimitIsRejected() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        when(twinClassService.findEntitySafe(classId)).thenReturn(new TwinClassEntity().setId(classId));
        when(twinSearchServiceV2.resolveGroupProvider(assigneeId)).thenReturn(GroupExpressionProvider.ofField("assignerUserId"));
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getDomainId()).thenReturn(UUID.randomUUID());
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(twinSearchServiceV2.createFilterSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.count(any(Specification.class))).thenReturn((long) TwinGroupedSearchService.MAX_TWINS + 1);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(classId))
                .setLevels(List.of(assignee(TwinGroupSort.KEY_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_INCORRECT.getCode(), exception.getErrorCode());
        verify(executor, never()).findAll(any(Specification.class));
    }

    @Test
    void invalidMemberSortIsRejectedBeforeTheCount() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        when(twinClassService.findEntitySafe(classId)).thenReturn(new TwinClassEntity().setId(classId));
        when(twinSearchServiceV2.resolveGroupProvider(assigneeId)).thenReturn(GroupExpressionProvider.ofField("assignerUserId"));
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(classId))
                .setLevels(List.of(assignee(TwinGroupSort.KEY_ASC)))
                .setMemberSorts(List.of(new TwinMemberSort()))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).count(any(Specification.class));
    }

    @Test
    void fieldOutsideTheSearchedClassIsRejectedBeforeTheCount() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        TwinClassEntity twinClass = new TwinClassEntity().setId(classId);
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        when(twinClassService.findEntitySafe(classId)).thenReturn(twinClass);
        when(twinClassFieldService.findEntitySafe(fieldId)).thenReturn(field);
        when(twinClassFieldService.isValidForClass(twinClass, field)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(classId))
                .setLevels(List.of(new TwinGroupCriterion().setFieldId(fieldId).setSort(TwinGroupSort.KEY_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).count(any(Specification.class));
    }

    @Test
    void backwardLinkFieldIsRejectedBeforeTheCount() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        UUID linkId = UUID.randomUUID();
        TwinClassEntity twinClass = new TwinClassEntity().setId(classId);
        HashMap<String, String> params = new HashMap<>();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId).setFieldTyperFeaturerId(1310).setFieldTyperParams(params);
        LinkEntity link = new LinkEntity().setId(linkId);
        FieldTyperLink typer = mock(FieldTyperLink.class);
        when(twinClassService.findEntitySafe(classId)).thenReturn(twinClass);
        when(twinClassFieldService.findEntitySafe(fieldId)).thenReturn(field);
        when(twinClassFieldService.isValidForClass(twinClass, field)).thenReturn(true);
        when(featurerService.getFeaturer(1310, FieldTyper.class)).thenReturn(typer);
        when(typer.getStorage(field)).thenReturn(mock(TwinFieldStorageLink.class));
        when(typer.getLinkId(params)).thenReturn(linkId);
        when(linkService.findEntitySafe(linkId)).thenReturn(link);
        when(linkService.detectLinkDirection(eq(link), isNull())).thenReturn(LinkService.LinkDirection.backward);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(classId))
                .setLevels(List.of(new TwinGroupCriterion().setFieldId(fieldId).setSort(TwinGroupSort.KEY_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).count(any(Specification.class));
    }

    @Test
    void unsupportedBasicMemberSortIsRejectedBeforeTheCount() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        when(twinClassService.findEntitySafe(classId)).thenReturn(new TwinClassEntity().setId(classId));
        when(twinSearchServiceV2.resolveGroupProvider(assigneeId)).thenReturn(GroupExpressionProvider.ofField("assignerUserId"));
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(classId))
                .setLevels(List.of(assignee(TwinGroupSort.KEY_ASC)))
                .setMemberSorts(List.of(new TwinMemberSort()
                        .setFieldId(SystemIds.TwinClassField.Base.HEAD_ID)
                        .setDirection(SortDirection.DESC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).count(any(Specification.class));
    }

    @Test
    void linkCriterionLoadsOnlyTheResolvedLinkIdsInOneQuery() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        UUID linkId = UUID.randomUUID();
        TwinClassEntity twinClass = new TwinClassEntity().setId(classId);
        HashMap<String, String> params = new HashMap<>();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId).setFieldTyperFeaturerId(1310).setFieldTyperParams(params);
        LinkEntity link = new LinkEntity().setId(linkId);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        FieldTyperLink typer = mock(FieldTyperLink.class);
        when(twinClassService.findEntitySafe(classId)).thenReturn(twinClass);
        when(twinClassFieldService.findEntitySafe(fieldId)).thenReturn(field);
        when(twinClassFieldService.isValidForClass(twinClass, field)).thenReturn(true);
        when(featurerService.getFeaturer(1310, FieldTyper.class)).thenReturn(typer);
        when(typer.getStorage(field)).thenReturn(mock(TwinFieldStorageLink.class));
        when(typer.getLinkId(params)).thenReturn(linkId);
        when(linkService.findEntitySafe(linkId)).thenReturn(link);
        when(linkService.detectLinkDirection(eq(link), isNull())).thenReturn(LinkService.LinkDirection.forward);
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getDomainId()).thenReturn(UUID.randomUUID());
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(twinSearchServiceV2.createFilterSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.count(any(Specification.class))).thenReturn(1L);
        when(twinSearchServiceV2.jpaSpecificationExecutor()).thenReturn(executor);
        when(executor.findAll(any(Specification.class))).thenReturn(List.of(twin));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(classId))
                .setLevels(List.of(new TwinGroupCriterion().setFieldId(fieldId).setSort(TwinGroupSort.KEY_ASC))));

        assertEquals(1, result.getGroups().size());
        assertNull(result.getGroups().get(0).getKey());
        verify(twinLinkService).loadTwinForwardLinksByLinkIds(List.of(twin), Set.of(linkId));
    }

    private static BasicSearch classes(UUID... classIds) {
        BasicSearch search = new BasicSearch();
        search.setTwinClassIdList(Set.of(classIds));
        return search;
    }

    private static TwinGroupCriterion assignee(TwinGroupSort sort) {
        return new TwinGroupCriterion()
                .setFieldId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID)
                .setSort(sort);
    }
}
