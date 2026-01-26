
package org.twins.core.dao.search;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_search_alias")
public class TwinSearchAliasEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "alias")
    private String alias;

    @Column(name = "twin_search_detector_featurer_id")
    private Integer twinSearchDetectorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_search_detector_params", columnDefinition = "hstore")
    private HashMap<String, String> twinSearchDetectorParams;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "twinSearchAlias[" + id + "]";
            default:
                return "twinSearchAlias[id:" + id + ", domainId:" + domainId + ", alias:" + alias + "]";
        }

    }
}

