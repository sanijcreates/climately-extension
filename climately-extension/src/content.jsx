// content.jsx
import React from "react";
import ReactDOM from "react-dom";
import App from "./App";

const injectCalendarEvents = () => {
  const gridcells = document.querySelectorAll(
    "div[role='gridcell'][data-datekey]"
  );

  gridcells.forEach((gridcell) => {
    const existingReactContainer = gridcell.querySelector(
      ".react-events-container"
    );
    if (existingReactContainer) return;

    const dateKey = gridcell.getAttribute("data-datekey");
    const reactContainer = document.createElement("div");
    reactContainer.className = "react-events-container";
    reactContainer.style.position = "absolute";
    reactContainer.style.top = "0";
    reactContainer.style.left = "0";
    reactContainer.style.width = "100%";
    reactContainer.style.height = "100%";
    reactContainer.style.pointerEvents = "none";

    gridcell.appendChild(reactContainer);
    ReactDOM.render(<App dateKey={dateKey} />, reactContainer);
  });
};

// Initial injection
document.addEventListener("DOMContentLoaded", injectCalendarEvents);

// Re-inject when calendar changes
const observer = new MutationObserver(injectCalendarEvents);
observer.observe(document.body, { childList: true, subtree: true });
