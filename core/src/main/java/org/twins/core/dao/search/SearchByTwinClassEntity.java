

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
@Table(name = "search_by_twin_class")
public class SearchByTwinClassEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "search_id")
    private UUID searchId;

    @Column(name = "exclude")
    private boolean exclude;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_id", insertable = false, updatable = false, nullable = false)
    private SearchEntity search;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "searchByTwinClass[" + id + "]";
            case NORMAL:
                return "searchByTwinClass[id:" + id + ", searchId:" + searchId + "]";
            default:
                return "searchByTwinClass[id:" + id + ", searchId:" + searchId + ", twinClassId:" + twinClassId  + "]";
        }
    }
}

