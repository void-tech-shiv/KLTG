#!/usr/bin/env python3
"""Replace upstream launcher pictures with original GLACIA gradient/G artwork."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import math

root = Path(__file__).resolve().parent.parent / "TMessagesProj/src/main"
candidates = []
for p in root.rglob("*"):
    if not p.is_file() or p.suffix.lower() != ".png":
        continue
    if not any(part.startswith("mipmap-") for part in p.parts):
        continue
    if ("res-glacia" not in p.parts and not p.stem.startswith(("icon_", "ic_launcher"))) or not p.stem.startswith(("icon_", "ic_launcher")):
        continue
    candidates.append(p)

font_path = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
changed = 0
for p in candidates:
    try:
        original = Image.open(p)
        w, h = original.size
        if min(w, h) < 24:
            continue
        scale = 3
        sw, sh = w * scale, h * scale
        foreground = "foreground" in p.stem
        background = "background" in p.stem
        image = Image.new("RGBA", (sw, sh), (0, 0, 0, 0))
        px = image.load()
        if not foreground:
            for y in range(sh):
                t = y / max(1, sh - 1)
                for x in range(sw):
                    horiz = x / max(1, sw - 1)
                    px[x,y] = (int(7 + 16*t), int(44 + 35*horiz + 12*t),
                               int(105 + 60*(1-t)), 255)
        draw = ImageDraw.Draw(image)
        if not background:
            font = ImageFont.truetype(font_path, int(min(sw, sh) * .64))
            box = draw.textbbox((0,0), "G", font=font)
            draw.text(((sw-(box[2]-box[0]))/2-box[0],
                       (sh-(box[3]-box[1]))/2-box[1]-sh*.015),
                      "G", font=font, fill=(246,251,255,255), stroke_width=max(1,sw//200),
                      stroke_fill=(125,215,255,255))
            rad = int(min(sw, sh)*.035)
            cx, cy = int(sw*.77), int(sh*.27)
            draw.line((cx-rad*2,cy,cx+rad*2,cy),fill=(135,235,255,255),width=max(1,sw//75))
            draw.line((cx,cy-rad*2,cx,cy+rad*2),fill=(135,235,255,255),width=max(1,sw//75))
        image = image.resize((w,h), Image.Resampling.LANCZOS)
        image.save(p)
        changed += 1
    except Exception as ex:
        print(f"Skipped {p}: {ex}")
print(f"GLACIA launcher assets regenerated: {changed}")
