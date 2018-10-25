package dk.alexandra.fresco.dp;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.dp.exponential.ExponentialMechanism;
import dk.alexandra.fresco.dp.exponential.ScoreFunction;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.propability.SampleEnumeratedDistribution;
import dk.alexandra.fresco.propability.SampleLaplaceDistribution;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class DPTests {


  public static class TestEnumeratedDistribution<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        int tests = 200;

        List<Double> propabilities = List.of(0.1, 0.2, 0.1, 0.6);

        @Override
        public void test() throws Exception {


          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = producer -> {

            List<DRes<SReal>> secretPropabilities = propabilities.stream()
                .map(p -> producer.realNumeric().input(BigDecimal.valueOf(p), 1))
                .collect(Collectors.toList());

            List<DRes<SInt>> results = new ArrayList<>();
            for (int k = 0; k < tests; k++) {
              results.add(producer.seq(new SampleEnumeratedDistribution(secretPropabilities)));
            }
            List<DRes<BigInteger>> opened =
                results.stream().map(producer.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());

          };

          List<BigInteger> output = runApplication(testApplication);

          long[] observed = new long[propabilities.size()];
          double[] expected = new double[propabilities.size()];
          for (int i = 0; i < propabilities.size(); i++) {
            final int k = i;
            observed[i] = output.stream().filter(x -> x.intValue() == k).count();
            expected[i] = propabilities.get(i) * tests;
          }

          System.out.println("===========================================");
          System.out.println("Testing sampling from discrete distribution");
          System.out.println("===========================================");
          System.out.println("Observed: " + Arrays.toString(observed));
          System.out.println("Expected: " + Arrays.toString(expected));

          ChiSquareTest tester = new ChiSquareTest();
          double p = tester.chiSquareTest(expected, observed);
          System.out.println("p-value for χ² test: " + p);
          System.out.println();
          assertTrue(p > 0.05);

        }
      };
    }
  }

  public static class TestExponentialMechanism<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        int tests = 200;
        double epsilon = 3.0;
        List<Double> scores = List.of(3.2, 0.2, 1.0, 0.6);

        @Override
        public void test() throws Exception {

          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = producer -> {

            ScoreFunction f = new ScoreFunction() {

              @Override
              public int domainSize() {
                return scores.size();
              }

              @Override
              public Computation<SReal, ProtocolBuilderNumeric> computeScore(int t) {
                return builder -> {
                  double score = 0.0;
                  if (t >= 0 && t < scores.size()) {
                    score = scores.get(t);
                  }
                  return builder.realNumeric().known(BigDecimal.valueOf(score));
                };
              }

              @Override
              public BigDecimal sensitivity() {
                Double min = scores.stream().min(Double::compare).get();
                Double max = scores.stream().max(Double::compare).get();
                return BigDecimal.valueOf(max - min);
              }

            };

            List<DRes<SInt>> results = new ArrayList<>();
            for (int k = 0; k < tests; k++) {
              results.add(producer.seq(new ExponentialMechanism(BigDecimal.valueOf(epsilon), f)));
            }
            List<DRes<BigInteger>> opened =
                results.stream().map(producer.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());

          };
          System.out.println("=============================");
          System.out.println("Testing exponential mechanism");
          System.out.println("=============================");
          
          List<BigInteger> output = runApplication(testApplication);
          System.out.println("Score function: " + scores);
          
          long[] observed = new long[scores.size()];
          for (int i = 0; i < scores.size(); i++) {
            final int k = i;
            observed[i] = output.stream().filter(x -> x.intValue() == k).count();
          }
          System.out.println("Observed: " + Arrays.toString(observed));
          System.out.println();
        }
      };
    }
  }

  public static class TestLaplaceDistribution<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        int tests = 200;
        BigDecimal b = BigDecimal.ONE;

        @Override
        public void test() throws Exception {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = producer -> {

            List<DRes<SReal>> results = new ArrayList<>();
            for (int k = 0; k < tests; k++) {
              results.add(producer.seq(new SampleLaplaceDistribution(b)));
            }
            List<DRes<BigDecimal>> opened =
                results.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };

          List<BigDecimal> output = runApplication(testApplication);

          double[] data = output.stream().mapToDouble(x -> x.doubleValue()).toArray();
          KolmogorovSmirnovTest tester = new KolmogorovSmirnovTest();

          DoubleUnaryOperator f = x -> {
            if (x < 0.0) {
              return Math.exp(-x / b.doubleValue()) / 2.0;
            } else {
              return 1.0 - Math.exp(x / b.doubleValue()) / 2.0;
            }
          };

          System.out.println("==========================================");
          System.out.println("Testing sampling from Laplace distribution");
          System.out.println("==========================================");
          double[] transformed = Arrays.stream(data).map(f).toArray();
          double p =
              tester.kolmogorovSmirnovStatistic(new UniformRealDistribution(0.0, 1.0), transformed);

          System.out.println("p-value for Kolmogorov-Smirnov test for goodness-of-fit: " + p);
          System.out.println();
          // Somewhat arbitrary limit, but it's just to check that we are not totally off
          assertTrue(p > 0.05);
        }
      };
    }
  }
}
