package org.twins.core.service.i18n;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.AdvancedEntityManager;
import org.twins.core.dao.i18n.I18nTranslationRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.domain.ApiUser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class I18nServiceTranslateToLocaleTest {

    @Mock
    private I18nTranslationRepository i18nTranslationRepository;
    @Mock
    private AuthService authService;

    @Mock
    private AdvancedEntityManager advancedEntityManager;

    @InjectMocks
    private I18nService i18nService;

    @Nested
    class TranslateToLocaleTests {
        @Test
        void testTranslateToLocale_EmptySet() {
            // Given
            Set<UUID> idsToLoad = Collections.emptySet();

            // When
            Map<UUID, String> result = i18nService.translateToLocale(idsToLoad);

            // Then
            assertTrue(result.isEmpty());
            verify(i18nTranslationRepository, never()).findByI18nIdInAndLocaleArray(any(), any());
        }

        @Test
        void testTranslateToLocale_LargeSet_DoesNotExceedParameterLimit() throws ServiceException {
            // Given - more than 65535 UUIDs to test the fix
            Set<UUID> largeIdSet = IntStream.range(0, 70000)
                    .mapToObj(i -> UUID.randomUUID())
                    .collect(Collectors.toSet());

            ApiUser apiUser = mock(ApiUser.class);
            when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(advancedEntityManager.buildPostgresUuidArrayLiteral(any())).thenReturn("{}");
            when(i18nTranslationRepository.findByI18nIdInAndLocaleArray(any(), any())).thenReturn(Collections.emptyList());

            // When - this should not throw "PreparedStatement can have at most 65.535 parameters"
            Map<UUID, String> result = i18nService.translateToLocale(largeIdSet);

            // Then
            assertNotNull(result);
            assertEquals(70000, result.size());
            verify(advancedEntityManager).buildPostgresUuidArrayLiteral(largeIdSet);
            verify(i18nTranslationRepository).findByI18nIdInAndLocaleArray(any(), any());
        }
    }
}
