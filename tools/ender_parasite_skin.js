// Generates the 64x32 Ender Parasite skin plus a glow mask: a dark violet carapace with a swollen,
// luminous magenta abdomen and pale glowing eyes.
// Usage: node ender_parasite_skin.js <skin.png> <glow.png>
// Layout (matches EnderParasiteModel): thorax 4x3x4 @(0,0), head 3x2x3 @(0,8), mandible 1x1x2 @(0,14),
//   leg 1x5x1 @(0,18), abdomen 5x4x6 @(20,0).
const zlib = require('zlib');
const fs = require('fs');
const W = 64, H = 32;
function crc32(buf) { let c = ~0; for (let i = 0; i < buf.length; i++) { c ^= buf[i]; for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xEDB88320 & -(c & 1)); } return ~c >>> 0; }
function chunk(t, d) { const tb = Buffer.from(t, 'ascii'); const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(Buffer.concat([tb, d]))); return Buffer.concat([l, tb, d, c]); }
function encodePNG(px) {
  const raw = Buffer.alloc(H * (1 + W * 4));
  for (let y = 0; y < H; y++) { raw[y * (1 + W * 4)] = 0; px.copy(raw, y * (1 + W * 4) + 1, y * W * 4, (y + 1) * W * 4); }
  const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]);
}
function rng(a) { return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
const r = rng(555);
const px = Buffer.alloc(W * H * 4);
const glowPx = Buffer.alloc(W * H * 4);
const put = (buf, x, y, c) => { const i = (y * W + x) * 4; buf[i] = c[0]; buf[i + 1] = c[1]; buf[i + 2] = c[2]; buf[i + 3] = 255; };
const sac = [226, 76, 208], sacHot = [255, 150, 240], eye = [235, 215, 255];
const set = (x, y, c) => {
  if (x < 0 || y < 0 || x >= W || y >= H) return;
  put(px, x, y, c);
  if (c === sac || c === sacHot || c === eye) put(glowPx, x, y, c);
};
const clampc = (c) => c.map(v => Math.max(0, Math.min(255, v)));
function carapace(x, y) {
  const n = r();
  if (y % 3 === 0) return n < 0.5 ? [58, 38, 86] : [50, 32, 76]; // segment bands
  return n < 0.5 ? [30, 22, 46] : [38, 28, 58];
}
function box(u, v, w, h, d, fn) {
  const faces = { top: [u + d, v, w, d], bottom: [u + d + w, v, w, d], right: [u, v + d, d, h], front: [u + d, v + d, w, h], left: [u + d + w, v + d, d, h], back: [u + 2 * d + w, v + d, w, h] };
  for (const [name, [fx, fy, fw, fh]] of Object.entries(faces)) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
      const shade = name === 'bottom' ? -12 : (name === 'left' || name === 'right') ? -5 : 0;
      let c = clampc(carapace(fx + x, fy + y).map(v => v + shade));
      if (fn) c = fn(name, x, y, fw, fh, c) || c;
      set(fx + x, fy + y, c);
    }
  }
}
box(0, 0, 4, 3, 4, null);
box(0, 8, 3, 2, 3, (name, x, y) => (name === 'front' && y === 0 && (x === 0 || x === 2)) ? eye : null);
box(0, 14, 1, 1, 2, () => [150, 140, 160]);
box(0, 18, 1, 5, 1, (name, x, y) => (y === 4) ? [20, 14, 32] : null);
// Abdomen: a dark shell split open along the sides and underside by a pulsing sac.
box(20, 0, 5, 4, 6, (name, x, y, fw, fh) => {
  if (name === 'bottom') return (x + y) % 5 === 0 ? sacHot : sac;
  if (name === 'left' || name === 'right') return y >= 1 ? ((x + y) % 4 === 0 ? sacHot : sac) : null;
  if (name === 'back') return (y >= 2 && x >= 1 && x <= fw - 2) ? sac : null;
  if (name === 'top' && x === 2 && y % 2 === 0) return sac;
});

fs.writeFileSync(process.argv[2], encodePNG(px));
fs.writeFileSync(process.argv[3], encodePNG(glowPx));
