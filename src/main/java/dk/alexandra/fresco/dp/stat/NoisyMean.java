package dk.alexandra.fresco.dp.stat;

import dk.alexandra.fresco.dp.laplace.LaplaceMechanism;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;
import java.util.List;

/**
 * Calculate the mean of a list of real numbers and
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class NoisyMean implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> data;
  private BigDecimal epsilon;
  private BigDecimal upperBound;

  public NoisyMean(List<DRes<SReal>> data, BigDecimal epsilon, BigDecimal upperBound) {
    this.data = data;
    this.epsilon = epsilon;
    this.upperBound = upperBound;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SReal> mean =
          seq.realNumeric().div(seq.realAdvanced().sum(data), BigDecimal.valueOf(data.size()));

      BigDecimal sensitivity = upperBound.setScale(seq.getRealNumericContext().getPrecision())
          .divide(BigDecimal.valueOf(data.size()));
      return seq.seq(new LaplaceMechanism(mean, epsilon, sensitivity));
    });
  }

}
