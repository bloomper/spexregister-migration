package nu.fgv.register.migration.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Spexare extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String firstName;

    private String lastName;

    private String nickName;

    private LocalDate birthDate;

    private String socialSecurityNumber;

    private String graduation;

    private String comment;

    private String imageUrl;

    private String imageContentType;

    @ToString.Exclude
    private Spexare partner;

    @ToString.Exclude
    private List<Activity> activities;

    @ToString.Exclude
    private List<Tagging> taggings;

    @ToString.Exclude
    private List<Address> addresses;

    @ToString.Exclude
    private List<Membership> memberships;

    @ToString.Exclude
    private List<Consent> consents;

    @ToString.Exclude
    private List<Toggle> toggles;

    @Builder
    public Spexare(
            final Long id,
            final String firstName,
            final String lastName,
            final String nickName,
            final LocalDate birthDate,
            final String socialSecurityNumber,
            final String graduation,
            final String comment,
            final String imageUrl,
            final String imageContentType,
            final Spexare partner,
            final List<Activity> activities,
            final List<Tagging> taggings,
            final List<Address> addresses,
            final List<Membership> memberships,
            final List<Consent> consents,
            final List<Toggle> toggles,
            final String createdBy,
            final LocalDateTime createdAt,
            final String lastModifiedBy,
            final LocalDateTime lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
        this.birthDate = birthDate;
        this.socialSecurityNumber = socialSecurityNumber;
        this.graduation = graduation;
        this.comment = comment;
        this.imageUrl = imageUrl;
        this.imageContentType = imageContentType;
        this.partner = partner;
        this.activities = activities;
        this.taggings = taggings;
        this.addresses = addresses;
        this.memberships = memberships;
        this.consents = consents;
        this.toggles = toggles;
    }
}
