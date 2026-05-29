package fr.zenika.opensource.stats.config;

public enum FirestoreCollections {
    PROJECTS("projects"),
    MEMBERS("members"),
    STATS("stats"),
    PARAMS("params"),
    ;

    private static volatile String prefix = "";

    public static void setPrefix(String prefix) {
        FirestoreCollections.prefix = prefix == null ? "" : prefix;
    }

    private final String baseName;

    FirestoreCollections(String baseName) {
        this.baseName = baseName;
    }

    public String getValue() {
        return prefix + baseName;
    }
}
