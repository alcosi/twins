package org.twins.core.dto.rest.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentBaseDTOv1")
public class CommentBaseDTOv1 extends Request {
    @Schema(name = "text")
    public String text;

    @JsonIgnore
    public UUID twinId;
}
