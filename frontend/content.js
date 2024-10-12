navigator.geolocation.getCurrentPosition(function (position) {
  alert(
    "Latitude : " +
      position.coords.latitude +
      ":" +
      "Longitude : " +
      position.coords.longitude
  );
});
