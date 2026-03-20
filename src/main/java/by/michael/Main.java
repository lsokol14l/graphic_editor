package by.michael;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class Main extends Application {

  private final TextField firstImageNameField = new TextField();
  private final TextField secondImageNameField = new TextField();
  private final ComboBox<ImageProcessor.Operation> operationBox = new ComboBox<>();
  private final ComboBox<ImageProcessor.ColorChannels> channelsBox = new ComboBox<>();
  private final ComboBox<MaskFactory.MaskShape> maskShapeBox = new ComboBox<>();
  private final ImageView resultView = new ImageView();

  private BufferedImage resultImage;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    stage.setTitle("Image Processor");

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.setPadding(new Insets(10));

    firstImageNameField.setPromptText("Например: in.jpg");
    secondImageNameField.setPromptText("Например: sae.jpg");

    operationBox.getItems().addAll(ImageProcessor.Operation.values());
    operationBox.setValue(ImageProcessor.Operation.SUM);

    channelsBox.getItems().addAll(ImageProcessor.ColorChannels.values());
    channelsBox.setValue(ImageProcessor.ColorChannels.RGB);

    maskShapeBox.getItems().addAll(MaskFactory.MaskShape.values());
    maskShapeBox.setValue(MaskFactory.MaskShape.CIRCLE);

    operationBox
        .valueProperty()
        .addListener((obs, oldVal, newVal) -> updateControlsByOperation(newVal));
    updateControlsByOperation(operationBox.getValue());

    form.add(new Label("Имя 1-го файла (resources):"), 0, 0);
    form.add(firstImageNameField, 1, 0);

    form.add(new Label("Имя 2-го файла (resources):"), 0, 1);
    form.add(secondImageNameField, 1, 1);

    form.add(new Label("Операция:"), 0, 2);
    form.add(operationBox, 1, 2);

    form.add(new Label("Цветовые каналы:"), 0, 3);
    form.add(channelsBox, 1, 3);

    form.add(new Label("Форма маски:"), 0, 4);
    form.add(maskShapeBox, 1, 4);

    GridPane.setHgrow(firstImageNameField, Priority.ALWAYS);
    GridPane.setHgrow(secondImageNameField, Priority.ALWAYS);

    Button runButton = new Button("Выполнить");
    Button saveButton = new Button("Сохранить результат");

    runButton.setOnAction(event -> processImages());
    saveButton.setOnAction(event -> saveResult(stage));

    HBox actions = new HBox(10, runButton, saveButton);
    actions.setAlignment(Pos.CENTER_LEFT);
    actions.setPadding(new Insets(0, 10, 10, 10));

    resultView.setPreserveRatio(true);
    resultView.setFitWidth(950);
    resultView.setFitHeight(550);

    VBox content = new VBox(8, form, actions, resultView);
    content.setPadding(new Insets(10));

    BorderPane root = new BorderPane(content);
    Scene scene = new Scene(root, 1040, 760);

    stage.setScene(scene);
    stage.show();
  }

  private void updateControlsByOperation(ImageProcessor.Operation operation) {
    boolean isMask = operation == ImageProcessor.Operation.MASK;
    secondImageNameField.setDisable(isMask);
    maskShapeBox.setDisable(!isMask);
  }

  private void processImages() {
    try {
      String firstName = firstImageNameField.getText().trim();
      String secondName = secondImageNameField.getText().trim();

      if (firstName.isEmpty()) {
        showError("Введите имя первого файла.");
        return;
      }

      ImageProcessor.Operation operation = operationBox.getValue();
      ImageProcessor.ColorChannels channels = channelsBox.getValue();
      MaskFactory.MaskShape maskShape = maskShapeBox.getValue();

      File firstFile = findFileInResources(firstName);
      if (firstFile == null) {
        showError("Файл не найден в resources: " + firstName);
        return;
      }

      BufferedImage firstImage = ImageIO.read(firstFile);
      if (firstImage == null) {
        showError("Не удалось прочитать изображение: " + firstName);
        return;
      }

      BufferedImage secondImage = null;
      if (operation != ImageProcessor.Operation.MASK) {
        if (secondName.isEmpty()) {
          showError("Для выбранной операции нужно второе изображение.");
          return;
        }

        File secondFile = findFileInResources(secondName);
        if (secondFile == null) {
          showError("Файл не найден в resources: " + secondName);
          return;
        }

        secondImage = ImageIO.read(secondFile);
        if (secondImage == null) {
          showError("Не удалось прочитать изображение: " + secondName);
          return;
        }
      }

      resultImage = ImageProcessor.process(firstImage, secondImage, operation, channels, maskShape);
      resultView.setImage(SwingFXUtils.toFXImage(resultImage, null));

    } catch (IOException e) {
      showError("Ошибка при чтении файлов: " + e.getMessage());
    } catch (Exception e) {
      showError("Ошибка обработки: " + e.getMessage());
    }
  }

  private void saveResult(Stage stage) {
    if (resultImage == null) {
      showError("Сначала выполните обработку изображения.");
      return;
    }

    FileChooser chooser = new FileChooser();
    chooser.setTitle("Сохранить результат");
    chooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("PNG Image", "*.png"),
            new FileChooser.ExtensionFilter("JPEG Image", "*.jpg", "*.jpeg"));

    File target = chooser.showSaveDialog(stage);
    if (target == null) {
      return;
    }

    String format = getFormatByName(target.getName());
    try {
      ImageIO.write(resultImage, format, target);
    } catch (IOException e) {
      showError("Не удалось сохранить файл: " + e.getMessage());
    }
  }

  private String getFormatByName(String fileName) {
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".png")) {
      return "png";
    }
    return "jpg";
  }

  private File findFileInResources(String fileName) throws IOException {
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

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Ошибка");
    alert.setHeaderText("Операция не выполнена");
    alert.setContentText(message);
    alert.showAndWait();
  }
}
