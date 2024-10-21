import { useEffect, useState } from "react";
import classNames from "classnames";

export default function Switch({ isOn, handleToggle }) {
  const [shouldAnimate, setShouldAnimate] = useState(false);

  useEffect(() => {
    // Set a delay before enabling animation to avoid initial load animation
    const timeoutId = setTimeout(() => setShouldAnimate(true), 100); // delay by 100ms to avoid animation on load

    return () => clearTimeout(timeoutId); // Cleanup timeout on component unmount
  }, []);

  return (
    <div
      onClick={handleToggle}
      className={classNames(
        "flex w-10 h-5 bg-gray-500 rounded-full transition-all duration-300 shadow-lg cursor-pointer",
        {
          "bg-green-500": isOn,
        }
      )}
    >
      <span
        className={classNames("h-5 w-5 bg-white rounded-full transform", {
          "translate-x-5": isOn,
          // Apply the transition class only after the initial delay
          "transition-all duration-300": shouldAnimate,
        })}
      />
    </div>
  );
}
