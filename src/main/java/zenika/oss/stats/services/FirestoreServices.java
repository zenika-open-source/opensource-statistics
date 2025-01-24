package zenika.oss.stats.services;

import com.google.cloud.firestore.Firestore;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.ZenikaMember;

public class FirestoreServices {

    @Inject
    Firestore firestore;

    /**
     * Create one member in the firestore collection "members".
     * @param zMember the member to create.
     * @return the member created.
     */
    public void createMember(ZenikaMember zMember) {
        return firestore.collection("members").document(zMember.getId()).set(zMember);
    }


}
