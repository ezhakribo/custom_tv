from lib import *

def aurora():
    im=new(); vgrad(im,["#020414","#06122a","#0b1d3a","#123050"],0,26); stars(im,70,22,1)
    for x in range(W):
        for band,(col,ph,amp,top) in enumerate([("#3dff9a",0,3,4),("#5af0ff",1.7,4,7),("#b07af0",3.1,3,3)]):
            y=int(top+amp*math.sin(x/23+ph)+2*math.sin(x/7+ph*2))
            for k in range(10):
                if (k/10)<0.75 and (1-k/10)>B4[(y+k)%4][x%4]/16*1.2 and 0<=y+k<26: im.putpixel((x,y+k),hx(col))
    hs=ridge(5,19,9,0.58); fill_ridge(im,hs,"#1a2a4a",26)
    for x,h in enumerate(hs):
        for y in range(max(0,h),min(26,h+2)): im.putpixel((x,y),hx("#dfe8f5"))
    fill_ridge(im,ridge(6,24,3,0.55),"#0e1a30",26)
    d=ImageDraw.Draw(im); d.rectangle((0,26,W,H),fill=hx("#0b1d3a")); reflect(im,26,(0,10,30),0.45,3)
    return im,"AURORA UTARA"

def synthwave():
    im=new(); vgrad(im,["#12002a","#3a0a5a","#8a1a7a","#ff4f8a","#ffb04a"],0,24)
    stars(im,30,10,2)
    cx,cy,r=192,22,15
    for y in range(cy-r,cy+1):
        for x in range(cx-r,cx+r+1):
            if math.hypot(x-cx,y-cy)<=r and 0<=y<H:
                band=(cy-y)
                if band<9 and (band%3==0): continue
                im.putpixel((x,y),mixc(hx("#ffe14a"),hx("#ff3fa0"),(y-(cy-r))/r))
    fill_ridge(im,ridge(9,22,5,0.55),"#2a0a4a",24)
    d=ImageDraw.Draw(im); d.rectangle((0,24,W,H),fill=hx("#14002a"))
    for y in [24,25,27,30,34,39]: d.line((0,y,W,y),fill=hx("#ff3fd0"))
    for k in range(-30,31):
        x0=192+k*6; x1=192+k*40; d.line((x0,24,x1,H),fill=hx("#b030ff"))
    return im,"SENJA SYNTHWAVE"

def galaxy():
    im=new(); vgrad(im,["#03020c","#0a0620","#12082c","#0a0620"],0,H); stars(im,160,H,3)
    r=random.Random(4); cx,cy=150,20
    for i in range(1400):
        arm=i%2; t=r.random()*4.2; ang=t*2.2+arm*math.pi+r.gauss(0,0.25); rad=t*14+r.gauss(0,2)
        x=int(cx+math.cos(ang)*rad*2.2); y=int(cy+math.sin(ang)*rad*0.55)
        if 0<=x<W and 0<=y<H:
            im.putpixel((x,y),hx(r.choice(["#b07af0","#5ad1e6","#ffffff","#ff8fd0"]) if t>1 else "#fff4d0"))
    disk(im,cx,cy,3,"#fffbe8","#ffd0a0",5)
    disk(im,300,12,6,"#e07a4a"); [im.putpixel((x,12),hx("#b05a30")) for x in range(295,306)]
    disk(im,60,30,3,"#5a8ad8"); disk(im,345,32,2,"#d0d0e0")
    return im,"GALAKSI SPIRAL"

def blackhole():
    im=new(); vgrad(im,["#000000","#05030c","#000000"],0,H); stars(im,140,H,5)
    cx,cy=192,20
    for y in range(H):
        for x in range(W):
            dx,dy=(x-cx)/3.2,(y-cy)*1.0; d=math.hypot(dx,dy)
            ring=abs(d-11)
            if ring<2.6 and d>7:
                t=1-ring/2.6; im.putpixel((x,y),mixc(hx("#ff6a1a"),hx("#fff2c0"),t) if t>B4[y%4][x%4]/16 else hx("#7a1a00"))
            disk_band=abs(y-cy)<=1.2 and 7<abs(x-cx)<70
            if disk_band:
                t=1-abs(x-cx)/70; im.putpixel((x,y),mixc(hx("#ff4a00"),hx("#fff6d0"),t))
            if d<7.5: im.putpixel((x,y),(0,0,0))
    return im,"CAKRAWALA PERISTIWA"

