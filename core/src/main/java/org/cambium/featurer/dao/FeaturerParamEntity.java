package org.cambium.featurer.dao;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Entity
@Data
@Table(name = "featurer_param")
@IdClass(FeaturerParamEntity.PK.class)
public class FeaturerParamEntity {
    @Id
    @Column(name = "featurer_id")
    private Integer featurerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "featurer_id", insertable = false, updatable = false)
    private FeaturerEntity featurer;

    @Id
    @Column(name = "key")
    private String key;

    @Column(name = "featurer_param_type_id")
    @Basic
    private String featurerParamTypeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "featurer_param_type_id", insertable = false, updatable = false)
    private FeaturerParamTypeEntity featurerParamType;

    @Basic
    @Column(name = "injectable")
    private boolean injectable;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "name")
    @Basic
    private String name;

    @Column(name = "description")
    @Basic
    private String description;

    @Column(name = "is_optional")
    @Basic
    public Boolean optional;

    @Column(name = "default_value")
    @Basic
    public String defaultValue;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "example_values", columnDefinition = "varchar[]")
    public String[] exampleValues;

    @Data
    protected static class PK implements Serializable {
        @Column(name = "featurer_id")
        private int featurerId;

        @Column(name = "key")
        private String key;
    }
}
