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
public class Address extends AbstractAuditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String streetAddress;

    private String postalCode;

    private String city;

    private String country;

    private String phone;

    private String phoneMobile;

    private String emailAddress;

    private Type type;

    @ToString.Exclude
    private Spexare spexare;

    @Builder
    public Address(
            final Long id,
            final String streetAddress,
            final String postalCode,
            final String city,
            final String country,
            final String phone,
            final String phoneMobile,
            final String emailAddress,
            final Type type,
            final Spexare spexare,
            final String createdBy,
            final Date createdAt,
            final String lastModifiedBy,
            final Date lastModifiedAt
    ) {
        super(createdBy, createdAt, lastModifiedBy, lastModifiedAt);
        this.id = id;
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.phone = phone;
        this.phoneMobile = phoneMobile;
        this.emailAddress = emailAddress;
        this.type = type;
        this.spexare = spexare;
    }
}
