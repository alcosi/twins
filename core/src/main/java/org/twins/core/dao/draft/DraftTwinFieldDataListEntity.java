package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.CUD;
import org.twins.core.dao.CUDConverter;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "draft_twin_field_data_list")
public class DraftTwinFieldDataListEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "id")
    private UUID id;

    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "time_in_millis")
    private long timeInMillis;

    @Column(name = "cud_id")
    @Convert(converter = CUDConverter.class)
    private CUD cud;

    @Column(name = "twin_field_data_list_id")
    private UUID twinFieldDataListId;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "data_list_option_id")
    private UUID dataListOptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id")
    private DraftEntity draft;

}