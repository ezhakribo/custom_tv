from themes import *
import os
from PIL import ImageDraw, ImageFont
out="/tmp/ph/PixelHome/app/src/main/res/drawable-nodpi"; names=[]
f=ImageFont.truetype("/tmp/PressStart2P.ttf",8)
sheet=Image.new("RGB",(2*(W*2+8)+8,10*(H*2+8)+8),(20,16,40))
for i,t in enumerate(THEMES):
    im,name=t(); im.save(f"{out}/slide{i:02d}.png"); names.append(name)
    big=im.resize((W*2,H*2),Image.NEAREST); d=ImageDraw.Draw(big); d.fontmode="1"
    d.text((5,5),f"{i+1:02d} {name}",font=f,fill=(0,0,0)); d.text((4,4),f"{i+1:02d} {name}",font=f,fill=(255,216,74))
    sheet.paste(big,(8+(i%2)*(W*2+8),8+(i//2)*(H*2+8)))
sheet.save("/tmp/ph/slides/sheet.png")
open("/tmp/ph/PixelHome/app/src/main/res/raw/slide_names.txt","w").write("\n".join(names))
print(len(names),"slides")
