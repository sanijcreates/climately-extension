import Switch from "./components/Switch";
import { useEffect, useState } from "react";

function App() {
  const [locationRequested, setLocationRequested] = useState(false);

  useEffect(() => {
    // Retrieve the location request status when the component mounts
    chrome.storage.local.get("locationRequested", (result) => {
      setLocationRequested(result.locationRequested || false);
    });
  }, []);

  // Function to reset the location request status
  const resetLocationRequest = () => {
    chrome.storage.local.set({ locationRequested: false }, () => {
      alert(
        "Location request has been reset. It will ask for your location the next time you open the extension."
      );
      setLocationRequested(false); // Update local state
    });
  };

  return (
    <>
      <div className="bg-blue-50">
        <div className="p-3 w-64">
          <header className="flex items-center">
            {/* <img src="assets/icon.png" className="w-6 h-6 mr-2" /> */}
            <h1 className="text-sm text-left text-gray-900">Calendar</h1>
          </header>
        </div>
        <hr className="w-full border-blue-200 mx-0 " />
        <div className="w-64">
          <div className="p-3 overflow-y-auto max-h-48">
            <h2 className="text-sm font-bold text-left mb-4 text-gray-900">
              Quick Settings
            </h2>
            {/* Scroll View */}
            <div className="mb-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-900">Get weather</span>
                <div className="ml-auto">
                  <Switch />
                </div>
              </div>
            </div>
            <div className="mb-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-900">Get notifications</span>
                <div className="ml-auto">
                  <Switch />
                </div>
              </div>
            </div>
            <div className="mb-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-900">Get weather data</span>
                <div className="ml-auto">
                  <Switch />
                </div>
              </div>
            </div>
            <div className="mb-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-900"> Get dates</span>
                <div className="ml-auto">
                  <Switch />
                </div>
              </div>
            </div>
            <div className="mb-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-900"> Get time</span>
                <div className="ml-auto">
                  <Switch />
                </div>
              </div>
            </div>
            {/* Add more settings here if needed */}
            {/* Centered Reset Location Button */}
          </div>
        </div>
        <hr className="border-blue-200 mx-0" />
        {/* Footer Section */}
        <div className="p-2 w-64">
          <footer className="flex justify-center items-center">
            <button
              onClick={resetLocationRequest}
              className="rounded bg-blue-500 w-full text-white py-2 px-4 hover:bg-blue-600"
            >
              Reset Location
            </button>
          </footer>
        </div>
      </div>
    </>
  );
}

export default App;
