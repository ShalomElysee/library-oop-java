package com.junia.library.model;

/**
 * Anything the library can lend. Abstract because "a generic item" does not exist
 * in real life: each concrete type (book, DVD) defines its own loan rules.
 */
public abstract class LibraryItem {

    private final String id;
    private final String title;
    private final String creator;
    private boolean available = true;

    protected LibraryItem(String id, String title, String creator) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Item id must not be blank.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Item title must not be blank.");
        }
        this.id = id;
        this.title = title;
        this.creator = creator;
    }

    /** Number of days an item of this type can be kept. */
    public abstract int getLoanDurationDays();

    /** Late fee charged per day of delay. */
    public abstract double getDailyLateFee();

    public abstract String getTypeLabel();

    public void markAsBorrowed() {
        available = false;
    }

    public void markAsReturned() {
        available = true;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return getTypeLabel() + " " + id + " - \"" + title + "\" by " + creator;
    }
}
