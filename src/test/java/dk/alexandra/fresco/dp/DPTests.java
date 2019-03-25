package dk.alexandra.fresco.dp;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.dp.exponential.ExponentialMechanism;
import dk.alexandra.fresco.dp.exponential.ScoreFunction;
import dk.alexandra.fresco.dp.stat.NoisyChiSquareTest;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

public class DPTests {



  public static class TestExponentialMechanism<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        int tests = 100;
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

  public static class TestNoisyChiSquareTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        double[] ps = new double[] {0.5752475247524753, 0.3415841584158416, 0.06831683168316832,
            0.01485148514851485};
        long[] ob = new long[] {7700, 4800, 850, 200};

        int classes = ps.length;

        double epsilon = 0.5;
        double df = ob.length - 1;

        List<BigDecimal> propabilities =
            Arrays.stream(ps).mapToObj(x -> BigDecimal.valueOf(x)).collect(Collectors.toList());

        List<BigInteger> observed =
            Arrays.stream(ob).mapToObj(BigInteger::valueOf).collect(Collectors.toList());
        int n = observed.stream().mapToInt(x -> x.intValue()).sum();

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {

            List<DRes<SReal>> p = propabilities.stream().map(x -> builder.realNumeric().input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SInt>> o = observed.stream().map(x -> builder.numeric().input(x, 2))
                .collect(Collectors.toList());
            DRes<SReal> x =
                builder.seq(new NoisyChiSquareTest(epsilon, o, BigInteger.valueOf(n), p));
            return builder.realNumeric().open(x);
          };

          BigDecimal output = runApplication(testApplication);

          // Compare the test statistics with a χ² distribution. Note that the test statistics is
          // NOT χ² distributed, but we use it as an approximation since the actual distribution is
          // hard to calculate analytically.
          ChiSquaredDistribution dist = new ChiSquaredDistribution(df);
          double noisyP = 1.0 - dist.cumulativeProbability(output.doubleValue());
          
          // Calculate the p-value of the test without noise
          double[] ex = new double[ob.length];
          for (int i = 0; i < classes; i++) {
            ex[i] = ps[i] * n;
          }
          ChiSquareTest actualTest = new ChiSquareTest();
          double actualP = actualTest.chiSquareTest(ex, ob);
          
          System.out.println("===================================================");
          System.out.println("Testing noisy χ² goodness of fit hypothesis testing");
          System.out.println("===================================================");
          System.out.println("Actual p-value = " + actualP);
          System.out.println("Noisy p-value = " + noisyP);
          System.out.println("");
          
          assertTrue(noisyP < 0.05 == actualP < 0.05);
        }
      };
    }
  }
}
