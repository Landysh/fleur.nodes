package io.landysh.inflor.main.knime.nodes.summaryStats;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import io.landysh.inflor.main.core.dataStructures.FCSDimension;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.dataStructures.Subset;
import io.landysh.inflor.main.core.utils.FCSUtilities;

public enum StatType {
    MEDIAN {
      @Override
      public Double evaluate(FCSFrame dataFrame, String refDimension, String[] args) {
        FCSDimension referenceDim = FCSUtilities.findCompatibleDimension(dataFrame, refDimension);
        double[] data = referenceDim.getData();
        Median median = new Median();
        Double result = median.evaluate(data);  
        return result;
      }

      @Override
      public String getLabel(String subsetLabel, String dimensionLabel, String[] args) {
        return subsetLabel + " - " + this + ": " + dimensionLabel;
      }
    }, MEAN {
      @Override
      public Double evaluate(FCSFrame dataFrame, String refDimension, String[] args) {
        FCSDimension referenceDim = FCSUtilities.findCompatibleDimension(dataFrame, refDimension);
        double[] data = referenceDim.getData();
        Mean mean = new Mean();
        Double result = mean.evaluate(data);          
        return result;
      }

      @Override
      public String getLabel(String subsetLabel, String dimensionLabel, String[] args) {
        return subsetLabel + " - " + this + ": " + dimensionLabel;
      }
      
    }, STDEV {
      @Override
      public Double evaluate(FCSFrame dataFrame, String refDimension, String[] args) {
        FCSDimension referenceDim = FCSUtilities.findCompatibleDimension(dataFrame, refDimension);
        double[] data = referenceDim.getData();
        StandardDeviation stdev = new StandardDeviation();
        Double result = stdev.evaluate(data);              
        return result;
      }

      @Override
      public String getLabel(String subsetLabel, String dimensionLabel, String[] args) {
        return subsetLabel + " - " + this + ": " + dimensionLabel;
      }
    }, CV {
      @Override
      public Double evaluate(FCSFrame dataFrame, String refDimension, String[] args) {
        FCSDimension referenceDim = FCSUtilities.findCompatibleDimension(dataFrame, refDimension);
        double[] data = referenceDim.getData();
        StandardDeviation stdev = new StandardDeviation();
        Mean mean = new Mean();
        Double result = stdev.evaluate(data)/mean.evaluate(data);
        return result;
      }

      @Override
      public String getLabel(String subsetLabel, String dimensionLabel, String[] args) {
        return subsetLabel + " - " + this + ": " + dimensionLabel;
      }
    }, PERCENTILE {
      @Override
      public Double evaluate(FCSFrame dataFrame, String refDimension, String[] args) {
        FCSDimension referenceDim = FCSUtilities.findCompatibleDimension(dataFrame, refDimension);
        double[] data = referenceDim.getData();
        Double targetPercentile = Double.parseDouble(args[0]);
        Double percentileMin = (double) 0;
        Double percentileMax = (double) 100;
        Double result;
        if (targetPercentile >= percentileMin && targetPercentile<=percentileMax){
          Percentile percentile = new Percentile();
          result = percentile.evaluate(data, targetPercentile);
          return result;
        } else {
          throw new RuntimeException("Invalid target percentile.");
        }
      }

      @Override
      public String getLabel(String subsetLabel, String dimensionLabel, String[] args) {
        return subsetLabel + " - " + this + args[0] + ": " + dimensionLabel;
      }
    }, FREQUENCY {
      @Override
      public Double evaluate(FCSFrame dataFrame, String refDimension, String[] args) {
        String targetSubset = args[0];
        String parentSubset = args[1];
        List<Subset> subsets = dataFrame.getSubsets();
        
        if (targetSubset!=null&&parentSubset!=null){
          Optional<Subset> child = subsets
            .stream()
            .filter(subset -> subset.getID().equals(targetSubset))
            .findAny();
          
          Optional<Subset> parent = subsets
              .stream()
              .filter(subset -> subset.getID().equals(parentSubset))
              .findAny();
          
          if (child.isPresent() && parent.isPresent()){
            List<Subset> childAncestors = child
              .get()
              .findAncestors(subsets);
            
            List<Subset> parentAncestors = parent
                .get()
                .findAncestors(subsets);          
            
            if (childAncestors.contains(parent)){
              BitSet childMask = child.get().evaluate(childAncestors);
              BitSet parentMask = parent.get().evaluate(parentAncestors);
              Double result = (double) (childMask.cardinality()/parentMask.cardinality());
              return result;
            }
          }
        }
        return null;
      }

      @Override
      public String getLabel(String subsetLabel, String dimensionLabel, String[] args) {
        return subsetLabel + "Frequency of Parent";
      }
    };

    public abstract Double evaluate(FCSFrame dataFrame, String refDimension, String[] args);

    public abstract String getLabel(String subsetLabel, String dimensionLabel, String[] args);
}
