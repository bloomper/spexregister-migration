package nu.fgv.register.migration.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class News extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Date visibleFrom;

    private Date visibleTo;

    private String subject;

    private String text;

    private Boolean published;

    @Builder
    public News(
            final Long id,
            final Date visibleFrom,
            final Date visibleTo,
            final String subject,
            final String text,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.visibleFrom = visibleFrom;
        this.visibleTo = visibleTo;
        this.subject = subject;
        this.text = text;
    }
}
