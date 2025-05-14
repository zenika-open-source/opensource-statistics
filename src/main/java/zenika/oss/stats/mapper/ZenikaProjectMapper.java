package zenika.oss.stats.mapper;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import zenika.oss.stats.beans.github.GitHubProject;

public class ZenikaProjectMapper {
    public static GitHubProject mapFirestoreZenikaProjectToGitHubProject(QueryDocumentSnapshot queryDocumentSnapshot) {
        GitHubProject project = new GitHubProject();

        project.setArchived(queryDocumentSnapshot.getBoolean("archived"));
        project.setFork(queryDocumentSnapshot.getBoolean("fork"));
        project.setId(queryDocumentSnapshot.getString("id"));
        project.setName(queryDocumentSnapshot.getString("name"));
        project.setFull_name(queryDocumentSnapshot.getString("full_name"));
        project.setHtml_url(queryDocumentSnapshot.getString("html_url"));
        project.setVisibility(queryDocumentSnapshot.getString("visibility"));
        project.setWatchers_count(queryDocumentSnapshot.getLong("watchers_count"));

        return project;
    }
}
