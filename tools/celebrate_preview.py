from PIL import Image, ImageDraw, ImageFont
import math, random
R="/tmp/ph/PixelHome/app/src/main/res/drawable-nodpi/"
L=lambda n: Image.open(R+n+".png").convert("RGBA")
def M(walk,front,cheer,surprise,delay): return dict(walk=walk,front=front,cheer=cheer,surprise=surprise,delay=delay,x=None,face=1,step=0,moving=False)
fam=[M([L(f"dad_walk{i}") for i in range(4)],L("dad_front"),[L("dad_cheer0"),L("dad_cheer1")],None,0),
     M([L(f"mom_walk{i}") for i in range(4)],L("mom_front"),[L("mom_cheer0"),L("mom_cheer1")],None,11),
     M([L(f"kid_walk{i}") for i in range(4)],L("kid_front"),[L("kid_cheer0"),L("kid_cheer1")],None,20),
     M([L(f"cat_a{i}") for i in range(4)],None,[L(f"cat_a_dance{i}") for i in range(3)],L("cat_a_surprise"),28),
     M([L(f"cat_b{i}") for i in range(4)],None,[L(f"cat_b_dance{i}") for i in range(3)],L("cat_b_surprise"),34)]
VW,VH,k=960,240,2; CEL=64; INK=(26,20,48)
bg=L("slide09").crop((60,0,60+320,40)).resize((VW,VH),Image.NEAREST)
font=ImageFont.truetype("/tmp/PressStart2P.ttf",8*k)
hist=[]; dirn=1; speed=3*k; dad=fam[0]; dadW=30*k; dad["x"]=VW*0.62
for i in range(63,-1,-1): hist.append(dad["x"]-i*speed)
conf=[]; CC=[(255,216,74),(232,52,59),(90,209,230),(38,178,87),(255,122,176),(255,255,255)]
rnd=random.Random(4)
def sprite(img,b,left,feet,mirror=False,lift=0):
    s=b.resize((b.width*k,b.height*k),Image.NEAREST)
    if mirror: s=s.transpose(Image.FLIP_LEFT_RIGHT)
    sw=0.4 if lift else 0.6
    img.alpha_composite(Image.new("RGBA",(int(s.width*sw),k*2),(0,0,0,85)),(int(left+s.width*(0.5-sw/2)),int(feet-k)))
    img.alpha_composite(s,(int(left),int(feet-s.height-lift)))
