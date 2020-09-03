package com.studo.campusqr.campusqr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.fragment_scanner.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * This Fragment shows a camera preview, and processes the video frames, and looks for a QR code.
 * After the QR code has been found, it checks it's contents, validates that it's a Campus QR code, and sends a location visit post request.
 */
class ScannerFragment : Fragment(), SurfaceHolder.Callback, Detector.Processor<Barcode> {

  private var detector: BarcodeDetector? = null
  private var cameraSource: CameraSource? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_scanner, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    cameraPreview.holder.addCallback(this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    cameraPreview.holder.removeCallback(this)
    stopDetection()
  }


  // SurfaceHolder callbacks

  override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
  }

  override fun surfaceDestroyed(holder: SurfaceHolder?) {
    stopDetection()
  }

  override fun surfaceCreated(holder: SurfaceHolder?) {
    startDetection()
  }

  private fun startDetection() {
    // Make sure we have camera permission. Request it otherwise and try again.
    if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    } else {
      // Create a QR code detector instance
      detector = BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.QR_CODE).build()
      detector!!.setProcessor(this)

      cameraSource = CameraSource.Builder(context, detector)
        .setRequestedPreviewSize(view!!.height, view!!.width)
        .setRequestedFps(25f)
        .setAutoFocusEnabled(true)
        .build()

      cameraSource!!.start(cameraPreview.holder)
    }
  }

  // Stop detection and release resources
  private fun stopDetection() {
    cameraSource?.stop()
    cameraSource?.release()
    detector?.release()

    cameraSource = null
    detector = null
  }

  // Detector Processor callbacks

  override fun release() {
  }

  override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
    val detectedItems = detections?.detectedItems
    if (detectedItems?.isNotEmpty() == true) {
      detectedItems.valueAt(0)?.displayValue?.let { qrValue ->
        Log.d(tag, "QR Value: $qrValue")

        // Qr code contains a check-in url. We have to extract location paramter first.
        val httpUrl = qrValue.toHttpUrlOrNull() ?: return
        if (!httpUrl.pathSegments.contains("campus-qr")) {
          // Not a Campus Qr url
          return
        }
        // Extract location id query parameter
        val locationId = httpUrl.queryParameter("l") ?: return
        // Extract the base url
        val baseUrl = "${httpUrl.scheme}://${httpUrl.host}:${httpUrl.port}".removeSuffix(":80")
        if (locationId.isNotEmpty()) {
          runOnUiThread {
            stopDetection()
            checkIn(baseUrl = baseUrl, locationId = locationId, email = "name.lastname@uni.at")
          }
        }
      }
    }
  }

  // Sends a post request to the backend, linking provided location with the provided email address.
  private fun checkIn(baseUrl: String, locationId: String, email: String) {
    val client = OkHttpClient()

    val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()!!
    val requestBody = JSONObject()
      .put("email", email)
      .toString()
      .toRequestBody(jsonMediaType)

    val request = Request.Builder()
      .url("$baseUrl/location/$locationId/visit")
      .post(requestBody)
      .build()

    client.newCall(request).enqueue(responseCallback = object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        runOnUiThread {
          Log.d(tag, "request failed! $e")
          Toast.makeText(this@ScannerFragment.context, "Checking in failed.", Toast.LENGTH_SHORT).show()
        }
      }

      override fun onResponse(call: Call, response: Response) {
        runOnUiThread {
          Log.d(tag, "request success! ${response.body?.string()}")
          if (response.code == 200 && response.body?.string() == "ok") {
            Toast.makeText(this@ScannerFragment.context, "Checking in successful! :)", Toast.LENGTH_SHORT).show()
          } else {
            Toast.makeText(this@ScannerFragment.context, "Checking in failed.", Toast.LENGTH_SHORT).show()
          }
        }
      }
    })
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == REQUEST_CAMERA_PERMISSION) {
      startDetection()
    }
  }

  companion object {
    const val REQUEST_CAMERA_PERMISSION = 1234
  }
}