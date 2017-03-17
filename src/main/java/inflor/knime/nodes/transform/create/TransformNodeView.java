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
package inflor.knime.nodes.transform.create;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "Transorm" Node.
 * 
 *
 * @author Aaron Hart
 */
public class TransformNodeView extends NodeView<TransformNodeModel> {

  /**
   * Creates a new view.
   * 
   * @param nodeModel The model (class: {@link TransformNodeModel})
   */
  protected TransformNodeView(final TransformNodeModel nodeModel) {
    super(nodeModel);
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void modelChanged() {
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onClose() {
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onOpen() {
    // TODO: generated method stub
  }

}

