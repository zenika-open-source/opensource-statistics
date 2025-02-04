package zenika.oss.stats.beans;

import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.gitlab.GitLabMember;

public class ZenikaMember {

    private String id;

    private String name;

    private String firstname;

    private GitHubMember gitHubAccount;

    private GitLabMember gitlabAccount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public GitHubMember getGitHubAccount() {
        return gitHubAccount;
    }

    public void setGitHubAccount(GitHubMember gitHubAccount) {
        this.gitHubAccount = gitHubAccount;
    }

    public GitLabMember getGitlabAccount() {
        return gitlabAccount;
    }

    public void setGitlabAccount(GitLabMember gitlabAccount) {
        this.gitlabAccount = gitlabAccount;
    }
}
