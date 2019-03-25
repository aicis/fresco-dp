package dk.alexandra.fresco.dp.laplace;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.sampling.SampleLaplaceDistribution;
import java.math.BigDecimal;

public class LaplaceMechanism implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> input;
  private BigDecimal sensitivity;
  private BigDecimal epsilon;

  public LaplaceMechanism(DRes<SReal> input, BigDecimal epsilon, BigDecimal sensitivity) {
    // TODO: We assume that the epsilon is known, but this will maybe not always be the case.
    this.input = input;
    this.sensitivity = sensitivity;
    this.epsilon = epsilon;
  }
  
  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SReal> noise = seq.seq(new SampleLaplaceDistribution(sensitivity.divide(epsilon)));
      return seq.realNumeric().add(input, noise);
    });
  }

}