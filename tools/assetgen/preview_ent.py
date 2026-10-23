import sys
from PIL import Image, ImageDraw
from common import TEX
def sheet(names, out, scale=5, cols=3, bg=(52,46,64,255)):
    ims=[]
    for n in names:
        im=Image.open(TEX/'entity'/(n+'.png')).convert('RGBA')
        im=im.resize((im.width*scale, im.height*scale), Image.NEAREST)
        ims.append((n,im))
    cw=max(i.width for _,i in ims)+8; ch=max(i.height for _,i in ims)+18
    rows=(len(ims)+cols-1)//cols
    sh=Image.new('RGBA',(cols*cw,rows*ch),bg); d=ImageDraw.Draw(sh)
    for k,(n,im) in enumerate(ims):
        x,y=(k%cols)*cw+4,(k//cols)*ch+4
        # checker-ish backdrop for transparency
        bgim=Image.new('RGBA',im.size,(30,26,40,255)); bgim.alpha_composite(im); sh.alpha_composite(bgim,(x,y))
        d.text((x,y+im.height+2),n,fill=(230,230,250,255))
    sh.convert('RGB').save(out)
if __name__=='__main__':
    out=sys.argv[1]; scale=int(sys.argv[2]); cols=int(sys.argv[3]); sheet(sys.argv[4:],out,scale,cols)
