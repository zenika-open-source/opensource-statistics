package fr.zenika.opensource.stats.beans.github.graphql;

import java.util.List;

public class PullRequestContributions {
    private int totalCount;
    private List<PullRequestContributionNode> nodes;

    public int getTotalCount() {

        return totalCount;
    }

    public void setTotalCount(final int totalCount) {

        this.totalCount = totalCount;
    }

    public List<PullRequestContributionNode> getNodes() {
        return nodes;
    }

    public void setNodes(final List<PullRequestContributionNode> nodes) {
        this.nodes = nodes;
    }
}

