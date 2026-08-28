package org.twins.core.unit.service.i18n;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.AdvancedEntityManager;
import org.twins.core.dao.i18n.I18nTranslationRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class I18nServiceTranslateToLocaleTest extends BaseUnitTest {

    @Mock private I18nTranslationRepository i18nTranslationRepository;
    @Mock private AuthService authService;
    @Mock private AdvancedEntityManager advancedEntityManager;
    @Mock private ApiUser apiUser;

    @InjectMocks private I18nService i18nService;

    @Test
    void translateToLocale_emptySet_returnsEmptyAndSkipsQuery() {
        var result = i18nService.translateToLocale(Collections.emptySet());

        assertTrue(result.isEmpty());
        verify(i18nTranslationRepository, never()).findByI18nIdInAndLocaleArray(any(), any());
    }

    @Test
    void translateToLocale_largeSet_doesNotExceedParameterLimit() throws ServiceException {
        Set<UUID> largeIdSet = IntStream.range(0, 70000)
                .mapToObj(i -> UUID.randomUUID())
                .collect(Collectors.toSet());

        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(authService.getApiUser()).thenReturn(apiUser);
        when(advancedEntityManager.buildPostgresUuidArrayLiteral(any())).thenReturn("{}");
        when(i18nTranslationRepository.findByI18nIdInAndLocaleArray(any(), any()))
                .thenReturn(Collections.emptyList());

        var result = i18nService.translateToLocale(largeIdSet);

        assertEquals(70000, result.size());
        verify(advancedEntityManager).buildPostgresUuidArrayLiteral(largeIdSet);
        verify(i18nTranslationRepository).findByI18nIdInAndLocaleArray(any(), any());
    }
}
