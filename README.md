# TableSchema
Library for reading structs from SQL and table-like databases.

Originally created for [Stonks](https://github.com/nahkd123/stonks).

## Example usage
```java
import static io.github.nahkd123.tableschema.schema.Constraint.*;

enum Status {
	WAITING,
	REJECTED,
	IN_PROGRESS,
	EVALUATING,
	COMPLETED;
}

record Project(UUID id, String name) {
	static final Field<Project, UUID> ID = new Field<>(FieldType.UUID, "id", Project::id).with(notNull()).with(unique());
	static final Field<Project, String> NAME = new Field<>(FieldType.fixedString(50), "name", Project::name).with(defaulted(""));

	// Schema describes the layout of your object (when saving to tabular data structure)
	// Schema version is used to automatically migrate table to new layout. Use -1 for version
	// to enforce migration every time .migrate() is called.
	static final Schema<UUID, Project> SCHEMA = Schema.of(ID, NAME, Project::new)
		.withVersion(0);
}

record Task(UUID id, UUID projectId, String name, Status status) {
	static final Field<Task, UUID> ID = new Field<>(FieldType.UUID, "id", Task::id).with(notNull()).with(unique());
	static final Field<Task, UUID> PROJECTID = new Field<>(FieldType.UUID, "projectId", Task::projectId).with(notNull());
	static final Field<Task, String> NAME = new Field<>(FieldType.fixedString(50), "name", Task::name).with(defaulted(""));
	static final Field<Task, Status> STATUS = new Field<>(FieldType.ofEnum(Status.values()), "status", Task::status).with(defaulted(Status.WAITING));
	static final Schema<UUID, Task> SCHEMA = Schema.of(ID, PROJECTID, NAME, STATUS, Task::new)
		.withVersion(0)
		.withIndexes(List.of(
			new Index<>("byProjectId").appendField(PROJECTID),
			new Index<>("byStatus").appendField(STATUS)));
}

try (JdbcDatabase db = new JdbcDatabase(DriverManager.getConnection("jdbc:sqlite:my_database.db"))) {
	Table<UUID, Project> projects = db.table("projects", Project.SCHEMA);
	Table<UUID, Task> tasks = db.table("tasks", Task.SCHEMA);

	// Migrate all tables to new schema version (if needed)
	projects.migrate(false);
	tasks.migrate(false);

	// Insert our project and related tasks
	projects.insert(new Project(UUID.randomUUID(), "Cool Mountain Co. Ltd construction"));
	Project project = projects.query(null, null).first();

	tasks.insert(List.of(
		new Task(UUID.randomUUID(), project.id(), "Planning", Status.IN_PROGRESS),
		new Task(UUID.randomUUID(), project.id(), "Relocating", Status.REJECTED),
		new Task(UUID.randomUUID(), project.id(), "Construction", Status.WAITING)));

	// Let's delete all rejected tasks
	try (QueryResult<Task> result = tasks.query(Filter.eq(Task.PROJECTID, project.id()), null)) {
		for (Task task : result) {
			System.out.println(task);
			if (task.status() == Status.REJECTED) tasks.deleteRow(task);
		}
	}

	try (QueryResult<Task> result = tasks.query(Filter.eq(Task.PROJECTID, project.id()), null)) {
		for (Task task : result) System.out.println(task);
	}

	// Delete our tables
	projects.drop();
	tasks.drop();
}
```

## License
MIT License.