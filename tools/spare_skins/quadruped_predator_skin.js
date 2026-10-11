// Generates the 64x32 Void Stalker skin: End Stone camouflage with void-purple cracks and glowing eyes.
const zlib = require('zlib');
const fs = require('fs');
const W = 64, H = 32;
function crc32(buf) { let c = ~0; for (let i = 0; i < buf.length; i++) { c ^= buf[i]; for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xEDB88320 & -(c & 1)); } return ~c >>> 0; }
function chunk(t, d) { const tb = Buffer.from(t, 'ascii'); const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(Buffer.concat([tb, d]))); return Buffer.concat([l, tb, d, c]); }
function rng(a) { return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
const r = rng(1337);
const px = Buffer.alloc(W * H * 4); // transparent
const set = (x, y, c) => { if (x < 0 || y < 0 || x >= W || y >= H) return; const i = (y * W + x) * 4; px[i] = c[0]; px[i + 1] = c[1]; px[i + 2] = c[2]; px[i + 3] = 255; };
const stone = (x, y) => {
  const n = r();
  let c = [216, 219, 158];
  if (n < 0.2) c = [196, 199, 138]; else if (n > 0.85) c = [232, 234, 176];
  if (r() < 0.07) c = [92, 40, 140]; // void speckle
  return c;
};
// cuboid unwrap: fills the net for (w,h,d) at (u,v), calling f(face,x,y) per pixel
function box(u, v, w, h, d, shadeFn) {
  const faces = { top: [u + d, v, w, d], bottom: [u + d + w, v, w, d], right: [u, v + d, d, h], front: [u + d, v + d, w, h], left: [u + d + w, v + d, d, h], back: [u + 2 * d + w, v + d, w, h] };
  for (const [name, [fx, fy, fw, fh]] of Object.entries(faces)) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
      let c = stone(fx + x, fy + y);
      const dark = name === 'bottom' ? -40 : (name === 'left' || name === 'right') ? -14 : 0;
      c = c.map(v => Math.max(0, Math.min(255, v + dark)));
      set(fx + x, fy + y, shadeFn ? shadeFn(name, x, y, fw, fh, c) : c);
    }
  }
}
const purple = [138, 43, 226], dark = [50, 20, 80];
box(0, 0, 5, 5, 5, (name, x, y, fw, fh, c) => {
  if (name === 'front') {
    if (y === 2 && (x === 0 || x === 4)) return [255, 80, 255]; // glowing eyes
    if (y === 3 && (x === 0 || x === 4)) return purple;
    if (y === 4 && x >= 1 && x <= 3) return dark; // maw
  }
  return c;
});
box(22, 0, 1, 2, 1, (n, x, y, fw, fh, c) => purple); // horns/spikes
box(0, 12, 6, 5, 12, (name, x, y, fw, fh, c) => {
  if (name === 'top' && (x + y * 3) % 7 === 0) return purple; // cracks along the back
  if (name === 'top' && (x * 2 + y) % 11 === 0) return dark;
  return c;
});
box(40, 0, 2, 6, 2, null); // legs
box(40, 10, 2, 2, 8, (name, x, y, fw, fh, c) => (name === 'top' && y % 3 === 0) ? purple : c);

const raw = Buffer.alloc(H * (1 + W * 4));
for (let y = 0; y < H; y++) { raw[y * (1 + W * 4)] = 0; px.copy(raw, y * (1 + W * 4) + 1, y * W * 4, (y + 1) * W * 4); }
const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
const png = Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]);
fs.writeFileSync(process.argv[2], png);
