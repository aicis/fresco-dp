package dk.alexandra.fresco.dp.exponential;

import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;

/**
 * Interface for score function used in the {@link ExponentialMechanism}.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public interface ScoreFunction {

	public int domainSize();

	public Computation<SReal, ProtocolBuilderNumeric> computeScore(int t);
	
	public BigDecimal sensitivity();
	
}
