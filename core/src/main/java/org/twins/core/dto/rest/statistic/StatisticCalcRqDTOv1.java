package org.twins.core.dto.rest.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "StatisticCalcRqV1")
public class StatisticCalcRqDTOv1 extends Request {
    @Schema(description = "Twin id set")
    public Set<UUID> twinIdSet;
}
