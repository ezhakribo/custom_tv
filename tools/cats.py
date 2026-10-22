from PIL import Image
# facing right. k outline, o fur, s stripe/dark fur, w white, p pink, e eye, g green eye-glint
BASE=[
"..k...........k.k.",
".kok.........kokok",
".kok.........kooook",
]
CAT_TOP=[
"..............k...k.",
".kk..........kok.kok",
"kok.........kooooook",
"kok.........koeooeok",
".kok........kowpwook",
"..kokkkkkkkkkowwwok.",
"...koooooooooowwwk..",
"...koososososooowk..",
"...kooooooooooooook.",
]
LEGS=[
["....kok.kok.kok.kok.", "...kk...kk...kk...kk"],
[".....kok..kok.kok...", ".....kk...kk..kk...."],
["....kok.kok.kok.kok.", "....kk.kk...kk.kk..."],
[".....kok..kok.kok...", ".....kk...kk..kk...."],
]
for r in CAT_TOP: assert len(r)==20,(r,len(r))
for f in LEGS:
    for r in f: assert len(r)==20,(r,len(r))
PALS={
 "oranye":{'k':(26,20,48),'o':(240,150,60),'s':(196,96,30),'w':(255,244,230),'p':(255,140,160),'e':(40,30,20)},
 "tuxedo":{'k':(18,14,24),'o':(52,52,66),'s':(40,40,52),'w':(240,240,248),'p':(255,140,160),'e':(250,220,90)},
}
def frame(pal,i):
    rows=CAT_TOP+LEGS[i]; im=Image.new("RGBA",(20,len(rows)),(0,0,0,0))
    for y,r in enumerate(rows):
        for x,ch in enumerate(r):
            if ch!='.': im.putpixel((x,y),PALS[pal][ch]+(255,))
    return im
if __name__=="__main__":
    big=Image.new("RGBA",(8*20*8+80,2*11*8+40),(37,30,72,255))
    for r,p in enumerate(PALS):
        for i in range(4): big.alpha_composite(frame(p,i).resize((160,88),Image.NEAREST),(i*180+10,r*100+10))
    big.save("cats.png")
