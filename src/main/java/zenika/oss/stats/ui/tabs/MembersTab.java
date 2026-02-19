package zenika.oss.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.mapper.ZenikaMemberMapper;
import zenika.oss.stats.services.FirestoreServices;
import zenika.oss.stats.services.GitHubServices;
import zenika.oss.stats.services.GitLabServices;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class MembersTab {

    private static final Logger LOG = Logger.getLogger(MembersTab.class);

    @Inject
    GitHubServices gitHubServices;

    @Inject
    FirestoreServices firestoreServices;

    @Inject
    GitLabServices gitLabServices;

    private String selectedMemberId;
    private String memberSortColumn = "Firstname";
    private boolean memberSortAscending = true;

    public void render(JtContainer membersTab) {
        try {
            // Inject CSS for red buttons and vertical alignment
            Jt.markdown("""
                    <style>
                    button[id^="btn_save_"], button[id^="btn_cancel_"] {
                        background-color: #d32f2f !important;
                        color: white !important;
                        border: none;
                        padding: 8px 16px;
                        border-radius: 4px;
                        cursor: pointer;
                        transition: background-color 0.3s;
                    }
                    button[id^="btn_save_"]:hover, button[id^="btn_cancel_"]:hover {
                        background-color: #b71c1c !important;
                    }
                    button[id^="btn_save_"]:active, button[id^="btn_cancel_"]:active {
                        background-color: #a93226 !important;
                    }
                    div[id^="edit_row_"] {
                        align-items: center !important;
                    }
                    </style>
                    """).use(membersTab);

            List<ZenikaMember> allMembers = firestoreServices.getAllMembers();
            // Create a new list to avoid modifying the cached one and ensure uniqueness
            List<ZenikaMember> members = allMembers.stream()
                    .filter(m -> m.getId() != null)
                    .collect(Collectors.toMap(
                            ZenikaMember::getId,
                            m -> m,
                            (existing, replacement) -> existing))
                    .values().stream()
                    .collect(Collectors.toList());

            var columns = Jt.columns(2).key("members_columns").use(membersTab);

            Jt.subheader("Zenika Members (" + members.size() + ")").use(columns.col(0));

            if (Jt.button("🔄 Sync Members from GitHub").use(columns.col(1))) {
                try {
                    // Load current state from Firestore and GitHub
                    List<ZenikaMember> existingMembers = firestoreServices.getAllMembers();
                    List<GitHubMember> gitHubMembers = gitHubServices.getZenikaOpenSourceMembers();

                    Set<String> currentGitHubLogins = gitHubMembers.stream()
                            .map(GitHubMember::getLogin)
                            .collect(Collectors.toSet());

                    int added = 0;
                    int updated = 0;
                    int removed = 0;

                    // Upsert: add/update members based on GitHub organization
                    for (GitHubMember gitHubMember : gitHubMembers) {
                        ZenikaMember existing = existingMembers.stream()
                                .filter(m -> m.getGitHubAccount() != null
                                        && gitHubMember.getLogin().equals(m.getGitHubAccount().getLogin()))
                                .findFirst()
                                .orElse(null);

                        if (existing == null) {
                            firestoreServices.createMember(
                                    ZenikaMemberMapper.mapGitHubMemberToZenikaMember(gitHubMember));
                            added++;
                        } else {
                            existing.setGitHubAccount(gitHubMember);
                            firestoreServices.createMember(existing);
                            updated++;
                        }
                    }

                    // Remove members that are no longer in the GitHub organization
                    for (ZenikaMember member : existingMembers) {
                        if (member.getGitHubAccount() != null
                                && !currentGitHubLogins.contains(member.getGitHubAccount().getLogin())) {
                            firestoreServices.deleteMember(member.getId());
                            removed++;
                        }
                    }

                    Jt.success("✅ Synchronization completed: " + added + " added, " + updated + " updated, " + removed
                            + " removed.").use(membersTab);
                } catch (DatabaseException e) {
                    Jt.error("Error syncing members: " + e.getMessage()).use(membersTab);
                }
            }

            Jt.markdown("<br/>").use(membersTab);

            // Sort
            sortMembers(members);

            var header = Jt.columns(6).key("member_header").use(membersTab);

            if (Jt.button(getMemberSortLabel("Firstname")).use(header.col(0))) {
                toggleMemberSort("Firstname");
            }
            if (Jt.button(getMemberSortLabel("Lastname")).use(header.col(1))) {
                toggleMemberSort("Lastname");
            }
            Jt.text("GitHub").use(header.col(2));
            Jt.text("GitLab").use(header.col(3));
            if (Jt.button(getMemberSortLabel("City")).use(header.col(4))) {
                toggleMemberSort("City");
            }
            Jt.text("Actions").use(header.col(5));

            for (ZenikaMember m : members) {
                var row = Jt.columns(6).key("member_row_" + m.getId()).use(membersTab);
                Jt.text(m.getFirstname() != null ? m.getFirstname() : "").use(row.col(0));
                Jt.text(m.getName() != null ? m.getName() : "").use(row.col(1));

                if (m.getGitHubAccount() != null && m.getGitHubAccount().getLogin() != null) {
                    String githubLogin = m.getGitHubAccount().getLogin();
                    String githubLinkMarkdown = "<a href=\"https://github.com/" + githubLogin
                            + "\" target=\"_blank\" rel=\"noopener noreferrer\">"
                            + githubLogin + "</a>";
                    Jt.markdown(githubLinkMarkdown).use(row.col(2));
                } else {
                    Jt.text("").use(row.col(2));
                }

                if (m.getGitlabAccount() != null && m.getGitlabAccount().getUsername() != null) {
                    String gitlabUsername = m.getGitlabAccount().getUsername();
                    String gitlabLinkMarkdown = "<a href=\"https://gitlab.com/" + gitlabUsername
                            + "\" target=\"_blank\" rel=\"noopener noreferrer\">"
                            + gitlabUsername + "</a>";
                    Jt.markdown(gitlabLinkMarkdown).use(row.col(3));
                } else {
                    Jt.text("").use(row.col(3));
                }
                Jt.text(m.getCity() != null ? m.getCity() : "").use(row.col(4));
                if (Jt.button("📝").key("btn_edit_" + m.getId()).use(row.col(5))) {
                    selectedMemberId = m.getId();
                }

                if (m.getId().equals(selectedMemberId)) {
                    var editRow = Jt.columns(6).key("edit_row_" + m.getId()).use(membersTab);

                    String newFirstname = Jt.textInput("Firstname").value(m.getFirstname())
                            .use(editRow.col(0));
                    String newName = Jt.textInput("Name").value(m.getName()).use(editRow.col(1));
                    String newGitLabHandle = Jt.textInput("GitLab")
                            .value(m.getGitlabAccount() != null ? m.getGitlabAccount().getUsername() : "")
                            .use(editRow.col(2));
                    String newCity = Jt.textInput("City").value(m.getCity()).use(editRow.col(3));

                    if (Jt.button("Save").key("btn_save_" + m.getId()).use(editRow.col(4))) {
                        m.setFirstname(newFirstname);
                        m.setName(newName);
                        if (newGitLabHandle != null && !newGitLabHandle.isEmpty()) {
                            // Fetch GitLab user info if:
                            // 1. Account is new
                            // 2. Handle has changed
                            // 3. Current ID is missing or not numeric
                            boolean shouldFetch = m.getGitlabAccount() == null
                                    || !newGitLabHandle.equals(m.getGitlabAccount().getUsername())
                                    || m.getGitlabAccount().getId() == null
                                    || !m.getGitlabAccount().getId().matches("\\d+");

                            if (shouldFetch) {
                                // Fetch GitLab user ID (returns object with numeric ID and handle)
                                gitLabServices.getUserInformation(newGitLabHandle).ifPresentOrElse(glUser -> {
                                    m.setGitlabAccount(glUser);
                                    LOG.info("Successfully fetched GitLab ID " + glUser.getId() + " for "
                                            + newGitLabHandle);
                                }, () -> {
                                    LOG.warn("Could not find GitLab user for handle: " + newGitLabHandle);
                                    // Fallback: at least save the handle if fetch fails
                                    if (m.getGitlabAccount() == null)
                                        m.setGitlabAccount(new zenika.oss.stats.beans.gitlab.GitLabMember());
                                    m.getGitlabAccount().setUsername(newGitLabHandle);
                                });
                            }
                        } else {
                            m.setGitlabAccount(null);
                        }
                        m.setCity(newCity);
                        firestoreServices.createMember(m);
                        selectedMemberId = null;
                        Jt.success("✅ Successfully updated").use(membersTab);
                        Jt.markdown("<style>#edit_row_" + m.getId() + " { display: none !important; }</style>")
                                .use(membersTab);
                    }
                    if (Jt.button("Cancel").key("btn_cancel_" + m.getId()).use(editRow.col(5))) {
                        selectedMemberId = null;
                        Jt.markdown("<style>#edit_row_" + m.getId() + " { display: none !important; }</style>")
                                .use(membersTab);
                    }
                }
            }

        } catch (Exception e) {
            Jt.warning("Could not load current members: " + e.getMessage()).use(membersTab);
            LOG.error("Could not load current members", e);
        }
    }

    private void toggleMemberSort(String column) {
        if (memberSortColumn.equals(column)) {
            memberSortAscending = !memberSortAscending;
        } else {
            memberSortColumn = column;
            memberSortAscending = true;
        }
    }

    private String getMemberSortLabel(String column) {
        if (memberSortColumn.equals(column)) {
            return column + (memberSortAscending ? " ▲" : " ▼");
        }
        return column;
    }

    private void sortMembers(List<ZenikaMember> members) {
        Comparator<ZenikaMember> comparator = switch (memberSortColumn) {
            case "Firstname" ->
                Comparator.comparing(m -> m.getFirstname() != null ? m.getFirstname().toLowerCase() : "");
            case "Lastname" -> Comparator.comparing(m -> m.getName() != null ? m.getName().toLowerCase() : "");
            case "City" -> Comparator.comparing(m -> m.getCity() != null ? m.getCity().toLowerCase() : "");
            default -> Comparator.comparing(m -> m.getFirstname() != null ? m.getFirstname().toLowerCase() : "");
        };

        if (!memberSortAscending) {
            comparator = comparator.reversed();
        }
        members.sort(comparator);
    }
}