frames=[]; cel=-1
for T in range(150):
    if T==40: cel=0
    if cel>=0:
        cel+=1
        if cel in (12,24):
            for _ in range(48 if cel==12 else 24):
                conf.append([bx+rnd.random()*bw, by+rnd.random()*bh, (rnd.random()*2-1)*2*k, -(1+rnd.random()*3)*k, rnd.randrange(6)])
        for c in conf: c[0]+=c[2]; c[1]+=c[3]; c[3]+=0.7*k; c[2]*=0.95
        if cel>=CEL: cel=-1; conf=[]
    else:
        dad["step"]=(dad["step"]+1)%4; dad["x"]+=dirn*speed; dad["face"]=dirn; dad["moving"]=True; hist.append(dad["x"])
        for m in fam[1:]:
            tgt=hist[max(0,len(hist)-1-m["delay"])]; d=0 if m["x"] is None else tgt-m["x"]
            m["moving"]=abs(d)>0.5
            if m["moving"]: m["face"]=1 if d>0 else -1; m["step"]=(m["step"]+1)%4
            m["x"]=tgt
    img=bg.copy(); feet=VH-8; dr=ImageDraw.Draw(img); dr.fontmode="1"
    if cel>=0:
        t=cel; cs=[m["x"]+dadW/2 for m in fam[:3]]
        tw=dr.textlength("WELCOME TO OUR FAMILY",font=font); bw=tw+12*k; bh=16*k; by=feet-54*k-bh
        bx=max(4*k,min(VW-bw-4*k,(min(cs)+max(cs))/2-bw/2))
        op=0 if t<8 else (t-8)/8 if t<16 else 1 if t<CEL-8 else (CEL-t)/8
        if op>0:
            for i in range(2):
                hand=fam[i]["x"]+dadW/2+(11 if i==0 else -11)*k
                dr.rectangle((hand-k/2,by+bh,hand+k/2+k,feet-38*k),fill=(122,74,42))
        for i in range(4,-1,-1):
            m=fam[i]; lift=0; mir=False
            if m["front"] is not None: b=m["cheer"][(t//3)%2] if 8<=t<CEL-8 else m["front"]
            elif t<10: b=m["surprise"]; lift=(5-abs(t-2.5)*2)*k*2 if t<5 else 0
            elif t<CEL-6: b=m["cheer"][[0,1,2,1][(t//2)%4]]; lift=((t//2)%2)*2*k; mir=((t//8)%2)==1
            else: b=m["walk"][1]; mir=m["face"]<0
            left=m["x"]+(dadW-b.width*k)/2; sprite(img,b,left,feet,mir,lift); dr=ImageDraw.Draw(img); dr.fontmode="1"
            if m["front"] is None:
                top=feet-b.height*k-lift
                if t<10:
                    cx=left+b.width*k/2; dr.text((cx-4*k+k,top-2*k-8*k+k),"!",font=font,fill=INK); dr.text((cx-4*k,top-2*k-8*k),"!",font=font,fill=(255,216,74))
                elif t<CEL-6:
                    nx=left+(-4 if (t//4)%2==0 else b.width+1)*k; ny=top-((t%8)+2)*k
                    dr.rectangle((nx+2*k,ny,nx+3*k-1,ny+5*k),fill=(255,255,255)); dr.rectangle((nx,ny+4*k,nx+3*k-1,ny+6*k),fill=(255,255,255)); dr.rectangle((nx+3*k,ny,nx+5*k-1,ny+k-1),fill=(255,255,255))
        if op>0:
            w=bw*op; x0=bx+(bw-w)/2
            dr.rectangle((x0-k,by-k,x0+w+k,by+bh+k),fill=INK); dr.rectangle((x0,by,x0+w,by+bh),fill=(255,241,168))
            dr.rectangle((x0,by,x0+w,by+k-1),fill=(232,52,59)); dr.rectangle((x0,by+bh-k,x0+w,by+bh),fill=(232,52,59))
            layer=Image.new("RGBA",img.size,(0,0,0,0)); ld=ImageDraw.Draw(layer); ld.fontmode="1"
            ld.text((bx+7*k,by+bh/2-4*k+k),"WELCOME TO OUR FAMILY",font=font,fill=(255,176,192)); ld.text((bx+6*k,by+bh/2-4*k),"WELCOME TO OUR FAMILY",font=font,fill=(232,52,59))
            mask=Image.new("L",img.size,0); ImageDraw.Draw(mask).rectangle((x0,by,x0+w,by+bh),fill=255)
            img.paste(layer,(0,0),Image.composite(layer.split()[3],Image.new("L",img.size,0),mask))
        dr=ImageDraw.Draw(img)
        for c in conf: dr.rectangle((c[0],c[1],c[0]+k*1.5,c[1]+k*1.5),fill=CC[c[4]])
    else:
        for m in reversed(fam):
            b=m["walk"][m["step"]] if m["moving"] else (m["front"] or m["walk"][1])
            sprite(img,b,m["x"]+(dadW-b.width*k)/2,feet,(m["moving"] or m["front"] is None) and m["face"]<0)
    frames.append(img.convert("RGB"))
pal=frames[60].quantize(colors=230,method=Image.Quantize.MEDIANCUT)
q=[f.quantize(palette=pal,dither=Image.Dither.NONE) for f in frames]
q[0].save("/tmp/ph/perayaan-keluarga.gif",save_all=True,append_images=q[1:],duration=125,loop=0)
S=Image.new("RGB",(VW,4*(VH+4)))
for n,i in enumerate([20,44,62,80]): S.paste(frames[i],(0,n*(VH+4)))
S.save("/tmp/ph/perayaan-steps.png"); print("ok")
