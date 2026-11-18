from PIL import Image, ImageDraw, ImageFont
R="/tmp/ph/PixelHome/app/src/main/res/drawable-nodpi"
sw=Image.open("/tmp/ph/PixelHome/tools/palette.png").convert("RGB")
P=sorted({sw.getpixel((i*12+6,6)) for i in range(sw.width//12)})
def near(c): return min(P,key=lambda q:(q[0]-c[0])**2*3+(q[1]-c[1])**2*4+(q[2]-c[2])**2*2)
def topal(im):
    im=im.convert("RGBA"); out=Image.new("RGBA",im.size)
    out.putdata([near(p[:3])+(255,) if p[3]>0 else (0,0,0,0) for p in im.getdata()]); return out
hx=lambda h:tuple(int(h[i:i+2],16) for i in (1,3,5))
# 24x24 icon: yellow sticky note with lines and a red pin, on slate (like APPS/SETTINGS icons)
ic=Image.new("RGB",(24,24),hx("#3b4a6b")); d=ImageDraw.Draw(ic)
d.rectangle((0,0,23,23),outline=hx("#0b0820"))
d.rectangle((5,5,19,19),fill=hx("#fff1a8")); d.polygon([(15,19),(19,15),(19,19)],fill=hx("#e8d080"))
for y in (9,12,15): d.line((7,y,16 if y<15 else 12,y),fill=hx("#6b6b80"))
d.rectangle((11,3,13,5),fill=hx("#e8343b")); d.point((12,6),fill=hx("#6b6b80"))
topal(ic).save(f"{R}/thumb_notes.png")
# 160x90 banner, shown 2x: night sky + three sticky notes + title
sky=Image.open(f"{R}/sky7.png").convert("RGBA").copy(); land=Image.open(f"{R}/land7.png").convert("RGBA"); sky.alpha_composite(land)
d=ImageDraw.Draw(sky)
for (x,y,c,a) in [(28,22,"#fff1a8",-1),(64,18,"#ffc8d8",0),(100,24,"#bfe8ff",1)]:
    d.rectangle((x+2,y+2,x+30,y+30),fill=hx("#1a1430")); d.rectangle((x,y,x+28,y+28),fill=hx(c))
    for k in range(3): d.line((x+4,y+8+k*6,x+22-k*4,y+8+k*6),fill=hx("#6b6b80"))
    d.rectangle((x+13,y-2,x+15,y),fill=hx("#e8343b"))
f=ImageFont.truetype("/tmp/PressStart2P.ttf",8); d.fontmode="1"
for t,yy in (("CATATAN",62),("KELUARGA",73)):
    w=d.textlength(t,font=f); d.text((80-w/2+1,yy+1),t,font=f,fill=hx("#1a1430")); d.text((80-w/2,yy),t,font=f,fill=hx("#ffd84a"))
topal(sky).resize((320,180),Image.NEAREST).save(f"{R}/banner_notes.png")
prev=Image.new("RGB",(320+24*6+20,180),(20,16,40)); prev.paste(Image.open(f"{R}/banner_notes.png").convert("RGB"),(0,0)); prev.paste(Image.open(f"{R}/thumb_notes.png").convert("RGB").resize((144,144),Image.NEAREST),(330,18)); prev.save("/tmp/ph/notes-art.png")
print(len(P),"palette colours")
