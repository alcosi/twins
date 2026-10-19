package org.twins.core.featurer.user.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.params.FeaturerParamString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class UserFinderRequestedTest extends BaseUnitTest {

    private TestUserFinderRequested finder;

    @BeforeEach
    void setUp() {
        finder = new TestUserFinderRequested();
    }

    /**
     * Concrete test subclass to expose the protected getRequestedId method.
     */
    static class TestUserFinderRequested extends UserFinderRequested {
        @Override
        protected void concatSearch(Properties properties, org.twins.core.domain.search.UserSearch userSearch, Map<String, String> namedParamsMap) {
            // no-op for testing
        }

        public UUID callGetRequestedId(FeaturerParamString paramKey, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
            return getRequestedId(paramKey, properties, namedParamsMap);
        }
    }

    private static final FeaturerParamString testParamKey = new FeaturerParamString("testParamKey");

    @Nested
    class GetRequestedId {

        @Test
        void getRequestedId_returnsUuidWhenPresent() throws ServiceException {
            var expectedId = UUID.randomUUID();
            var props = new Properties();
            props.setProperty("testParamKey", "myKey");
            props.setProperty("required", "true");
            var namedParams = Map.of("myKey", expectedId.toString());

            var result = finder.callGetRequestedId(testParamKey, props, namedParams);

            assertEquals(expectedId, result);
        }

        @Test
        void getRequestedId_throwsWhenRequiredAndParamBlank() {
            var props = new Properties();
            props.setProperty("testParamKey", "myKey");
            props.setProperty("required", "true");
            var namedParams = Map.of("myKey", "");

            var ex = assertThrows(ServiceException.class, () ->
                    finder.callGetRequestedId(testParamKey, props, namedParams));

            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedId_throwsWhenRequiredAndParamMissing() {
            var props = new Properties();
            props.setProperty("testParamKey", "myKey");
            props.setProperty("required", "true");
            var namedParams = Map.of("otherKey", "some-value");

            var ex = assertThrows(ServiceException.class, () ->
                    finder.callGetRequestedId(testParamKey, props, namedParams));

            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedId_returnsNullWhenNotRequiredAndParamMissing() throws ServiceException {
            var props = new Properties();
            props.setProperty("testParamKey", "myKey");
            props.setProperty("required", "false");
            var namedParams = Collections.<String, String>emptyMap();

            var result = finder.callGetRequestedId(testParamKey, props, namedParams);

            assertNull(result);
        }

        @Test
        void getRequestedId_returnsNullWhenNotRequiredAndParamBlank() throws ServiceException {
            var props = new Properties();
            props.setProperty("testParamKey", "myKey");
            props.setProperty("required", "false");
            var namedParams = Map.of("myKey", "   ");

            var result = finder.callGetRequestedId(testParamKey, props, namedParams);

            assertNull(result);
        }

        @Test
        void getRequestedId_throwsWhenParamValueIsNotUuid() {
            var props = new Properties();
            props.setProperty("testParamKey", "myKey");
            props.setProperty("required", "true");
            var namedParams = Map.of("myKey", "not-a-valid-uuid");

            var ex = assertThrows(ServiceException.class, () ->
                    finder.callGetRequestedId(testParamKey, props, namedParams));

            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedId_handlesNullNamedParamsMapAsRequired() {
            var props = new Properties();
            props.setProperty("testParamKey", "myKey");
            props.setProperty("required", "true");

            var ex = assertThrows(ServiceException.class, () ->
                    finder.callGetRequestedId(testParamKey, props, null));

            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedId_handlesNullNamedParamsMapAsNotRequired() throws ServiceException {
            var props = new Properties();
            props.setProperty("testParamKey", "myKey");
            props.setProperty("required", "false");

            var result = finder.callGetRequestedId(testParamKey, props, null);

            assertNull(result);
        }
    }
}
