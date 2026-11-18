# Give every slideshow scene a walkway along its bottom edge (rows 34-39), so the family walks ON something.
from PIL import Image, ImageDraw
import random, shutil, os
R="/tmp/ph/PixelHome/app/src/main/res/drawable-nodpi"
BK="/tmp/ph/paths/orig"
os.makedirs(BK,exist_ok=True)
sw=Image.open("/tmp/ph/PixelHome/tools/palette.png").convert("RGB")
P=sorted({sw.getpixel((i*12+6,6)) for i in range(sw.width//12)})
def near(c): return min(P,key=lambda q:(q[0]-c[0])**2*3+(q[1]-c[1])**2*4+(q[2]-c[2])**2*2)
hx=lambda h:tuple(int(h[i:i+2],16) for i in (1,3,5))
W,H,Y0=384,40,34
def planks(d,r,base,dark,light,gap=7):        # wooden boardwalk / pier
    d.rectangle((0,Y0,W,H),fill=hx(base)); d.line((0,Y0,W,Y0),fill=hx(light)); d.line((0,H-1,W,H-1),fill=hx(dark))
    for x in range(0,W,gap): d.line((x,Y0+1,x,H-2),fill=hx(dark))
    for x in range(3,W,gap*2): d.point((x,Y0+3),fill=hx(dark))
def speckled(d,r,base,dark,light,dots=("#000000",),n=260):   # sand, dirt, snow, gravel
    d.rectangle((0,Y0,W,H),fill=hx(base)); d.line((0,Y0,W,Y0),fill=hx(light)); d.line((0,H-1,W,H-1),fill=hx(dark))
    for _ in range(n): d.point((r.randrange(W),r.randrange(Y0+1,H-1)),fill=hx(r.choice(dots)))
def metal(d,r,base,dark,light,lamp):          # space / sci-fi walkway with little lights
    d.rectangle((0,Y0,W,H),fill=hx(base)); d.line((0,Y0,W,Y0),fill=hx(light)); d.line((0,Y0+1,W,Y0+1),fill=hx(dark))
    d.line((0,H-1,W,H-1),fill=hx(dark))
    for x in range(0,W,16): d.line((x,Y0+2,x,H-2),fill=hx(dark)); d.point((x+8,Y0+3),fill=hx(lamp))
def tiles(d,r,a,b,edge):                      # patterned floor
    for y in range(Y0,H):
        for x in range(W): d.point((x,y),fill=hx(a if ((x//4)+(y-Y0)//3)%2 else b))
    d.line((0,Y0,W,Y0),fill=hx(edge))
def grass_edge(d,r,col):                      # tufts along the top of a path
    for x in range(0,W,3):
        if r.random()<0.6: d.point((x,Y0-1),fill=hx(col))
STYLE=[
 lambda d,r: speckled(d,r,"#dfe8f5","#9fb0c8","#ffffff",("#bfe3ff","#ffffff"),200),            # aurora: snowy path
 lambda d,r: metal(d,r,"#2a0a4a","#14002a","#ff3fd0","#5af0ff"),                               # synthwave: neon road
 lambda d,r: metal(d,r,"#4a4a5c","#2a2a3a","#9a9ab0","#5af0ff"),                               # galaxy: space walkway
 lambda d,r: metal(d,r,"#3a3a4a","#1a1a2a","#8a8aa0","#ff8a3a"),                               # black hole: walkway
 lambda d,r: speckled(d,r,"#e8d29a","#b89a5a","#fff1c8",("#c8a868","#fff8e0"),240),            # reef: seabed sand
 lambda d,r: (speckled(d,r,"#9a6a3a","#6a4224","#c08a52",("#7a5230","#b07a48"),220),grass_edge(d,r,"#62c048")), # terraces: dirt path
 lambda d,r: speckled(d,r,"#2a2228","#140c10","#4a3a40",("#ff6a1a","#3a2a30"),120),            # volcano: dark gravel
 lambda d,r: (planks(d,r,"#4a4a5a","#2a2a3a","#8a8aa0",12),),                                    # neon city: wet pavement
 lambda d,r: planks(d,r,"#a87a4a","#6a4424","#d0a070",6),                                       # floating islands: rope bridge
 lambda d,r: planks(d,r,"#7a5a3a","#4a321e","#a07850",7),                                       # lighthouse: pier
 lambda d,r: (speckled(d,r,"#8a5a2a","#5a3818","#b07a40",("#e8902a","#c85a2a","#f0b04a"),300),), # autumn: leafy path
 lambda d,r: (speckled(d,r,"#3a2a1a","#20160c","#5a4228",("#2a1e12",),120),grass_edge(d,r,"#1a4a2a")), # forest trail
 lambda d,r: speckled(d,r,"#f0d090","#c0a060","#fff0c0",("#d8b878","#ffffff"),220),             # ocean: beach sand
 lambda d,r: planks(d,r,"#8a6a5a","#5a4038","#b08878",7),                                       # mirror lake: boardwalk
 lambda d,r: metal(d,r,"#5a5a6a","#2a2a3a","#b0b0c8","#ffd84a"),                               # orbit: station walkway
 lambda d,r: tiles(d,r,"#5a1a8a","#ffd84a","#ff7a5a"),                                          # mandala: mosaic floor
 lambda d,r: (speckled(d,r,"#a07848","#6a4a2a","#c8a070",("#7a5a38",),160),grass_edge(d,r,"#4a7a5a")), # balloons: country path
 lambda d,r: planks(d,r,"#6a5a6a","#3a2a3a","#9a8a9a",7),                                       # whale: night pier
 lambda d,r: (speckled(d,r,"#b08858","#7a5a38","#d0a878",("#ff5a7a","#ffd84a","#ffffff"),120),grass_edge(d,r,"#4aa83a")), # flowers: path
 lambda d,r: speckled(d,r,"#a04a3a","#6a2a22","#c86a50",("#7a3a2a","#d08060"),220),             # ring planet: red rock
]
for i,style in enumerate(STYLE):
    src=f"{BK}/slide{i:02d}.png"
    if not os.path.exists(src): shutil.copy(f"{R}/slide{i:02d}.png",src)   # keep the originals so this can be re-run
    im=Image.open(src).convert("RGB"); d=ImageDraw.Draw(im); style(d,random.Random(100+i))
    out=Image.new("RGB",im.size); out.putdata([near(p) for p in im.getdata()]); out.save(f"{R}/slide{i:02d}.png")
S=Image.new("RGB",(2*(W*2+8),10*(H*2+8)),(20,16,40))
for i in range(20): S.paste(Image.open(f"{R}/slide{i:02d}.png").resize((W*2,H*2),Image.NEAREST),((i%2)*(W*2+8),(i//2)*(H*2+8)))
S.save("/tmp/ph/paths/sheet.png"); print("ok")
