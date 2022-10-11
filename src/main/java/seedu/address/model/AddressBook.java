package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.TreeSet;

import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;

/**
 * Wraps all data at the address-book level
 * Duplicates are not allowed (by .isSamePerson comparison)
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons;
    private final UniqueTagList tags;

    /*
     * The 'unusual' code block below is a non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        persons = new UniquePersonList();
        tags = new UniqueTagList();
    }

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Replaces the contents of the tag list with {@code tags}.
     * {@code tags} must not contain duplicate tags.
     */
    public void setTags(List<Tag> tags) {
        this.tags.setTags(tags);
    }
    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        setTags(newData.getTagList());
    }

    /// sorting operations

    /**
     * Sorts the address book by name in alphabetical order.
     *
     * @param isReverse Whether the sorting should be in reverse order
     */
    public void sortByName(Boolean isReverse) {
        persons.sortByName(isReverse);
    }

    /**
     * Sorts the address book by phone number in increasing order.
     *
     * @param isReverse Whether the sorting should be in reverse order
     */
    public void sortByPhone(Boolean isReverse) {
        persons.sortByPhone(isReverse);
    }

    /**
     * Sorts the address book by email in alphabetical order.
     *
     * @param isReverse Whether the sorting should be in reverse order
     */
    public void sortByEmail(Boolean isReverse) {
        persons.sortByEmail(isReverse);
    }

    /**
     * Sorts the address book by address in alphabetical order.
     *
     * @param isReverse Whether the sorting should be in reverse order
     */
    public void sortByAddress(Boolean isReverse) {
        persons.sortByAddress(isReverse);
    }

    /**
     * Sorts the address book by a tag.
     * Contacts with the tag appear before those without the tag.
     *
     * @param tag       The tag to sort with
     * @param isReverse Whether the sorting should be in reverse order
     */
    public void sortByTag(Tag tag, Boolean isReverse) {
        persons.sortByTag(tag, isReverse);
    }

    public TreeSet<String> getUniqueNames() {
        return persons.getUniqueNames();
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// tag-level operations

    /**
     * Returns true if tag already does exists.
     * @param tag The tag added.
     * @return True if tag exists. False if otherwise.
     */
    public boolean hasTag(Tag tag) {
        requireNonNull(tag);
        return tags.hasTag(tag);
    }

    /**
     * Adds the tag to the tagList.
     *
     * @param tag The tag to be added
     */
    public void addTag(Tag tag) {
        tags.addTagToList(tag);
    }

    /**
     * Changes the original tag to the new tag.
     *
     * @param oldTag The original tag
     * @param newTag The new tag to replace the original tag
     */
    public void editTag(Tag oldTag, Tag newTag) {
        tags.editTag(oldTag, newTag);
        persons.changeRelevantPersonTag(oldTag, newTag);
    }

    //// util methods

    @Override
    public String toString() {
        return persons.asUnmodifiableObservableList().size() + " persons";
        // TODO: refine later
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Tag> getTagList() {
        return tags.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddressBook // instanceof handles nulls
                && persons.equals(((AddressBook) other).persons));
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }
}
