package dk.alexandra.fresco.dp.exponential;

import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;

public interface ScoreFunction {

	public int domainSize();

	public Computation<SReal, ProtocolBuilderNumeric> computeScore(int t);
	
	public BigDecimal sensitivity();
	
}
