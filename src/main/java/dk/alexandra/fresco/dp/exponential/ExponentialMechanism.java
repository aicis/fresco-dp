package dk.alexandra.fresco.dp.exponential;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.sampling.SampleCatagoricalDistribution;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation of the
 * <a href="https://en.wikipedia.org/wiki/Exponential_mechanism_(differential_privacy)">exponential
 * mechanism</a>.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class ExponentialMechanism implements Computation<SInt, ProtocolBuilderNumeric> {

  private ScoreFunction function;
  private BigDecimal exp;

  public ExponentialMechanism(BigDecimal epsilon, ScoreFunction function) {
    // TODO: We assume that the epsilon is known, but this will propably not always be the case
    this.function = function;
    this.exp = epsilon.divide(BigDecimal.valueOf(2.0).multiply(function.sensitivity()));
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(r1 -> {
      List<DRes<SReal>> scores = IntStream.range(0, function.domainSize())
          .mapToObj(t -> function.computeScore(t).buildComputation(r1)).collect(Collectors.toList());
      return () -> scores;
    }).par((r2, scores) -> {
      List<DRes<SReal>> propabilities =
          scores.stream().map(s -> computeExponent(s).buildComputation(r2)).collect(Collectors.toList());
      return () -> propabilities;
    }).seq((r3, propabilities) -> {
      return new SampleCatagoricalDistribution(propabilities, false).buildComputation(r3);
    });
  }

  private Computation<SReal, ProtocolBuilderNumeric> computeExponent(DRes<SReal> s) {
    return builder -> builder.realAdvanced().exp(builder.realNumeric().mult(exp, s));
  }
}
