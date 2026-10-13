package fr.zenika.opensource.stats.beans.github.graphql;

import jakarta.json.bind.annotation.JsonbProperty;

public class PullRequestRepository {

    private String name;
    private String nameWithOwner;
    private String url;

    @JsonbProperty("isPrivate")
    private boolean isPrivate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameWithOwner() {
        return nameWithOwner;
    }

    public void setNameWithOwner(String nameWithOwner) {
        this.nameWithOwner = nameWithOwner;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    @JsonbProperty("isPrivate")
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
