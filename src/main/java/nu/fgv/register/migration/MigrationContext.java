package nu.fgv.register.migration;

import lombok.Getter;
import lombok.Setter;
import nu.fgv.register.migration.model.Spex;
import nu.fgv.register.migration.model.SpexCategory;
import nu.fgv.register.migration.model.Task;
import nu.fgv.register.migration.model.TaskCategory;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MigrationContext {

    private final List<Spex> spex = new ArrayList<>();
    private final List<SpexCategory> spexCategories = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private final List<TaskCategory> taskCategories = new ArrayList<>();
}
