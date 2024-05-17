


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
@Table(name = "search_by_link")
public class SearchByLinkEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "search_id")
    private UUID searchId;

    @Column(name = "link_id")
    private UUID linkId;

    @Column(name = "dst_twin_id")
    private UUID dstTwinId;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_id", insertable = false, updatable = false, nullable = false)
    private SearchEntity search;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "searchByLink[" + id + "]";
            case NORMAL:
                return "searchByLink[id:" + id + ", searchId:" + searchId + "]";
            default:
                return "searchByLink[id:" + id + ", searchId:" + searchId + ", linkId:" + linkId + ", dstTwinId:" + dstTwinId + "]";
        }
    }
}

