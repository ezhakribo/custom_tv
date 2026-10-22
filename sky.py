# 8 sessions x (sky, land) + windows list + favorite tile + 8-bit sounds
from PIL import Image, ImageDraw
import random, math, wave, struct, os
W,H=160,90; HORIZON=62
def hx(h,a=255): h=h.lstrip('#'); return tuple(int(h[i:i+2],16) for i in (0,2,4))+(a,)
BAYER=[[0,8,2,10],[12,4,14,6],[3,11,1,9],[15,7,13,5]]
S=[ # name, sky bands top->horizon, far, near, city, stars
 ("00-03 tengah malam",["#05061a","#0a0b26","#141440","#1f1a52","#2e2066"],"#151236","#0e0b24","#07061a",90),
 ("03-06 dini hari",   ["#0b1030","#1a1d52","#3a2c6e","#6a3f80","#b0607a"],"#2a2050","#1a1438","#100c26",45),
 ("06-09 pagi",        ["#5a8fd8","#86b4ec","#c8c8e0","#f4c0a0","#ffb070"],"#6a6fa8","#4a4a80","#2e2c58",0),
 ("09-12 menjelang siang",["#2f7fe0","#4a9af0","#6ab4f8","#8ccaf8","#b4e0fa"],"#5a8ab8","#3e6a94","#2a4a6e",0),
 ("12-15 siang",       ["#1e70e0","#3a8ef4","#5aa8fa","#82c2fc","#aadcff"],"#5e90c0","#40709c","#2c4e74",0),
 ("15-18 sore",        ["#3a6ac0","#6a8ad0","#c89aa8","#f2a868","#ffc24a"],"#7a5a88","#5a3e6a","#3a284a",0),
 ("18-21 petang",      ["#141440","#2a1f62","#5a2f78","#a2457a","#e0705a"],"#2a1a48","#1a1032","#0e0a20",35),
 ("21-24 malam",       ["#0a0b26","#141440","#22195a","#3a2370","#5a2f78"],"#1c1640","#120e2a","#0a081c",70),
]
def sky(bands,stars,seed):
    im=Image.new("RGB",(W,H)); b=[hx(c)[:3] for c in bands]
    for y in range(H):
        f=min(y/HORIZON,0.999)*(len(b)-1); i=int(f); fr=f-i
        for x in range(W):
            im.putpixel((x,y), b[min(i+1,len(b)-1)] if fr>BAYER[y%4][x%4]/16 else b[i])
    r=random.Random(seed)
    for _ in range(stars):
        x,y=r.randrange(W),r.randrange(54); im.putpixel((x,y),r.choice([(255,255,255),(255,241,168),(191,227,255)]))
    return im
# fixed silhouettes (same shapes every session, only colours change)
FAR=[(0,70),(18,52),(34,64),(52,46),(74,66),(96,50),(118,64),(140,48),(160,62),(160,90),(0,90)]
NEAR=[(0,78),(30,68),(60,76),(95,66),(130,76),(160,70),(160,90),(0,90)]
r=random.Random(11); BUILD=[]; WIN=[]; x=4
while x<156:
    bw,bh=r.randrange(5,10),r.randrange(6,16); BUILD.append((x,84-bh,x+bw,90))
    for wy in range(84-bh+2,88,3):
        for wx in range(x+1,min(x+bw,159),2): WIN.append((wx,wy))
    x+=bw+r.randrange(1,4)
def land(far,near,city):
    im=Image.new("RGBA",(W,H),(0,0,0,0)); d=ImageDraw.Draw(im)
    d.polygon(FAR,fill=hx(far)); d.polygon(NEAR,fill=hx(near))
    for bx in BUILD: d.rectangle(bx,fill=hx(city))
    return im
out="/tmp/ph/PixelHome/app/src/main/res/drawable-nodpi"
for i,(name,bands,far,near,city,stars) in enumerate(S):
    sky(bands,stars,100+i).save(f"{out}/sky{i}.png"); land(far,near,city).save(f"{out}/land{i}.png")
os.makedirs("/tmp/ph/PixelHome/app/src/main/res/raw",exist_ok=True)
open("/tmp/ph/PixelHome/app/src/main/res/raw/windows.txt","w").write(";".join(f"{a},{b}" for a,b in WIN))
print(len(WIN),"windows")

# favorite tile: heart with play mark
def base(top,bot):
    im=Image.new("RGBA",(64,36)); d=ImageDraw.Draw(im); t,b=hx(top),hx(bot)
    for y in range(36):
        for x in range(64): im.putpixel((x,y), b if y/35>0.35+[[0,2],[3,1]][y%2][x%2]/4*0.4 else t)
    d.rectangle((0,0,63,35),outline=hx("#0b0820")); return im,d
im,d=base("#e8508a","#8e1f50"); OUT=hx("#1a1430")
HEART=["..####...####..",".######.######.","###############","###############","###############",".#############.","..###########..","...#########...","....#######....",".....#####.....","......###......",".......#......."]
for ry,row in enumerate(HEART):
    for cx,c in enumerate(row):
        if c=="#": d.rectangle((17+cx*2,5+ry*2,18+cx*2,6+ry*2),fill=hx("#ffffff"))
d.polygon([(29,11),(29,21),(37,16)],fill=hx("#e8343b"))
for (x,y) in [(8,6),(54,8),(52,27),(10,26)]: d.point([(x,y),(x-1,y),(x+1,y),(x,y-1),(x,y+1)],fill=hx("#ffd84a"))
im.save(f"{out}/thumb_favorite.png")

# 8-bit sounds (square waves), 22.05 kHz mono 16-bit
def tone(path,notes,vol=0.18,rate=22050):
    frames=bytearray()
    for f,ms in notes:
        n=int(rate*ms/1000)
        for k in range(n):
            env=1-(k/n)**2
            v=vol*env*(1 if math.sin(2*math.pi*f*k/rate)>=0 else -1)
            frames+=struct.pack("<h",int(v*32767))
    w=wave.open(path,"wb"); w.setnchannels(1); w.setsampwidth(2); w.setframerate(rate); w.writeframes(bytes(frames)); w.close()
raw="/tmp/ph/PixelHome/app/src/main/res/raw"
tone(f"{raw}/blip.wav",[(990,35)],0.12); tone(f"{raw}/select.wav",[(660,45),(1320,90)],0.16)
print("ok")
