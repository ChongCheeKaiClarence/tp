package soconnect.logic.commands.todo;

import static java.util.Objects.requireNonNull;
import static soconnect.commons.util.CollectionUtil.requireAllNonNull;
import static soconnect.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static soconnect.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static soconnect.logic.parser.CliSyntax.PREFIX_TAG;
import static soconnect.model.Model.PREDICATE_SHOW_ALL_TODOS;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import soconnect.commons.core.Messages;
import soconnect.commons.core.index.Index;
import soconnect.commons.util.CollectionUtil;
import soconnect.logic.commands.CommandResult;
import soconnect.logic.commands.TagCommand;
import soconnect.logic.commands.TagCreateCommand;
import soconnect.logic.commands.exceptions.CommandException;
import soconnect.model.Model;
import soconnect.model.tag.Tag;
import soconnect.model.todo.Description;
import soconnect.model.todo.Priority;
import soconnect.model.todo.Todo;

/**
 * Edits the details of an existing {@code Todo} in the TodoList.
 */
public class TodoEditCommand extends TodoCommand {

    public static final String SUB_COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUB_COMMAND_WORD
        + ": Edits the details of the Todo identified by the index number used in the displayed Todo List. \n"
        + "Existing values will be overwritten by the input values. Tags are overwritten as a group.\n"
        + "Parameters: INDEX (must be a positive integer) "
        + "[" + PREFIX_DESCRIPTION + "DESCRIPTION] "
        + "[" + PREFIX_PRIORITY + "PRIORITY] "
        + "[" + PREFIX_TAG + "TAG]...\n"
        + "Example: " + COMMAND_WORD + " " + SUB_COMMAND_WORD + " 1 "
        + PREFIX_DESCRIPTION + "Watch math lecture recording \n"
        + COMMAND_WORD + " " + SUB_COMMAND_WORD + " 3 "
        + PREFIX_TAG + "CS2100 \n";

    public static final String MESSAGE_EDIT_TODO_SUCCESS = "Edited Todo: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_TODO = "This Todo already exists in SoConnect!";
    public static final String MESSAGE_TAG_DOES_NOT_EXIST = "The tag(s) do not exist, please create them first using `"
        + TagCommand.COMMAND_WORD + " " + TagCreateCommand.COMMAND_WORD + "`.";

    private final Index index;
    private final EditTodoDescriptor editTodoDescriptor;

    /**
     * Constructs an {@code TodoEditCommand} to edit the details of an existing {@code Todo} in SoConnect.
     *
     * @param index Index of the {@code Todo} in the filtered todo list to edit.
     * @param editTodoDescriptor Details to edit the {@code Todo} with.
     */
    public TodoEditCommand(Index index, EditTodoDescriptor editTodoDescriptor) {
        requireAllNonNull(index, editTodoDescriptor);

        this.index = index;
        this.editTodoDescriptor = new EditTodoDescriptor(editTodoDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Todo> lastShownList = model.getFilteredTodoList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TODO_DISPLAYED_INDEX);
        }

        Todo todoToEdit = lastShownList.get(index.getZeroBased());
        Todo editedTodo = createEditedTodo(todoToEdit, editTodoDescriptor);

        if (!todoToEdit.equals(editedTodo) && model.hasTodo(editedTodo)) {
            throw new CommandException(MESSAGE_DUPLICATE_TODO);
        }

        if (!model.areTagsAvailable(editedTodo)) {
            throw new CommandException(MESSAGE_TAG_DOES_NOT_EXIST);
        }

        model.setTodo(todoToEdit, editedTodo);
        model.updateFilteredTodoList(PREDICATE_SHOW_ALL_TODOS);
        return new CommandResult(String.format(MESSAGE_EDIT_TODO_SUCCESS, editedTodo));
    }

    /**
     * Creates and returns a {@code Todo} with the details of {@code todoToEdit}
     * edited with {@code editTodoDescriptor}.
     */
    private static Todo createEditedTodo(Todo todoToEdit, EditTodoDescriptor editTodoDescriptor) {
        assert todoToEdit != null;

        Description updatedDescription = editTodoDescriptor.getDescription().orElse(todoToEdit.getDescription());
        Priority updatedPriority = editTodoDescriptor.getPriority().orElse(todoToEdit.getPriority());
        Set<Tag> updatedTags = editTodoDescriptor.getTags().orElse(todoToEdit.getTags());

        return new Todo(updatedDescription, updatedPriority, updatedTags);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TodoEditCommand)) {
            return false;
        }

        // state check
        TodoEditCommand e = (TodoEditCommand) other;
        return index.equals(e.index)
            && editTodoDescriptor.equals(e.editTodoDescriptor);
    }

    /**
     * Stores the details to edit the todo with. Each non-empty field value will replace the
     * corresponding field value of the todo.
     */
    public static class EditTodoDescriptor {
        private Description description;
        private Priority priority;
        private Set<Tag> tagSet;

        public EditTodoDescriptor() {}

        /**
         * Copy constructor.
         */
        public EditTodoDescriptor(EditTodoDescriptor toCopy) {
            setDescription(toCopy.description);
            setPriority(toCopy.priority);
            setTags(toCopy.tagSet);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(description, priority, tagSet);
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public Optional<Description> getDescription() {
            return Optional.ofNullable(description);
        }

        public void setPriority(Priority priority) {
            this.priority = priority;
        }

        public Optional<Priority> getPriority() {
            return Optional.ofNullable(priority);
        }

        public void setTags(Set<Tag> tagSet) {
            this.tagSet = tagSet;
        }

        public Optional<Set<Tag>> getTags() {
            return Optional.ofNullable(tagSet);
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditTodoDescriptor)) {
                return false;
            }

            // state check
            EditTodoDescriptor e = (EditTodoDescriptor) other;

            return getDescription().equals(e.getDescription())
                    && getPriority().equals(e.getPriority())
                    && getTags().equals(e.getTags());
        }
    }
}
