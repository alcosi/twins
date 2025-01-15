package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryCreateRqV1")
public class FactoryCreateRqDTOv1 extends FactorySaveRqDTOv1 {
}
