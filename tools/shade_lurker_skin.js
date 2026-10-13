// Generates the 64x64 Shade Lurker skin plus a glow mask (eyes, faint veins, twig tips).
// Usage: node shade_lurker_skin.js <skin.png> <glow.png>
// Layout (matches ShadeLurkerModel): body 8x7x10 @(0,0), head 5x5x4 @(0,18), twig 1x5x1 @(0,30),
//   arm 3x16x3 @(36,0), leg 3x8x3 @(48,0).
const zlib = require('zlib');
const fs = require('fs');
const W = 64, H = 64;
function crc32(buf) { let c = ~0; for (let i = 0; i < buf.length; i++) { c ^= buf[i]; for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xEDB88320 & -(c & 1)); } return ~c >>> 0; }
function chunk(t, d) { const tb = Buffer.from(t, 'ascii'); const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(Buffer.concat([tb, d]))); return Buffer.concat([l, tb, d, c]); }
function encodePNG(px) {
  const raw = Buffer.alloc(H * (1 + W * 4));
  for (let y = 0; y < H; y++) { raw[y * (1 + W * 4)] = 0; px.copy(raw, y * (1 + W * 4) + 1, y * W * 4, (y + 1) * W * 4); }
  const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]);
}
function rng(a) { return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
const r = rng(31337);
const px = Buffer.alloc(W * H * 4);
const glowPx = Buffer.alloc(W * H * 4);
const put = (buf, x, y, c) => { const i = (y * W + x) * 4; buf[i] = c[0]; buf[i + 1] = c[1]; buf[i + 2] = c[2]; buf[i + 3] = 255; };
const eye = [150, 255, 205], vein = [70, 190, 150];
const set = (x, y, c) => {
  if (x < 0 || y < 0 || x >= W || y >= H) return;
  put(px, x, y, c);
  if (c === eye || c === vein) put(glowPx, x, y, c);
};
const grid = []; for (let i = 0; i < 16; i++) { grid[i] = []; for (let j = 0; j < 16; j++) grid[i][j] = r(); }
function blotch(x, y) {
  const fx = x / 4, fy = y / 4, x0 = Math.floor(fx) & 15, y0 = Math.floor(fy) & 15, x1 = (x0 + 1) & 15, y1 = (y0 + 1) & 15;
  const tx = fx - Math.floor(fx), ty = fy - Math.floor(fy);
  const a = grid[x0][y0] + (grid[x1][y0] - grid[x0][y0]) * tx, b = grid[x0][y1] + (grid[x1][y1] - grid[x0][y1]) * tx;
  return a + (b - a) * ty;
}
// Deep blue-black hide with mossy green patches and bark-dark streaks.
function hide(x, y) {
  const m = blotch(x, y), n = r();
  if (m > 0.68) return n < 0.5 ? [26, 54, 46] : [32, 64, 52];   // moss
  if (m < 0.28) return n < 0.5 ? [8, 8, 16] : [12, 12, 22];      // near-black
  return n < 0.5 ? [18, 20, 34] : [22, 24, 40];                  // blue-black
}
const clampc = (c) => c.map(v => Math.max(0, Math.min(255, v)));
function box(u, v, w, h, d, fn) {
  const faces = { top: [u + d, v, w, d], bottom: [u + d + w, v, w, d], right: [u, v + d, d, h], front: [u + d, v + d, w, h], left: [u + d + w, v + d, d, h], back: [u + 2 * d + w, v + d, w, h] };
  for (const [name, [fx, fy, fw, fh]] of Object.entries(faces)) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
      const dark = name === 'bottom' ? -8 : (name === 'left' || name === 'right') ? -3 : 0;
      let c = clampc(hide(fx + x, fy + y).map(c => c + dark));
      if (fn) c = fn(name, x, y, fw, fh, c) || c;
      set(fx + x, fy + y, c);
    }
  }
}
// Body: sparse glowing veins along the back.
box(0, 0, 8, 7, 10, (name, x, y) => (name === 'top' && (x === 3 || x === 4) && y % 3 === 0) ? vein : null);
// Head: two glowing eye slits, dark mouth line.
box(0, 18, 5, 5, 4, (name, x, y) => {
  if (name === 'front') {
    if (y === 2 && (x === 0 || x === 1 || x === 3 || x === 4)) return eye;
    if (y === 4 && x >= 1 && x <= 3) return [4, 4, 8];
  }
});
box(0, 30, 1, 5, 1, (name, x, y) => (name === 'top') ? vein : [34, 28, 24]);
box(36, 0, 3, 16, 3, (name, x, y, fw, fh) => (y > fh - 3 && name !== 'top') ? [10, 10, 16] : null);
box(48, 0, 3, 8, 3, null);

fs.writeFileSync(process.argv[2], encodePNG(px));
fs.writeFileSync(process.argv[3], encodePNG(glowPx));
