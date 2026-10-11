// Generates the 64x64 Void Stalker skin: a gaunt figure of charred black and End Stone, split by
// void-purple fissures, with glowing eyes. Also emits a glow mask (only the purple / glowing pixels)
// that the renderer draws fullbright with a soft pulse.
// Usage: node void_stalker_skin.js <skin.png> <glow.png>
// Layout (matches VoidStalkerModel):
//   head 4x7x4 @(0,0)  lower torso 6x7x4 @(0,12)  upper torso 6x7x4 @(0,24)
//   upper arm 2x11x2 @(24,12)  forearm 2x11x2 @(24,26)  leg 2x16x2 @(36,12)
//   head spike 1x3x1 @(40,40)  claw 1x4x1 @(48,40)
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
const r = rng(666);
const px = Buffer.alloc(W * H * 4);
const glowPx = Buffer.alloc(W * H * 4);
const put = (buf, x, y, c) => { const i = (y * W + x) * 4; buf[i] = c[0]; buf[i + 1] = c[1]; buf[i + 2] = c[2]; buf[i + 3] = 255; };
const clampc = (c) => c.map(v => Math.max(0, Math.min(255, v)));
const purple = [138, 43, 226], deep = [50, 20, 80], glow = [255, 90, 255];
const black = [12, 10, 18], char = [30, 27, 38];
const set = (x, y, c) => {
  if (x < 0 || y < 0 || x >= W || y >= H) return;
  put(px, x, y, c);
  if (c === purple || c === glow) put(glowPx, x, y, c === glow ? glow : [190, 90, 255]);
};

// Blotchy low-frequency mask so black and stone read as patches, not noise.
const grid = []; for (let i = 0; i < 16; i++) { grid[i] = []; for (let j = 0; j < 16; j++) grid[i][j] = r(); }
function blotch(x, y) {
  const fx = x / 4, fy = y / 4, x0 = Math.floor(fx) & 15, y0 = Math.floor(fy) & 15, x1 = (x0 + 1) & 15, y1 = (y0 + 1) & 15;
  const tx = fx - Math.floor(fx), ty = fy - Math.floor(fy);
  const a = grid[x0][y0] + (grid[x1][y0] - grid[x0][y0]) * tx, b = grid[x0][y1] + (grid[x1][y1] - grid[x0][y1]) * tx;
  return a + (b - a) * ty;
}
// bias: 0..1, how much pale End Stone survives on this part (rest is char/black).
function surface(ax, ay, bias) {
  const m = blotch(ax, ay);
  const n = r();
  if (m > 1 - bias) {
    let c = [206, 209, 150];
    if (n < 0.2) c = [186, 189, 130]; else if (n > 0.85) c = [224, 226, 168];
    return c;
  }
  if (m > 0.4) return n < 0.5 ? char : [38, 34, 48];
  return n < 0.7 ? black : [20, 17, 28];
}
function box(u, v, w, h, d, bias, fn) {
  const faces = { top: [u + d, v, w, d], bottom: [u + d + w, v, w, d], right: [u, v + d, d, h], front: [u + d, v + d, w, h], left: [u + d + w, v + d, d, h], back: [u + 2 * d + w, v + d, w, h] };
  for (const [name, [fx, fy, fw, fh]] of Object.entries(faces)) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
      const dark = name === 'bottom' ? -14 : (name === 'left' || name === 'right') ? -6 : 0;
      let c = clampc(surface(fx + x, fy + y, bias).map(c => c + dark));
      if (r() < 0.04) c = purple;
      if (fn) c = fn(name, x, y, fw, fh, c) || c;
      set(fx + x, fy + y, c);
    }
  }
}
// Head: mostly black, a few bone-pale patches, glowing eyes, ragged slit mouth.
box(0, 0, 4, 7, 4, 0.25, (name, x, y) => {
  if (name === 'front') {
    if ((x === 0 || x === 3) && (y === 2 || y === 3)) return glow;
    if (y === 1 && (x === 0 || x === 3)) return black; // brow shadow
    if (y === 5) return (x === 0 || x === 3) ? deep : black;
    if (y === 6 && (x === 1 || x === 2)) return deep;
  }
  if (name === 'top') return black;
});
// Torso (two 7px halves; the model bends between them): a void seam runs down chest and spine.
function torso(v, yOffset, ribs) {
  box(0, v, 6, 7, 4, 0.35, (name, x, y) => {
    const gy = y + yOffset;
    if ((name === 'front' || name === 'back') && x === 2 && gy % 5 !== 4) return purple;
    if ((name === 'front' || name === 'back') && x === 3 && gy % 5 === 1) return deep;
    if (ribs && name === 'front' && y >= 1 && y <= 6 && (x === 0 || x === 5) && y % 2 === 1) return [60, 55, 70];
  });
}
torso(12, 7, false); // lower torso
torso(24, 0, true);  // upper torso
// Upper arm / forearm: darker toward the hands.
box(24, 12, 2, 11, 2, 0.3, (name) => (name === 'bottom') ? deep : null);
box(24, 26, 2, 11, 2, 0.1, (name, x, y, fw, fh) => {
  if (name === 'bottom') return deep;
  if (y > fh - 5 && (x + y) % 2 === 0) return purple;
});
box(36, 12, 2, 16, 2, 0.2, (name, x, y) => (name !== 'top' && name !== 'bottom' && y % 6 === 0) ? deep : null);
// Skull spikes and claws: obsidian with a purple tip.
box(40, 40, 1, 3, 1, 0, (name) => (name === 'top') ? purple : black);
box(48, 40, 1, 4, 1, 0, (name) => (name === 'bottom') ? purple : [8, 6, 12]);

fs.writeFileSync(process.argv[2], encodePNG(px));
fs.writeFileSync(process.argv[3], encodePNG(glowPx));
