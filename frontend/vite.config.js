import { defineConfig } from "vite";
const proxy = {
  "/api": { target: "http://127.0.0.1:8080", changeOrigin: false },
};
export default defineConfig({
  server: { port: 5173, strictPort: true, proxy },
  preview: { port: 4173, strictPort: true, proxy },
});
