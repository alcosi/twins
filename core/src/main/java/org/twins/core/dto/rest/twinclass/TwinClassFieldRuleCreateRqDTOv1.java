package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

/**
 * Request wrapper for creating a new field–dependency rule together with its conditions.
 * <p>
 * Mirrors the commonly used *CreateRq* pattern across the code-base where the request
 * contains one top-level object that describes what should be created.
 * </p>
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldRuleCreateRqV1")
public class TwinClassFieldRuleCreateRqDTOv1 extends Request {

    @Schema(description = "rules that should be created")
    public List<TwinClassFieldRuleCreateDTOv1> rules;
}
