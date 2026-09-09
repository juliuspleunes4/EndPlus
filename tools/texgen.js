// Procedural texture generator for End+. No deps (Node zlib only).
// Usage: node texgen.js <out16Dir> [<zoomDir>]
//   emits <out16Dir>/{block,item}/<name>.png  (real 16x16)
//   and if zoomDir given, <zoomDir>/<name>_x16.png (256x256 preview)
const zlib = require('zlib');
const fs = require('fs');
const path = require('path');

const S = 16;

/* ============================ PNG ============================ */
function crc32(buf) {
  let c = ~0;
  for (let i = 0; i < buf.length; i++) { c ^= buf[i]; for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xEDB88320 & -(c & 1)); }
  return ~c >>> 0;
}
function chunk(type, data) {
  const t = Buffer.from(type, 'ascii');
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length);
  const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(Buffer.concat([t, data])));
  return Buffer.concat([len, t, data, crc]);
}
function encodePNG(px, w, h) {
  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0); ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8; ihdr[9] = 6;
  const raw = Buffer.alloc(h * (1 + w * 4));
  for (let y = 0; y < h; y++) { raw[y * (1 + w * 4)] = 0; px.copy(raw, y * (1 + w * 4) + 1, y * w * 4, (y + 1) * w * 4); }
  return Buffer.concat([sig, chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]);
}

/* ========================== helpers ========================= */
function mulberry32(a) {
  return function () {
    a |= 0; a = a + 0x6D2B79F5 | 0;
    let t = Math.imul(a ^ a >>> 15, 1 | a);
    t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t;
    return ((t ^ t >>> 14) >>> 0) / 4294967296;
  };
}
const hx = (s) => [parseInt(s.slice(0, 2), 16), parseInt(s.slice(2, 4), 16), parseInt(s.slice(4, 6), 16)];
const clamp = (v) => v < 0 ? 0 : v > 255 ? 255 : v | 0;
const mix = (a, b, t) => [clamp(a[0] + (b[0] - a[0]) * t), clamp(a[1] + (b[1] - a[1]) * t), clamp(a[2] + (b[2] - a[2]) * t)];
const shade = (c, amt) => [clamp(c[0] + amt), clamp(c[1] + amt), clamp(c[2] + amt)];

function tileNoise(rng, cells) {
  const g = [];
  for (let i = 0; i < cells; i++) { g[i] = []; for (let j = 0; j < cells; j++) g[i][j] = rng(); }
  return (x, y) => {
    const fx = x / S * cells, fy = y / S * cells;
    const x0 = (Math.floor(fx) % cells + cells) % cells, y0 = (Math.floor(fy) % cells + cells) % cells;
    const x1 = (x0 + 1) % cells, y1 = (y0 + 1) % cells;
    const tx = fx - Math.floor(fx), ty = fy - Math.floor(fy);
    const sx = tx * tx * (3 - 2 * tx), sy = ty * ty * (3 - 2 * ty);
    const top = g[x0][y0] + (g[x1][y0] - g[x0][y0]) * sx;
    const bot = g[x0][y1] + (g[x1][y1] - g[x0][y1]) * sx;
    return top + (bot - top) * sy;
  };
}
// crack network, wrapped for tiling; returns distance-field fn
function crackField(rng, count, seglen) {
  const cracks = [];
  for (let k = 0; k < count; k++) {
    let cx = rng() * S, cy = rng() * S, ang = rng() * Math.PI * 2;
    const pts = [[cx, cy]]; const len = seglen[0] + (rng() * (seglen[1] - seglen[0]) | 0);
    for (let s = 0; s < len; s++) { ang += (rng() - 0.5) * 1.2; cx += Math.cos(ang) * 1.6; cy += Math.sin(ang) * 1.6; pts.push([cx, cy]); }
    cracks.push(pts);
  }
  const distSeg = (px, py, a, b) => {
    const dx = b[0] - a[0], dy = b[1] - a[1];
    const t = Math.max(0, Math.min(1, ((px - a[0]) * dx + (py - a[1]) * dy) / (dx * dx + dy * dy || 1)));
    return Math.hypot(px - (a[0] + dx * t), py - (a[1] + dy * t));
  };
  return (x, y) => {
    let cd = 9;
    for (const p of cracks) for (let i = 0; i < p.length - 1; i++)
      for (const ox of [-S, 0, S]) for (const oy of [-S, 0, S])
        cd = Math.min(cd, distSeg(x + 0.5, y + 0.5, [p[i][0] + ox, p[i][1] + oy], [p[i + 1][0] + ox, p[i + 1][1] + oy]));
    return cd;
  };
}
function make(fn) {
  const px = Buffer.alloc(S * S * 4);
  for (let y = 0; y < S; y++) for (let x = 0; x < S; x++) {
    const r = fn(x, y);
    const i = (y * S + x) * 4;
    if (!r) { px[i + 3] = 0; continue; }
    px[i] = clamp(r[0]); px[i + 1] = clamp(r[1]); px[i + 2] = clamp(r[2]);
    px[i + 3] = r[3] === undefined ? 255 : clamp(r[3]);
  }
  return px;
}

