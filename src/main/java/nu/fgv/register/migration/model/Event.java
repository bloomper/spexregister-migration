package nu.fgv.register.migration.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Event extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private EventType event;

    private SourceType source;

    @Builder
    public Event(
            final Long id,
            final SourceType source,
            final EventType event,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.source = source;
        this.event = event;
    }

    public enum EventType {
        CREATE,
        UPDATE,
        REMOVE
    }

    public enum SourceType {
        NEWS,
        SPEX,
        SPEX_CATEGORY,
        SPEXARE,
        TAG,
        TASK,
        TASK_CATEGORY,
        USER,
        SESSION
    }
}
