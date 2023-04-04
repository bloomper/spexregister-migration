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
public class Spex extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String year;

    @ToString.Exclude
    private Spex parent;

    @ToString.Exclude
    private SpexDetails details;

    public boolean isRevival() {
        return parent != null;
    }

    @Builder
    public Spex(
            final Long id,
            final String year,
            final SpexDetails details,
            final Spex parent,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.year = year;
        this.details = details;
        this.parent = parent;
    }
}
