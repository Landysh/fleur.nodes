package inflor.unit;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import inflor.core.singlets.SingletsModel;

public class SingletsModelTest {
  // Define Constants

  // Case 1
  String[] testParameters1 = {"FL3-H", "FL2-A", "FL2-H", "FL13-H", "FL1-A", "Time", "FL12-A",
      "FL1-H", "FSC-Width", "FL12-H", "SSC-A", "FL13-A", "FL11-A", "SSC-H", "FL11-H", "FSC-A",
      "FL10-A", "FSC-H", "FL10-H", "FL8-H", "FL9-A", "FL7-A", "FL9-H", "FL7-H", "FL8-A", "FL6-A",
      "FL6-H", "FL5-A", "FL5-H", "FL4-A", "FL4-H", "FL3-A"};

  // Case 2
  String[] testParameters2 = {"CD3 CD14 Live/Dead-A", "PE-Cy7-A", "PE-Cy55-A", "PE-A",
      "FITC/Alexa 488-A", "SSC-W", "SSC-H", "SSC-A", "FSC-W", "Time", "FSC-H", "APC-H7-A", "FSC-A",
      "APC-Ax700-A", "APC/Ax 647-A"};

  // Case 3
  String[] testParameters3 =
      {"FSC-A", "APC-A", "PE-Cy7-A", "PerCP-A", "PE-A", "FITC-A", "Time", "SSC-A", "APC-Cy7-A"};

  public void testCreateVectorPairs() {

  }

  @Test
  public void testFindColumnNames() throws Exception {
    // Setup. Reference future new cases here.
    final SingletsModel model1 = new SingletsModel(testParameters1);
    final SingletsModel model2 = new SingletsModel(testParameters2);
    final SingletsModel model3 = new SingletsModel(testParameters3);

    final SingletsModel[] testModels = {model1, model2, model3};
    System.out.println("SingletsModel testFindColumnNames completed (succefully or otherwise)");

  }
}
