package dk.alexandra.fresco.propability;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;
import java.math.BigInteger;

public class SampleLaplaceDistribution implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> b;
  private BigDecimal bKnown;

  public SampleLaplaceDistribution(DRes<SReal> b) {
    this.b = b;
  }

  public SampleLaplaceDistribution(BigDecimal b) {
    this.bKnown = b;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.par(par -> {
      DRes<SReal> exponential = par.seq(sampleExponentialDistribution());
      DRes<SReal> rademacher = par.seq(sampleRademacherDistribution());
      return () -> new Pair<>(exponential, rademacher);
    }).seq((seq, p) -> {
      return seq.realNumeric().mult(p.getFirst(), p.getSecond());
    });
  }

  private Computation<SReal, ProtocolBuilderNumeric> sampleExponentialDistribution() {
    return builder -> {
      DRes<SReal> uniform =
          builder.realAdvanced().random(builder.getRealNumericContext().getPrecision());
      DRes<SReal> logUniform = builder.realAdvanced().log(uniform);

      if (bKnown != null) {
        return builder.realNumeric().mult(bKnown, logUniform);
      } else {
        return builder.realNumeric().mult(b, logUniform);
      }
    };
  }

  private Computation<SReal, ProtocolBuilderNumeric> sampleRademacherDistribution() {
    return builder -> {
      DRes<SInt> bernoulli = builder.numeric().randomBit();
      return builder.realNumeric().fromSInt(builder.numeric()
          .sub(builder.numeric().mult(BigInteger.valueOf(2), bernoulli), BigInteger.ONE));
    };
  }

}
