package zenika.oss.stats.beans.gitlab.graphql;

import java.util.List;

public class GitLabUsersNodes {
    private List<GitLabUserNode> nodes;

    public List<GitLabUserNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GitLabUserNode> nodes) {
        this.nodes = nodes;
    }
}
