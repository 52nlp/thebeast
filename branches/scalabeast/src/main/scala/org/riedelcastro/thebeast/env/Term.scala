package org.riedelcastro.thebeast.env

/**
 * @author Sebastian Riedel
 */
trait Term[+T] {

  /**
   * The domain of a term is the set of all variables that appear in the term. If possible, this
   * method may return function application variables. 
   */
  def variables: Set[EnvVar[Any]]

  /**
   * The values of a term are all objects the term can be evaluated to
   */
  def values: Values[T]

  /**
   * Returns a term where each function application of a constant function with a constant argument
   * is replaced by a constant representing the application result 
   */
  def simplify: Term[T]

  /**
   * Replace the free variables in this term which are set in the specified environment
   */
  def ground(env: Env): Term[T]

  /**
   * Evaluates this term with respect to the given environment.
   */
  def eval(env: Env): Option[T]

  /**
   * Are there no free variables in this term 
   */
  def isGround: Boolean

  

}

case class Constant[T](val value: T) extends Term[T] {
  def variables = Set.empty

  def values = Values(value)

  def simplify = this

  def ground(env: Env) = this

  override def eval(env: Env) = Some(value)

  override def toString = value.toString

  def isGround = true
}




trait BoundedTerm[T] extends Term[T] {
  def upperBound: T
}





case class Var[+T](val name: String, override val values: Values[T]) extends Term[T] with EnvVar[T] {
  def variables = Set(this)

  def simplify = this

  override def toString = name

  def ground(env: Env) = {
    val x = env.eval(this);
    if (x.isDefined) Constant(x.get) else this
  }

  override def eval(env: Env) = env.resolveVar[T](this)


  def isGround = false
}

case class FunApp[T, R](val function: Term[T => R], val arg: Term[T]) extends Term[R] {
  override def eval(env: Env) = {
    val evalFun = env.eval(function);
    val evalArg = env.eval(arg);
    if (evalFun.isDefined && evalArg.isDefined) Some(evalFun.get(evalArg.get)) else None
  }

  def variables = {
    //if we have something like f(1)(2)(3) we should create a funapp variable
    if (isAtomic)
      Set(asFunAppVar)
    else
      function.variables ++ arg.variables
  }

  def values =
    function.values match {
      case functions: FunctionValues[_, _] => functions.range
      case _ => new ValuesProxy(function.values.flatMap(f => arg.values.map(v => f(v))))
    }


  def simplify =
    function.simplify match {
      case Constant(f) => arg.simplify match {
        case Constant(x) => Constant(f(x));
        case x => FunApp(Constant(f), x)
      }
      case f => FunApp(f, arg.simplify)
    }

  def isGround = arg.isGround && function.isGround

  def isAtomic: Boolean = arg.simplify.isInstanceOf[Constant[_]] &&
          (function.isInstanceOf[EnvVar[_]] ||
                  (function.isInstanceOf[FunApp[_, _]] && function.asInstanceOf[FunApp[_, _]].isAtomic))

  def asFunAppVar: FunAppVar[T, R] =
    if (function.isInstanceOf[EnvVar[_]])
      FunAppVar(function.asInstanceOf[EnvVar[T => R]], arg.simplify.asInstanceOf[Constant[T]].value)
    else
      FunAppVar(function.asInstanceOf[FunApp[Any, T => R]].asFunAppVar, arg.asInstanceOf[Constant[T]].value)


  def ground(env: Env) = FunApp(function.ground(env), arg.ground(env))

  override def toString = function.toString + "(" + arg.toString + ")"
}


case class Fold[R](val function: Term[R => (R => R)], val args: Seq[Term[R]], val init: Term[R]) extends Term[R] {
  def values =
    function.values match {
      case functions: FunctionValues[_, _] => functions.range match {
        case range: FunctionValues[_, _] => range.range.asInstanceOf[Values[R]]
        case _ => new ValuesProxy(recursiveValues(init.values, args))
      }
      case _ => new ValuesProxy(recursiveValues(init.values, args))
    }

  private[this] def recursiveValues(input: Iterable[R], args: Seq[Term[R]]): Iterable[R] = {
    if (args.isEmpty) input else {
      val myValues = for (f <- function.values; x <- input; a <- args(0).values) yield f(x)(a)
      recursiveValues(myValues, args.drop(1))
    }
  }

  def simplify = null

  def variables = function.variables ++ init.variables ++ args.flatMap(a => a.variables)

  def ground(env: Env) = Fold(function.ground(env), args.map(a => a.ground(env)), init.ground(env))

  override def toString = function.toString + "(" + init + "):" + args


  override def eval(env: Env) = {
    if (args.isEmpty)
      env.eval(init)
    else
      env.eval(FunApp(FunApp(function, Fold(function, args.drop(1), init)), args(0)))
  }


  def isGround = function.isGround && init.isGround && args.forall(x => x.isGround)
}




case class Quantification[R, V](val function: Term[R => (R => R)], val variable: Var[V], val formula: Term[R], val init: Term[R])
        extends Term[R] {
  lazy val unroll = {
    val env = new MutableEnv
    Fold(function, variable.values.map(value => {env += variable -> value; formula.ground(env)}).toSeq, init)
  }

  def simplify = unroll.simplify

  def variables = unroll.variables

  def values = unroll.values

  def ground(env: Env) = unroll.ground(env)

  def eval(env: Env) = unroll.eval(env)


  def isGround = {
    val env = new MutableEnv
    env += variable -> variable.values.defaultValue
    formula.ground(env).isGround
  }
}


sealed trait EnvVar[+T] extends Term[T]{
  /**
   *  The values of a variables are all objects the variable can be assigned to
   */
  def values: Values[T]

}

case class FunAppVar[T, R](val funVar: EnvVar[T => R], val argValue: T)
        extends FunApp(funVar, Constant(argValue))
        with EnvVar[R] {
  //def of[U](arg: U) = FunAppVar(this.asInstanceOf[EnvVar[U => Any]], argValue)

//  def values = range
//
//  def range: Values[R] = {
//    funVar match {
//      case x: Var[_] => x.values.asInstanceOf[FunctionValues[T, R]].range
//      case x: FunAppVar[_, _] => x.range.asInstanceOf[FunctionValues[T, R]].range
//    }
//
//  }


}

case class ConditionedTerm[T, C](term: Term[T], condition: Term[C])

sealed class Singleton extends Term[Singleton] {

  def simplify = this

  def isGround = true

  def variables = Set()

  def ground(env: Env) = this

  def eval(env: Env) = Some(this)

  def values = Values(this)

}

object Singleton {
  val Singleton = new Singleton
}






