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
public class Toggle extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Boolean value;

    private Type type;

    @ToString.Exclude
    private Spexare spexare;

    @Builder
    public Toggle(
            final Long id,
            final Boolean value,
            final Type type,
            final Spexare spexare,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.value = value;
        this.type = type;
        this.spexare = spexare;
    }
}
