package ict.ada.drawline.util;

import ict.ada.drawline.Document;
import ict.ada.drawline.result.Instance;

import java.util.Arrays;
import java.util.List;

/**
 * Mapping from character offset to byte offset.
 *
 * @author Jiyuan Lin
 */
public class ByteCalculator {

  protected int[] offsets;
  protected int[] byteOffsets;

  /**
   * Primary constructor which use a list of instances and the underlying document
   * to build a mapping between character offset and byte offset.
   *
   * @param instances
   * @param doc
   */
  public ByteCalculator(List<Instance> instances, Document doc) {
    int[] allOffsets = new int[instances.size() * 2];
    int n = 0;
    for (Instance instance : instances) {
      allOffsets[n++] = instance.getOffset();
      allOffsets[n++] = instance.getOffset() + instance.getLength();
    }
    Arrays.sort(allOffsets);
    offsets = ArrayUtil.unique(allOffsets);
    byteOffsets = new int[offsets.length];
    String text = doc.getText();
    n = 0;
    int bytes = 0;
    for (int i = 0; i < text.length() && n < offsets.length; ++i) {
      if (i == offsets[n]) {
        byteOffsets[n++] = bytes;
      }
      bytes += text.substring(i, i + 1).getBytes().length;
    }
    if (n < offsets.length) {
      byteOffsets[n] = bytes;
    }
  }

  /**
   * Get an instance's length in bytes.
   *
   * @param instance
   */
  public int getByteLength(Instance instance) {
    return getByteLength(instance.getOffset(), instance.getLength());
  }

  /**
   * Get an instance's offset in bytes.
   *
   * @param instance
   */
  public int getByteOffset(Instance instance) {
    return getByteOffset(instance.getOffset());
  }

  /**
   * Map character offset to byte offset.
   *
   * @param offset
   */
  public int getByteOffset(int offset) {
    int p = Arrays.binarySearch(offsets, offset);
    if (p < 0) {
      throw new IllegalArgumentException(String.format("Offset %d not in ByteCalculator", offset));
    }
    return byteOffsets[p];
  }

  /**
   * Map character length into byte length.
   *
   * @param offset
   * @param length
   */
  public int getByteLength(int offset, int length) {
    return getByteOffset(offset + length) - getByteOffset(offset);
  }
}
