package dk.alexandra.fresco.propability;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample an element from a discrete distribution.
 */
public class SampleEnumeratedDistribution implements Computation<SInt, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> propabilities;
  private boolean normalized;

  /**
   * 
   * @param propabilities The i'th element of this list is the propabily of drawing i from this
   *        distribution.
   * @param normalized Does the propabilities sum to 1?
   */
  public SampleEnumeratedDistribution(List<DRes<SReal>> propabilities, boolean normalized) {
    this.propabilities = propabilities;
    this.normalized = normalized;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {

    /*
     * Let p_0,...,p_{n-1} be the propabilities of drawing 0, ..., n-1 resp.
     * 
     * Now sample r uniformly in [0,1). Let c_i = p_0 + ... + p_i and let t_i = 0 if c_i <= r and 1
     * otherwise.
     * 
     * We return Sum_{j=0}^n t_j which will be i with propability p_i
     */

    DRes<SReal> r = builder.seq(new SampleUniformDistribution());

    if (!normalized) {
      DRes<SReal> sum = builder.realAdvanced().sum(propabilities);
      r = builder.realNumeric().mult(sum, r);
    }

    DRes<SReal> c = propabilities.get(0);
    List<DRes<SInt>> terms = new ArrayList<>();

    for (int i = 0; i < propabilities.size(); i++) {
      if (i > 0) {
        c = builder.realNumeric().add(c, propabilities.get(i));
      }
      terms.add(builder.realNumeric().leq(c, r));
    }
    return builder.advancedNumeric().sum(terms);
  }

}