/* ===================== shared materials ===================== */
const PAL = {
  endLo: hx('cdce9f'), endHi: hx('e6e8bb'), endGrain: hx('b9ba86'),
  voidBlack: hx('0b0b12'), voidBase: hx('201a2b'), voidLight: hx('2c2440'),
  oreDark: hx('34145c'), oreMid: hx('6a29a0'), oreLit: hx('9a4fd6'), oreGlow: hx('c88ae8'),
  endriteDark: hx('173d33'), endriteMid: hx('2f7a5f'), endriteLit: hx('56d9ac'), endriteGlow: hx('9af0d4'),
  crystalDark: hx('3f8f96'), crystalMid: hx('8fe6dc'), crystalLit: hx('eafcf8'),
  barkDark: hx('241a2e'), barkMid: hx('42324f'), barkHeart: hx('3d1a6b'),
  stripDark: hx('43354c'), stripMid: hx('5b4a68'), stripLite: hx('76627f'),
  lilac: hx('c77dff'), lilacDim: hx('7a3fb0'),
};
function endStone(rng) {
  const n1 = tileNoise(rng, 8), n2 = tileNoise(rng, 4);
  return (x, y) => {
    const v = n1(x, y) * 0.55 + n2(x, y) * 0.45;
    return mix(mix(PAL.endLo, PAL.endHi, v), PAL.endGrain, (x * 7 + y * 13) % 3 === 0 ? 0.18 : 0);
  };
}
// vertical wood grain, tiling on Y. Streaks are the dominant feature.
function woodSide(rng, dark, mid, lite, streaks) {
  const nfine = tileNoise(rng, 8);
  // each streak: base column, per-row wobble noise, width, tone (-1 dark .. +1 light)
  const lines = [];
  for (let k = 0; k < streaks; k++) lines.push([rng() * S, tileNoise(rng, 4), 0.5 + rng() * 1.4, rng() * 2 - 1]);
  return (x, y) => {
    let c = mix(dark, mid, 0.35 + nfine(x, y) * 0.3);
    for (const [lx, wob, lw, tone] of lines) {
      const col = lx + (wob(0, y) - 0.5) * 2.2;
      let dx = Math.abs(x + 0.5 - col); dx = Math.min(dx, S - dx);
      if (dx < lw) {
        const k = (1 - dx / lw) * 0.8;
        c = mix(c, tone < 0 ? dark : lite, k * Math.abs(tone));
      }
    }
    if ((y % 8) === 0) c = shade(c, -12);
    return c;
  };
}
function woodTop(rng, dark, mid, lite, core) {
  const n = tileNoise(rng, 6);
  return (x, y) => {
    const d = Math.hypot(x + 0.5 - 8, y + 0.5 - 8);
    const ring = Math.sin(d * 1.7 + n(x, y) * 1.5) * 0.5 + 0.5;
    let c = mix(mid, dark, ring * 0.6);
    if (d < 2.2) c = mix(c, core, 0.5 - d * 0.15);
    if (ring > 0.85) c = mix(c, lite, 0.3);
    return c;
  };
}

/* ========================= TEXTURES ========================= */
const BLOCK = {};
const ITEM = {};

/* ---- void stone family ---- */
BLOCK.void_stone = () => {
  const rng = mulberry32(7);
  const n1 = tileNoise(rng, 8), n2 = tileNoise(rng, 4);
  const specks = []; for (let k = 0; k < 10; k++) specks.push([rng() * S | 0, rng() * S | 0, rng()]);
  return make((x, y) => {
    const v = n1(x, y) * 0.6 + n2(x, y) * 0.4;
    let c = v > 0.62 ? mix(PAL.voidBase, PAL.voidLight, (v - 0.62) / 0.38) : mix(PAL.voidBlack, PAL.voidBase, v / 0.62);
    for (const [sx, sy, b] of specks) if (sx === x && sy === y) c = mix(c, b > 0.5 ? PAL.lilac : PAL.lilacDim, 0.4 + b * 0.3);
    return [...c, 255];
  });
};
BLOCK.void_stone_bricks = () => {
  const rng = mulberry32(19);
  const n = tileNoise(rng, 8);
  const brick = hx('2a2338'), brickHi = hx('3d3352'), brickLo = hx('1a1526'), mortar = hx('0a0810');
  const tone = []; for (let r = 0; r < 4; r++) { tone[r] = []; for (let cc = 0; cc < 4; cc++) tone[r][cc] = rng(); }
  return make((x, y) => {
    const row = Math.floor(y / 4);
    const off = (row % 2) * 4;
    const mortarX = ((x + off) % 8) === 0;
    const mortarY = (y % 4) === 0;
    if (mortarX || mortarY) return [...mortar, 255];
    const bx = Math.floor(((x + off) % S) / 8);
    const t = tone[row % 4][((bx + row) % 4 + 4) % 4];
    let c = mix(brickLo, brickHi, 0.25 + t * 0.55 + n(x, y) * 0.25);
    if ((x + off) % 8 === 1 || y % 4 === 1) c = mix(c, brickHi, 0.4);   // top-left bevel
    if ((x + off) % 8 === 7 || y % 4 === 3) c = mix(c, brickLo, 0.5);   // bottom-right shade
    if (n(x + 5, y + 3) > 0.8) c = mix(c, PAL.lilacDim, 0.25);          // rare purple fleck
    return [...c, 255];
  });
};

