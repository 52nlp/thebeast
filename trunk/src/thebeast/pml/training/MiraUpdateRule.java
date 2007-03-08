package thebeast.pml.training;

import thebeast.pml.FeatureVector;
import thebeast.pml.SparseVector;
import thebeast.pml.Weights;
import thebeast.util.Profiler;
import thebeast.util.QP;
import thebeast.util.TreeProfiler;

import java.util.List;
import java.util.ArrayList;

/**
 * Update weights according to (Single-best) MIRA as presented in McDonald et al. 2005.
 *
 * @author Sebastian Riedel
 */
public class MiraUpdateRule implements UpdateRule {

  private boolean enforceSigns = false;

  //private int[] mapping;
  //private Weights weights;

  private static Profiler profiler = new TreeProfiler();

  public void endEpoch() {

  }


  public void update(FeatureVector gold, List<FeatureVector> candidates, List<Double> losses, Weights weights) {
    profiler.start("build difs");

    ArrayList<SparseVector> allVectors = new ArrayList<SparseVector>(candidates.size());
    int[][] nnIndices = new int[candidates.size() + 1][];
    int[][] npIndices = new int[candidates.size() + 1][];
    nnIndices[0] = gold.getNonnegative().getIndexArray();
    npIndices[0] = gold.getNonpositive().getIndexArray();
    int c = 1;
    for (FeatureVector candidate : candidates) {
      allVectors.add(candidate.getAll());
      if (enforceSigns) {
        nnIndices[c] = candidate.getNonnegative().getIndexArray();
        npIndices[c] = candidate.getNonpositive().getIndexArray();
      }
      ++c;
    }
    List<SparseVector> diffVectors = weights.add(gold.getAll(), -1.0, allVectors);

    int nnCount = 0;
    int npCount = 0;

    SparseVector nnOld = null;
    SparseVector npOld = null;

    int rebasedSize = diffVectors.get(0).size();
    int base[] = null;

    if (enforceSigns) {
      base = diffVectors.get(0).getIndexArray();
      int[] nnAllIndices = weights.intersectIndices(nnIndices);
      int[] npAllIndices = weights.intersectIndices(npIndices);
      nnOld = weights.getSubWeights(base, nnAllIndices);
      npOld = weights.getSubWeights(base, npAllIndices);
      nnCount = nnAllIndices.length;
      npCount = npAllIndices.length;
    }

    double[][] a = new double[candidates.size() + nnCount + npCount][];
    double[] b = new double[candidates.size() + nnCount + npCount];
    SparseVector[] diffs = new SparseVector[candidates.size() + nnCount + npCount];

    for (int candidateIndex = 0; candidateIndex < candidates.size(); ++candidateIndex) {
      SparseVector diffVector = diffVectors.get(candidateIndex);
      diffs[candidateIndex] = diffVector;
      double diffScore = weights.dotProduct(diffVector);
      double loss = losses.get(candidateIndex);
      a[candidateIndex] = diffVector.getValueArray();
      b[candidateIndex] = loss - diffScore;
    }

    if (enforceSigns) {
      int current = candidates.size();
      int[] nnRebased = nnOld.getIndexArray();
      int[] npRebased = npOld.getIndexArray();
      double[] nnWeights = nnOld.getValueArray();
      double[] npWeights = npOld.getValueArray();
      for (int i = 0; i < nnCount; ++i) {
        a[current] = new double[rebasedSize];
        a[current][nnRebased[i]] = 1.0;
        b[current] = -nnWeights[i];
        diffs[current] = new SparseVector(new int[]{base[nnRebased[i]]},1.0);
        ++current;
      }
      for (int i = 0; i < npCount; ++i) {
        a[current] = new double[rebasedSize];
        a[current][npRebased[i]] = 1.0;
        b[current] = npWeights[i];
        diffs[current] = new SparseVector(new int[]{base[npRebased[i]]},-1.0);
        ++current;
      }
    }


    profiler.end();
    profiler.start("qp");
    double[] alpha = QP.runHildreth(a, b);
    profiler.end();
    profiler.start("update");
    for (int i = 0; i < alpha.length; ++i) {
      weights.add(alpha[i], diffs[i]);
    }
    profiler.end();
    //System.out.println(profiler);
  }
}
