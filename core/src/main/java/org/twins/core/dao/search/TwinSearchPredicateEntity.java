

package org.twins.core.dao.search;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_search_predicate")
public class TwinSearchPredicateEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "twin_search_id")
    private UUID twinSearchId;

    @Column(name = "description")
    private String description;

    @Column(name = "twin_finder_featurer_id", insertable = false, updatable = false)
    private int twinFinderFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> twinFinderParams;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_id", insertable = false, updatable = false, nullable = false)
    private TwinSearchEntity search;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "twinSearchPredicate[" + id + "]";
            default:
                return "twinSearchPredicate[id:" + id + ", twinSearchId:" + twinSearchId + "]";
        }
    }
}

