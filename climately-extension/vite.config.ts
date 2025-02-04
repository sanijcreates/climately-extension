import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      input: "src/content.jsx", // Your entry file
      output: {
        format: "es", // Output ES module format
        dir: "dist", // Output directory
        entryFileNames: "content.js", // Custom output JS filename
      },
    },
  },
});
