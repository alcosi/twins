package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dto.rest.i18n.I18nDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = I18nMode.class)
public class I18nRestDTOMapper  extends RestSimpleDTOMapper<I18nEntity, I18nDTOv1> {
}
