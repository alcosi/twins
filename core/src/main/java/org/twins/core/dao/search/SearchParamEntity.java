

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
import org.twins.core.featurer.search.function.SearchFunction;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "search_param")
public class SearchParamEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "search_id")
    private UUID searchId;

    @Column(name = "key")
    private String key;

    @Column(name = "search_function_featurer_id")
    private UUID searchFunctionFeaturerId;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_id", insertable = false, updatable = false, nullable = false)
    private SearchEntity search;

    @FeaturerList(type = SearchFunction.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "search_function_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity searchFunctionFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "search_function_params", columnDefinition = "hstore")
    private HashMap<String, String> searchFunctionParams;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "searchParam[" + id + "]";
            case NORMAL:
                return "searchParam[id:" + id + ", searchId:" + searchId + "]";
            default:
                return "searchParam[id:" + id + ", searchId:" + searchId + ", key:" + key  + "]";
        }
    }
}

