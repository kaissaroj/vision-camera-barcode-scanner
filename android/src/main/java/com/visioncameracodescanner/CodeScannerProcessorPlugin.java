package com.visioncameracodescanner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.internal.ImageConvertUtils;
import com.mrousavy.camera.core.FrameInvalidError;
import com.mrousavy.camera.frameprocessors.Frame;
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin;
import com.mrousavy.camera.frameprocessors.VisionCameraProxy;
import com.mrousavy.camera.core.types.Orientation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CodeScannerProcessorPlugin extends FrameProcessorPlugin {

  private static final String TAG = VisionCameraCodeScannerModule.NAME;
  private static BarcodeScanner barcodeScanner = null;
  private static int previousBarcodeScannerFormatsBitmap = -1;

  CodeScannerProcessorPlugin(@NotNull VisionCameraProxy proxy,
                             @Nullable Map<String, Object> options) {
    Log.d(TAG,
          "CodeScannerProcessorPlugin initialized with options: " + options);
  }

  @Nullable
  @Override
  public Object callback(@NotNull Frame frame,
                         @Nullable Map<String, Object> params) {

    try {
      Image mediaImage = frame.getImage();

      if (mediaImage.getFormat() != ImageFormat.YUV_420_888) {
        Log.e(TAG, "Unsupported image format: " + mediaImage.getFormat() +
                ". Only YUV_420_888 is supported for now.");
        return null;
      }
    } catch (FrameInvalidError e) {
      Log.e(TAG, "Received an invalid frame.");
      return null;
    }

    InputImage inputImage = getInputImageFromFrame(frame);
    if (inputImage == null) {
      return null;
    }

    BarcodeScanner scanner = getBarcodeScannerClient(params);

    // Getting checkInverted parameter from options
    boolean checkInverted = false;
    if (params != null && params.containsKey("checkInverted")) {
      Object checkInvertedObj = params.get("checkInverted");
      if (checkInvertedObj instanceof Boolean) {
        checkInverted = (Boolean) checkInvertedObj;
      }
    }

    List<Object> barcodes = new ArrayList<>();
    try {
      List<Barcode> barcodeList = new ArrayList<>();
      
      // Process regular image
      barcodeList.addAll(Tasks.await(scanner.process(inputImage)));
      
      // Process inverted image if requested
      if (checkInverted) {
        try {
          Bitmap bitmap = ImageConvertUtils.getInstance().getUpRightBitmap(inputImage);
          Bitmap invertedBitmap = invert(bitmap);
          InputImage invertedImage = InputImage.fromBitmap(invertedBitmap, 0);
          barcodeList.addAll(Tasks.await(scanner.process(invertedImage)));
        } catch (Exception e) {
          Log.e(TAG, "Error processing inverted image: " + e.getMessage());
        }
      }
      
      // Convert all found barcodes
      barcodeList.forEach(
          barcode -> barcodes.add(BarcodeConverter.convertBarcode(barcode)));
    } catch (ExecutionException | InterruptedException e) {
      Log.e(TAG, "Error processing image for barcodes: " + e.getMessage());
      return null;
    }

    return barcodes;
  }

  // Bitmap Inversion method from old code
  private Bitmap invert(Bitmap src) { 
    int height = src.getHeight();
    int width = src.getWidth();    

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    
    ColorMatrix matrixGrayscale = new ColorMatrix();
    matrixGrayscale.setSaturation(0);
    
    ColorMatrix matrixInvert = new ColorMatrix();
    matrixInvert.set(new float[] {
      -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
      0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
      0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
      0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    });
    matrixInvert.preConcat(matrixGrayscale);
    
    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixInvert);
    paint.setColorFilter(filter);
    
    canvas.drawBitmap(src, 0, 0, paint);
    return bitmap;
  }

  @Nullable
  private InputImage getInputImageFromFrame(@NotNull Frame frame) {
    try {
      Image mediaImage = frame.getImage();
      String orientation = frame.getOrientation().getUnionValue();

      int degrees = toDegrees(orientation);

      return InputImage.fromMediaImage(mediaImage, degrees);
    } catch (FrameInvalidError e) {
      Log.e(TAG, "Received an invalid frame.");
      return null;
    }
  }

  private int toDegrees(String orientation) {
    switch (orientation) {
      case "portrait":
        return 0;
      case "landscape-left":
        return 90;
      case "portrait-upside-down":
        return 180;
      case "landscape-right":
        return 270;
      default:
        return 0;
    }
  };

  private synchronized BarcodeScanner
  getBarcodeScannerClient(@Nullable Map<String, Object> params) {
    int formatsBitmap = getFormatsBitmapFromParams(params);

    if (barcodeScanner == null ||
        previousBarcodeScannerFormatsBitmap != formatsBitmap) {
      previousBarcodeScannerFormatsBitmap = formatsBitmap;
      BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                                          .setBarcodeFormats(formatsBitmap)
                                          .build();
      barcodeScanner = BarcodeScanning.getClient(options);
    }

    return barcodeScanner;
  }

  private int getFormatsBitmapFromParams(@Nullable Map<String, Object> params) {
    if (params == null)
      return Barcode.FORMAT_ALL_FORMATS;
    Set<Integer> barcodeFormats = new HashSet<>();
    Object barcodeTypesObj = params.get("barcodeTypes");
    if (barcodeTypesObj instanceof Map) {
      Map<String, String> barcodeTypes = (Map<String, String>)barcodeTypesObj;
      barcodeTypes.values().forEach(type -> {
        Integer format = BarcodeFormatMapper.getFormat(type);
        if (format != null)
          barcodeFormats.add(format);
        else
          Log.e(TAG, "Unsupported barcode type: " + type);
      });
    }
    return barcodeFormats.isEmpty() ? Barcode.FORMAT_ALL_FORMATS
                                    : barcodeFormatsToBitmap(barcodeFormats);
  }

  private int barcodeFormatsToBitmap(Set<Integer> barcodeFormats) {
    return barcodeFormats.stream().reduce(0, (a, b) -> a | b);
  }
}