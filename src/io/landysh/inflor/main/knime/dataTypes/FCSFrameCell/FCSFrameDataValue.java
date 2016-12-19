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

package io.landysh.inflor.main.knime.dataTypes.FCSFrameCell;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.ExtensibleUtilityFactory;

import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.knime.core.Icons;

public interface FCSFrameDataValue extends DataValue {
  UtilityFactory UTILITY = new FCSFrameUtilityFactory();
  
  FCSFrame getFCSFrameValue();

  final class FCSFrameUtilityFactory extends ExtensibleUtilityFactory {

    private static final Icon ICON = new ImageIcon(Icons.FCSFRAME_ICON_PATH);

    public FCSFrameUtilityFactory() {
      super(FCSFrameDataValue.class);
    }
    
    @Override
    public String getGroupName(){
      return "Biology";
    }
    
    @Override
    public Icon getIcon() {
      return ICON;
    }

    @Override
    public String getName() {
      return "FCS Frame";
    }

    @Override
    protected DataValueComparator getComparator() {
      return new DataValueComparator() {

        @Override
        protected int compareDataValues(final DataValue v1, final DataValue v2) {
          FCSFrameDataValue df1 = (FCSFrameDataValue) v1;
          FCSFrameDataValue df2 = (FCSFrameDataValue) v2;
          String s1 = df1.getFCSFrameValue().getPrefferedName();
          String s2 = df2.getFCSFrameValue().getPrefferedName();
          return s1.compareTo(s2);
        }
      };
    }

  }

  static boolean equalContent(final FCSFrameDataValue df1, final FCSFrameDataValue df2)
      throws IOException {
    return df1.getFCSFrameValue().getID().equals(df2.getFCSFrameValue().getID());
  }


}
