package by.michael;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:5173")
public class ImageController {

  @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> process(
      @RequestParam("image1") MultipartFile image1,
      @RequestParam(value = "image2", required = false) MultipartFile image2,
      @RequestParam("operation") String operation,
      @RequestParam("channels") String channels,
      @RequestParam(value = "maskShape", required = false) String maskShape)
      throws IOException {

    BufferedImage first = readImage(image1);
    BufferedImage second = readImage(image2);

    if (first == null) {
      return ResponseEntity.badRequest().body("Не удалось прочитать первое изображение");
    }

    ImageProcessor.Operation op;
    ImageProcessor.ColorChannels selectedChannels;
    MaskFactory.MaskShape selectedMask = MaskFactory.MaskShape.CIRCLE;

    try {
      op = ImageProcessor.Operation.valueOf(operation.trim().toUpperCase());
      selectedChannels = ImageProcessor.ColorChannels.valueOf(channels.trim().toUpperCase());
      if (maskShape != null && !maskShape.isBlank()) {
        selectedMask = MaskFactory.MaskShape.valueOf(maskShape.trim().toUpperCase());
      }
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body("Неверные параметры operation/channels/maskShape");
    }

    if (op != ImageProcessor.Operation.MASK && second == null) {
      return ResponseEntity.badRequest().body("Для выбранной операции нужно второе изображение");
    }

    BufferedImage result = ImageProcessor.process(first, second, op, selectedChannels, selectedMask);
    byte[] bytes = toPng(result);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    headers.setContentLength(bytes.length);
    headers.set("Content-Disposition", "inline; filename=result.png");

    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
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
