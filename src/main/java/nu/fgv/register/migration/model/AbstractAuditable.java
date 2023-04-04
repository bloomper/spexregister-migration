package nu.fgv.register.migration.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public abstract class AbstractAuditable {

    private String createdBy;

    private LocalDateTime createdAt;

    private String lastModifiedBy;

    private LocalDateTime lastModifiedAt;

}
