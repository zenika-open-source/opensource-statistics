package zenika.oss.stats.config;

public enum FirestoreCollections {
    PROJECTS("projects"),
    MEMBERS("members"),
    STATS("stats"),
    ;

    public final String value;

    FirestoreCollections(String value) {
        this.value = value;
    }
}