/* ---- ores (share end-stone base) ---- */
function oreTex(seed, dark, mid, lit, glow, blobN) {
  const rng = mulberry32(seed);
  const base = endStone(rng);
  const nb = tileNoise(rng, 8);
  const blobs = Array.from({ length: blobN }, () => [2.5 + rng() * 11, 2.5 + rng() * 11, 1.2 + rng() * 1.5]);
  return make((x, y) => {
    let c = base(x, y);
    let d = 99;
    for (const [bx, by, br] of blobs) { const wob = (nb(x, y) - 0.5) * 1.1; d = Math.min(d, Math.hypot(x + 0.5 - bx, y + 0.5 - by) - br + wob); }
    if (d < -0.6) c = mix(mid, lit, Math.min(1, -d / 2.2));
    else if (d < 0) c = mix(dark, mid, (d + 0.6) / 0.6);
    else if (d < 0.8) c = mix(c, glow, 0.3 * (0.8 - d));
    return [...c, 255];
  });
}
BLOCK.void_ore = () => oreTex(4, PAL.oreDark, PAL.oreMid, PAL.oreLit, PAL.oreGlow, 4);
BLOCK.endrite_ore = () => oreTex(31, PAL.endriteDark, PAL.endriteMid, PAL.endriteLit, PAL.endriteGlow, 5);

BLOCK.cracked_end_stone = () => {
  const rng = mulberry32(5);
  const base = endStone(rng);
  const cf = crackField(rng, 5, [5, 11]);
  return make((x, y) => {
    let c = base(x, y);
    const cd = cf(x, y);
    if (cd < 0.55) c = mix(c, hx('3f3f2c'), 0.9);
    else if (cd < 1.1) c = mix(c, hx('6a6a4c'), 0.45);
    return [...c, 255];
  });
};

/* ---- endrite block (crystalline metal) ---- */
BLOCK.endrite_block = () => {
  const rng = mulberry32(44);
  const n = tileNoise(rng, 4), n2 = tileNoise(rng, 8);
  return make((x, y) => {
    // faceted: quantise a noise field into flat plates
    const f = Math.floor((n(x, y) * 0.7 + n2(x, y) * 0.3) * 5) / 5;
    let c = mix(PAL.endriteDark, PAL.endriteLit, f);
    // bevel highlights on facet edges
    const fr = Math.floor((n(x + 1, y) * 0.7 + n2(x + 1, y) * 0.3) * 5) / 5;
    const fb = Math.floor((n(x, y + 1) * 0.7 + n2(x, y + 1) * 0.3) * 5) / 5;
    if (fr !== f) c = mix(c, PAL.endriteGlow, 0.4);
    if (fb !== f) c = mix(c, PAL.endriteDark, 0.4);
    return [...c, 255];
  });
};

/* ---- crystal spire (CROSS, transparent) ---- */
BLOCK.crystal_spire = () => {
  const rng = mulberry32(61);
  const n = tileNoise(rng, 6);
  return make((x, y) => {
    // diamond-tipped column centred, tapering
    const cx = 8, halfW = 2.4 + (y < 3 ? -(3 - y) * 0.8 : 0) + (y > 12 ? -(y - 12) * 0.6 : 0);
    const dx = Math.abs(x + 0.5 - cx);
    if (dx > halfW || y < 1 || y > 15) return null;
    const t = 1 - dx / halfW; // 0 edge .. 1 centre
    let c = mix(PAL.crystalDark, PAL.crystalMid, t * 0.7 + n(x, y) * 0.3);
    if (dx < 0.8) c = mix(c, PAL.crystalLit, 0.6); // centre facet highlight
    if (dx > halfW - 1) c = mix(c, PAL.crystalDark, 0.3);
    return [...c, 255];
  });
};
BLOCK.crystal_glass = () => {
  const rng = mulberry32(62);
  const n = tileNoise(rng, 8);
  return make((x, y) => {
    const edge = (x === 0 || y === 0 || x === 15 || y === 15);
    const lat = (x % 5 === 0 || y % 5 === 0);
    if (edge) return [...mix(PAL.crystalMid, PAL.crystalLit, 0.5), 235];
    if (lat) return [...PAL.crystalMid, 150];
    return [...mix(PAL.crystalDark, PAL.crystalMid, 0.4 + n(x, y) * 0.3), 70];
  });
};

/* ---- void lamp ---- */
function voidLamp(top) {
  const rng = mulberry32(top ? 71 : 70);
  const n = tileNoise(rng, 6);
  const fdark = hx('16131c'), fmid = hx('2b2636'), flite = hx('3f3850');
  return make((x, y) => {
    const bx = Math.min(x, 15 - x), by = Math.min(y, 15 - y), b = Math.min(bx, by);
    if (b < 3) { // chunky metal frame with bevel
      let c = mix(fdark, fmid, n(x, y) * 0.6);
      if (b === 2) c = mix(c, flite, 0.5);
      if (b === 0) c = fdark;
      return [...c, 255];
    }
    const cd = Math.hypot(x + 0.5 - 8, y + 0.5 - 8);
    let c = mix(PAL.lilacDim, PAL.lilac, 0.35 + n(x, y) * 0.35 - cd * 0.03);
    c = mix(c, hx('efdcff'), Math.max(0, 0.5 - cd * 0.14)); // small hot core
    if (top && (Math.abs(x - 7.5) < 0.55 || Math.abs(y - 7.5) < 0.55)) c = mix(c, fdark, 0.7); // grille
    // inner frame shadow
    if (b === 3) c = shade(c, -18);
    return [...c, 255];
  });
}
BLOCK.void_lamp_side = () => voidLamp(false);
BLOCK.void_lamp_top = () => voidLamp(true);

