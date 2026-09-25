import { cp, copyFile, mkdir, rm } from "node:fs/promises";
import { dirname, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const projectRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const pagesRoot = resolve(projectRoot, "cloudflare-pages");
const contentRoot = resolve(pagesRoot, "content");
const outputRoot = resolve(pagesRoot, "site");
const staticRoot = resolve(projectRoot, "src/main/resources/static");

if (!outputRoot.startsWith(`${pagesRoot}${sep}`)) {
  throw new Error("The Pages output directory must stay inside cloudflare-pages.");
}

const sharedAssets = [
  { source: "css/styles.css", destination: "css/styles.css" },
  { source: "images/marcos-aurelio.jpeg", destination: "images/marcos-aurelio.jpeg" },
  { source: "js/about-tilt.js", destination: "js/about-tilt.js" },
  { source: "js/app.js", destination: "js/app.js" },
  { source: "js/currency-input.js", destination: "js/currency-input.js" },
  { source: "js/theme-init.js", destination: "js/theme-init.js" },
];

await rm(outputRoot, { recursive: true, force: true });
await mkdir(outputRoot, { recursive: true });
await cp(contentRoot, outputRoot, { recursive: true });

for (const asset of sharedAssets) {
  const source = resolve(staticRoot, asset.source);
  const destination = resolve(outputRoot, asset.destination);
  await mkdir(dirname(destination), { recursive: true });
  await copyFile(source, destination);
}

process.stdout.write("Cloudflare Pages files generated in cloudflare-pages/site.\n");
