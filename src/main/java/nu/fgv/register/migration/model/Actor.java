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
public class Actor extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String role;

    private Type vocal;

    @ToString.Exclude
    private TaskActivity taskActivity;

    @Builder
    public Actor(
            final Long id,
            final String role,
            final Type vocal,
            final TaskActivity taskActivity,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.role = role;
        this.vocal = vocal;
        this.taskActivity = taskActivity;
    }
}
