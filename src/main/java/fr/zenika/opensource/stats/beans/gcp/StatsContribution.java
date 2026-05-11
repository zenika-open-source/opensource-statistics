package fr.zenika.opensource.stats.beans.gcp;

public class StatsContribution {

    public String year;

    public String month;

    public String idMember;

    public String githubHandle;

    public int numberOfContributionsOnGitHub;

    public String gitlabHandle;

    public int numberOfContributionsOnGitLab;

    public String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getIdMember() {
        return idMember;
    }

    public void setIdMember(String idMember) {
        this.idMember = idMember;
    }

    public String getGithubHandle() {
        return githubHandle;
    }

    public void setGithubHandle(String githubHandle) {
        this.githubHandle = githubHandle;
    }

    public int getNumberOfContributionsOnGitHub() {
        return numberOfContributionsOnGitHub;
    }

    public void setNumberOfContributionsOnGitHub(int numberOfContributionsOnGitHub) {
        this.numberOfContributionsOnGitHub = numberOfContributionsOnGitHub;
    }

    public String getGitlabHandle() {
        return gitlabHandle;
    }

    public void setGitlabHandle(String gitlabHandle) {
        this.gitlabHandle = gitlabHandle;
    }

    public int getNumberOfContributionsOnGitLab() {
        return numberOfContributionsOnGitLab;
    }

    public void setNumberOfContributionsOnGitLab(int numberOfContributionsOnGitLab) {
        this.numberOfContributionsOnGitLab = numberOfContributionsOnGitLab;
    }
}
