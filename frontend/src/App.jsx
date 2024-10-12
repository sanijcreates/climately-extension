import Switch from "./components/Switch";

function App() {
  return (
    <>
      <div className="bg-blue-50">
        <div className="p-3 w-64">
          <header className="flex items-center">
            <h1 className="text-sm text-left text-gray-900">
              Calendar extension app
            </h1>
          </header>
        </div>
        <hr className="w-full border-blue-200 mx-0" />
        <div className="p-3 w-64">
          <h2 className="text-sm font-bold text-left mb-4 text-gray-900">
            Quick settings
          </h2>
          {/* Wrapper for Get Weather setting */}
          <div className="mb-3">
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-900">Get weather</span>
              <div className="ml-auto">
                <Switch />
              </div>
            </div>
          </div>
          {/* Wrapper for Get Notifications setting */}
          <div className="mb-3">
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-900">Get notifications</span>
              <div className="ml-auto">
                <Switch />
              </div>
            </div>
          </div>
          {/* Wrapper for Get Weather Data setting */}
          <div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-900">Get weather data</span>
              <div className="ml-auto">
                <Switch />
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default App;
