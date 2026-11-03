package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderBySpaceRoleUserRequestedTest extends BaseUnitTest {

    private static final UUID USER_CLASS_ID = UUID.fromString("00000000-0000-0000-0001-000000000001");

    private final SpaceRoleUserRepository spaceRoleUserRepository = Mockito.mock(SpaceRoleUserRepository.class);
    private final TwinRepository twinRepository = Mockito.mock(TwinRepository.class);
    private final TwinFinderBySpaceRoleUserRequested finder = new TwinFinderBySpaceRoleUserRequested();

    private TwinFinderBySpaceRoleUserRequested finderWithRepository() {
        ReflectionTestUtils.setField(finder, "spaceRoleUserRepository", spaceRoleUserRepository);
        ReflectionTestUtils.setField(finder, "twinRepository", twinRepository);
        return finder;
    }

    @Nested
    class Concat {

        @Test
        void concat_spaceMembers_addsUserIdsAsTwinIds() throws ServiceException {
            var spaceTwinId = UUID.randomUUID();
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();
            Mockito.when(spaceRoleUserRepository.findUserIdsByTwinId(spaceTwinId))
                    .thenReturn(Set.of(userId1, userId2));

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", spaceTwinId.toString());

            finderWithRepository().concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinIdList());
            assertEquals(2, twinSearch.getTwinIdList().size());
            assertTrue(twinSearch.getTwinIdList().contains(userId1));
            assertTrue(twinSearch.getTwinIdList().contains(userId2));
        }

        @Test
        void concat_emptyTeam_addsNullifyMarkerInsteadOfAllTwins() throws ServiceException {
            var spaceTwinId = UUID.randomUUID();
            Mockito.when(spaceRoleUserRepository.findUserIdsByTwinId(spaceTwinId))
                    .thenReturn(Set.of());

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", spaceTwinId.toString());

            finderWithRepository().concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinIdList());
            assertTrue(twinSearch.getTwinIdList().contains(UuidUtils.NULLIFY_MARKER));
        }

        @Test
        void concat_twinClassIdsConfigured_addsClassToSearch() throws ServiceException {
            var spaceTwinId = UUID.randomUUID();
            var memberId = UUID.randomUUID();
            Mockito.when(spaceRoleUserRepository.findUserIdsByTwinId(spaceTwinId))
                    .thenReturn(Set.of(memberId));

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("twinClassIds", USER_CLASS_ID.toString());
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", spaceTwinId.toString());

            finderWithRepository().concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinClassIdList());
            assertTrue(twinSearch.getTwinClassIdList().contains(USER_CLASS_ID));
            assertTrue(twinSearch.getTwinIdList().contains(memberId));
        }

        @Test
        void concat_nonUserClass_resolvesOwnerUserTwins() throws ServiceException {
            var spaceTwinId = UUID.randomUUID();
            var domainUserClassId = UUID.randomUUID();
            var memberId = UUID.randomUUID();
            var memberTwinId = UUID.randomUUID();
            Mockito.when(spaceRoleUserRepository.findUserIdsByTwinId(spaceTwinId))
                    .thenReturn(Set.of(memberId));
            Mockito.when(twinRepository.findIdsByOwnerIdInAndTwinClassIdIn(Set.of(memberId), Set.of(domainUserClassId)))
                    .thenReturn(Set.of(memberTwinId));

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("twinClassIds", domainUserClassId.toString());
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", spaceTwinId.toString());

            finderWithRepository().concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinClassIdList());
            assertTrue(twinSearch.getTwinClassIdList().contains(domainUserClassId));
            assertTrue(twinSearch.getTwinIdList().contains(memberTwinId));
            assertFalse(twinSearch.getTwinIdList().contains(memberId));
        }

        @Test
        void concat_nonUserClass_noOwnerTwins_addsNullifyMarker() throws ServiceException {
            var spaceTwinId = UUID.randomUUID();
            var domainUserClassId = UUID.randomUUID();
            var memberId = UUID.randomUUID();
            Mockito.when(spaceRoleUserRepository.findUserIdsByTwinId(spaceTwinId))
                    .thenReturn(Set.of(memberId));
            Mockito.when(twinRepository.findIdsByOwnerIdInAndTwinClassIdIn(Set.of(memberId), Set.of(domainUserClassId)))
                    .thenReturn(Set.of());

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("twinClassIds", domainUserClassId.toString());
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", spaceTwinId.toString());

            finderWithRepository().concat(twinSearch, properties, namedParams);

            assertTrue(twinSearch.getTwinIdList().contains(UuidUtils.NULLIFY_MARKER));
        }

        @Test
        void concat_paramMissedAndNotRequired_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("required", "false");
            var twinSearch = new TwinSearch();

            finderWithRepository().concat(twinSearch, properties, Map.of());

            assertNull(twinSearch.getTwinIdList());
        }

        @Test
        void concat_paramMissedAndRequired_throwsException() {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("required", "true");
            var twinSearch = new TwinSearch();

            var ex = assertThrows(ServiceException.class,
                    () -> finderWithRepository().concat(twinSearch, properties, Map.of()));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concat_invalidUuid_throwsException() {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", "not-a-uuid");

            var ex = assertThrows(ServiceException.class,
                    () -> finderWithRepository().concat(twinSearch, properties, namedParams));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }
    }
}
