package org.twins.core.dto.rest.i18n;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "I18nTranslationUpdateV1")
public class I18nTranslationUpdateDTOv1 extends I18nTranslationSaveDTOv1 {
    @JsonIgnore
    public UUID i18nId;
}
