// EventComponent.jsx
import React from "react";

const parseTimeToMinutes = (timeStr) => {
  const [time, modifier] = timeStr.split(/(am|pm)/i);
  let [hours, minutes] = time.split(":").map(Number);
  if (modifier.toLowerCase() === "pm" && hours !== 12) hours += 12;
  if (modifier.toLowerCase() === "am" && hours === 12) hours = 0;
  return hours * 60 + (minutes || 0);
};

const EventComponent = ({ event }) => {
  // Precision-tuned values from Google Calendar's DOM measurements
  const PIXELS_PER_MINUTE = 0.7992; // 959px ÷ 1200 minutes (8PM)
  const TIME_OFFSET_MINUTES = 1; // Compensate for 20-minute visual lag

  const startMinutes =
    parseTimeToMinutes(event.startTime) + TIME_OFFSET_MINUTES;
  const endMinutes = parseTimeToMinutes(event.endTime) + TIME_OFFSET_MINUTES;

  const top = Math.round(startMinutes * PIXELS_PER_MINUTE);
  const height = Math.round((endMinutes - startMinutes) * PIXELS_PER_MINUTE);

  return (
    <div
      style={{
        position: "absolute",
        top: `${top}px`,
        height: `${height}px`,
        left: "2px",
        right: "2px",
        backgroundColor: event.color || "#3F51B5",
        color: "white",
        padding: "4px",
        borderRadius: "4px",
        zIndex: 1,
        overflow: "hidden",
        boxSizing: "border-box",
      }}
    >
      <div className="event-title" style={{ fontWeight: "bold" }}>
        {event.title}
      </div>
      <div className="event-time">
        {event.startTime} - {event.endTime}
      </div>
      {event.location && <div className="event-location">{event.location}</div>}
    </div>
  );
};

export default EventComponent;
