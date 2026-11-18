# IBO's family from the reference photo: one chibi template, consistent style.
from PIL import Image, ImageDraw
sw=Image.open("/tmp/ph/PixelHome/tools/palette.png").convert("RGB")
PAL=sorted({sw.getpixel((i*12+6,6)) for i in range(sw.width//12)})
K=(20,14,30); EYE=(36,24,30); WHITE=(245,242,237); BLUSH=(236,138,140)
HAIR=(22,18,26); HAIRL=(66,54,70)
SPEC={
 "dad":dict(W=30,H=52,OY=6,skin=(214,160,122),skin2=(178,124,94),hair="curly",glasses=True,
            top=(30,28,36),top2=(18,16,24),stitch=(232,232,236),sleeve=(30,28,36),sleeve2=(18,16,24),
            pants=(176,178,186),pants2=(140,142,152),legw=5,shoe=(214,214,220),shoe2=(96,98,110),watch=True,lips=(150,70,70)),
 "mom":dict(W=30,H=52,OY=6,skin=(242,200,170),skin2=(214,164,136),hair="hijab",hijab=(212,200,188),hijab2=(178,166,154),
            top=(28,28,40),top2=(16,16,26),sleeve=(244,242,236),sleeve2=(206,204,200),trim=(130,30,50),peplum=True,
            pants=(236,228,214),pants2=(204,194,178),legw=5,shoe=(150,94,64),shoe2=(110,64,40),lips=(206,90,100)),
 "kid":dict(W=24,H=38,OY=2,skin=(244,204,176),skin2=(216,168,140),hair="bangs",clip=(255,120,160),
            top=(244,190,206),top2=(214,150,172),floral=True,sleeve=(244,190,206),sleeve2=(214,150,172),
            pants=(244,190,206),pants2=(214,150,172),shorts=True,legw=3,shoe=(40,52,96),shoe2=(220,50,60),lips=(206,90,100)),
}
def geom(s):
    if s["W"]==30:
        return dict(face=(9,6,23,21),cap=(7,1,24,10),neck=(13,20,15,21),torso=(10,21,18,33),hip=33,ankle=43,ground=45,
                    shoulder=(13,22),armlen=8,eye=(15,19,13),mouth=(18,18),blush=((14,17),(22,17)),ear=(9,13,11,16),nose=(24,15))
    return dict(face=(6,5,19,17),cap=(5,1,19,8),neck=(10,16,12,17),torso=(7,17,14,25),hip=25,ankle=33,ground=35,
                shoulder=(10,18),armlen=6,eye=(12,16,10),mouth=(15,14),blush=((11,13),(18,13)),ear=(6,10,8,12),nose=(20,12))

def outline(img):
    px=img.load(); W,H=img.size; out=img.copy(); o=out.load()
    for y in range(H):
        for x in range(W):
            if px[x,y][3]==0 and any(0<=x+dx<W and 0<=y+dy<H and px[x+dx,y+dy][3] for dx,dy in((1,0),(-1,0),(0,1),(0,-1))):
                o[x,y]=K+(255,)
    return out
def curls(im,y_max):
    px=im.load(); W,H=im.size
    for cy in range(1,y_max,3):
        for cx in ((cy//3)%2*2,):
            for x0 in range(cx,W,4):
                for (x,y) in [(x0,cy),(x0+1,cy-1)]:
                    if 0<=x<W and 0<=y<H and px[x,y][:3]==HAIR: px[x,y]=HAIRL+(255,)
def floral(im,col,col2):
    px=im.load(); W,H=im.size
    for y in range(H):
        for x in range(W):
            if px[x,y][:3]==col:
                if (x+2*y)%5==0: px[x,y]=WHITE+(255,)
                elif (x*3+y)%7==0: px[x,y]=col2+(255,)

def body(d,im,s,g,view,f,OY):
    W=s["W"]; side=view=="side"
    st=[1,0,-1,0][f] if side else 0; bob=([0,-1,0,-1][f] if side else 0)+OY
    hip=g["hip"]+bob; ank=g["ankle"]+OY; ground=g["ground"]+OY; lw=s["legw"]
    SK,SK2=s["skin"],s["skin2"]
    t=g["torso"]; cx=t[0]+(t[2]-t[0])//2
    if not side: tw=t[2]-t[0]+2; t=(W//2-tw//2,t[1],W//2-tw//2+tw,t[3]); cx=W//2
    t=(t[0],t[1]+bob,t[2],t[3]+bob)
    # legs + shoes
    stride=4 if W==30 else 3
    legs=[(cx-stride*st,True),(cx+stride*st,False)] if side else [(cx-3,False),(cx+2,False)]
    for fx,back in legs:
        hx=cx-1 if back else cx+1
        if not side: hx=fx
        upper=s["pants"] if not back else s["pants2"]
        if s.get("shorts"):   # shorts then bare legs
            d.polygon([(hx-lw//2,hip),(hx+lw//2,hip),(fx+lw//2,ank),(fx-lw//2,ank)],fill=SK2 if back else SK)
            d.rectangle((min(hx,fx)-lw//2,hip,max(hx,fx)+lw//2,hip+2),fill=upper)
        else:
            d.polygon([(hx-lw//2,hip),(hx+lw//2,hip),(fx+lw//2,ank),(fx-lw//2,ank)],fill=upper)
            if not back: d.line([(hx+lw//2,hip),(fx+lw//2,ank)],fill=s["pants2"])
        sh=s["shoe2"] if back and W==30 and s["hair"]!="curly" else s["shoe"]
        toe=2 if side else 1
        d.rectangle((fx-lw//2,ank,fx+lw//2+toe,ground),fill=sh)
        d.line((fx-lw//2,ground,fx+lw//2+toe,ground),fill=s["shoe2"])
        if s["hair"]=="curly": d.point((fx,ank+1),fill=s["shoe2"])            # sneaker accent
        if s["hair"]=="bangs": d.line((fx-1,ank,fx+1,ank),fill=s["shoe2"])    # crocs strap
    # arms
    sx,sy=(g["shoulder"][0],g["shoulder"][1]+bob) if side else (0,0)
    L=g["armlen"]; a=(-3*st if W==30 else -2*st)
    def arm(sw_,col,col2,skin,front):
        d.polygon([(sx-1,sy),(sx+2,sy),(sx+2+sw_,sy+L),(sx-1+sw_,sy+L)],fill=col,outline=col2)
        sk=3 if (W==30 and not s.get("longsleeve",s["hair"]=="hijab")) else 1
        if s["hair"]=="hijab": sk=1
        d.polygon([(sx-1+sw_*0.6,sy+L-sk),(sx+2+sw_*0.6,sy+L-sk),(sx+2+sw_,sy+L),(sx-1+sw_,sy+L)],fill=skin)
        d.rectangle((sx-1+sw_,sy+L,sx+1+sw_,sy+L+1),fill=skin)
        if s.get("stitch") and W==30: d.line((sx-1+sw_*0.6,sy+L-sk-1,sx+2+sw_*0.6,sy+L-sk-1),fill=s["stitch"])
        if front and s.get("watch"): d.line((sx-1+sw_*0.8,sy+L-1,sx+2+sw_*0.8,sy+L-1),fill=K)
    if side: arm(-a,s["sleeve2"],s["sleeve2"],SK2,False)
    # torso
    d.rounded_rectangle(t,radius=2,fill=s["top"]); d.line((t[2],t[1]+2,t[2],t[3]),fill=s["top2"])
    if s.get("peplum"):
        d.polygon([(t[0],t[3]-3),(t[2],t[3]-3),(t[2]+2,t[3]+2),(t[0]-2,t[3]+2)],fill=s["top"])
        # white cardigan: back panel (side) or both front panels (front), maroon edge
        if side:
            d.rectangle((t[0],t[1],t[0]+3,t[3]-1),fill=s["sleeve"]); d.line((t[0]+4,t[1]+1,t[0]+4,t[3]-1),fill=s["trim"])
        else:
            d.rectangle((t[0],t[1],t[0]+2,t[3]+1),fill=s["sleeve"]); d.rectangle((t[2]-2,t[1],t[2],t[3]+1),fill=s["sleeve"])
            d.line((t[0]+3,t[1]+1,t[0]+3,t[3]+1),fill=s["trim"]); d.line((t[2]-3,t[1]+1,t[2]-3,t[3]+1),fill=s["trim"])
    if s.get("stitch"):   # contrast stitching: neckline + hem
        d.line((t[0]+1,t[3]-1,t[2]-1,t[3]-1),fill=s["stitch"])
        d.line((cx-2,t[1],cx+2,t[1]),fill=s["stitch"])
        d.line((t[0]+2,t[1]+4,t[0]+4,t[1]+4),fill=s["stitch"])   # pocket
    if s.get("shorts"): d.line((t[0],t[3]-1,t[2],t[3]-1),fill=s["top2"])
    if not side:
        for ax in (t[0]-2,t[2]):
            d.rectangle((ax,t[1]+1,ax+2,t[1]+L),fill=s["sleeve"],outline=s["sleeve2"])
            d.rectangle((ax,t[1]+L-1,ax+2,t[1]+L+1),fill=SK)
            if s.get("stitch"): d.line((ax,t[1]+L-3,ax+2,t[1]+L-3),fill=s["stitch"])
        if s.get("watch"): d.line((t[2],t[1]+L-1,t[2]+2,t[1]+L-1),fill=K)
    nk=g["neck"]; d.rectangle((cx-1 if not side else nk[0],nk[1]+bob,cx if not side else nk[2],nk[3]+bob),fill=SK2)
    if side: arm(a,s["sleeve"],s["sleeve2"],SK,True)
    return bob

def head(d,im,s,g,view,bob):
    W=s["W"]; side=view=="side"; SK,SK2=s["skin"],s["skin2"]
    up=lambda b:(b[0],b[1]+bob,b[2],b[3]+bob)
    f0,f1,f2,f3=up(g["face"])
    if not side: w=f2-f0; f0=(W-w)//2; f2=f0+w
    cxh=(f0+f2)//2
    if s["hair"]=="hijab":
        # scarf around the face and over the shoulders
        hb=(f0-2,f1-3,f2+1,f3+1) if side else (f0-2,f1-3,f2+2,f3+1)
        d.ellipse(hb,fill=s["hijab"])
        drape=[(hb[0]+2,f3-2),(hb[2]-1,f3-2),(hb[2],f3+4),(hb[0]+1,f3+4)] if side else [(hb[0]+2,f3-2),(hb[2]-2,f3-2),(hb[2]-1,f3+4),(hb[0]+1,f3+4)]
        d.polygon(drape,fill=s["hijab"]); d.line((drape[3][0],f3+4,drape[2][0],f3+4),fill=s["hijab2"])
        d.arc(hb,30,150,fill=s["hijab2"])
        fo=(f0+1,f1,f2,f3-1) if side else (f0+1,f1,f2-1,f3-1)   # face opening
        d.ellipse(fo,fill=SK); d.arc(fo,200,340,fill=s["hijab2"])
    else:
        d.ellipse((f0,f1,f2,f3),fill=SK); d.arc((f0,f1,f2,f3),20,160,fill=SK2)
    if s["hair"]=="curly":
        c=up(g["cap"]);
        if not side: cw=c[2]-c[0]; c=((W-cw)//2,c[1],(W-cw)//2+cw,c[3])
        d.ellipse(c,fill=HAIR)
        for i,x in enumerate(range(c[0]+1,c[2],3)):
            dy=-2 if i%2 else -1
            d.ellipse((x-2,c[1]+dy,x+2,c[1]+dy+4),fill=HAIR)                             # curly top
        if side: d.rectangle((f0+1,f1+5,f0+2,f1+9),fill=HAIR)                         # sideburn
        else: d.rectangle((f0,f1+4,f0+1,f1+8),fill=HAIR); d.rectangle((f2-1,f1+4,f2,f1+8),fill=HAIR)
        curls(im,c[3]+1)
    if s["hair"]=="bangs":
        c=up(g["cap"])
        if not side: cw=c[2]-c[0]; c=((W-cw)//2,c[1],(W-cw)//2+cw,c[3])
        d.ellipse(c,fill=HAIR)
        if side: d.rectangle((f0+3,f1+2,f2-2,f1+4),fill=HAIR); d.rectangle((f0,f1+2,f0+3,f1+8),fill=HAIR)
        else: d.rectangle((f0+2,f1+2,f2-2,f1+4),fill=HAIR); d.rectangle((f0,f1+2,f0+1,f1+8),fill=HAIR); d.rectangle((f2-1,f1+2,f2,f1+8),fill=HAIR)
        tails=[(c[0]-1,c[1]-1),(c[0]+8,c[1]-3)] if side else [(c[0]-2,c[1]),(c[2]-3,c[1])]
        for (x,y) in tails:
            d.ellipse((x,y,x+4,y+4),fill=HAIR); d.rectangle((x+3,y+3,x+4,y+4),fill=s["clip"])
    if side and s["hair"]=="curly":
        ex0,ey0,ex1,ey1=g["ear"]; d.rectangle((ex0,ey0+bob,ex1,ey1+bob),fill=SK); d.point((ex0+1,ey0+1+bob),fill=SK2)
    if side:
        e1,e2,ey=g["eye"]; ey+=bob
        if s["hair"]=="hijab": e1+=1; e2+=0
        if s.get("glasses"):
            # two round lenses (corners cut) with a gap for the bridge, small pupils inside
            for ex in (e1-1,e2):
                x0,y0=ex-1,ey
                d.line((x0+1,y0,x0+2,y0),fill=K); d.line((x0+1,y0+3,x0+2,y0+3),fill=K)
                d.line((x0,y0+1,x0,y0+2),fill=K); d.line((x0+3,y0+1,x0+3,y0+2),fill=K)
                d.rectangle((x0+1,y0+1,x0+2,y0+2),fill=SK); d.point((x0+2,y0+1),fill=EYE); d.point((x0+2,y0+2),fill=EYE)
            d.point((e1+2,ey+1),fill=K)
            d.line((g["ear"][1]+1,ey+1,e1-3,ey+1),fill=K)
        else:
            for ex in (e1,e2):
                d.line((ex,ey,ex+1,ey),fill=K); d.rectangle((ex,ey+1,ex+1,ey+2),fill=EYE); d.point((ex,ey+1),fill=WHITE)

        nx,ny=g["nose"]; d.point((nx-1,ny+bob),fill=SK)
        mx,my=g["mouth"]; d.line((mx,my+bob,mx+2,my+bob),fill=s["lips"]); d.point((mx+3,my-1+bob),fill=s["lips"])
        for (x,y) in g["blush"]: d.point((x+(1 if s["hair"]=="hijab" else 0),y+bob),fill=BLUSH)
    else:
        cx=W//2; ey=g["eye"][2]+bob+1
        for ex in (cx-4,cx+2):
            d.point((ex,ey+1),fill=K); d.point((ex+1,ey),fill=K); d.point((ex+2,ey+1),fill=K)
            if s.get("glasses"): d.rectangle((ex-1,ey-2,ex+3,ey+2),outline=K)
        if s.get("glasses"): d.line((cx-1,ey,cx+1,ey),fill=K)
        my=g["mouth"][1]+bob
        d.line((cx-2,my,cx+1,my),fill=s["lips"]); d.point((cx-3,my-1),fill=s["lips"]); d.point((cx+2,my-1),fill=s["lips"])
        if s["hair"]=="curly": d.line((cx-1,my+1,cx,my+1),fill=WHITE)   # big smile like the photo
        d.point((cx-6,ey+3),fill=BLUSH); d.point((cx+5,ey+3),fill=BLUSH)

def draw(name,view,f=0):
    s=SPEC[name]; g=geom(s); im=Image.new("RGBA",(s["W"],s["H"]),(0,0,0,0)); d=ImageDraw.Draw(im)
    bob=body(d,im,s,g,view,f,s["OY"])
    if s.get("floral"): floral(im,s["top"],s["top2"])
    head(d,im,s,g,view,bob)
    return outline(im)
def frames(name): return [draw(name,"side",f) for f in range(4)]+[draw(name,"front")]
def export(dst):
    for n in SPEC:
        fr=frames(n)
        for i in range(4): fr[i].save(f"{dst}/{n}_walk{i}.png")
        fr[4].save(f"{dst}/{n}_front.png")
if __name__=="__main__":
    Z=6; S=Image.new("RGBA",(5*32*Z,3*54*Z),(37,30,72,255))
    for r,n in enumerate(SPEC):
        for i,fr in enumerate(frames(n)):
            S.alpha_composite(fr.resize((fr.width*Z,fr.height*Z),Image.NEAREST),(i*32*Z+6,r*54*Z+(54-fr.height)*Z))
    S.save("family2_sheet.png"); print("ok")
