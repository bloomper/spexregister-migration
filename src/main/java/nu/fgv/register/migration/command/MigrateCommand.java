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
import nu.fgv.register.migration.writer.NewsWriter;
import nu.fgv.register.migration.writer.SpexCategoryWriter;
import nu.fgv.register.migration.writer.SpexWriter;
import nu.fgv.register.migration.writer.SpexareWriter;
import nu.fgv.register.migration.writer.TagWriter;
import nu.fgv.register.migration.writer.TaskCategoryWriter;
import nu.fgv.register.migration.writer.TaskWriter;
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
    private final SpexCategoryWriter spexCategoryWriter;
    private final SpexWriter spexWriter;
    private final TaskCategoryWriter taskCategoryWriter;
    private final TaskWriter taskWriter;
    private final TagWriter tagWriter;
    private final NewsWriter newsWriter;
    private final SpexareWriter spexareWriter;

    public MigrateCommand(final SpexCategoryReader spexCategoryReader,
                          final SpexReader spexReader,
                          final TaskCategoryReader taskCategoryReader,
                          final TaskReader taskReader,
                          final TagReader tagReader,
                          final NewsReader newsReader,
                          final SpexareReader spexareReader,
                          final TypeReader typeReader,
                          final SpexCategoryWriter spexCategoryWriter,
                          final SpexWriter spexWriter,
                          final TaskCategoryWriter taskCategoryWriter,
                          final TaskWriter taskWriter,
                          final TagWriter tagWriter,
                          final NewsWriter newsWriter,
                          final SpexareWriter spexareWriter) {
        this.spexCategoryReader = spexCategoryReader;
        this.spexReader = spexReader;
        this.taskCategoryReader = taskCategoryReader;
        this.taskReader = taskReader;
        this.tagReader = tagReader;
        this.newsReader = newsReader;
        this.spexareReader = spexareReader;
        this.typeReader = typeReader;
        this.spexCategoryWriter = spexCategoryWriter;
        this.spexWriter = spexWriter;
        this.taskCategoryWriter = taskCategoryWriter;
        this.taskWriter = taskWriter;
        this.tagWriter = tagWriter;
        this.newsWriter = newsWriter;
        this.spexareWriter = spexareWriter;
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
            log.info("Starting to cleaning target database");
            log.info("Cleaning spexare");
            spexareWriter.clean();
            log.info("Cleaning spex");
            spexWriter.clean();
            log.info("Cleaning spex categories");
            spexCategoryWriter.clean();
            log.info("Cleaning tasks");
            taskWriter.clean();
            log.info("Cleaning task categories");
            taskCategoryWriter.clean();
            log.info("Cleaning tags");
            tagWriter.clean();
            log.info("Cleaning news");
            newsWriter.clean();
            log.info("Done cleaning target database");

            log.info("Starting to write to target database");
            log.info("Writing news");
            newsWriter.write(context);
            log.info("Writing tags");
            tagWriter.write(context);
            log.info("Writing task categories");
            taskCategoryWriter.write(context);
            log.info("Writing tasks");
            taskWriter.write(context);
            log.info("Writing spex categories");
            spexCategoryWriter.write(context);
            log.info("Writing spex");
            spexWriter.write(context);
            log.info("Writing spexare");
            spexareWriter.write(context);
            log.info("Done writing to target database");
        }
    }
}
