package org.riedelcastro.thebeast.learn

import org.specs._
import runner.JUnit4
import org.riedelcastro.thebeast.solve.ExhaustiveSearch
import org.riedelcastro.thebeast.env.vectors.UnitVector
import org.riedelcastro.thebeast.CitationMatchingFixtures
import org.riedelcastro.thebeast.env.TheBeastEnv

/**
 * @author Sebastian Riedel
 */

class OnlineLearnerTest extends JUnit4(OnlineLearnerSpecification)
object OnlineLearnerSpecification extends Specification with TheBeastEnv with CitationMatchingFixtures {

  "An Online Learner" should {
    "separate data if data is separable" in {

      //todo: factor out this training set
      var y1 = createWorldWhereABAreSimilarAndSame
      var y2 = createWorldWhereABAreSimilarButABCAreSame

      var features = 
        vectorSum(Citations,Citations)
                  {(c1,c2)=>$(similar(c1,c2) ~> same(c1,c2)) * UnitVector("similar")} +
        vectorSum(Citations,Citations,Citations)
                  {(c1,c2,c3)=>$((same(c1,c2) && same(c2,c3)) ~> same(c1,c3)) * UnitVector("trans")}
      var trainingSet = Seq(y1,y2).map(y => y.mask(Set(same)))

      var learner = new OnlineLearner

      learner.maxEpochs = 1

      var weights = learner.learn(features,trainingSet)

      for (y <- trainingSet){
        var score = (features dot weights).ground(y)
        var guess = ExhaustiveSearch.argmax(score).result
        y.unmasked(same).getSources(Some(true)) must_== guess(same).getSources(Some(true))
      }
    }
  }
}