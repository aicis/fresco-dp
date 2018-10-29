package dk.alexandra.fresco.propability;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

public class SampleUniformDistribution implements Computation<SReal, ProtocolBuilderNumeric> {

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.realAdvanced().random(builder.getRealNumericContext().getPrecision());
  }

}
