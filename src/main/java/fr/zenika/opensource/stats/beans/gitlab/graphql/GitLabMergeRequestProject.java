package fr.zenika.opensource.stats.beans.gitlab.graphql;

public class GitLabMergeRequestProject {
    private String name;
    private String fullPath;
    private String webUrl;
    private String visibility;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public boolean isPublic() {
        return visibility == null || "public".equalsIgnoreCase(visibility);
    }
}

