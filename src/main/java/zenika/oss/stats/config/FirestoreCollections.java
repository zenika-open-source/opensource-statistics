package zenika.oss.stats.config;

import org.eclipse.microprofile.config.ConfigProvider;

public enum FirestoreCollections {
    PROJECTS("projects"),
    MEMBERS("members"),
    STATS("stats"),
    ;

    private final String baseName;

    FirestoreCollections(String baseName) {
        this.baseName = baseName;
    }

    public String getValue() {
        String prefix = ConfigProvider.getConfig()
                .getOptionalValue("firestore.collection.prefix", String.class)
                .orElse("");
        return prefix + baseName;
    }
}
