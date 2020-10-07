function onLoad() {
  regenerateCheckoutView();
  setInterval(regenerateCheckoutView, 1000);
}

if (document.readyState === "loading") {
  document.onload = onLoad
} else {
  onLoad();
}

