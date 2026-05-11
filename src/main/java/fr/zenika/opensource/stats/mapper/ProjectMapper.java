package fr.zenika.opensource.stats.mapper;

import com.google.cloud.firestore.QueryDocumentSnapshot;

import fr.zenika.opensource.stats.beans.Project;
import fr.zenika.opensource.stats.beans.github.GitHubProject;
import fr.zenika.opensource.stats.beans.gitlab.GitLabProject;

public class ProjectMapper {
    public static Project mapFirestoreProjectToProject(QueryDocumentSnapshot queryDocumentSnapshot) {
        Project project = new Project();

        project.setId(queryDocumentSnapshot.getString("id"));
        project.setName(queryDocumentSnapshot.getString("name"));
        project.setFull_name(queryDocumentSnapshot.getString("full_name"));
        project.setHtml_url(queryDocumentSnapshot.getString("html_url"));
        project.setWatchers_count(queryDocumentSnapshot.getLong("watchers_count"));
        project.setForks(queryDocumentSnapshot.getLong("forks"));
        project.setSource(queryDocumentSnapshot.getString("source"));
        project.setFork(Boolean.TRUE.equals(queryDocumentSnapshot.getBoolean("fork")));
        project.setArchived(Boolean.TRUE.equals(queryDocumentSnapshot.getBoolean("archived")));
        project.setVisibility(queryDocumentSnapshot.getString("visibility"));

        return project;
    }

    public static GitHubProject mapFirestoreProjectToGitHubProject(QueryDocumentSnapshot queryDocumentSnapshot) {
        Project base = mapFirestoreProjectToProject(queryDocumentSnapshot);
        GitHubProject project = new GitHubProject();
        copyProperties(base, project);
        return project;
    }

    public static GitLabProject mapFirestoreProjectToGitLabProject(QueryDocumentSnapshot queryDocumentSnapshot) {
        Project base = mapFirestoreProjectToProject(queryDocumentSnapshot);
        GitLabProject project = new GitLabProject();
        copyProperties(base, project);
        return project;
    }

    private static void copyProperties(Project source, Project target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setFull_name(source.getFull_name());
        target.setHtml_url(source.getHtml_url());
        target.setWatchers_count(source.getWatchers_count());
        target.setForks(source.getForks());
        target.setSource(source.getSource());
        target.setFork(source.isFork());
        target.setArchived(source.isArchived());
        target.setVisibility(source.getVisibility());
    }
}
