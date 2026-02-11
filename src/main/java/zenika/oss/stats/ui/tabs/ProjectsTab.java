package zenika.oss.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubProject;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectsTab {

    private static final Logger LOG = Logger.getLogger(ProjectsTab.class);

    @Inject
    GitHubServices gitHubServices;

    @Inject
    FirestoreServices firestoreServices;

    private String projectSearchTerm = "";
    private String projectSortColumn = "Name";
    private boolean projectSortAscending = true;

    public void render(JtContainer projectsTab) {
        try {
            List<GitHubProject> allProjects = firestoreServices.getAllProjects();

            var columns = Jt.columns(2).key("projects_columns").use(projectsTab);
            Jt.subheader("User Projects (" + allProjects.size() + ")").use(columns.col(0));

            if (Jt.button("🚀 Sync Personal Projects").use(columns.col(1))) {
                try {
                    firestoreServices.deleteAllProjects();
                    List<ZenikaMember> members = firestoreServices.getAllMembers();
                    int totalProjects = 0;
                    for (ZenikaMember member : members) {
                        if (member.getGitHubAccount() != null) {
                            List<GitHubProject> gitHubProjects = gitHubServices
                                    .getPersonalProjectForAnUser(member.getGitHubAccount().getLogin());
                            for (GitHubProject project : gitHubProjects) {
                                firestoreServices.createProject(project);
                            }
                            totalProjects += gitHubProjects.size();
                        }
                    }
                    Jt.success("Successfully synced " + totalProjects + " projects for " + members.size() + " members!")
                            .use(projectsTab);
                } catch (DatabaseException e) {
                    Jt.error("Error syncing projects: " + e.getMessage()).use(projectsTab);
                }
            }

            if (!allProjects.isEmpty()) {
                // Search Bar
                var searchRow = Jt.columns(2).use(projectsTab);
                projectSearchTerm = Jt.textInput("Search Projects").value(projectSearchTerm).use(searchRow.col(0));
                
                List<GitHubProject> filteredProjects = allProjects.stream()
                    .filter(p -> matchesProject(p, projectSearchTerm))
                    .collect(Collectors.toList());

                // Sort
                sortProjects(filteredProjects);

                record ProjectDisplay(String id, String name, String fullName, String url, Long stars, Long forks) {
                }
                List<ProjectDisplay> rows = filteredProjects.stream()
                        .map(p -> new ProjectDisplay(p.getId(), p.getName(), p.getFull_name(), p.getHtml_url(),
                                p.getWatchers_count(), p.getForks()))
                        .collect(Collectors.toList());

                Jt.subheader("Projects List").use(projectsTab);
                
                // Custom Header with Sort Buttons
                var header = Jt.columns(5).key("projects_header").use(projectsTab);
                
                if (Jt.button(getSortLabel("Name")).use(header.col(0))) {
                    toggleSort("Name");
                }
                Jt.text("Full Name").use(header.col(1));
                Jt.text("URL").use(header.col(2));
                if (Jt.button(getSortLabel("Stars")).use(header.col(3))) {
                    toggleSort("Stars");
                }
                if (Jt.button(getSortLabel("Forks")).use(header.col(4))) {
                    toggleSort("Forks");
                }

                // Custom Table Rows (replacing Jt.table to match header)
                for (ProjectDisplay p : rows) {
                    var row = Jt.columns(5).key("project_row_" + p.id()).use(projectsTab);
                    Jt.text(p.name()).use(row.col(0));
                    Jt.text(p.fullName()).use(row.col(1));
                    Jt.text(p.url()).use(row.col(2));
                    Jt.text(String.valueOf(p.stars())).use(row.col(3));
                    Jt.text(String.valueOf(p.forks())).use(row.col(4));
                }

            } else {
                Jt.text("no data available").use(projectsTab);
            }

            Jt.text("Fetch and save personal projects (not forked) for all saved members.").use(projectsTab);

        } catch (Exception e) {
            Jt.warning("Could not load current projects: " + e.getMessage()).use(projectsTab);
            LOG.error("Could not load current projects", e);
        }
    }

    private boolean matchesProject(GitHubProject p, String term) {
        if (term == null || term.isBlank()) return true;
        String lowerTerm = term.toLowerCase();
        return (p.getName() != null && p.getName().toLowerCase().contains(lowerTerm)) ||
               (p.getFull_name() != null && p.getFull_name().toLowerCase().contains(lowerTerm)) ||
               (p.getHtml_url() != null && p.getHtml_url().toLowerCase().contains(lowerTerm));
    }

    private void toggleSort(String column) {
        if (projectSortColumn.equals(column)) {
            projectSortAscending = !projectSortAscending;
        } else {
            projectSortColumn = column;
            projectSortAscending = true;
        }
    }

    private String getSortLabel(String column) {
        if (projectSortColumn.equals(column)) {
            return column + (projectSortAscending ? " ▲" : " ▼");
        }
        return column;
    }

    private void sortProjects(List<GitHubProject> projects) {
        Comparator<GitHubProject> comparator = switch (projectSortColumn) {
            case "Name" -> Comparator.comparing(p -> p.getName() != null ? p.getName().toLowerCase() : "");
            case "Stars" -> Comparator.comparing(p -> p.getWatchers_count() != null ? p.getWatchers_count() : 0L);
            case "Forks" -> Comparator.comparing(p -> p.getForks() != null ? p.getForks() : 0L);
            default -> Comparator.comparing(p -> p.getName() != null ? p.getName().toLowerCase() : "");
        };

        if (!projectSortAscending) {
            comparator = comparator.reversed();
        }
        projects.sort(comparator);
    }
}
