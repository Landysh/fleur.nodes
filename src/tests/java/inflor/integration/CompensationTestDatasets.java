package tests.java.inflor.integration;

public enum CompensationTestDatasets {
  OMIP_16 ("src/tests/resources/OMIP016/compensation"),
  OMIP_18 ("src/tests/resources/OMIP018/compensation"),
  OMIP_21 ("src/tests/resources/OMIP021/compensation"),
  OMIP_25 ("src/tests/resources/OMIP025/compensation"),
  OMIP_28 ("src/tests/resources/OMIP028/compensation"),
  OMIP_35 ("src/tests/resources/OMIP035/compensation");
  
  private final String folderPath;

  CompensationTestDatasets(String path) {
    this.folderPath = path;
  }
  public String getPath(){
    return this.folderPath;
  }
}
