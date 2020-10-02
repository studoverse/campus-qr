function pad(num, size) {
  return ('000' + num).substr(-size)
}

let fullLocationId = new URLSearchParams(window.location.search).get("l")

// True if the page was opened directly from the QR code and not reloaded / bookmarked
let justScanned = false;

function onLoad() {
  if (window.location.search.includes("s=1")) {
    // If present, remove "just scanned" parameter from url and replace history state so the user cannot check in again by just reloading
    let url = window.location.href.split(window.location.search)[0];
    if (!window.location.search.includes("clang=1")) {
      window.localStorage.removeItem("checkin-" + fullLocationId);
    }
    window.history.replaceState(null, null, url + "?l=" + fullLocationId);
    justScanned = true;
  }

  handleOldCheckin({
    onShowLastCheckin: function (lastDateLong, email) {
      // All okay, use old checkin. Need to set displayed check-in time from last checkin
      setDatetimeElement(lastDateLong);
      showCheckin(email);
    },
    onShowNewCheckin: normalStartup,
    onCheckinExpired: checkinExpired,
  })

  deleteExpiredCheckins();
}

function normalStartup() {
  let overlay = document.getElementById("overlay");
  let contentWrapper = document.getElementById("all-content-wrapper");
  if (!justScanned) {
    overlay.className = overlay.className.replace("hidden", "");
    contentWrapper.className = contentWrapper.className.replace("hidden", "") + "hidden"; // Hide page content
    return;
  }

  if (!fullLocationId) {
    overlay.className = overlay.className.replace("hidden", "");
    contentWrapper.className = contentWrapper.className.replace("hidden", "") + "hidden"; // Hide page content

  } else {
    let emailInput = document.getElementById("email-input");
    let acceptTosCheckbox = document.getElementById("accept-tos-checkbox");
    let submitButton = document.getElementById("submit-button");
    let resultOkWrapper = document.getElementsByClassName("result-wrapper")[0];
    let resultForbiddenAccessRestricted = document.getElementById("result-forbidden-access-restricted");
    let resultForbiddenEmail = document.getElementById("result-forbidden-email");
    let resultNetErr = document.getElementById("result-net-err");

    let email = window.localStorage.getItem("email");
    if (email) {
      emailInput.value = email;
      acceptTosCheckbox.checked = true;
    }

    function onSubmit() {
      let emailWrapper = emailInput.parentElement;
      let email = emailInput.value.replace(" ", "")
      if (email.length < 5 || !email.includes("@") || !email.includes(".")) {
        emailWrapper.className = emailWrapper.className.replace("highlighted", "") + " highlighted";
        return;
      } else {
        emailWrapper.className = emailWrapper.className.replace("highlighted", "");
      }
      let acceptTosCheckboxWrapper = acceptTosCheckbox.parentElement;
      if (acceptTosCheckbox.checked !== true) {
        acceptTosCheckboxWrapper.className = acceptTosCheckboxWrapper.className.replace("highlighted", "") + " highlighted";
        return;
      } else {
        acceptTosCheckboxWrapper.className = acceptTosCheckboxWrapper.className.replace("highlighted", "");
      }

      // Reset result visibility (hide)
      resultOkWrapper.className = resultOkWrapper.className.replace("hidden", "") + " hidden";
      resultForbiddenAccessRestricted.className = resultForbiddenAccessRestricted.className.replace("hidden", "") + " hidden";
      resultForbiddenEmail.className = resultForbiddenEmail.className.replace("hidden", "") + " hidden";
      resultNetErr.className = resultNetErr.className.replace("hidden", "") + " hidden";

      let submitButtonText = submitButton.innerText;
      let xhttp = new XMLHttpRequest();
      xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
          setTimeout(() => {
            submitButton.innerText = submitButtonText
            if (this.status === 200) {
              if (this.responseText === "ok") {
                let locationName = document.head.querySelector("[name~=location-name][content]").content;
                if (fullLocationId.includes("-")) {
                  locationName += ` #${fullLocationId.split("-")[1]}`;
                }
                // Success
                setDatetimeElement(Date.now())
                window.localStorage.setItem("email", email);
                window.localStorage.setItem("checkin-" + fullLocationId, `${btoa(email)}::${Date.now().toString()}::${locationName}`);
                showCheckin(email)
              } else if (this.responseText === "forbidden_access_restricted") {
                // E-Mail address not found in restricted access email list
                resultForbiddenAccessRestricted.className = resultForbiddenAccessRestricted.className.replace("hidden", "");
              } else if (this.responseText === "forbidden_email") {
                // E-Mail address does not match allowed email-regex
                resultForbiddenEmail.className = resultForbiddenEmail.className.replace("hidden", "");
              }
            } else {
              // Network or internal error
              resultNetErr.className = resultNetErr.className.replace("hidden", "");
            }
          }, 700)
        }
      }
      xhttp.open("POST", "/location/" + fullLocationId + "/visit");
      xhttp.send(JSON.stringify({email: email}));
      submitButton.innerText = "..."
    }

    submitButton.onclick = onSubmit;
  }
}

// Sets the correct checkin time, either from lastCheckin (loaded) or from current time
function setDatetimeElement(dateLong) {
  // datetimeElement contains only localized "at" / "um"
  let datetimeElement = document.getElementsByClassName("datetime")[0];
  if (datetimeElement.innerText.length < 8) {
    let date = new Date(dateLong);
    // Format: "dd.MM.YYYY at HH:mm"
    datetimeElement.innerText =
        `${pad(date.getDate(), 2)}.${pad(date.getMonth() + 1, 2)}.${date.getFullYear()} ${
            datetimeElement.innerText} ${pad(date.getHours(), 2)}:${pad(date.getMinutes(), 2)}`;
  }
}

