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
public class SpexCategory extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String firstYear;

    private String logoUrl;

    private String logoContentType;

    @Builder
    public SpexCategory(
            final Long id,
            final String name,
            final String firstYear,
            final String logoUrl,
            final String logoContentType,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.name = name;
        this.firstYear = firstYear;
        this.logoUrl = logoUrl;
        this.logoContentType = logoContentType;
    }
}
