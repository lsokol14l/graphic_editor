package by.michael.domain.image;

import java.awt.image.BufferedImage;
import java.util.List;

public class LayerComposer {

  public static BufferedImage compose(
      List<BufferedImage> images, List<BlendMode> modes, List<Double> opacities) {
    if (images == null || images.isEmpty()) {
      throw new IllegalArgumentException("Список изображений пуст");
    }
    if (modes == null
        || opacities == null
        || modes.size() != images.size()
        || opacities.size() != images.size()) {
      throw new IllegalArgumentException("Размеры images/modes/opacities должны совпадать");
    }

    int width = 1;
    int height = 1;
    for (BufferedImage image : images) {
      if (image == null) {
        throw new IllegalArgumentException("Одно из изображений не удалось прочитать");
      }
      width = Math.max(width, image.getWidth());
      height = Math.max(height, image.getHeight());
    }

    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    // Первый слой кладем как базу с учетом его opacity.
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int firstRgb = getScaledPixel(images.getFirst(), x, y, width, height);
        double opacity = clampOpacity(opacities.getFirst());

        int r = (int) Math.round(((firstRgb >> 16) & 0xFF) * opacity);
        int g = (int) Math.round(((firstRgb >> 8) & 0xFF) * opacity);
        int b = (int) Math.round((firstRgb & 0xFF) * opacity);

        result.setRGB(x, y, (clamp(r) << 16) | (clamp(g) << 8) | clamp(b));
      }
    }

    // Остальные слои накладываем снизу вверх в порядке списка.
    for (int i = 1; i < images.size(); i++) {
      BlendMode mode = modes.get(i) == null ? BlendMode.NONE : modes.get(i);
      double opacity = clampOpacity(opacities.get(i));
      BufferedImage layer = images.get(i);

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int baseRgb = result.getRGB(x, y);
          int layerRgb = getScaledPixel(layer, x, y, width, height);

          int br = (baseRgb >> 16) & 0xFF;
          int bg = (baseRgb >> 8) & 0xFF;
          int bb = baseRgb & 0xFF;

          int lr = (layerRgb >> 16) & 0xFF;
          int lg = (layerRgb >> 8) & 0xFF;
          int lb = layerRgb & 0xFF;

          int mr = applyMode(br, lr, mode);
          int mg = applyMode(bg, lg, mode);
          int mb = applyMode(bb, lb, mode);

          int outR = mix(br, mr, opacity);
          int outG = mix(bg, mg, opacity);
          int outB = mix(bb, mb, opacity);

          result.setRGB(x, y, (outR << 16) | (outG << 8) | outB);
        }
      }
    }

    return result;
  }

  private static int applyMode(int base, int layer, BlendMode mode) {
    return switch (mode) {
      case NONE -> layer;
      case SUM -> clamp(base + layer);
      case DIFFERENCE -> Math.abs(base - layer);
      case MULTIPLY -> (base * layer) / 255;
      case AVERAGE -> (base + layer) / 2;
      case MINIMUM -> Math.min(base, layer);
      case MAXIMUM -> Math.max(base, layer);
    };
  }

  private static int mix(int base, int blended, double opacity) {
    double value = (base * (1.0 - opacity)) + (blended * opacity);
    return clamp((int) Math.round(value));
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

  private static int clamp(int value) {
    return Math.max(0, Math.min(255, value));
  }

  private static double clampOpacity(Double opacity) {
    if (opacity == null) {
      return 1.0;
    }
    return Math.max(0.0, Math.min(1.0, opacity));
  }
}
