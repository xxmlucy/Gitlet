package gitlet;



import java.io.File;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;


/**
 * Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 * @author Ziyang Zeng, Xiaomeng Xu
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */

    private final String message;
    private final String timestamp;
    private String parentID = "";
    private String parentID2 = "";
    private String commitID;


    //Reference: https://www.w3schools.com/java/java_hashmap.asp

    private HashMap<String, Blob> blobs = new HashMap<String, Blob>();
    private HashMap<String, String> blobs1 = new HashMap<String, String>();


    //commit constructor
    public Commit(String msg, String par) {
        if (par == null) { //initial commit
            this.timestamp = "Thu Jan 1 00:00:00 1970 -0800"; //default time
        } else {
            ZonedDateTime now = ZonedDateTime.now();
            timestamp = now.format(DateTimeFormatter.ofPattern
                    ("EEE MMM d HH:mm:ss yyyy xxxx", Locale.ENGLISH));

        }
        this.message = msg;

        if (par == null) {
            parentID = "";
        } else {
            this.parentID = par;
        }
        this.commitID = Utils.sha1(Utils.serialize(this));


        if (parentID.equals("")) {
            File parentCommit = Utils.join(Repository.FCOMMIT, parentID);
            Commit parent = Utils.readObject(parentCommit, Commit.class);
            this.blobs = parent.getBlobs(); //preserve previous hashmap
            this.blobs1 = parent.getBlobs1();
        } else {
            blobs = new HashMap<String, Blob>();
//            HashMap<String, String> blobs1 = new HashMap<String, String>();
        }
    }


    public Commit(String timestamp, HashMap<String, Blob> blob,
                  HashMap<String, String> blob1, String msg, String par, String commitid) {
        this.parentID = par;
        this.blobs1 = blob1;
        this.blobs = blob;
        this.timestamp = timestamp;
        this.message = msg;
        this.commitID = commitid;

    }

    public Commit(String msg, String par, String par2,
                  HashMap<String, String> blobs1, HashMap<String, Blob> blobs) {
        if (par == null) { //initial commit
            this.timestamp = "Thu Jan 1 00:00:00 1970 -0800"; //default time
        } else {
            ZonedDateTime now = ZonedDateTime.now();
            timestamp = now.format(DateTimeFormatter.ofPattern
                    ("EEE MMM d HH:mm:ss yyyy xxxx", Locale.ENGLISH));
        }
        this.message = msg;
        if (par == null) {
            parentID = "";
        } else {
            this.parentID = par;
        }
        if (par2 == null) {
            parentID2 = "";
        } else {
            this.parentID2 = par2;
        }

        this.commitID = Utils.sha1(Utils.serialize(this));

        setBlobs(blobs);
        setBlobs1(blobs1);

    }

    public void saveCommit() {
        File saveCommit = Utils.join(Repository.FCOMMIT, commitID);
        Utils.writeObject(saveCommit, this);
    }

    public String getCommitID() {
        return this.commitID;
    }

    public String getParentID() {
        return this.parentID;
    }

    public void setParentID(String id) {
        this.commitID = id;
    }

    public HashMap<String, Blob> getBlobs() {
        return this.blobs;
    }

    public void setBlobs(HashMap<String, Blob> blobs) {
        this.blobs = blobs;
    }

    public HashMap<String, String> getBlobs1() {
        return this.blobs1;
    }

    public void setBlobs1(HashMap<String, String> blobs1) {
        this.blobs1 = blobs1;
    }

    public void addBlobs(String blobid, Blob blob) {
        if (blobs == null) {
            blobs = new HashMap<String, Blob>();
        }
        blobs.put(blobid, blob);
    }

    public void addBlobs1(File f, String blobid) {
        if (blobs1 == null) {
            blobs1 = new HashMap<String, String>();
        }
        blobs1.put(f.getName(), blobid);
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getMessage() {
        return this.message;
    }

    public String getParentID2() {
        return this.parentID2;
    }

    public void setParentID2(String id) {
        this.parentID2 = id;
    }
}
