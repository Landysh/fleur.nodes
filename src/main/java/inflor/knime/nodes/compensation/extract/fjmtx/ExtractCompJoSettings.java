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
 */
package inflor.knime.nodes.compensation.extract.fjmtx;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class ExtractCompJoSettings {
    public static final String KEY_FILEPATH = "Matrix filepath";
    public static final String DEFAULT_FILEPATH = "";

    private final SettingsModelString m_FileLocation =
        new SettingsModelString(KEY_FILEPATH, DEFAULT_FILEPATH);
    
    public String getFilePath() {
      return m_FileLocation.getStringValue();
    }
    
    public void setFilePath(String newPath) {
      m_FileLocation.setStringValue(newPath);
    }

    public void save(NodeSettingsWO settings) {
      settings.addString(KEY_FILEPATH, m_FileLocation.getStringValue());
    }

    public void load(NodeSettingsRO settings) throws InvalidSettingsException {
      m_FileLocation.setStringValue(settings.getString(KEY_FILEPATH));
    }

    public void validate() throws InvalidSettingsException{
      // TODO check if the settings could be applied to our model
      // e.g. if the count is in a certain range (which is ensured by the
      // SettingsModel).
      // Do not actually set any values of any member variables.
    }
}
