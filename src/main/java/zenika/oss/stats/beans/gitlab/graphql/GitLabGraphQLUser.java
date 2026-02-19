package zenika.oss.stats.beans.gitlab.graphql;

public class GitLabGraphQLUser {
    private GitLabUserCalendar userCalendar;
    private GitLabMergeRequestsCount authoredMergeRequests;

    public GitLabUserCalendar getUserCalendar() {
        return userCalendar;
    }

    public void setUserCalendar(GitLabUserCalendar userCalendar) {
        this.userCalendar = userCalendar;
    }

    public GitLabMergeRequestsCount getAuthoredMergeRequests() {
        return authoredMergeRequests;
    }

    public void setAuthoredMergeRequests(GitLabMergeRequestsCount authoredMergeRequests) {
        this.authoredMergeRequests = authoredMergeRequests;
    }
}
