package fr.zenika.opensource.stats.mapper;

import com.google.cloud.firestore.QueryDocumentSnapshot;

import fr.zenika.opensource.stats.beans.Member;
import fr.zenika.opensource.stats.beans.github.GitHubMember;
import fr.zenika.opensource.stats.beans.gitlab.GitLabMember;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemberMapper {

    /**
     * Map a document representation of a Member to a Member.
     *
     * @param documentMember : the document to map.
     * @return a Member.
     */
    public static Member mapFirestoreMemberToMember(QueryDocumentSnapshot documentMember) {
        Member member = new Member();

        member.setFirstname(documentMember.getString("firstname"));
        member.setName(documentMember.getString("name"));
        member.setCity(documentMember.getString("city"));
        member.setId(documentMember.getId());
        member.setGitHubAccount(documentMember.get("gitHubAccount", GitHubMember.class));
        member.setGitlabAccount(documentMember.get("gitlabAccount", GitLabMember.class));

        return member;

    }

    /**
     * Map a Member to a Firestore Map.
     * 
     * @param member : the member to map.
     * @return a map for Firestore.
     */
    public static Map<String, Object> mapMemberToMap(Member member) {
        Map<String, Object> map = new HashMap<>();

        map.put("firstname", member.getFirstname());
        map.put("name", member.getName());
        map.put("city", member.getCity());
        map.put("gitHubAccount", member.getGitHubAccount());
        map.put("gitlabAccount", member.getGitlabAccount());

        return map;
    }

    /**
     * Create a Member from a GitHub Member.
     * 
     * @param gitHubMember : the github Member to map
     * @return an instance of Member
     */
    public static Member mapGitHubMemberToMember(GitHubMember gitHubMember) {
        Member member = new Member();

        member.setId(UUID.randomUUID().toString());
        member.setGitHubAccount(gitHubMember);

        return member;
    }
}
