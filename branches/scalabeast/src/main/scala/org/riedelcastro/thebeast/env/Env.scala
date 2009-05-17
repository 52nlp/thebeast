package org.riedelcastro.thebeast.env
/**
 * @author Sebastian Riedel
 */
trait Env {
  def apply[T](term: Term[T]): T = eval(term).get

  def eval[T](term: Term[T]): Option[T] = {
    term match {
      case Constant(x) => Some(x)
      case v: Var[_] => resolveVar[T](v)
      case FunApp(funTerm, argTerm) =>
        {
          val fun = eval(funTerm);
          val arg = eval(argTerm);
          if (fun.isDefined && arg.isDefined) Some(fun.get(arg.get)) else None
        }
      case Fold(funTerm,argTerms,initTerm) =>
        if (argTerms.isEmpty)
          eval(initTerm)
        else 
          eval(FunApp(FunApp(funTerm, Fold(funTerm,argTerms.drop(1),initTerm)),argTerms(0)))
    }
  }

  def ground[T](term: Term[T]): Term[T] = {
    term match {
      case FunApp(f, arg) => FunApp(ground(f), ground(arg))
      case Fold(f,args,init) => Fold(ground(f), args.map(a => ground(a)), ground(init))
      case c: Constant[_] => c
      case v: Var[_] => {val x = eval(v); if (x.isDefined) Constant(x.get) else v}
    }
  }

  def resolveVar[T](variable: Var[T]): Option[T]


}

class MutableEnv extends Env {
  private[this] type MutableMap = scala.collection.mutable.HashMap[Any, Any]
  private[this] val values = new MutableMap

  def resolveVar[T](variable: Var[T]) = values.get(variable).asInstanceOf[Option[T]]

  private[this] def getMap(variable: EnvVar[Any]): MutableMap = {
    variable match {
      case v: Var[_] => values.getOrElseUpdate(v, new MutableMap()).asInstanceOf[MutableMap]
      case FunAppVar(funVar, arg) => getMap(funVar).getOrElseUpdate(arg, new MutableMap()).asInstanceOf[MutableMap]
    }
  }

  def set[T](variable: EnvVar[T], value: T) {
    variable match {
      case v: Var[_] => values += Tuple2[Any, Any](v, value)
      case FunAppVar(funVar, arg) => getMap(funVar) += Tuple2[Any, Any](arg, value)
    }
  }

  def +=[T](mapping: Tuple2[EnvVar[T], T]) = set(mapping._1, mapping._2)

}