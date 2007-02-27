package thebeast.pml.corpora;

import thebeast.nod.FileSink;
import thebeast.nod.FileSource;
import thebeast.pml.GroundAtoms;
import thebeast.pml.Signature;
import thebeast.pml.TheBeast;

import java.io.File;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A DumpedCorpus is backed by a binary dump of the data of the corpus. It can be configured to only ever use a certain
 * amount of memory and to stream in and out bits which are needed/not needed.
 *
 * @author Sebastian Riedel
 */
public class DumpedCorpus extends AbstractCollection<GroundAtoms> implements Corpus {

  private ArrayList<GroundAtoms> active;
  private int size;
  private int byteSize = 0;
  private FileSource fileSource;
  private int activeCount;
  private Signature signature;
  private boolean iterating = false;
  private File file;

  public DumpedCorpus(File file, Corpus corpus, int from, int to, int maxByteSize) throws IOException {
    this.file = file;
    FileSink fileSink = TheBeast.getInstance().getNodServer().createSink(file, 1024);
    active = new ArrayList<GroundAtoms>(to - from);
    this.signature = corpus.getSignature();
    Iterator<GroundAtoms> iter = corpus.iterator();
    for (int i = 0; i < from; ++i) iter.next();
    this.size = to - from;
    int numDumps = 0;
    for (int i = 0; i < size; ++i) {
      GroundAtoms atoms = iter.next();
      int memUsage = atoms.getMemoryUsage();
      if (byteSize + memUsage > maxByteSize) {
        activeCount += active.size();
        dump(fileSink);
        ++numDumps;
        byteSize = 0;
      }
      active.add(atoms);
      byteSize += memUsage;
    }
    activeCount = numDumps == 0 ? size : activeCount / numDumps;
    dump(fileSink);
    for (int i = 0; i < activeCount; ++i)
      active.add(signature.createGroundAtoms());
    fileSink.flush();
    fileSource = TheBeast.getInstance().getNodServer().createSource(file, 1024);
  }

  public DumpedCorpus(File file, Corpus corpus, int maxByteSize) throws IOException {
    this.file = file;
    FileSink fileSink = TheBeast.getInstance().getNodServer().createSink(file, 1024);
    active = new ArrayList<GroundAtoms>(10000);
    this.signature = corpus.getSignature();
    this.size = 0;
    int numDumps = 0;
    for (GroundAtoms atoms : corpus) {
      int memUsage = atoms.getMemoryUsage();
      if (byteSize + memUsage > maxByteSize) {
        activeCount += active.size();
        dump(fileSink);
        ++numDumps;
        byteSize = 0;
      }
      active.add(atoms);
      byteSize += memUsage;
      ++size;
    }
    activeCount = numDumps == 0 ? size : activeCount / numDumps;
    dump(fileSink);
    for (int i = 0; i < activeCount; ++i)
      active.add(signature.createGroundAtoms());
    fileSink.flush();
    fileSource = TheBeast.getInstance().getNodServer().createSource(file, 1024);
  }


  private void dump(FileSink fileSink) throws IOException {
    for (GroundAtoms atoms : active) {
      atoms.write(fileSink);
    }
    active.clear();
  }

  public String toString() {
    return "DumpedCorpus size :" + size + " activeCount: " + activeCount;
  }

  public synchronized Iterator<GroundAtoms> iterator() {
    if (iterating)
      throw new RuntimeException("Dumped Corpus can only have one active iterator at a time!");
    iterating = true;
    //todo: why does reset not work?
    fileSource = TheBeast.getInstance().getNodServer().createSource(file, 1024);
//    fileSource.reset();
    //fill up initial actives
    try {
      if (activeCount >= size) {
        for (int i = 0; i < size; ++i) {
          GroundAtoms atoms = active.get(i);
          atoms.read(fileSource);
          //System.out.println(atoms.getMemoryUsage());
        }
        iterating = false;
        return active.subList(0, size).iterator();
      } else {
        for (GroundAtoms atoms : active) {
          atoms.read(fileSource);
        }
        return new Iterator<GroundAtoms>() {
          Iterator<GroundAtoms> delegate = active.iterator();
          int current = 0;

          public boolean hasNext() {
            return current < size;
          }

          public GroundAtoms next() {
            //System.out.println(current + " of " + size);
            if (delegate.hasNext()) {
              ++current;
              if (!hasNext()) {
                iterating = false;
              }
              return delegate.next();
            }
            update();
            ++current;
            if (!hasNext()) {
              iterating = false;
            }
            return delegate.next();
          }

          public void update() {
            try {
              if (activeCount >= size - current) {
                for (int i = 0; i < size - current; ++i) {
                  GroundAtoms atoms = active.get(i);
                  atoms.read(fileSource);
                }
                delegate = active.subList(0, size - current).iterator();
              } else {
                for (GroundAtoms atoms : active) {
                  atoms.read(fileSource);
                }
                delegate = active.iterator();
              }
            } catch (IOException e) {
              e.printStackTrace();
            }

          }

          public void remove() {

          }
        };
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public int size() {
    return size;
  }

  public Signature getSignature() {
    return signature;
  }

  public ListIterator<GroundAtoms> listIterator() {
    return null;
  }

  public int getUsedMemory() {
    int usage = 0;
    for (GroundAtoms atoms : active)
      usage+=atoms.getMemoryUsage();
    return usage;
  }
}