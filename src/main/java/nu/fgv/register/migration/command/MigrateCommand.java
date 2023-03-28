package nu.fgv.register.migration.command;

import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.reader.SpexCategoryReader;
import nu.fgv.register.migration.reader.SpexReader;
import nu.fgv.register.migration.reader.TaskCategoryReader;
import nu.fgv.register.migration.reader.TaskReader;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class MigrateCommand {

    private final SpexCategoryReader spexCategoryReader;
    private final SpexReader spexReader;
    private final TaskCategoryReader taskCategoryReader;
    private final TaskReader taskReader;

    public MigrateCommand(final SpexCategoryReader spexCategoryReader,
                          final SpexReader spexReader,
                          final TaskCategoryReader taskCategoryReader,
                          final TaskReader taskReader) {
        this.spexCategoryReader = spexCategoryReader;
        this.spexReader = spexReader;
        this.taskCategoryReader = taskCategoryReader;
        this.taskReader = taskReader;
    }

    @ShellMethod("Migrates Spexregister 1.x")
    public boolean migrate(
            @ShellOption(value = {"-d"}, defaultValue = "false") final boolean dryRun,
            @ShellOption(value = {"-e"}, defaultValue = "ALL") final String model
    ) {
        final MigrationContext context = new MigrationContext();

        spexCategoryReader.read(context);
        spexReader.read(context);
        taskCategoryReader.read(context);
        taskReader.read(context);

        return dryRun;
    }
}
