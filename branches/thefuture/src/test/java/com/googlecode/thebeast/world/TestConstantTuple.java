package com.googlecode.thebeast.world;

import com.googlecode.thebeast.world.sql.SQLSignature;
import junit.framework.TestCase;

/**
 * Testing the methods of the ConstantTuple class.
 *
 * @author Sebastian Riedel
 * @see Tuple
 */
public final class TestConstantTuple extends TestCase {
  /**
   * A type with two user constants = {A,B}.
   */
  private UserType typeAB;

  /**
   * A type with two user constants = {C,D}.
   */
  private UserType typeBC;

  /**
   * Sets up a test fixture with a few types and constants to pick from.
   *
   * @throws Exception if something goes wrong.
   */
  protected void setUp() throws Exception {
    super.setUp();
    Signature signature = SQLSignature.createSignature();
    typeAB = signature.createType("type1", false, "A", "B");
    typeBC = signature.createType("type2", false, "C", "D");
  }

  /**
   * Tests whether the constructor creates a tuple with the right constants at
   * the right positions.
   */
  public void testConstructor() {
    Tuple tuple = new Tuple(
      typeAB.getConstant("A"),
      typeBC.getConstant("C"),
      typeAB.getConstant("B"));
    assertEquals(typeAB.getConstant("A"), tuple.get(0));
    assertEquals(typeBC.getConstant("C"), tuple.get(1));
    assertEquals(typeAB.getConstant("B"), tuple.get(2));
  }

  /**
   * Tests the size method.
   */
  public void testSize() {
    Tuple tuple = new Tuple(
      typeAB.getConstant("A"),
      typeBC.getConstant("C"),
      typeAB.getConstant("B"));
    assertEquals(3, tuple.size());
  }

  /**
   * Tests whether the getUserPredicate method is consistent with the get
   * method.
   */
  public void testGetUserPredicate() {
    Tuple tuple = new Tuple(
      typeAB.getConstant("A"),
      typeBC.getConstant("C"),
      typeAB.getConstant("B"));
    for (int i = 0; i < tuple.size(); ++i) {
      assertEquals(tuple.get(i), tuple.getUserConstant(i));
    }
  }
}