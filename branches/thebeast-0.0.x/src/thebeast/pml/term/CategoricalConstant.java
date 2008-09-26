package thebeast.pml.term;

import thebeast.nod.expression.ScalarExpression;
import thebeast.nod.type.CategoricalType;
import thebeast.pml.Type;

/**
 * Created by IntelliJ IDEA. User: s0349492 Date: 21-Jan-2007 Time: 18:26:05
 */
public class CategoricalConstant extends Constant {

  private String name;

  public CategoricalConstant(Type type, String name) {
    super(type);
    this.name = name;
  }

  public void acceptTermVisitor(TermVisitor visitor) {
    visitor.visitCategoricalConstant(this);
  }

  public String getName() {
    return name;
  }

  public ScalarExpression toScalar() {
    return factory.createCategoricalConstant((CategoricalType) getType().getNodType(),name);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CategoricalConstant that = (CategoricalConstant) o;

    if (!name.equals(that.name)) return false;

    return true;
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  public String toString(){
    return name;
  }

  public boolean isNonPositive() {
    return false;
  }

  public boolean isNonNegative() {
    return false;
  }

}
