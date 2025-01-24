package zenika.oss.stats.beans;

import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.gitlab.GitLabMember;

import java.rmi.server.UID;

public class ZenikaMember {

    private UID id;

    private String name;

    private String firstname;

    private GitHubMember gitHubAccount;

    private GitLabMember gitlabAccount;
}
