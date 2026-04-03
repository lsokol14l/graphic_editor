package by.michael.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/histogram")
public class HistogramController {

  @PostMapping("/calculate")
  public ResponseEntity<HistogramData> calculateHistogram(
      @RequestParam("image") MultipartFile imageFile) {
    try {
      BufferedImage image = ImageIO.read(imageFile.getInputStream());
      if (image == null) {
        return ResponseEntity.badRequest().build();
      }

      HistogramData data = calculateHistogramFromImage(image);
      return ResponseEntity.ok(data);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/transform")
  public ResponseEntity<byte[]> applyGradationalTransformation(
      @RequestParam("image") MultipartFile imageFile,
      @RequestParam(value = "transformation", required = false) String transformationJson) {
    try {
      BufferedImage image = ImageIO.read(imageFile.getInputStream());
      if (image == null) {
        return ResponseEntity.badRequest().build();
      }

      BufferedImage result = applyTransformation(image);
      return ResponseEntity.ok(bufferedImageToBytes(result));
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  private BufferedImage applyTransformation(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = image.getRGB(x, y);
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        int gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
        result.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
      }
    }

    return result;
  }

  private byte[] bufferedImageToBytes(BufferedImage image) throws IOException {
    java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private HistogramData calculateHistogramFromImage(BufferedImage image) {
    int[] histogram = new int[256];
    int width = image.getWidth();
    int height = image.getHeight();
    long sum = 0;
    long sumSquares = 0;
    int min = 255;
    int max = 0;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = image.getRGB(x, y);
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        // RGB to grayscale: Y = 0.299*R + 0.587*G + 0.114*B
        int gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);

        histogram[gray]++;
        sum += gray;
        sumSquares += gray * gray;

        if (gray < min)
          min = gray;
        if (gray > max)
          max = gray;
      }
    }

    int pixelCount = width * height;
    int mean = (int) (sum / pixelCount);
    int variance = (int) (sumSquares / pixelCount - (long) mean * mean);

    return new HistogramData(histogram, min, max, mean, variance);
  }

  public static class HistogramData {
    public int[] histogram;
    public int min;
    public int max;
    public int mean;
    public int variance;

    public HistogramData(int[] histogram, int min, int max, int mean, int variance) {
      this.histogram = histogram;
      this.min = min;
      this.max = max;
      this.mean = mean;
      this.variance = variance;
    }
  }
}
