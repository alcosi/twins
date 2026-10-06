package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryUpdateV1")
public class FactoryUpdateDTOv1 {
    @NotNull
    @Schema(description = "factory id", example = DTOExamples.FACTORY_ID)
    public UUID id;

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
