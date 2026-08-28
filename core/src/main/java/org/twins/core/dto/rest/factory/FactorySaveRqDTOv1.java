package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.HashMap;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactorySaveRqV1")
public class FactorySaveRqDTOv1 extends Request {
    @Schema(description = "key", example = DTOExamples.FACTORY_KEY)
    public String key;

    @Schema(description = "name i18n")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "description i18n")
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "Factory processor featurer ID. Drives a single factory run (multipliers, pipelines, branches, erasers, triggers). Defaults to the db-driven processor when null", example = "5401")
    public Integer factoryProcessorFeaturerId;

    @Schema(description = "Factory processor featurer parameters", example = "{}")
    public HashMap<String, String> factoryProcessorParams;
}
