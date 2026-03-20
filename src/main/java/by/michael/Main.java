package by.michael;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;

public class Main {
  public static void main(String[] args) {
    String input1Path = "build/resources/main/in.jpg";
    String input2Path = "build/resources/main/in.jpg";
    String out1Path = "out1.jpg"; // попиксельная сумма
    String out2Path = "out2.jpg"; // попиксельное среднее
    String out3Path = "out3.jpg"; // попиксельный максимум
    String out4Path = "out4.jpg"; // попиксельный min

    System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

    try {
      BufferedImage img1 = ImageIO.read(new File(input1Path));
      BufferedImage img2 = ImageIO.read(new File(input2Path));

      if (img1 == null || img2 == null) {
        System.err.println("Ошибка: не удалось прочитать одно из изображений.");
        return;
      }

      int width = img1.getWidth();
      int height = img1.getHeight();

      if (width != img2.getWidth() || height != img2.getHeight()) {
        System.err.println("Ошибка: изображения должны быть одинакового размера.");
        return;
      }

      BufferedImage outSum = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      BufferedImage outAvg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      BufferedImage outMax = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      BufferedImage outMin = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

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

          int rs = clamp(r1 + r2);
          int gs = clamp(g1 + g2);
          int bs = clamp(b1 + b2);
          int sumRgb = (rs << 16) | (gs << 8) | bs;

          int ra = (r1 + r2) / 2;
          int ga = (g1 + g2) / 2;
          int ba = (b1 + b2) / 2;
          int avgRgb = (ra << 16) | (ga << 8) | ba;

          int rm = Math.max(r1, r2);
          int gm = Math.max(g1, g2);
          int bm = Math.max(b1, b2);
          int maxRgb = (rm << 16) | (gm << 8) | bm;

          int rmin = Math.min(r1, r2);
          int gmin = Math.min(g1, g2);
          int bmin = Math.min(b1, b2);
          int minRgb = (rmin << 16) | (gmin << 8) | bmin;

          outSum.setRGB(x, y, sumRgb);
          outAvg.setRGB(x, y, avgRgb);
          outMax.setRGB(x, y, maxRgb);
          outMin.setRGB(x, y, minRgb);
        }
      }

      ImageIO.write(outSum, "jpg", new File(out1Path));
      ImageIO.write(outAvg, "jpg", new File(out2Path));
      ImageIO.write(outMax, "jpg", new File(out3Path));
      ImageIO.write(outMax, "jpg", new File(out4Path));

      System.out.println("Готово.");
      System.out.println("Созданы файлы: " + out1Path + ", " + out2Path + ", " + out4Path);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static int clamp(int value) {
    return Math.min(255, Math.max(0, value));
  }
}
