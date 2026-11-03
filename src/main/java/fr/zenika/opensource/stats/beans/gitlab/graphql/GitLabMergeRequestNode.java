package fr.zenika.opensource.stats.beans.gitlab.graphql;

public class GitLabMergeRequestNode {
    private String title;
    private String webUrl;
    private String state;
    private String createdAt;
    private GitLabMergeRequestProject project;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public GitLabMergeRequestProject getProject() {
        return project;
    }

    public void setProject(GitLabMergeRequestProject project) {
        this.project = project;
    }
}

