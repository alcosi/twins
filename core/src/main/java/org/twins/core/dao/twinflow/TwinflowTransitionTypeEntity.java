package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.enums.twinflow.TwinflowTransitionType;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_transition_type")
@FieldNameConstants
public class TwinflowTransitionTypeEntity {
    @Id
    @Column(name = "id")
    @Enumerated(EnumType.STRING)
    private TwinflowTransitionType id;

    @Column(name = "description")
    private String description;
}