/* ---- void-touched purpur ---- */
function purpur(top) {
  const rng = mulberry32(top ? 81 : 80);
  const n = tileNoise(rng, 8), n2 = tileNoise(rng, 16);
  const lo = hx('3a2c45'), hi = hx('574465'), sp = hx('2a2036'), spLite = hx('6a5878');
  // a couple of short straight hairline cracks
  const cracks = []; for (let k = 0; k < 3; k++) cracks.push([rng() * S, rng() * S, rng() * Math.PI, 2 + rng() * 3]);
  return make((x, y) => {
    const v = n(x, y) * 0.55 + n2(x, y) * 0.45;
    let c = mix(lo, hi, v);
    if (n2(x, y) < 0.22) c = mix(c, sp, 0.7);           // dark speckle
    else if (n2(x, y) > 0.82) c = mix(c, spLite, 0.4);  // light speckle
    for (const [ox, oy, ang, len] of cracks) {
      const dx = x + 0.5 - ox, dy = y + 0.5 - oy;
      const along = dx * Math.cos(ang) + dy * Math.sin(ang);
      const perp = Math.abs(-dx * Math.sin(ang) + dy * Math.cos(ang));
      if (along > 0 && along < len && perp < 0.55) c = mix(c, hx('241830'), 0.7);
    }
    if (top) { const b = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y)); if (b === 0) c = shade(c, -16); if (b === 1) c = shade(c, -6); }
    return [...c, 255];
  });
}
BLOCK.void_touched_purpur = () => purpur(false);
BLOCK.void_touched_purpur_top = () => purpur(true);

/* ---- void rune block ---- */
BLOCK.void_rune_block = () => {
  const rng = mulberry32(90);
  const n = tileNoise(rng, 6);
  // hand-drawn 12x12 rune mask centred in a black tile
  const R = [
    '............',
    '...X....X...',
    '...X....X...',
    '...XXXXXX...',
    '......X.....',
    '..XXXXXXXX..',
    '.....X......',
    '...XXXXXX...',
    '...X....X...',
    '...X....X...',
    '...X....X...',
    '............',
  ];
  return make((x, y) => {
    const b = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
    let c = mix(PAL.voidBlack, PAL.voidBase, n(x, y) * 0.4);
    if (b === 0) c = mix(c, PAL.lilacDim, 0.2);
    const rx = x - 2, ry = y - 2;
    if (rx >= 0 && rx < 12 && ry >= 0 && ry < 12) {
      if (R[ry][rx] === 'X') c = mix(PAL.lilac, hx('ffffff'), 0.15);
      else {
        // glow bleed
        let near = false;
        for (let dy = -1; dy <= 1; dy++) for (let dx = -1; dx <= 1; dx++) {
          const yy = ry + dy, xx = rx + dx;
          if (yy >= 0 && yy < 12 && xx >= 0 && xx < 12 && R[yy][xx] === 'X') near = true;
        }
        if (near) c = mix(c, PAL.lilac, 0.4);
      }
    }
    return [...c, 255];
  });
};

/* ---- umbral wood set (shared palette) ---- */
BLOCK.umbral_log_side = () => make(woodSide(mulberry32(100), PAL.barkDark, PAL.barkMid, PAL.barkHeart, 5));
BLOCK.umbral_log_top = () => make(woodTop(mulberry32(101), PAL.barkDark, PAL.barkMid, PAL.stripLite, PAL.barkHeart));
BLOCK.stripped_umbral_log_side = () => make(woodSide(mulberry32(102), PAL.stripDark, PAL.stripMid, PAL.stripLite, 4));
BLOCK.stripped_umbral_log_top = () => make(woodTop(mulberry32(103), PAL.stripDark, PAL.stripMid, PAL.lilacDim, PAL.barkHeart));
BLOCK.umbral_planks = () => {
  const rng = mulberry32(104);
  const n = tileNoise(rng, 8);
  return make((x, y) => {
    const plankH = 4; const row = Math.floor(y / plankH);
    const off = (row * 6) % S;
    let c = mix(PAL.stripDark, PAL.stripMid, 0.25 + n(x, y) * 0.6);
    if (y % plankH === 0) c = shade(c, -26);            // horizontal seam
    else if (y % plankH === 1) c = mix(c, PAL.stripLite, 0.3); // lit top of plank
    if (((x + off) % S) === 0) c = shade(c, -22);       // staggered vertical seam
    if ((x * 5 + y * 3 + row) % 6 === 0) c = mix(c, PAL.stripLite, 0.3); // grain streak
    return [...c, 255];
  });
};
BLOCK.umbral_leaves = () => {
  const rng = mulberry32(105);
  const n = tileNoise(rng, 8), n2 = tileNoise(rng, 4);
  const base = hx('14121c'), clump = hx('241830'), clumpLite = hx('33224a');
  return make((x, y) => {
    const v = n(x, y) * 0.6 + n2(x, y) * 0.4;
    if (v < 0.16) return null;                 // gaps in the canopy
    let c = mix(base, clump, v);
    if (v > 0.72) c = mix(c, clumpLite, (v - 0.72) / 0.28);
    // glowing edge pixels: bright where a transparent gap is adjacent
    let edge = false;
    for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const vv = n(x + dx, y + dy) * 0.6 + n2(x + dx, y + dy) * 0.4;
      if (vv < 0.16) edge = true;
    }
    if (edge) c = mix(c, PAL.lilac, 0.5);
    return [...c, 255];
  });
};

