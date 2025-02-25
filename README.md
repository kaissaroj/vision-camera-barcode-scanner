# vision-camera-barcode-scanner

A React Native library for barcode scanning using VisionCamera, forked from [mgcrea/vision-camera-barcode-scanner](https://github.com/mgcrea/vision-camera-barcode-scanner) with added support for inverted barcode scanning.

## Features

- Scan barcodes and QR codes using VisionCamera
- Support for various barcode formats
- High performance with worklets
- Region of interest support
- Different scan modes: continuous, manual
- **NEW: With Inverted Scanning Feature - Scan inverted barcodes and QR codes**

## Installation

```bash
npm i @kaizer433/vision-camera-barcode-scanner
# or
yarn add @kaizer433/vision-camera-barcode-scanner
```

Make sure to also install the required peer dependencies:

```bash
npm install react-native-vision-camera react-native-worklets-core
# or
yarn add react-native-vision-camera react-native-worklets-core
```

## Usage

```jsx
import React from "react";
import { View, Text } from "react-native";
import {
  useCameraDevices,
  useCameraFormat,
  Templates,
} from "react-native-vision-camera";
import { BarcodeCamera } from "@kaizer433/vision-camera-barcode-scanner";

export const BarcodeScannerScreen = () => {
  const devices = useCameraDevices();
  const device = devices.find(({ position }) => position === "back");
  const format = useCameraFormat(device, Templates.FrameProcessingBarcodeXGA);

  const handleBarcodeScanned = (barcodes) => {
    console.log("Scanned barcodes:", barcodes);
  };

  if (!device) {
    return <Text>Loading camera...</Text>;
  }

  return (
    <View style={{ flex: 1 }}>
      <BarcodeCamera
        device={device}
        format={format}
        onBarcodeScanned={handleBarcodeScanned}
        style={{ flex: 1 }}
        barcodeTypes={["ean-13", "qr"]}
        checkInverted={true} // Enable scanning of inverted barcodes
      />
    </View>
  );
};
```

## Configuration

The `BarcodeCamera` component accepts the following props:

| Prop                | Type                                                      | Default        | Description                                       |
| ------------------- | --------------------------------------------------------- | -------------- | ------------------------------------------------- |
| `onBarcodeScanned`  | `(barcodes: Barcode[]) => void`                           | Required       | Callback when barcodes are detected               |
| `device`            | `CameraDevice`                                            | Required       | Camera device from VisionCamera                   |
| `format`            | `CameraDeviceFormat`                                      | Required       | Camera format from VisionCamera                   |
| `barcodeTypes`      | `string[]`                                                | `[]`           | Array of barcode types to scan                    |
| `fps`               | `number`                                                  | `5`            | Frame processing rate                             |
| `scanMode`          | `'continuous' \| 'manual'`                                | `'continuous'` | Mode of scanning                                  |
| `regionOfInterest`  | `{ x: number, y: number, width: number, height: number }` | `undefined`    | Define a specific region to scan                  |
| `defaultResizeMode` | `'contain' \| 'cover'`                                    | `undefined`    | Resize mode for the camera view                   |
| `checkInverted`     | `boolean`                                                 | `false`        | Enable scanning of inverted barcodes and QR codes |

## Custom Hooks

The library provides the `useBarcodeScanner` hook that can be used for more advanced configurations:

```jsx
import { useBarcodeScanner } from "@kaizer433/vision-camera-barcode-scanner";

// In your component:
const { props, highlights } = useBarcodeScanner({
  fps: 10,
  barcodeTypes: ["ean-13", "qr"],
  scanMode: "continuous",
  checkInverted: true,
  onBarcodeScanned: (barcodes) => {
    console.log("Scanned barcodes:", barcodes);
  },
});
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the original repository for details.

## Acknowledgments

- This project is a fork of [mgcrea/vision-camera-barcode-scanner](https://github.com/mgcrea/vision-camera-barcode-scanner)
- Thanks to the original author for the excellent library
