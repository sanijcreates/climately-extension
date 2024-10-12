const geolocationChannel = new BroadcastChannel("geolocationChannel");

let locationRequested = false; // Track the location request status

geolocationChannel.onmessage = (event) => {
  console.log("Service Worker received:", event.data);

  if (event.data.action === "checkLocationRequest") {
    // Send back the status of the location request
    geolocationChannel.postMessage({ locationRequested });
  } else if (event.data.action === "setLocationRequested") {
    // Set the location request status based on the message
    locationRequested = event.data.locationRequested;
  }
};
