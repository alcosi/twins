package org.twins.core.featurer.classfield.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldFinderByIdExcludeHeadMatchedTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldFinderByIdExcludeHeadMatched finder;

    private UUID twinId;
    private UUID twinClassId;
    private UUID headTwinId;
    private UUID headTwinClassId;

    @BeforeEach
    void setUp() {
        finder = new FieldFinderByIdExcludeHeadMatched(twinService, twinClassFieldService);

        twinId = UUID.randomUUID();
        twinClassId = UUID.randomUUID();
        headTwinId = UUID.randomUUID();
        headTwinClassId = UUID.randomUUID();
    }

    private TwinEntity twinEntity() {
        var twin = new TwinEntity();
        twin.setId(twinId);
        twin.setTwinClassId(twinClassId);
        twin.setHeadTwinId(headTwinId);
        return twin;
    }

    private TwinEntity headTwinEntity() {
        var headTwin = new TwinEntity();
        headTwin.setId(headTwinId);
        headTwin.setTwinClassId(headTwinClassId);
        return headTwin;
    }

    private TwinClassEntity twinClassEntity(UUID classId) {
        var twinClass = new TwinClassEntity();
        twinClass.setId(classId);
        return twinClass;
    }

    private TwinClassFieldEntity fieldEntity(UUID id, String key, int fieldTyperFeaturerId, HashMap<String, String> params) {
        var field = new TwinClassFieldEntity();
        field.setId(id);
        field.setKey(key);
        field.setFieldTyperFeaturerId(fieldTyperFeaturerId);
        field.setFieldTyperParams(params);
        return field;
    }

    private Map<String, String> namedParams() {
        return Map.of(FieldFinder.PARAM_CURRENT_TWIN_ID, twinId.toString());
    }

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_matchingFields_excludesMatchedIds() throws ServiceException {
            var fieldTyperId = 100;
            var currentFieldId = UUID.randomUUID();
            var headFieldId = UUID.randomUUID();
            var params = new HashMap<String, String>();
            params.put("p1", "v1");

            var twin = twinEntity();
            var headTwin = headTwinEntity();
            twin.setHeadTwin(headTwin);
            twin.setTwinClass(twinClassEntity(twinClassId));
            headTwin.setTwinClass(twinClassEntity(headTwinClassId));

            var headField = fieldEntity(headFieldId, "fieldA", fieldTyperId, params);
            var currentField = fieldEntity(currentFieldId, "fieldA", fieldTyperId, params);

            ((TwinClassEntity) headTwin.getTwinClass()).setTwinClassFieldKit(new Kit<>(TwinClassFieldEntity::getId));
            headTwin.getTwinClass().getTwinClassFieldKit().add(headField);
            ((TwinClassEntity) twin.getTwinClass()).setTwinClassFieldKit(new Kit<>(TwinClassFieldEntity::getId));
            twin.getTwinClass().getTwinClassFieldKit().add(currentField);

            var properties = new Properties();
            properties.setProperty("exclude", "false");

            when(twinService.findEntitySafe(twinId)).thenReturn(twin);
            doAnswer(invocation -> {
                TwinEntity t = invocation.getArgument(0);
                t.setHeadTwin(headTwin);
                return null;
            }).when(twinService).loadHead(twin);

            var search = new TwinClassFieldSearch();
            finder.concatSearch(properties, search, namedParams());

            assertTrue(search.getIdList().contains(currentFieldId));
        }

        @Test
        void concatSearch_excludeTrue_addsToExcludeList() throws ServiceException {
            var fieldTyperId = 100;
            var currentFieldId = UUID.randomUUID();
            var headFieldId = UUID.randomUUID();
            var params = new HashMap<String, String>();
            params.put("p1", "v1");

            var twin = twinEntity();
            var headTwin = headTwinEntity();
            twin.setHeadTwin(headTwin);
            twin.setTwinClass(twinClassEntity(twinClassId));
            headTwin.setTwinClass(twinClassEntity(headTwinClassId));

            var headField = fieldEntity(headFieldId, "fieldA", fieldTyperId, params);
            var currentField = fieldEntity(currentFieldId, "fieldA", fieldTyperId, params);

            ((TwinClassEntity) headTwin.getTwinClass()).setTwinClassFieldKit(new Kit<>(TwinClassFieldEntity::getId));
            headTwin.getTwinClass().getTwinClassFieldKit().add(headField);
            ((TwinClassEntity) twin.getTwinClass()).setTwinClassFieldKit(new Kit<>(TwinClassFieldEntity::getId));
            twin.getTwinClass().getTwinClassFieldKit().add(currentField);

            var properties = new Properties();
            properties.setProperty("exclude", "true");

            when(twinService.findEntitySafe(twinId)).thenReturn(twin);
            doAnswer(invocation -> {
                TwinEntity t = invocation.getArgument(0);
                t.setHeadTwin(headTwin);
                return null;
            }).when(twinService).loadHead(twin);

            var search = new TwinClassFieldSearch();
            finder.concatSearch(properties, search, namedParams());

            assertTrue(search.getIdExcludeList().contains(currentFieldId));
            assertNull(search.getIdList());
        }

        @Test
        void concatSearch_noMatchingFields_emptyExcludeSet() throws ServiceException {
            var currentFieldId = UUID.randomUUID();
            var headFieldId = UUID.randomUUID();

            var twin = twinEntity();
            var headTwin = headTwinEntity();
            twin.setHeadTwin(headTwin);
            twin.setTwinClass(twinClassEntity(twinClassId));
            headTwin.setTwinClass(twinClassEntity(headTwinClassId));

            var headField = fieldEntity(headFieldId, "fieldA", 100, new HashMap<>());
            var currentField = fieldEntity(currentFieldId, "fieldB", 200, new HashMap<>());

            ((TwinClassEntity) headTwin.getTwinClass()).setTwinClassFieldKit(new Kit<>(TwinClassFieldEntity::getId));
            headTwin.getTwinClass().getTwinClassFieldKit().add(headField);
            ((TwinClassEntity) twin.getTwinClass()).setTwinClassFieldKit(new Kit<>(TwinClassFieldEntity::getId));
            twin.getTwinClass().getTwinClassFieldKit().add(currentField);

            var properties = new Properties();
            properties.setProperty("exclude", "false");

            when(twinService.findEntitySafe(twinId)).thenReturn(twin);
            doAnswer(invocation -> {
                TwinEntity t = invocation.getArgument(0);
                t.setHeadTwin(headTwin);
                return null;
            }).when(twinService).loadHead(twin);

            var search = new TwinClassFieldSearch();
            finder.concatSearch(properties, search, namedParams());

            assertNull(search.getIdList());
            assertNull(search.getIdExcludeList());
        }

        @Test
        void concatSearch_collidingConcatenation_notTreatedAsMatch() throws ServiceException {
            var currentFieldId = UUID.randomUUID();
            var headFieldId = UUID.randomUUID();

            var twin = twinEntity();
            var headTwin = headTwinEntity();
            twin.setHeadTwin(headTwin);
            twin.setTwinClass(twinClassEntity(twinClassId));
            headTwin.setTwinClass(twinClassEntity(headTwinClassId));

            // Without separators both match codes concatenate to "a12" and would be treated as a match
            var headField = fieldEntity(headFieldId, "a1", 2, null);
            var currentField = fieldEntity(currentFieldId, "a", 12, null);

            ((TwinClassEntity) headTwin.getTwinClass()).setTwinClassFieldKit(new Kit<>(TwinClassFieldEntity::getId));
            headTwin.getTwinClass().getTwinClassFieldKit().add(headField);
            ((TwinClassEntity) twin.getTwinClass()).setTwinClassFieldKit(new Kit<>(TwinClassFieldEntity::getId));
            twin.getTwinClass().getTwinClassFieldKit().add(currentField);

            var properties = new Properties();
            properties.setProperty("exclude", "false");

            when(twinService.findEntitySafe(twinId)).thenReturn(twin);
            doAnswer(invocation -> {
                TwinEntity t = invocation.getArgument(0);
                t.setHeadTwin(headTwin);
                return null;
            }).when(twinService).loadHead(twin);

            var search = new TwinClassFieldSearch();
            finder.concatSearch(properties, search, namedParams());

            assertNull(search.getIdList());
            assertNull(search.getIdExcludeList());
        }

        @Test
        void concatSearch_missingTwinId_throwsException() {
            var properties = new Properties();
            var search = new TwinClassFieldSearch();

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.concatSearch(properties, search, Map.of())
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concatSearch_blankTwinId_throwsException() {
            var properties = new Properties();
            var search = new TwinClassFieldSearch();
            var namedParams = Map.of(FieldFinder.PARAM_CURRENT_TWIN_ID, "");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.concatSearch(properties, search, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concatSearch_nonUuidTwinId_throwsException() {
            var properties = new Properties();
            var search = new TwinClassFieldSearch();
            var namedParams = Map.of(FieldFinder.PARAM_CURRENT_TWIN_ID, "not-a-uuid");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.concatSearch(properties, search, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concatSearch_noHeadTwin_throwsException() throws ServiceException {
            var twin = twinEntity();

            var properties = new Properties();
            when(twinService.findEntitySafe(twinId)).thenReturn(twin);
            doAnswer(invocation -> {
                TwinEntity t = invocation.getArgument(0);
                t.setHeadTwin(null);
                return null;
            }).when(twinService).loadHead(twin);

            var search = new TwinClassFieldSearch();

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.concatSearch(properties, search, namedParams())
            );
            assertEquals(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED.getCode(), ex.getErrorCode());
        }
    }
}
