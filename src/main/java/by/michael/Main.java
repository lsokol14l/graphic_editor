package by.michael;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class Main {

  // resources/sae.jpg
  // BlueLockPuzzle.png

  public static void main(String[] args) {
    // Установка кодировки UTF-8 для консоли
    System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

    ConsoleMenu menu = new ConsoleMenu();

    try {
      System.out.println("=== Приложение для обработки изображений ===\n");

      // Получаем имена исходных файлов и ищем их в resources
      String img1Name = menu.getImageName("Введите имя первого изображения (например, in.jpg): ");
      String img2Name = menu.getImageName("Введите имя второго изображения (например, sae.jpg): ");

      File img1File = findFileInResources(img1Name);
      File img2File = findFileInResources(img2Name);

      if (img1File == null || img2File == null) {
        if (img1File == null) {
          System.err.println("Ошибка: файл не найден в папке resources: " + img1Name);
        }
        if (img2File == null) {
          System.err.println("Ошибка: файл не найден в папке resources: " + img2Name);
        }
        menu.close();
        return;
      }

      // Загружаем изображения
      BufferedImage img1 = ImageIO.read(img1File);
      BufferedImage img2 = ImageIO.read(img2File);

      // Проверяем, что изображения загружены
      if (img1 == null || img2 == null) {
        System.err.println("Ошибка: не удалось прочитать одно из изображений.");
        menu.close();
        return;
      }

      // Проверяем размеры изображений
      if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
        System.err.println("Ошибка: изображения должны быть одинакового размера.");
        System.err.println("Размер первого: " + img1.getWidth() + "x" + img1.getHeight());
        System.err.println("Размер второго: " + img2.getWidth() + "x" + img2.getHeight());
        menu.close();
        return;
      }

      // Показываем меню операций
      int operation = menu.showOperationMenu();
      BufferedImage result = null;

      // Выполняем выбранную операцию
      switch (operation) {
        case 1:
          System.out.println("\nВыполняю: Попиксельная сумма...");
          result = ImageProcessor.sum(img1, img2);
          break;
        case 2:
          System.out.println("\nВыполняю: Попиксельное произведение...");
          result = ImageProcessor.multiply(img1, img2);
          break;
        case 3:
          System.out.println("\nВыполняю: Попиксельное среднее...");
          result = ImageProcessor.average(img1, img2);
          break;
        case 4:
          System.out.println("\nВыполняю: Попиксельный минимум...");
          result = ImageProcessor.minimum(img1, img2);
          break;
        case 5:
          System.out.println("\nВыполняю: Попиксельный максимум...");
          result = ImageProcessor.maximum(img1, img2);
          break;
        case 6:
          System.out.println("\nПрименяю маску (круг) к первому изображению...");
          boolean[][] circleMask = MaskFactory.createCircleMask(img1.getWidth(), img1.getHeight());
          result = ImageProcessor.applyMask(img1, circleMask);
          break;
        case 7:
          System.out.println("\nПрименяю маску (квадрат) к первому изображению...");
          boolean[][] squareMask = MaskFactory.createSquareMask(img1.getWidth(), img1.getHeight());
          result = ImageProcessor.applyMask(img1, squareMask);
          break;
        case 8:
          System.out.println("\nПрименяю маску (прямоугольник) к первому изображению...");
          boolean[][] rectMask = MaskFactory.createRectangleMask(img1.getWidth(), img1.getHeight());
          result = ImageProcessor.applyMask(img1, rectMask);
          break;
      }

      // Получаем имя файла для сохранения
      String outputPath = menu.getOutputPath();

      // Сохраняем результат
      if (result != null) {
        String format = outputPath.contains(".")
            ? outputPath.substring(outputPath.lastIndexOf(".") + 1)
            : "jpg";
        ImageIO.write(result, format, new File(outputPath));
        System.out.println("✓ Успешно! Результат сохранен в файл: " + outputPath);
      }

      menu.close();

    } catch (IOException e) {
      System.err.println("Ошибка при работе с файлом: " + e.getMessage());
      e.printStackTrace();
      menu.close();
    } catch (Exception e) {
      System.err.println("Непредвиденная ошибка: " + e.getMessage());
      e.printStackTrace();
      menu.close();
    }
  }

  private static File findFileInResources(String fileName) throws IOException {
    Path resourcesDir = Paths.get("src", "main", "resources");

    if (!Files.exists(resourcesDir) || !Files.isDirectory(resourcesDir)) {
      throw new IOException("Папка resources не найдена: " + resourcesDir.toAbsolutePath());
    }

    try (Stream<Path> stream = Files.walk(resourcesDir)) {
      Path found = stream
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().equalsIgnoreCase(fileName))
          .findFirst()
          .orElse(null);
      return found == null ? null : found.toFile();
    }
  }
}
