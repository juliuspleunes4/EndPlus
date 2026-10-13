// Generates the 128x128 Ancient End Golem skin plus a glow mask: weathered violet-slate stone,
// mossy in the creases, carved with golden runes that the renderer lights up.
// Usage: node ancient_end_golem_skin.js <skin.png> <glow.png>
// Layout (matches AncientEndGolemModel): head 8x8x8 @(0,0), torso 18x16x10 @(0,18), arm 6x26x6 @(0,46),
//   leg 6x16x6 @(26,46), rune pillar 4x12x3 @(52,46), floating shard 3x3x3 @(70,46).
const zlib = require('zlib');
const fs = require('fs');
const W = 128, H = 128;
function crc32(buf) { let c = ~0; for (let i = 0; i < buf.length; i++) { c ^= buf[i]; for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xEDB88320 & -(c & 1)); } return ~c >>> 0; }
function chunk(t, d) { const tb = Buffer.from(t, 'ascii'); const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(Buffer.concat([tb, d]))); return Buffer.concat([l, tb, d, c]); }
function encodePNG(px) {
  const raw = Buffer.alloc(H * (1 + W * 4));
  for (let y = 0; y < H; y++) { raw[y * (1 + W * 4)] = 0; px.copy(raw, y * (1 + W * 4) + 1, y * W * 4, (y + 1) * W * 4); }
  const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]);
}
function rng(a) { return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
const r = rng(7777);
const px = Buffer.alloc(W * H * 4);
const glowPx = Buffer.alloc(W * H * 4);
const put = (buf, x, y, c) => { const i = (y * W + x) * 4; buf[i] = c[0]; buf[i + 1] = c[1]; buf[i + 2] = c[2]; buf[i + 3] = 255; };
const rune = [255, 214, 120], runeHot = [255, 244, 190];
const set = (x, y, c) => {
  if (x < 0 || y < 0 || x >= W || y >= H) return;
  put(px, x, y, c);
  if (c === rune || c === runeHot) put(glowPx, x, y, c);
};
const clampc = (c) => c.map(v => Math.max(0, Math.min(255, v)));

const grid = []; for (let i = 0; i < 32; i++) { grid[i] = []; for (let j = 0; j < 32; j++) grid[i][j] = r(); }
function blotch(x, y) {
  const fx = x / 5, fy = y / 5, x0 = Math.floor(fx) & 31, y0 = Math.floor(fy) & 31, x1 = (x0 + 1) & 31, y1 = (y0 + 1) & 31;
  const tx = fx - Math.floor(fx), ty = fy - Math.floor(fy);
  const a = grid[x0][y0] + (grid[x1][y0] - grid[x0][y0]) * tx, b = grid[x0][y1] + (grid[x1][y1] - grid[x0][y1]) * tx;
  return a + (b - a) * ty;
}
// Violet-slate stone, lighter weathered patches, dark crevices and a little moss.
function stone(x, y) {
  const m = blotch(x, y), n = r();
  if (m > 0.78) return n < 0.5 ? [70, 96, 78] : [60, 84, 68];            // moss
  if (m < 0.22) return n < 0.5 ? [44, 40, 58] : [52, 48, 68];            // dark crevice
  if (m > 0.6) return n < 0.5 ? [112, 106, 130] : [122, 116, 140];       // bleached
  return n < 0.5 ? [84, 78, 104] : [92, 86, 112];
}
function box(u, v, w, h, d, fn) {
  const faces = { top: [u + d, v, w, d], bottom: [u + d + w, v, w, d], right: [u, v + d, d, h], front: [u + d, v + d, w, h], left: [u + d + w, v + d, d, h], back: [u + 2 * d + w, v + d, w, h] };
  for (const [name, [fx, fy, fw, fh]] of Object.entries(faces)) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
      const shade = name === 'bottom' ? -26 : (name === 'left' || name === 'right') ? -10 : 0;
      let c = clampc(stone(fx + x, fy + y).map(v => v + shade));
      // chipped edge lines between blocks of stone
      if ((y % 8 === 7 && r() < 0.6) || (x % 9 === 8 && r() < 0.35)) c = clampc(c.map(v => v - 22));
      if (fn) c = fn(name, x, y, fw, fh, c) || c;
      set(fx + x, fy + y, c);
    }
  }
}
// Head: a carved face, two glowing eye runes and a brow glyph.
box(0, 0, 8, 8, 8, (name, x, y) => {
  if (name === 'front') {
    if (y === 3 && (x === 1 || x === 2 || x === 5 || x === 6)) return runeHot;
    if (y === 1 && x >= 2 && x <= 5) return rune;
    if (y === 5 && (x === 3 || x === 4)) return [30, 26, 44];
  }
  if (name === 'top' && x === 3 && y >= 2 && y <= 5) return rune;
});
// Torso: a glowing glyph column with a diamond, repeated on the back.
const torsoRunes = (name, x, y, fw, fh) => {
  if (name !== 'front' && name !== 'back') return null;
  const cx = fw >> 1;
  if ((x === cx - 1 || x === cx) && (y < 4 || (y > 10 && y < 14))) return rune;      // spine bar
  const dist = Math.abs(x - cx + 0.5) + Math.abs(y - 7);
  if (Math.abs(dist - 3) < 0.6) return rune;                                          // diamond outline
  if ((x === cx - 1 || x === cx) && y >= 6 && y <= 8) return runeHot;                // core
  if (y === 12 && (x === 3 || x === fw - 4)) return rune;                             // side glyph ticks
  if (y === 2 && (x === 4 || x === fw - 5)) return rune;
};
box(0, 18, 18, 16, 10, torsoRunes);
// Arms: rune bands at the elbow and wrist.
box(0, 46, 6, 26, 6, (name, x, y) => {
  if (name === 'top' || name === 'bottom') return null;
  if (y === 9 && (x % 2 === 0)) return rune;
  if (y === 20 && (x % 3 === 0)) return rune;
});
// Legs: a single knee glyph.
box(26, 46, 6, 16, 6, (name, x, y) => (name === 'front' && y === 5 && (x === 2 || x === 3)) ? rune : null);
// Back pillar: banded with runes.
box(52, 46, 4, 12, 3, (name, x, y) => (name !== 'top' && name !== 'bottom' && y % 3 === 1) ? rune : null);
// Shards: pure light.
box(70, 46, 3, 3, 3, () => runeHot);

fs.writeFileSync(process.argv[2], encodePNG(px));
fs.writeFileSync(process.argv[3], encodePNG(glowPx));
