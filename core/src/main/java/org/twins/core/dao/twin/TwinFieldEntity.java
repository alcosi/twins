package org.twins.core.dao.twin;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_field")
public class TwinFieldEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twinByTwinId;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassFieldByTwinClassFieldId;
}
