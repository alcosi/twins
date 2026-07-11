package org.twins.core.featurer.user.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UserFinderHeadBySpaceIdAndRoleIdTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private UserFinderHeadBySpaceIdAndRoleId finder;

    @BeforeEach
    void setUp() throws Exception {
        finder = new UserFinderHeadBySpaceIdAndRoleId();
        injectTwinService(finder, twinService);
    }

    private void injectTwinService(Object target, Object value) throws Exception {
        var field = target.getClass().getDeclaredField("twinService");
        field.setAccessible(true);
        field.set(target, value);
    }

    private Properties buildProperties(UUID roleId, boolean exclude) {
        var props = new Properties();
        if (roleId != null) {
            props.setProperty("roleId", roleId.toString());
        }
        props.setProperty("exclude", String.valueOf(exclude));
        props.setProperty("paramKey", "spaceId");
        return props;
    }

    private Properties buildProperties(UUID roleId, boolean exclude, String paramKey) {
        var props = buildProperties(roleId, exclude);
        if (paramKey != null) {
            props.setProperty("paramKey", paramKey);
        }
        return props;
    }

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_addsSpaceToListWhenExcludeFalse() throws ServiceException {
            var roleId = UUID.randomUUID();
            var spaceId = UUID.randomUUID();
            var headTwinId = UUID.randomUUID();
            var headTwin = new TwinEntity();
            headTwin.setId(headTwinId);
            var props = buildProperties(roleId, false);
            var userSearch = new UserSearch();

            when(twinService.findHeadTwin(spaceId)).thenReturn(headTwin);

            finder.concatSearch(props, userSearch, Map.of("spaceId", spaceId.toString()));

            assertNotNull(userSearch.getSpaceList());
            assertEquals(1, userSearch.getSpaceList().size());
            assertNull(userSearch.getSpaceExcludeList());

            var spaceSearch = userSearch.getSpaceList().get(0);
            assertEquals(roleId, spaceSearch.getRoleId());
            assertEquals(headTwinId, spaceSearch.getSpaceId());
        }

        @Test
        void concatSearch_addsSpaceToExcludeListWhenExcludeTrue() throws ServiceException {
            var roleId = UUID.randomUUID();
            var spaceId = UUID.randomUUID();
            var headTwinId = UUID.randomUUID();
            var headTwin = new TwinEntity();
            headTwin.setId(headTwinId);
            var props = buildProperties(roleId, true);
            var userSearch = new UserSearch();

            when(twinService.findHeadTwin(spaceId)).thenReturn(headTwin);

            finder.concatSearch(props, userSearch, Map.of("spaceId", spaceId.toString()));

            assertNotNull(userSearch.getSpaceExcludeList());
            assertEquals(1, userSearch.getSpaceExcludeList().size());
            assertNull(userSearch.getSpaceList());

            var spaceSearch = userSearch.getSpaceExcludeList().get(0);
            assertEquals(roleId, spaceSearch.getRoleId());
            assertEquals(headTwinId, spaceSearch.getSpaceId());
        }

        @Test
        void concatSearch_usesHeadTwinIdNotOriginalSpaceId() throws ServiceException {
            var roleId = UUID.randomUUID();
            var spaceId = UUID.randomUUID();
            var headTwinId = UUID.randomUUID();
            var headTwin = new TwinEntity();
            headTwin.setId(headTwinId);
            var props = buildProperties(roleId, false);
            var userSearch = new UserSearch();

            when(twinService.findHeadTwin(spaceId)).thenReturn(headTwin);

            finder.concatSearch(props, userSearch, Map.of("spaceId", spaceId.toString()));

            var spaceSearch = userSearch.getSpaceList().get(0);
            assertEquals(headTwinId, spaceSearch.getSpaceId());
            assertNotEquals(spaceId, spaceSearch.getSpaceId());
        }

        @Test
        void concatSearch_usesCustomParamKey() throws ServiceException {
            var roleId = UUID.randomUUID();
            var spaceId = UUID.randomUUID();
            var headTwinId = UUID.randomUUID();
            var headTwin = new TwinEntity();
            headTwin.setId(headTwinId);
            var props = buildProperties(roleId, false, "customSpaceId");
            var userSearch = new UserSearch();

            when(twinService.findHeadTwin(spaceId)).thenReturn(headTwin);

            finder.concatSearch(props, userSearch, Map.of("customSpaceId", spaceId.toString()));

            assertNotNull(userSearch.getSpaceList());
            var spaceSearch = userSearch.getSpaceList().get(0);
            assertEquals(headTwinId, spaceSearch.getSpaceId());
        }

        @Test
        void concatSearch_throwsWhenHeadTwinNotFound() {
            var roleId = UUID.randomUUID();
            var spaceId = UUID.randomUUID();
            var props = buildProperties(roleId, false);
            var userSearch = new UserSearch();

            when(twinService.findHeadTwin(spaceId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class, () ->
                    finder.concatSearch(props, userSearch, Map.of("spaceId", spaceId.toString())));

            assertEquals(ErrorCodeTwins.TWIN_ID_IS_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void concatSearch_throwsWhenRequiredParamMissing() {
            var roleId = UUID.randomUUID();
            var props = buildProperties(roleId, false);
            props.setProperty("required", "true");
            var userSearch = new UserSearch();

            var ex = assertThrows(ServiceException.class, () ->
                    finder.concatSearch(props, userSearch, Map.of()));

            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concatSearch_notRequiredAndParamMissing_throwsWhenHeadTwinNotFound() {
            var roleId = UUID.randomUUID();
            var props = buildProperties(roleId, false);
            props.setProperty("required", "false");
            var userSearch = new UserSearch();

            when(twinService.findHeadTwin(null)).thenReturn(null);

            var ex = assertThrows(ServiceException.class, () ->
                    finder.concatSearch(props, userSearch, Map.of()));

            assertEquals(ErrorCodeTwins.TWIN_ID_IS_INCORRECT.getCode(), ex.getErrorCode());
        }
    }
}