def reef():
    im=new(); vgrad(im,["#5ad1f0","#1e8ad0","#0c4a90","#062a5a"],0,H)
    d=ImageDraw.Draw(im); r=random.Random(6)
    for k in range(9):
        x0=r.randrange(W)
        for y in range(0,30):
            for w in range(3):
                x=x0+w+y//2
                if 0<=x<W and (y+x)%3: im.putpixel((x,y),mixc(im.getpixel((x,y)),(255,255,255),0.18))
    fill_ridge(im,ridge(7,36,2,0.4),"#d8c08a")
    for i in range(40):
        x=r.randrange(W); h=r.randrange(4,12); col=r.choice(["#ff5a7a","#ffa04a","#b07af0","#ff7ad0","#3dd0a0"])
        for y in range(H-h,H-2):
            d.point((x+int(2*math.sin(y/2+i)),y),fill=hx(col)); 
            if r.random()<0.5: d.point((x+1+int(2*math.sin(y/2+i)),y),fill=hx(col))
    for i in range(14):
        x,y=r.randrange(10,W-10),r.randrange(6,28); c=hx(r.choice(["#ffd84a","#ff8a3a","#5af0ff","#ffffff"]))
        dirx=r.choice([-1,1])
        for p in [(0,0),(1,0),(2,0),(1,-1),(1,1),(-dirx*1+ (2 if dirx<0 else 0),0)]: pass
        d.rectangle((x,y,x+3,y+1),fill=c); d.point((x+(-1 if dirx>0 else 4),y),fill=c); d.point((x+(-1 if dirx>0 else 4),y+1),fill=c)
        d.point((x+(3 if dirx>0 else 0),y),fill=(20,20,40))
    for i in range(25): d.point((r.randrange(W),r.randrange(H-6)),fill=hx("#bff4ff"))
    return im,"TERUMBU KARANG"

def terraces():
    im=new(); vgrad(im,["#ffd9a0","#ffb07a","#ff8a7a","#c0d0f0"],0,14)
    disk(im,300,9,5,"#fff4c0","#ffe0a0",4)
    cols=["#2e6a2a","#3a8a32","#4aa83a","#62c048","#86d65a","#aee070"]
    for k in range(10):
        base=14+k*3; hs=[int(base+2*math.sin(x/(18+k*3)+k)+(k%2)) for x in range(W)]
        fill_ridge(im,hs,cols[k%len(cols)])
        for x in range(W):
            if 0<=hs[x]<H and x%2==0: im.putpixel((x,hs[x]),hx("#bfe8ff"))
    d=ImageDraw.Draw(im)
    for px in [40,90,250,340]:
        d.line((px,8,px+1,22),fill=hx("#1a2a1a"))
        for a in range(6):
            ang=a*math.pi/3; d.line((px,8,px+int(5*math.cos(ang)),8+int(3*math.sin(ang))),fill=hx("#1a3a1a"))
    return im,"SAWAH TERASERING"

def volcano():
    im=new(); vgrad(im,["#04030e","#0a0a24","#1a1240","#3a1a4a"],0,H); stars(im,90,30,8)
    r=random.Random(9)
    for i in range(500):
        t=r.random(); x=int(t*W); y=int(30-t*28+r.gauss(0,3))
        if 0<=y<H: im.putpixel((x,y),hx(r.choice(["#ffffff","#d0c0ff","#ffd0f0","#a0d0ff"])))
    hs=[int(40-max(0,30-abs(x-200)*0.32)) for x in range(W)]
    fill_ridge(im,hs,"#140c1a")
    for x in range(188,213): 
        for y in range(9,12): im.putpixel((x,y),hx("#ff6a1a") if (x+y)%2 else hx("#ffd04a"))
    for i in range(80):
        x=200+int(r.gauss(0,4)+i*0.4); y=8-int(i*0.1)
        if 0<=y<H and 0<=x<W: im.putpixel((x,y),hx("#5a4a6a"))
    fill_ridge(im,ridge(10,36,2,0.5),"#0a0610")
    return im,"GUNUNG API & BIMA SAKTI"

