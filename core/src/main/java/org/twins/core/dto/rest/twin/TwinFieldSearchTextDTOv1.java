package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;


@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldSearchTextDTOv1.KEY)
public class TwinFieldSearchTextDTOv1 implements TwinFieldSearchDTOv1 {
    public static final String KEY = "TwinFieldSearchTextV1";
    @Override
    public String type() {
        return KEY;
    }

    @Schema(description = "Include like given strings. AND join. Add % symbols manual to use LIKE features.")
    public Set<String> valueLikeAllOfList;

    @Schema(description = "Include like given strings. OR join. Add % symbols manual to use LIKE features.")
    public Set<String> valueLikeAnyOfList;

    @Schema(description = "Exclude like given strings. AND join. Add % symbols manual to use LIKE features.")
    public Set<String> valueLikeNoAllOfList;

    @Schema(description = "Exclude like given strings. OR join. Add % symbols manual to use LIKE features.")
    public Set<String> valueLikeNoAnyOfList;
}
