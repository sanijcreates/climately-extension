// utils.js
export const timeToPixels = (time, startHour = 0, pxPerHour = 60) => {
  const [hours, minutes] = time.split(":").map(Number);
  const totalMinutes = (hours - startHour) * 60 + minutes;
  return totalMinutes * (pxPerHour / 60);
};
