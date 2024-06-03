

package org.twins.core.dao.search;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilder;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "search_predicate")
public class SearchPredicateEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "search_id")
    private UUID searchId;

    @Column(name = "search_field_id")
    @Convert(converter = SearchFieldConverter.class)
    private SearchField searchField;

    @Column(name = "exclude")
    private boolean exclude;

    @Column(name = "search_criteria_builder_featurer_id")
    private int searchFunctionFeaturerId;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_id", insertable = false, updatable = false, nullable = false)
    private SearchEntity search;

    @FeaturerList(type = SearchCriteriaBuilder.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "search_criteria_builder_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity searchCriteriaBuilderFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "search_criteria_builder_params", columnDefinition = "hstore")
    private HashMap<String, String> searchFunctionParams;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "searchPredicate[" + id + "]";
            case NORMAL:
                return "searchPredicate[id:" + id + ", searchId:" + searchId + "]";
            default:
                return "searchPredicate[id:" + id + ", searchId:" + searchId + ", field:" + searchField  + "]";
        }
    }
}

