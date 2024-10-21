chrome.storage.local.get("locationRequested", function (result) {
  if (result.locationRequested !== true) {
    chrome.storage.local.set({ locationRequested: true });
    navigator.geolocation.getCurrentPosition(function (position) {
      const lat = position.coords.latitude;
      const long = position.coords.longitude;
      const notificationsEnabled = result.notificationsEnabled || false;
      alert(
        "We retrieved your location, now please log in to Google Calendar \n" +
          "Latitude: " +
          lat + // Accessible here
          "\n" +
          "Longitude: " +
          long // Accessible here
      );
      setTimeout(function () {
        window.open(
          `http://localhost:8080/finalEndpoint?location=${lat},${long}&notifications=${notificationsEnabled}`
        );
      }, 15000);
    });
  } else {
    console.log("Location has already been requested.");
  }
});
