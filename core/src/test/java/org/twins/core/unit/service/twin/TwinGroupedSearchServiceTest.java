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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupedSearch;
import org.twins.core.domain.twin.TwinGroupedSearchResult;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinGroupedSearchService;
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.user.UserService;
import org.cambium.featurer.FeaturerService;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
    private FeaturerService featurerService;
    @Mock
    private UserService userService;
    @Mock
    private TwinService twinService;
    @Mock
    private AuthService authService;
    @Mock
    private ApiUser apiUser;
    @Mock
    private JpaSpecificationExecutor<TwinEntity> executor;

    private TwinGroupedSearchService service;

    @BeforeEach
    void setUp() {
        service = new TwinGroupedSearchService(twinSearchServiceV2, twinClassFieldService, featurerService, userService, twinService, authService);
    }

    @Test
    void nameSortForStatusIsRejectedBeforeTheQuery() throws Exception {
        UUID statusId = SystemIds.TwinClassField.Base.STATUS_ID;
        when(twinSearchServiceV2.resolveGroupProvider(statusId)).thenReturn(GroupExpressionProvider.ofField("twinStatusId"));
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setGroupFieldId(statusId)
                .setGroupSort(TwinGroupSort.NAME_ASC)));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).count(any(Specification.class));
    }

    @Test
    void selectionAboveTheLimitIsRejectedWhole() throws Exception {
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        when(twinSearchServiceV2.resolveGroupProvider(assigneeId)).thenReturn(GroupExpressionProvider.ofField("assignerUserId"));
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getDomainId()).thenReturn(UUID.randomUUID());
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(twinSearchServiceV2.emptySearch()).thenReturn(new org.twins.core.domain.search.BasicSearch());
        when(twinSearchServiceV2.createFilterSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.count(any(Specification.class))).thenReturn((long) TwinGroupedSearchService.MAX_TWINS + 1);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setGroupFieldId(assigneeId)
                .setGroupSort(TwinGroupSort.KEY_ASC)));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_INCORRECT.getCode(), exception.getErrorCode());
        verify(executor, never()).findAll(any(Specification.class));
    }

    @Test
    void assigneeGroupsFollowLoadedNamesAndKeepTheEmptyKeyLast() throws Exception {
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        UUID ann = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        UUID bob = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
        TwinEntity annTask = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(ann);
        TwinEntity bobTask = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(bob);
        TwinEntity empty = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(null);
        when(twinSearchServiceV2.resolveGroupProvider(assigneeId)).thenReturn(GroupExpressionProvider.ofField("assignerUserId"));
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getDomainId()).thenReturn(UUID.randomUUID());
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(twinSearchServiceV2.emptySearch()).thenReturn(new org.twins.core.domain.search.BasicSearch());
        when(twinSearchServiceV2.createFilterSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.count(any(Specification.class))).thenReturn(3L);
        when(twinSearchServiceV2.jpaSpecificationExecutor()).thenReturn(executor);
        when(executor.findAll(any(Specification.class))).thenReturn(List.of(bobTask, annTask, empty));
        when(userService.findEntities(anyList(), any(), any(), any())).thenReturn(new Kit<>(
                List.of(new UserEntity().setId(ann).setName("Ann"), new UserEntity().setId(bob).setName("Bob")),
                UserEntity::getId));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setGroupFieldId(assigneeId)
                .setGroupSort(TwinGroupSort.NAME_ASC));

        assertEquals(Arrays.asList(ann, bob, null), result.getGroups().stream().map(TwinGroup::getKey).toList());
        assertEquals(1, result.getGroups().get(2).getTwins().size());
    }
}
