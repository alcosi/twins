package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinFieldSearchTextV1")
public class TwinFieldSearchTextDTOv1 extends TwinFieldSearchDTOv1 {

    public static final String KEY = "searchTextValueV1";
    public String type = KEY;

    @Schema(description = "Include like given strings. AND join. Add % symbols manual to use LIKE features.")
    public Set<String> valueLikeAllOfList;

    @Schema(description = "Include like given strings. OR join. Add % symbols manual to use LIKE features.")
    public Set<String> valueLikeAnyOfList;

    @Schema(description = "Exclude like given strings. AND join. Add % symbols manual to use LIKE features.")
    public Set<String> valueLikeNoAllOfList;

    @Schema(description = "Exclude like given strings. OR join. Add % symbols manual to use LIKE features.")
    public Set<String> valueLikeNoAnyOfList;
}
