package org.twins.face.dto.rest.page.pg002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.face.dao.page.pg002.FacePG002Entity;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "FacePG002v1")
public class FacePG002DTOv1 extends FaceDTOv1 {
    @Schema(description = "page title")
    public String title;

    @Schema(description = "page layout")
    public FacePG002Entity.Layout layout;

    @Schema(description = "page widgets list")
    public List<FacePG002TabDTOv1> tabs;
}
