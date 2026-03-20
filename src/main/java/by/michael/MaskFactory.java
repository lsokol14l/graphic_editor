package by.michael;


public class MaskFactory {

  /** Создает маску в виде круга */
  public static boolean[][] createCircleMask(int width, int height) {
    boolean[][] mask = new boolean[height][width];
    int centerX = width / 2;
    int centerY = height / 2;
    int radius = Math.min(width, height) / 2;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int dx = x - centerX;
        int dy = y - centerY;
        mask[y][x] = (dx * dx + dy * dy) <= (radius * radius);
      }
    }
    return mask;
  }

  /** Создает маску в виде квадрата */
  public static boolean[][] createSquareMask(int width, int height) {
    boolean[][] mask = new boolean[height][width];
    int size = Math.min(width, height) / 2;
    int centerX = width / 2;
    int centerY = height / 2;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        mask[y][x] = Math.abs(x - centerX) <= size && Math.abs(y - centerY) <= size;
      }
    }
    return mask;
  }

  /** Создает маску в виде прямоугольника */
  public static boolean[][] createRectangleMask(int width, int height) {
    boolean[][] mask = new boolean[height][width];
    int rectWidth = (int) (width * 0.7);
    int rectHeight = (int) (height * 0.5);
    int startX = (width - rectWidth) / 2;
    int startY = (height - rectHeight) / 2;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        mask[y][x] =
            x >= startX && x < startX + rectWidth && y >= startY && y < startY + rectHeight;
      }
    }
    return mask;
  }
}
