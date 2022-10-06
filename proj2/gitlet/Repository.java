package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 *  does at a high level.
 *
 * @author Xiaomeng Xu, Ziyang Zeng
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File FSTAGING = join(GITLET_DIR, "staging");
    public static final File FCOMMIT = join(GITLET_DIR, "commit");
    public static final File FBLOBS = join(GITLET_DIR, "blobs");
    public static final File FHEAD = join(GITLET_DIR, "head");
    public static final File FREMOVE = join(GITLET_DIR, "remove");
    public static final File FBRANCH = join(GITLET_DIR, "branch");
    private File HEAD = join(FHEAD, "head");
    private File MAIN = join(FBRANCH, "main");


    public void init() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        if (!FSTAGING.exists()) {
            FSTAGING.mkdir();
        }
        if (!FCOMMIT.exists()) {
            FCOMMIT.mkdir();
        }
        if (!FBLOBS.exists()) {
            FBLOBS.mkdir();
        }
        if (!FHEAD.exists()) {
            FHEAD.mkdir();
        }
        if (!FREMOVE.exists()) {
            FREMOVE.mkdir();
        }
        if (!FBRANCH.exists()) {
            FBRANCH.mkdir();
        }
        Commit currCommit = new Commit("initial commit", null);
        File main = Utils.join(FBRANCH, "main");
        Utils.writeContents(main, currCommit.getCommitID());
        currCommit.saveCommit(); //save the Commit object into the file under the FCOMMIT
        Utils.writeContents(HEAD, currCommit.getCommitID());

        File currBranchHead = Utils.join(FHEAD, "currBranchHead");
        Utils.writeContents(currBranchHead, "main");
    }


    public void add(String fileName) {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (!FSTAGING.exists()) {
            FSTAGING.mkdir();
        }

        if (Utils.join(FREMOVE, fileName).exists()) {
            Utils.join(FREMOVE, fileName).delete();

        }

        File fileObj = Utils.join(CWD, fileName);
        if (!fileObj.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        File currFile = Utils.join(FSTAGING, fileName);
        if (!currFile.exists()) {
            Utils.writeContents(currFile, Utils.readContentsAsString(fileObj));
        } else {
            String str1 = readContentsAsString(fileObj);
            String str2 = readContentsAsString(currFile);
            if (str1.equals(str2)) {
                return;
            } else {
                Utils.writeContents(currFile, Utils.readContentsAsString(fileObj));
            }
        }

        Blob currBlob = new Blob(fileName);
        currBlob.saveBlobs();
    }

    public void commit(String msg) {
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        File[] listOfFiles = FSTAGING.listFiles();
        File[] rmvFiles = FREMOVE.listFiles();

        if (listOfFiles.length == 0 && rmvFiles.length == 0) {
            System.out.println("No changes added to the commit.");
        }


        String head = Utils.readContentsAsString(HEAD);
        Commit currCommit = new Commit(msg, head);
        for (String fileName : getCurrCommit().getBlobs1().keySet()) {
            if (Utils.join(FREMOVE, fileName).exists()) {
                Utils.join(FREMOVE, fileName).delete();
            }
        }

        HashMap<String, String> blobs1 = currCommit.getBlobs1();
        HashMap<String, Blob> blob = currCommit.getBlobs();

        for (File file : rmvFiles) {
            if (blobs1.containsKey(file.getName())) {
                String blobID = blobs1.get(file.getName());
                blobs1.remove(file.getName());
                blob.remove(blobID);
            }
        }
        currCommit.setBlobs1(blobs1);
        currCommit.setBlobs(blob);


        for (File f : listOfFiles) {

            byte[] content = Utils.readContents(f);
            String blobid = Utils.sha1(f.getName() + new String(content));
            Blob newBlob = Utils.readObject(Utils.join(FBLOBS, blobid), Blob.class);
            currCommit.addBlobs(blobid, newBlob);
            currCommit.addBlobs1(f, blobid);
            f.delete();
        }
        currCommit.saveCommit();

        HEAD.delete();
        Utils.writeContents(HEAD, currCommit.getCommitID());

        String branchName = getBranch();
        File branch = Utils.join(FBRANCH, branchName);
        branch.delete();
        Utils.writeContents(branch, currCommit.getCommitID());
    }


    public void log() {
        if (!HEAD.exists()) {
            System.out.println("no log available");
            return;
        } else {
            String headCopy = Utils.readContentsAsString(HEAD);
            File currCommit = Utils.join(FCOMMIT, headCopy);
            Commit currCommitObj = Utils.readObject(currCommit, Commit.class);

            while (true) {

                System.out.println("===");
                System.out.println("commit " + headCopy);
                System.out.println("Date: " + currCommitObj.getTimestamp());
                System.out.println(currCommitObj.getMessage());
                System.out.println();
                headCopy = currCommitObj.getParentID();

                if (headCopy.equals("")) { //stop
                    break;
                }
                currCommit = Utils.join(FCOMMIT, headCopy);
                currCommitObj = Utils.readObject(currCommit, Commit.class);
            }
        }
    }

    public void checkout1(String arg) {
        Commit head = readObject(Utils.join(FCOMMIT, readContentsAsString(HEAD)), Commit.class);
        HashMap<String, String> files = head.getBlobs1();

        if (!files.containsKey(arg)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            String blobName = files.get(arg);
            Blob blob = Utils.readObject(Utils.join(FBLOBS, blobName), Blob.class);
            File newFile = Utils.join(CWD, arg);
            if (newFile.exists()) {
                newFile.delete();
            }
            Utils.writeContents(newFile, blob.getContent());
        }


    }

    public void checkout2(String arg1, String arg2) {
        List<String> filesList = Utils.plainFilenamesIn(FCOMMIT);
        for (String file : filesList) {
            if (file.startsWith(arg1)) {
                Commit curr = readObject(Utils.join(FCOMMIT, file), Commit.class);
                HashMap<String, String> files = curr.getBlobs1();
                if (!files.containsKey(arg2)) {
                    System.out.println("File does not exist in that commit.");
                    return;
                } else {
                    String blobName = files.get(arg2);
                    Blob blob = Utils.readObject(Utils.join(FBLOBS, blobName), Blob.class);
                    File newFile = Utils.join(CWD, arg2);
                    if (newFile.exists()) {
                        newFile.delete();
                    }
                    Utils.writeContents(newFile, blob.getContent());
                }
                return;
            }
        }
        System.out.println("No commit with that id exists.");
    }

    public void checkout3(String arg1) {
        File branch = Utils.join(FBRANCH, arg1);

        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        String branchHeadId = Utils.readContentsAsString(branch);
        String currentHeadId = getCurrCommit().getCommitID();

        if (arg1.equals(getBranch())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        Commit currHead = getCurrCommit();
        Commit branchHead = Utils.readObject(Utils.join(FCOMMIT, branchHeadId), Commit.class);
        HashMap<String, String> filesInHead = currHead.getBlobs1();
        HashMap<String, String> filesInBranchHead = branchHead.getBlobs1();

        List<String> files = Utils.plainFilenamesIn(CWD);
        for (String file : files) {
            if (!filesInHead.containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        for (String file : files) {
            File file1 = Utils.join(CWD, file);
            if (file1.exists()) {
                file1.delete();
            }
        }

        for (String file : filesInBranchHead.keySet()) {
            checkout2(branchHeadId, file);
        }

        for (String file : Utils.plainFilenamesIn(FSTAGING)) {
            File file1 = Utils.join(FSTAGING, file);
            file1.delete();
        }
        for (String file : Utils.plainFilenamesIn(FREMOVE)) {
            File file1 = Utils.join(FREMOVE, file);
            file1.delete();
        }

        HEAD.delete();
        Utils.writeContents(HEAD, branchHeadId);
        updateBranch(arg1);
    }

    public void globalLog() {
        List<String> files = Utils.plainFilenamesIn(FCOMMIT);
        for (String f : files) {
            Commit commit = Utils.readObject(Utils.join(FCOMMIT, f), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commit.getCommitID());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }


    public void rm(String rmvFileName) { //input removed file name

        File stageFile = Utils.join(FSTAGING, rmvFileName);
        boolean staged = stageFile.exists();

        boolean tracked = getCurrCommit().getBlobs1().containsKey(rmvFileName);

        File removeFile = new File(rmvFileName);

//        1. stage, not track : unstage
//        2. track, not stage : stage it for removal, remove from cwd
//        3. track and staged : unstage & stage it for removal, remove from cwd
//        4. not track and not stage : print

        //1. stage but not track
        if (staged && !tracked) {
            stageFile.delete(); //delete the file in fstaging

        } else if (tracked && !staged) {  //If the file is tracked in the current commit
            File rmvFile = Utils.join(FREMOVE, rmvFileName);
            Utils.writeContents(rmvFile, rmvFileName);
            restrictedDelete(removeFile);
        } else if (tracked && staged) {
            stageFile.delete();
            File rmvFile = Utils.join(FREMOVE, rmvFileName);
            Utils.writeContents(rmvFile, rmvFileName);
            Utils.restrictedDelete(removeFile);
        } else {
            System.out.println("No reason to remove the file.");
        }

    }


    public void find(String args) { //args: commit message
        File[] listOfFiles = FCOMMIT.listFiles();
        boolean found = false;

        for (File f : listOfFiles) {
            Commit currCommit = Utils.readObject(f, Commit.class);
            if (currCommit.getMessage().equals(args)) {
                System.out.println(currCommit.getCommitID());
                found = true;
            }
        }
        if (found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public Commit getCurrCommit() {
        return readObject(Utils.join(FCOMMIT, readContentsAsString(HEAD)), Commit.class);
    }

    public void status() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        File[] bF = FBRANCH.listFiles();
        System.out.println("*" + getBranch());
        for (File f : bF) {
            if (f.getName().equals(getBranch())) {
                continue;
            }
            System.out.println(f.getName());
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        File[] listOfFiles = FSTAGING.listFiles();
        for (File f : listOfFiles) {
            System.out.println(f.getName());
        }
        System.out.println();

        System.out.println("=== Removed Files ===");

        File[] rmvFile = FREMOVE.listFiles();
        for (File f : rmvFile) {
            System.out.println(f.getName());
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void branch(String arg) {

        Commit head = getCurrCommit();
        String id = head.getCommitID();
        File branchF = Utils.join(FBRANCH, arg);
        if (branchF.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Utils.writeContents(branchF, id);
    }

    public void rmBranch(String rmBName) {

        File branchF = Utils.join(FBRANCH, rmBName);

        //Error: if a branch with the given name does not exist
        if (!branchF.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        //Error: try to remove the branch you’re currently on
        String currHead = Utils.readContentsAsString(HEAD);
        String currB = Utils.readContentsAsString(branchF);
        if (currB.equals(currHead)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branchF.delete();

    }

    public void reset(String id) {
        File commitPath = Utils.join(FCOMMIT, id);
        if (!commitPath.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        String currentHeadId = getCurrCommit().getCommitID();
        Commit currHead = getCurrCommit();
        Commit branchHead = Utils.readObject(Utils.join(FCOMMIT, id), Commit.class);
        HashMap<String, String> filesInHead = currHead.getBlobs1();
        HashMap<String, String> filesInBranchHead = branchHead.getBlobs1();

        List<String> files = Utils.plainFilenamesIn(CWD);
        for (String file : files) {
            if (!filesInHead.containsKey(file)) {
                if (filesInBranchHead.containsKey(file)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
        }
        for (String file : files) {
            File file1 = Utils.join(CWD, file);
            if (file1.exists()) {
                file1.delete();
            }
        }

        for (String file : filesInBranchHead.keySet()) {
            checkout2(id, file);
        }

        for (String file : Utils.plainFilenamesIn(FSTAGING)) {
            File file1 = Utils.join(FSTAGING, file);
            file1.delete();
        }
        for (String file : Utils.plainFilenamesIn(FREMOVE)) {
            File file1 = Utils.join(FREMOVE, file);
            file1.delete();
        }

        Commit head = getCurrCommit();
        List<String> branches = Utils.plainFilenamesIn(FBRANCH);
        for (String branch : branches) {
            if (head.getCommitID().equals(Utils.readContentsAsString(
                    Utils.join(FBRANCH, branch)))) {
                Utils.join(FBRANCH, branch).delete();
                Utils.writeContents(Utils.join(FBRANCH, branch), id);
            }
        }

        HEAD.delete();
        Utils.writeContents(HEAD, id);

        String branchName = getBranch();
        File branch = Utils.join(FBRANCH, branchName);
        branch.delete();
        Utils.writeContents(branch, id);

    }

    private void updateBranch(String branch) {
        File currBranchHead = Utils.join(FHEAD, "currBranchHead");
//        String currBranch
        if (currBranchHead.exists()) {
            currBranchHead.delete();
        }
        Utils.writeContents(currBranchHead, branch);
    }

    private String getBranch() {
        File currBranchHead = Utils.join(FHEAD, "currBranchHead");
        return Utils.readContentsAsString(currBranchHead);
    }

    //input: branch name
    private String findSplit(String currBranch, String inputBranch) {
        File currBranchFile = Utils.join(FBRANCH, currBranch);
        File inputBranchFile = Utils.join(FBRANCH, inputBranch);
        if (!inputBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return "0";
        }

        String currBranchCommitId = Utils.readContentsAsString(currBranchFile);
        String inputBranchCommitId = Utils.readContentsAsString(inputBranchFile);
        if (currBranchCommitId.equals(inputBranchCommitId)) {
            System.out.println("Cannot merge a branch with itself.");
            return "0";
        }
        Commit currBranchCommit = Utils.readObject(
                Utils.join(FCOMMIT, currBranchCommitId), Commit.class);
        Commit pointer = currBranchCommit;
        Commit pointer2 = currBranchCommit;

        List<String> currBranchAllCommits = new ArrayList<String>();
        while (true) {
            currBranchAllCommits.add(pointer.getCommitID());
            if (pointer.getParentID().equals("")) {
                break;
            }
            pointer = Utils.readObject(Utils.join(
                    FCOMMIT, pointer.getParentID()), Commit.class);
        }

        if (!pointer2.getParentID2().equals("")) {
            pointer2 = Utils.readObject(Utils.join(
                    FCOMMIT, pointer2.getParentID2()), Commit.class);
            while (true) {
                currBranchAllCommits.add(pointer2.getCommitID());
                if (pointer2.getParentID().equals("")) {
                    break;
                }
                pointer2 = Utils.readObject(Utils.join(
                        FCOMMIT, pointer2.getParentID()), Commit.class);
            }
        }

        Commit inputBranchCommit = Utils.readObject(Utils.join(
                FCOMMIT, inputBranchCommitId), Commit.class);
        Commit pointer1 = inputBranchCommit;

        while (true) {
            if (currBranchAllCommits.contains(pointer1.getCommitID())) {
                return pointer1.getCommitID();
            }

            pointer1 = Utils.readObject(Utils.join(
                    FCOMMIT, pointer1.getParentID()), Commit.class);
        }
    }

    public void merge(String inputBranch) {
        List<String> remove = Utils.plainFilenamesIn(FREMOVE);
        List<String> add = Utils.plainFilenamesIn(FSTAGING);
        if (remove.size() != 0 || add.size() != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        String currBranchName = Utils.readContentsAsString(Utils.join(FHEAD, "currBranchHead"));
        String splitPointId = findSplit(currBranchName, inputBranch);
        if (splitPointId.equals("0")) {
            return;
        }
        if (splitPointId.equals(Utils.readContentsAsString(Utils.join(FBRANCH, currBranchName)))) {
            checkout3(inputBranch);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        if (splitPointId.equals(Utils.readContentsAsString(Utils.join(FBRANCH, inputBranch)))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }


        Commit split = Utils.readObject(Utils.join(FCOMMIT, splitPointId), Commit.class);
        Commit head = readObject(Utils.join(FCOMMIT, Utils.readContentsAsString(Utils.join(FBRANCH, currBranchName))), Commit.class);
        Commit givenBranch = readObject(Utils.join(FCOMMIT, Utils.readContentsAsString(Utils.join(FBRANCH, inputBranch))), Commit.class);

        //special merge commit
        Commit last = new Commit("Merged " + inputBranch + " into " + currBranchName + ".", head.getCommitID());
        last.setParentID2(givenBranch.getCommitID());
        HashMap<String, String> blobs1 = new HashMap<String, String>();
        HashMap<String, Blob> blobs = new HashMap<String, Blob>();


        HashMap<String, String> headBlob1 = head.getBlobs1();
        HashMap<String, String> splitBlob1 = split.getBlobs1();
        HashMap<String, String> givenBranchBlob1 = givenBranch.getBlobs1();
        Set<String> filesInHead = headBlob1.keySet();
        Set<String> filesInSplit = splitBlob1.keySet();
        Set<String> filesInGivenBranch = givenBranchBlob1.keySet();

//        headBlob1.get()
        for (String i : filesInSplit) {
            if (headBlob1.containsKey(i) && givenBranchBlob1.containsKey(i)) {
                String fileInHead = headBlob1.get(i);
                String fileInGivenBranch = givenBranchBlob1.get(i);
                String fileInSplit = splitBlob1.get(i);
                //finish 1
                if (fileInHead.equals(fileInSplit) && !fileInHead.equals(fileInGivenBranch)) {
                    checkout2(Utils.readContentsAsString(Utils.join(FBRANCH, inputBranch)), i);
                    add(i);
                    blobs1.put(i, new Blob(i).getBlobID());
                    blobs.put(new Blob(i).getBlobID(), new Blob(i));

                    //finish 2
                } else if (!fileInHead.equals(fileInSplit) && fileInSplit.equals(fileInGivenBranch)) {

                    blobs1.put(i, headBlob1.get(i));
                    blobs.put(headBlob1.get(i), head.getBlobs().get(headBlob1.get(i)));

                    filesInHead.remove(i);
                    filesInGivenBranch.remove(i);
                    continue;

                    //finish 3(first half)
                } else if (fileInHead.equals(fileInGivenBranch) && !fileInSplit.equals(fileInGivenBranch)) {
                    blobs1.put(i, headBlob1.get(i));
                    blobs.put(headBlob1.get(i), head.getBlobs().get(headBlob1.get(i)));
                    filesInHead.remove(i);
                    filesInGivenBranch.remove(i);
                    continue;

                } else if (!fileInHead.equals(fileInSplit) && !fileInHead.equals(fileInGivenBranch) && !fileInSplit.equals(fileInGivenBranch)) {
                    File f = Utils.join(CWD, i);
                    if (f.exists()) {
                        f.delete();
                    }
                    System.out.println("Encountered a merge conflict.");
                    Utils.writeContents(f, "<<<<<<< HEAD\n" +
                            new String(head.getBlobs().get(headBlob1.get(i)).getContent()) +
                            "=======\n" +
                            new String(givenBranch.getBlobs().get(givenBranchBlob1.get(i)).getContent()) +
                            ">>>>>>>" + "\n");
                    add(i);
                    blobs1.put(i, new Blob(i).getBlobID());
                    blobs.put(new Blob(i).getBlobID(), new Blob(i));
                }


            } else if (!headBlob1.containsKey(i) && !givenBranchBlob1.containsKey(i)) {
                if (Utils.join(CWD, i).exists()) {
                    filesInHead.remove(i);
                    filesInGivenBranch.remove(i);
                    continue;//finish half 3
                }

                //finish 6
            } else if (headBlob1.containsKey(i) && !givenBranchBlob1.containsKey(i)) {
                if (headBlob1.get(i).equals(splitBlob1.get(i))) {
                    File removeFile = Utils.join(CWD, i);
                    if (removeFile.exists()) {
                        removeFile.delete();
                    }

//                    headBlob1.remove(i);
//                    Commit newCommit=new Commit(head.getTimestamp(),head.getBlobs(),headBlob1,head.getMessage(),head.getParentID(),head.getCommitID());
//                    Utils.join(FCOMMIT, readContentsAsString(HEAD)).delete();
//                    Utils.writeObject(Utils.join(FCOMMIT, readContentsAsString(HEAD)),newCommit);

                    //remove and untracked
//                    blobs1.put(i,headBlob1.get(i));
//                    blobs.put(headBlob1.get(i),head.getBlobs().get(headBlob1.get(i)));
                } else {//A B X
                    File f = Utils.join(CWD, i);
                    if (f.exists()) {
                        f.delete();
                    }
                    System.out.println("Encountered a merge conflict.");
                    Utils.writeContents(f, "<<<<<<< HEAD\n" +
                            new String(head.getBlobs().get(headBlob1.get(i)).getContent()) +
                            "=======\n" +
                            "" +
                            ">>>>>>>" + "\n");

                    add(i);
                    blobs1.put(i, new Blob(i).getBlobID());
                    blobs.put(new Blob(i).getBlobID(), new Blob(i));

                }


            } else if (!headBlob1.containsKey(i) && givenBranchBlob1.containsKey(i)) {
                if (splitBlob1.get(i).equals(givenBranchBlob1.get(i))) {
                    filesInHead.remove(i);
                    filesInGivenBranch.remove(i);
                    continue;
                }
                File f = Utils.join(CWD, i);
                if (f.exists()) {
                    f.delete();
                }
                System.out.println("Encountered a merge conflict.");
                Utils.writeContents(f, "<<<<<<< HEAD\n" +
                        new String(head.getBlobs().get(headBlob1.get(i)).getContent()) +
                        "=======\n" +
                        new String(givenBranch.getBlobs().get(givenBranchBlob1.get(i)).getContent()) +
                        ">>>>>>>" + "\n");
                add(i);
                blobs1.put(i, new Blob(i).getBlobID());
                blobs.put(new Blob(i).getBlobID(), new Blob(i));

            }
            filesInHead.remove(i);
            filesInGivenBranch.remove(i);
        }

        //finish 4
        if (!filesInHead.isEmpty()) {
            for (String j : filesInHead) {
                if (!givenBranchBlob1.containsKey(j)) {
                    blobs1.put(j, headBlob1.get(j));
                    blobs.put(headBlob1.get(j), head.getBlobs().get(headBlob1.get(j)));
                    continue;
                }
            }
        }

        //finish 5
        if (!filesInGivenBranch.isEmpty()) {
            for (String j : filesInGivenBranch) {
                if (!headBlob1.containsKey(j)) {
                    if (Utils.join(CWD, j).exists() && !Utils.readContentsAsString(Utils.join(CWD, j)).equals(new String(givenBranch.getBlobs().get(givenBranchBlob1.get(j)).getContent()))) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                    checkout2(Utils.readContentsAsString(Utils.join(FBRANCH, inputBranch)), j);
                    add(j);
                    blobs1.put(j, new Blob(j).getBlobID());
                    blobs.put(new Blob(j).getBlobID(), new Blob(j));
                }
            }
        }
        if (!filesInHead.isEmpty() && !filesInGivenBranch.isEmpty()) {
            for (String i : filesInGivenBranch) {
                if (filesInHead.contains(i)) {
                    if (!headBlob1.get(i).equals(givenBranchBlob1.get(i))) {
                        File f = Utils.join(CWD, i);
                        if (f.exists()) {
                            f.delete();
                        }
                        System.out.println("Encountered a merge conflict.");
                        Utils.writeContents(f, "<<<<<<< HEAD\n"
                                + "contents of file in current branch\n"
                                + "=======\n"
                                + "contents of file in given branch\n"
                                + ">>>>>>>" + "\n");
                        add(i);
                        blobs1.put(i, new Blob(i).getBlobID());
                        blobs.put(new Blob(i).getBlobID(), new Blob(i));
                    }
                }
                filesInHead.remove(i);
            }
        }

        last.setBlobs1(blobs1);
        last.setBlobs(blobs);
        last.saveCommit();

        HEAD.delete();
        Utils.writeContents(HEAD, last.getCommitID());
        Utils.join(FBRANCH, currBranchName).delete();
        Utils.writeContents(Utils.join(FBRANCH, currBranchName), last.getCommitID());


        for (File f : FSTAGING.listFiles()) {
            f.delete();
        }
        for (File f : FREMOVE.listFiles()) {
            f.delete();
        }

    }
}
