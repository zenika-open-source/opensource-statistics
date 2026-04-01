package zenika.oss.stats.mapper;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubMember;
import zenika.oss.stats.beans.gitlab.GitLabMember;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZenikaMemberMapper {

    /**
     * Map a document representation of a Zenika Member to a ZenikaMember.
     *
     * @param documentMember : the document to map.
     * @return a Zenika Member.
     */
    public static ZenikaMember mapFirestoreZenikaMemberToZenikaMember(QueryDocumentSnapshot documentMember) {
        ZenikaMember zenikaMember = new ZenikaMember();

        zenikaMember.setFirstname(documentMember.getString("firstname"));
        zenikaMember.setName(documentMember.getString("name"));
        zenikaMember.setCity(documentMember.getString("city"));
        zenikaMember.setId(documentMember.getId());
        zenikaMember.setGitHubAccount(documentMember.get("gitHubAccount", GitHubMember.class));
        zenikaMember.setGitlabAccount(documentMember.get("gitlabAccount", GitLabMember.class));

        return zenikaMember;

    }

    /**
     * Map a ZenikaMember to a Firestore Map.
     * 
     * @param member : the member to map.
     * @return a map for Firestore.
     */
    public static Map<String, Object> mapZenikaMemberToMap(ZenikaMember member) {
        Map<String, Object> map = new HashMap<>();

        map.put("firstname", member.getFirstname());
        map.put("name", member.getName());
        map.put("city", member.getCity());
        map.put("gitHubAccount", member.getGitHubAccount());
        map.put("gitlabAccount", member.getGitlabAccount());

        return map;
    }

    /**
     * Create a ZenikaMember from a GitHub Member.
     * 
     * @param gitHubMember : the github Member to map
     * @return an instance of ZenikaMember
     */
    public static ZenikaMember mapGitHubMemberToZenikaMember(GitHubMember gitHubMember) {
        ZenikaMember zenikaMember = new ZenikaMember();

        zenikaMember.setId(UUID.randomUUID().toString());
        zenikaMember.setGitHubAccount(gitHubMember);

        return zenikaMember;
    }
}
