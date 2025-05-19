package com.visioncameracodescanner;

import androidx.annotation.NonNull;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.mrousavy.camera.frameprocessors.FrameProcessorPluginRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisionCameraCodeScannerPackage implements ReactPackage {

  static {
    FrameProcessorPluginRegistry.addFrameProcessorPlugin(
        VisionCameraCodeScannerModule.NAME, CodeScannerProcessorPlugin::new);
  }

  @NonNull
  @Override
  public List<NativeModule>
  createNativeModules(@NonNull ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();
    modules.add(new VisionCameraCodeScannerModule(reactContext));
    return modules;
  }

  @NonNull
  @Override
  public List<ViewManager>
  createViewManagers(@NonNull ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}
