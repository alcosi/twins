package org.cambium.featurer.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.domain.Identifiable;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "featurer_type")
@FieldNameConstants
public class FeaturerTypeEntity implements Identifiable<Integer> {
    @Id
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "description")
    private String description;
}