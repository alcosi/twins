package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twin.*;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.twin.TwinGroupedSearchService;
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinstatus.TwinStatusService;
import org.twins.core.service.user.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private AuthService authService;
    @Mock
    private TwinStatusService twinStatusService;
    @Mock
    private DataListOptionService dataListOptionService;
    @Mock
    private ApiUser apiUser;

    private TwinGroupedSearchService service;

    @BeforeEach
    void setUp() {
        service = new TwinGroupedSearchService(twinSearchServiceV2, twinClassFieldService, twinClassService, featurerService,
                userService, twinService, authService, twinStatusService, dataListOptionService);
    }

    @Test
    void searchIsRequired() {
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void twoClassesAreAccepted() throws Exception {
        stubLocale();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), eq((long) TwinGroupedSearchService.MAX_TWINS))).thenReturn(List.of());

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID(), UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.KEY_ASC))));

        assertTrue(result.getGroups().isEmpty());
    }

    @Test
    void rejectsNameSortForStatus() {
        UUID statusId = SystemIds.TwinClassField.Base.STATUS_ID;
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupLevel()
                        .setFieldId(statusId)
                        .setSort(TwinGroupSort.NAME_ASC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void assigneeGroupsKeepTheEmptyKeyLast() throws Exception {
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        UUID ann = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        TwinEntity task = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(ann);
        TwinEntity unassigned = new TwinEntity().setId(UUID.randomUUID());
        stubLocale();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(task, unassigned));
        when(userService.findEntitiesSafe(any())).thenReturn(new Kit<>(
                List.of(new UserEntity().setId(ann).setName("Ann")), UserEntity::getId));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.NAME_ASC))));

        assertEquals(Arrays.asList(ann, null), result.getGroups().stream().map(TwinGroup::getKey).toList());
        assertEquals(List.of(task.getId()), result.getGroups().get(0).getTwins().stream().map(TwinEntity::getId).toList());
        assertNull(result.getGroups().get(1).getKey());
    }

    @Test
    void invalidMemberSortIsRejectedBeforeTheFetch() throws Exception {
        stubLocale();

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.KEY_ASC)))
                .setMemberSorts(List.of(new TwinMemberSort()))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).findAllUnpaged(any(), any(), anyLong());
    }

    @Test
    void fieldOfAnotherClassGroupsUnderTheEmptyKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId).setFieldTyperFeaturerId(1);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        FieldTyper typer = mock(FieldTyper.class);
        TwinFieldStorageSimple storage = mock(TwinFieldStorageSimple.class);
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(
                new Kit<>(List.of(field), TwinClassFieldEntity::getId));
        when(featurerService.getFeaturer(1, FieldTyper.class)).thenReturn(typer);
        when(typer.getStorage(field)).thenReturn(storage);
        stubLocale();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(twin));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID(), UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupLevel().setFieldId(fieldId).setSort(TwinGroupSort.KEY_ASC))));

        assertEquals(1, result.getGroups().size());
        assertNull(result.getGroups().get(0).getKey());
        assertEquals(List.of(twin.getId()), result.getGroups().get(0).getTwins().stream().map(TwinEntity::getId).toList());
    }

    @Test
    void unsupportedBasicMemberSortIsRejectedBeforeTheFetch() throws Exception {
        stubLocale();

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.KEY_ASC)))
                .setMemberSorts(List.of(new TwinMemberSort()
                        .setFieldId(SystemIds.TwinClassField.Base.HEAD_ID)
                        .setDirection(SortDirection.DESC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).findAllUnpaged(any(), any(), anyLong());
    }

    @Test
    void linkFieldWithoutAValueGroupsUnderTheEmptyKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId).setFieldTyperFeaturerId(1310);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        FieldTyperLink typer = mock(FieldTyperLink.class);
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(
                new Kit<>(List.of(field), TwinClassFieldEntity::getId));
        when(featurerService.getFeaturer(1310, FieldTyper.class)).thenReturn(typer);
        when(typer.getStorage(field)).thenReturn(mock(TwinFieldStorageLink.class));
        // canonical load filled the kit, but this twin has no value for the level field
        doAnswer(invocation -> {
            twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            return null;
        }).when(twinService).loadFieldsValues(anyCollection());
        stubLocale();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(twin));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupLevel().setFieldId(fieldId).setSort(TwinGroupSort.KEY_ASC))));

        assertEquals(1, result.getGroups().size());
        assertNull(result.getGroups().get(0).getKey());
        assertEquals(List.of(twin.getId()), result.getGroups().get(0).getTwins().stream().map(TwinEntity::getId).toList());
    }

    @Test
    void linkFieldIsGroupedByTheFarTwinIdFromTheFieldValuesKit() throws Exception {
        UUID fieldId = UUID.randomUUID();
        UUID dstId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId).setFieldTyperFeaturerId(1310);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        TwinEntity farTwin = new TwinEntity().setId(dstId);
        FieldTyperLink typer = mock(FieldTyperLink.class);
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(
                new Kit<>(List.of(field), TwinClassFieldEntity::getId));
        when(featurerService.getFeaturer(1310, FieldTyper.class)).thenReturn(typer);
        when(typer.getStorage(field)).thenReturn(mock(TwinFieldStorageLink.class));
        // the canonical load fills twin.fieldValuesKit; the FieldValueLink carries the far twins
        doAnswer(invocation -> {
            twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            twin.getFieldValuesKit().add(new FieldValueLink(field).setForwardLink(true).setItems(List.of(farTwin)));
            return null;
        }).when(twinService).loadFieldsValues(anyCollection());
        when(twinService.findEntitiesSafe(any())).thenReturn(
                new Kit<>(List.of(farTwin), TwinEntity::getId));
        stubLocale();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(twin));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupLevel().setFieldId(fieldId).setSort(TwinGroupSort.KEY_ASC))));

        assertEquals(1, result.getGroups().size());
        assertEquals(dstId, result.getGroups().get(0).getKey());
        assertEquals(farTwin, result.getGroups().get(0).getKeyEntity());
        verify(twinService).loadFieldsValues(List.of(twin));
    }

    private void stubLocale() throws Exception {
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    private static BasicSearch classes(UUID... classIds) {
        BasicSearch search = new BasicSearch();
        search.setTwinClassIdList(Set.of(classIds));
        return search;
    }

    private static TwinGroupLevel assignee(TwinGroupSort sort) {
        return new TwinGroupLevel()
                .setFieldId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID)
                .setSort(sort);
    }
}
