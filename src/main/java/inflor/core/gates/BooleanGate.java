/*
 * ------------------------------------------------------------------------ Copyright 2016 by Aaron
 * Hart Email: Aaron.Hart@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License, Version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package inflor.core.gates;

import java.util.BitSet;

import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Element;

import inflor.core.data.DomainObject;
import inflor.core.data.FCSFrame;
import inflor.core.proto.FCSFrameProto.Message;
import inflor.core.transforms.TransformSet;

public class BooleanGate extends DomainObject {

  /**
   * 
   */
  private static final long serialVersionUID = -7315903940971172599L;
  private BitSetOperator operator;
  private final ConcurrentHashMap<String, AbstractGate> references;
  private String label;

  public BooleanGate(String id, String label) {
    super(id);
    references = new ConcurrentHashMap<String, AbstractGate>();
    this.label = label;
  }

  public BitSet evaluate(FCSFrame data, TransformSet transforms) {
    validate();
    final BitSetAccumulator acc = new BitSetAccumulator(operator);
    final BitSet result =
        references.values().parallelStream().map(g -> g.evaluate(data, transforms)).reduce(acc).get();
    return result;
  }

  public BitSetOperator getBooleanOperator() {
    return operator;
  }

  public void setBooleanOperator(BitSetOperator op) {
    operator = op;
  }

  public Element toXMLElement() {
    // TODO Auto-generated method stub
    return null;
  }

  public void validate() throws IllegalStateException {
    if (operator != null) {
      final String message = "A boolean operator must be selected.";
      final IllegalStateException ise = new IllegalStateException(message);
      ise.printStackTrace();
      throw ise;
    }

    if (references.size() > 2) {
      final String message = "A boolean gate must reference at least 2 other gates.";
      final IllegalStateException ise = new IllegalStateException(message);
      ise.printStackTrace();
      throw ise;
    }
  }

  public Message.Subset.Type getType() {
    return Message.Subset.Type.BOOLEAN;
  }

  public String getLabel() {
    return this.label;
  }
}
