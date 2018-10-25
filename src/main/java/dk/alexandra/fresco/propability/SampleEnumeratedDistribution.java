package dk.alexandra.fresco.propability;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample an element from an enumerated distribution.
 */
public class SampleEnumeratedDistribution implements Computation<SInt, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> propabilities;

  public SampleEnumeratedDistribution(List<DRes<SReal>> propabilities) {
    this.propabilities = propabilities;
  }
  
  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
      
      /*
       * Let p_0,...,p_{n-1} be the propabilities of drawing 0, ..., n-1 resp.
       * 
       * Now sample r uniformly in [0,1). Let c_i = p_0 + ... + p_i and let 
       * t_i = 0 if c_i <= r and t_i = 1 otherwise. 
       * 
       * We return Sum_{i=0}^n t_i which will be i with propability p_i
       */
      
      DRes<SReal> r = builder.realAdvanced().random(builder.getRealNumericContext().getPrecision());

      DRes<SReal> c = builder.realNumeric().known(BigDecimal.ZERO);
      List<DRes<SInt>> terms = new ArrayList<>();
      
      for (int i = 0; i < propabilities.size(); i++) {
        c = builder.realNumeric().add(c, propabilities.get(i));
        terms.add(builder.realNumeric().leq(c, r));
      }
      return builder.advancedNumeric().sum(terms);
  }

}