def neon():
    im=new(); vgrad(im,["#0a0420","#2a0f4a","#6a1f6a","#a0306a"],0,31); r=random.Random(11); d=ImageDraw.Draw(im)
    x=0
    while x<W:
        w=r.randrange(10,26); h=r.randrange(14,36); top=30-h+r.randrange(0,6)
        d.rectangle((x,top,x+w,30),fill=hx("#0a0614")); d.line((x,top,x+w,top),fill=hx("#3a2a5a"))
        for wy in range(top+2,29,3):
            for wx in range(x+2,x+w-1,3):
                if r.random()<0.35: d.point((wx,wy),fill=hx(r.choice(["#ffd84a","#5af0ff","#ff5ad0"])))
        if r.random()<0.6:
            c=r.choice(["#ff2fd0","#2fffe0","#ff8a2f","#8a5aff"]); sy=top+r.randrange(1,6)
            d.rectangle((x+2,sy,x+w-3,sy+2),outline=hx(c))
        x+=w+r.randrange(1,4)
    d.rectangle((0,31,W,H),fill=hx("#0a0616")); reflect(im,31,(10,0,30),0.5,12)
    for i in range(110):
        rx,ry=r.randrange(W),r.randrange(H-2); d.line((rx,ry,rx-1,ry+2),fill=hx("#6a7ab0"))
    return im,"KOTA NEON HUJAN"

