package org.twins.core.dao.user;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;

import java.util.HashMap;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "user_group_type")
public class UserGroupTypeEntity {
    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "slugger_featurer_id")
    private Integer sluggerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "slugger_params", columnDefinition = "hstore")
    private HashMap<String, String> sluggerParams;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity sluggerFeaturer;

}
