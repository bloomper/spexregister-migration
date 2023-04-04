package nu.fgv.register.migration.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Type extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private Map<String, String> labels;

    private TypeType type;

    @Builder
    public Type(
            final String id,
            final Map<String, String> labels,
            final TypeType type,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.labels = labels;
        this.type = type;
    }
}
