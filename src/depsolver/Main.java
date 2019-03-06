package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

class Package {
    private String name;
    private String version;
    private Integer size;
    private List<List<String>> depends = new ArrayList<>();
    private List<String> conflicts = new ArrayList<>();

    //gets
    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Integer getSize() {
        return size;
    }

    public List<List<String>> getDepends() {
        return depends;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    //sets
    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setDepends(List<List<String>> depends) {
        this.depends = depends;
    }

    public void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }

    //Reused Tostrings
    public String toStringNameVersion() {
        return getName() + "=" + getVersion();
    }
}

/**
 * Psudeocode to implemenet
 * search(x):
 * if not valid(x) return
 * if x seen, return
 * make x seen
 * if final(x): solution found!
 * <p>
 * for each package p in repo:
 * obtain y from x by flipping state of p (installed<->uninstalled)
 * search(y)
 * <p>
 * seen:
 * The simplest way is to use a set (of the seen states, where "state" = "set of packages").
 * Marking as seen is adding to the set; checking if seen is testing membership.
 * In Java, you'd use HashSet.
 * <p>
 * Ideas to follow:
 * Cost of adding package is size of package
 * Cost of removing package is a million (units?)
 * <p>
 * CONSTRAINTS: what needs to be added / removed for the system
 * REPO: all known packages
 * INITAL: packages on system at start
 */

public class Main {
    private static List<String> steps = new ArrayList<>();
    private static HashSet<List<String>> seen = new HashSet<>();
    private static HashMap<String, Integer> outputAndCost = new HashMap<>();

    public static void main(String[] args) throws IOException {
        TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {
        };
        List<Package> repo = JSON.parseObject(readFile(args[0]), repoType);
        TypeReference<List<String>> strListType = new TypeReference<List<String>>() {
        };
        List<String> initial = JSON.parseObject(readFile(args[1]), strListType);
        List<String> constraints = JSON.parseObject(readFile(args[2]), strListType);

        // CHANGE CODE BELOW:
        // using repo, initial and constraints, compute a solution and print the answer

        search(repo, initial, constraints, initial);
        printCheapToJSON();
        System.out.println(outputAndCost);

    }

    /**
     * Psudeocode to implemenet
     * search(x):
     * if not valid(x) return
     * if x seen, return
     * make x seen
     * if final(x): solution found!
     * <p>
     * for each package p in repo:
     * obtain y from x by flipping state of p (installed<->uninstalled)
     * search(y)
     **/
    static void search(List<Package> repo, List<String> initial, List<String> constraints, List<String> builder) {
        //builder starts as copy of inital but that can then be manipulated during search process
        if (!isValid(builder, repo)) {
            return;
        }
        if (seen.contains(builder)) {
            return;
        }
        if (isFinal(builder, constraints)) {
            //possible solution found - calculate cost and store
            int cost = getCost(repo);
            StringBuilder output = new StringBuilder();
            for (String s : steps) {
                output.append(s + "priyesh"); // arbituray string I can use to split on later
            }
            outputAndCost.put(output.toString(), cost);
            return;
        } else {
            //not final
            seen.add(builder);
        }

        for (Package p : repo) {
            String pack = p.toStringNameVersion();
            String plusPack = "+" + pack;
            String minuPack = "-" + pack;
            //if package not in builder and isn't added in steps add it and search again
            // else if it was alreay in system remove and search again
            // (flipping step, may be working as hoped or its just a mess - time will tell :P)
            if (!builder.contains(pack) && !steps.contains(minuPack)) {
                builder.add(pack);
                steps.add(plusPack);
                search(repo, initial, constraints, builder);
            } else if (initial.contains(pack)) {
                builder.remove(pack);
                steps.add(minuPack);
                search(repo, initial, constraints, builder);
            }
        }

    }

