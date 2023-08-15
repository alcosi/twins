package org.cambium.featurer.dao;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Data
@Table(name = "featurer_param_type")
public class FeaturerParamTypeEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "regexp")
    @Basic
    private String regexp;

    @Column(name = "description")
    @Basic
    private String description;

    @Column(name = "example")
    @Basic
    private String example;
}
