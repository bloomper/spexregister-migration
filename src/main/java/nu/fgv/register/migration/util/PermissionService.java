package nu.fgv.register.migration.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Service
public class PermissionService {

    private final MutableAclService mutableAclService;

    public void grantPermission(final ObjectIdentity oid, final Permission permission, final Sid... recipients) {
        Arrays.asList(recipients).forEach(r -> grantPermission(oid, r, permission));
    }

    public void grantPermission(final ObjectIdentity oid, final Sid recipient, final Permission permission) {
        MutableAcl acl;

        try {
            acl = (MutableAcl) mutableAclService.readAclById(oid);
        } catch (final NotFoundException e) {
            acl = mutableAclService.createAcl(oid);
        }

        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        mutableAclService.updateAcl(acl);
    }

}
