function onLoad() {
  regenerateCheckoutView();
  setInterval(regenerateCheckoutView, 1000);
}

if (/complete|interactive|loaded/.test(document.readyState)) {
  onLoad();
} else {
  document.addEventListener('DOMContentLoaded', onLoad, false);
}