package by.michael;

import java.awt.image.BufferedImage;

public class ImageProcessor {

  private static final int CLAMP_MAX = 255;

  /** Попиксельная сумма двух изображений */
  public static BufferedImage sum(BufferedImage img1, BufferedImage img2) {
    return processPixels(
        img1,
        img2,
        (r1, g1, b1, r2, g2, b2) -> {
          int r = clamp(r1 + r2);
          int g = clamp(g1 + g2);
          int b = clamp(b1 + b2);
          return (r << 16) | (g << 8) | b;
        });
  }

  /**
   * Попиксельное произведение (умножение) двух изображений Результат = (пиксель1 * пиксель2) / 255
   */
  public static BufferedImage multiply(BufferedImage img1, BufferedImage img2) {
    return processPixels(
        img1,
        img2,
        (r1, g1, b1, r2, g2, b2) -> {
          int r = (r1 * r2) / 255;
          int g = (g1 * g2) / 255;
          int b = (b1 * b2) / 255;
          return (r << 16) | (g << 8) | b;
        });
  }

  /** Попиксельное среднее арифметическое */
  public static BufferedImage average(BufferedImage img1, BufferedImage img2) {
    return processPixels(
        img1,
        img2,
        (r1, g1, b1, r2, g2, b2) -> {
          int r = (r1 + r2) / 2;
          int g = (g1 + g2) / 2;
          int b = (b1 + b2) / 2;
          return (r << 16) | (g << 8) | b;
        });
  }

  /** Попиксельный максимум */
  public static BufferedImage maximum(BufferedImage img1, BufferedImage img2) {
    return processPixels(
        img1,
        img2,
        (r1, g1, b1, r2, g2, b2) -> {
          int r = Math.max(r1, r2);
          int g = Math.max(g1, g2);
          int b = Math.max(b1, b2);
          return (r << 16) | (g << 8) | b;
        });
  }

  /** Попиксельный минимум */
  public static BufferedImage minimum(BufferedImage img1, BufferedImage img2) {
    return processPixels(
        img1,
        img2,
        (r1, g1, b1, r2, g2, b2) -> {
          int r = Math.min(r1, r2);
          int g = Math.min(g1, g2);
          int b = Math.min(b1, b2);
          return (r << 16) | (g << 8) | b;
        });
  }

  /** Наложение маски на изображение (умножение) */
  public static BufferedImage applyMask(BufferedImage img, boolean[][] mask) {
    int width = img.getWidth();
    int height = img.getHeight();
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (mask[y][x]) {
          result.setRGB(x, y, img.getRGB(x, y));
        } else {
          result.setRGB(x, y, 0); // Черный цвет для областей вне маски
        }
      }
    }
    return result;
  }

  /** Вспомогательный метод для обработки пиксельных операций */
  private static BufferedImage processPixels(
      BufferedImage img1, BufferedImage img2, PixelOperator operator) {
    int width = img1.getWidth();
    int height = img1.getHeight();
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int rgb1 = img1.getRGB(x, y);
        int rgb2 = img2.getRGB(x, y);

        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        int resultRgb = operator.operate(r1, g1, b1, r2, g2, b2);
        result.setRGB(x, y, resultRgb);
      }
    }
    return result;
  }

  /** Зажатие значения в диапазон [0, 255] */
  private static int clamp(int value) {
    return Math.max(0, Math.min(CLAMP_MAX, value));
  }

  /** Функциональный интерфейс для пиксельных операций */
  @FunctionalInterface
  interface PixelOperator {
    int operate(int r1, int g1, int b1, int r2, int g2, int b2);
  }
}
