from PIL import Image, ImageDraw, ImageFont
import math, random
R="/tmp/ph/PixelHome/app/src/main/res"
SKY=[Image.open(f"{R}/drawable-nodpi/sky{i}.png").convert("RGBA") for i in range(8)]
LAND=[Image.open(f"{R}/drawable-nodpi/land{i}.png").convert("RGBA") for i in range(8)]
WIN=[tuple(map(int,p.split(","))) for p in open(f"{R}/raw/windows.txt").read().split(";")]
NIGHT=[1,.6,0,0,0,0,.6,.9]; DAY=[0,.05,.8,1,1,.8,.2,0]; LIGHTS=[.5,.35,.08,.02,.02,.12,.9,.8]
CLOUD=[["....###.....","..#######...",".##########.","############"],["...####...",".########.","##########"],["..#####......",".#########....","#############."]]
CL=[(10,12,0),(70,22,1),(120,8,2),(40,32,1)]
lerp=lambda a,b,t:a+(b-a)*t
def mix(m):
    pos=(m-90)/180
    if pos<0: pos+=8
    i=int(math.floor(pos))%8; t=pos-math.floor(pos); t=t*t*(3-2*t); return i,(i+1)%8,t
def frame(m):
    a,b,t=mix(m); night=lerp(NIGHT[a],NIGHT[b],t); day=lerp(DAY[a],DAY[b],t); lights=lerp(LIGHTS[a],LIGHTS[b],t); h=m/60
    im=Image.blend(SKY[a],SKY[b],t); d=ImageDraw.Draw(im,"RGBA")
    r=random.Random(7)
    for _ in range(34):
        x,y=r.randrange(160),r.randrange(52)
        if night>0.02: d.point((x,y),fill=(255,255,255,int(255*night*0.8)))
    ts=(h-6)/12
    if -0.05<=ts<=1.05:
        hi=math.sin(math.pi*max(0,min(1,ts))); x=8+ts*144; y=64-hi*50
        core=tuple(int(lerp(c1,c2,min(1,hi*1.6))) for c1,c2 in zip((255,150,60),(255,244,170)))
        for dy in range(-6,7):
            for dx in range(-6,7):
                dd=dx*dx+dy*dy
                if dd<=16: d.point((int(x+dx),int(y+dy)),fill=core+(255,))
                elif dd<=30: d.point((int(x+dx),int(y+dy)),fill=core+(90,))
    tm=((h-18+24)%24)/12
    if tm<=1.05:
        x=8+tm*144; y=64-math.sin(math.pi*min(1,tm))*48
        for dy in range(-5,6):
            for dx in range(-5,6):
                if dx*dx+dy*dy<=22 and not (dx-3)**2+(dy+1)**2<=18: d.point((int(x+dx),int(y+dy)),fill=(246,239,196,255))
    if day>0.02:
        for cx,cy,k in CL:
            for ry,row in enumerate(CLOUD[k]):
                for kx,ch in enumerate(row):
                    if ch=="#": d.point((cx+kx,cy+ry),fill=(255,255,255,int(220*day)))
    land=Image.blend(LAND[a],LAND[b],t); im.alpha_composite(land)
    d=ImageDraw.Draw(im,"RGBA"); wa=int(255*min(1,lights*1.6)); rr=random.Random(3)
    if wa>4:
        for (x,y) in WIN:
            if rr.random()<lights: d.point((x,y),fill=(255,216,74,wa))
    return im
f=ImageFont.truetype("/tmp/PressStart2P.ttf",8)
labels=["01:30","04:30","07:30","10:30","13:30","16:30","19:30","22:30"]
sheet=Image.new("RGB",(4*324+12,2*196+12),(20,16,40))
for i,l in enumerate(labels):
    hh,mm=map(int,l.split(":")); fr=frame(hh*60+mm).resize((320,180),Image.NEAREST)
    d=ImageDraw.Draw(fr); d.fontmode="1"; d.text((7,7),l,font=f,fill=(26,20,48)); d.text((6,6),l,font=f,fill=(255,216,74))
    sheet.paste(fr.convert("RGB"),(6+(i%4)*324,6+(i//4)*196))
sheet.save("/tmp/ph/sky-8-sessions.png")
strip=Image.new("RGB",(6*244+12,150),(20,16,40))
for i,m in enumerate([16*60+30,17*60+15,17*60+50,18*60+20,18*60+50,19*60+30]):
    fr=frame(m).resize((240,135),Image.NEAREST); d=ImageDraw.Draw(fr); d.fontmode="1"; s=f"{m//60:02d}:{m%60:02d}"
    d.text((6,6),s,font=f,fill=(26,20,48)); d.text((5,5),s,font=f,fill=(255,216,74))
    strip.paste(fr.convert("RGB"),(6+i*244,8))
strip.save("/tmp/ph/sky-transition.png")
