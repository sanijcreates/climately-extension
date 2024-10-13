// Check if the location has already been requested
chrome.storage.local.get("locationRequested", function (result) {
  if (result.locationRequested !== true) {
    chrome.storage.local.set({ locationRequested: true });
    window.open("https://www.google.com");
    navigator.geolocation.getCurrentPosition(function (position) {
      alert(
        "Latitude: " +
          position.coords.latitude +
          "\n" +
          "Longitude: " +
          position.coords.longitude
      );
    });
  } else {
    console.log("Location has already been requested.");
  }
});
