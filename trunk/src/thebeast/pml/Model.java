package thebeast.pml;

import thebeast.pml.formula.*;
import thebeast.pml.function.WeightFunction;
import thebeast.pml.predicate.Predicate;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class Model {

  private Signature signature;

  private LinkedList<UserPredicate>
          hidden = new LinkedList<UserPredicate>(),
          observed = new LinkedList<UserPredicate>(),
          instance = new LinkedList<UserPredicate>(),
          globalPreds = new LinkedList<UserPredicate>(),
          auxiliary = new LinkedList<UserPredicate>();


  private LinkedList<FactorFormula>
          nondeterministicFormulas = new LinkedList<FactorFormula>(),
          deterministicFormulas = new LinkedList<FactorFormula>(),
          factorFormulas = new LinkedList<FactorFormula>(),
          localFactorFormulas = new LinkedList<FactorFormula>(),
          directScoreFormulas = new LinkedList<FactorFormula>(),
          globalFactorFormulas = new LinkedList<FactorFormula>();

  private GroundAtoms globalAtoms;

  private LinkedList<WeightFunction>
          localWeightFunctions = new LinkedList<WeightFunction>(),
          nnLocalWeightFunctions = new LinkedList<WeightFunction>(),
          npLocalWeightFunctions = new LinkedList<WeightFunction>();

  private HashMap<String, FactorFormula> name2formula = new HashMap<String, FactorFormula>();

  /**
   * Creates a new Model with the given signature, i.e. it only contains predicates and functions which are described in
   * the signature object.
   *
   * @param signature the signature the predicates of this model use.
   */
  Model(Signature signature) {
    this.signature = signature;
  }

  /**
   * A hidden predicate is a predicate for which we don't have observed ground atoms. Instead, the solver is responsible
   * for inferring the true ground atoms of the given predicate.
   *
   * @param predicate the predicate to be defined as hidden.
   */
  public void addHiddenPredicate(UserPredicate predicate) {
    hidden.add(predicate);
    instance.add(predicate);
    Collections.sort(hidden);
    Collections.sort(instance);
  }

  /**
   * An observed predicate is a predicate for which we have observed data. The solver will take these as ground truth.
   *
   * @param predicate the predicate to be defined as observed.
   */
  public void addObservedPredicate(UserPredicate predicate) {
    observed.add(predicate);
    instance.add(predicate);
    Collections.sort(observed);
    Collections.sort(instance);
  }

  /**
   * An auxilary predicate is a predicate whose ground atoms are not part of the original data but generated by an
   * implication B => pred(term1,term2,...)
   *
   * @param predicate the predicate to be defined as auxilary.
   */
  public void addAuxilaryPredicate(UserPredicate predicate) {
    auxiliary.add(predicate);
    instance.add(predicate);
    Collections.sort(auxiliary);
    Collections.sort(instance);
  }


  /**
   * A global predicate is a predicate for which the same ground atoms hold over a whole corpus, not just for a single
   * problem instance.
   *
   * @param predicate the predicate to be defined as global.
   */
  public void addGlobalPredicate(UserPredicate predicate) {
    if (globalAtoms != null) {
      GroundAtoms newGlobal = signature.createGroundAtoms();
      newGlobal.load(globalAtoms, getGlobalPredicates());
      globalPreds.add(predicate);
      Collections.sort(globalPreds);
      globalAtoms = newGlobal;
    } else {
      globalPreds.add(predicate);
      Collections.sort(globalPreds);
    }

  }


  /**
   * All formulas of this model contain predicates and functions of the same signature. This method returns this
   * signature.
   *
   * @return The common signature containing all used predicates in this model.
   */
  public Signature getSignature() {
    return signature;
  }

  /**
   * Gets the formula with a given name
   *
   * @param name the name of the formula to return
   * @return the formula in this model with the specified name.
   */
  public FactorFormula getFactorFormula(String name) {
    return name2formula.get(name);
  }

  /**
   * Creates a new container for ground formulas parameterized by a weights container that associates ground formulas
   * with weights.
   *
   * @param weights the weights to use to weight the ground formulas with
   * @return a container for ground formulas of the formulas in this model
   */
  public GroundFormulas createGroundFormulas(Weights weights) {
    return new GroundFormulas(this, weights);
  }

  /**
   * Adds a factor formula to this model.
   *
   * @param factorFormula the formula to add.
   */
  public void addFactorFormula(FactorFormula factorFormula) {
    factorFormulas.add(factorFormula);
    name2formula.put(factorFormula.getName(), factorFormula);
    if (factorFormula.isLocal()) {
      localFactorFormulas.add(factorFormula);
      if (factorFormula.usesWeights()) {
        WeightFunction function = factorFormula.getWeightFunction();
        if (factorFormula.isAlwaysRewarding())
          nnLocalWeightFunctions.add(function);
        else if (factorFormula.isAlwaysPenalizing())
          npLocalWeightFunctions.add(function);
        else
          localWeightFunctions.add(function);
      } else {
        directScoreFormulas.add(factorFormula);
      }
    } else {
      globalFactorFormulas.add(factorFormula);
    }
    if (factorFormula.isDeterministic()) {
      deterministicFormulas.add(factorFormula);
    } else
      nondeterministicFormulas.add(factorFormula);

  }

  /**
   * Returns all factor formulas.
   *
   * @return all factor formulas contained in this model.
   */
  public List<FactorFormula> getFactorFormulas() {
    return factorFormulas;
  }

  /**
   * Get all formulas that only contain one atom.
   *
   * @return the local formulae of this model.
   */
  public List<FactorFormula> getLocalFactorFormulas() {
    return localFactorFormulas;
  }

  /**
   * Get the formulas that contain more than one atom.
   *
   * @return the global factor formulas.
   */
  public List<FactorFormula> getGlobalFactorFormulas() {
    return globalFactorFormulas;
  }


  /**
   * Return all deterministic formulas of this model.
   *
   * @return all deterministic formulas of this model.
   */
  public List<FactorFormula> getDeterministicFormulas() {
    return deterministicFormulas;
  }


  /**
   * Return all nondeterministic formulas (where weights are not infinite)
   *
   * @return a list of all all nondetermministic formulas.
   */
  public List<FactorFormula> getNondeterministicFormulas() {
    return nondeterministicFormulas;
  }

  /**
   * Global atoms are atoms that hold within all possible universes of the signature. Or in other words, if the model is
   * applied to some observation these ground atoms will be implicitely included.
   *
   * @return the set of ground atoms that hold in all universes of the model's signature.
   */
  public GroundAtoms getGlobalAtoms() {
    if (globalAtoms == null)
      globalAtoms = signature.createGroundAtoms();
    return globalAtoms;
  }


  public List<WeightFunction> getLocalNonnegativeWeightFunctions() {
    return nnLocalWeightFunctions;
  }

  public List<WeightFunction> getLocalNonpositiveWeightFunctions() {
    return npLocalWeightFunctions;
  }

  public List<WeightFunction> getLocalWeightFunctions() {
    return localWeightFunctions;
  }

  public void validateModel() throws InvalidModelException {
    if (hidden.size() == 0)
      throw new RuntimeException("Model does not contain any hidden predicates -> senseless");
    for (FactorFormula formula : localFactorFormulas) {
      if (!hidden.contains(formula.getLocalPredicate()))
        throw new InvalidModelException(formula.getLocalPredicate() + " is not defined as hidden but appears " +
                "as if it was hidden in " + formula);

    }
  }

  /**
   * Returns a list of formulas that generate auxilary predicate atoms.
   *
   * @return a list of formulas that generate auxilary predicate atoms.
   */
  public List<FactorFormula> getAuxiliaryGenerators() {
    HashSet<Predicate> set = new HashSet<Predicate>(auxiliary);
    LinkedList<FactorFormula> result = new LinkedList<FactorFormula>();
    for (FactorFormula formula : factorFormulas) {
      if (formula.isDeterministic() &&
              formula.getFormula() instanceof Implication) {
        Implication implication = (Implication) formula.getFormula();
        if (implication.getConclusion() instanceof PredicateAtom) {
          PredicateAtom atom = (PredicateAtom) implication.getConclusion();
          if (set.contains(atom.getPredicate())) result.add(formula);
        }
      }
    }
    return result;
  }

  public List<UserPredicate> getHiddenPredicates() {
    return hidden;
  }

  public List<UserPredicate> getObservedPredicates() {
    return observed;
  }

  public List<UserPredicate> getGlobalPredicates() {
    return globalPreds;
  }

  public List<UserPredicate> getInstancePredicates() {
    return instance;
  }

  public List<UserPredicate> getAuxiliaryPredicates() {
    return auxiliary;
  }

  /**
   * Returns the list of (local) factors that directly generate scores without using weights.
   *
   * @return the list of (local) factors that directly generate scores without using weights.
   */
  public List<FactorFormula> getDirectScoreFormulas() {
    return directScoreFormulas;
  }

  /**
   * Checks whether the formula contains hidden predicates.
   *
   * @param formula the formula to check.
   * @return true iff the truth value of this formula can't be evaluated in advance.
   */
  public boolean isHidden(BooleanFormula formula) {
    final boolean[] isHidden = new boolean[1];
    formula.acceptBooleanFormulaVisitor(new FormulaDepthFirstVisitor() {
      public void visitPredicateAtom(PredicateAtom predicateAtom) {
        if (predicateAtom.getPredicate() instanceof UserPredicate) {
          UserPredicate userPredicate = (UserPredicate) predicateAtom.getPredicate();
          if (hidden.contains(userPredicate)) isHidden[0] = true;
        }
      }
    });
    return isHidden[0];
  }

}
