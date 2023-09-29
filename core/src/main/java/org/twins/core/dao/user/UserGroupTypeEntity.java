package org.twins.core.dao.user;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.usergroup.slugger.Slugger;

import java.util.HashMap;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_group_type")
public class UserGroupTypeEntity {
    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "slugger_featurer_id")
    private int sluggerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "slugger_params", columnDefinition = "hstore")
    private HashMap<String, String> sluggerParams;

    @FeaturerList(type = Slugger.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slugger_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity sluggerFeaturer;



}
