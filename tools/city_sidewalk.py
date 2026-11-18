from PIL import Image, ImageDraw
import os, shutil
R="/tmp/ph/PixelHome/app/src/main/res/drawable-nodpi"; RAW="/tmp/ph/PixelHome/app/src/main/res/raw/windows.txt"
BK="/tmp/ph/paths/orig"
sw=Image.open("/tmp/ph/PixelHome/tools/palette.png").convert("RGB")
P=sorted({sw.getpixel((i*12+6,6)) for i in range(sw.width//12)})
def near(c): return min(P,key=lambda q:(q[0]-c[0])**2*3+(q[1]-c[1])**2*4+(q[2]-c[2])**2*2)
hx=lambda h:tuple(int(h[i:i+2],16) for i in (1,3,5))
# pavement colours per session: night darker, day lighter
PAVE=[("#3a3450","#2a2440","#5a5470"),("#4a4060","#342c48","#6a6080"),("#9a8a9a","#7a6a7a","#c0b0b8"),("#a8a8b0","#8a8a94","#d0d0d8"),
      ("#b0b0b8","#90909a","#dadae0"),("#a08a88","#806a68","#c8b0a8"),("#4a3a5a","#30243e","#6a5a7a"),("#34304a","#24203a","#545070")]
for i,(base,dark,light) in enumerate(PAVE):
    src=f"{BK}/land{i}.png"
    if not os.path.exists(src): shutil.copy(f"{R}/land{i}.png",src)
    im=Image.open(src).convert("RGBA"); d=ImageDraw.Draw(im)
    d.rectangle((0,85,159,89),fill=hx(base)); d.line((0,85,159,85),fill=hx(light)); d.line((0,86,159,86),fill=hx(dark))
    for x in range(0,160,10): d.line((x,87,x,89),fill=hx(dark))
    out=Image.new("RGBA",im.size); out.putdata([near(p[:3])+(255,) if p[3] else (0,0,0,0) for p in im.getdata()]); out.save(f"{R}/land{i}.png")
if not os.path.exists(f"{BK}/windows.txt"): shutil.copy(RAW,f"{BK}/windows.txt")
wins=[w for w in open(f"{BK}/windows.txt").read().split(";") if int(w.split(",")[1])<84]   # no lit windows on the pavement
open(RAW,"w").write(";".join(wins)); print(len(wins),"windows kept")
