// App.jsx
import React from "react";
import EventComponent from "./EventComponent";

const App = ({ dateKey }) => {
  const events = [
    {
      id: 1,
      dateKey: "28227", // Monday, February 3
      startTime: "11:00am",
      endTime: "11:50am",
      title: "MATH 211",
      location: "Glatfelter Hall 102",
      color: "#039BE5",
    },
    {
      id: 2,
      dateKey: "28228",
      startTime: "8:00pm",
      endTime: "11:00pm",
      title: "LeetCode Meetings",
      location: "Glatfelater Hall, Basement",
      color: "#8e24aa",
    },
  ];

  const dayEvents = events.filter((event) => event.dateKey === dateKey);

  return (
    <div
      style={{
        position: "absolute",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        pointerEvents: "none",
      }}
    >
      {dayEvents.map((event) => (
        <EventComponent key={event.id} event={event} />
      ))}
    </div>
  );
};

export default App;
