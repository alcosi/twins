package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twin.*;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.*;
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
    private UserService userService;
    @Mock
    private TwinService twinService;
    @Mock
    private TwinFieldService twinFieldService;
    @Mock
    private AuthService authService;
    @Mock
    private I18nService i18nService;
    @Mock
    private TwinStatusService twinStatusService;
    @Mock
    private DataListOptionService dataListOptionService;
    @Mock
    private TwinTagService twinTagService;
    @Mock
    private TwinMarkerService twinMarkerService;
    @Mock
    private ApiUser apiUser;

    private TwinGroupedSearchService service;

    @BeforeEach
    void setUp() {
        service = new TwinGroupedSearchService(twinSearchServiceV2, twinClassFieldService, twinClassService,
                userService, twinService, twinFieldService, authService, i18nService, twinStatusService, dataListOptionService,
                twinTagService, twinMarkerService);
    }

    @Test
    void searchIsRequired() {
        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void twoClassesAreAccepted() throws Exception {
        stubBaseFields();
        stubLocale();
        stubTranslations();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), eq((long) TwinGroupedSearchService.MAX_TWINS))).thenReturn(List.of());

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID(), UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.VALUE_ASC))));

        assertTrue(result.getGroups().isEmpty());
    }

    @Test
    void assigneeGroupsKeepTheEmptyKeyLast() throws Exception {
        stubBaseFields();
        stubGetFieldValue();
        UUID assigneeId = SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
        UUID ann = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        TwinEntity task = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(ann);
        TwinEntity unassigned = new TwinEntity().setId(UUID.randomUUID());
        stubLocale();
        stubTranslations();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(task, unassigned));
        when(userService.findEntitiesSafe(any())).thenReturn(new Kit<>(
                List.of(new UserEntity().setId(ann).setName("Ann")), UserEntity::getId));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.VALUE_ASC))));

        assertEquals(Arrays.asList(ann, null), result.getGroups().stream().map(TwinGroup::getKey).toList());
        assertEquals(List.of(task.getId()), result.getGroups().get(0).getTwins().stream().map(TwinEntity::getId).toList());
        // the ids-only reference was bulk-materialized into a concrete user value
        assertEquals("Ann", ((FieldValueUserSingle) result.getGroups().get(0).getKeyValue()).getValue().getName());
        assertNull(result.getGroups().get(1).getKey());
    }

    @Test
    void statusGroupsSortByTheLocalizedI18nName() throws Exception {
        stubBaseFields();
        stubLocale();
        UUID done = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        UUID pending = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
        UUID doneI18n = UUID.randomUUID();
        UUID pendingI18n = UUID.randomUUID();
        TwinEntity doneTask = new TwinEntity().setId(UUID.randomUUID()).setTwinStatusId(done);
        TwinEntity pendingTask = new TwinEntity().setId(UUID.randomUUID()).setTwinStatusId(pending);
        // the eager twinStatus is attached at once, carrying the business key and the i18n id
        when(twinFieldService.getFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class))).thenAnswer(invocation -> {
            TwinEntity twin = invocation.getArgument(0);
            TwinClassFieldEntity field = invocation.getArgument(1);
            return new FieldValueStatus(field).setValue(new TwinStatusEntity()
                    .setId(twin.getTwinStatusId())
                    .setKey(done.equals(twin.getTwinStatusId()) ? "zzz_done" : "aaa_pending")
                    .setNameI18nId(done.equals(twin.getTwinStatusId()) ? doneI18n : pendingI18n));
        });
        when(i18nService.translateToLocale(anySet(), any(Locale.class)))
                .thenReturn(Map.of(doneI18n, "Done", pendingI18n, "Pending"));
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(pendingTask, doneTask));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(status(TwinGroupSort.VALUE_ASC))));

        // the business keys sort the other way round — only the localized names give this order
        assertEquals(List.of(done, pending), result.getGroups().stream().map(TwinGroup::getKey).toList());
    }

    @Test
    void tagsLevelBulkLoadsTheTagKit() throws Exception {
        UUID tagOption1 = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        UUID tagOption2 = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.TAGS);
        TwinEntity tagged = new TwinEntity().setId(UUID.randomUUID());
        TwinEntity untagged = new TwinEntity().setId(UUID.randomUUID());
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(new Kit<>(List.of(field), TwinClassFieldEntity::getId));
        // the bulk kit load fills twinTagKit; getFieldValue then deserializes a FieldValueSelect from it
        doAnswer(invocation -> {
            tagged.setTwinTagKit(new Kit<>(List.of(
                    new DataListOptionEntity().setId(tagOption1),
                    new DataListOptionEntity().setId(tagOption2)), DataListOptionEntity::getId));
            untagged.setTwinTagKit(new Kit<>(new ArrayList<>(), DataListOptionEntity::getId));
            return null;
        }).when(twinTagService).loadTags(anyCollection());
        when(twinFieldService.getFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class))).thenAnswer(invocation -> {
            TwinEntity twin = invocation.getArgument(0);
            TwinClassFieldEntity tagField = invocation.getArgument(1);
            FieldValueSelect select = new FieldValueSelect(tagField);
            if (twin.getTwinTagKit() != null) {
                select.setItems(twin.getTwinTagKit().getList());
            }
            return select;
        });
        stubLocale();
        stubTranslations();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(tagged, untagged));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupLevel().setFieldId(SystemIds.TwinClassField.Base.TAGS).setSort(TwinGroupSort.VALUE_ASC))));

        // one twin lands in every branch of its options, the untagged one under the empty key
        assertEquals(Arrays.asList(tagOption1, tagOption2, null),
                result.getGroups().stream().map(TwinGroup::getKey).toList());
        verify(twinTagService).loadTags(List.of(tagged, untagged));
        verify(twinMarkerService, never()).loadMarkers(anyCollection());
    }

    @Test
    void invalidMemberSortIsRejectedBeforeTheFetch() throws Exception {
        stubBaseFields();
        stubLocale();

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.VALUE_ASC)))
                .setMemberSorts(List.of(new TwinMemberSort()))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).findAllUnpaged(any(), any(), anyLong());
    }

    @Test
    void fieldOfAnotherClassGroupsUnderTheEmptyKey() throws Exception {
        stubGetFieldValue();
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(
                new Kit<>(List.of(field), TwinClassFieldEntity::getId));
        stubLocale();
        stubTranslations();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(twin));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID(), UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupLevel().setFieldId(fieldId).setSort(TwinGroupSort.VALUE_ASC))));

        assertEquals(1, result.getGroups().size());
        assertNull(result.getGroups().get(0).getKey());
        assertEquals(List.of(twin.getId()), result.getGroups().get(0).getTwins().stream().map(TwinEntity::getId).toList());
    }

    @Test
    void unsupportedBasicMemberSortIsRejectedBeforeTheFetch() throws Exception {
        stubBaseFields();
        stubLocale();

        ServiceException exception = assertThrows(ServiceException.class, () -> service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(assignee(TwinGroupSort.VALUE_ASC)))
                .setMemberSorts(List.of(new TwinMemberSort()
                        .setFieldId(SystemIds.TwinClassField.Base.HEAD_ID)
                        .setDirection(SortDirection.DESC)))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
        verify(twinSearchServiceV2, never()).findAllUnpaged(any(), any(), anyLong());
    }

    @Test
    void linkFieldWithoutAValueGroupsUnderTheEmptyKey() throws Exception {
        stubGetFieldValue();
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(
                new Kit<>(List.of(field), TwinClassFieldEntity::getId));
        // canonical load filled the kit, but this twin has no value for the level field
        doAnswer(invocation -> {
            twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            return null;
        }).when(twinService).loadFieldsValues(anyCollection());
        stubLocale();
        stubTranslations();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(twin));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupLevel().setFieldId(fieldId).setSort(TwinGroupSort.VALUE_ASC))));

        assertEquals(1, result.getGroups().size());
        assertNull(result.getGroups().get(0).getKey());
        assertEquals(List.of(twin.getId()), result.getGroups().get(0).getTwins().stream().map(TwinEntity::getId).toList());
    }

    @Test
    void linkFieldIsGroupedByTheFarTwinIdFromTheFieldValuesKit() throws Exception {
        stubGetFieldValue();
        UUID fieldId = UUID.randomUUID();
        UUID dstId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        TwinEntity farTwin = new TwinEntity().setId(dstId);
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(
                new Kit<>(List.of(field), TwinClassFieldEntity::getId));
        // the canonical load fills twin.fieldValuesKit; the FieldValueLink carries the far twins
        doAnswer(invocation -> {
            twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            twin.getFieldValuesKit().add(new FieldValueLink(field).setForwardLink(true).setItems(List.of(farTwin)));
            return null;
        }).when(twinService).loadFieldsValues(anyCollection());
        stubLocale();
        stubTranslations();
        when(twinSearchServiceV2.createSortSpecification(any(), any(), any())).thenReturn((root, query, cb) -> null);
        when(twinSearchServiceV2.findAllUnpaged(any(), any(), anyLong())).thenReturn(List.of(twin));

        TwinGroupedSearchResult result = service.search(new TwinGroupedSearch()
                .setSearch(classes(UUID.randomUUID()))
                .setLevels(List.of(new TwinGroupLevel().setFieldId(fieldId).setSort(TwinGroupSort.VALUE_ASC))));

        assertEquals(1, result.getGroups().size());
        assertEquals(dstId, result.getGroups().get(0).getKey());
        // the far twin rides in the key value: no second findEntitiesSafe load is needed
        assertEquals(farTwin, ((FieldValueLinkSingle) result.getGroups().get(0).getKeyValue()).getValue());
        verify(twinService).loadFieldsValues(List.of(twin));
        verify(twinService, never()).findEntitiesSafe(any());
    }

    /**
     * Base fields are regular twin_class_field rows of the global ancestor class; the test fabricates
     * a bare row per requested id.
     */
    private void stubBaseFields() throws Exception {
        when(twinClassFieldService.findEntitiesSafe(any())).thenAnswer(invocation -> {
            Collection<UUID> ids = invocation.getArgument(0);
            List<TwinClassFieldEntity> fields = new ArrayList<>();
            for (UUID id : ids) {
                fields.add(new TwinClassFieldEntity().setId(id));
            }
            return new Kit<>(fields, TwinClassFieldEntity::getId);
        });
    }

    /**
     * Stands in for TwinFieldService.getFieldValue: the assignee column becomes a reference (or the
     * typed empty value), dynamic fields read the canonical kit, an absent value comes back
     * typed-empty (the empty link shape covers the level-less twins of every dynamic test here —
     * the scalar is null either way).
     */
    private void stubGetFieldValue() throws Exception {
        when(twinFieldService.getFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class))).thenAnswer(invocation -> {
            TwinEntity twin = invocation.getArgument(0);
            TwinClassFieldEntity field = invocation.getArgument(1);
            if (TwinEntity.BasicField.convertOrNull(field.getId()) == TwinEntity.BasicField.ASSIGNEE_USER_ID) {
                return twin.getAssignerUserId() == null
                        ? new FieldValueUserSingle(field)
                        : new FieldValueReferenceSingle(field, FieldValueUserSingle.class, twin.getAssignerUserId());
            }
            FieldValue kitValue = twin.getFieldValuesKit() == null ? null : twin.getFieldValuesKit().get(field.getId());
            return kitValue != null ? kitValue : new FieldValueLink(field);
        });
    }

    private void stubLocale() throws Exception {
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    /**
     * The bulk translate before the sort: no i18n-backed keys in these tests, so the map is empty
     * (the status test stubs its own names).
     */
    private void stubTranslations() {
        when(i18nService.translateToLocale(anySet(), any(Locale.class))).thenReturn(Map.of());
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

    private static TwinGroupLevel status(TwinGroupSort sort) {
        return new TwinGroupLevel()
                .setFieldId(SystemIds.TwinClassField.Base.STATUS_ID)
                .setSort(sort);
    }
}
