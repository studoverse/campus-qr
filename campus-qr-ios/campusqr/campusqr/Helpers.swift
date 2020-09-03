import Foundation
import UIKit

// MARK: Helper functions

/// Makes a post request with a json-encoded body.
func postRequest(url: String, json: Any, onResponse: @escaping (String?) -> Void) {
  let jsonData = try? JSONSerialization.data(withJSONObject: json)
  let url = URL(string: url)!
  var request = URLRequest(url: url)
  request.httpMethod = "POST"
  request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
  
  // insert json data to the request
  request.httpBody = jsonData
  
  URLSession.shared.dataTask(with: request) { data, response, error in
    guard let data = data, error == nil else {
      onResponse(nil)
      return
    }
    let response = String(decoding: data, as: UTF8.self)
    
    onResponse(response)
  }.resume()
}

func runAsync(block: @escaping () -> Void) {
  DispatchQueue.global(qos: .background).async(execute: block)
}

func runOnUiThread(block: @escaping () -> Void) {
  DispatchQueue.main.async(execute: block)
}

extension UIViewController {
  func showToast(message: String) {
    let alert = UIAlertController(title: nil, message: message,
                                  preferredStyle: .alert)
    alert.view.backgroundColor = UIColor.black
    alert.view.alpha = 0.6
    alert.view.layer.cornerRadius = 15
    present(alert, animated: true)
    DispatchQueue.main.asyncAfter(
      deadline: DispatchTime.now() + 4.0, execute: {
        alert.dismiss(animated: true)
    })
  }
}
