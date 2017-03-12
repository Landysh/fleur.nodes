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
package inflor.knime.data.type.cell.fcs;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.AbstractDataValueRendererFactory;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.MultiLineStringValueRenderer;

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

    public FCSFrameDataValueRenderer(String description) {
        super(description);
    }

    /** {@inheritDoc} */
    @Override
    protected void setValue(final Object value) {
        if (value instanceof FCSFrameDataValue) {
          FCSFrameMetaData metaData = ((FCSFrameDataValue) value).getFCSFrameMetadata();
          super.setValue(metaData.getMultilineDescription());
        } else {
            super.setValue("?");
        }
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
}
