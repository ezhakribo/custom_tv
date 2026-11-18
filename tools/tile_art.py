from PIL import Image, ImageDraw
import random
R="/tmp/ph/PixelHome/app/src/main/res/drawable-nodpi"
sw=Image.open("/tmp/ph/PixelHome/tools/palette.png").convert("RGB")
P=sorted({sw.getpixel((i*12+6,6)) for i in range(sw.width//12)})
def near(c): return min(P,key=lambda q:(q[0]-c[0])**2*3+(q[1]-c[1])**2*4+(q[2]-c[2])**2*2)
hx=lambda h:tuple(int(h[i:i+2],16) for i in (1,3,5))
def topal(im): out=Image.new("RGB",im.size); out.putdata([near(p) for p in im.convert("RGB").getdata()]); return out
B4=[[0,2],[3,1]]
def base(top,bot):
    im=Image.new("RGB",(64,36)); t,b=hx(top),hx(bot)
    for y in range(36):
        for x in range(64): im.putpixel((x,y), b if y/35>0.35+B4[y%2][x%2]/4*0.4 else t)
    d=ImageDraw.Draw(im); d.rectangle((0,0,63,35),outline=hx("#0b0820")); return im,d
OUT=hx("#1a1430")
# GAME: an arcade cabinet with a starry screen, a little rocket, joystick and buttons
im,d=base("#f08a3a","#a04a1a")
r=random.Random(3)
d.polygon([(20,4),(44,4),(46,8),(46,34),(18,34),(18,8)],fill=hx("#3a2a6a"),outline=OUT)      # cabinet
d.rectangle((20,2,44,6),fill=hx("#ffd84a"),outline=OUT)                                      # marquee
for x in range(23,42,3): d.point((x,4),fill=hx("#e8343b"))
d.rectangle((22,9,42,21),fill=hx("#0b0820"),outline=OUT)                                    # screen
for _ in range(14): d.point((r.randrange(23,42),r.randrange(10,21)),fill=hx("#ffffff"))
d.polygon([(30,19),(32,13),(34,19)],fill=hx("#5ad1e6")); d.point((32,20),fill=hx("#ffd84a")) # rocket
d.rectangle((20,23,44,27),fill=hx("#5a4a8a"),outline=OUT)                                   # control panel
d.line((25,22,25,24),fill=OUT); d.rectangle((24,21,26,22),fill=hx("#e8343b"))               # joystick
for x,c in [(34,"#26b257"),(38,"#ffd84a"),(42,"#5ad1e6")]: d.rectangle((x-1,24,x,25),fill=hx(c))
d.rectangle((26,29,38,32),fill=hx("#2a1f4a"),outline=OUT); d.line((30,30,34,30),fill=hx("#ffd84a"))  # coin slot
for (x,y) in [(6,6),(56,9),(9,27),(55,28)]:
    d.point([(x,y),(x-1,y),(x+1,y),(x,y-1),(x,y+1)],fill=hx("#fff1a8"))
topal(im).save(f"{R}/thumb_arcade.png")
# + TAMBAH: an empty slot to fill with any app
im,d=base("#3b4a6b","#26304a")
for x in range(8,57,4): d.line((x,6,x+1,6),fill=hx("#9fb0c8")); d.line((x,29,x+1,29),fill=hx("#9fb0c8"))
for y in range(6,30,4): d.line((8,y,8,y+1),fill=hx("#9fb0c8")); d.line((56,y,56,y+1),fill=hx("#9fb0c8"))
d.rectangle((30,11,33,24),fill=hx("#ffd84a"),outline=OUT); d.rectangle((24,16,39,19),fill=hx("#ffd84a"),outline=OUT)
d.rectangle((31,12,32,23),fill=hx("#ffd84a")); d.rectangle((25,17,38,18),fill=hx("#ffd84a"))
topal(im).save(f"{R}/thumb_add.png")
s=Image.new("RGB",(64*6*2+12,36*6),(20,16,40))
s.paste(Image.open(f"{R}/thumb_arcade.png").resize((384,216),Image.NEAREST),(0,0)); s.paste(Image.open(f"{R}/thumb_add.png").resize((384,216),Image.NEAREST),(396,0)); s.save("/tmp/ph/new-tiles.png")
