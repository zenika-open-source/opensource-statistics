package fr.zenika.opensource.stats.services;

import fr.zenika.opensource.stats.beans.ContributionDetail;
import fr.zenika.opensource.stats.beans.ExternalContribution;
import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.Project;
import fr.zenika.opensource.stats.beans.gcp.StatsContribution;
import fr.zenika.opensource.stats.exception.DatabaseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ExternalContributionService {

    @Inject
    FirestoreServices firestoreServices;

    @ConfigProperty(name = "organization.name", defaultValue = "zenika-open-source")
    String organizationName;

    @ConfigProperty(name = "organization.display-name", defaultValue = "Zenika")
    String organizationDisplayName;

    public List<ExternalContribution> getExternalContributions(
            Integer year,
            String state,
            String memberId,
            Integer limit) throws DatabaseException {

        List<Member> members = firestoreServices.getAllMembers();
        List<Project> projects = firestoreServices.getAllProjects();

        Set<String> orgPrefixes = getOrgPrefixes();
        Set<String> memberLogins = getMemberLogins(members);
        Set<String> knownInternalFullNames = getKnownInternalFullNames(projects);
        Set<String> knownInternalUrls = getKnownInternalUrls(projects);

        Map<String, Member> membersById = Optional.ofNullable(members).orElse(List.of()).stream()
                .filter(m -> m.getId() != null)
                .collect(Collectors.toMap(Member::getId, m -> m, (existing, replacement) -> existing));

        Map<String, Member> membersByLogin = new HashMap<>();
        for (Member m : Optional.ofNullable(members).orElse(List.of())) {
            Optional.ofNullable(m.getGitHubAccount())
                    .map(gh -> gh.getLogin())
                    .filter(l -> !l.isBlank())
                    .ifPresent(l -> membersByLogin.put(l.toLowerCase().trim(), m));
            Optional.ofNullable(m.getGitlabAccount())
                    .map(gl -> gl.getUsername())
                    .filter(u -> !u.isBlank())
                    .ifPresent(u -> membersByLogin.put(u.toLowerCase().trim(), m));
        }

        List<StatsContribution> stats;
        if (year != null) {
            stats = firestoreServices.getStatsForYear(year);
        } else {
            stats = firestoreServices.getAllStats();
        }

        Optional<String> targetState = Optional.ofNullable(state)
                .map(String::trim)
                .filter(s -> !s.isBlank() && !"ALL".equalsIgnoreCase(s))
                .map(ExternalContribution::normalizeState);

        Optional<String> targetMemberId = Optional.ofNullable(memberId)
                .map(String::trim)
                .filter(s -> !s.isBlank());

        int maxLimit = Optional.ofNullable(limit)
                .filter(l -> l > 0)
                .orElse(Integer.MAX_VALUE);

        Set<String> seenPrUrls = new HashSet<>();
        List<ExternalContribution> result = new ArrayList<>();

        for (StatsContribution stat : Optional.ofNullable(stats).orElse(List.of())) {
            if (stat == null) {
                continue;
            }

            if (targetMemberId.isPresent() && !targetMemberId.get().equals(stat.getIdMember())) {
                continue;
            }

            Member member = Optional.ofNullable(stat.getIdMember())
                    .map(membersById::get)
                    .orElseGet(() -> {
                        String gh = Optional.ofNullable(stat.getGithubHandle()).map(String::toLowerCase).orElse("");
                        if (!gh.isEmpty() && membersByLogin.containsKey(gh)) {
                            return membersByLogin.get(gh);
                        }
                        String gl = Optional.ofNullable(stat.getGitlabHandle()).map(String::toLowerCase).orElse("");
                        if (!gl.isEmpty() && membersByLogin.containsKey(gl)) {
                            return membersByLogin.get(gl);
                        }
                        return null;
                    });

            String memberName = Optional.ofNullable(member)
                    .map(m -> (Optional.ofNullable(m.getFirstname()).orElse("") + " " + Optional.ofNullable(m.getName()).orElse("")).trim())
                    .filter(s -> !s.isBlank())
                    .orElseGet(() -> Optional.ofNullable(stat.getGithubHandle())
                            .filter(s -> !s.isBlank())
                            .orElse(Optional.ofNullable(stat.getGitlabHandle()).orElse("Unknown")));

            String memberHandle = Optional.ofNullable(member)
                    .flatMap(m -> Optional.ofNullable(m.getGitHubAccount()).map(gh -> gh.getLogin())
                            .or(() -> Optional.ofNullable(m.getGitlabAccount()).map(gl -> gl.getUsername())))
                    .orElse(Optional.ofNullable(stat.getGithubHandle()).orElse(Optional.ofNullable(stat.getGitlabHandle()).orElse("")));

            List<ContributionDetail> details = Optional.ofNullable(stat.getContributionsDetails()).orElse(List.of());

            for (ContributionDetail detail : details) {
                if (detail == null) {
                    continue;
                }

                String prUrl = Optional.ofNullable(detail.getPrUrl()).orElse("").trim();
                String projectUrl = Optional.ofNullable(detail.getProjectUrl()).orElse("").trim();
                String projectFullName = Optional.ofNullable(detail.getProjectFullName()).orElse("").trim();
                String projectName = Optional.ofNullable(detail.getProjectName()).orElse("").trim();

                if (projectFullName.isBlank() && !prUrl.isBlank()) {
                    projectFullName = extractProjectFullNameFromPrUrl(prUrl).orElse("");
                }
                if (projectName.isBlank() && !projectFullName.isBlank()) {
                    int slashIdx = projectFullName.lastIndexOf('/');
                    projectName = slashIdx >= 0 ? projectFullName.substring(slashIdx + 1) : projectFullName;
                }
                if (projectUrl.isBlank() && !prUrl.isBlank()) {
                    projectUrl = extractProjectUrlFromPrUrl(prUrl).orElse("");
                }

                if (!isExternalProject(projectFullName, projectUrl, prUrl, orgPrefixes, memberLogins, knownInternalFullNames, knownInternalUrls)) {
                    continue;
                }

                String normState = ExternalContribution.normalizeState(detail.getPrState());
                if (targetState.isPresent() && !targetState.get().equalsIgnoreCase(normState)) {
                    continue;
                }

                if (!prUrl.isBlank()) {
                    if (seenPrUrls.contains(prUrl)) {
                        continue;
                    }
                    seenPrUrls.add(prUrl);
                }

                ExternalContribution contrib = new ExternalContribution(
                        stat.getIdMember(),
                        memberName,
                        memberHandle,
                        projectName,
                        projectFullName,
                        projectUrl,
                        detail.getPrTitle(),
                        prUrl,
                        normState,
                        detail.getCreatedAt(),
                        stat.getSource(),
                        stat.getYear(),
                        stat.getMonth()
                );

                result.add(contrib);
            }
        }

        result.sort((c1, c2) -> {
            String d1 = Optional.ofNullable(c1.getCreatedAt()).orElse("");
            String d2 = Optional.ofNullable(c2.getCreatedAt()).orElse("");
            int cmp = d2.compareTo(d1);
            if (cmp != 0) {
                return cmp;
            }
            String y1 = Optional.ofNullable(c1.getYear()).orElse("");
            String y2 = Optional.ofNullable(c2.getYear()).orElse("");
            int cmpY = y2.compareTo(y1);
            if (cmpY != 0) {
                return cmpY;
            }
            return Optional.ofNullable(c1.getPrTitle()).orElse("").compareTo(Optional.ofNullable(c2.getPrTitle()).orElse(""));
        });

        if (maxLimit < result.size()) {
            return result.subList(0, maxLimit);
        }
        return result;
    }

    public List<ExternalContribution> getExternalContributions(Integer year) throws DatabaseException {
        return getExternalContributions(year, null, null, null);
    }

    public List<ExternalContribution> getAllExternalContributions() throws DatabaseException {
        return getExternalContributions(null, null, null, null);
    }

    public boolean isExternalProject(String projectFullName, String projectUrl, String prUrl,
            Set<String> orgPrefixes, Set<String> memberLogins,
            Set<String> knownInternalFullNames, Set<String> knownInternalUrls) {

        String fullName = Optional.ofNullable(projectFullName).orElse("").trim().toLowerCase();
        String url = Optional.ofNullable(projectUrl).orElse("").trim().toLowerCase().replaceAll("/+$", "");
        String pr = Optional.ofNullable(prUrl).orElse("").trim().toLowerCase().replaceAll("/+$", "");

        if (!fullName.isEmpty() && knownInternalFullNames.contains(fullName)) {
            return false;
        }

        if (!url.isEmpty() && knownInternalUrls.contains(url)) {
            return false;
        }

        if (fullName.contains("/")) {
            String owner = fullName.split("/")[0].trim();
            if (orgPrefixes.contains(owner) || memberLogins.contains(owner)) {
                return false;
            }
        }

        for (String org : orgPrefixes) {
            if ((!url.isEmpty() && (url.contains("/" + org + "/") || url.endsWith("/" + org)))
                    || (!pr.isEmpty() && pr.contains("/" + org + "/"))) {
                return false;
            }
        }

        for (String login : memberLogins) {
            if ((!url.isEmpty() && (url.contains("/" + login + "/") || url.endsWith("/" + login)))
                    || (!pr.isEmpty() && pr.contains("/" + login + "/"))) {
                return false;
            }
        }

        return true;
    }

    public Set<String> getOrgPrefixes() {
        Set<String> prefixes = new HashSet<>();
        Optional.ofNullable(organizationName)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .ifPresent(prefixes::add);
        Optional.ofNullable(organizationDisplayName)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .ifPresent(prefixes::add);
        prefixes.add("zenika");
        prefixes.add("zenika-open-source");
        return prefixes;
    }

    public Set<String> getMemberLogins(List<Member> members) {
        return Optional.ofNullable(members).orElse(List.of()).stream()
                .flatMap(m -> {
                    List<String> logins = new ArrayList<>();
                    Optional.ofNullable(m.getGitHubAccount())
                            .map(gh -> gh.getLogin())
                            .filter(l -> !l.isBlank())
                            .map(String::toLowerCase)
                            .map(String::trim)
                            .ifPresent(logins::add);
                    Optional.ofNullable(m.getGitlabAccount())
                            .map(gl -> gl.getUsername())
                            .filter(u -> !u.isBlank())
                            .map(String::toLowerCase)
                            .map(String::trim)
                            .ifPresent(logins::add);
                    return logins.stream();
                })
                .collect(Collectors.toSet());
    }

    public Set<String> getKnownInternalFullNames(List<Project> projects) {
        return Optional.ofNullable(projects).orElse(List.of()).stream()
                .map(Project::getFull_name)
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    public Set<String> getKnownInternalUrls(List<Project> projects) {
        return Optional.ofNullable(projects).orElse(List.of()).stream()
                .map(Project::getHtml_url)
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .map(s -> s.replaceAll("/+$", ""))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    public Optional<String> extractProjectFullNameFromPrUrl(String prUrl) {
        if (prUrl == null || prUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            if (prUrl.contains("github.com/")) {
                int ghIdx = prUrl.indexOf("github.com/") + 11;
                int pullIdx = prUrl.indexOf("/pull/", ghIdx);
                if (pullIdx > ghIdx) {
                    return Optional.of(prUrl.substring(ghIdx, pullIdx));
                }
            } else if (prUrl.contains("/-/merge_requests/")) {
                URI uri = URI.create(prUrl);
                String path = uri.getPath();
                if (path != null && path.contains("/-/merge_requests/")) {
                    String sub = path.substring(0, path.indexOf("/-/merge_requests/"));
                    return Optional.of(sub.startsWith("/") ? sub.substring(1) : sub);
                }
            }
        } catch (Exception e) {
            // ignore malformed URLs
        }
        return Optional.empty();
    }

    public Optional<String> extractProjectUrlFromPrUrl(String prUrl) {
        return extractProjectFullNameFromPrUrl(prUrl).map(fullName -> {
            if (prUrl.contains("gitlab.com")) {
                return "https://gitlab.com/" + fullName;
            }
            return "https://github.com/" + fullName;
        });
    }

    public String buildSocialPostSnippet(ExternalContribution contrib) {
        if (contrib == null) {
            return "";
        }
        String author = Optional.ofNullable(contrib.getMemberName()).filter(s -> !s.isBlank())
                .orElse(Optional.ofNullable(contrib.getMemberHandle()).orElse("Un membre de l'équipe"));
        String project = Optional.ofNullable(contrib.getProjectFullName()).filter(s -> !s.isBlank())
                .orElse(Optional.ofNullable(contrib.getProjectName()).orElse("un projet open source"));
        String title = Optional.ofNullable(contrib.getPrTitle()).orElse("");
        String url = Optional.ofNullable(contrib.getPrUrl()).orElse("");
        String actionVerb = "MERGED".equalsIgnoreCase(contrib.getPrState())
                ? "a fait merger une contribution sur"
                : "a contribué au projet";

        String tagRepo = project.contains("/") ? project.substring(project.lastIndexOf('/') + 1) : project;
        String hashtag = tagRepo.replaceAll("[^a-zA-Z0-9]", "");

        StringBuilder sb = new StringBuilder();
        sb.append("🎉 Félicitations à ").append(author).append(" qui ").append(actionVerb)
                .append(" ").append(project).append(" !\n\n");
        if (!title.isBlank()) {
            sb.append("💡 PR : « ").append(title).append(" »\n");
        }
        if (!url.isBlank()) {
            sb.append("🔗 ").append(url).append("\n");
        }
        sb.append("\n#OpenSource #Zenika #Tech");
        if (!hashtag.isBlank()) {
            sb.append(" #").append(hashtag);
        }
        return sb.toString();
    }
}
