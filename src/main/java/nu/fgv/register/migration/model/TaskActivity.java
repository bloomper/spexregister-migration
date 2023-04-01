package nu.fgv.register.migration.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class TaskActivity extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ToString.Exclude
    private List<Actor> actors;

    @ToString.Exclude
    private Task task;

    @ToString.Exclude
    private Activity activity;

    @Builder
    public TaskActivity(
            final Long id,
            final Activity activity,
            final Task task,
            final List<Actor> actors,
            final String createdBy,
            final Date createdAt,
            final String lastModifiedBy,
            final Date lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.activity = activity;
        this.task = task;
        this.actors = actors;
    }
}