def floating():
    im=new(); vgrad(im,["#5aa0f0","#8ac4fa","#c4e4ff","#ffffff"],0,H); r=random.Random(13); d=ImageDraw.Draw(im)
    for i in range(10):
        cx,cy=r.randrange(W),r.randrange(22,38)
        for k in range(6): d.ellipse((cx-8+k*3,cy-2,cx-2+k*3,cy+3),fill=hx("#ffffff"))
    for cx,cy,w in [(70,14,30),(200,10,44),(320,18,26)]:
        for y in range(0,12):
            half=int(w/2*(1-y/12)**1.3)
            d.line((cx-half,cy+y,cx+half,cy+y),fill=hx("#6a4a3a" if y>1 else "#4aa83a"))
        d.line((cx-w//2,cy,cx+w//2,cy),fill=hx("#62c048"))
        for t in range(3): d.ellipse((cx-w//3+t*w//3-3,cy-6,cx-w//3+t*w//3+3,cy),fill=hx("#2e8a3a"))
        wx=cx+w//4
        for y in range(cy+1,H): 
            if y%2==0: d.point((wx,y),fill=hx("#e0f6ff"))
            d.point((wx+1,y),fill=hx("#bfe8ff"))
    return im,"PULAU MELAYANG"

def lighthouse():
    im=new(); vgrad(im,["#0a0e1a","#1a2234","#2a3448"],0,28); r=random.Random(14); d=ImageDraw.Draw(im)
    for i in range(6):
        cx,cy=r.randrange(W),r.randrange(2,14)
        for k in range(8): d.ellipse((cx-10+k*3,cy-2,cx-4+k*3,cy+3),fill=hx("#3a4458"))
    bx=lambda x,y: im.putpixel((x,y),hx("#fff6c0"))
    for x in range(260,W):
        span=(x-260)*0.18
        for y in range(int(12-span),int(12+span)+1):
            if 0<=y<28 and (x+y)%2==0: im.putpixel((x,y),mixc(im.getpixel((x,y)),(255,246,192),0.35))
    d.rectangle((250,10,260,28),fill=hx("#e8e8f0")); d.rectangle((250,15,260,17),fill=hx("#e8343b")); d.rectangle((250,21,260,23),fill=hx("#e8343b"))
    d.rectangle((248,7,262,10),fill=hx("#2a2a3a")); d.rectangle((252,4,258,7),fill=hx("#ffe060"))
    fill_ridge(im,[28+int(2*math.sin(x/9)) for x in range(W)],"#1a2a44")
    for x in range(W):
        for y in range(28,H):
            if (x*3+y*7)%11==0: im.putpixel((x,y),hx("#8aa0c0"))
    pts=[(90,0),(86,8),(92,9),(84,20),(90,20),(82,28)]
    for (a,b),(c,e) in zip(pts,pts[1:]): d.line((a,b,c,e),fill=hx("#fffde0"))
    return im,"MERCUSUAR BADAI"

def autumn():
    im=new(); vgrad(im,["#ffd8a8","#ffc08a","#f0a080"],0,H); r=random.Random(15); d=ImageDraw.Draw(im)
    for layer,(cols,base) in enumerate([(["#c85a2a","#e07a3a"],14),(["#e8902a","#f0b04a","#d0482a"],20),(["#b8302a","#e8602a","#ffb03a"],26)]):
        for i in range(70):
            cx=r.randrange(W); cy=base+r.randrange(-4,4); rad=r.randrange(4,8)
            d.ellipse((cx-rad,cy-rad,cx+rad,cy+rad),fill=hx(r.choice(cols)))
    d.rectangle((0,30,W,H),fill=hx("#3a6a9a")); reflect(im,30,(20,40,80),0.4,16)
    for i in range(40): d.point((r.randrange(W),r.randrange(30)),fill=hx("#ffe0a0"))
    return im,"HUTAN MUSIM GUGUR"

def fireflies():
    im=new(); vgrad(im,["#020a0a","#06181a","#0a2420"],0,H); r=random.Random(17); d=ImageDraw.Draw(im)
    stars(im,40,14,18)
    for layer,(col,base,hmin) in enumerate([("#0c2a24",24,10),("#081c18",30,14),("#040e0c",38,18)]):
        x=-5
        while x<W:
            h=r.randrange(hmin,hmin+10); w=h//2
            d.polygon([(x,base),(x+w,base-h),(x+2*w,base)],fill=hx(col)); x+=r.randrange(4,10)
        d.rectangle((0,base,W,H),fill=hx(col))
    for i in range(70):
        x,y=r.randrange(W),r.randrange(8,H-2)
        d.point((x,y),fill=hx("#eaff7a"))
        for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]:
            if 0<=x+dx<W and 0<=y+dy<H and r.random()<0.7: im.putpixel((x+dx,y+dy),mixc(im.getpixel((x+dx,y+dy)),hx("#b8ff4a"),0.5))
    return im,"KUNANG-KUNANG"

def oceansunset():
    im=new(); vgrad(im,["#2a1a5a","#8a3a7a","#ff6a5a","#ffb05a","#ffe08a"],0,24)
    disk(im,192,21,8,"#fff0b0","#ffd07a",6); d=ImageDraw.Draw(im)
    d.rectangle((0,24,W,H),fill=hx("#3a2a6a"))
    for y in range(24,H):
        span=6+(y-24)*0.8
        for x in range(int(192-span),int(192+span)):
            if (x+y)%3 and 0<=x<W: im.putpixel((x,y),hx("#ffd07a") if (x*7+y)%5 else hx("#fff0b0"))
        for x in range(W):
            if (x*5+y*3)%13==0: im.putpixel((x,y),hx("#5a3a8a"))
    for bx in [70,300]:
        d.polygon([(bx,22),(bx,10),(bx+7,22)],fill=hx("#1a0a2a")); d.polygon([(bx-6,23),(bx+10,23),(bx+7,25),(bx-3,25)],fill=hx("#1a0a2a"))
    for i,(x,y) in enumerate([(120,8),(126,7),(132,9),(250,6),(256,5)]):
        d.point([(x,y),(x+1,y-1),(x+2,y)],fill=hx("#2a1a3a"))
    return im,"SAMUDRA SENJA"

def mirrorlake():
    im=new(); vgrad(im,["#9ab0e8","#c8b8e8","#f4c8d0","#ffe0c8"],0,22)
    fill_ridge(im,ridge(21,13,10,0.58),"#7a7ab0",22); fill_ridge(im,ridge(22,18,5,0.55),"#5a5a90",22)
    for x in range(W):
        for y in range(10,22):
            if im.getpixel((x,y))==hx("#7a7ab0") and y<12: im.putpixel((x,y),hx("#f0f0ff"))
    d=ImageDraw.Draw(im); d.rectangle((0,22,W,H),fill=0); reflect(im,22,(60,60,110),0.3,23,ripple=False)
    for x in range(0,W,7): im.putpixel((x,30+(x//7)%5),hx("#ffffff"))
    return im,"DANAU CERMIN FAJAR"

def orbit():
    im=new(); vgrad(im,["#000000","#02030a"],0,H); stars(im,120,H,24)
    cx,cy,R=192,330,320
    for y in range(H):
        for x in range(W):
            d=math.hypot(x-cx,y-cy)
            if d<R:
                t=(R-d)
                im.putpixel((x,y),hx("#1a5ad0") if t>3 else hx("#5ac8ff"))
                if t>6 and ((x*13+y*7)%23<5): im.putpixel((x,y),hx("#3a9a4a"))
                if t>4 and ((x*5+y*11)%17<3): im.putpixel((x,y),hx("#ffffff"))
            elif d<R+4:
                if (1-(d-R)/4)>B4[y%4][x%4]/16: im.putpixel((x,y),hx("#4ab0ff"))
    disk(im,330,14,2,"#ffffff","#ffe0a0",7)
    for k in range(-14,15): 
        if 0<=330+k<W: im.putpixel((330+k,14),hx("#fff4d0"))
    return im,"BUMI DARI ORBIT"

def mandala():
    im=new(); pal=[hx(c) for c in ["#1a0a3a","#5a1a8a","#c03aa0","#ff7a5a","#ffd84a","#5af0d0","#2a6ad0"]]
    for y in range(H):
        for x in range(W):
            tx=x%40-20; ty=y-20
            a=math.atan2(ty,tx); r=math.hypot(tx,ty)
            v=math.sin(r*0.9)+math.cos(a*8+r*0.3)+math.sin((x//40)*1.3)
            i=int((v+3)/6*len(pal)+B4[y%4][x%4]/16*0.6)%len(pal)
            im.putpixel((x,y),pal[i])
    return im,"MANDALA KALEIDOSKOP"

def balloons():
    im=new(); vgrad(im,["#7ab8f0","#a8d4f8","#e0f0ff","#fff0d8"],0,H); r=random.Random(25); d=ImageDraw.Draw(im)
    fill_ridge(im,ridge(26,30,6,0.55),"#7aa0c0"); fill_ridge(im,ridge(27,35,3,0.5),"#4a7a5a")
    for i,(x,y,s) in enumerate([(40,12,5),(110,6,4),(170,16,6),(240,8,4),(300,14,5),(355,5,3)]):
        cols=[["#e8343b","#ffd84a"],["#26b257","#ffffff"],["#4a7af0","#ffd84a"],["#ff7ad0","#ffffff"],["#ff8a2a","#5a3aa0"],["#5ad1e6","#e8343b"]][i]
        for yy in range(-s,s+1):
            half=int(math.sqrt(max(0,s*s-yy*yy))*1.0)
            for xx in range(-half,half+1):
                im.putpixel((x+xx,y+yy),hx(cols[(xx+half)//max(1,(half+1)//2)%2]))
        for yy in range(s+1,s+3): d.line((x-s//2+ (yy-s-1),y+yy,x+s//2-(yy-s-1),y+yy),fill=hx(cols[0]))
        d.line((x-1,y+s+3,x-1,y+s+5),fill=hx("#3a2a1a")); d.line((x+1,y+s+3,x+1,y+s+5),fill=hx("#3a2a1a"))
        d.rectangle((x-1,y+s+6,x+1,y+s+7),fill=hx("#7a4a2a"))
    return im,"BALON UDARA"

def whale():
    im=new(); vgrad(im,["#04081a","#0a1636","#14285a","#1e3a7a"],0,24); stars(im,60,20,28)
    disk(im,250,13,10,"#f6f0d0","#c8d0f0",6); d=ImageDraw.Draw(im)
    for (x,y) in [(246,10),(252,16),(256,9)]: im.putpixel((x,y),hx("#d8d0b0"))
    d.rectangle((0,26,W,H),fill=hx("#0a1a40"))
    for y in range(26,H):
        for x in range(238,262):
            if (x+y)%3==0: im.putpixel((x,y),hx("#c8d0f0"))
    body=[(150,25),(158,16),(170,10),(184,9),(196,12),(204,18),(200,20),(190,17),(178,17),(168,21),(162,26)]
    d.polygon(body,fill=hx("#0a0e24")); d.polygon([(150,25),(142,20),(146,27)],fill=hx("#0a0e24"))
    d.point((190,13),fill=hx("#fff6d0"))
    r=random.Random(29)
    for i in range(40):
        x=r.randrange(140,212); y=r.randrange(18,27); d.point((x,y),fill=hx("#dfe8ff"))
    return im,"PAUS DI BAWAH BULAN"

def flowers():
    im=new(); vgrad(im,["#5aa8f0","#8ac8fa","#d0ecff"],0,H); r=random.Random(30); d=ImageDraw.Draw(im)
    for i in range(6):
        cx,cy=r.randrange(W),r.randrange(3,12)
        for k in range(5): d.ellipse((cx-6+k*3,cy-2,cx+k*3,cy+2),fill=hx("#ffffff"))
    fill_ridge(im,[int(20+3*math.sin(x/30)) for x in range(W)],"#62c048")
    fill_ridge(im,[int(26+2*math.sin(x/22+1)) for x in range(W)],"#4aa83a")
    for i in range(700):
        x=r.randrange(W); y=r.randrange(22,H)
        im.putpixel((x,y),hx(r.choice(["#ff5a7a","#ffd84a","#ffffff","#b07af0","#ff8a3a","#ff7ad0"])))
    return im,"PADANG BUNGA"

def ringplanet():
    im=new(); vgrad(im,["#1a0a2a","#3a1a4a","#8a3a5a","#d0705a"],0,28); stars(im,50,20,31)
    cx,cy,R=120,14,11
    for y in range(H):
        for x in range(W):
            dx,dy=x-cx,(y-cy)
            if math.hypot(dx,dy)<=R:
                band=int((dy+R)/3)%3; im.putpixel((x,y),hx(["#f0c080","#d8a060","#ffe0a0"][band]))
            e=math.hypot(dx/2.6,dy*1.6)
            if 11.5<e<15 and not (dy<0 and math.hypot(dx,dy)<=R):
                im.putpixel((x,y),hx("#e8d8c0") if (x+y)%3 else hx("#b8a890"))
    disk(im,300,8,3,"#d8d8f0"); disk(im,330,15,2,"#f0b0a0")
    fill_ridge(im,ridge(32,30,4,0.55),"#7a2a2a"); fill_ridge(im,ridge(33,34,3,0.5),"#4a1a1a")
    for x in range(W):
        for y in range(28,H):
            if (x*7+y*3)%19==0: im.putpixel((x,y),hx("#a04a3a"))
    return im,"PLANET BERCINCIN"

THEMES=[aurora,synthwave,galaxy,blackhole,reef,terraces,volcano,neon,floating,lighthouse,autumn,fireflies,oceansunset,mirrorlake,orbit,mandala,balloons,whale,flowers,ringplanet]
