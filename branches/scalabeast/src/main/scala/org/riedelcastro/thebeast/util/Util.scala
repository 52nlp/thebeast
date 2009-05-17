package org.riedelcastro.thebeast.util

/**
 * @author Sebastian Riedel
 */

object Util {
  object AllFunctions {
    def apply[T, R](domain: Iterable[T], range: Iterable[R]): Stream[Map[T, R]] = {
      domain.foldLeft(Stream.make(1, Map[T, R]()))
                {(result, d) => result.flatMap(f => range.map(r => f + (d -> r)))}
    }
  }

  object Cartesian {
    def cartesianProduct[T](args: Seq[Stream[T]]): Stream[Seq[T]] = {
      args.foldLeft(Stream.make(1, Seq[T]()))
                {(result, elements) => result.flatMap((tuple: Seq[T]) => elements.map(e => tuple ++ Seq(e)))}
    }

    def removeAll[T](i1: Iterable[T], i2: Iterable[T]): Iterable[T] =
      i1.filter(e => !i2.exists(_ == e))

//    def allWorlds(sig: Seq[FunctionSymbol[Any, Any]], obs: PartiallyObservedWorld): Stream[World] = {
//      cartesianProduct(sig.map(
//        f => AllFunctions(removeAll(f.domain, obs.getFunction(f).getObservedDomain()), f.range))).map(
//        (tuple: Seq[Map[Any, Any]]) => {
//          val world = new MutableWorld;
//          for (i <- 0 until tuple.size) {
//            world.setFunction(sig(i), WithBackoffObservation[Any, Any](tuple(i), obs.getFunction(sig(i))))
//          }
//          world
//        }
//        )
//    }

  }

  case class CartesianProduct1[T1](val _1: Iterable[T1]) extends Iterable[Tuple1[T1]] {
    def elements = _1.map(v => Tuple1(v)).elements

    def x[T2](_2: Iterable[T2]) = CartesianProduct2(_1, _2)
  }

  case class CartesianProduct2[T1, T2](val _1: Iterable[T1], val _2: Iterable[T2])
          extends Iterable[Tuple2[T1, T2]] {
    def elements = toStream.elements

    override def toStream = Cartesian.cartesianProduct(Seq(_1.toStream, _2.toStream)).map(
      seq => Tuple2(seq(0).asInstanceOf[T1], seq(1).asInstanceOf[T2]))
  }


}