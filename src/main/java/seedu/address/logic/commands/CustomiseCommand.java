package seedu.address.logic.commands;

import static seedu.address.logic.commands.CustomiseCommand.Attribute.ADDRESS;
import static seedu.address.logic.commands.CustomiseCommand.Attribute.EMAIL;
import static seedu.address.logic.commands.CustomiseCommand.Attribute.PHONE;
import static seedu.address.logic.commands.CustomiseCommand.Attribute.TAGS;
import static seedu.address.logic.commands.CustomiseCommand.CustomiseSubCommand.HIDE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Arrays;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Customises the user experience.
 */
public class CustomiseCommand extends Command {
    public static final String COMMAND_WORD = "customise";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " order: Changes the order of the details\n"
            + "Parameters: "
            + "[" + PREFIX_PHONE + "] "
            + "[" + PREFIX_EMAIL + "] "
            + "[" + PREFIX_ADDRESS + "] "
            + "[" + PREFIX_TAG + "]\n"
            + "Example: " + COMMAND_WORD + " order p/ a/\n\n"
            + COMMAND_WORD + " hide / " + COMMAND_WORD + " show: Changes the visibility of the details\n"
            + "Parameters: "
            + "[" + PREFIX_PHONE + "] "
            + "[" + PREFIX_EMAIL + "] "
            + "[" + PREFIX_ADDRESS + "] "
            + "[" + PREFIX_TAG + "]\n"
            + "Examples: " + COMMAND_WORD + " hide p/ a/, " + COMMAND_WORD + " show a/ e/ p/ t/";

    public static final String MESSAGE_ORDER_SUCCESS = "Preferred order of details changed.";

    public static final String MESSAGE_HIDE_SUCCESS = "Attributes specified are hidden.";

    public static final String MESSAGE_SHOW_SUCCESS = "Attributes specified are shown.";

    private final CustomiseSubCommand subCommand;

    private final Attribute[] arguments;

    /**
     * The subcommands of the customise command.
     */
    public enum CustomiseSubCommand {
        ORDER,
        HIDE,
        SHOW
    }

    /**
     * The attributes of the contacts excluding name.
     */
    public enum Attribute {
        ADDRESS,
        EMAIL,
        PHONE,
        TAGS
    }

    /**
     * Creates an CustomiseCommand to customise user preferences.
     *
     * @param subCommand the subcommand that is specified
     * @param args attributes
     */
    public CustomiseCommand(CustomiseSubCommand subCommand, Attribute[] args) {
        this.subCommand = subCommand;
        this.arguments = args;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        switch (subCommand) {
        case ORDER:
            changeAttributeOrder(model);
            return new CommandResult(MESSAGE_ORDER_SUCCESS);
        case HIDE:
            toggleAttributes(model);
            return new CommandResult(MESSAGE_HIDE_SUCCESS);
        case SHOW:
            toggleAttributes(model);
            return new CommandResult(MESSAGE_SHOW_SUCCESS);
        default:
            throw new CommandException(Messages.MESSAGE_INVALID_COMMAND_FORMAT);
        }
    }

    /**
     * Changes the attribute order shown to the user.
     *
     * @param model the model which the command operates on
     * @throws CommandException if an error occurs during command execution
     */
    private void changeAttributeOrder(Model model) throws CommandException {
        GuiSettings currSettings = model.getGuiSettings();
        int xCoordinate = 0;
        int yCoordinate = 0;
        if (currSettings.getWindowCoordinates() != null) {
            xCoordinate = (int) currSettings.getWindowCoordinates().getX();
            yCoordinate = (int) currSettings.getWindowCoordinates().getY();
        }
        GuiSettings newSettings = new GuiSettings(currSettings.getWindowWidth(), currSettings.getWindowHeight(),
                xCoordinate, yCoordinate, convertOrderToString(arguments), currSettings.getHiddenAttributes());

        model.setGuiSettings(newSettings);
        refreshList(model);
    }

