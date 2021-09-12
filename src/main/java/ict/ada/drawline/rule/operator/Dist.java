package ict.ada.drawline.rule.operator;

/**
 * Dist operator which models DIST_K.
 * The definition of word is in Document.
 *
 * @author Jiyuan Lin
 */
public class Dist extends Operator {

  private int dist;

  public Dist(int dist) {
    this.dist = dist;
  }

  public Dist(Dist dist) {
    this(dist.getDist());
  }

  public void setDist(int dist) {
    this.dist = dist;
  }

  public void setMinDist(Dist that) {
    if (this.dist > that.dist) {
      this.dist = that.dist;
    }
  }

  public int getDist() {
    return dist;
  }

  @Override public String toString() {
    return String.format("Dist_%d", dist);
  }
}
