package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.Set;
import java.util.UUID;

import static org.twins.core.dto.rest.twin.TwinFieldSearchListDTOv1.KEY;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(fluent = true)
@Schema(name = KEY)
public class TwinFieldSearchListDTOv1 extends TwinFieldSearchDTOv1 {

    public static final String KEY = "searchListOptionsV1";

    @JsonProperty("type")
    public String type = KEY;

    @Schema(description = "Include options with given ids. AND join")
    public Set<UUID> optionsAllOfList;

    @Schema(description = "Include options with given ids. OR join")
    public Set<UUID> optionsAnyOfList;

    @Schema(description = "Exclude options with given ids. AND join")
    public Set<UUID> optionsNoAllOfList;

    @Schema(description = "Exclude options with given ids. OR join")
    public Set<UUID> optionsNoAnyOfList;
}
