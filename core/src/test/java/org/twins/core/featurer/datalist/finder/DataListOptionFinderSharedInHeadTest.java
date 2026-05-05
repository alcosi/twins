package org.twins.core.featurer.datalist.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataListOptionFinderSharedInHeadTest extends BaseUnitTest {

    @Mock
    private DataListService dataListService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    @Mock
    private FeaturerService featurerService;

    private DataListOptionFinderSharedInHead finder;

    private UUID headTwinId;
    private UUID twinClassFieldId;
    private UUID dataListId;
    private Set<UUID> optionIds;

    @BeforeEach
    void setUp() {
        finder = new DataListOptionFinderSharedInHead(dataListService, twinClassFieldService);
        finder.featurerService = featurerService;

        headTwinId = UUID.randomUUID();
        twinClassFieldId = UUID.randomUUID();
        dataListId = UUID.randomUUID();
        optionIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
    }

    private Map<String, String> namedParams() {
        return Map.of(
                DataListOptionFinder.PARAM_CURRENT_HEAD_TWIN_ID, headTwinId.toString(),
                DataListOptionFinder.PARAM_CURRENT_TWIN_CLASS_FIELD_ID, twinClassFieldId.toString()
        );
    }

    private TwinClassFieldEntity fieldEntity(int featurerId, HashMap<String, String> params) {
        var entity = new TwinClassFieldEntity();
        entity.setFieldTyperFeaturerId(featurerId);
        entity.setFieldTyperParams(params);
        return entity;
    }

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_populatesSearchCorrectly() throws ServiceException {
            var fieldTyperParams = new HashMap<String, String>();
            var fieldEntity = fieldEntity(100, fieldTyperParams);
            var fieldTyper = mock(FieldTyper.class);
            var fieldTyperProperties = new Properties();

            when(twinClassFieldService.findEntitySafe(twinClassFieldId)).thenReturn(fieldEntity);
            when(featurerService.getFeaturer(100, FieldTyper.class)).thenReturn(fieldTyper);
            doReturn(TwinFieldStorageDatalist.class).when(fieldTyper).getStorageType();
            doReturn(fieldTyperProperties).when(fieldTyper).extractProperties(fieldTyperParams);
            when(dataListService.findOptionIdsByDataListIdAndNotUsedInHead(dataListId, twinClassFieldId, headTwinId))
                    .thenReturn(optionIds);

            try (MockedStatic<FieldTyperList> fieldTyperListStatic = mockStatic(FieldTyperList.class)) {
                fieldTyperListStatic.when(() -> FieldTyperList.getDataListId(fieldTyperProperties)).thenReturn(dataListId);

                var search = new DataListOptionSearch();
                finder.concatSearch(new Properties(), search, namedParams());

                assertEquals(1, search.getDataListIdList().size());
                assertTrue(search.getDataListIdList().contains(dataListId));
                assertEquals(optionIds, search.getIdList());
            }
        }

        @Test
        void concatSearch_wrongFieldTyperType_throwsException() throws ServiceException {
            var fieldEntity = fieldEntity(100, new HashMap<>());
            var wrongTyper = mock(FieldTyper.class);

            when(twinClassFieldService.findEntitySafe(twinClassFieldId)).thenReturn(fieldEntity);
            when(featurerService.getFeaturer(100, FieldTyper.class)).thenReturn(wrongTyper);
            doReturn(TwinFieldStorageSimple.class).when(wrongTyper).getStorageType();

            var search = new DataListOptionSearch();

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.concatSearch(new Properties(), search, namedParams())
            );
            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE.getCode(), ex.getErrorCode());
        }
    }
}
