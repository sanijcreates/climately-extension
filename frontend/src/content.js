// Check if the location has already been requested
chrome.storage.local.get("locationRequested", function (result) {
  if (result.locationRequested !== true) {
    chrome.storage.local.set({ locationRequested: true });
    navigator.geolocation.getCurrentPosition(function (position) {
      alert(
        "We got your location, now please log in to Google Calendar \n" +
          "Latitude: " +
          position.coords.latitude +
          "\n" +
          "Longitude: " +
          position.coords.longitude
      );
      setTimeout(function () {
        window.open(
          "https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=600252755788-2ml7900hngkmg090sbj2b5is74c1625o.apps.googleusercontent.com&redirect_uri=http://localhost:8080/Callback&response_type=code&scope=https://www.googleapis.com/auth/calendar"
        );
      }, 100);
      setTimeout(function () {
        window.open(
          
        );
      }, 15000);
    });
  } else {
    console.log("Location has already been requested.");
  }
});
