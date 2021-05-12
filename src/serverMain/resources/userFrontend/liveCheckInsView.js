let fullLocationId = new URLSearchParams(window.location.search).get("l")
let liveCheckinDisabledHolder = document.getElementById("live-check-in-disabled")
let reEnableRefreshingButton = document.getElementById("re-enable-refreshing-button")
let liveCheckInCount = document.getElementById("live-check-in-count")

let pollCount = 0
let pollInterval = 5000
let maxPollCount = 300_000 / pollInterval // Poll for 5 minutes

function onLoad() {
  reEnableRefreshingButton.onclick = function () {
    pollCount = 0
    pollLiveCheckIns()
  }
  liveCheckinDisabledHolder.hidden = true
  liveCheckInCount.innerText = ""
  pollLiveCheckIns()
}

function pollLiveCheckIns() {
  if (fullLocationId == null) {
    return
  }
  pollCount += 1
  if (pollCount < maxPollCount) {
    liveCheckinDisabledHolder.hidden = true
    fetch(`/location/${fullLocationId}/pollLiveCheckIns`)
        .then(res => res.json())
        .then(json => {
          let activeCheckIns = json.activeCheckIns
          if (activeCheckIns !== undefined) {
            liveCheckInCount.innerText = activeCheckIns
          }
        })
        .catch(err => console.error(err))
        .finally(function () {
          setTimeout(pollLiveCheckIns, pollInterval)
        })
  } else {
    liveCheckinDisabledHolder.hidden = false
  }
}

if (/complete|interactive|loaded/.test(document.readyState)) {
  onLoad()
} else {
  document.addEventListener('DOMContentLoaded', onLoad, false)
}