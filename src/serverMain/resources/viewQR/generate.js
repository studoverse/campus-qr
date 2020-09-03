let href = window.location.href;
let baseUrl = href.substring(0, href.lastIndexOf("/location/"))

let elements = document.getElementsByClassName("qrcode");
for (let i = 0; i < elements.length; i++) {
  let element = elements[i];
  let locationId = element.id;
  new QRCode(element, {
    text: baseUrl + "/campus-qr?s=1&l=" + locationId,
    width: 512,
    height: 512
  });
}