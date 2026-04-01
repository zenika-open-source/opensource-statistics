package zenika.oss.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import zenika.oss.stats.beans.Project;
import zenika.oss.stats.beans.github.GitHubProject;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrganizationProjectsTab {

    private static final Logger LOG = Logger.getLogger(OrganizationProjectsTab.class);

    @Inject
    GitHubServices gitHubServices;

    @Inject
    FirestoreServices firestoreServices;

    @ConfigProperty(name = "organization.name")
    String organizationName;

    private String projectSearchTerm = "";
    private String projectSortColumn = "Stars";
    private boolean projectSortAscending = false;

    public void render(JtContainer projectsTab) {
        try {
            List<Project> allProjects = firestoreServices.getAllProjects();
            List<Project> organizationProjects = allProjects.stream()
                    .filter(p -> "GitHub Organization".equals(p.getSource()))
                    .collect(Collectors.toList());

            var columns = Jt.columns(2).key("org_projects_columns").use(projectsTab);
            Jt.subheader("Organization Projects (" + organizationProjects.size() + ")").use(columns.col(0));

            if (Jt.button("🚀 Sync " + organizationName + " Organization Projects").use(columns.col(1))) {
                try {
                    firestoreServices.deleteAllGitHubOrganizationProjects();
                    List<GitHubProject> gitHubProjects = gitHubServices.getOrganizationProjects(organizationName);
                    int totalProjects = 0;
                    for (GitHubProject project : gitHubProjects) {
                        project.setSource("GitHub Organization");
                        firestoreServices.createProject(project);
                        totalProjects++;
                    }
                    Jt.success("Successfully synced " + totalProjects + " projects for " + organizationName
                            + " organization!")
                            .use(projectsTab);
                } catch (DatabaseException e) {
                    Jt.error("Error syncing organization projects: " + e.getMessage()).use(projectsTab);
                }
            }

            if (!organizationProjects.isEmpty()) {
                // Search Bar
                var searchRow = Jt.columns(2).key("org_projects_search").use(projectsTab);
                projectSearchTerm = Jt.textInput("Search Projects").key("org_projects_search_input")
                        .value(projectSearchTerm).use(searchRow.col(0));

                List<Project> filteredProjects = organizationProjects.stream()
                        .filter(p -> matchesProject(p, projectSearchTerm))
                        .collect(Collectors.toList());

                // Sort
                sortProjects(filteredProjects);

                record ProjectDisplay(String id, String name, String fullName, String url, Long stars, Long forks,
                        String source) {
                }
                List<ProjectDisplay> rows = filteredProjects.stream()
                        .map(p -> new ProjectDisplay(p.getId(), p.getName(), p.getFull_name(), p.getHtml_url(),
                                p.getWatchers_count(), p.getForks(), p.getSource()))
                        .collect(Collectors.toList());

                // Custom Header with Sort Buttons
                var header = Jt.columns(6).key("org_projects_header").use(projectsTab);

                if (Jt.button(getSortLabel("Name")).key("org_sort_name").use(header.col(0))) {
                    toggleSort("Name");
                }
                Jt.text("Full Name").use(header.col(1));
                Jt.text("URL").use(header.col(2));
                if (Jt.button(getSortLabel("Stars")).key("org_sort_stars").use(header.col(3))) {
                    toggleSort("Stars");
                }
                if (Jt.button(getSortLabel("Forks")).key("org_sort_forks").use(header.col(4))) {
                    toggleSort("Forks");
                }
                Jt.text("Source").use(header.col(5));

                // Custom Table Rows
                for (ProjectDisplay p : rows) {
                    var row = Jt.columns(6).key("org_project_row_" + p.id()).use(projectsTab);
                    Jt.text(p.name()).use(row.col(0));
                    Jt.text(p.fullName()).use(row.col(1));

                    String linkMarkdown = "<a href=\"" + p.url() + "\" target=\"_blank\" rel=\"noopener noreferrer\">"
                            + p.url() + "</a>";
                    Jt.markdown(linkMarkdown).use(row.col(2));

                    Jt.text(String.valueOf(p.stars())).use(row.col(3));
                    Jt.text(String.valueOf(p.forks())).use(row.col(4));
                    Jt.text(p.source() != null ? p.source() : "").use(row.col(5));
                }

            } else {
                Jt.text("No data available").use(projectsTab);
            }

        } catch (Exception e) {
            Jt.warning("Could not load current projects: " + e.getMessage()).use(projectsTab);
            LOG.error("Could not load current projects", e);
        }
    }

    private boolean matchesProject(Project p, String term) {
        if (term == null || term.isBlank())
            return true;
        String lowerTerm = term.toLowerCase();
        return (p.getName() != null && p.getName().toLowerCase().contains(lowerTerm)) ||
                (p.getFull_name() != null && p.getFull_name().toLowerCase().contains(lowerTerm)) ||
                (p.getHtml_url() != null && p.getHtml_url().toLowerCase().contains(lowerTerm)) ||
                (p.getSource() != null && p.getSource().toLowerCase().contains(lowerTerm));
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

    private void sortProjects(List<Project> projects) {
        Comparator<Project> comparator = switch (projectSortColumn) {
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
