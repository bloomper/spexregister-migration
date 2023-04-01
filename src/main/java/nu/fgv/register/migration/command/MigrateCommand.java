package nu.fgv.register.migration.command;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.reader.NewsReader;
import nu.fgv.register.migration.reader.SpexCategoryReader;
import nu.fgv.register.migration.reader.SpexReader;
import nu.fgv.register.migration.reader.SpexareReader;
import nu.fgv.register.migration.reader.TagReader;
import nu.fgv.register.migration.reader.TaskCategoryReader;
import nu.fgv.register.migration.reader.TaskReader;
import nu.fgv.register.migration.reader.TypeReader;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@Slf4j
public class MigrateCommand {

    private final SpexCategoryReader spexCategoryReader;
    private final SpexReader spexReader;
    private final TaskCategoryReader taskCategoryReader;
    private final TaskReader taskReader;
    private final TagReader tagReader;
    private final NewsReader newsReader;
    private final SpexareReader spexareReader;
    private final TypeReader typeReader;

    public MigrateCommand(final SpexCategoryReader spexCategoryReader,
                          final SpexReader spexReader,
                          final TaskCategoryReader taskCategoryReader,
                          final TaskReader taskReader,
                          final TagReader tagReader,
                          final NewsReader newsReader,
                          final SpexareReader spexareReader,
                          final TypeReader typeReader) {
        this.spexCategoryReader = spexCategoryReader;
        this.spexReader = spexReader;
        this.taskCategoryReader = taskCategoryReader;
        this.taskReader = taskReader;
        this.tagReader = tagReader;
        this.newsReader = newsReader;
        this.spexareReader = spexareReader;
        this.typeReader = typeReader;
    }

    @ShellMethod("Migrates Spexregister 1.x -> 2.x")
    public void migrate(
            @ShellOption(value = {"-d"}, defaultValue = "true") final boolean dryRun
    ) {
        final MigrationContext context = new MigrationContext();

        log.info("Starting to read from source database");
        log.info("Reading types");
        typeReader.read(context);
        log.info("Reading spex categories");
        spexCategoryReader.read(context);
        log.info("Reading spex");
        spexReader.read(context);
        log.info("Reading task categories");
        taskCategoryReader.read(context);
        log.info("Reading tasks");
        taskReader.read(context);
        log.info("Reading tags");
        tagReader.read(context);
        log.info("Reading news");
        newsReader.read(context);
        log.info("Reading spexare");
        spexareReader.read(context);
        log.info("Done reading from source database");

        log.info("Spex categories: {}", context.getSpexCategories().size());
        log.info("Spex           : {}", context.getSpex().size());
        log.info("Task categories: {}", context.getTaskCategories().size());
        log.info("Tasks          : {}", context.getTasks().size());
        log.info("Tags           : {}", context.getTags().size());
        log.info("News           : {}", context.getNews().size());
        log.info("Spexare        : {}", context.getSpexare().size());
        log.info("Types          : {}", context.getTypes().size());

        if (!dryRun) {
            log.info("Starting to write to target database");
            // TODO: Truncate (not types!)
            // TODO: Write
            log.info("Done writing to target database");
        }
    }
}
