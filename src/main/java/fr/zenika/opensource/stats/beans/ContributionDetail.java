package fr.zenika.opensource.stats.beans;

public class ContributionDetail {

    private String projectName;
    private String projectFullName;
    private String projectUrl;
    private String prTitle;
    private String prUrl;
    private String prState;
    private String createdAt;

    public ContributionDetail() {
    }

    public ContributionDetail(String projectName, String projectFullName, String projectUrl,
            String prTitle, String prUrl, String prState, String createdAt) {
        this.projectName = projectName;
        this.projectFullName = projectFullName;
        this.projectUrl = projectUrl;
        this.prTitle = prTitle;
        this.prUrl = prUrl;
        this.prState = prState;
        this.createdAt = createdAt;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectFullName() {
        return projectFullName;
    }

    public void setProjectFullName(String projectFullName) {
        this.projectFullName = projectFullName;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getPrTitle() {
        return prTitle;
    }

    public void setPrTitle(String prTitle) {
        this.prTitle = prTitle;
    }

    public String getPrUrl() {
        return prUrl;
    }

    public void setPrUrl(String prUrl) {
        this.prUrl = prUrl;
    }

    public String getPrState() {
        return prState;
    }

    public void setPrState(String prState) {
        this.prState = prState;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