    /**
     * Toggles the attributes between being shown or hidden.
     *
     * @param model the model which the command operates on
     * @throws CommandException if an error occurs during command execution
     */
    private void toggleAttributes(Model model) throws CommandException {
        GuiSettings currSettings = model.getGuiSettings();
        String currHiddenAttributes = currSettings.getHiddenAttributes().trim();
        boolean[] isHidden = new boolean[4]; //0: ADDRESS, 1: EMAIL, 2: PHONE, 3: TAGS
        if (!currHiddenAttributes.equals("NONE")) {
            String[] currHiddenAttributesArr = currHiddenAttributes.split(",");
            try {
                setCurrentHiddenState(currHiddenAttributesArr, isHidden);
            } catch (IllegalArgumentException e) {
                isHidden = new boolean[4];
            }
        }

        setNewHiddenState(isHidden, subCommand.equals(HIDE));

        int xCoordinate = 0;
        int yCoordinate = 0;
        if (currSettings.getWindowCoordinates() != null) {
            xCoordinate = (int) currSettings.getWindowCoordinates().getX();
            yCoordinate = (int) currSettings.getWindowCoordinates().getY();
        }
        GuiSettings newSettings = new GuiSettings(currSettings.getWindowWidth(), currSettings.getWindowHeight(),
                xCoordinate, yCoordinate, currSettings.getAttributeOrder(), convertHiddenToString(isHidden));

        model.setGuiSettings(newSettings);
        refreshList(model);
    }

    /**
     * Sets the array elements to true if attributes are hidden based on the input String array.
     *
     * @param strArr an array of string representations of the attributes
     * @param isHidden a boolean array where index 0 represents address, index 1 represents email
     *                 index 2 represents phone and index 3 represents tags
     */
    private void setCurrentHiddenState(String[] strArr, boolean[] isHidden) {
        for (String s : strArr) {
            isHidden[convertAttributeToIndex(s)] = true;
        }
    }

    /**
     * Sets the new attributes to be hidden based on the arguments.
     *
     * @param isHidden a boolean array where index 0 represents address, index 1 represents email,
     *                 index 2 represents phone and index 3 represents tags
     * @param isSetToHide is set to true to hide and false to show
     */
    private void setNewHiddenState(boolean[] isHidden, boolean isSetToHide) {
        for (Attribute argument : arguments) {
            isHidden[convertAttributeToIndex(argument.toString())] = isSetToHide;
        }
    }

    /**
     * Converts the boolean array to a string representing what attributes are hidden.
     *
     * @param isHidden a boolean array where index 0 represents address, index 1 represents email,
     *                 index 2 represents phone and index 3 represents tags
     * @return a string representation of what attributes are hidden
     */
    private String convertHiddenToString(boolean[] isHidden) throws CommandException {
        if (isHidden == null || isHidden.length != 4) {
            throw new CommandException(Messages.MESSAGE_INVALID_COMMAND_FORMAT);
        }

        StringBuilder sb = new StringBuilder();
        if (isHidden[0]) {
            sb.append(ADDRESS).append(",");
        }
        if (isHidden[1]) {
            sb.append(EMAIL).append(",");
        }
        if (isHidden[2]) {
            sb.append(PHONE).append(",");
        }
        if (isHidden[3]) {
            sb.append(TAGS).append(",");
        }

        if (sb.length() == 0) {
            return "NONE";
        }

        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Converts the given attribute into the index corresponding to the attribute.
     *
     * @param attribute the string representation of the attribute
     * @return an index that corresponds to the attribute
     */
    private int convertAttributeToIndex(String attribute) {
        switch(attribute) {
        case "ADDRESS":
            return 0;
        case "EMAIL":
            return 1;
        case "PHONE":
            return 2;
        case "TAGS":
            return 3;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Converts a given Attribute array into a string.
     *
     * @param attributes an array of Attribute objects
     * @return the string representation of the attribute order
     * @throws CommandException if the execution of the command encountered an error
     */
    private String convertOrderToString(Attribute[] attributes) throws CommandException {
        if (attributes == null || attributes.length != 4) {
            throw new CommandException(Messages.MESSAGE_INVALID_COMMAND_FORMAT);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attributes.length - 1; i++) {
            sb.append(attributes[i].toString()).append(">");
        }

        sb.append(attributes[attributes.length - 1]);

        return sb.toString();
    }

    /**
     * Refreshes the list.
     *
     * @param model the model which the command operates on
     */
    private void refreshList(Model model) {
        model.updateFilteredPersonList(unused -> false);
        model.updateFilteredPersonList(unused -> true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof CustomiseCommand
                && subCommand.equals(((CustomiseCommand) other).subCommand))
                && Arrays.equals(arguments, ((CustomiseCommand) other).arguments);
    }
}
