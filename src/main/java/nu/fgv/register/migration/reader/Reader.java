package nu.fgv.register.migration.reader;

import nu.fgv.register.migration.MigrationContext;

public interface Reader {

    void read(MigrationContext context);
}
