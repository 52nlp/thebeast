package thebeast.pml.training;

import thebeast.pml.SparseVector;
import thebeast.pml.GroundAtoms;
import thebeast.pml.LocalFeatures;

/**
 * Created by IntelliJ IDEA. User: s0349492 Date: 09-Feb-2007 Time: 12:28:40
 */
public class TrainingInstance {

  private SparseVector gold;
  private GroundAtoms data;
  private LocalFeatures features;

  public TrainingInstance(GroundAtoms data, LocalFeatures features, SparseVector gold) {
    this.data = data;
    this.features = features;
    this.gold = gold;
  }


  public GroundAtoms getData() {
    return data;
  }

  public LocalFeatures getFeatures() {
    return features;
  }

  public SparseVector getGold() {
    return gold;
  }

  public int getMemoryUsage(){
    return data.getMemoryUsage() + features.getMemoryUsage() + gold.getMemoryUsage();
  }
}
