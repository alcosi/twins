package org.twins.core.dao.notification.email;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.email.EmailSenderEntity;
import org.twins.core.dao.event.EventEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.template.generator.TemplateGeneratorEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "notification_email")
@DomainSetting
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class NotificationEmailEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "email_sender_id")
    private UUID emailSenderId;

    @Column(name = "subject_i18n_id")
    private UUID subjectI18nId;

    @Column(name = "subject_template_generator_id")
    private UUID subjectTemplateGeneratorId;

    @Column(name = "body_i18n_id")
    private UUID bodyI18nId;

    @Column(name = "body_template_generator_id")
    private UUID bodyTemplateGeneratorId;

    @Column(name = "active")
    private boolean active;

    @Column(name = "notification_mode_id")
    @Enumerated(EnumType.STRING)
    private NotificationMode notificationMode;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private EventEntity event;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_sender_id", insertable = false, updatable = false)
    private EmailSenderEntity emailSender;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_template_generator_id", insertable = false, updatable = false)
    private TemplateGeneratorEntity subjectTemplateGenerator;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_template_generator_id", insertable = false, updatable = false)
    private TemplateGeneratorEntity bodyTemplateGenerator;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_i18n_id", insertable = false, updatable = false)
    private I18nEntity subjectI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_i18n_id", insertable = false, updatable = false)
    @JoinColumn(name = "body_i18n_id", insertable = false, updatable = false)
    private I18nEntity bodyI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    public String easyLog(Level level) {
        return "notificationEmail[id:" + id + "]";
    }
}
