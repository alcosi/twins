package org.twins.face.dao.twidget.tw002;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.LocaleConverter;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.Locale;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Entity
@DomainSetting
@Table(name = "face_tw002_accordion_item")
public class FaceTW002AccordionItemEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_tw002_id")
    private UUID faceTW002Id;

    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_tw002_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceTW002Entity faceTW002;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;
}