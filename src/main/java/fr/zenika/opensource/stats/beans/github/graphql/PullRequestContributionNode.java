package fr.zenika.opensource.stats.beans.github.graphql;

public class PullRequestContributionNode {

    private PullRequestNode pullRequest;

    public PullRequestNode getPullRequest() {
        return pullRequest;
    }

    public void setPullRequest(PullRequestNode pullRequest) {
        this.pullRequest = pullRequest;
    }
}
