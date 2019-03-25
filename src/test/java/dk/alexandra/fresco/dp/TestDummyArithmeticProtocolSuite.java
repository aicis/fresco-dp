package dk.alexandra.fresco.dp;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {
  
  @Test
  public void test_exponential_mechanism() throws Exception {
    runTest(new DPTests.TestExponentialMechanism<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }
  
  @Test
  public void test_chi_square_test() throws Exception {
    runTest(new DPTests.TestNoisyChiSquareTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }


}
