package by.michael.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
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

  private final ObjectMapper objectMapper;

  public HistogramController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostMapping("/calculate")
  public ResponseEntity<HistogramData> calculateHistogram(
      @RequestParam("image") MultipartFile imageFile) {
    try {
      BufferedImage image = ImageIO.read(imageFile.getInputStream());
      if (image == null) {
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity.ok(calculateHistogramFromImage(image));
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/transform")
  public ResponseEntity<TransformationResponse> applyGradationalTransformation(
      @RequestParam("image") MultipartFile imageFile,
      @RequestParam("points") String pointsJson,
      @RequestParam(value = "mode", defaultValue = "linear") String mode) {
    try {
      BufferedImage image = ImageIO.read(imageFile.getInputStream());
      if (image == null) {
        return ResponseEntity.badRequest().build();
      }

      List<TransformationPoint> points = parsePoints(pointsJson);
      BufferedImage result = applyTransformation(image, points, mode);
      HistogramData histogram = calculateHistogramFromImage(result);

      return ResponseEntity.ok(
          new TransformationResponse(bufferedImageToDataUrl(result), histogram));
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  private BufferedImage applyTransformation(
      BufferedImage image, List<TransformationPoint> points, String mode) {
    int width = image.getWidth();
    int height = image.getHeight();
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    List<TransformationPoint> normalizedPoints = normalizePoints(points);
    boolean useCubic = "cubic".equalsIgnoreCase(mode) && normalizedPoints.size() >= 3;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = image.getRGB(x, y);
        int alpha = (argb >> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        int transformedRed = transformValue(red, normalizedPoints, useCubic);
        int transformedGreen = transformValue(green, normalizedPoints, useCubic);
        int transformedBlue = transformValue(blue, normalizedPoints, useCubic);

        int transformedArgb = (alpha << 24) | (transformedRed << 16) | (transformedGreen << 8) | transformedBlue;
        result.setRGB(x, y, transformedArgb);
      }
    }

    return result;
  }

  private String bufferedImageToDataUrl(BufferedImage image) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return "data:image/png;base64,"
        + Base64.getEncoder().encodeToString(outputStream.toByteArray());
  }

  private List<TransformationPoint> parsePoints(String pointsJson) throws IOException {
    TransformationPoint[] parsed = objectMapper.readValue(pointsJson, new TypeReference<TransformationPoint[]>() {
    });
    List<TransformationPoint> points = new ArrayList<>();
    for (TransformationPoint point : parsed) {
      points.add(new TransformationPoint(clamp(point.x(), 0, 255), clamp(point.y(), 0, 255)));
    }
    return points;
  }

  private List<TransformationPoint> normalizePoints(List<TransformationPoint> inputPoints) {
    List<TransformationPoint> points = new ArrayList<>(inputPoints);
    points.sort(Comparator.comparingInt(TransformationPoint::x));

    List<TransformationPoint> uniquePoints = new ArrayList<>();
    for (TransformationPoint point : points) {
      if (!uniquePoints.isEmpty() && uniquePoints.get(uniquePoints.size() - 1).x() == point.x()) {
        uniquePoints.set(uniquePoints.size() - 1, point);
      } else {
        uniquePoints.add(point);
      }
    }

    if (uniquePoints.isEmpty()) {
      uniquePoints.add(new TransformationPoint(0, 0));
      uniquePoints.add(new TransformationPoint(255, 255));
      return uniquePoints;
    }

    if (uniquePoints.get(0).x() != 0) {
      uniquePoints.add(0, new TransformationPoint(0, uniquePoints.get(0).y()));
    }
    if (uniquePoints.get(uniquePoints.size() - 1).x() != 255) {
      uniquePoints.add(
          new TransformationPoint(255, uniquePoints.get(uniquePoints.size() - 1).y()));
    }

    return uniquePoints;
  }

  private int transformValue(int inputValue, List<TransformationPoint> points, boolean useCubic) {
    if (points.isEmpty()) {
      return inputValue;
    }

    if (useCubic) {
      return cubicInterpolate(inputValue, points);
    }

    return linearInterpolate(inputValue, points);
  }

  private int linearInterpolate(int inputValue, List<TransformationPoint> points) {
    TransformationPoint lower = points.get(0);
    TransformationPoint upper = points.get(points.size() - 1);

    for (int i = 0; i < points.size() - 1; i++) {
      if (points.get(i).x() <= inputValue && inputValue <= points.get(i + 1).x()) {
        lower = points.get(i);
        upper = points.get(i + 1);
        break;
      }
    }

    double t = (double) (inputValue - lower.x()) / Math.max(1, upper.x() - lower.x());
    double outputValue = lower.y() + t * (upper.y() - lower.y());
    return clamp((int) Math.round(outputValue), 0, 255);
  }

  private int cubicInterpolate(double inputValue, List<TransformationPoint> points) {
    int n = points.size();

    if (n < 2) {
      throw new IllegalArgumentException("At least 2 points required");
    }

    if (n < 3) {
      return linearInterpolate((int) inputValue, points);
    }

    double[] x = new double[n];
    double[] y = new double[n];

    for (int i = 0; i < n; i++) {
      x[i] = points.get(i).x();
      y[i] = points.get(i).y();
    }

    // Проверка: x должны быть строго возрастающими
    for (int i = 0; i < n - 1; i++) {
      if (x[i + 1] <= x[i]) {
        throw new IllegalArgumentException("x values must be strictly increasing");
      }
    }

    double[] h = new double[n - 1];
    for (int i = 0; i < n - 1; i++) {
      h[i] = x[i + 1] - x[i];
    }

    double[] alpha = new double[n];
    for (int i = 1; i < n - 1; i++) {
      alpha[i] = (3.0 / h[i]) * (y[i + 1] - y[i]) -
          (3.0 / h[i - 1]) * (y[i] - y[i - 1]);
    }

    double[] l = new double[n];
    double[] mu = new double[n];
    double[] z = new double[n];

    l[0] = 1.0;
    mu[0] = 0.0;
    z[0] = 0.0;

    for (int i = 1; i < n - 1; i++) {
      l[i] = 2.0 * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1];

      if (Math.abs(l[i]) < 1e-12) {
        throw new IllegalStateException("Degenerate spline system");
      }

      mu[i] = h[i] / l[i];
      z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
    }

    l[n - 1] = 1.0;
    z[n - 1] = 0.0;

    double[] c = new double[n];
    double[] b = new double[n - 1];
    double[] d = new double[n - 1];

    c[n - 1] = 0.0;

    for (int j = n - 2; j >= 0; j--) {
      c[j] = z[j] - mu[j] * c[j + 1];
      b[j] = (y[j + 1] - y[j]) / h[j] -
          (h[j] * (2.0 * c[j] + c[j + 1])) / 3.0;
      d[j] = (c[j + 1] - c[j]) / (3.0 * h[j]);
    }

    // Обработка выхода за границы (без экстраполяции)
    if (inputValue <= x[0]) {
      return clamp((int) Math.round(y[0]), 0, 255);
    }
    if (inputValue >= x[n - 1]) {
      return clamp((int) Math.round(y[n - 1]), 0, 255);
    }

    // Бинарный поиск интервала
    int left = 0;
    int right = n - 1;

    while (left <= right) {
      int mid = (left + right) / 2;

      if (inputValue < x[mid]) {
        right = mid - 1;
      } else if (inputValue > x[mid + 1]) {
        left = mid + 1;
      } else {
        left = mid;
        break;
      }
    }

    int i = left;

    double dx = inputValue - x[i];

    double result = y[i] +
        b[i] * dx +
        c[i] * dx * dx +
        d[i] * dx * dx * dx;

    return clamp((int) Math.round(result), 0, 255);
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
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        int gray = (int) Math.round(0.299 * red + 0.587 * green + 0.114 * blue);

        histogram[gray]++;
        sum += gray;
        sumSquares += gray * gray;

        if (gray < min) {
          min = gray;
        }
        if (gray > max) {
          max = gray;
        }
      }
    }

    int pixelCount = width * height;
    int mean = (int) (sum / pixelCount);
    int variance = (int) (sumSquares / pixelCount - (long) mean * mean);

    return new HistogramData(histogram, min, max, mean, variance);
  }

  private int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  public record TransformationPoint(int x, int y) {
  }

  public record HistogramData(int[] histogram, int min, int max, int mean, int variance) {
  }

  public record TransformationResponse(String imageDataUrl, HistogramData histogram) {
  }
}