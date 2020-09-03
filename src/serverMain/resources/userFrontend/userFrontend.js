let locationId = new URLSearchParams(window.location.search).get("l")

function onLoad() {
  if (window.location.search.includes("s=1")) {
    let url = window.location.href.split(window.location.search)[0];
    window.history.replaceState(null, null, url + "?l=locationName");
  } else {
    let overlay = document.getElementById("overlay");
    //overlay.innerText = "Please scan the qr code again"
    overlay.className = overlay.className.replace("hidden", "");
  }

  if (!locationId) {
    let overlay = document.getElementById("overlay");
    overlay.className = overlay.className.replace("hidden", "");

  } else {
    let form = document.getElementById("form");
    let emailInput = document.getElementById("email-input");
    let acceptTosCheckbox = document.getElementById("accept-tos-checkbox");
    let submitButton = document.getElementById("submit-button");
    let resultOk = document.getElementById("result-ok");
    let resultFail = document.getElementById("result-fail");

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
      resultOk.className = resultOk.className.replace("hidden", "") + " hidden";
      resultFail.className = resultFail.className.replace("hidden", "") + " hidden";

      let xhttp = new XMLHttpRequest();
      let done = false;
      xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
          done = true;
          submitButton.innerText = submitButton.innerText.replace("...", "");
          if (this.status === 200) {
            // Success
            window.localStorage.setItem("email", email);
            form.className += " hidden";
            resultOk.className = resultOk.className.replace("hidden", "");
          } else {
            // Failure
            resultFail.className = resultFail.className.replace("hidden", "");
          }
        }
      }
      xhttp.open("POST", "/location/" + locationId + "/visit");
      xhttp.send(JSON.stringify({email: email}));
      setTimeout(() => {
        if (!done) {
          submitButton.innerText += "..."
        }
      }, 150)
    }

    submitButton.onclick = onSubmit;
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