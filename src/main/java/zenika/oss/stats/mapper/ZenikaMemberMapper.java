package zenika.oss.stats.mapper;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubMember;

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
        //zenikaMember.setId( documentMember.getId());
        zenikaMember.setGitHubAccount(new GitHubMember());
        //zenikaMember.getGitHubAccount().setLogin(documentMember.;

        return zenikaMember;
    }

    /**
     * Create a ZenikaMember from a GitHub Member.
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
