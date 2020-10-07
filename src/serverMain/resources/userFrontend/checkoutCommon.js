function checkOut(checkinKey, buttonElement) {
  return function () {

    let buttonText = buttonElement.innerText;
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
      if (this.readyState === 4) {
        setTimeout(() => {
          buttonElement.innerText = buttonText;
          if (typeof fullLocationId !== 'undefined' && fullLocationId === checkinKey.split("checkin-")[1]) {
            hideVerification();
          }
          regenerateCheckoutView();
        }, 700);
      }
    }
    buttonElement.innerText = "...";
    xhttp.open("POST", `/location/${checkinKey.split("checkin-")[1]}/checkout`);
    xhttp.send(JSON.stringify({email: atob(window.localStorage[checkinKey].split("::")[0])}));
    window.localStorage.removeItem(checkinKey); // Check out locally in any case, even if the checkout call fails
  }
}

function regenerateCheckoutView({locationId = "", onCurrentLocationRemoved = null} = {}) {
  let savedCheckins = 0;
  let checkinsWrapper = document.getElementById("checkins-wrapper");
  // Remove previous elements
  for (let i = checkinsWrapper.childElementCount - 1; i >= 4; i--) {
    checkinsWrapper.removeChild(checkinsWrapper.children[i]);
  }

  let foundCurrentLocation = false;

  for (let i = 0; i < window.localStorage.length; i++) {
    let key = window.localStorage.key(i);
    let value = window.localStorage.getItem(key);

    if (key.startsWith("checkin-")) { // Is a saved checkin
      savedCheckins += 1;

      if (key.split("checkin-")[1] === locationId) {
        foundCurrentLocation = true;
      }

      // Show checkins wrapper if at least one checkin is present
      checkinsWrapper.className = checkinsWrapper.className.replace("hidden", "");

      let checkinNode = checkinsWrapper.children[2].cloneNode(true);
      checkinNode.className = checkinNode.className.replace("hidden", "");
      checkinNode.children[0].innerText = value.split("::")[2];
      checkinNode.children[1].addEventListener("click", checkOut(key, checkinNode.childNodes[1]));

      checkinsWrapper.appendChild(checkinNode);
    }
  }

  if (!foundCurrentLocation && onCurrentLocationRemoved) {
    onCurrentLocationRemoved();
  }

  // Hide / show "no checkins" text if checkins were added / not added
  let noCheckinsClass = savedCheckins > 0 ? " hidden" : "";
  checkinsWrapper.children[3].className = checkinsWrapper.children[3].className.replace("hidden", "") + noCheckinsClass;
}