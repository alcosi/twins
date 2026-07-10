package org.twins.core.featurer.user.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class UserFinderBySpaceIdAndRoleIdTest extends BaseUnitTest {

    private UserFinderBySpaceIdAndRoleId finder;

    @BeforeEach
    void setUp() {
        finder = new UserFinderBySpaceIdAndRoleId();
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
            var props = buildProperties(roleId, false);
            var userSearch = new UserSearch();

            finder.concatSearch(props, userSearch, Map.of("spaceId", spaceId.toString()));

            assertNotNull(userSearch.getSpaceList());
            assertEquals(1, userSearch.getSpaceList().size());
            assertNull(userSearch.getSpaceExcludeList());

            var spaceSearch = userSearch.getSpaceList().get(0);
            assertEquals(roleId, spaceSearch.getRoleId());
            assertEquals(spaceId, spaceSearch.getSpaceId());
        }

        @Test
        void concatSearch_addsSpaceToExcludeListWhenExcludeTrue() throws ServiceException {
            var roleId = UUID.randomUUID();
            var spaceId = UUID.randomUUID();
            var props = buildProperties(roleId, true);
            var userSearch = new UserSearch();

            finder.concatSearch(props, userSearch, Map.of("spaceId", spaceId.toString()));

            assertNotNull(userSearch.getSpaceExcludeList());
            assertEquals(1, userSearch.getSpaceExcludeList().size());
            assertNull(userSearch.getSpaceList());

            var spaceSearch = userSearch.getSpaceExcludeList().get(0);
            assertEquals(roleId, spaceSearch.getRoleId());
            assertEquals(spaceId, spaceSearch.getSpaceId());
        }

        @Test
        void concatSearch_usesCustomParamKey() throws ServiceException {
            var roleId = UUID.randomUUID();
            var spaceId = UUID.randomUUID();
            var props = buildProperties(roleId, false, "customSpaceId");
            var userSearch = new UserSearch();

            finder.concatSearch(props, userSearch, Map.of("customSpaceId", spaceId.toString()));

            assertNotNull(userSearch.getSpaceList());
            var spaceSearch = userSearch.getSpaceList().get(0);
            assertEquals(spaceId, spaceSearch.getSpaceId());
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
        void concatSearch_notRequiredAndParamMissing_addsSearchWithNullSpaceId() throws ServiceException {
            var roleId = UUID.randomUUID();
            var props = buildProperties(roleId, false);
            props.setProperty("required", "false");
            var userSearch = new UserSearch();

            finder.concatSearch(props, userSearch, Map.of());

            assertNotNull(userSearch.getSpaceList());
            assertEquals(1, userSearch.getSpaceList().size());
            assertNull(userSearch.getSpaceExcludeList());

            var spaceSearch = userSearch.getSpaceList().get(0);
            assertEquals(roleId, spaceSearch.getRoleId());
            assertNull(spaceSearch.getSpaceId());
        }

        @Test
        void concatSearch_throwsWhenParamValueIsNotUuid() {
            var roleId = UUID.randomUUID();
            var props = buildProperties(roleId, false);
            props.setProperty("required", "true");
            var userSearch = new UserSearch();

            var ex = assertThrows(ServiceException.class, () ->
                    finder.concatSearch(props, userSearch, Map.of("spaceId", "not-a-uuid")));

            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void concatSearch_nullNamedParamsMap_notRequired_addsSearchWithNullSpaceId() throws ServiceException {
            var roleId = UUID.randomUUID();
            var props = buildProperties(roleId, false);
            props.setProperty("required", "false");
            var userSearch = new UserSearch();

            finder.concatSearch(props, userSearch, null);

            assertNotNull(userSearch.getSpaceList());
            assertEquals(1, userSearch.getSpaceList().size());
            assertNull(userSearch.getSpaceExcludeList());

            var spaceSearch = userSearch.getSpaceList().get(0);
            assertEquals(roleId, spaceSearch.getRoleId());
            assertNull(spaceSearch.getSpaceId());
        }

        @Test
        void concatSearch_appendsToExistingSpaces() throws ServiceException {
            var roleId1 = UUID.randomUUID();
            var roleId2 = UUID.randomUUID();
            var spaceId1 = UUID.randomUUID();
            var spaceId2 = UUID.randomUUID();
            var userSearch = new UserSearch();

            var props1 = buildProperties(roleId1, false);
            finder.concatSearch(props1, userSearch, Map.of("spaceId", spaceId1.toString()));

            var props2 = buildProperties(roleId2, false);
            finder.concatSearch(props2, userSearch, Map.of("spaceId", spaceId2.toString()));

            assertEquals(2, userSearch.getSpaceList().size());
        }
    }
}
