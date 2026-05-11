package fr.zenika.opensource.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.github.GitHubMember;
import fr.zenika.opensource.stats.exception.DatabaseException;
import fr.zenika.opensource.stats.mapper.MemberMapper;
import fr.zenika.opensource.stats.services.FirestoreServices;
import fr.zenika.opensource.stats.services.GitHubServices;
import fr.zenika.opensource.stats.services.GitLabServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

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

    @ConfigProperty(name = "oss.stats.sync.buttons.enabled", defaultValue = "false")
    boolean syncButtonsEnabled;

    private String selectedMemberId;
    private String memberSortColumn = "Firstname";
    private boolean memberSortAscending = true;

    public void render(JtContainer membersTab) {
        try {
            List<Member> allMembers = firestoreServices.getAllMembers();
            // Create a new list to avoid modifying the cached one and ensure uniqueness
            List<Member> members = allMembers.stream()
                    .filter(m -> m.getId() != null)
                    .collect(Collectors.toMap(
                            Member::getId,
                            m -> m,
                            (existing, replacement) -> existing))
                    .values().stream()
                    .collect(Collectors.toList());

            var columns = Jt.columns(2).key("members_columns").use(membersTab);

            Jt.subheader("Members (" + members.size() + ")").use(columns.col(0));

            if (syncButtonsEnabled) {
                if (Jt.button("🔄 Sync Members from GitHub").use(columns.col(1))) {
                    try {
                        // Load current state from Firestore and GitHub
                        List<Member> existingMembers = firestoreServices.getAllMembers();
                        List<GitHubMember> gitHubMembers = gitHubServices.getOrganizationMembersFromConfig();

                        Set<String> currentGitHubLogins = gitHubMembers.stream()
                                .map(GitHubMember::getLogin)
                                .collect(Collectors.toSet());

                        int added = 0;
                        int updated = 0;
                        int removed = 0;

                        // Upsert: add/update members based on GitHub organization
                        for (GitHubMember gitHubMember : gitHubMembers) {
                            Member existing = existingMembers.stream()
                                    .filter(m -> m.getGitHubAccount() != null
                                            && gitHubMember.getLogin().equals(m.getGitHubAccount().getLogin()))
                                    .findFirst()
                                    .orElse(null);

                            if (existing == null) {
                                firestoreServices.createMember(
                                        MemberMapper.mapGitHubMemberToMember(gitHubMember));
                                added++;
                            } else {
                                existing.setGitHubAccount(gitHubMember);
                                firestoreServices.createMember(existing);
                                updated++;
                            }
                        }

                        // Remove members that are no longer in the GitHub organization
                        for (Member member : existingMembers) {
                            if (member.getGitHubAccount() != null
                                    && !currentGitHubLogins.contains(member.getGitHubAccount().getLogin())) {
                                firestoreServices.deleteMember(member.getId());
                                removed++;
                            }
                        }

                        Jt.success(
                                "✅ Synchronization completed: " + added + " added, " + updated + " updated, " + removed
                                        + " removed.")
                                .use(membersTab);
                    } catch (DatabaseException e) {
                        Jt.error("Error syncing members: " + e.getMessage()).use(membersTab);
                    }
                }
            }

            Jt.markdown("<br/>").use(membersTab);

            // Sort
            sortMembers(members);

            if (selectedMemberId == null) {
                // Inject CSS to perfectly match native Jt.table
                Jt.markdown("""
                        <style>
                        [id$="_header"] {
                            background-color: #f9fafb !important;
                            font-weight: bold !important;
                            border: 1px solid #e5e7eb !important;
                            border-bottom: 2px solid #e5e7eb !important;
                            padding: 12px 15px !important;
                        }
                        [id$="_header"] button {
                            font-weight: bold !important;
                            background: transparent !important;
                            border: none !important;
                            padding: 0 !important;
                            font-size: 1rem !important;
                            cursor: pointer;
                        }
                        [id*="_row_"] {
                            border-bottom: 1px solid #e5e7eb !important;
                            border-left: 1px solid #e5e7eb !important;
                            border-right: 1px solid #e5e7eb !important;
                            padding: 10px 15px !important;
                            background-color: white !important;
                            align-items: center !important;
                        }
                        [id*="_row_"]:hover {
                            background-color: #f9fafb !important;
                        }
                        </style>
                        """).use(membersTab);

                int numCols = syncButtonsEnabled ? 6 : 5;
                var header = Jt.columns(numCols).key("member_header").use(membersTab);

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
                if (syncButtonsEnabled) {
                    Jt.text("Actions").use(header.col(5));
                }

                for (Member m : members) {
                    var row = Jt.columns(numCols).key("member_row_" + m.getId()).use(membersTab);
                    Jt.text(m.getFirstname() != null ? m.getFirstname() : "").use(row.col(0));
                    Jt.text(m.getName() != null ? m.getName() : "").use(row.col(1));
                    Jt.text(m.getGitHubAccount() != null && m.getGitHubAccount().getLogin() != null
                            ? m.getGitHubAccount().getLogin()
                            : "").use(row.col(2));
                    Jt.text(m.getGitlabAccount() != null && m.getGitlabAccount().getUsername() != null
                            ? m.getGitlabAccount().getUsername()
                            : "").use(row.col(3));
                    Jt.text(m.getCity() != null ? m.getCity() : "").use(row.col(4));

                    if (syncButtonsEnabled) {
                        if (Jt.button("📝 Edit").key("btn_edit_" + m.getId()).use(row.col(5))) {
                            selectedMemberId = m.getId();
                        }
                    }
                }
            } else {
                // Show ONLY the Edit Form
                Member memberToEdit = members.stream()
                        .filter(m -> m.getId().equals(selectedMemberId))
                        .findFirst().orElse(null);

                if (memberToEdit != null) {
                    String editName = (memberToEdit.getFirstname() != null ? memberToEdit.getFirstname() : "") + " " +
                            (memberToEdit.getName() != null ? memberToEdit.getName() : "");
                    Jt.subheader("Editing: " + editName.trim()).use(membersTab);

                    var editRow = Jt.columns(4).use(membersTab);

                    String newFirstname = Jt.textInput("Firstname")
                            .value(memberToEdit.getFirstname() != null ? memberToEdit.getFirstname() : "")
                            .use(editRow.col(0));
                    String newName = Jt.textInput("Name")
                            .value(memberToEdit.getName() != null ? memberToEdit.getName() : "").use(editRow.col(1));
                    String newGitLab = Jt.textInput("GitLab")
                            .value(memberToEdit.getGitlabAccount() != null
                                    && memberToEdit.getGitlabAccount().getUsername() != null
                                            ? memberToEdit.getGitlabAccount().getUsername()
                                            : "")
                            .use(editRow.col(2));
                    String newCity = Jt.textInput("City")
                            .value(memberToEdit.getCity() != null ? memberToEdit.getCity() : "").use(editRow.col(3));

                    var actionsRow = Jt.columns(2).use(membersTab);
                    if (Jt.button("Save Changes").use(actionsRow.col(0))) {
                        memberToEdit.setFirstname(newFirstname);
                        memberToEdit.setName(newName);
                        memberToEdit.setCity(newCity);
                        if (newGitLab != null && !newGitLab.equals(
                                memberToEdit.getGitlabAccount() != null ? memberToEdit.getGitlabAccount().getUsername()
                                        : "")) {
                            gitLabServices.getUserInformation(newGitLab).ifPresentOrElse(memberToEdit::setGitlabAccount,
                                    () -> {
                                        if (memberToEdit.getGitlabAccount() == null)
                                            memberToEdit
                                                    .setGitlabAccount(
                                                            new fr.zenika.opensource.stats.beans.gitlab.GitLabMember());
                                        memberToEdit.getGitlabAccount().setUsername(newGitLab);
                                    });
                        }
                        firestoreServices.createMember(memberToEdit);
                        Jt.success("Member updated!").use(membersTab);
                        selectedMemberId = null;
                        Jt.rerun();
                    }
                    if (Jt.button("Cancel").use(actionsRow.col(1))) {
                        selectedMemberId = null;
                        Jt.rerun();
                    }
                    Jt.markdown("</div>").use(membersTab);
                } else {
                    selectedMemberId = null;
                    Jt.rerun();
                }
            }

        } catch (Exception e) {
            if (e.getClass().getName().contains("BreakAndReloadAppException") ||
                    (e.getCause() != null
                            && e.getCause().getClass().getName().contains("BreakAndReloadAppException"))) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
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

    private void sortMembers(List<Member> members) {
        Comparator<Member> comparator = switch (memberSortColumn) {
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
