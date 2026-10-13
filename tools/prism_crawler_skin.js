// Generates the 64x32 Prism Crawler skin using the vanilla spider model's UV layout:
// head 8x8x8 @(32,4), neck 6x6x6 @(0,0), body 10x8x12 @(0,12), legs 16x2x2 @(18,0).
// Faceted purple/violet crystal plates with white-hot glints and glowing eyes.
// Usage: node prism_crawler_skin.js <out.png>
const zlib = require('zlib');
const fs = require('fs');
const W = 64, H = 32;
function crc32(buf) { let c = ~0; for (let i = 0; i < buf.length; i++) { c ^= buf[i]; for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xEDB88320 & -(c & 1)); } return ~c >>> 0; }
function chunk(t, d) { const tb = Buffer.from(t, 'ascii'); const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(Buffer.concat([tb, d]))); return Buffer.concat([l, tb, d, c]); }
function rng(a) { return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
const r = rng(2024);
const px = Buffer.alloc(W * H * 4);
const set = (x, y, c) => { if (x < 0 || y < 0 || x >= W || y >= H) return; const i = (y * W + x) * 4; px[i] = c[0]; px[i + 1] = c[1]; px[i + 2] = c[2]; px[i + 3] = 255; };
const clampc = (c) => c.map(v => Math.max(0, Math.min(255, v)));
const cyan = [150, 100, 220], pale = [212, 178, 246], violet = [96, 52, 170], deep = [46, 24, 96], white = [250, 238, 255], eye = [255, 120, 255];
function crystal(x, y) {
  // diagonal facet bands give a cut-gem look
  const band = ((x + y * 2) >> 1) % 4;
  let c = band === 0 ? pale : band === 1 ? cyan : band === 2 ? [124, 78, 196] : violet;
  if (r() < 0.08) c = white;
  else if (r() < 0.06) c = deep;
  return c;
}
function box(u, v, w, h, d, fn) {
  const faces = { top: [u + d, v, w, d], bottom: [u + d + w, v, w, d], right: [u, v + d, d, h], front: [u + d, v + d, w, h], left: [u + d + w, v + d, d, h], back: [u + 2 * d + w, v + d, w, h] };
  for (const [name, [fx, fy, fw, fh]] of Object.entries(faces)) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
      const dark = name === 'bottom' ? -45 : (name === 'left' || name === 'right') ? -18 : 0;
      let c = clampc(crystal(fx + x, fy + y).map(v => v + dark));
      if (fn) c = fn(name, x, y, fw, fh, c) || c;
      set(fx + x, fy + y, c);
    }
  }
}
box(32, 4, 8, 8, 8, (name, x, y) => {
  if (name === 'front') {
    if ((x === 1 || x === 6) && y === 3) return eye;
    if ((x === 2 || x === 5) && y === 5) return eye;
    if ((x === 3 || x === 4) && y === 4) return deep;
  }
});
box(0, 0, 6, 6, 6, null);
box(0, 12, 10, 8, 12, (name, x, y) => (name === 'top' && (x === 4 || x === 5) && y % 3 !== 2) ? violet : null);
box(18, 0, 16, 2, 2, (name, x, y, fw) => (name === 'top' && x % 5 === 0) ? deep : null);

const raw = Buffer.alloc(H * (1 + W * 4));
for (let y = 0; y < H; y++) { raw[y * (1 + W * 4)] = 0; px.copy(raw, y * (1 + W * 4) + 1, y * W * 4, (y + 1) * W * 4); }
const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
fs.writeFileSync(process.argv[2], Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]));
