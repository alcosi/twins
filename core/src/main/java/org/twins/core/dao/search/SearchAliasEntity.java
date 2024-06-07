
package org.twins.core.dao.search;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
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
@Table(name = "search_alias")
public class SearchAliasEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "alias")
    private String alias;

    @Column(name = "search_detector_featurer_id")
    private int searchDetectorFeaturerId;

    @FeaturerList(type = SearchCriteriaBuilder.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "search_detector_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity searchDetectorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "search_detector_params", columnDefinition = "hstore")
    private HashMap<String, String> searchDetectorParams;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "searchAlias[" + id + "]";
            default:
                return "searchAlias[id:" + id + ", domainId:" + domainId + ", alias:" + alias + "]";
        }

    }
}