/* ---- voidmoss ---- */
BLOCK.voidmoss = () => {
  const rng = mulberry32(110);
  const n = tileNoise(rng, 8), n2 = tileNoise(rng, 4);
  const lo = hx('132420'), mid = hx('1e3a34'), hi = hx('2a5049');
  const tend = []; for (let k = 0; k < 14; k++) tend.push([rng() * S | 0, rng() * S | 0]);
  return make((x, y) => {
    const v = n(x, y) * 0.6 + n2(x, y) * 0.4;
    let c = mix(lo, mid, v);
    if (v > 0.7) c = mix(c, hi, (v - 0.7) / 0.3);
    for (const [tx, ty] of tend) if (tx === x && Math.abs(ty - y) < 2) c = mix(c, hi, 0.5);
    return [...c, 255];
  });
};

/* ========================== ITEMS ========================== */
// shape helper: returns t in [0,1] inside shape else -1, via signed dist
function crystalItem(seed, dark, mid, lit) {
  const rng = mulberry32(seed);
  const n = tileNoise(rng, 6);
  return make((x, y) => {
    // vertical lens / sliver from (8,2)..(8,14), width ~3
    const cx = 8 + Math.sin((y - 2) * 0.3) * 0.6;
    const w = 3.1 * Math.sin(Math.PI * (y - 1.5) / 13);
    if (w <= 0) return null;
    const dx = x + 0.5 - cx;
    if (Math.abs(dx) > w) return null;
    const t = 1 - Math.abs(dx) / w;
    let c = mix(dark, mid, t * 0.6 + n(x, y) * 0.3);
    if (dx < -w * 0.3) c = mix(c, lit, 0.5);       // lit left face
    if (dx > w * 0.5) c = mix(c, dark, 0.4);       // shadow right face
    if (Math.abs(dx) < 0.7 && y < 8) c = mix(c, lit, 0.4);
    // outline
    let out = false;
    for (const [ox, oy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const yy = y + oy; const ww = 3.1 * Math.sin(Math.PI * (yy - 1.5) / 13);
      const cxx = 8 + Math.sin((yy - 2) * 0.3) * 0.6;
      if (ww <= 0 || Math.abs(x + ox + 0.5 - cxx) > ww || yy < 1 || yy > 14) out = true;
    }
    if (out) c = shade(mix(dark, mid, 0.2), -20);
    return [...c, 255];
  });
}
ITEM.crystal_shard = () => crystalItem(200, PAL.crystalDark, PAL.crystalMid, PAL.crystalLit);
ITEM.endrite_shard = () => crystalItem(201, PAL.endriteDark, PAL.endriteMid, PAL.endriteLit);
ITEM.navigation_crystal = () => {
  const rng = mulberry32(202); const n = tileNoise(rng, 6);
  return make((x, y) => {
    const dx = x + 0.5 - 8, dy = y + 0.5 - 8;
    const d = Math.abs(dx) + Math.abs(dy);        // octahedron (diamond)
    if (d > 6.2) return null;
    let c = mix(PAL.crystalDark, PAL.crystalMid, 1 - d / 6.2);
    if (dx < 0 && dy < 0) c = mix(c, PAL.crystalLit, 0.4);   // lit facet
    if (dx > 0 && dy > 0) c = mix(c, PAL.crystalDark, 0.4);
    if (Math.abs(dx) < 1.2 && Math.abs(dy) < 1.2) c = mix(c, hx('ffffff'), 0.6); // inner mote
    if (d > 5.2) c = shade(c, -25);
    // faint pointer streak
    if (Math.abs(dx - dy) < 0.7 && d < 5) c = mix(c, PAL.crystalLit, 0.25 + n(x, y) * 0.2);
    return [...c, 255];
  });
};

// scale: teardrop / shield shape, rounded top, point at bottom, ridge lines
function scaleItem(seed, body, bodyHi, sheen, iridescent) {
  const rng = mulberry32(seed); const n = tileNoise(rng, 6);
  const inside = (px, py) => {
    const dx = px - 8, dy = py - 6.5;
    if (dy < 0) return dx * dx / 30 + dy * dy / 22 < 1;        // rounded dome top
    return Math.abs(dx) < 5.4 - dy * 0.72 && dy < 8.2;         // tapering to a point
  };
  return make((x, y) => {
    const px = x + 0.5, py = y + 0.5;
    if (!inside(px, py)) return null;
    const dx = px - 8, dy = py - 6.5;
    let c = mix(body, bodyHi, 0.25 + n(x, y) * 0.5);
    const ridge = Math.sin(dy * 1.15 + 0.6) * 0.5 + 0.5;      // horizontal ridge bands
    if (ridge > 0.72) c = shade(c, -16);
    if (dx < -1 && dy < -1) c = mix(c, sheen, 0.5);            // top-left sheen
    if (dy > 4) c = shade(c, -12);                            // darker toward tip
    if (iridescent) {
      const ir = Math.sin(px * 0.9 + py * 1.3);
      if (ir > 0.4) c = mix(c, hx('9a4fd6'), 0.3 * ir);
      if (ir < -0.6) c = mix(c, hx('2f7a9f'), 0.2);
    }
    let out = false;
    for (const [ox, oy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) if (!inside(px + ox, py + oy)) out = true;
    if (out) c = shade(body, -30);
    return [...c, 255];
  });
}
ITEM.void_scale = () => scaleItem(210, hx('1a1424'), hx('35284a'), hx('6a4f8c'), true);
ITEM.shadow_scale = () => scaleItem(211, hx('201a2c'), hx('3a2e50'), hx('4f4068'), false);
ITEM.camouflage_membrane = () => {
  const rng = mulberry32(212); const n = tileNoise(rng, 5), n2 = tileNoise(rng, 8);
  return make((x, y) => {
    const dx = x + 0.5 - 8, dy = y + 0.5 - 8;
    const wob = (n(x, y) - 0.5) * 4;
    const r = Math.hypot(dx, dy) + wob;
    if (r > 6.5) return null;
    const beige = hx('d8d3a8');
    let a = clamp(255 * (1 - r / 6.5) + 40);
    let c = mix(beige, shade(beige, -30), n2(x, y) * 0.7);
    return [...c, Math.min(230, a)];
  });
};

// dust mound
ITEM.void_dust = () => {
  const rng = mulberry32(220);
  const clumps = []; for (let k = 0; k < 5; k++) clumps.push([4 + rng() * 8, 8 + rng() * 5, 1.6 + rng() * 1.8]);
  const sp = []; for (let k = 0; k < 14; k++) sp.push([rng() * S | 0, (7 + rng() * 8) | 0, rng()]);
  return make((x, y) => {
    let d = 99; for (const [bx, by, br] of clumps) d = Math.min(d, Math.hypot(x + 0.5 - bx, y + 0.5 - by) - br);
    if (d > 0.6) return null;
    let c = mix(hx('2d0059'), hx('6a29a0'), (y - 6) / 9);
    if (d < -1) c = mix(c, hx('3a0f66'), 0.4);
    for (const [px, py, b] of sp) if (px === x && py === y) c = mix(c, b > 0.5 ? PAL.lilac : hx('9a4fd6'), 0.7);
    if (d > -0.3) c = shade(c, -25);
    return [...c, 255];
  });
};

// heart: two top lobes + V bottom, drawn from an explicit 16x16 mask
const HEART = [
  '................',
  '................',
  '...XX.....XX....',
  '..XXXX...XXXX...',
  '.XXXXXX.XXXXXX..',
  '.XXXXXXXXXXXXX..',
  '.XXXXXXXXXXXXX..',
  '.XXXXXXXXXXXXX..',
  '..XXXXXXXXXXX...',
  '...XXXXXXXXX....',
  '....XXXXXXX.....',
  '.....XXXXX......',
  '......XXX.......',
  '.......X.......',
  '................',
  '................',
];
const heartAt = (x, y) => x >= 0 && x < 16 && y >= 0 && y < 16 && HEART[y][x] === 'X';
ITEM.dragon_heart = () => {
  const rng = mulberry32(230); const n = tileNoise(rng, 6);
  return make((x, y) => {
    if (!heartAt(x, y)) return null;
    const pulse = Math.hypot(x + 0.5 - 8, y + 0.5 - 6);
    let c = mix(hx('1c0838'), hx('4a1a82'), n(x, y) * 0.7);
    c = mix(c, hx('d59cf0'), Math.max(0, 0.55 - pulse * 0.085));  // inner glow
    if (x < 8 && y < 8) c = mix(c, hx('7a3fb0'), 0.25);           // top-left facet sheen
    if ((x + y) % 4 === 0) c = shade(c, -12);                     // facet lines
    let out = false;
    for (const [ox, oy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) if (!heartAt(x + ox, y + oy)) out = true;
    if (out) c = hx('120430');
    return [...c, 255];
  });
};
ITEM.dragon_heart_fragment = () => {
  const rng = mulberry32(231); const n = tileNoise(rng, 6);
  const poly = [[5, 3], [11, 6], [8, 13], [4, 9]];
  const inside = (px, py) => {
    let s = false;
    for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
      const [xi, yi] = poly[i], [xj, yj] = poly[j];
      if (((yi > py) !== (yj > py)) && (px < (xj - xi) * (py - yi) / (yj - yi) + xi)) s = !s;
    }
    return s;
  };
  return make((x, y) => {
    if (!inside(x + 0.5, y + 0.5)) return null;
    let c = mix(hx('1c0838'), hx('4a1080'), n(x, y) * 0.8);
    if ((x + y) % 3 === 0) c = mix(c, hx('6a29a0'), 0.35);
    let out = false;
    for (const [ox, oy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) if (!inside(x + ox + 0.5, y + oy + 0.5)) out = true;
    if (out) c = hx('12042a');
    return [...c, 255];
  });
};
ITEM.heartwood_core = () => {
  const rng = mulberry32(232); const n = tileNoise(rng, 6);
  return make((x, y) => {
    const d = Math.hypot(x + 0.5 - 8, y + 0.5 - 8) + (n(x, y) - 0.5) * 2.2;
    if (d > 6.4) return null;
    const ring = Math.sin(d * 1.9) * 0.5 + 0.5;
    let c = mix(hx('3a2350'), hx('5a3070'), ring * 0.7);
    c = mix(c, hx('d9a3ff'), Math.max(0, 0.7 - d * 0.11)); // warm centre glow
    if (d > 5.4) c = shade(c, -30);
    return [...c, 255];
  });
};
ITEM.gloom_essence = () => {
  const rng = mulberry32(233); const n = tileNoise(rng, 5), n2 = tileNoise(rng, 8);
  return make((x, y) => {
    const d = Math.hypot(x + 0.5 - 8, y + 0.5 - 8) + (n(x, y) - 0.5) * 3;
    if (d > 6.2) return null;
    let c = mix(hx('0d0a14'), hx('2a1c3a'), n2(x, y) * 0.8);
    const a = clamp(235 * (1 - d / 6.2) + 30);
    if (d < 2) c = mix(c, hx('4a2f66'), 0.4);
    return [...c, Math.min(220, a)];
  });
};
ITEM.wraith_feather = () => {
  const rng = mulberry32(234); const n = tileNoise(rng, 8);
  // explicit feather mask: leaning vane + thin quill at bottom-left
  const F = [
    '............X...',
    '...........XX...',
    '..........XXXX..',
    '.........XXXXX..',
    '........XXXXXX..',
    '........XXXXXX..',
    '.......XXXXXXX..',
    '......XXXXXXX...',
    '......XXXXXX....',
    '.....XXXXXX.....',
    '.....XXXXX......',
    '....XXXXX.......',
    '....XXX.........',
    '...XX...........',
    '..XX............',
    '.X..............',
  ];
  const at = (x, y) => x >= 0 && x < 16 && y >= 0 && y < 16 && F[y][x] === 'X';
  // rachis column per row (roughly the right inner edge of the vane, then the quill)
  const rachis = (y) => y < 13 ? 12 - y * 0.55 : 5 - (y - 13) * 1.4;
  return make((x, y) => {
    if (!at(x, y)) return null;
    const rc = rachis(y);
    const d = x + 0.5 - rc;                 // <0 outer vane, ~0 shaft
    let c, a = 255;
    if (Math.abs(d) < 0.75) {
      c = mix(hx('c2c2d2'), hx('eeeef6'), 0.5);          // bright rachis
    } else {
      const e = Math.min(1, -d / 6);                     // 0 near shaft .. 1 outer tip
      c = mix(hx('70708a'), hx('a6acc2'), 1 - e);
      if ((x + y) % 2 === 0) c = shade(c, -16);          // barb striations
      a = clamp(255 - e * 90);
      if (n(x, y) > 0.80 && e > 0.45) a = 0;             // torn holes
    }
    if (y >= 13) { c = mix(hx('8a8a9c'), hx('c0c0cc'), 0.4); a = 255; } // solid quill
    if (y <= 2) a = clamp(a - (3 - y) * 45);             // faded tip
    c = mix(c, hx('93bcdb'), 0.16 + n(x + 4, y + 1) * 0.14); // ghost tint
    let out = false;
    for (const [ox, oy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) if (!at(x + ox, y + oy)) out = true;
    if (out && a > 120) c = shade(c, -34);
    return [...c, Math.max(0, a)];
  });
};
ITEM.parasite_fluid = () => {
  const rng = mulberry32(235); const n = tileNoise(rng, 6);
  return make((x, y) => {
    // droplet: circle body + pointed top
    const dx = x + 0.5 - 8, dy = y + 0.5 - 9;
    const r = Math.hypot(dx, dy);
    const pointed = dy < 0 && Math.abs(dx) < (4 + dy) * 0.9;
    if (!(r < 5 || pointed)) return null;
    let c = mix(hx('1e3a1c'), hx('4a8c3a'), n(x, y) * 0.7);
    c = mix(c, hx('9af07a'), Math.max(0, 0.6 - r * 0.11));
    if (dx < -1 && dy < 1) c = mix(c, hx('c8ffb0'), 0.4);  // highlight
    if (r > 4.2 && !pointed) c = shade(c, -22);
    // sickly purple mottle
    if (n(x + 3, y) > 0.7) c = mix(c, hx('7a3fb0'), 0.4);
    return [...c, 235];
  });
};
ITEM.ancient_rune_fragment = () => {
  const rng = mulberry32(236); const n = tileNoise(rng, 6), n2 = tileNoise(rng, 12);
  // irregular torn shard mask
  const T = [
    '................',
    '....XXXXX.......',
    '...XXXXXXXX.....',
    '..XXXXXXXXXX....',
    '..XXXXXXXXXXX...',
    '.XXXXXXXXXXXX...',
    '.XXXXXXXXXXXXX..',
    '.XXXXXXXXXXXX...',
    '..XXXXXXXXXXX...',
    '..XXXXXXXXXX....',
    '..XXXXXXXXXX....',
    '...XXXXXXXX.....',
    '...XXXXXXX......',
    '....XXXXX.......',
    '.....XXX........',
    '................',
  ];
  const at = (x, y) => x >= 0 && x < 16 && y >= 0 && y < 16 && T[y][x] === 'X';
  const G = ['..X..', '.XXX.', 'X.X.X', '..X..', '.X.X.']; // half of a glyph
  const stoneLo = hx('54524a'), stoneHi = hx('736f63'), crack = hx('34322c');
  return make((x, y) => {
    if (!at(x, y)) return null;
    let c = mix(stoneLo, stoneHi, 0.3 + n(x, y) * 0.55);
    if (n2(x, y) < 0.2) c = shade(c, -16);               // pitting
    const gx = x - 4, gy = y - 4;
    if (gx >= 0 && gx < 5 && gy >= 0 && gy < 5 && G[gy][gx] === 'X') c = mix(PAL.lilac, hx('ffffff'), 0.25); // engraved rune, glows
    else if (gx >= -1 && gx < 6 && gy >= -1 && gy < 6) {
      let near = false;
      for (let dy = -1; dy <= 1; dy++) for (let dx = -1; dx <= 1; dx++) {
        const yy = gy + dy, xx = gx + dx;
        if (yy >= 0 && yy < 5 && xx >= 0 && xx < 5 && G[yy][xx] === 'X') near = true;
      }
      if (near) c = mix(c, PAL.lilacDim, 0.35);
    }
    let out = false;
    for (const [ox, oy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) if (!at(x + ox, y + oy)) out = true;
    if (out) c = crack;
    return [...c, 255];
  });
};

/* ========================== run ========================== */
const out = process.argv[2] || './out';
const zoom = process.argv[3];
for (const sub of ['block', 'item']) fs.mkdirSync(path.join(out, sub), { recursive: true });
if (zoom) fs.mkdirSync(zoom, { recursive: true });

function writeAll(map, sub) {
  for (const [name, fn] of Object.entries(map)) {
    const px = fn();
    fs.writeFileSync(path.join(out, sub, name + '.png'), encodePNG(px, S, S));
    if (zoom) {
      const Z = 16, zp = Buffer.alloc(S * Z * S * Z * 4);
      for (let y = 0; y < S * Z; y++) for (let x = 0; x < S * Z; x++) {
        const si = ((y / Z | 0) * S + (x / Z | 0)) * 4, di = (y * S * Z + x) * 4;
        zp[di] = px[si]; zp[di + 1] = px[si + 1]; zp[di + 2] = px[si + 2]; zp[di + 3] = px[si + 3];
      }
      fs.writeFileSync(path.join(zoom, sub + '_' + name + '_x16.png'), encodePNG(zp, S * Z, S * Z));
    }
    console.log('wrote', sub + '/' + name);
  }
}
writeAll(BLOCK, 'block');
writeAll(ITEM, 'item');

// contact sheet: each tex at 10x, on a mid-grey checker, labels omitted
function montage(map, file, cols) {
  const names = Object.keys(map);
  const Z = 10, pad = 4, cell = S * Z + pad;
  const rows = Math.ceil(names.length / cols);
  const W = cols * cell + pad, H = rows * cell + pad;
  const buf = Buffer.alloc(W * H * 4);
  for (let i = 0; i < W * H; i++) {
    const x = i % W, y = (i / W | 0);
    const chk = ((x >> 3) + (y >> 3)) & 1;
    buf[i * 4] = chk ? 90 : 78; buf[i * 4 + 1] = chk ? 90 : 78; buf[i * 4 + 2] = chk ? 96 : 84; buf[i * 4 + 3] = 255;
  }
  names.forEach((name, k) => {
    const px = map[name]();
    const ox = pad + (k % cols) * cell, oy = pad + ((k / cols | 0)) * cell;
    for (let y = 0; y < S * Z; y++) for (let x = 0; x < S * Z; x++) {
      const si = ((y / Z | 0) * S + (x / Z | 0)) * 4;
      const a = px[si + 3] / 255;
      const di = ((oy + y) * W + (ox + x)) * 4;
      buf[di] = clamp(buf[di] * (1 - a) + px[si] * a);
      buf[di + 1] = clamp(buf[di + 1] * (1 - a) + px[si + 1] * a);
      buf[di + 2] = clamp(buf[di + 2] * (1 - a) + px[si + 2] * a);
    }
  });
  fs.writeFileSync(file, encodePNG(buf, W, H));
  console.log('montage', file, W + 'x' + H);
}
if (zoom) {
  montage(BLOCK, path.join(zoom, '_sheet_blocks.png'), 5);
  montage(ITEM, path.join(zoom, '_sheet_items.png'), 5);
}
