package nu.fgv.register.migration.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class User extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String uid;

    private String password;

    private String passwordSalt;

    private Integer spexareId;

    private String state;

    private List<String> groups = new ArrayList<>();

    @Builder
    public User(
            final Long id,
            final String uid,
            final String password,
            final String passwordSalt,
            final Integer spexareId,
            final String state,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.uid = uid;
        this.password = password;
        this.passwordSalt = passwordSalt;
        this.spexareId = spexareId;
        this.state = state;
    }
}
