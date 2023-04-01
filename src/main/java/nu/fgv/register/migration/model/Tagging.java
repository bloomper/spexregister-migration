package nu.fgv.register.migration.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Tagging extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ToString.Exclude
    private Tag tag;

    @ToString.Exclude
    private Spexare spexare;

    @Builder
    public Tagging(
            final Long id,
            final Tag tag,
            final Spexare spexare,
            final String createdBy,
            final Date createdAt,
            final String lastModifiedBy,
            final Date lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.tag = tag;
        this.spexare = spexare;
    }
}
