"""Koi Atelier 텍스처 생성기: 망토, 흰색 바탕, 미니맵 화살표, 모드 아이콘. (후광/날개는 KoiSprites.java의 도트 그림)

사용법: python tools/gen_textures.py  (Pillow 필요)
결과는 src/client/resources/assets/koi-atelier/textures/ 에 저장됨
"""
import math
import random
from pathlib import Path

from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parent.parent / "src/client/resources/assets/koi-atelier/textures"


# ---------- 망토 (64x32, 바닐라 망토 + 엘리트라 UV) ----------
# 망토 앞/뒷면 10x16, 엘리트라 앞/뒷면 10x20
CAPE_FACES = [(1, 1, 10, 16), (12, 1, 10, 16)]
ELYTRA_FACES = [(24, 2, 10, 20), (36, 2, 10, 20)]


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(len(a)))


def cape(name, edge, paint):
    """edge: 테두리/옆면 색, paint(w, h) -> Image: 한 면에 그릴 무늬"""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 21, 16], fill=edge)   # 망토 전체 (옆면, 윗면 포함)
    d.rectangle([22, 0, 45, 21], fill=edge)  # 엘리트라 전체
    for x, y, w, h in CAPE_FACES + ELYTRA_FACES:
        img.paste(paint(w, h), (x, y))
    img.save(OUT / "cape" / f"{name}.png")


def koi(w, h):
    img = Image.new("RGBA", (w, h), (20, 20, 26, 255))
    d = ImageDraw.Draw(img)
    gold, orange, white = (240, 190, 40, 255), (235, 110, 30, 255), (240, 240, 235, 255)
    # 위: 금색 잉어 (머리가 아래로), 아래: 흰 잉어 (머리가 위로) - 서로 도는 모양
    d.ellipse([1, 1, 6, 8], fill=gold)
    d.polygon([(6, 1), (9, 0), (8, 3)], fill=gold)  # 꼬리
    d.point([(3, 3), (4, 5)], fill=orange)
    d.ellipse([3, h - 9, 8, h - 2], fill=white)
    d.polygon([(3, h - 2), (0, h - 1), (1, h - 4)], fill=white)
    d.point([(5, h - 6), (6, h - 4)], fill=orange)
    # 물결
    for y in (h // 2 - 1, h // 2):
        for x in range(w):
            if (x + y) % 3 == 0:
                d.point((x, y), fill=(60, 70, 110, 255))
    return img


def flame(w, h):
    img = Image.new("RGBA", (w, h))
    for y in range(h):
        t = y / (h - 1)
        c = lerp((255, 220, 80, 255), (150, 20, 10, 255), t) if t > 0.3 else lerp((40, 10, 10, 255), (255, 220, 80, 255), t / 0.3)
        for x in range(w):
            img.putpixel((x, y), c)
    d = ImageDraw.Draw(img)
    # 불꽃 혀
    for x0, top in ((1, 2), (4, 0), (7, 3)):
        d.polygon([(x0, h - 3), (x0 + 1, top + 4), (x0 + 2, h - 3)], fill=(255, 245, 160, 255))
    return img


def sakura(w, h):
    rnd = random.Random(7)
    img = Image.new("RGBA", (w, h))
    for y in range(h):
        c = lerp((255, 220, 235, 255), (220, 120, 160, 255), y / (h - 1))
        for x in range(w):
            img.putpixel((x, y), c)
    d = ImageDraw.Draw(img)
    d.line([(0, 3), (4, 6), (9, 5)], fill=(90, 50, 40, 255))  # 가지
    for _ in range(14):
        x, y = rnd.randrange(w), rnd.randrange(h)
        d.point((x, y), fill=rnd.choice([(255, 255, 255, 255), (255, 170, 200, 255)]))
    return img


def galaxy(w, h):
    rnd = random.Random(3)
    img = Image.new("RGBA", (w, h))
    for y in range(h):
        for x in range(w):
            t = (y + x * 0.5) / (h + w * 0.5)
            img.putpixel((x, y), lerp((15, 10, 40, 255), (70, 30, 120, 255), math.sin(t * math.pi)))
    for _ in range(12):
        img.putpixel((rnd.randrange(w), rnd.randrange(h)), rnd.choice([(255, 255, 255, 255), (180, 200, 255, 255)]))
    return img


def ocean(w, h):
    img = Image.new("RGBA", (w, h))
    for y in range(h):
        c = lerp((120, 210, 240, 255), (10, 40, 110, 255), y / (h - 1))
        for x in range(w):
            img.putpixel((x, y), c)
    d = ImageDraw.Draw(img)
    for y0 in range(2, h, 4):
        for x in range(w):
            y = y0 + (1 if (x // 2) % 2 else 0)
            d.point((x, y), fill=(220, 245, 255, 255))
    return img


# ---------- 흰색 텍스처 (후광/날개 도트 블록에 색을 입힐 때 바탕) ----------
def white():
    Image.new("RGBA", (16, 16), (255, 255, 255, 255)).save(OUT / "white.png")


# ---------- 미니맵 화살표 (16x16, 위쪽이 바라보는 방향) ----------
def arrow():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(8, 1), (14, 14), (8, 11), (2, 14)], fill=(255, 255, 255, 255), outline=(20, 20, 20, 255))
    img.save(OUT / "minimap_arrow.png")


# ---------- 모드 아이콘 (16x16 도트를 128x128로 확대) ----------
ICON = [
    "................",
    "....oooooooo....",
    "..oo~~~~~~~~oo..",
    ".o~~~~~~~~~~~~o.",
    ".o~~~~GGGG~~~~o.",
    "o~~~~GGRGGG~~~~o",
    "o~~GGGWRRGGG~~~o",
    "o~GGKGWWRGGGGF~o",
    "o~GGGGWRRGGGFF~o",
    "o~~GGGRRGGG~~F~o",
    "o~~~~GGGGG~~~~~o",
    ".o~~~~~GG~~~~~o.",
    ".o~~~~~~~~~~~~o.",
    "..oo~~~~~~~~oo..",
    "....oooooooo....",
    "................",
]
ICON_COLORS = {
    "o": (60, 40, 30, 255),     # 연못 테두리
    "~": (40, 90, 150, 255),    # 물
    "G": (245, 190, 40, 255),   # 금색 비늘
    "R": (230, 90, 40, 255),    # 주황 무늬
    "W": (250, 245, 235, 255),  # 흰 무늬
    "K": (20, 20, 25, 255),     # 눈
    "F": (255, 220, 120, 255),  # 꼬리
}


def icon():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(ICON):
        for x, ch in enumerate(row):
            if ch in ICON_COLORS:
                img.putpixel((x, y), ICON_COLORS[ch])
    return img


if __name__ == "__main__":
    (OUT / "cape").mkdir(parents=True, exist_ok=True)
    cape("koi", (20, 20, 26, 255), koi)
    cape("flame", (120, 20, 10, 255), flame)
    cape("sakura", (220, 120, 160, 255), sakura)
    cape("galaxy", (15, 10, 40, 255), galaxy)
    cape("ocean", (10, 40, 110, 255), ocean)
    white()
    arrow()
    # 모드 아이콘 (Mod Menu, 런처에서 보임)
    icon().resize((128, 128), Image.NEAREST).save(OUT.parent / "icon.png")
    print("saved to", OUT)
