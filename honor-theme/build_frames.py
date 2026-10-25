"""Export existing Pompi atlas to HONOR Theme Editor's documented RGBA32 format.

These are input assets, not a fabricated/installable .hnt file. No screenshots,
clock, battery percentage, notifications or extra battery are baked into them.
"""
from __future__ import annotations

import argparse
import json
import math
import colorsys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

WIDTH, HEIGHT, FPS, COUNT = 1080, 2160, 30, 180


def curve(a, b, c, d, count=70):
    points = []
    for index in range(count + 1):
        t = index / count
        q = 1 - t
        points.append((q**3*a[0]+3*q*q*t*b[0]+3*q*t*t*c[0]+t**3*d[0],
                       q**3*a[1]+3*q*q*t*b[1]+3*q*t*t*c[1]+t**3*d[1]))
    return points


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--atlas', type=Path, default=Path(__file__).resolve().parents[1]/'assets/pompi-atlas.png')
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    frames_dir = args.output/'frames'
    frames_dir.mkdir(exist_ok=True)
    atlas = Image.open(args.atlas).convert('RGBA')
    cell_w, cell_h = atlas.width//2, atlas.height//2
    size, left, top = 360, 100, 930
    cells = [atlas.crop((i%2*cell_w, i//2*cell_h, (i%2+1)*cell_w, (i//2+1)*cell_h)).resize((size,size), Image.Resampling.LANCZOS) for i in range(4)]
    mouths = [(.927,.74),(.94,.75),(.94,.75),(.955,.76)]
    previews = []
    for index in range(COUNT):
        seconds = index/FPS
        wiping = 3.5 <= seconds < 5
        pose = 2 if wiping else (0 if math.sin(seconds*math.tau*2) >= 0 else 1)
        bob = round(math.sin(seconds*math.tau*2)*3) if not wiping else 0
        # Original procedural ring, rather than any pixels from the user's lockscreen.
        ring = Image.new('RGBA', (WIDTH, HEIGHT), (0,0,0,0))
        rd = ImageDraw.Draw(ring)
        for angle in range(0,360,2):
            rgb = tuple(round(v*255) for v in colorsys.hsv_to_rgb((angle/360+seconds/12)%1,.75,1))
            rd.arc((170,690,950,1470),angle,angle+3,fill=(*rgb,235),width=9)
        frame = ring.filter(ImageFilter.GaussianBlur(16))
        frame.alpha_composite(ring.filter(ImageFilter.GaussianBlur(3)))
        frame.alpha_composite(ring)
        draw = ImageDraw.Draw(frame)
        mouth = (left+mouths[pose][0]*size,top+bob+mouths[pose][1]*size)
        # Fixed endpoint meets the charging ring, staying clear of native percentage.
        points = curve(mouth,(mouth[0]+5,mouth[1]+210),(850,1450),(900,1270))
        draw.line(points, fill=(66,14,18,255), width=13, joint='curve')
        draw.line(points, fill=(255,48,33,255), width=8, joint='curve')
        draw.line(points, fill=(255,174,118,255), width=2, joint='curve')
        frame.alpha_composite(cells[pose], (left,top+bob))
        frame.save(frames_dir/f'frame_{index+1:04}.png', compress_level=2)
        if index % 3 == 0:
            # A generic animation preview, not a screenshot of the user's device.
            bg=Image.new('RGB',(WIDTH,HEIGHT),(7,13,30))
            bg.paste(frame,mask=frame.getchannel('A'))
            previews.append(bg.resize((360,720),Image.Resampling.LANCZOS))
    previews[0].save(args.output/'animation-preview.webp',save_all=True,append_images=previews[1:],duration=100,loop=0,quality=80)
    manifest={
        'target':'HONOR Magic7 Pro / MagicOS 10+',
        'assetStatus':'HONOR Theme Editor input frames; .hnt export and device validation required',
        'width':WIDTH,'height':HEIGHT,'pixelFormat':'RGBA, 8 bits per channel (32 bits total)',
        'fps':FPS,'frameCount':COUNT,'durationSeconds':6,'loopStartFrame':1,'loopEndFrame':COUNT,
        'characterBounds':{'x':left,'y':top,'width':size,'height':size},
        'suggestedNativePercentage':{'x':650,'y':1065,'note':'Native system text, not baked into video. Device validation required.'},
        'hoseEndpoint':{'x':900,'y':1270,'target':'procedural charging ring'},
        'containsClock':False,'containsPercentage':False,'containsSecondBattery':False,'containsUserScreenshot':False,
        'officialGuide':'https://developer.honor.com/cn/docs/11106/guides/themetool',
    }
    (args.output/'frame-manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    for path in frames_dir.glob('*.png'):
        with Image.open(path) as image:
            assert image.mode=='RGBA' and image.size==(WIDTH,HEIGHT)
            assert image.getpixel((0,0))[3]==0
            assert not image.info.get('transparency')
    assert len(list(frames_dir.glob('*.png')))==COUNT
    print(json.dumps({'frames':COUNT,'size':[WIDTH,HEIGHT],'format':'RGBA32','output':str(args.output)}))


if __name__ == '__main__':
    main()
