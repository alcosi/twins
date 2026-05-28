package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "I18nTranslationSearchRsV1")
public class I18nTranslationSearchRsDTOv1 extends I18nTranslationListRsDTOv1 {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;
}
