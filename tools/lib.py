from PIL import Image, ImageDraw
import math, random
W,H=384,40
B4=[[0,8,2,10],[12,4,14,6],[3,11,1,9],[15,7,13,5]]
def hx(h): h=h.lstrip('#'); return tuple(int(h[i:i+2],16) for i in (0,2,4))
def new(): return Image.new("RGB",(W,H))
def mixc(a,b,t): return tuple(int(round(a[i]+(b[i]-a[i])*t)) for i in range(3))
def vgrad(im,cols,y0=0,y1=H):
    c=[hx(x) for x in cols]; n=len(c)-1
    for y in range(y0,y1):
        f=(y-y0)/max(1,(y1-y0-1))*n; i=min(int(f),n-1); fr=f-i
        for x in range(W):
            im.putpixel((x,y), c[i+1] if fr>B4[y%4][x%4]/16 else c[i])
def stars(im,n,y1,seed,cols=("#ffffff","#fff1a8","#bfe3ff")):
    r=random.Random(seed)
    for _ in range(n):
        x,y=r.randrange(W),r.randrange(y1); im.putpixel((x,y),hx(r.choice(cols)))
def ridge(seed,base,amp,rough=0.55,n=W):
    N=1
    while N<n: N*=2
    r=random.Random(seed); pts=[0.0]*(N+1); step=N
    pts[0]=r.uniform(-1,1); pts[N]=r.uniform(-1,1); scale=1.0
    while step>1:
        half=step//2
        for i in range(half,N,step):
            pts[i]=(pts[i-half]+pts[i+half])/2+r.uniform(-1,1)*scale
        step=half; scale*=rough
    return [int(base-amp*p) for p in pts[:n]]
def fill_ridge(im,hs,col,y1=H):
    d=ImageDraw.Draw(im)
    for x,h in enumerate(hs): d.line((x,max(0,h),x,y1-1),fill=hx(col) if isinstance(col,str) else col)
def disk(im,cx,cy,r,col,glow=None,glow_r=0):
    for y in range(int(cy-r-glow_r)-1,int(cy+r+glow_r)+2):
        for x in range(int(cx-r-glow_r)-1,int(cx+r+glow_r)+2):
            if not(0<=x<W and 0<=y<H): continue
            d=math.hypot(x-cx,y-cy)
            if d<=r: im.putpixel((x,y),hx(col) if isinstance(col,str) else col)
            elif glow and d<=r+glow_r:
                t=1-(d-r)/glow_r
                if t*0.9>B4[y%4][x%4]/16: im.putpixel((x,y),hx(glow))
def reflect(im,y0,tint=(0,0,40),k=0.55,seed=1,ripple=True):
    r=random.Random(seed)
    for y in range(y0,H):
        src=y0-(y-y0)-1
        if src<0: src=0
        off=r.choice([-1,0,0,1]) if ripple else 0
        for x in range(W):
            c=im.getpixel(((x+off)%W,src)); im.putpixel((x,y),mixc(c,tint,k))
def sparkle(im,x,y,col="#ffffff"):
    for dx,dy in [(0,0),(1,0),(-1,0),(0,1),(0,-1)]:
        if 0<=x+dx<W and 0<=y+dy<H: im.putpixel((x+dx,y+dy),hx(col))
