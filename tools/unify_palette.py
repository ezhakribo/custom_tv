# Map every image to one shared 64-colour palette (nearest colour, alpha kept).
from PIL import Image
import glob, os, random, sys
SRC=sys.argv[1]; DST=sys.argv[2]; N=int(sys.argv[3]) if len(sys.argv)>3 else 64
files=sorted(glob.glob(f"{SRC}/*.png"))
random.seed(1); sample=[]
for f in files:
    im=Image.open(f).convert("RGBA"); px=[p[:3] for p in im.getdata() if p[3]>128]
    w=1 if os.path.basename(f).startswith(("slide","sky","land")) else 3   # small sprites count more so their colours survive
    sample+=random.sample(px,min(len(px),1500))*w
side=int(len(sample)**0.5)+1; s=Image.new("RGB",(side,side)); s.putdata(sample+[sample[0]]*(side*side-len(sample)))
pal=s.quantize(colors=N-18,method=Image.Quantize.MEDIANCUT,dither=Image.Dither.NONE)
P=[tuple(pal.getpalette()[i*3:i*3+3]) for i in range(N-18)]
# colours that must survive: aurora, earth, sun, autumn, neon, foliage, UI yellow
FORCE=["#e07a3a","#c85a2a","#3a8a32","#86d65a","#aee070","#2e6a2a","#3dff9a","#5af0ff","#b07af0","#1a5ad0","#5ac8ff","#fff0b0","#ffd84a","#e8902a","#f0b04a","#ff2fd0","#62c048","#e8343b"]
P+=[tuple(int(h[i:i+2],16) for i in (1,3,5)) for h in FORCE]
cache={}
def near(c):
    if c not in cache: cache[c]=min(P,key=lambda q:(q[0]-c[0])**2*3+(q[1]-c[1])**2*4+(q[2]-c[2])**2*2)
    return cache[c]
os.makedirs(DST,exist_ok=True)
for f in files:
    im=Image.open(f).convert("RGBA"); out=Image.new("RGBA",im.size)
    out.putdata([near(p[:3])+(p[3],) if p[3]>0 else (0,0,0,0) for p in im.getdata()])
    out.save(f"{DST}/{os.path.basename(f)}")
sw=Image.new("RGB",(N*12,12))
for i,c in enumerate(P): sw.paste(c,(i*12,0,i*12+12,12))
sw.resize((N*12,24),Image.NEAREST).save(f"{DST}/../palette.png" if False else "palette.png")
print(len(files),"images mapped to",N,"colours")
