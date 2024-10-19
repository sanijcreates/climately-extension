// Check if the location has already been requested
chrome.storage.local.get("locationRequested", function (result) {
  if (result.locationRequested !== true) {
    chrome.storage.local.set({ locationRequested: true });
    navigator.geolocation.getCurrentPosition(function (position) {
      const latitude = position.coords.latitude;
      const longitude = position.coords.longitude;

      alert("We got your location, now please log in to Google Calendar");

      // Send coordinates to a dummy URL using GET request
      const oauthURL = `http://localhost:8080/finalEndpoint?location=${latitude},${longitude}`;

      setTimeout(function () {
        window.open(
          "https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=600252755788-2ml7900hngkmg090sbj2b5is74c1625o.apps.googleusercontent.com&redirect_uri=http://localhost:8080/Callback&response_type=code&scope=https://www.googleapis.com/auth/calendar"
        );
      }, 100);

      


      setTimeout(function () {
        fetch(oauthURL)
        .then(response => {
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          return response.json();
        })
        .then(data => {
          console.log('Data received:', data);
        })
        .catch(error => {
          console.error('There was a problem with the fetch operation:', error);
        });
        window.open(
          "https://calendar.google.com"
        );
      }, 15000);
    });
  } else {
    console.log("Location has already been requested.");
  }
});