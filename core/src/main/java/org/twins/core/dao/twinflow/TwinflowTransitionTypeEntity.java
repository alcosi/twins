package org.twins.core.dao.twinflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_transition_type")
@FieldNameConstants
public class TwinflowTransitionTypeEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "description")
    private String description;
}