    /**
     * state is vaild if all deps installed and installed packages don't conflict
     */
    static boolean isValid(List<String> toCheck, List<Package> repo) {
        for (Package p : repo) {
            if (toCheck.contains(p.toStringNameVersion())) {
                for (List<String> dep : p.getDepends()) {
                    boolean depsFound = false;
                    for (String d : dep) {
                        String[] depPacked = depAndConSplit(d);
                        if (depsFound == false) {
                            for (String tC : toCheck) {
                                String[] tCPacked = depAndConSplit(tC);
                                if (tCPacked[0].equals(depPacked[0])) { //same package found
                                    depsFound = true; //TODO: Version Handling
                                }
                            }
                        }

                    }
                    if (depsFound == false) return false;
                }
                for (String con : p.getConflicts()) {
                    boolean conFound = false;
                    String[] conPacked = depAndConSplit(con);
                    if (conFound == false) {
                        for (String tC : toCheck) {
                            String[] tCPacked = depAndConSplit(tC);
                            if (tCPacked[0].equals(conPacked[0])) { //same package found
                                conFound = true; //TODO: Version Handling
                            }
                        }
                    }

                    if (conFound == true) return false;
                }
            }
        }
        return true; //if here then valid
    }

    /**
     * state is final if all contraints are met
     */
    static boolean isFinal(List<String> toCheck, List<String> constraints) {
        boolean conMet = false;
        for (String con : constraints) {
            String action = Character.toString(con.charAt(0));
            con = con.substring(1);
            String[] conPacked = depAndConSplit(con);

            if (action.equals("+")) {
                for (String tC : toCheck) {
                    String[] tCPacked = depAndConSplit(tC);
                    if (tCPacked[0].equals(conPacked[0])) {
                        conMet = true; //TODO Version Handling
                    }
                }
            } else if (action.equals("-")) {
                boolean matchFound = false;
                for (String tC : toCheck) {
                    String[] tcPacked = depAndConSplit(tC);
                    if (tcPacked[0].equals(conPacked[0])) {
                        matchFound = true;
                    }
                }
                if (matchFound) {
                    conMet = false;
                }
            }
        }
        return conMet;
    }

    /**
     * Cost of adding package is size of package
     * Cost of removing package is a million
     */
    private static int getCost(List<Package> repo) {
        int cost = 0;
        for (String s : steps) {
            if (s.contains("-")) {
                cost += 1000000;
            } else {
                for (Package p : repo) {
                    if (s.substring(1).equals(p.getName() + "=" + p.getVersion())) {
                        cost += p.getSize();
                    }
                }
            }
        }
        return cost;
    }

    //basic min num checking then storing when lowest and at end print the lowest
    private static void printCheapToJSON() {
        List<String> lowestSteps = new ArrayList<>();
        int lowestCost = -1;
        for (Map.Entry<String, Integer> e : outputAndCost.entrySet()) {
            if (e.getValue() < lowestCost) {
                lowestSteps = Arrays.asList(e.getKey().split("priyesh"));
                lowestCost = e.getValue();
            }
        }
        System.out.println(JSON.toJSON(lowestSteps));
    }

    /**
     * creates a "package" from dependecy and conflict formot (i.e
     * "B>=3.1" becomes name = B version = 3.1 comparator = >=
     */
    private static String[] depAndConSplit(String packString) {
        String[] temp = new String[2];
        String[] packaged = new String[3];
        if (packString.contains("<=")) {
            temp = packString.split("<=");
            packaged[0] = temp[0];
            packaged[1] = "<=";
            packaged[2] = temp[1];
        } else if (packString.contains(">=")) {
            temp = packString.split(">=");
            packaged[0] = temp[0];
            packaged[1] = ">=";
            packaged[2] = temp[1];
        } else if (packString.contains("<")) {
            temp = packString.split("<");
            packaged[0] = temp[0];
            packaged[1] = "<";
            packaged[2] = temp[1];
        } else if (packString.contains(">")) {
            temp = packString.split(">");
            packaged[0] = temp[0];
            packaged[1] = ">";
            packaged[2] = temp[1];
        } else if (packString.contains("=")) {
            temp = packString.split("=");
            packaged[0] = temp[0];
            packaged[1] = "=";
            packaged[2] = temp[1];
        } else {
            //unversioned
            packaged[0] = packString;
            packaged[1] = "";
            packaged[2] = "";
        }

        return packaged;

    }

    static String readFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        br.lines().forEach(line -> sb.append(line));
        return sb.toString();
    }
}
