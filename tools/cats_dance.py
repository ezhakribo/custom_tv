# Standing cats: surprised + 3 dance frames, for the orange and the tuxedo cat.
from PIL import Image, ImageDraw
sw=Image.open("/tmp/ph/PixelHome/tools/palette.png").convert("RGB")
P=sorted({sw.getpixel((i*12+6,6)) for i in range(sw.width//12)})
def near(c): return min(P,key=lambda q:(q[0]-c[0])**2*3+(q[1]-c[1])**2*4+(q[2]-c[2])**2*2)
K=(20,14,30)
CATS={"a":dict(fur=(240,150,60),dark=(196,96,30),belly=(255,244,230),eye=(40,30,20)),
      "b":dict(fur=(52,52,66),dark=(36,36,48),belly=(240,240,248),eye=(250,220,90))}
def outline(img):
    px=img.load(); W,H=img.size; out=img.copy(); o=out.load()
    for y in range(H):
        for x in range(W):
            if px[x,y][3]==0 and any(0<=x+dx<W and 0<=y+dy<H and px[x+dx,y+dy][3] for dx,dy in((1,0),(-1,0),(0,1),(0,-1))):
                o[x,y]=K+(255,)
    return out
def cat(c,pose):
    """pose: 'surprise', 'dance0' (left paw up), 'dance1' (both up), 'dance2' (right paw up)."""
    im=Image.new("RGBA",(18,18),(0,0,0,0)); d=ImageDraw.Draw(im)
    lean={"dance0":-1,"dance2":1}.get(pose,0)
    # tail curling up behind
    d.line([(12+lean,13),(15,11),(15,7),(14,5)],fill=c["fur"],width=2)
    # legs and body
    d.rectangle((6,14,7,16),fill=c["fur"]); d.rectangle((10,14,11,16),fill=c["fur"])
    d.ellipse((5+lean,8,12+lean,15),fill=c["fur"]); d.ellipse((7+lean,10,10+lean,14),fill=c["belly"])
    # arms
    up_l = pose in ("dance0","dance1"); up_r = pose in ("dance2","dance1")
    if pose=="surprise":
        d.line([(5,10),(3,12)],fill=c["fur"],width=2); d.line([(12,10),(14,12)],fill=c["fur"],width=2)
    # head
    hx=lean
    d.ellipse((4+hx,2,13+hx,10),fill=c["fur"])
    d.polygon([(4+hx,4),(5+hx,0),(7+hx,3)],fill=c["fur"]); d.polygon([(10+hx,3),(12+hx,0),(13+hx,4)],fill=c["fur"])
    d.point((5+hx,2),fill=(255,140,160)); d.point((12+hx,2),fill=(255,140,160))
    d.point((8+hx,3),fill=c["dark"]); d.point((9+hx,3),fill=c["dark"]); d.point((8+hx,4),fill=c["dark"])   # forehead marking
    d.ellipse((7+hx,6,10+hx,9),fill=c["belly"])                                        # muzzle
    if pose=="surprise":   # big round eyes, little "o" mouth
        for ex in (6,10): d.rectangle((ex+hx,4,ex+1+hx,5),fill=(255,255,255)); d.point((ex+hx,5),fill=K)
        d.point((8+hx,8),fill=K); d.point((9+hx,8),fill=K)
    else:                  # happy closed eyes, smile; paws drawn in front of the head
        for ex in (6,10): d.point((ex+hx,5),fill=c["eye"]); d.point((ex+1+hx,4),fill=c["eye"])
        d.point((8+hx,8),fill=(255,140,160))
        for sx,tx,up in ((6+lean,1+lean,up_l),(11+lean,16+lean,up_r)):
            d.line([(sx,10),(tx,2 if up else 12)],fill=c["fur"],width=2)
            if up: d.rectangle((tx-1,1,tx,2),fill=c["belly"])
    out=outline(im)
    if pose=="surprise":   # puffed fur: little spikes on the outline
        o=out.load()
        for (x,y) in [(3,7),(14,7),(4,11),(13,11),(2,9),(15,9)]:
            if o[x,y][3]==0: o[x,y]=K+(255,)
    q=Image.new("RGBA",out.size); q.putdata([near(p[:3])+(255,) if p[3] else (0,0,0,0) for p in out.getdata()]); return q
def export(dst):
    for k,c in CATS.items():
        cat(c,"surprise").save(f"{dst}/cat_{k}_surprise.png")
        for i in range(3): cat(c,f"dance{i}").save(f"{dst}/cat_{k}_dance{i}.png")
if __name__=="__main__":
    Z=10; S=Image.new("RGBA",(4*20*Z,2*20*Z),(37,30,72,255))
    for r,(k,c) in enumerate(CATS.items()):
        for i,pz in enumerate(["surprise","dance0","dance1","dance2"]):
            S.alpha_composite(cat(c,pz).resize((180,180),Image.NEAREST),(i*200+10,r*200+10))
    S.save("cats2_sheet.png"); print("ok")
