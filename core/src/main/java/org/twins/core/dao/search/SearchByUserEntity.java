

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
@Table(name = "search_by_user")
public class SearchByUserEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "search_id")
    private UUID searchId;

    @Column(name = "search_field_id")
    @Convert(converter = SearchFieldConverter.class)
    private SearchField searchField;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_id", insertable = false, updatable = false, nullable = false)
    private SearchEntity search;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "search_param_id", insertable = false, updatable = false, nullable = false)
    private SearchParamEntity searchParam;

    public String easyLog(Level level)  {
        switch (level) {
            case SHORT:
                return "searchByUser[" + id + "]";
            case NORMAL:
                return "searchByUser[id:" + id + ", searchId:" + searchId + "]";
            default:
                return "searchByUser[id:" + id + ", searchId:" + searchId + ", userId:" + userId  + "]";
        }
    }
}

