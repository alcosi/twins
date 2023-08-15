package org.twins.core.dao.view;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.ChannelEntity;

import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "view_tab_layout")
public class ViewTabLayoutEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "key")
    private String key;

    @ManyToOne
    @JoinColumn(name = "channel_id", insertable = false, updatable = false, nullable = false)
    private ChannelEntity channel;
}
