package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "twin_action")
public class TwinActionEntity {

    @Id
    @Column(name = "id")
    private TwinAction id;

    @Column(name = "i18n_id")
    private UUID i18nId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", insertable = false, updatable = false)
    private I18nEntity i18nEntity;
}
