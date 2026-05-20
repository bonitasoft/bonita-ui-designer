/* globals require, module, process, __dirname */
const { execSync } = require('child_process');
const https = require('https');
const fs = require('fs');
const path = require('path');

const OUT_DIR = path.resolve(__dirname, '..', 'node_modules', '.chromedriver');
const BIN_NAME = process.platform === 'win32' ? 'chromedriver.exe' : 'chromedriver';
const BIN_PATH = path.join(OUT_DIR, BIN_NAME);

function detectChromeVersion() {
  const candidates = process.platform === 'darwin'
    ? ['"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --version']
    : process.platform === 'win32'
      ? ['reg query "HKLM\\Software\\Google\\Chrome\\BLBeacon" /v version']
      : ['google-chrome --version', 'google-chrome-stable --version', 'chromium --version', 'chromium-browser --version'];

  for (const cmd of candidates) {
    try {
      const out = execSync(cmd, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] });
      const m = out.match(/(\d+)\.(\d+)\.(\d+)\.(\d+)/);
      if (m) return `${m[1]}.${m[2]}.${m[3]}.${m[4]}`;
    } catch (_) { /* try next candidate */ }
  }
  throw new Error('Unable to detect installed Chrome version. Install Google Chrome before running e2e tests.');
}

function platformSlug() {
  if (process.platform === 'darwin') return process.arch === 'arm64' ? 'mac-arm64' : 'mac-x64';
  if (process.platform === 'win32') return 'win64';
  return 'linux64';
}

function httpGet(url) {
  return new Promise((resolve, reject) => {
    const req = https.get(url, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        res.resume();
        return httpGet(res.headers.location).then(resolve, reject);
      }
      if (res.statusCode !== 200) {
        res.resume();
        return reject(new Error('GET ' + url + ' -> HTTP ' + res.statusCode));
      }
      resolve(res);
    });
    req.on('error', reject);
  });
}

function fetchText(url) {
  return httpGet(url).then((res) => new Promise((resolve, reject) => {
    let body = '';
    res.setEncoding('utf8');
    res.on('data', (chunk) => body += chunk);
    res.on('end', () => resolve(body));
    res.on('error', reject);
  }));
}

function downloadTo(url, dest) {
  return httpGet(url).then((res) => new Promise((resolve, reject) => {
    const file = fs.createWriteStream(dest);
    res.pipe(file);
    file.on('finish', () => file.close((err) => err ? reject(err) : resolve()));
    file.on('error', (err) => { fs.unlink(dest, () => reject(err)); });
  }));
}

function extractZip(zipPath, destDir) {
  if (process.platform === 'win32') {
    execSync(`powershell -NoProfile -Command "Expand-Archive -Force -LiteralPath '${zipPath}' -DestinationPath '${destDir}'"`, { stdio: 'inherit' });
  } else {
    execSync(`unzip -o "${zipPath}" -d "${destDir}"`, { stdio: 'inherit' });
  }
}

async function main() {
  const chromeVersion = detectChromeVersion();
  const major = chromeVersion.split('.')[0];
  console.log(`Detected Chrome ${chromeVersion} (major ${major})`);

  const driverVersion = (await fetchText(`https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_${major}`)).trim();
  console.log(`Resolved ChromeDriver ${driverVersion}`);

  const slug = platformSlug();
  const url = `https://storage.googleapis.com/chrome-for-testing-public/${driverVersion}/${slug}/chromedriver-${slug}.zip`;

  fs.mkdirSync(OUT_DIR, { recursive: true });
  const zipPath = path.join(OUT_DIR, 'chromedriver.zip');
  console.log(`Downloading ${url}`);
  await downloadTo(url, zipPath);

  extractZip(zipPath, OUT_DIR);
  fs.unlinkSync(zipPath);

  const binSrc = path.join(OUT_DIR, `chromedriver-${slug}`, BIN_NAME);
  if (fs.existsSync(BIN_PATH)) fs.unlinkSync(BIN_PATH);
  fs.renameSync(binSrc, BIN_PATH);
  if (process.platform !== 'win32') fs.chmodSync(BIN_PATH, 0o755);
  console.log(`Installed chromedriver at ${BIN_PATH}`);
}

if (require.main === module) {
  main().catch((err) => { console.error(err.message || err); process.exit(1); });
}

module.exports = { BIN_PATH };
