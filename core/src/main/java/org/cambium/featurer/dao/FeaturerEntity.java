package org.cambium.featurer.dao;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.kit.Kit;
import org.twins.core.domain.Identifiable;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "featurer")
@FieldNameConstants
public class FeaturerEntity implements Identifiable<Integer> {
    @Id
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "featurer_type_id")
    private int featurerTypeId;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "featurer_type_id", insertable = false, updatable = false)
    private FeaturerTypeEntity featurerTypeSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerTypeEntity featurerType;

    @Basic
    @Column(name = "class")
    private String clazz;

    @Basic
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "deprecated")
    private boolean deprecated;

    @Transient
    @ToString.Exclude
    private Kit<FeaturerParamEntity, String> params;
}