package fr.zenika.opensource.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.Project;
import fr.zenika.opensource.stats.beans.github.GitHubProject;
import fr.zenika.opensource.stats.beans.gitlab.GitLabProject;
import fr.zenika.opensource.stats.exception.DatabaseException;
import fr.zenika.opensource.stats.services.FirestoreServices;
import fr.zenika.opensource.stats.services.GitHubServices;
import fr.zenika.opensource.stats.services.GitLabServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectsTab {

    private static final Logger LOG = Logger.getLogger(ProjectsTab.class);

    @Inject
    GitHubServices gitHubServices;

    @Inject
    GitLabServices gitLabServices;

    @Inject
    FirestoreServices firestoreServices;

    @ConfigProperty(name = "oss.stats.sync.buttons.enabled", defaultValue = "false")
    boolean syncButtonsEnabled;

    private String projectSearchTerm = "";
    private String projectSortColumn = "Name";
    private boolean projectSortAscending = true;

    public void render(JtContainer projectsTab) {
        try {
            List<Project> allProjects = firestoreServices.getAllProjects();
            List<Project> memberProjects = allProjects.stream()
                    .filter(p -> !"GitHub Organization".equals(p.getSource()))
                    .collect(Collectors.toList());

            var columns = Jt.columns(2).key("projects_columns").use(projectsTab);
            Jt.subheader("Members Projects (" + memberProjects.size() + ")").use(columns.col(0));

            if (syncButtonsEnabled) {
                if (Jt.button("🚀 Sync Personal Projects").use(columns.col(1))) {
                    try {
                        firestoreServices.deleteAllGitHubProjects();
                        firestoreServices.deleteAllGitLabProjects();
                        List<Member> members = firestoreServices.getAllMembers();
                        int totalGitHubProjects = 0;
                        int totalGitLabProjects = 0;

                        for (Member member : members) {
                            // GitHub Projects
                            if (member.getGitHubAccount() != null) {
                                List<GitHubProject> gitHubProjects = gitHubServices
                                        .getPersonalProjectForAnUser(member.getGitHubAccount().getLogin());
                                for (GitHubProject project : gitHubProjects) {
                                    firestoreServices.createProject(project);
                                }
                                totalGitHubProjects += gitHubProjects.size();
                            }

                            // GitLab Projects
                            if (member.getGitlabAccount() != null &&
                                    member.getGitlabAccount().getUsername() != null) {
                                List<GitLabProject> gitLabProjects = gitLabServices
                                        .getPersonalProjectsForAnUser(member.getGitlabAccount().getUsername());
                                for (GitLabProject gitLabProject : gitLabProjects) {
                                    firestoreServices.createProject(gitLabProject);
                                }
                                totalGitLabProjects += gitLabProjects.size();
                            }
                        }
                        Jt.success("Successfully synced " + (totalGitHubProjects + totalGitLabProjects)
                                + " projects (GitHub: " + totalGitHubProjects + ", GitLab: " + totalGitLabProjects
                                + ") for " + members.size() + " members!")
                                .use(projectsTab);
                    } catch (DatabaseException e) {
                        Jt.error("Error syncing projects: " + e.getMessage()).use(projectsTab);
                    }
                }
            }

            if (!memberProjects.isEmpty()) {
                // Search Bar
                var searchRow = Jt.columns(2).key("members_projects_search").use(projectsTab);
                projectSearchTerm = Jt.textInput("Search Projects").key("members_projects_search_input")
                        .value(projectSearchTerm).use(searchRow.col(0));

                List<Project> filteredProjects = memberProjects.stream()
                        .filter(p -> matchesProject(p, projectSearchTerm))
                        .collect(Collectors.toList());

                // Sort
                sortProjects(filteredProjects);

                record ProjectDisplay(String Name, String Full_Name, String URL, Long Stars, Long Forks,
                        String Source) {
                }
                List<ProjectDisplay> rows = filteredProjects.stream()
                        .map(p -> new ProjectDisplay(p.getName(), p.getFull_name(), p.getHtml_url(),
                                p.getWatchers_count(), p.getForks(), p.getSource()))
                        .collect(Collectors.toList());

                Jt.table(rows).key("member_projects_table").use(projectsTab);

            } else {
                Jt.text("no data available").use(projectsTab);
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
