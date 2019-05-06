/*
 * ------------------------------------------------------------------------
 *  Copyright 2016 by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package inflor.core.utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.logging.Logger;

import fleur.core.data.FCSDimension;
import fleur.core.data.FCSFrame;
import fleur.core.data.Subset;
import inflor.core.logging.LogFactory;

public class FCSConcatenator implements BinaryOperator<FCSFrame> {
  
    private Logger logger = LogFactory.createLogger(this.getClass().toString());

	@Override
	public FCSFrame apply(FCSFrame f1, FCSFrame f2) {
	    
	  FCSFrame frame1 = f1.deepCopy();
      FCSFrame frame2 = f2.deepCopy();

    if (!frame1.getKeywords().containsKey(FCSUtilities.KEY_MERGE_MAP))
			initMergeMap(frame1);

    if (!frame2.getKeywords().containsKey(FCSUtilities.KEY_MERGE_MAP))
			initMergeMap(frame2);

		Map<String, String> mergedHeader = mergeHeaders(frame1.getKeywords(), frame2.getKeywords());
		FCSFrame mergedFrame = new FCSFrame(mergedHeader, frame1.getRowCount() + frame2.getRowCount());
		TreeSet<FCSDimension> mergedData = mergeData(frame1, frame2);
		mergedFrame.setData(mergedData);
		mergedFrame.setDisplayName("Concatenated Frame");
		ArrayList<Subset> mergedSubsets = mergeSubsets(frame1, frame2);
		mergedFrame.setSubsets(mergedSubsets);

		return mergedFrame;

	}

	private ArrayList<Subset> mergeSubsets(FCSFrame frame1, FCSFrame frame2) {
		List<Subset> s1 = frame1.getSubsets();
		List<Subset> s2 = frame2.getSubsets();
		if (s1!=null && s1.size()==s2.size()){
			ArrayList<Subset> s3 = new ArrayList<>();
			for (int i=0;i<s1.size();i++){
				Subset a = s1.get(i);
				Subset b = s2.get(i);
				BitSet newMask = new BitSet(a.getMembers().size() + b.getMembers().size());
				for (int j=0;j<newMask.size();j++){
					if (j < a.getMembers().size()&&a.getMembers().get(j)){
						newMask.set(j);
					} else if (j >= a.getMembers().size()&&b.getMembers().get(j-a.getMembers().size())){
						newMask.set(j);//TODO Test.
					}
				}
				Subset c = new Subset(a.getLabel(), newMask, a.getParentID(), a.getID(), a.getType(), a.getDimensions(), a.getDescriptors());
				s3.add(c);
			}
			return s3;
		} else {
			return null;
		}
	}

	private void initMergeMap(FCSFrame dataFrame) {
		dataFrame.getKeywords().put(FCSUtilities.KEY_MERGE_MAP, dataFrame.getDisplayName());
		FCSDimension mmDimension = new FCSDimension(dataFrame.getRowCount(), FCSUtilities.MERGE_DIMENSION_INDEX,
				FCSUtilities.MERGE_DIMENSION_NAME, null, 0, 0, 1);
		double[] zeros = new double[dataFrame.getRowCount()];
		for (int i = 0; i < zeros.length; i++) {
			zeros[i] = 0;
		}
		mmDimension.setData(zeros);
		dataFrame.addDimension(mmDimension);
	}

	private TreeSet<FCSDimension> mergeData(FCSFrame frame1, FCSFrame frame2) {
		TreeSet<FCSDimension> mergedData = new TreeSet<>();
		for (FCSDimension dimension : frame1.getData()) {
			FCSDimension mergedDimension;
			if (dimension.getShortName().equals(FCSUtilities.MERGE_DIMENSION_NAME)) {
				mergedDimension = mergeMapDimensions(frame1, frame2);
	            mergedData.add(mergedDimension);
			} else {
				Optional<FCSDimension> secondDimension = FCSUtilities.findCompatibleDimension(frame2, dimension.getShortName());
				if (secondDimension.isPresent()){
				  mergedDimension = mergeDimensions(dimension, secondDimension.get());
		          mergedData.add(mergedDimension);
				} else {
				  logger.info("Dimension not found: " + dimension.getDisplayName());
				}
			}
		}
		return mergedData;
	}

  private FCSDimension mergeDimensions(FCSDimension dimension, FCSDimension secondDimension) {
    FCSDimension mergedDimension;
    mergedDimension = new FCSDimension(dimension.size() + secondDimension.size(),
    		dimension.getIndex(), dimension.getShortName(), dimension.getStainName(), dimension.getPNEF1(),
    		dimension.getPNEF2(), dimension.getRange());
    double[] mergedArray = MatrixUtilities.appendVectors(dimension.getData(), secondDimension.getData());
    mergedDimension.setData(mergedArray);
    return mergedDimension;
  }

	private FCSDimension mergeMapDimensions(FCSFrame frame1, FCSFrame frame2) {

		String map1 = frame1.getKeywords().get(FCSUtilities.KEY_MERGE_MAP);
		String map2 = frame2.getKeywords().get(FCSUtilities.KEY_MERGE_MAP);
		String newMap = String.join(FCSUtilities.DELIMITER, new String[] { map1, map2 });

		double offset = map1.split(FCSUtilities.DELIMITER_REGEX).length;

		FCSDimension mergedDimension = new FCSDimension(frame1.getRowCount() + frame2.getRowCount(),
				FCSUtilities.MERGE_DIMENSION_INDEX, FCSUtilities.MERGE_DIMENSION_NAME, "", 0, 0,
				newMap.split(FCSUtilities.DELIMITER_REGEX).length);

		double[] mapDimesnion2 = frame2.getDimension(FCSUtilities.MERGE_DIMENSION_NAME).getData();

		double[] offsetDimensionData = new double[mapDimesnion2.length];

		for (int i = 0; i < mapDimesnion2.length; i++) {
			offsetDimensionData[i] = mapDimesnion2[i] + offset;
		}

		double[] newData = MatrixUtilities
				.appendVectors(frame1.getDimension(FCSUtilities.MERGE_DIMENSION_NAME).getData(), offsetDimensionData);
		mergedDimension.setData(newData);
		return mergedDimension;
	}

	private Map<String, String> mergeHeaders(Map<String, String> header1, Map<String, String> header2) {

		HashMap<String, String> mergedHeader = new HashMap<>();

		String map1 = header1.get(FCSUtilities.KEY_MERGE_MAP);
		String map2 = header2.get(FCSUtilities.KEY_MERGE_MAP);
		String mergedHeaderMapValue = updateMapHeader(map1, map2);

		for (Entry<String, String> entry : header1.entrySet()) {
			mergedHeader.put(entry.getKey(), entry.getValue());
		}
		for (Entry<String, String> entry : header2.entrySet()) {
			if (mergedHeader.containsKey(entry.getKey())) {
				mergedHeader.put(entry.getKey(), mergedHeader.get(entry.getKey()) + "||" + entry.getValue());
			} else {
				mergedHeader.put(entry.getKey(), entry.getValue());
			}
		}

		mergedHeader.put(FCSUtilities.KEY_MERGE_MAP, mergedHeaderMapValue);
		return mergedHeader;
	}

	private String updateMapHeader(String m1, String m2) {
		return String.join(FCSUtilities.DELIMITER, new String[] { m1, m2 });
	}
}