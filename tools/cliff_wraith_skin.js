// Generates the 128x64 Cliff Wraith skin: bone-pale skull, ash-grey feathered body, Ambopteryx-style
// wings (bone arm + rod, torn leathery membrane), cold glowing eyes.
// Usage: node cliff_wraith_skin.js <out.png>
// Layout (matches CliffWraithModel): body 4x4x9 @(0,0), head 4x4x4 @(0,14), beak 2x1x3 @(18,14),
//   bones: upper arm 12x3x3 @(0,24), forearm 15x3x3 @(0,30), wrist 5x3x3 @(0,36), rod 36x2x2 @(0,42),
//   membrane patch 52x11 @(0,50), tail 2x1x9 @(64,0), talon 1x3x1 @(40,0).
const zlib = require('zlib');
const fs = require('fs');
const W = 128, H = 64;
function crc32(buf) { let c = ~0; for (let i = 0; i < buf.length; i++) { c ^= buf[i]; for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xEDB88320 & -(c & 1)); } return ~c >>> 0; }
function chunk(t, d) { const tb = Buffer.from(t, 'ascii'); const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(Buffer.concat([tb, d]))); return Buffer.concat([l, tb, d, c]); }
function rng(a) { return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
const r = rng(909);
const px = Buffer.alloc(W * H * 4);
const set = (x, y, c) => { if (x < 0 || y < 0 || x >= W || y >= H) return; const i = (y * W + x) * 4; px[i] = c[0]; px[i + 1] = c[1]; px[i + 2] = c[2]; px[i + 3] = 255; };
const clampc = (c) => c.map(v => Math.max(0, Math.min(255, v)));
const bone = [214, 212, 198], boneDark = [168, 166, 152], ash = [122, 130, 150], ashDark = [84, 92, 112], ashLight = [160, 168, 186], black = [24, 26, 36], eye = [120, 230, 255];
function box(u, v, w, h, d, base, fn) {
  const faces = { top: [u + d, v, w, d], bottom: [u + d + w, v, w, d], right: [u, v + d, d, h], front: [u + d, v + d, w, h], left: [u + d + w, v + d, d, h], back: [u + 2 * d + w, v + d, w, h] };
  for (const [name, [fx, fy, fw, fh]] of Object.entries(faces)) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
      const shade = name === 'bottom' ? -34 : (name === 'left' || name === 'right') ? -14 : 0;
      let c = clampc(base(name, x, y, fw, fh).map(c => c + shade + ((r() * 14) | 0) - 7));
      if (fn) c = fn(name, x, y, fw, fh, c) || c;
      set(fx + x, fy + y, c);
    }
  }
}
// Body: grey with lighter feather rows.
box(0, 0, 4, 4, 9, (n, x, y) => y % 3 === 0 ? ashLight : ash);
// Head: bare skull, glowing eyes, dark beak line.
box(0, 14, 4, 4, 4, () => bone, (name, x, y) => {
  if (name === 'front') {
    if (y === 1 && (x === 0 || x === 3)) return eye;
    if (y === 3 && (x === 1 || x === 2)) return boneDark;
  }
});
box(18, 14, 2, 1, 3, () => boneDark, null);
// Wing bones: pale bone with darker joint bands and a worn, bleached top edge.
const joint = (x, period) => x % period === period - 1;
function boneBox(u, v, len, thick) {
  box(u, v, len, thick, thick, () => bone, (name, x, y, fw, fh, c) => {
    if (joint(x, 6)) return boneDark;
    if (name === 'top' && x % 6 === 2) return [232, 230, 216];
    if (r() < 0.05) return [150, 148, 134]; // pitting
  });
}
boneBox(0, 24, 12, 3);  // upper arm
boneBox(0, 30, 15, 3);  // forearm
boneBox(0, 36, 5, 3);   // wrist (the wrist claw samples the same strip)
boneBox(0, 42, 36, 2);  // styliform rod
// Membrane patch: every panel samples this region. Dark leathery skin with pale veins, a lighter
// strip along the leading edge, and increasingly ragged, torn holes (alpha 0) towards the trailing edge.
const membrane = [[58, 50, 72], [66, 56, 82], [50, 44, 64]];
for (let y = 0; y < 11; y++) {
  for (let x = 0; x < 52; x++) {
    const tear = y >= 2 ? 0.05 + 0.04 * (y - 2) : 0;
    if (r() < tear) continue; // hole: stays transparent
    let c = membrane[(r() * 3) | 0];
    if (y % 3 === 1) c = [96, 86, 116];               // veins running along the span
    if (y < 1) c = [150, 146, 160];                   // thickened leading edge
    if (x % 9 === 4 && y > 1) c = [88, 78, 108];      // cross-veins
    set(x, 50 + y, c);
  }
}
box(64, 0, 2, 1, 9, (n, x, y) => (y % 2 === 0 ? ashDark : ash), (n, x, y, fw, fh) => (y >= fh - 3 ? black : null));
box(40, 0, 1, 3, 1, () => boneDark, null);

const raw = Buffer.alloc(H * (1 + W * 4));
for (let y = 0; y < H; y++) { raw[y * (1 + W * 4)] = 0; px.copy(raw, y * (1 + W * 4) + 1, y * W * 4, (y + 1) * W * 4); }
const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
fs.writeFileSync(process.argv[2], Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]));
