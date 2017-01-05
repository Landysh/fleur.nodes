package tests.java.inflor.integration;

public enum CompensationTestDatasets {
  OMIP16 ("src/tests/resources/OMIP016/compensation");
  
  private final String folderPath;

  CompensationTestDatasets(String path) {
    this.folderPath = path;
  }
  public String getPath(){
    return this.folderPath;
  }
}
