
package org.twins.core.dao.search;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_search")
public class TwinSearchEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "twin_search_alias_id")
    private UUID twinSearchAliasId;

    @Column(name = "force_sorting")
    private boolean forceSorting;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "head_twin_search_id")
    private UUID headTwinSearchId;

    @Column(name = "twin_search_featurer_id")
    private Integer twinSearchFeaturerId = FeaturerTwins.ID_5701;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_search_featurer_params", columnDefinition = "hstore")
    private HashMap<String, String> twinSearchFeaturerParams;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinSearchPredicateEntity, UUID> searchPredicateKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinSearchSortEntity,UUID> sortKit;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "twinSearch[" + id + "]";
            default:
                return "twinSearch[id:" + id + ", alias:" + twinSearchAliasId + "]";
        }

    }
}

