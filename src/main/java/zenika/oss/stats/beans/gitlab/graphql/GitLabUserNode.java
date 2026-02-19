package zenika.oss.stats.beans.gitlab.graphql;

public class GitLabUserNode {
    private String username;
    private GitLabMergeRequestsCount authoredMergeRequests;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public GitLabMergeRequestsCount getAuthoredMergeRequests() {
        return authoredMergeRequests;
    }

    public void setAuthoredMergeRequests(GitLabMergeRequestsCount authoredMergeRequests) {
        this.authoredMergeRequests = authoredMergeRequests;
    }
}
