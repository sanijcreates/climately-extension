// Check if the location has already been requested
chrome.storage.local.get("locationRequested", function (result) {
  if (result.locationRequested !== true) {
    chrome.storage.local.set({ locationRequested: true });

    // Open Google first
    window.open("https://www.google.com");

    // Request the user's location
    navigator.geolocation.getCurrentPosition(
      function (position) {
        alert(
          "Latitude: " +
            position.coords.latitude +
            "\n" +
            "Longitude: " +
            position.coords.longitude
        );
      },
      function (error) {
        console.error("Error getting location: ", error);
      }
    );
  } else {
    console.log("Location has already been requested.");
  }
});
