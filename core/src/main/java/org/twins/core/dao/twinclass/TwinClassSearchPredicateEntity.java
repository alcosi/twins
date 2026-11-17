package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;
import org.twins.core.dao.annotation.ConfiguredSearchPredicate;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_search_predicate")
@DataScope({DataScopeType.SYSTEM, DataScopeType.DOMAIN_MEMBER})
public class TwinClassSearchPredicateEntity implements EasyLoggable, ConfiguredSearchPredicate {
    @Id
    private UUID id;

    @Column(name = "twin_class_search_id")
    private UUID twinClassSearchId;

    @Column(name = "class_finder_featurer_id")
    private Integer classFinderFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "class_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> classFinderParams;

    @Override
    public Integer getFinderFeaturerId() {
        return classFinderFeaturerId;
    }

    @Override
    public HashMap<String, String> getFinderParams() {
        return classFinderParams;
    }

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "twin_class_search_id", insertable = false, updatable = false, nullable = false)
    private TwinClassSearchEntity twinClassSearchSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
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
