package nu.fgv.register.migration.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public abstract class AbstractAuditable {

    private String createdBy;

    private Date createdAt;

    private String lastModifiedBy;

    private Date lastModifiedAt;

}
