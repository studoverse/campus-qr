let baseUrl = document.querySelector('meta[name="qrCodeBaseUrl"]').content

function generateQrCode(element) {
  let locationId = element.id;
  let subUrl = locationId === "checkout" ? "/checkout" : ("?s=1&l=" + locationId);
  new QRCode(element, {
    text: baseUrl + "/campus-qr" + subUrl,
    width: 512,
    height: 512
  });
}

let elements = document.getElementsByClassName("qrcode");
let loadingText = document.getElementById("loading-text");

// Performance optimized function. Allows DOM to update before generating next qr code
function generateNext(index) {
  if (index < elements.length) {
    // Generate next
    let element = elements[index];
    generateQrCode(element);

    // Update ui progress
    if (loadingText && index % 10 === 0) { // Only update text every 10 elements
      loadingText.innerText = Math.round(index * 100 / elements.length) + " %"
    }

    // Add to end of event-loop
    setTimeout(() => generateNext(index + 1), 0)
  } else {
    // Finished
    if (loadingText) {
      loadingText.remove();
    }

    // Show codes
    let allCodesWrapper = document.getElementById("all-codes")
    allCodesWrapper.className = allCodesWrapper.className.replace("hidden", "")
  }
}

generateNext(0);