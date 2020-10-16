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

let includedCurrentLocation = false; // true if the provided locationId (below) has ever been checked in

function regenerateCheckoutView({locationId = "", onCurrentLocationRemoved = null, forceShow = false} = {}) {
  let savedCheckins = 0;
  let activeCheckinsWrapper = document.getElementById("active-checkins-wrapper");
  let activeCheckins = document.getElementById("active-checkins");
  // Remove previous checkin elements
  for (let i = activeCheckins.childElementCount - 1; i >= 0; i--) {
    activeCheckins.children[i].remove();
  }

  let foundCurrentLocation = false;

  for (let i = 0; i < window.localStorage.length; i++) {
    let key = window.localStorage.key(i);
    let value = window.localStorage.getItem(key);

    if (key.startsWith("checkin-")) { // Is a saved checkin
      savedCheckins += 1;

      if (key.split("checkin-")[1] === locationId) {
        foundCurrentLocation = true;
        includedCurrentLocation = true;
      }

      // Show activeCheckinsWrapper if at least one checkin is present
      activeCheckinsWrapper.className = activeCheckinsWrapper.className.replace("hidden", "");
      let checkinNode = activeCheckinsWrapper.children[2].cloneNode(true); // Clone pre-made checkin-node
      checkinNode.className = checkinNode.className.replace("hidden", "");
      checkinNode.children[0].innerText = value.split("::")[2];
      checkinNode.children[1].addEventListener("click", checkOut(key, checkinNode.childNodes[1]));

      activeCheckins.appendChild(checkinNode);
    }
  }

  if (includedCurrentLocation && !foundCurrentLocation && onCurrentLocationRemoved) {
    onCurrentLocationRemoved();
  }

  if (forceShow) {
    activeCheckinsWrapper.className = activeCheckinsWrapper.className.replace("hidden", "");
  }

  // Hide / show "no checkins" text if checkins were added / not added
  let noCheckinsClass = savedCheckins > 0 ? " hidden" : "";
  activeCheckinsWrapper.children[3].className = activeCheckinsWrapper.children[3].className.replace("hidden", "") + noCheckinsClass;
}