package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinBasicFieldsV1")
public class TwinBasicFieldsDTOv1 {

    @Schema(description = "assignee user id", example = DTOExamples.USER_ID)
    public UUID assigneeUserId;

    @Schema(description = "created by user_id", example = DTOExamples.USER_ID)
    public UUID createdByUserId;

    @Column(name = "name")
    public String name;

    @Column(name = "description")
    public String description;

}
