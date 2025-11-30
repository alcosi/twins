package org.twins.core.dao.projection;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "projection_type_group")
public class ProjectionTypeGroupEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "key")
    private String key;
}
