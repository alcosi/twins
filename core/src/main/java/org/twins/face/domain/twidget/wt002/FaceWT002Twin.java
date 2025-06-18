package org.twins.face.domain.twidget.wt002;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.face.dao.widget.wt002.FaceWT002Entity;

import java.util.UUID;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class FaceWT002Twin {
    private FaceWT002Entity entity;
    private UUID currentTwinId;
}
