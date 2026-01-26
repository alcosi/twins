package org.twins.core.dao.twinclass;

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
@Table(name = "twin_class_field_search_predicate")
public class TwinClassFieldSearchPredicateEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "twin_class_field_search_id")
    private UUID twinClassFieldSearchId;

    @Column(name = "field_finder_featurer_id")
    private Integer fieldFinderFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldFinderParams;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "twin_class_field_search_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldSearchEntity twinClassFieldSearch;


    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinClassFieldSearchPredicate[" + id + "]";
            case NORMAL:
                return "twinClassFieldSearchPredicate[id:" + id + ", searchId:" + twinClassFieldSearchId + "]";
            default:
                return "twinClassFieldSearchPredicate[id:" + id + ", searchId:" + twinClassFieldSearchId + ", featurerId:" + fieldFinderFeaturerId + "]";
        }
    }
}
