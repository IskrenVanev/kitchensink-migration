import path from "node:path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  root: "frontend",
  plugins: [react()],
  server: {
    proxy: {
      "/rest": "http://localhost:8081",
      "/css": "http://localhost:8081",
      "/gfx": "http://localhost:8081"
    }
  },
  build: {
    outDir: path.resolve(__dirname, "src/main/resources/static/js"),
    emptyOutDir: false,
    rollupOptions: {
      input: path.resolve(__dirname, "frontend/src/main.jsx"),
      output: {
        entryFileNames: "app.js",
        chunkFileNames: "[name]-[hash].js",
        assetFileNames: "[name]-[hash][extname]"
      }
    }
  }
});
