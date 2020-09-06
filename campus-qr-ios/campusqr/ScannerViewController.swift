import Foundation
import UIKit
import AVFoundation

/// This view controller shows a camera preview, and processes the video frames, looking for a QR code.
/// After the QR code has been found, it checks it's contents, validates that it's a Campus QR code, and sends a location visit post request.
class ScannerViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {
  
  private var captureSession: AVCaptureSession!
  private var previewLayer: AVCaptureVideoPreviewLayer!
  
  // MARK: Lifecycle
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    startDetection()
  }
  
  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    stopDetection()
  }
  
  // MARK: Start / Stop detection
  
  private func startDetection() {
    captureSession = AVCaptureSession()
    
    guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else { return }
    let videoInput: AVCaptureDeviceInput
    
    do {
      videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
    } catch {
      return
    }
    
    if (captureSession.canAddInput(videoInput)) {
      captureSession.addInput(videoInput)
    } else {
      showToast(message: "This device doesn't support use of the camera.")
      return
    }
    
    let metadataOutput = AVCaptureMetadataOutput()
    
    if (captureSession.canAddOutput(metadataOutput)) {
      captureSession.addOutput(metadataOutput)
      
      metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
      metadataOutput.metadataObjectTypes = [.qr]
    } else {
      showToast(message: "Something went wrong.")
      return
    }
    
    previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
    previewLayer.frame = view.layer.bounds
    previewLayer.videoGravity = .resizeAspectFill
    view.layer.addSublayer(previewLayer)
    
    captureSession.startRunning()
  }
  
  private func stopDetection() {
    if (captureSession?.isRunning == true) {
      captureSession.stopRunning()
      captureSession = nil
    }
  }
  
  /// Analyze the given qr code and trigger a check-in if it's a valid Campus QR code.
  private func analyzeQrCode(qrCodeValue: String) {
    // Qr code contains a check-in url. We have to extract location paramter first.
    guard let urlComponents = URLComponents(string: qrCodeValue) else { return }
    guard let queryItems = urlComponents.queryItems else { return }
    if !urlComponents.path.contains("/campus-qr") {
      // Not a valid url
      return
    }
    // Extract location id query parameter
    guard let locationId = queryItems.filter({ $0.name == "l" }).first?.value else { return }
    
    let port = urlComponents.port != nil ? ":\(urlComponents.port!)" : ""
    let baseApiUrl = "\(urlComponents.scheme!)://\(urlComponents.host!)\(port)"
    
    if !locationId.isEmpty {
      AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
      stopDetection()
      checkIn(baseApiUrl: baseApiUrl, locationId: locationId, email: "name.lastname@uni.at")
    }
  }
  
  /// Sends a post request to the backend, linking provided location with the provided email address.
  private func checkIn(baseApiUrl: String, locationId: String, email: String) {
    postRequest(
      url: "\(baseApiUrl)/location/\(locationId)/visit",
      json: [
        "email": email,
        // Sending date is useful for offline dispatching, as we want to save the date of the visit and not when the request arrives on the server.
        "date": String(Int64(Date().timeIntervalSince1970 * 1000))
      ],
      onResponse: { response in
        runOnUiThread {
          if response == "ok" {
            self.showToast(message: "Checking in successful! :)")
          } else {
            self.showToast(message: "Checking in failed.")
          }
        }
    })
  }
  
  // MARK: AVCaptureMetadataOutputObjectsDelegate delegate
  
  func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
    if let metadataObject = metadataObjects.first {
      guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
      guard let stringValue = readableObject.stringValue else { return }

      analyzeQrCode(qrCodeValue: stringValue)
    }
  }
}
