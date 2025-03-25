package org.twins.core.dto.rest.transition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.twins.core.dto.rest.history.context.HistoryContextUserDTOv1;
import org.twins.core.dto.rest.history.context.HistoryContextUserMultiDTOv1;

//not used in jackson serialization(reverse mappers)
//be sure that polymorph classes do not has the same-named fields.
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TwinTransitionPerformResultMinorDTOv1.class, name = TwinTransitionPerformResultMinorDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinTransitionPerformResultMajorDTOv1.class, name = TwinTransitionPerformResultMajorDTOv1.KEY)
})
@Schema(description = "On of values", example = "", discriminatorProperty = TwinTransitionPerformResultDTO.DISCRIMINATOR, discriminatorMapping = {
        @DiscriminatorMapping(value = HistoryContextUserDTOv1.KEY, schema = TwinTransitionPerformResultMinorDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextUserMultiDTOv1.KEY, schema = TwinTransitionPerformResultMajorDTOv1.class)
})
public interface TwinTransitionPerformResultDTO {
    String DISCRIMINATOR = "resultType";
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED, examples = {
            TwinTransitionPerformResultMinorDTOv1.KEY,
            TwinTransitionPerformResultMajorDTOv1.KEY
    })
    String getResultType();
}
