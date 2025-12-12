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
@Table(name = "twin_class_search_predicate")
public class TwinClassSearchPredicateEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "twin_class_search_id")
    private UUID twinClassSearchId;

    @Column(name = "class_finder_featurer_id")
    private Integer classFinderFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "class_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> classFinderParams;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "twin_class_search_id", insertable = false, updatable = false, nullable = false)
    private TwinClassSearchEntity twinClassSearch;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinClassSearchPredicate[" + id + "]";
            case NORMAL:
                return "twinClassSearchPredicate[id:" + id + ", searchId:" + twinClassSearchId + "]";
            default:
                return "twinClassSearchPredicate[id:" + id + ", searchId:" + twinClassSearchId + ", featurerId:" + classFinderFeaturerId + "]";
        }
    }
}
