package zenika.oss.stats.beans.gitlab.graphql;

import java.util.List;

public class GitLabUserCalendar {
    private List<GitLabCalendarDay> nodes;

    public List<GitLabCalendarDay> getNodes() {
        return nodes;
    }

    public void setNodes(List<GitLabCalendarDay> nodes) {
        this.nodes = nodes;
    }
}
