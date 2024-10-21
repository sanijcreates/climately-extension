import Switch from "./components/Switch";
import { useEffect, useState } from "react";

function App() {
  const [locationRequested, setLocationRequested] = useState(false);
  const [notificationsEnabled, setNotificationsEnabled] = useState(false);
  const [averageTempEnabled, setAverageTempEnabled] = useState(false);

  useEffect(() => {
    // Retrieve the states from chrome.storage.local when the component mounts
    chrome.storage.local.get(
      ["locationRequested", "notificationsEnabled", "averageTempEnabled"],
      (result) => {
        setLocationRequested(result.locationRequested || false);
        setNotificationsEnabled(result.notificationsEnabled || false);
        setAverageTempEnabled(result.averageTempEnabled || false);
      }
    );
  }, []);

  const handleToggle = (key, value) => {
    chrome.storage.local.set({ [key]: value }, () => {
      console.log(`${key} has been set to ${value}`);
    });
  };

  function create_oauth2_url() {
    // let nonce = encodeURIComponent(
    //   Math.random().toString(36).substring(2, 15) +
    //     Math.random().toString(36).substring(2, 15)
    // );

    const clientId = encodeURIComponent(
      "253329998078-3pb9onak1atd3i0f1c31sn3oj033jq0k.apps.googleusercontent.com"
    );
    const responseType = encodeURIComponent("token");
    const redirectURI = encodeURIComponent(
      "https://gljfejoplneepammmlackdnlipfgfcgh.chromiumapp.org"
    );
    const scope = encodeURIComponent(
      "https://www.googleapis.com/auth/calendar"
    );
    const state = encodeURIComponent("jfkls3n");
    const prompt = encodeURIComponent("consent");

    let url = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&response_type=${responseType}&redirect_uri=${redirectURI}&state=${state}&scope=${scope}&prompt=${prompt}`;

    return url;
  }

  const handleAuthentication = () => {
    chrome.identity.launchWebAuthFlow(
      {
        url: create_oauth2_url(),
        interactive: true,
      },
      function (redirect_url) {
        if (chrome.runtime.lastError) {
          console.error(
            "Authentication failed: " + chrome.runtime.lastError.message
          );
          return;
        }
        // Extract the access token from the redirect URL
        const urlParams = new URLSearchParams(
          new URL(redirect_url).hash.substring(1)
        );
        const accessToken = urlParams.get("access_token");
        if (accessToken) {
          console.log("Access Token: ", accessToken);
          // Now you can use the access token to call Google Calendar API
        } else {
          console.error("Access token not found in the redirect URL.");
        }
      }
    );
  };

  const resetLocationRequest = () => {
    chrome.storage.local.set({ locationRequested: false }, () => {
      alert(
        "Location request has been reset. It will ask for your location the next time you open the extension."
      );
      setLocationRequested(false); // Update local state
    });
    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
      if (tabs.length > 0) {
        chrome.tabs.reload(tabs[0].id);
      }
    });

    document
      .getElementById("reloadButton")
      .addEventListener("click", reloadParentPage);
  };

  return (
    <>
      <div className="bg-blue-50">
        <div className="p-3 w-64">
          <header className="flex items-center">
            <img
              src="assets/icon.png"
              alt="Calendaly Logo"
              className="w-5 h-5 mr-2"
            />
            <h1 className="text-sm text-left text-gray-900">Climately</h1>
          </header>
        </div>
        <hr className="w-full border-blue-200 mx-0 " />
        <div className="w-64">
          <div className="p-3 overflow-y-auto max-h-48">
            <h2 className="text-sm font-bold text-left mb-4 text-gray-900">
              Quick Settings
            </h2>

            <div className="mb-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-900">Notifications</span>
                <div className="ml-auto">
                  <Switch
                    isOn={notificationsEnabled}
                    handleToggle={() => {
                      setNotificationsEnabled(!notificationsEnabled);
                      handleToggle(
                        "notificationsEnabled",
                        !notificationsEnabled
                      );
                    }}
                  />
                </div>
              </div>
            </div>

            <div className="mb-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-900">
                  Average Daily Temperature
                </span>
                <div className="ml-auto">
                  <Switch
                    isOn={averageTempEnabled}
                    handleToggle={() => {
                      setAverageTempEnabled(!averageTempEnabled);
                      handleToggle("averageTempEnabled", !averageTempEnabled);
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        <hr className="border-blue-200 mx-0" />
        <div className="p-2 w-64">
          <footer className="flex flex-col justify-center items-center space-y-2">
            <button
              onClick={resetLocationRequest}
              className="rounded bg-blue-500 w-full text-white py-2 px-4 hover:bg-blue-600"
            >
              Reset Climately
            </button>
            <button
              onClick={handleAuthentication}
              className="rounded bg-blue-500 w-full text-white py-2 px-4 hover:bg-blue-600"
            >
              Log In With Google
            </button>
          </footer>
        </div>
      </div>
    </>
  );
}

export default App;
