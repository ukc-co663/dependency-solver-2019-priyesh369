package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Psudeocode to implemenet
// search(x):
//   if not valid(x) return
//   if x seen, return
//   make x seen
//   if final(x):
//     solution found!
//   for each package p in repo:
//     obtain y from x by flipping state of p (installed<->uninstalled)
//     search(y)

public class Main {
  public static void main(String[] args) throws IOException {
    TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {};
    List<Package> repo = JSON.parseObject(readFile(args[0]), repoType);
    TypeReference<List<String>> strListType = new TypeReference<List<String>>() {};
    List<String> initial = JSON.parseObject(readFile(args[1]), strListType);
    List<String> constraints = JSON.parseObject(readFile(args[2]), strListType);

    // CHANGE CODE BELOW:
    // using repo, initial and constraints, compute a solution and print the answer
    // for (Package p : repo) {
    //   System.out.printf("package %s version %s\n", p.getName(), p.getVersion());
    //   for (List<String> clause : p.getDepends()) {
    //     System.out.printf("  dep:");
    //     for (String q : clause) {
    //       System.out.printf(" %s", q);
    //     }
    //     System.out.printf("\n");
    //   }
    // }
    for (Package p : repo) {
      p.stringToPackage(repo);
    }
  }

  static String readFile(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    StringBuilder sb = new StringBuilder();
    br.lines().forEach(line -> sb.append(line));
    return sb.toString();
  }
}

class Package {
  private String name;
  private String version;
  private Integer size;
  private List<List<String>> depends = new ArrayList<>();
  private List<String> conflicts = new ArrayList<>();
  private List<List<Package>> pDepends = new ArrayList<>();
  private List<Package> pConflicts = new ArrayList<>();

  //gets
  public String getName() { return name; }
  public String getVersion() { return version; }
  public Integer getSize() { return size; }
  public List<List<String>> getDepends() { return depends; }
  public List<String> getConflicts() { return conflicts; }
  public List<List<Package>> getPDepends() { return pDepends; } 
  public List<Package> getPConflicts() { return pConflicts; }
  //sets
  public void setName(String name) { this.name = name; }
  public void setVersion(String version) { this.version = version; }
  public void setSize(Integer size) { this.size = size; }
  public void setDepends(List<List<String>> depends) { this.depends = depends; }
  public void setConflicts(List<String> conflicts) { this.conflicts = conflicts; }

  public void setPDepends(List<List<Package>> depends) { this.pDepends = depends; }
  public void setPConflicts(List<Package> conflicts) { this.pConflicts = conflicts; }

  

  public void stringToPackage(List<Package> repo) {
    List<List<Package>> pDependsTemp = new ArrayList<>();
      for (List<String> dependencies : getDepends()) {
        pDependsTemp.add(expandPackageList(dependencies, repo));
      }
      setPDepends(pDependsTemp);
      setPConflicts(expandPackageList(getConflicts(), repo));

  }

  private List<Package> expandPackageList(List<String> packageNames, List<Package> repo){
      List<Package> packages = new ArrayList<>();
      for (String name : packageNames) {
        packages.addAll(createPackage(name, repo));
      }
      return packages;
  }

  private createPackage(String name, List<Package> repo){

  }
}
