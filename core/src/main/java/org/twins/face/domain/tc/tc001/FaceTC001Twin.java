package org.twins.face.domain.tc.tc001;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.face.dao.tc.tc001.FaceTC001Entity;

import java.util.UUID;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class FaceTC001Twin {
    private FaceTC001Entity entity;
    private UUID twinId;
}
