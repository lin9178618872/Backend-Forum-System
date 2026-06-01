import http from "node:http";
import { readFile } from "node:fs/promises";
import { createReadStream, existsSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const port = Number(process.env.FRONTEND_PORT || 5173);
const apiTarget = process.env.API_TARGET || "http://localhost:18080";

const contentTypes = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml"
};

function proxyApi(req, res) {
  const target = new URL(req.url, apiTarget);
  const proxyReq = http.request(
    target,
    {
      method: req.method,
      headers: {
        ...req.headers,
        host: target.host
      }
    },
    proxyRes => {
      res.writeHead(proxyRes.statusCode || 500, proxyRes.headers);
      proxyRes.pipe(res);
    }
  );

  proxyReq.on("error", () => {
    res.writeHead(502, { "content-type": "application/json" });
    res.end(JSON.stringify({ message: `Cannot reach backend at ${apiTarget}` }));
  });

  req.pipe(proxyReq);
}

const server = http.createServer(async (req, res) => {
  if (req.url?.startsWith("/api/")) {
    proxyApi(req, res);
    return;
  }

  const requested = decodeURIComponent(new URL(req.url || "/", `http://localhost:${port}`).pathname);
  const filePath = requested === "/" ? path.join(__dirname, "index.html") : path.join(__dirname, requested);
  const safePath = filePath.startsWith(__dirname) ? filePath : path.join(__dirname, "index.html");
  const finalPath = existsSync(safePath) ? safePath : path.join(__dirname, "index.html");
  const ext = path.extname(finalPath);

  try {
    res.writeHead(200, { "content-type": contentTypes[ext] || "application/octet-stream" });
    createReadStream(finalPath).pipe(res);
  } catch {
    res.writeHead(500, { "content-type": "text/plain; charset=utf-8" });
    res.end("Failed to load frontend asset.");
  }
});

server.listen(port, async () => {
  const index = await readFile(path.join(__dirname, "index.html"), "utf8");
  if (!index.includes("Forum Console")) {
    console.warn("Frontend index loaded, but title marker was not found.");
  }
  console.log(`Forum frontend: http://localhost:${port}`);
  console.log(`Proxying /api to: ${apiTarget}`);
});
