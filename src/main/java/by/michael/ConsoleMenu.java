package by.michael;

import java.util.Scanner;

public class ConsoleMenu {
  private final Scanner scanner;

  public ConsoleMenu() {
    this.scanner = new Scanner(System.in);
  }

  /** Запрашивает у пользователя имя файла изображения */
  public String getImageName(String prompt) {
    System.out.print(prompt);
    return scanner.nextLine().trim();
  }

  /** Показывает меню выбора операции */
  public int showOperationMenu() {
    System.out.println("\n=== Выберите операцию ===");
    System.out.println("1. Попиксельная сумма");
    System.out.println("2. Попиксельное произведение (умножение)");
    System.out.println("3. Попиксельное среднее арифметическое");
    System.out.println("4. Попиксельный минимум");
    System.out.println("5. Попиксельный максимум");
    System.out.println("6. Наложить маску (круг)");
    System.out.println("7. Наложить маску (квадрат)");
    System.out.println("8. Наложить маску (прямоугольник)");
    System.out.print("Введите номер операции (1-8): ");

    try {
      int choice = Integer.parseInt(scanner.nextLine().trim());
      if (choice >= 1 && choice <= 8) {
        return choice;
      } else {
        System.out.println("Ошибка: выберите число от 1 до 8!");
        return showOperationMenu();
      }
    } catch (NumberFormatException e) {
      System.out.println("Ошибка: введите корректное число!");
      return showOperationMenu();
    }
  }

  /** Показывает меню выбора маски для операций с одним изображением */
  public int showMaskMenu() {
    System.out.println("\n=== Выберите форму маски ===");
    System.out.println("1. Круг");
    System.out.println("2. Квадрат");
    System.out.println("3. Прямоугольник");
    System.out.print("Введите номер маски (1-3): ");

    try {
      int choice = Integer.parseInt(scanner.nextLine().trim());
      if (choice >= 1 && choice <= 3) {
        return choice;
      } else {
        System.out.println("Ошибка: выберите число от 1 до 3!");
        return showMaskMenu();
      }
    } catch (NumberFormatException e) {
      System.out.println("Ошибка: введите корректное число!");
      return showMaskMenu();
    }
  }

  /** Запрашивает имя файла для сохранения результата */
  public String getOutputPath() {
    System.out.print("\nВведите имя файла для сохранения результата (например: out.jpg): ");
    return scanner.nextLine().trim();
  }

  /** Закрывает сканер */
  public void close() {
    scanner.close();
  }
}
