package org.cambium.featurer.dao;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Data
@Table(name = "featurer_type")
public class FeaturerTypeEntity {
    @Id
    @Column(name = "id")
    private int id;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "description")
    private String description;
}