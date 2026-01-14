package org.twins.core.dao.draft;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;
import org.twins.core.dao.CUD;
import org.twins.core.dao.CUDConverter;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "draft_twin_link")
public class DraftTwinLinkEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "time_in_millis")
    private long timeInMillis;

    @Column(name = "cud_id")
    @Convert(converter = CUDConverter.class)
    private CUD cud;

    @Column(name = "twin_link_id")
    private UUID twinLinkId;

    //we can not create @ManyToOne relation, because it can be new twin here, which is not in twin table yet
    @Column(name = "src_twin_id")
    private UUID srcTwinId;

    //we can not create @ManyToOne relation, because it can be new twin here, which is not in twin table yet
    @Column(name = "dst_twin_id")
    private UUID dstTwinId;

    @Column(name = "link_id")
    private UUID linkId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "draft_id", insertable = false, updatable = false)
    private DraftEntity draft;
}