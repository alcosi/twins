package org.twins.core.dao.datalist;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list_option_search_predicate")
@FieldNameConstants
public class DataListOptionSearchPredicateEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "data_list_option_search_id")
    private UUID dataListOptionSearchId;

    @Column(name = "field_finder_featurer_id")
    private Integer optionFinderFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> optionFinderParams;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "data_list_option_search_id", insertable = false, updatable = false, nullable = false)
    private DataListOptionSearchEntity dataListOptionSearch;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "dataListOptionSearchPredicateEntity[" + id + "]";
            case NORMAL:
                return "dataListOptionSearchPredicateEntity[id" + id + ", searchId:" + dataListOptionSearchId + "]";
            default:
                return "dataListOptionSearchPredicateEntity[id" + id + ", searchId:" + dataListOptionSearchId + ", featurerId:" + optionFinderFeaturerId + "]";
        }
    }
}
