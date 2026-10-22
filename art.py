from PIL import Image, ImageDraw
import random, math
random.seed(7)
W,H=64,36
def hexc(h): h=h.lstrip('#'); return tuple(int(h[i:i+2],16) for i in (0,2,4))+(255,)
def base(top,bot):
    im=Image.new("RGBA",(W,H)); d=ImageDraw.Draw(im); t,b=hexc(top),hexc(bot)
    for y in range(H):
        f=y/(H-1)
        for x in range(W):
            # 4-level ordered dither between the two colours
            bayer=[[0,2],[3,1]][y%2][x%2]/4
            im.putpixel((x,y), b if f>0.35+bayer*0.4 else t)
    d.rectangle((0,0,W-1,H-1),outline=hexc("#0b0820"))
    for _ in range(6):
        x,y=random.randrange(3,W-3),random.randrange(3,H-3); im.putpixel((x,y),(255,255,255,90))
    return im,d
OUT=hexc("#1a1430"); WHITE=hexc("#ffffff")

def youtube():
    im,d=base("#d63a3f","#8e1b22")
    d.rectangle((17,5,46,30),fill=hexc("#d9d9e3"),outline=OUT)
    d.rectangle((20,8,37,27),fill=hexc("#5ad1e6"),outline=OUT)
    for y in range(9,27,2): d.line((21,y,36,y),fill=hexc("#47b5ca"))
    d.polygon([(26,12),(26,23),(32,17)],fill=WHITE)
    d.point([(42,10),(42,14)],fill=OUT); d.point([(41,10),(41,14)],fill=hexc("#6b6b80"))
    for y in range(19,28,2): d.line((40,y,44,y),fill=hexc("#6b6b80"))
    d.rectangle((20,31,23,32),fill=OUT); d.rectangle((40,31,43,32),fill=OUT)
    return im
