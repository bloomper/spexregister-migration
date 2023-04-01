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
public class Activity extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ToString.Exclude
    private SpexActivity spexActivity;

    @ToString.Exclude
    private List<TaskActivity> taskActivities;

    @ToString.Exclude
    private Spexare spexare;

    @Builder
    public Activity(
            final Long id,
            final SpexActivity spexActivity,
            final List<TaskActivity> taskActivities,
            final Spexare spexare,
            final String createdBy,
            final Date createdAt,
            final String lastModifiedBy,
            final Date lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.spexActivity = spexActivity;
        this.taskActivities = taskActivities;
        this.spexare = spexare;
    }
}
