package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    private final byte[] content;
    private final String blobID;
    private final String name;

    public Blob(String name) {
        this.name = name;
        File f = Utils.join(Repository.CWD, name);
        this.content = Utils.readContents(f);
        this.blobID = Utils.sha1(name + new String(content));

    }


    public void saveBlobs() {
        File saveBlob = Utils.join(Repository.FBLOBS, this.blobID);
        if (!saveBlob.exists()) {
            Utils.writeObject(saveBlob, this);
        } else {
            Utils.join(Repository.FSTAGING, name).delete();
        }
    }

    public String getBlobID() {
        return this.blobID;
    }

    public byte[] getContent() {
        return this.content;
    }
}