// Hides the form and shows the checkin / verification view
function showCheckin(email) {
  let form = document.getElementById("form");
  let resultOk = document.getElementById("result-ok");
  let resultOkWrapper = document.getElementsByClassName("result-wrapper")[0];

  form.className += " hidden";
  document.getElementById("result-ok-id").innerText = email
  resultOkWrapper.className = resultOkWrapper.className.replace("hidden", "");
  resultOk.className = resultOk.className.replace("hidden", "");

  // Check if checkin has expired every four seconds
  let interval = setInterval(function () {
    handleOldCheckin(
        {
          onCheckinExpired: function () {
            clearInterval(interval);
            checkinExpired();
          }
        }
    );
  }, 4000);
}

function checkOut(checkinKey, buttonElement) {
  return function () {

    let buttonText = buttonElement.innerText;
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
      if (this.readyState === 4) {
        setTimeout(() => {
          buttonElement.innerText = buttonText;
          if (fullLocationId === checkinKey.split("checkin-")[1]) {
            checkinExpired();
          }
          handleOldCheckin({});
        }, 700);
      }
    }
    buttonElement.innerText = "...";
    xhttp.open("POST", `/location/${checkinKey.split("checkin-")[1]}/checkout`);
    xhttp.send(JSON.stringify({email: atob(window.localStorage[checkinKey].split("::")[0])}));
    window.localStorage.removeItem(checkinKey); // Check out locally in any case, even if the checkout call fails
  }
}

function handleOldCheckin({onShowNewCheckin = null, onShowLastCheckin = null, onCheckinExpired = null}) {
  let checkinsWrapper = document.getElementById("checkins-wrapper");
  // Remove previous elements
  for (let i = checkinsWrapper.childElementCount - 1; i >= 2; i--) {
    checkinsWrapper.removeChild(checkinsWrapper.children[i]);
  }

  for (let i = 0; i < window.localStorage.length; i++) {
    let key = window.localStorage.key(i);
    let value = window.localStorage.getItem(key);

    if (key.startsWith("checkin-")) { // is a saved checkin
      // Show checkins wrapper if at least one checkin is present
      checkinsWrapper.className = checkinsWrapper.className.replace("hidden", "");

      let checkinNode = checkinsWrapper.children[1].cloneNode(true);
      checkinNode.className = checkinNode.className.replace("hidden", "");
      checkinNode.childNodes[0].innerText = value.split("::")[2];
      checkinNode.childNodes[1].onclick = checkOut(key, checkinNode.childNodes[1]);

      checkinsWrapper.appendChild(checkinNode);

    }
  }

  let lastCheckin = window.localStorage.getItem("checkin-" + fullLocationId)
  if (lastCheckin) {
    let email = atob(lastCheckin.split("::")[0])
    let lastDateLong = parseInt(lastCheckin.split("::")[1])
    if (Date.now() - lastDateLong < 1000 * 60 * 60) {
      // Checkin not expired
      if (onShowLastCheckin) onShowLastCheckin(lastDateLong, email);
    } else {
      // Checkin expired
      if (onCheckinExpired) onCheckinExpired();
      if (onShowNewCheckin) onShowNewCheckin();
    }
  } else {
    // No saved checkin
    if (onShowNewCheckin) onShowNewCheckin();
  }
}

function checkinExpired() {
  let overlay = document.getElementById("overlay");
  overlay.className = overlay.className.replace("hidden", ""); // show overlay

  let overlayExpired = document.getElementById("overlay-expired");
  overlayExpired.className = overlayExpired.className.replace("hidden", ""); // Show expired text

  let overlayRetry = document.getElementById("overlay-retry");
  overlayRetry.className = overlayRetry.className.replace("hidden", "") + "hidden"; // Hide default text

  let contentWrapper = document.getElementById("all-content-wrapper");
  contentWrapper.className = contentWrapper.className.replace("hidden", "") + "hidden"; // Hide page content
}

// Deletes expired checkins from other locations that are older than 24 hours
function deleteExpiredCheckins() {
  for (let i = 0; i < window.localStorage.length; i++) {
    let key = window.localStorage.key(i);

    // Only remove checkins from different locations
    if (key.startsWith("checkin-") && key !== "checkin-" + fullLocationId) {
      let checkinDate = new Date(window.localStorage.getItem(key).split("::")[1]);
      if (new Date() - checkinDate > 1000 * 60 * 60 * 24) { // Only remove checkins older than 24 hours
        window.localStorage.removeItem(key);
      }
    }
  }
}

if (document.readyState === "loading") {
  document.onload = onLoad
} else {
  onLoad();
}

function changeLanguageTo() {
  let lang = document.getElementById("lang-select").value;
  document.cookie = "MbLang=" + lang
  // Reload page with added s=1 parameter to prevent overlay from showing.
  // Normally we would delete this check-in from localstorage so we add clang=1 which indicates that the language was changed and the
  //  current check-in should not be deleted.
  window.location.href = window.location.href.replace("&s=1", "") + "&s=1&clang=1";
}

document.getElementById("lang-select").onclick = changeLanguageTo;
