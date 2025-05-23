package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "I18nTranslationListRsV1")
public class I18nTranslationListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "pagination")
    public PaginationDTOv1 pagination;

    @Schema(description = "result - i18n")
    public List<I18nTranslationDTOv1> translation;
}
