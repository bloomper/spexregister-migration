package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.MigrationContext;

public interface Writer {

    void clean();

    void write(MigrationContext context);
}
