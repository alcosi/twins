package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Map;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinSearchBatchCoooRqV1")
public class TwinSearchBatchRqDTOv1 extends Request {
    @Schema(description = "Map of { frontendId / TwinSearchRqV1 }", example = """
            {
                "id1": { "twinClassIdList": [ "ab750e98-70dd-404e-8164-4e0daa187164" ] },
                "id2": { "twinClassIdList": [ "ab750e98-70dd-404e-8164-4e0daa187164" ] }
            }
            """)
    public Map<String, TwinSearchRqDTOv1> searchMap;
}
