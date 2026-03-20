package by.michael.application;

import by.michael.domain.image.BlendMode;
import by.michael.domain.image.ImageProcessor;
import by.michael.domain.image.LayerComposer;
import by.michael.domain.image.MaskFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageProcessingService {

  public byte[] process(
      MultipartFile image1,
      MultipartFile image2,
      String operation,
      String channels,
      String maskShape)
      throws IOException {

    BufferedImage first = readImage(image1);
    BufferedImage second = readImage(image2);

    if (first == null) {
      throw new IllegalArgumentException("Не удалось прочитать первое изображение");
    }

    ImageProcessor.Operation op = parseOperation(operation);
    ImageProcessor.ColorChannels selectedChannels = parseChannels(channels);
    MaskFactory.MaskShape selectedMask = parseMask(maskShape);

    if (op != ImageProcessor.Operation.MASK && second == null) {
      throw new IllegalArgumentException("Для выбранной операции нужно второе изображение");
    }

    BufferedImage result = ImageProcessor.process(first, second, op, selectedChannels, selectedMask);
    return toPng(result);
  }

  public byte[] composeLayers(
      List<MultipartFile> images,
      List<String> modes,
      List<Double> opacities)
      throws IOException {

    if (images == null || images.isEmpty()) {
      throw new IllegalArgumentException("Добавьте хотя бы одно изображение");
    }

    if (modes == null || opacities == null || modes.size() != images.size() || opacities.size() != images.size()) {
      throw new IllegalArgumentException("Параметры modes/opacities должны соответствовать количеству изображений");
    }

    List<BufferedImage> readImages = new ArrayList<>(images.size());
    List<BlendMode> parsedModes = new ArrayList<>(images.size());

    for (int i = 0; i < images.size(); i++) {
      BufferedImage image = readImage(images.get(i));
      if (image == null) {
        throw new IllegalArgumentException("Не удалось прочитать изображение слоя #" + (i + 1));
      }
      readImages.add(image);
      parsedModes.add(parseBlendMode(modes.get(i)));
    }

    BufferedImage result = LayerComposer.compose(readImages, parsedModes, opacities);
    return toPng(result);
  }

  private ImageProcessor.Operation parseOperation(String value) {
    try {
      return ImageProcessor.Operation.valueOf(normalize(value));
    } catch (Exception ex) {
      throw new IllegalArgumentException("Неверный параметр operation");
    }
  }

  private ImageProcessor.ColorChannels parseChannels(String value) {
    try {
      return ImageProcessor.ColorChannels.valueOf(normalize(value));
    } catch (Exception ex) {
      throw new IllegalArgumentException("Неверный параметр channels");
    }
  }

  private MaskFactory.MaskShape parseMask(String value) {
    if (value == null || value.isBlank()) {
      return MaskFactory.MaskShape.CIRCLE;
    }
    try {
      return MaskFactory.MaskShape.valueOf(normalize(value));
    } catch (Exception ex) {
      throw new IllegalArgumentException("Неверный параметр maskShape");
    }
  }

  private BlendMode parseBlendMode(String value) {
    if (value == null || value.isBlank()) {
      return BlendMode.NONE;
    }
    try {
      return BlendMode.valueOf(normalize(value));
    } catch (Exception ex) {
      throw new IllegalArgumentException("Неверный параметр blend mode");
    }
  }

  private String normalize(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Пустой параметр запроса");
    }
    return value.trim().toUpperCase(Locale.ROOT);
  }

  private BufferedImage readImage(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      return null;
    }
    return ImageIO.read(new ByteArrayInputStream(file.getBytes()));
  }

  private byte[] toPng(BufferedImage image) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", baos);
      return baos.toByteArray();
    }
  }
}
