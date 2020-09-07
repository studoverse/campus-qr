function pad(num, size) {
  return ('000' + num).substr(-size)
}

let locationId = new URLSearchParams(window.location.search).get("l")

// True if the page was opened directly from the QR code and not reloaded / bookmarked
let justScanned = false;

function onLoad() {
  if (window.location.search.includes("s=1")) {
    // If present, remove "just scanned" parameter from url and replace history state so the user cannot check in again by just reloading
    let url = window.location.href.split(window.location.search)[0];
    window.history.replaceState(null, null, url + "?l=" + locationId);
    window.localStorage.removeItem("checkin-" + locationId);
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
  if (!justScanned) {
    overlay.className = overlay.className.replace("hidden", "");
    return;
  }

  if (!locationId) {
    overlay.className = overlay.className.replace("hidden", "");

  } else {
    let emailInput = document.getElementById("email-input");
    let acceptTosCheckbox = document.getElementById("accept-tos-checkbox");
    let submitButton = document.getElementById("submit-button");
    let resultOkWrapper = document.getElementsByClassName("result-wrapper")[0];
    let resultNotAllowed = document.getElementById("result-not-allowed");
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
      resultNotAllowed.className = resultNotAllowed.className.replace("hidden", "") + " hidden";
      resultNetErr.className = resultNetErr.className.replace("hidden", "") + " hidden";

      let submitButtonText = submitButton.innerText;
      let xhttp = new XMLHttpRequest();
      xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
          setTimeout(() => {
            submitButton.innerText = submitButtonText
            if (this.status === 200) {
              // Success
              setDatetimeElement(Date.now())
              window.localStorage.setItem("email", email);
              window.localStorage.setItem("checkin-" + locationId, `${btoa(email)}::${Date.now().toString()}`);
              showCheckin(email)

            } else if (this.status === 403) {
              resultNotAllowed.className = resultNotAllowed.className.replace("hidden", "");

            } else {
              // Network or internal error
              resultNetErr.className = resultNetErr.className.replace("hidden", "");
            }
          }, 700)
        }
      }
      xhttp.open("POST", "/location/" + locationId + "/visit");
      xhttp.send(JSON.stringify({email: email}));
      submitButtonText = submitButton.innerText
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
function showCheckin(email, time = null) {
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

function handleOldCheckin({onShowNewCheckin = null, onShowLastCheckin = null, onCheckinExpired = null}) {
  let lastCheckin = window.localStorage.getItem("checkin-" + locationId)
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
  let overlayExpired = document.getElementById("overlay-expired");
  let overlayRetry = document.getElementById("overlay-retry");
  let resultOkWrapper = document.getElementsByClassName("result-wrapper")[0];
  let resultNotAllowed = document.getElementById("result-not-allowed");
  let resultNetErr = document.getElementById("result-net-err");

  // Hide results
  resultOkWrapper.className = resultOkWrapper.className.replace("hidden", "") + " hidden";
  resultNotAllowed.className = resultNotAllowed.className.replace("hidden", "") + " hidden";
  resultNetErr.className = resultNetErr.className.replace("hidden", "") + " hidden";

  overlayRetry.className = overlayRetry.className.replace("hidden", "") + "hidden"; // Hide default text
  overlayExpired.className = overlayExpired.className.replace("hidden", ""); // Show expired text
  overlay.className = overlay.className.replace("hidden", ""); // show overlay
}

// Deletes expired checkins from other locations that are older than 24 hours
function deleteExpiredCheckins() {
  for (let i = 0; i < window.localStorage.length; i++) {
    let key = window.localStorage.key(i);

    // Only remove checkins from different locations
    if (key.startsWith("checkin-") && key.split("checkin-")[1] !== locationId) {
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
  window.location.href = window.location.href.replace("&s=1", "") + "&s=1"
}

document.getElementById("lang-select").onclick = changeLanguageTo;
