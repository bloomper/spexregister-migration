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
public class SpexActivity extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ToString.Exclude
    private Activity activity;

    @ToString.Exclude
    private Spex spex;

    @Builder
    public SpexActivity(
            final Long id,
            final Activity activity,
            final Spex spex,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.activity = activity;
        this.spex = spex;
    }
}
