package org.cambium.featurer.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Entity
@Data
@Table(name = "featurer")
@FieldNameConstants
public class FeaturerEntity {
    @Id
    @Column(name = "id")
    private int id;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "featurer_type_id", insertable = false, updatable = false)
//    private FeaturerTypeEntity featurerType;

    @Basic
    @Column(name = "featurer_type_id")
    private int featurerTypeId;

    @Basic
    @Column(name = "class")
    private String clazz;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "description")
    private String description;

    @Transient
    @ToString.Exclude
    private List<FeaturerParamEntity> params;
}