package fr.zenika.opensource.stats.beans.gitlab.graphql;

import java.util.List;

public class GitLabMergeRequestsCount {
    private int count;
    private List<GitLabMergeRequestNode> nodes;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<GitLabMergeRequestNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GitLabMergeRequestNode> nodes) {
        this.nodes = nodes;
    }
}
