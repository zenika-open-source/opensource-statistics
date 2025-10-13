package zenika.oss.stats.mcp;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;

@RegisterAiService
public interface GitHubAIService {

    @SystemMessage("You are an open source lovers")
    @UserMessage("""
                Get the GitHub profile url for this user {userHandle}
            """)
    @McpToolBox
    String getGitHubProfile(String userHandle);

}
