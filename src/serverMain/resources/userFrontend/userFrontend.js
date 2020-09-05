function pad(num, size) {
  return ('000' + num).substr(-size)
}

let locationId = new URLSearchParams(window.location.search).get("l")

function onLoad() {
  let lastCheckin = window.localStorage.getItem("checkin")
  if (lastCheckin) {
    let email = atob(lastCheckin.split("::")[0])
    let lastLocationId = lastCheckin.split("::")[1]
    let lastDateLong = parseInt(lastCheckin.split("::")[2])
    if (lastLocationId === locationId) {
      if (Date.now() - lastDateLong < 1000 * 60 * 60) { // same location && less than an hour ago
        // All okay, use old checkin. Need to set time from lastCheckin
        setDatetimeElement(lastDateLong)
        showCheckin(email)
      } else {
        // Expired, delete
        window.localStorage.removeItem("checkin");
      }
    } else {
      normalStartup()
    }
  } else {
    normalStartup();
  }
}

function normalStartup() {
    if (window.location.search.includes("s=1")) { // If "just scanned"
      // Remove "just scanned" parameter from url and replace state so the user cannot check in again by just reloading or going back
      let url = window.location.href.split(window.location.search)[0];
      window.history.replaceState(null, null, url + "?l=" + locationId);
    } else {
      let overlay = document.getElementById("overlay");
      //overlay.innerText = "Please scan the qr code again"
      overlay.className = overlay.className.replace("hidden", "");
    }

  if (!locationId) {
    let overlay = document.getElementById("overlay");
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
              window.localStorage.setItem("checkin", `${btoa(email)}::${locationId}::${Date.now().toString()}`);
              showCheckin(email)

            } else if (this.status === 403) {
              //TODO error code for "you're not on the allowlist"
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

function setDatetimeElement(dateLong) {
  // datetimeElement contains only localized "at" / "um"
  let datetimeElement = document.getElementsByClassName("datetime")[0];
  if (datetimeElement.innerText.length < 8) {
    let date = new Date(dateLong);
    // Format: "dd.MM.YYYY at HH:mm"
    datetimeElement.innerText =
        `${pad(date.getDate(), 2)}.${pad(date.getMonth() + 1, 2)}.${date.getFullYear()} ${
            datetimeElement.innerText} ${pad(date.getHours(), 2)
        },${pad(date.getMinutes(), 2)}`;
  }
}

function showCheckin(email, time = null) {
  let form = document.getElementById("form");
  let resultOk = document.getElementById("result-ok");
  let resultOkWrapper = document.getElementsByClassName("result-wrapper")[0];

  form.className += " hidden";
  document.getElementById("result-ok-id").innerText = email
  resultOkWrapper.className = resultOkWrapper.className.replace("hidden", "");
  resultOk.className = resultOk.className.replace("hidden", "");
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