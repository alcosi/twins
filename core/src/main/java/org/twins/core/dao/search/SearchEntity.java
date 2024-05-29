
package org.twins.core.dao.search;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "search")
public class SearchEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "search_alias_id")
    private String searchAliasId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "head_twin_search_id")
    private UUID headTwinSearchId;

    @OneToMany(fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_id", insertable = false, updatable = false, nullable = true)
    private List<SearchPredicateEntity> searchPredicateList;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "search[" + id + "]";
            default:
                return "search[id:" + id + ", alias:" + searchAliasId + "]";
        }

    }
}

