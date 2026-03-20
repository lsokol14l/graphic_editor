package by.michael;

import java.awt.image.BufferedImage;

public class ImageProcessor {

  private static final int CLAMP_MAX = 255;

  public enum Operation {
    SUM,
    MULTIPLY,
    AVERAGE,
    MINIMUM,
    MAXIMUM,
    MASK
  }

  public enum ColorChannels {
    RGB(true, true, true),
    R(true, false, false),
    G(false, true, false),
    B(false, false, true),
    RG(true, true, false),
    GB(false, true, true),
    RB(true, false, true);

    private final boolean useR;
    private final boolean useG;
    private final boolean useB;

    ColorChannels(boolean useR, boolean useG, boolean useB) {
      this.useR = useR;
      this.useG = useG;
      this.useB = useB;
    }

    public boolean useR() {
      return useR;
    }

    public boolean useG() {
      return useG;
    }

    public boolean useB() {
      return useB;
    }
  }

  public static BufferedImage process(
      BufferedImage img1,
      BufferedImage img2,
      Operation operation,
      ColorChannels channels,
      MaskFactory.MaskShape maskShape) {
    if (img1 == null) {
      throw new IllegalArgumentException("Первое изображение обязательно.");
    }

    if (operation == Operation.MASK) {
      boolean[][] mask = MaskFactory.createMask(maskShape, img1.getWidth(), img1.getHeight());
      return applyMaskWithChannels(img1, mask, channels);
    }

    if (img2 == null) {
      throw new IllegalArgumentException("Для данной операции нужно второе изображение.");
    }

    BufferedImage base = selectLargerImage(img1, img2);
    BufferedImage influence = (base == img1) ? img2 : img1;
    return processBinary(base, influence, operation, channels);
  }

  private static BufferedImage processBinary(
      BufferedImage base,
      BufferedImage influence,
      Operation operation,
      ColorChannels channels) {
    int width = base.getWidth();
    int height = base.getHeight();
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int baseRgb = base.getRGB(x, y);
        int influenceRgb = getScaledPixel(influence, x, y, width, height);

        int br = (baseRgb >> 16) & 0xFF;
        int bg = (baseRgb >> 8) & 0xFF;
        int bb = baseRgb & 0xFF;

        int ir = (influenceRgb >> 16) & 0xFF;
        int ig = (influenceRgb >> 8) & 0xFF;
        int ib = influenceRgb & 0xFF;

        int outR = channels.useR() ? combineChannel(br, ir, operation) : br;
        int outG = channels.useG() ? combineChannel(bg, ig, operation) : bg;
        int outB = channels.useB() ? combineChannel(bb, ib, operation) : bb;

        result.setRGB(x, y, (outR << 16) | (outG << 8) | outB);
      }
    }

    return result;
  }

  private static BufferedImage applyMaskWithChannels(
      BufferedImage img, boolean[][] mask, ColorChannels channels) {
    int width = img.getWidth();
    int height = img.getHeight();
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int rgb = img.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        if (!mask[y][x]) {
          if (channels.useR()) {
            r = 0;
          }
          if (channels.useG()) {
            g = 0;
          }
          if (channels.useB()) {
            b = 0;
          }
        }

        result.setRGB(x, y, (r << 16) | (g << 8) | b);
      }
    }
    return result;
  }

  private static int combineChannel(int base, int influence, Operation operation) {
    return switch (operation) {
      case SUM -> clamp(base + influence);
      case MULTIPLY -> (base * influence) / 255;
      case AVERAGE -> (base + influence) / 2;
      case MINIMUM -> Math.min(base, influence);
      case MAXIMUM -> Math.max(base, influence);
      case MASK -> base;
    };
  }

  private static int getScaledPixel(BufferedImage img, int x, int y, int targetW, int targetH) {
    int srcX = mapCoordinate(x, targetW, img.getWidth());
    int srcY = mapCoordinate(y, targetH, img.getHeight());
    return img.getRGB(srcX, srcY);
  }

  private static int mapCoordinate(int value, int maxFrom, int maxTo) {
    if (maxFrom <= 1 || maxTo <= 1) {
      return 0;
    }
    return (int) Math.round(value * (maxTo - 1.0) / (maxFrom - 1.0));
  }

  private static BufferedImage selectLargerImage(BufferedImage img1, BufferedImage img2) {
    long area1 = (long) img1.getWidth() * img1.getHeight();
    long area2 = (long) img2.getWidth() * img2.getHeight();
    return area1 >= area2 ? img1 : img2;
  }

  /** Зажатие значения в диапазон [0, 255] */
  private static int clamp(int value) {
    return Math.max(0, Math.min(CLAMP_MAX, value));
  }
}
