package by.michael.api;

import by.michael.application.ImageProcessingService;
import java.io.IOException;
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

  private final ImageProcessingService imageProcessingService;

  public ImageController(ImageProcessingService imageProcessingService) {
    this.imageProcessingService = imageProcessingService;
  }

  @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<byte[]> process(
      @RequestParam("image1") MultipartFile image1,
      @RequestParam(value = "image2", required = false) MultipartFile image2,
      @RequestParam("operation") String operation,
      @RequestParam("channels") String channels,
      @RequestParam(value = "maskShape", required = false) String maskShape)
      throws IOException {

    byte[] bytes = imageProcessingService.process(image1, image2, operation, channels, maskShape);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    headers.setContentLength(bytes.length);
    headers.set("Content-Disposition", "inline; filename=result.png");

    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
  }
}
