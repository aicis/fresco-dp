package dk.alexandra.fresco.dp.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.sampling.SampleLaplaceDistribution;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * We follow the approach of Gaboardi et.al (Differentially Private Chi-Squared Hypothesis Testing:
 * Goodness of Fit and Independence Testing") and calculate a noisy test statistics for a
 * &Chi;<sup>2</sup>-test. The noise is added to the observed number of elements in each class.
 * 
 * <p>
 * As discussed in the paper, the resulting test statistics is not &Chi;<sup>2</sup>-distributed
 * anymore, but the mean will be larger, meaning that comparing it directly to a
 * &Chi;<sup>2</sup>-distrubution would mean that we asymptotically will refuse more hypothesis than
 * with a non-noisy test.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class NoisyChiSquareTest implements Computation<SReal, ProtocolBuilderNumeric> {
  private List<DRes<SInt>> observed;
  private List<DRes<SReal>> p;
  private double epsilon;
  private BigInteger n;

  public NoisyChiSquareTest(double epsilon, List<DRes<SInt>> observed, BigInteger n,
      List<DRes<SReal>> p) {
    this.observed = observed;
    this.p = p;
    this.epsilon = epsilon;
    this.n = n;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SReal>> terms = new ArrayList<>();
      for (int i = 0; i < observed.size(); i++) {
        terms.add(par.seq(calculateTerm(observed.get(i), p.get(i))));
      }
      return () -> terms;
    }).seq((seq, terms) -> {
      return seq.realAdvanced().sum(terms);
    });
  }

  private Computation<SReal, ProtocolBuilderNumeric> calculateTerm(DRes<SInt> o, DRes<SReal> p) {
    return builder -> {
      // Subtract expected
      DRes<SReal> z = builder.seq(new SampleLaplaceDistribution(2.0 / epsilon));

      DRes<SReal> e = builder.realNumeric().mult(BigDecimal.valueOf(n.longValue()), p);
      DRes<SReal> t = builder.realNumeric().sub(builder.realNumeric().fromSInt(o), e);

      // Sample noise
      t = builder.realNumeric().add(t, z);

      // Square numerator and divide by expected number of
      t = builder.realNumeric().mult(t, t);
      t = builder.realNumeric().div(t, e);

      return t;
    };
  }

}
