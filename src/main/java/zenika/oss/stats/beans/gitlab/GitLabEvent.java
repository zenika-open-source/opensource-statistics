package zenika.oss.stats.beans.gitlab;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitLabEvent {
    private String id;
    private String actionName;
    private String createdAt;

    @JsonProperty("action_name")
    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
