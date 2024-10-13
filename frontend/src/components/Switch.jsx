import { useState } from "react";
import classNames from "classnames";

export default function Switch() {
  const [isSelected, setIsSelected] = useState(false);

  return (
    <div
      onClick={() => setIsSelected(!isSelected)}
      className={classNames(
        "flex items-center w-12 h-6 bg-gray-500 rounded-full transition-all duration-300 shadow-lg cursor-pointer", // Adjusted width and height
        {
          "bg-green-500": isSelected,
        }
      )}
    >
      <span
        className={classNames(
          "h-6 w-6 bg-white rounded-full transition-all duration-300 transform", // Adjusted size of the inner switch
          {
            "translate-x-6": isSelected, // Use translate instead of margin
          }
        )}
      />
    </div>
  );
}
