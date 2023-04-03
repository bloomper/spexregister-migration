package nu.fgv.register.migration;

import lombok.Getter;
import lombok.Setter;
import nu.fgv.register.migration.model.News;
import nu.fgv.register.migration.model.Spex;
import nu.fgv.register.migration.model.SpexCategory;
import nu.fgv.register.migration.model.Spexare;
import nu.fgv.register.migration.model.Tag;
import nu.fgv.register.migration.model.Task;
import nu.fgv.register.migration.model.TaskCategory;
import nu.fgv.register.migration.model.Type;
import nu.fgv.register.migration.model.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MigrationContext {

    private final List<Spex> spex = new ArrayList<>();
    private final List<SpexCategory> spexCategories = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private final List<TaskCategory> taskCategories = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();
    private final List<News> news = new ArrayList<>();
    private final List<Spexare> spexare = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final List<Type> types = new ArrayList<>();
}
