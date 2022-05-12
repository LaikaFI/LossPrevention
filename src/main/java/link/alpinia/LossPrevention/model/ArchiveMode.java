package link.alpinia.LossPrevention.model;

public enum ArchiveMode {

    /**
     * Archives the channel by renaming it to the current date and makes it so only administrators can see it.
     */
    ARCHIVAL,

    /**
     * Deletes the channel and its contents. This is irretrievable.
     */
    DELETION
}
