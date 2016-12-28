/*
 * ------------------------------------------------------------------------
 *  Copyright by Aaron Hart
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
 * Created on December 13, 2016 by Aaron Hart
 */
package main.java.inflor.knime.data.type.cell.fcs;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.AbstractDataValueRendererFactory;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.MultiLineStringValueRenderer;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.data.Subset;
import main.java.inflor.knime.core.Icons;

@SuppressWarnings("serial")
public final class FCSFrameDataValueRenderer extends MultiLineStringValueRenderer {

    public static final class FCSSummeryFactory extends AbstractDataValueRendererFactory {
        private static final String DESCRIPTION = "FCS Summary";

        @Override
        public String getDescription() {
            return DESCRIPTION;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataValueRenderer createRenderer(final DataColumnSpec colSpec) {
            return new FCSFrameDataValueRenderer(DESCRIPTION);
        }
    }

    private static final ImageIcon ICON = new ImageIcon(Icons.FCSFRAME_ICON_PATH) ;


    public FCSFrameDataValueRenderer(String description) {
        super(description);
    }

    /** {@inheritDoc} */
    @Override
    protected void setValue(final Object value) {
        if (value instanceof FCSFrameDataValue) {
          FCSFrame dataFrame = ((FCSFrameDataValue) value).getFCSFrameValue();
          String s = createFileSummary(dataFrame); 
          s+= createDimensionSummary(dataFrame);    
          s+= createSubsetSummary(dataFrame);
          super.setValue(s);
        } else {
            super.setValue("?");
        }
    }

    private String createSubsetSummary(FCSFrame dataFrame) {
      String subsetSummary = "\n";
      List<Subset> subsets = dataFrame.getSubsets();
      if (subsets!=null){
        for (Subset sub:  dataFrame.getSubsets()){
          subsetSummary+= sub.getLabel();
          subsetSummary+=",";
        }
      }
      return subsetSummary.trim();
    }

    private String createDimensionSummary(FCSFrame dataFrame) {
      String dimensionSummary = "\n";
      FCSDimension[] arr = dataFrame.getData().toArray(new FCSDimension[dataFrame.getDimensionCount()]);
      for (FCSDimension dim : arr){
        dimensionSummary += dim.getDisplayName();
        dimensionSummary +="    ";
        dimensionSummary +=dim.getPreferredTransform().toString();
        dimensionSummary +="\n";
      }
      return dimensionSummary;
    }

    private String createFileSummary(FCSFrame dataFrame) {
      String fileSummary = "";
      fileSummary+=dataFrame.getPrefferedName();
      fileSummary+="\n";
      fileSummary+= Integer.toString(dataFrame.getRowCount());
      fileSummary+="\n";
      fileSummary+= dataFrame.getKeywordValue("$CYT");
      fileSummary+="\n";
      fileSummary+= dataFrame.getKeywordValue("$DATE");
      fileSummary+="\n";
      fileSummary+= dataFrame.getKeywordValue("$BTIM");
      fileSummary+=" - ";
      fileSummary+= dataFrame.getKeywordValue("$ETIM");
      return fileSummary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }
    
    @Override
    public Icon getIcon(){
      return ICON;
    }
    
}
