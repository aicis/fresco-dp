package dk.alexandra.fresco.dp.exponential;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.propability.SampleEnumeratedDistribution;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExponentialMechanism implements Computation<SInt, ProtocolBuilderNumeric> {

  private BigDecimal epsilon;
  private ScoreFunction function;

  public ExponentialMechanism(BigDecimal epsilon, ScoreFunction function) {
    this.epsilon = epsilon;
    this.function = function;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(r1 -> {
      List<DRes<SReal>> scores = IntStream.range(0, function.domainSize())
          .mapToObj(t -> r1.seq(function.computeScore(t))).collect(Collectors.toList());
      return () -> scores;
    }).par((r2, scores) -> {
      List<DRes<SReal>> propabilities =
          scores.stream().map(s -> r2.seq(computeExponent(s))).collect(Collectors.toList());
      return () -> propabilities;
    }).seq((r3, propabilities) -> {
      DRes<SReal> sum = r3.realAdvanced().sum(propabilities);
      return () -> new Pair<>(propabilities, sum);
    }).par((r4, pair) -> {
      List<DRes<SReal>> pmf = pair.getFirst().stream()
          .map(p -> r4.realNumeric().div(p, pair.getSecond())).collect(Collectors.toList());
      return () -> pmf;
    }).seq((r5, pmf) -> {
      return r5.seq(new SampleEnumeratedDistribution(pmf));
    });
  }

  private Computation<SReal, ProtocolBuilderNumeric> computeExponent(DRes<SReal> s) {
    return builder -> builder.realAdvanced()
        .exp(builder.realNumeric().div(builder.realNumeric().mult(epsilon, s),
            BigDecimal.valueOf(2.0).multiply(function.sensitivity())));
  }
}