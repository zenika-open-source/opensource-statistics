package zenika.oss.stats.beans.gcp;

public class StatsContribution {

    public String year;

    public String month;

    public String idZenikaMember;

    public String githubHandle;

    public int numberOfContributionsOnGitHub;

    public String gitlabHandle;

    public int numberOfContributionsOnGitLab;

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

    public String getIdZenikaMember() {
        return idZenikaMember;
    }

    public void setIdZenikaMember(String idZenikaMember) {
        this.idZenikaMember = idZenikaMember;
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
