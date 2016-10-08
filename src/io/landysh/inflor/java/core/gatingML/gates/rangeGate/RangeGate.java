package io.landysh.inflor.java.core.gatingML.gates.rangeGate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Optional;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;
import io.landysh.inflor.java.core.gatingML.gates.BitSetAccumulator;
import io.landysh.inflor.java.core.gatingML.gates.BitSetOperator;
import io.landysh.inflor.java.core.utils.FCSUtils;

public class RangeGate extends AbstractGate {
	/**
	 *  TODO Why is this automatically created.
	 */
	private static final long serialVersionUID = -4829977491684130257L;
	ArrayList<RangeDimension> dimensions = new ArrayList<RangeDimension>();

	public RangeGate(String priorUUID, String[] names, double[] min, double[] max) {
		super(priorUUID);
		if (names.length<1){
			throw new IllegalArgumentException("CODING ERROR: Range gate array parameters must be of the same length and >=1");
		}
		for (int i=0;i<names.length;i++){
			dimensions.add(new RangeDimension(names[i], min[i], max[i]));
		}
	}
	
	public RangeGate() {
		super(null);
	}

	@Override
	public BitSet evaluate(ColumnStore FCSData) {
		
		int rowCount = FCSData.getRowCount();
		final BitSet result = new BitSet(rowCount);
		result.set(0, result.size()-1);
		
		for (RangeDimension dim: dimensions){
			String name = dim.getName();
			double[] data = FCSUtils.findCompatibleDimension(FCSData.getData(), name).getData();
			BitSet dimesnionBits = dim.evaluate(data);
			result.and(dimesnionBits);
		}
		return result;
	}
	
	
	public BitSet evaluateParallel(ColumnStore FCSData) {				
		Optional<BitSet> possibleResult = dimensions.parallelStream()
				.map(dimension -> dimension.evaluate(FCSUtils.findCompatibleDimension(FCSData.getData(), dimension.getName()).getData()))
				.reduce(new BitSetAccumulator(BitSetOperator.AND));
		if (possibleResult.isPresent()){
			return possibleResult.get();
		} else {
			throw new RuntimeException("Shit happened");
		}
		
	}

	public String[] getDimensionNames() {
		String[] names = (String[]) dimensions.stream().map(dim -> dim.getName()).toArray();
		return names;
	}

	@Override
	public Element toXMLElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validate() throws IllegalStateException {
		if (dimensions == null || dimensions.size() <= 1) {
			final String message = "A range gate must have at least 1 dimension";
			final IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}
}
// EOF