

package org.twins.core.dao.search;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "search_by_twin")
public class SearchByTwinEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "search_id")
    private UUID searchId;

    @Column(name = "exclude")
    private boolean exclude;

    @Column(name = "twin_id")
    private UUID twinId;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_id", insertable = false, updatable = false, nullable = false)
    private SearchEntity search;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "searchByTwin[" + id + "]";
            case NORMAL:
                return "searchByTwin[id:" + id + ", searchId:" + searchId + "]";
            default:
                return "searchByTwin[id:" + id + ", searchId:" + searchId + ", twinId:" + twinId  + "]";
        }
    }
}

