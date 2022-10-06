package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Ziyang Zeng, Xiaomeng Xu
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        Repository repo = new Repository(); //init a repository

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                repo.init();
                break;
            case "add":
                repo.add(args[1]);
                break;
            case "commit":
                repo.commit(args[1]);
                break;
            case "log":
                repo.log();
                break;
            case "checkout":
                if (args[1].equals("--")) {
                    repo.checkout1(args[2]);
                } else if (args.length == 2) {
                    repo.checkout3(args[1]);
                } else if (args[2].equals("--")) {
                    repo.checkout2(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    return;
                }
                break;
            case "rm":
                repo.rm(args[1]);
                break;
            case "find":
                repo.find(args[1]);
                break;
            case "status":
                repo.status();
                break;
            case "global-log":
                repo.globalLog();
                break;
            case "branch":
                repo.branch(args[1]);
                break;
            case "rm-branch":
                repo.rmBranch(args[1]);
                break;
            case "reset":
                repo.reset(args[1]);
                break;
            case "merge":
                repo.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                return;
        }
    }
}