def popcorn():
    im,d=base("#2b1a1a","#140c0c")
    top,bot=15,32
    for y in range(top,bot+1):
        f=(y-top)/(bot-top); l=int(21+3*f); r=int(42-3*f)
        for x in range(l,r+1): d.point((x,y),fill=hexc("#e8343b") if ((x-21)//3)%2==0 else hexc("#fbf3e6"))
    d.line((21,top,42,top),fill=OUT); d.line((24,bot,39,bot),fill=OUT)
    for (cx,cy) in [(23,12),(27,10),(31,12),(35,9),(39,12),(25,7),(30,6),(34,5),(38,8),(29,13),(42,13),(21,14),(33,13)]:
        d.ellipse((cx-2,cy-2,cx+2,cy+2),fill=hexc("#fff3c4"),outline=hexc("#d9a63a"))
        d.point((cx-1,cy-1),fill=WHITE)
    return im
def star(d,cx,cy,c):
    pts=[]
    for k in range(10):
        r=7 if k%2==0 else 3; a=-math.pi/2+k*math.pi/5
        pts.append((round(cx+r*math.cos(a)),round(cy+r*math.sin(a))))
    d.polygon(pts,fill=c,outline=OUT)
def wand():
    im,d=base("#2a3a9a","#0d1440")
    for i in range(14):
        x,y=19+i,31-i; d.rectangle((x,y,x+1,y+1),fill=hexc("#3a2a1a") if i<11 else WHITE)
    star(d,38,12,hexc("#ffd84a")); d.point((37,10),fill=WHITE)
    for (x,y) in [(48,6),(50,20),(27,8),(46,27),(24,20)]:
        d.point([(x,y),(x-1,y),(x+1,y),(x,y-1),(x,y+1)],fill=hexc("#bfe3ff"))
    return im
def headphones():
    im,d=base("#26b257","#11602c")
    d.arc((18,4,46,34),180,360,fill=OUT,width=4); d.arc((19,5,45,33),180,360,fill=hexc("#e8e8f0"),width=2)
    for x0 in (16,41):
        d.rectangle((x0,17,x0+7,30),fill=hexc("#232332"),outline=OUT); d.rectangle((x0+2,19,x0+3,27),fill=hexc("#5a5a78"))
    d.rectangle((33,10,34,22),fill=WHITE); d.rectangle((28,20,33,24),fill=WHITE); d.rectangle((35,10,37,12),fill=WHITE); d.point((37,13),fill=WHITE)
    return im
def oldtv():
    im,d=base("#7a4cc4","#3c2170")
    d.line((32,13,25,3),fill=OUT,width=1); d.line((32,13,41,2),fill=OUT,width=1)
    d.point([(25,3),(41,2)],fill=hexc("#ffd84a"))
    d.rectangle((18,13,46,31),fill=hexc("#9a6233"),outline=OUT)
    d.rectangle((21,16,37,28),fill=hexc("#8fa0b8"),outline=OUT)
    for y in range(17,28):
        for x in range(22,37):
            if random.random()<0.35: d.point((x,y),fill=hexc("#e9eef5") if random.random()<0.6 else hexc("#5c6880"))
    d.ellipse((39,17,44,22),fill=hexc("#e0c080"),outline=OUT); d.rectangle((40,25,43,26),fill=OUT)
    return im
def rca():
    im,d=base("#c0702a","#6e3a10")
    for cx,c in [(22,"#f2c522"),(32,"#f4f4f4"),(42,"#e0302a")]:
        d.rectangle((cx-1,0,cx,9),fill=hexc("#232332"))
        d.rectangle((cx-3,9,cx+2,22),fill=hexc(c),outline=OUT); d.line((cx-2,11,cx-2,20),fill=(255,255,255,140))
        d.rectangle((cx-2,23,cx+1,29),fill=hexc("#c8c8d4"),outline=OUT); d.rectangle((cx-1,30,cx,32),fill=hexc("#f0f0f8"))
    return im
THREE=["11110","00001","00001","01110","00001","00001","11110"]
def hdmi3():
    im,d=base("#1aa3a3","#0b5050")
    d.rectangle((19,0,24,6),fill=hexc("#232332"))
    d.rectangle((13,6,30,16),fill=hexc("#2c2c3c"),outline=OUT)
    d.polygon([(13,17),(30,17),(28,25),(15,25)],fill=hexc("#c8c8d4"),outline=OUT)
    d.rectangle((16,19,27,21),fill=hexc("#4a4a5c"))
    for r,row in enumerate(THREE):
        for c,v in enumerate(row):
            if v=="1":
                d.rectangle((38+c*3+1,7+r*3+1,38+c*3+3,7+r*3+3),fill=OUT)
                d.rectangle((38+c*3,7+r*3,38+c*3+2,7+r*3+2),fill=WHITE)
    return im
def apps():
    im=Image.new("RGBA",(24,24),hexc("#3b4a6b")); d=ImageDraw.Draw(im)
    cols=["#ffd84a","#e8343b","#26b257","#5ad1e6","#ffffff","#b07af0","#f2902a","#4a7af0","#e870b0"]
    for i,c in enumerate(cols):
        x,y=3+(i%3)*7,3+(i//3)*7; d.rectangle((x,y,x+4,y+4),fill=hexc(c))
    d.rectangle((0,0,23,23),outline=hexc("#0b0820")); return im
def gear():
    im=Image.new("RGBA",(24,24),hexc("#4a4a58")); d=ImageDraw.Draw(im); c=hexc("#dcdce8")
    for k in range(8):
        a=k*math.pi/4; x,y=12+8*math.cos(a),12+8*math.sin(a); d.rectangle((round(x)-2,round(y)-2,round(x)+1,round(y)+1),fill=c)
    d.ellipse((5,5,18,18),fill=c,outline=hexc("#9a9aac")); d.ellipse((9,9,14,14),fill=hexc("#4a4a58"))
    d.rectangle((0,0,23,23),outline=hexc("#0b0820")); return im

def digitaltv():
    im,d=base("#2f6fd6","#163a7a")
    d.rectangle((14,4,47,24),fill=hexc("#232332"),outline=OUT)            # flat panel
    d.rectangle((16,6,45,22),fill=hexc("#0f1830"))
    bars=["#ffffff","#ffd84a","#5ad1e6","#26b257","#e870b0","#e8343b","#4a7af0"]
    for i,c in enumerate(bars):
        d.rectangle((16+i*4,6,19+i*4,17),fill=hexc(c))
    d.rectangle((16,18,45,22),fill=hexc("#1a1430")); d.rectangle((18,19,27,20),fill=hexc("#ffd84a"))
    d.rectangle((28,25,33,26),fill=OUT); d.rectangle((24,27,37,27),fill=OUT)  # stand
    d.rectangle((19,29,42,33),fill=hexc("#3a3a4c"),outline=OUT)           # set-top box
    d.point([(39,31)],fill=hexc("#3dff6a")); d.rectangle((22,31,30,31),fill=hexc("#6b6b80"))
    for r in (3,6,9):                                                      # signal waves
        d.arc((49-r,13-r,49+r,13+r),305,55,fill=WHITE,width=1)
    d.rectangle((49,12,50,13),fill=WHITE)
    return im
def gamepad():
    im,d=base("#8a3fc4","#4a1f78")
    body=hexc("#f2f2f8"); sh=hexc("#b8b8cc")
    d.rounded_rectangle((12,10,52,28),radius=6,fill=body,outline=OUT)
    d.ellipse((10,16,24,32),fill=body,outline=OUT); d.ellipse((40,16,54,32),fill=body,outline=OUT)
    d.rectangle((14,12,50,26),fill=body); d.line((16,27,48,27),fill=sh)
    d.rectangle((18,16,25,18),fill=hexc("#232332")); d.rectangle((20,14,22,20),fill=hexc("#232332"))  # d-pad
    for (x,y,c) in [(43,13,"#ffd84a"),(47,17,"#26b257"),(39,17,"#e870b0"),(43,21,"#5ad1e6")]:
        d.ellipse((x-1,y-1,x+2,y+2),fill=hexc(c),outline=OUT)
    d.rectangle((29,16,31,17),fill=sh); d.rectangle((33,16,35,17),fill=sh)
    d.rectangle((28,4,35,9),fill=hexc("#ffd84a"),outline=OUT); d.point([(30,6),(33,6)],fill=OUT)  # little star badge
    return im

def background():
    w,h=160,90; im=Image.new("RGBA",(w,h)); d=ImageDraw.Draw(im)
    bands=[hexc(c) for c in ["#0a0b26","#141440","#22195a","#3a2370","#5a2f78"]]
    for y in range(h):
        f=min(y/62,0.999)*(len(bands)-1); i=int(f); frac=f-i
        for x in range(w):
            th=[[0,8,2,10],[12,4,14,6],[3,11,1,9],[15,7,13,5]][y%4][x%4]/16
            im.putpixel((x,y),bands[min(i+1,len(bands)-1)] if frac>th else bands[i])
    for _ in range(70):
        x,y=random.randrange(w),random.randrange(55); im.putpixel((x,y),hexc(random.choice(["#ffffff","#fff1a8","#bfe3ff"])))
    for (x,y) in [(20,10),(70,6),(110,22)]:
        d.point([(x-1,y),(x+1,y),(x,y-1),(x,y+1)],fill=hexc("#ffffff"))
    d.ellipse((80,5,92,17),fill=hexc("#f6efc4")); d.ellipse((83,4,95,15),fill=bands[0]) # crescent moon
    d.polygon([(0,70),(18,52),(34,64),(52,46),(74,66),(96,50),(118,64),(140,48),(160,62),(160,90),(0,90)],fill=hexc("#1c1640"))
    d.polygon([(0,78),(30,68),(60,76),(95,66),(130,76),(160,70),(160,90),(0,90)],fill=hexc("#120e2a"))
    x=4
    while x<156:
        bw,bh=random.randrange(5,10),random.randrange(6,16)
        d.rectangle((x,84-bh,x+bw,90),fill=hexc("#0a081c"))
        for wy in range(84-bh+2,88,3):
            for wx in range(x+1,min(x+bw,159),2):
                if random.random()<0.3: im.putpixel((wx,wy),hexc("#ffd84a"))
        x+=bw+random.randrange(1,4)
    return im

out={"thumb_youtube":youtube(),"thumb_netflix":popcorn(),"thumb_disney":wand(),"thumb_spotify":headphones(),
     "thumb_tv":oldtv(),"thumb_digitaltv":digitaltv(),"thumb_games":gamepad(),"thumb_apps":apps(),"thumb_settings":gear(),"bg":background()}
for k,v in out.items(): v.save(f"{k}.png")
# launcher icon + Android TV banner (320x180)
icon=Image.new("RGBA",(32,32),(0,0,0,0)); icon.paste(youtube().crop((15,3,49,35)).resize((32,32),Image.NEAREST)); icon.save("app_icon.png")
from PIL import ImageFont
f=ImageFont.truetype("/tmp/PressStart2P.ttf",8)
ban=background().crop((0,0,160,90)); d=ImageDraw.Draw(ban); d.fontmode="1"
d.text((41,39),"PIXEL HOME",font=f,fill=hexc("#1a1430")); d.text((40,38),"PIXEL HOME",font=f,fill=hexc("#ffd84a"))
ban.resize((320,180),Image.NEAREST).save("banner.png")
