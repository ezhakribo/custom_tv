from PIL import Image
from collections import Counter
im=Image.open('src.png').convert('RGB'); px=im.load()
bg=(37,30,72)
def isfg(c): return abs(c[0]-bg[0])+abs(c[1]-bg[1])+abs(c[2]-bg[2])>40
fgpix=[px[x,y] for y in range(150,640,2) for x in range(20,1390,2) if isfg(px[x,y])]
side=int(len(fgpix)**0.5)+1; sample=Image.new("RGB",(side,side)); sample.putdata(fgpix+[fgpix[0]]*(side*side-len(fgpix)))
palimg=sample.quantize(colors=24,method=Image.Quantize.MEDIANCUT,dither=Image.Dither.NONE)
q=im.quantize(palette=palimg,dither=Image.Dither.NONE); qi=q.load()
pal=palimg.getpalette(); P=[tuple(pal[i*3:i*3+3]) for i in range(24)]
frames=[(29,281),(305,556),(581,832),(851,1103),(1127,1384)]
TH=56; Y0,Y1=158,628; s=(Y1-Y0)/TH; TW=32
res=Image.new("RGBA",(TW*5,TH),(0,0,0,0))
for k,(a,b) in enumerate(frames):
    cx=(a+b)/2; x0=cx-TW*s/2
    for j in range(TH):
        for i in range(TW):
            cnt=Counter(); tot=0; nfg=0
            for yy in range(int(Y0+j*s+1),int(Y0+(j+1)*s)):
                for xx in range(int(x0+i*s+1),int(x0+(i+1)*s)):
                    if 0<=xx<im.width:
                        tot+=1
                        if isfg(px[xx,yy]): nfg+=1; cnt[qi[xx,yy]]+=1
            if tot and nfg/tot>0.45: res.putpixel((k*TW+i,j),P[cnt.most_common(1)[0][0]]+(255,))
OUT=(20,14,30,255); out=res.copy()
for y in range(TH):
    for x in range(TW*5):
        if res.getpixel((x,y))[3]==0:
            for dx,dy in((1,0),(-1,0),(0,1),(0,-1)):
                X,Y=x+dx,y+dy
                if 0<=X<TW*5 and 0<=Y<TH and X//TW==x//TW and res.getpixel((X,Y))[3]: out.putpixel((x,y),OUT); break
out.save("hero_strip.png")
a=Image.new("RGBA",(TW*5*7,TH*7),(37,30,72,255)); a.alpha_composite(out.resize((TW*5*7,TH*7),Image.NEAREST))
b=Image.new("RGBA",(TW*5*7,TH*7),(255,180,90,255)); b.alpha_composite(out.resize((TW*5*7,TH*7),Image.NEAREST))
c=Image.new('RGBA',(a.width,a.height*2)); c.paste(a,(0,0)); c.paste(b,(0,a.height)); c.save('compare.png')
