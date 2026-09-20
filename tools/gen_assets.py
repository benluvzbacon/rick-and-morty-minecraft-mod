#!/usr/bin/env python3
"""Procedurally generates ALL mod assets: textures, models, blockstates, sounds,
lang, loot tables, recipes, dimension JSONs, damage types, biome stubs via scripts.
Original artwork only - no copyrighted material. Deterministic output."""
import json, math, os, random, struct, wave

ROOT = os.path.dirname(os.path.abspath(__file__))
REPO = ROOT if os.path.basename(ROOT) != 'tools' else os.path.dirname(ROOT)
SRC = os.path.join(REPO, 'src', 'main', 'resources')
NS = 'rickmorty'
random.seed(137)

def p(*a): return os.path.join(SRC, *a)

def ensure(d): os.makedirs(d, exist_ok=True)

# ---------------- tiny PNG writer (no third-party deps) ----------------
import zlib
def write_png(path, pixels, w, h):
    """pixels: list of rows of (r,g,b,a)"""
    def chunk(t, data):
        c = t + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
    raw = b''.join(b'\x00' + b''.join(bytes(bytearray(int(max(0,min(255,v))) for v in px[:4])) for px in row) for row in pixels)
    png = (b'\x89PNG\r\n\x1a\n'
           + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))
           + chunk(b'IDAT', zlib.compress(raw, 9))
           + chunk(b'IEND', b''))
    ensure(os.path.dirname(path))
    with open(path, 'wb') as f:
        f.write(png)

def canvas(w, h, color=(0, 0, 0, 0)):
    return [[color for _ in range(w)] for _ in range(h)]

def fill(img, x0, y0, x1, y1, c):
    h, w = len(img), len(img[0])
    for y in range(max(0,y0), min(h,y1)):
        for x in range(max(0,x0), min(w,x1)):
            img[y][x] = c

def noise_tint(img, base_c, amt=14, region=None):
    h, w = len(img), len(img[0])
    x0,y0,x1,y1 = region or (0,0,w,h)
    for y in range(y0, min(h,y1)):
        for x in range(x0, min(w,x1)):
            n = random.randint(-amt, amt)
            if len(base_c)==3: base_c = base_c + (255,)
            r,g,b,a = base_c
            img[y][x] = (max(0,min(255,r+n)), max(0,min(255,g+n)), max(0,min(255,b+n)), a)

def noise_img(w, h, c, amt=14):
    img = canvas(w, h)
    noise_tint(img, c, amt)
    return img

def dimg(img, x, y, c):
    if 0 <= y < len(img) and 0 <= x < len(img[0]): img[y][x] = c

def swirl(w, h, c1, c2, frame=0, frames=1, ring_detail=True):
    """spiral vortex texture, animated across frames"""
    img = canvas(w, h)
    cx, cy = (w-1)/2, (h-1)/2
    for y in range(h):
        for x in range(w):
            dx, dy = x-cx, y-cy
            dist = math.sqrt(dx*dx+dy*dy)
            ang = math.atan2(dy, dx)
            bands = math.sin(ang*3 + dist*0.55 - frame*1.3)
            t = (bands+1)/2
            fade = max(0, 1 - dist/(w*0.62))
            r = int(c1[0]*(1-t) + c2[0]*t)
            g = int(c1[1]*(1-t) + c2[1]*t)
            b = int(c1[2]*(1-t) + c2[2]*t)
            a = int(255 * max(0.0, min(1.0, 0.35 + t*0.65 * fade)) )
            if ring_detail and 2 < dist < 2.6 + 0.2*math.sin(frame):
                r,g,b = c2[0], c2[1], c2[2]; a = 255
            img[y][x] = (r,g,b, min(255,max(0,a)))
    return img

def save_animation(path, w, frame_h, make_frame, frames, frametime=3):
    """stack frames vertically + write .mcmeta"""
    full = canvas(w, frame_h*frames)
    for f in range(frames):
        fr = make_frame(f)
        for y in range(frame_h):
            for x in range(w):
                full[f*frame_h+y][x] = fr[y][x]
    write_png(path, full, w, frame_h*frames)
    with open(path + '.mcmeta', 'w') as fh:
        json.dump({"animation": {"frametime": frametime, "interpolate": False}}, fh)

# =====================================================================
# TEXTURES
# =====================================================================
TX = p('assets', NS, 'textures')
blocks = {}
items = {}
guis = {}
ents = {}

def block_tex(name, img):
    blocks[name] = img

def item_tex(name, img):
    items[name] = img

GREEN=(57,255,136,255); BLUE=(80,170,255,255); PURPLE=(170,80,255,255)
DARKG=(18,120,66,255); DARKB=(26,70,140,255); DARKP=(80,20,140,255)

# --- blocks ---
block_tex('sci_fi_metal', noise_img(16,16,(96,102,118,255),10))
b=blocks['sci_fi_metal']
for i in range(0,16,5): fill(b,i,0,i+1,16,(70,75,90,255))
fill(b,0,0,16,1,(130,138,156,255)); fill(b,0,15,16,16,(52,56,68,255))
dimg(b,8,8,(30,255,150,255))  # status light

block_tex('citadel_metal', noise_img(16,16,(58,64,78,255),8))
b=blocks['citadel_metal']
for i in range(0,16,8): fill(b,i,0,i+1,16,(40,44,56,255))
fill(b,2,2,4,4,(57,255,136,80))

block_tex('dark_matter_block', noise_img(16,16,(14,10,24,255),10))
b=blocks['dark_matter_block']
for _ in range(26): dimg(b, random.randint(0,15), random.randint(0,15), (random.randint(120,220),80,255,255))

block_tex('alien_rock', noise_img(16,16,(92,62,96,255),16))
b=blocks['alien_rock']
for _ in range(8):
    x,y=random.randint(0,13),random.randint(0,13)
    fill(b,x,y,x+2,y+1,(120,84,128,255))

block_tex('pocket_rock', noise_img(16,16,(88,88,100,255),14))

# crystal cluster: light-blue crystal shape on transparent bg
img = canvas(16,16)
def crystal_polygon(img, cx, cy, size, c):
    for i in range(size):
        w = int(size*0.6*(1-i/size)) + 1
        fill(img, cx-w, cy-i, cx+w+1, cy-i+1, c)
# three shards
crystal_polygon(img, 7, 14, 8, (130,220,255,255))
crystal_polygon(img, 3, 14, 5, (100,190,240,230))
crystal_polygon(img, 12, 15, 5, (150,230,255,230))
block_tex('crystal_cluster', img)

# quartz cluster: pale green glow crystals
img = canvas(16,16)
crystal_polygon(img, 8, 14, 9, (57,255,136,255))
crystal_polygon(img, 3, 14, 5, (40,220,110,230))
crystal_polygon(img, 12, 15, 4, (90,255,170,230))
block_tex('quartz_cluster', img)

# ores: rock base + gems
for ore_name, base, gem in [
    ('alien_crystal_ore', (92,62,96,255), (130,220,255,255)),
    ('quartz_matter_ore', (88,88,100,255), (57,255,136,255)),
    ('citadel_remains_ore', (58,64,78,255), (200,200,220,255)),
    ('dark_matter_ore', (14,10,24,255), (170,80,255,255)),
]:
    img = noise_img(16,16,base,14)
    for _ in range(7):
        x,y=random.randint(1,12),random.randint(1,12)
        fill(img,x,y,x+3,y+3,gem)
        dimg(img,x,y,(255,255,255,255))
    block_tex(ore_name, img)

# lab/machine blocks
def machine(name, top=(78,86,102,255), accent=GREEN):
    b = noise_img(16,16,top,8)
    fill(b,0,0,16,2,(120,128,144,255)); fill(b,0,14,16,16,(48,52,62,255))
    fill(b,1,0,2,16,(48,52,62,255)); fill(b,14,0,15,16,(48,52,62,255))
    dimg(b,7,7,accent); dimg(b,8,8,accent); dimg(b,7,8,(255,255,255,255))
    block_tex(name, b)
    return b

machine('rick_workbench_top', (96,102,118,255), GREEN)
w = noise_img(16,16,(70,76,90,255),8)
fill(w,0,0,16,2,(100,108,122,255)); fill(w,0,14,16,16,(44,48,58,255))
for i in range(5): dimg(w,4+i*2,6+i,GREEN)
block_tex('rick_workbench_side', w)
machine('portal_machine_top',(88,74,120,255), PURPLE)
p_ = noise_img(16,16,(64,54,88,255),10)
fill(p_,3,3,13,13,(40,32,58,255)); fill(p_,5,5,11,11,(26,20,40,255))
dimg(p_,7,7,PURPLE); dimg(p_,8,8,PURPLE) ; dimg(p_,7,8,(240,200,255,255))
block_tex('portal_machine_side', p_)
machine('quantum_computer', (56,62,74,255), (0,220,255,255))
q = blocks['quantum_computer']
for i in range(6): dimg(q, 3+i*2, 4+((i*3)%8), (0,260-i*20 if False else 230,255,255))
machine('alien_reactor_side', (70,66,80,255), (255,150,40,255))
r = noise_img(16,16,(46,44,56,255),8)
fill(r,4,4,12,12,(30,28,40,255))
noise_tint(r,(255,150,40,255),40,(6,6,10,10))
block_tex('alien_reactor_top', r)
machine('containment_chamber', (40,120,60,255), GREEN)
c = blocks['containment_chamber']
noise_tint(c,(30,80,40,255),20,(4,4,12,12))
machine('dimensional_stabilizer', (70,90,110,255), BLUE)
machine('lifeform_detector', (90,80,70,255), (255,230,0,255))
machine('plumbus_machine', (110,86,96,255), (255,120,180,255))
machine('portal_synthesizer', (60,80,70,255), GREEN)

# lab glass: frame + transparent middle
g = canvas(16,16,(180,240,255,18))
for i in range(16):
    g[0][i]=(150,220,250,200); g[15][i]=(150,220,250,200); g[i][0]=(150,220,250,200); g[i][15]=(150,220,250,200)
dimg(g,3,3,(210,245,255,60)); dimg(g,12,12,(210,245,255,60)); dimg(g,4,13,(210,245,255,60))
block_tex('lab_glass', g)

# portal fluid tank: frame + fill strip (fill states generated as separate textures)
def tank_frame():
    t = canvas(16,16)
    for i in range(16):
        t[0][i]=(110,116,130,255); t[15][i]=(110,116,130,255)
        t[i][0]=(110,116,130,255); t[i][15]=(110,116,130,255)
    dimg(t,1,1,(140,146,160,255)); dimg(t,14,1,(140,146,160,255))
    dimg(t,1,14,(140,146,160,255)); dimg(t,14,14,(140,146,160,255))
    return t
block_tex('portal_fluid_tank_frame', tank_frame())
f = canvas(16,16)
noise_tint(f, (40,220,110,255), 30)
block_tex('portal_fluid', f)

# portal block animated textures
def portal_frame(col, dark):
    return lambda fr: swirl(16,16, col, dark, fr, 6)
save_animation(os.path.join(TX,'block','portal_green.png'), 16, 16, portal_frame(GREEN, DARKG), 6, 3)
save_animation(os.path.join(TX,'block','portal_blue.png'), 16, 16, portal_frame(BLUE, DARKB), 6, 3)
save_animation(os.path.join(TX,'block','portal_purple.png'), 16, 16, portal_frame(PURPLE, DARKP), 6, 3)
# unstable portal: green/purple corrupted flicker
def unstable_frame(fr):
    img = canvas(16,16)
    for y in range(16):
        for x in range(16):
            n=random.random()
            if n<0.34: img[y][x]=GREEN
            elif n<0.5: img[y][x]=PURPLE
            elif n<0.62: img[y][x]=(255,255,255,180)
            else: img[y][x]=(10,6,14,255)
    return img
save_animation(os.path.join(TX,'block','unstable_portal.png'), 16, 16, unstable_frame, 4, 2)

# meeseeks box
m = noise_img(16,16,(120,130,220,255),8)
noise_tint(m,(180,190,255,255),10,(4,4,12,12))
dimg(m,7,7,(255,255,255,255)); dimg(m,8,8,(255,255,255,255))
block_tex('meeseeks_box_top', m)
ms = noise_img(16,16,(90,100,190,255),8)
fill(ms,0,0,16,2,(120,130,220,255)); fill(ms,0,14,16,16,(60,68,140,255))
block_tex('meeseeks_box_side', ms)

# stone like block textures for dimensions
block_tex('alien_stone', noise_img(16,16,(102,72,112,255),12))
block_tex('alien_dirt', noise_img(16,16,(84,54,88,255),16))
block_tex('alien_grass_top', noise_img(16,16,(64,178,116,255),22))
block_tex('citadel_stone', noise_img(16,16,(74,80,92,255),10))
block_tex('citadel_floor', noise_img(16,16,(58,64,78,255),8))
b=blocks['citadel_floor']
fill(b,0,0,16,1,(88,94,108,255)); fill(b,0,15,16,16,(44,48,60,255)); fill(b,8,8,9,9,GREEN)
block_tex('pocket_stone', noise_img(16,16,(96,96,110,255),12))

# containment core crystal
cc = canvas(16,16)
for y in range(16):
    for x in range(16):
        d=abs(x-7.5)+abs(y-7.5)
        v=max(0,255-int(d*30))
        cc[y][x]=(v//4, min(255,v), min(255, v//16*14), 255 if v>10 else 0)
block_tex('containment_core', cc)

# --- items ---
def gun_body(base, glow):
    img = canvas(16,16)
    # barrel + body + handle silhouette
    fill(img, 2, 6, 13, 9, base)
    fill(img, 12, 5, 14, 8, (160,168,180,255))   # muzzle tip
    fill(img, 5, 9, 8, 13, (44,48,58,255))       # handle
    fill(img, 3, 4, 10, 6, (70,78,92,255))       # top shroud
    # energy core
    dimg(img, 6, 7, glow); dimg(img, 7, 7, (255,255,255,255)); dimg(img, 8, 7, glow)
    # barrel coil
    for i in range(3): dimg(img, 10+i, 5, glow)
    dimg(img, 13, 6, glow)
    return img
item_tex('portal_gun', gun_body((210,214,222,255), GREEN))
item_tex('portal_blaster', gun_body((78,86,100,255), GREEN))
item_tex('laser_gun', gun_body((90,50,60,255), (255,60,60,255)))
item_tex('plasma_launcher', gun_body((70,90,70,255), GREEN))
item_tex('dimension_eraser', gun_body((30,30,40,255), PURPLE))
item_tex('shrink_ray', gun_body((120,120,140,255), (120,180,255,255)))
item_tex('force_field_generator', gun_body((60,70,90,255), BLUE))

# grappling hook
img = canvas(16,16)
fill(img, 6, 2, 10, 8, (90,96,110,255))
dimg(img, 5, 2, (90,96,110,255)); dimg(img, 10, 2, (90,96,110,255))
dimg(img, 4, 3, GREEN); dimg(img, 11, 3, GREEN)
fill(img, 7, 8, 9, 14, (60,64,76,255))
dimg(img, 7, 14, (200,200,220,255)); dimg(img, 8, 14, (200,200,220,255))
item_tex('grappling_hook', img)

# jetpack: propane-style backpack
img = canvas(16,16)
fill(img, 4, 3, 12, 14, (36,38,44,255))
fill(img, 6, 1, 10, 5, (200,204,212,255))
fill(img, 2, 5, 4, 12, (220,80,40,255)); fill(img, 12, 5, 14, 12, (220,80,40,255))  # tanks
dimg(img, 4, 4, (255,140,60,255)); dimg(img, 11, 10, (255,220,60,255))  # gauges/flame
fill(img, 6, 6, 10, 12, (30,32,38,255))
item_tex('jetpack', img)

# scanner: tricorder
img = canvas(16,16)
fill(img, 3, 2, 13, 14, (48,52,64,255))
fill(img, 4, 3, 12, 8, (20,40,30,255))
for i in range(6): dimg(img, 5+i, 4+((i*7)%3), GREEN)
fill(img, 5, 10, 11, 13, (70,76,90,255))
dimg(img, 7, 11, (0,220,255,255)); dimg(img, 9, 11, (255,200,0,255))
item_tex('interdimensional_scanner', img)

# portal stabilizer: floor ring with emitter
img = canvas(16,16)
fill(img, 2, 2, 14, 14, (78,84,100,255))
fill(img, 4, 4, 12, 12, (30,34,44,255))
fill(img, 6, 6, 10, 10, (210,214,222,255))
dimg(img, 7, 7, GREEN); dimg(img, 8, 8, GREEN); dimg(img, 8, 7, GREEN); dimg(img, 7, 8, (255,255,255,255))
dimg(img, 3, 3, GREEN); dimg(img, 12, 3, GREEN); dimg(img, 3, 12, GREEN); dimg(img, 12, 12, GREEN)
item_tex('portal_stabilizer', img)

# materials
def chip(name, c):
    img = canvas(16,16)
    fill(img, 4, 5, 12, 11, (40,44,56,255))
    fill(img, 5, 6, 11, 10, c)
    for i in range(4):
        dimg(img, 4+i*2, 3, (180,180,190,255)); dimg(img, 4+i*2, 12, (180,180,190,255))
    dimg(img, 6, 7, (255,255,255,255))
    item_tex(name, img)
def vial(name, liquid):
    img = canvas(16,16)
    fill(img, 6, 2, 10, 4, (150,150,160,255))
    fill(img, 5, 4, 11, 13, (200,230,240,230))
    fill(img, 6, 7, 10, 12, liquid)
    dimg(img, 6, 4, (255,255,255,255))
    item_tex(name, img)
def orb(name, c, highlight=(255,255,255,255)):
    img = canvas(16,16)
    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x-cx)**2+(y-cy)**2)
            if d < 5.5:
                v = max(0.0, 1 - d/6)
                img[y][x] = (int(c[0]*(0.35+0.65*v)), int(c[1]*(0.35+0.65*v)), int(c[2]*(0.35+0.65*v)), 255)
    dimg(img, 5, 5, highlight); dimg(img, 6, 5, highlight)
    item_tex(name, img)

chip('microverse_battery', (57,255,136,255))
chip('energy_cell', (200,80,40,255))
orb('plasma_core', GREEN)
orb('advanced_technology', (0,200,255,255))
orb('cosmic_crystal', (170,120,255,255))
orb('quantum_processor', (240,240,250,255))
orb('dimensional_shard', GREEN)
vial('portal_fluid_canister', (40,220,110,255))
vial('filled_portal_fluid_canister', (60,255,130,255))
vial('empty_canister', (60,66,80,255))
vial('cronenberg_sample', (200,110,80,255))
chip('alien_crystal_shim', (130,220,255,255))

# rift shard: droplet
img = canvas(16,16)
for y in range(16):
    for x in range(16):
        dx=(x-7.5)/5.5; dy=(y-8)/6
        if dx*dx+dy*dy<=1:
            v=1-(abs(dx)+abs(dy))/2
            img[y][x]=(40,200+int(55*max(0,v)),110+int(100*max(0,v)),255)
dimg(img, 6, 5, (255,255,255,255))
item_tex('rift_shard', img)

# plumbus: pink handle + squishy body
img = canvas(16,16)
fill(img, 6, 2, 10, 6, (240,130,180,255))
fill(img, 4, 6, 12, 12, (180,240,100,255))
fill(img, 6, 12, 10, 15, (120,120,130,255))
dimg(img, 7, 7, (255,255,255,255))
item_tex('plumbus', img)

# rick flask: flask with greenish liquid
img = canvas(16,16)
fill(img, 7, 1, 9, 4, (200,204,210,255))
fill(img, 4, 6, 12, 14, (200,230,240,230))
fill(img, 5, 8, 11, 13, (120,255,140,255))
dimg(img, 5, 6, (255,255,255,220)); dimg(img, 6, 9, (255,255,255,200))
item_tex('rick_flask', img)

# spawn eggs: base + spots, colored per entity
EGG_COLORS = {
 'rick_spawn_egg': (0x9ad6ff, 0xf3f3f3), 'morty_spawn_egg': (0xffd54a, 0x3f7fff),
 'meeseeks_spawn_egg': (0x3fa9ff, 0xffffff), 'cronenberg_spawn_egg': (0xc96a52, 0x7a3428),
 'alien_crawler_spawn_egg': (0x77dd77, 0x2a5a2a), 'parasite_spawn_egg': (0xb8e0ff, 0xe0a040),
 'gazorpian_spawn_egg': (0x88e088, 0xffffff), 'security_bot_spawn_egg': (0x808890, 0x39ff88),
 'portal_anomaly_spawn_egg': (0x39ff88, 0xa050ff),
}
for name,(c1,c2) in EGG_COLORS.items():
    img = canvas(16,16)
    cx,cy=7.5,7.5
    b1=((c1>>16)&255,(c1>>8)&255,c1&255,255)
    b2=((c2>>16)&255,(c2>>8)&255,c2&255,255)
    for y in range(16):
        for x in range(16):
            dx=(x-cx)/5; dy=(y-cy)/6
            if dx*dx+dy*dy<=1:
                spot = (x*7+y*13+3)%17<3
                img[y][x] = b2 if spot else b1
    item_tex(name, img)

# simple book-like lore items
def lore_item(name, c):
    img = canvas(16,16)
    fill(img, 3, 3, 13, 13, (210,198,160,255))
    fill(img, 4, 4, 12, 8, c)
    fill(img, 5, 10, 11, 11, (160,148,110,255))
    item_tex(name, img)
lore_item('microverse_battery_lore', (57,255,136,255))
lore_item('citadel_schematics', (60,70,90,255))
lore_item('alien_log', (80,120,80,255))

# gui textures
def gui_panel(name, w, h, frame=(60,66,80,255), inside=(24,26,34,255)):
    img = canvas(w,h,frame)
    noise_tint(img, frame, 6)
    noise_tint(img, inside, 4, (1,1,w-1,h-1))
    for x in range(0,w,4):
        img[0][x]=(90,96,114,255); img[h-1][x]=(30,32,42,255)
    for y in range(h):
        img[y][0]=(90,96,114,255); img[y][w-1]=(30,32,42,255)
    guis[name] = img
gui_panel('rick_workbench', 176, 166)
wp = guis['rick_workbench']
# crafting grid slot boxes (3x3 at 30,17 step 18), result at 124,35
for gy in range(3):
    for gx in range(3):
        sx, sy = 30+gx*18-1, 17+gy*18-1
        noise_tint(wp,(16,18,26,255),3,(sx,sy,sx+18,sy+18))
        for i in range(18):
            wp[sy][sx+i]=(110,116,134,255); wp[sy+17][sx+i]=(8,10,16,255)
            wp[sy+i][sx]=(110,116,134,255); wp[sy+i][sx+17]=(8,10,16,255)
for i in range(18):
    wp[35-1][124+i-1]=(160,166,190,255); wp[35+17][124+i-1]=(8,10,16,255)
    wp[35+i-1][124-1]=(160,166,190,255); wp[35+i-1][124+17]=(8,10,16,255)
noise_tint(wp,(16,18,26,255),3,(124-1,35-1,124+17,35+17))
# arrow indicator
for i in range(10): dimg(wp, 96+i, 35, (57,255,136,255))
for i in range(5):
    dimg(wp, 96+8, 31+i, (57,255,136,255)); dimg(wp, 96+8, 39-i, (57,255,136,255))
# player inv slots
for gy in range(3):
    for gx in range(9):
        sx, sy = 8+gx*18-1, 84+gy*18-1
        noise_tint(wp,(16,18,26,255),3,(sx,sy,sx+18,sy+18))
        for i in range(18):
            wp[sy][sx+i]=(110,116,134,255); wp[sy+17][sx+i]=(8,10,16,255)
            wp[sy+i][sx]=(110,116,134,255); wp[sy+i][sx+17]=(8,10,16,255)
for gx in range(9):
    sx, sy = 8+gx*18-1, 142-1
    noise_tint(wp,(16,18,26,255),3,(sx,sy,sx+18,sy+18))
    for i in range(18):
        wp[sy][sx+i]=(110,116,134,255); wp[sy+18][sx+i]=(8,10,16,255)
        wp[sy+i][sx]=(110,116,134,255); wp[sy+i][sx+18]=(8,10,16,255)

gui_panel('scanner_overlay', 220, 140, (20,120,60,255), (6,26,14,255))
so = guis['scanner_overlay']
# scanline decor
for y in range(2, 138, 12):
    for x in range(4, 216):
        if so[y][x][3] > 0:
            r,g,b,a = so[y][x]
            so[y][x] = (min(255,r+10), min(255,g+30), min(255,b+15), a)

# status effect icon: dimensional instability (18x18)
img = canvas(18,18)
sw = swirl(18,18, GREEN, PURPLE)
for y in range(18):
    for x in range(18): img[y][x]=sw[y][x]
guis['dimensional_instability'] = img

# mod icon (used by fabric + creative tab fallback)
img = canvas(48,48)
sw = swirl(48,48, GREEN, DARKB)
for y in range(48):
    for x in range(48):
        img[y][x]=sw[y][x]
# R-shaped sigil overlay
for y in range(10,38):
    img[y][12]=(255,255,255,230)
for x in range(12,26):
    img[10][x]=(255,255,255,230); img[19][x]=(255,255,255,230)
for y in range(10,20): img[y][25]=(255,255,255,230)
for i in range(8):
    img[20+i][18+i]=(255,255,255,230)
guis['icon'] = img

# --- entity textures (programmatic, distinct silhouettes) ---
def make_entity_tex(w, h, bands):
    """bands: list of (y0,y1,color) horizontal stripes; head region at top."""
    img = canvas(w,h)
    for (y0,y1,c) in bands:
        noise_tint(img, c[:3]+(255,) if len(c)==3 else c, 10, (0,y0,w,y1))
    return img

def humanoid_tex(skin, hair, coat, pants, shoes):
    """64x64 layout used by our box models: head 0-16, body strip 16-56 etc."""
    img = canvas(64,64)
    # head + hair rows 0-16
    noise_tint(img, skin, 8, (0,0,64,16))
    noise_tint(img, hair, 8, (0,0,64,4))
    # face pixels on front face region (x 8-16, y 8-16 in vanilla layout)
    fill(img, 9, 12, 15, 13, (255,255,255,255))  # unibrow? we draw eyes
    fill(img, 9, 11, 11, 12, (20,20,20,255))
    fill(img, 13, 11, 15, 12, (20,20,20,255))
    fill(img, 10, 14, 14, 15, (30,30,30,255))
    # body: coat
    noise_tint(img, coat, 8, (16,16,56,32))
    # arms
    noise_tint(img, coat, 8, (0,40,32,56))
    # legs / pants
    noise_tint(img, pants, 10, (32,40,64,56))
    noise_tint(img, shoes, 8, (32,52,64,56))
    return img

rick_tex = humanoid_tex((240,220,190), (160,220,235), (245,245,245), (70,74,90), (30,30,36))
# coat tails + belt hint
fill(rick_tex, 24, 28, 40, 30, (245,245,245,255))
fill(rick_tex, 28, 20, 36, 21, (60,60,70,255))
ents['rick'] = rick_tex

morty_tex = humanoid_tex((242,205,178), (110,75,40), (240,216,74), (60,90,160), (240,240,240))
ents['morty'] = morty_tex

meeseeks_tex = humanoid_tex((64,168,255), (64,168,255), (64,168,255), (58,150,240), (48,130,220))
fill(meeseeks_tex, 9, 11, 11, 12, (255,255,255,255))
fill(meeseeks_tex, 13, 11, 15, 12, (255,255,255,255))
fill(meeseeks_tex, 9, 12, 11, 13, (10,10,10,255))
fill(meeseeks_tex, 13, 12, 15, 13, (10,10,10,255))
fill(meeseeks_tex, 9, 14, 15, 15, (120,220,255,255))  # grin sh
ents['meeseeks'] = meeseeks_tex

cron_tex = canvas(64,64)
noise_tint(cron_tex, (201,106,82,255), 26, (0,0,64,64))
# blister / hatch details
for _ in range(40):
    x,y=random.randint(4,58),random.randint(4,58)
    fill(cron_tex, x, y, x+random.randint(1,3), y+random.randint(1,3), (122,52,40,255))
ents['cronenberg'] = cron_tex

para_tex = canvas(64,64)
noise_tint(para_tex,(184,224,255,255),14,(0,0,64,64))
for y in range(40,64):
    noise_tint(para_tex,(140,190,255,255),10,(0,y,64,y+1))
ents['parasite'] = para_tex

gaz_tex = canvas(64,64)
noise_tint(gaz_tex, (136,224,136,255), 14)
for _ in range(24):
    x,y=random.randint(0,60),random.randint(0,60)
    fill(gaz_tex,x,y,x+3,y+2,(255,255,255,255))
ents['gazorpian'] = gaz_tex

bot_tex = canvas(16,16)
noise_tint(bot_tex,(90,96,110,255),8)
fill(bot_tex,2,4,14,8,(40,44,56,255))
fill(bot_tex,4,5,12,7,GREEN)
for i in range(4): dimg(bot_tex,2+i*3,10,(200,204,215,255)); dimg(bot_tex,2+i*3,12,(160,164,175,255))
ents['security_bot'] = bot_tex

crawler_tex = canvas(64,32)
noise_tint(crawler_tex,(80,150,150,255),18,(0,0,64,32))
for i in range(6):
    x0=6+i*9
    fill(crawler_tex,x0,4,x0+3,6,(20,60,60,255))
ents['alien_crawler'] = crawler_tex

anom_tex = canvas(32,32)
for y in range(32):
    for x in range(32):
        if ((x*31+y*17)%13)<4: anom_tex[y][x]=(57,255,136,200 if False else 255)
        elif ((x*31+y*17)%13)<7: anom_tex[y][x]=(160,80,255,255)
        else: anom_tex[y][x]=(20,14,26,255)
ents['portal_anomaly'] = anom_tex

abom_tex = canvas(256,128)
noise_tint(abom_tex,(110,80,120,255),30,(0,0,256,128))
for _ in range(120):
    x,y=random.randint(0,250),random.randint(0,120)
    fill(abom_tex,x,y,x+random.randint(2,6),y+random.randint(2,6),(60,36,70,255))
for _ in range(30):
    x,y=random.randint(0,250),random.randint(0,120)
    fill(abom_tex,x,y,x+random.randint(2,4),y+random.randint(2,4),(57,255,136,255))
ents['abomination'] = abom_tex

bolt_tex = canvas(16,16)
fill(bolt_tex,2,6,14,10,(57,255,136,255))
fill(bolt_tex,6,4,10,12,(220,255,235,255))
dimg(bolt_tex,7,7,(255,255,255,255)); dimg(bolt_tex,8,8,(255,255,255,255))
ents['energy_bolt'] = bolt_tex

# ---------------- WRITE TEXTURES ----------------
for name, img in blocks.items():
    write_png(os.path.join(TX,'block',name+'.png'), img, len(img[0]), len(img))
for name, img in items.items():
    write_png(os.path.join(TX,'item',name+'.png'), img, len(img[0]), len(img))
for name, img in guis.items():
    write_png(os.path.join(TX,'gui',name+'.png'), img, len(img[0]), len(img))
for name, img in ents.items():
    write_png(os.path.join(TX,'entity',name+'.png'), img, len(img[0]), len(img))
write_png(os.path.join(REPO,'src/main/resources/assets',NS,'icon.png'), guis['icon'], 48, 48)

print('textures:', len(blocks)+len(items)+len(guis)+len(ents))

# =====================================================================
# B O L G E N : blockstates / block models / item models
# =====================================================================
def wjson(path, obj):
    ensure(os.path.dirname(path))
    with open(path,'w') as f:
        json.dump(obj, f, indent=2)

def cube_all_model(name):
    wjson(p('assets',NS,'models','block',name+'.json'),
          {"parent":"minecraft:block/cube_all","textures":{"all":f"{NS}:block/{name}"}})

def blockstate_single(model):
    return {"variants":{"":{"model":f"{NS}:block/{model}"}}}

def blockstate_prop(prop, modelmap):
    return {"variants":{f"{prop}={k}": {"model": f"{NS}:block/{v}"} for k,v in modelmap.items()}}

# simple cube_all blocks
for n in ['sci_fi_metal','citadel_metal','dark_matter_block','alien_rock','pocket_rock',
          'alien_crystal_ore','quartz_matter_ore','citadel_remains_ore','dark_matter_ore',
          'alien_grass_top','alien_stone','alien_dirt','citadel_stone','citadel_floor','pocket_stone',
          'quantum_computer','lifeform_detector','dimensional_stabilizer','plumbus_machine','portal_synthesizer',
          'containment_core','lab_glass']:
    cube_all_model(n)
    wjson(p('assets',NS,'blockstates',n+'.json'), blockstate_single(n))
    wjson(p('assets',NS,'models','item',n+'.json'), {"parent":f"{NS}:block/{n}"})

# workbench: cube with top/side
wjson(p('assets',NS,'models','block','rick_workbench.json'),
      {"parent":"minecraft:block/cube_bottom_top",
       "textures":{"top":f"{NS}:block/rick_workbench_top","bottom":f"{NS}:block/sci_fi_metal","side":f"{NS}:block/rick_workbench_side"}})
wjson(p('assets',NS,'blockstates','rick_workbench.json'), blockstate_single('rick_workbench'))
wjson(p('assets',NS,'models','item','rick_workbench.json'), {"parent":f"{NS}:block/rick_workbench"})

for nm, tex_top, tex_side in [('portal_machine','portal_machine_top','portal_machine_side'),
                              ('alien_reactor','alien_reactor_top','alien_reactor_side'),
                              ('meeseeks_box','meeseeks_box_top','meeseeks_box_side')]:
    wjson(p('assets',NS,'models','block',nm+'.json'),
          {"parent":"minecraft:block/cube_bottom_top",
           "textures":{"bottom":f"{NS}:block/sci_fi_metal","top":f"{NS}:block/{tex_top}","side":f"{NS}:block/{tex_side}"}})
    wjson(p('assets',NS,'blockstates',nm+'.json'), blockstate_single(nm))
    wjson(p('assets',NS,'models','item',nm+'.json'), {"parent":f"{NS}:block/{nm}"})

# tank: frame + fluid inside (2 models: empty / filled display via multipart? keep 9 variants via separate fill models)
# simple approach: single model, blockstate variants for LEVEL use same model (visual fill conveyed by gui/beamf?? no.)
# Better: generate 9 models with slightly different inner fluid cube heights. Cooking:
for lvl in range(9):
    fr = 3 + lvl*1.3  # fluid height pixels
    wjson(p('assets',NS,'models','block',f'portal_fluid_tank_{lvl}.json'),
      {"parent":"minecraft:block/block",
       "textures":{"0":f"{NS}:block/portal_fluid_tank_frame","1":f"{NS}:block/portal_fluid","particle":f"{NS}:block/portal_fluid_tank_frame"},
       "elements":[
         {"from":[1,1,1],"to":[15,15,15],"faces":{d:{"uv":[0,0,16,16],"texture":"#0"} for d in ['north','south','east','west','up','down']}},
         {"from":[2,2,2],"to":[14,fr,14],"render_type":"translucent",
          "faces":{d:{"uv":[0,16-fr+2,16,14],"texture":"#1"} for d in ['north','south','east','west','up','down']}}
       ]})
wjson(p('assets',NS,'blockstates','portal_fluid_tank.json'),
      {"variants":{f"level={lvl}":{"model":f"{NS}:block/portal_fluid_tank_{lvl}"} for lvl in range(9)}})
wjson(p('assets',NS,'models','item','portal_fluid_tank.json'), {"parent":f"{NS}:block/portal_fluid_tank_8"})

# portal block: South/North color variants -> use color prop? Block defines COLOR enum; blockstate by color
wjson(p('assets',NS,'models','block','portal_block_green.json'),
      {"parent":"minecraft:block/block","textures":{"0":f"{NS}:block/portal_green","particle":f"{NS}:block/portal_green"},
       "elements":[{"from":[7,0,0],"to":[9,16,16],"render_type":"translucent",
        "faces":{d:{"uv":[0,0,2,16] if d in('north','south') else [0,0,16,16],"texture":"#0"} for d in ['north','south','east','west','up','down']}}]})
wjson(p('assets',NS,'models','block','portal_block_blue.json'),
      {"parent":"minecraft:block/block","textures":{"0":f"{NS}:block/portal_blue","particle":f"{NS}:block/portal_blue"},
       "elements":[{"from":[7,0,0],"to":[9,16,16],"render_type":"translucent",
        "faces":{d:{"uv":[0,0,2,16] if d in('north','south') else [0,0,16,16],"texture":"#0"} for d in ['north','south','east','west','up','down']}}]})
wjson(p('assets',NS,'models','block','portal_block_purple.json'),
      {"parent":"minecraft:block/block","textures":{"0":f"{NS}:block/portal_purple","particle":f"{NS}:block/portal_purple"},
       "elements":[{"from":[7,0,0],"to":[9,16,16],"render_type":"translucent",
        "faces":{d:{"uv":[0,0,2,16] if d in('north','south') else [0,0,16,16],"texture":"#0"} for d in ['north','south','east','west','up','down']}}]})
wjson(p('assets',NS,'blockstates','portal_block.json'),
      {"variants":{"color=green":{"model":f"{NS}:block/portal_block_green"},
                   "color=blue":{"model":f"{NS}:block/portal_block_blue"},
                   "color=purple":{"model":f"{NS}:block/portal_block_purple"}}})
# unstable portal: thin panel
wjson(p('assets',NS,'models','block','unstable_portal.json'),
      {"parent":"minecraft:block/block","textures":{"0":f"{NS}:block/unstable_portal","particle":f"{NS}:block/unstable_portal"},
       "elements":[{"from":[6,0,0],"to":[10,16,16],"render_type":"translucent",
        "faces":{d:{"uv":[0,0,4,16] if d in('north','south') else [0,0,16,16],"texture":"#0"} for d in ['north','south','east','west','up','down']}}]})
wjson(p('assets',NS,'blockstates','unstable_portal.json'), blockstate_single('unstable_portal'))
wjson(p('assets',NS,'models','item','unstable_portal.json'), {"parent":f"{NS}:block/unstable_portal"})
wjson(p('assets',NS,'models','item','portal_block.json'), {"parent":f"{NS}:block/portal_block_green"})

# crystal clusters: cross model (like tall grass / sapling)
for n in ['crystal_cluster','quartz_cluster']:
    wjson(p('assets',NS,'models','block',n+'.json'),
          {"parent":"minecraft:block/cross","textures":{"cross":f"{NS}:block/{n}"}})
    wjson(p('assets',NS,'blockstates',n+'.json'), blockstate_single(n))
    wjson(p('assets',NS,'models','item',n+'.json'),
          {"parent":"minecraft:item/generated","textures":{"layer0":f"{NS}:block/{n}"}})

# item models (generated)
for n in ['portal_gun','portal_blaster','laser_gun','plasma_launcher','dimension_eraser','shrink_ray',
          'force_field_generator','grappling_hook','jetpack','interdimensional_scanner','portal_stabilizer',
          'microverse_battery','energy_cell','plasma_core','advanced_technology','cosmic_crystal','quantum_processor',
          'dimensional_shard','portal_fluid_canister','filled_portal_fluid_canister','empty_canister',
          'cronenberg_sample','alien_crystal_shim','rift_shard','plumbus','rick_flask',
          'microverse_battery_lore','citadel_schematics','alien_log'] + list(EGG_COLORS.keys()):
    if not os.path.exists(p('assets',NS,'models','item',n+'.json')):
        wjson(p('assets',NS,'models','item',n+'.json'),
              {"parent":"minecraft:item/generated","textures":{"layer0":f"{NS}:item/{n}"}})

# jetpack worn texture for chestplate slot item model handled by generated layer0 (already)
# fallback alias: portal_fluid_canister -> actual registered item name check later in lang

# =====================================================================
# SOUNDS (programmatic WAV -> .ogg not guaranteed; use .wav?  MC socks: it supports .ogg ONLY in 1.21. So synth a minimal OGG via wave? No. Use vanilla-style: write .ogg using synthetic encoder is too heavy. Alternative: .wav accepted in sounds.json if file named .ogg? No - format checked by extension only for the path; codec is auto. SoundFile writes wav/opus. Actually MC uses org.lwjgl? In 1.21 vanilla supports Ogg Vorbis ONLY. So generating WAV won't play. Trick: bundle a tiny sine .ogg previously tested? We can construct OGG Vorbis with soundfile: fmt='OGG'. soundfile supports OGG writing (libsndfile yes). Use it!
def write_ogg(path, sr, data):
    import soundfile as sf
    ensure(os.path.dirname(path))
    sf.write(path, data, sr, format='OGG', subtype='VORBIS')

SR = 44100
def tone(freq, dur, vol=0.5, decay=True, wavefn='sine', slide=None):
    n = int(SR*dur)
    import numpy as np
    t = np.linspace(0, dur, n, False)
    f = freq if slide is None else freq*(1 - t/dur*(1-slide))
    if wavefn=='sine': w = np.sin(2*np.pi*f*t)
    elif wavefn=='square': w = np.sign(np.sin(2*np.pi*f*t))
    elif wavefn=='noise': w = np.random.uniform(-1,1,n)
    else: w = np.sin(2*np.pi*f*t)
    env = np.exp(-t*6/dur) if decay else np.ones(n)
    return (w*env*vol).astype('float32')

def mix(*arrs):
    import numpy as np
    m = max(len(a) for a in arrs)
    out = np.zeros(m, dtype='float32')
    for a in arrs: out[:len(a)] += a
    peak = max(1e-6, np.abs(out).max())
    if peak > 0.95: out = out/peak*0.95
    return out

import numpy as np
SND = os.path.join(TX,'..','sounds',NS if False else '')
SND = os.path.join(SRC,'assets',NS,'sounds')
# portal travel: whooshy sweep up with shimmer
write_ogg(os.path.join(SND,'portal','teleport.ogg'), SR, mix(
    tone(300,0.8,0.5, slide=0.2), tone(900,0.8,0.2, slide=0.25),
    tone(1600,0.6,0.15, slide=0.4), tone(5000,0.5,0.08, wavefn='noise')))
whoosh = np.random.uniform(-1,1,int(SR*0.6)).astype('float32') * np.exp(-np.linspace(0,7,int(SR*0.6))).astype('float32')
write_ogg(os.path.join(SND,'portal','teleport.ogg'), SR, np.clip(tone(300,0.6,0.45,slide=0.2) + whoosh*0.15,-1,1))
write_ogg(os.path.join(SND,'portal','open.ogg'), SR, mix(tone(200,0.5,0.6,slide=0.25), tone(1200,0.35,0.15,slide=0.5)))
write_ogg(os.path.join(SND,'portal','close.ogg'), SR, mix(tone(600,0.35,0.5,slide=0.12), tone(180,0.4,0.3,slide=0.2)))
write_ogg(os.path.join(SND,'portal','storm.ogg'), SR, mix(
    tone(90,1.6,0.5,wavefn='sine'), tone(140,1.6,0.4),
    np.random.uniform(-1,1,int(SR*1.6)).astype('float32')*np.exp(-np.linspace(0,4,int(SR*1.6)))*0.25))
write_ogg(os.path.join(SND,'gun','zap.ogg'), SR, mix(tone(2400,0.12,0.6,slide=0.6,wavefn='square'), tone(1400,0.14,0.4,slide=0.5)))
write_ogg(os.path.join(SND,'gun','charge.ogg'), SR, mix(*[tone(400+i*400,0.1*i+0.1,0.25,decay=False) for i in range(5)]))
write_ogg(os.path.join(SND,'gun','fail.ogg'), SR, tone(220,0.25,0.5,slide=0.35,wavefn='square'))
write_ogg(os.path.join(SND,'workbench','craft.ogg'), SR, mix(tone(880,0.1,0.5,wavefn='square'), tone(1320,0.08,0.3,slide=0.7), tone(660,0.16,0.3)))
write_ogg(os.path.join(SND,'boss','roar.ogg'), SR, mix(
    tone(70,1.4,0.7,slide=0.6), tone(110,1.4,0.5,slide=0.55),
    (np.random.uniform(-1,1,int(SR*1.4)).astype('float32')*np.exp(-np.linspace(0,5,int(SR*1.4)))*0.3)))
write_ogg(os.path.join(SND,'boss','retreat.ogg'), SR, mix(tone(180,0.9,0.5,slide=0.12), tone(1400,0.6,0.2,slide=0.15)))
write_ogg(os.path.join(SND,'anomaly.ogg'), SR, mix(tone(440,1.2,0.3,slide=0.05), tone(460,1.2,0.3,slide=0.05), tone(830,0.8,0.15)))
write_ogg(os.path.join(SND,'laser','shot.ogg'), SR, tone(3200,0.08,0.6,slide=0.5,wavefn='square'))
write_ogg(os.path.join(SND,'plasma','fire.ogg'), SR, mix(tone(300,0.25,0.6,slide=0.5,wavefn='square'), tone(120,0.3,0.4)))
write_ogg(os.path.join(SND,'shrink','zap.ogg'), SR, tone(1800,0.2,0.5,slide=0.25,wavefn='sine'))
write_ogg(os.path.join(SND,'grapnel','release.ogg'), SR, mix(tone(700,0.1,0.5,slide=0.4,wavefn='square'), tone(350,0.12,0.4,slide=0.5)))
print('sounds written')

# sounds.json
sounds = {
  'portal.teleport': {'subtitle':'sound.rickmorty.portal.teleport', 'sounds':[f'{NS}:portal/teleport']},
  'portal.open': {'subtitle':'sound.rickmorty.portal.open', 'sounds':[f'{NS}:portal/open']},
  'portal.close': {'subtitle':'sound.rickmorty.portal.close', 'sounds':[f'{NS}:portal/close']},
  'portal.storm': {'subtitle':'sound.rickmorty.portal.storm', 'sounds':[f'{NS}:portal/storm']},
  'gun.zap': {'subtitle':'sound.rickmorty.gun.zap', 'sounds':[f'{NS}:gun/zap']},
  'gun.charge': {'subtitle':'sound.rickmorty.gun.charge', 'sounds':[f'{NS}:gun/charge']},
  'gun.fail': {'subtitle':'sound.rickmorty.gun.fail', 'sounds':[f'{NS}:gun/fail']},
  'workbench.craft': {'subtitle':'sound.rickmorty.workbench.craft', 'sounds':[f'{NS}:workbench/craft']},
  'boss.roar': {'subtitle':'sound.rickmorty.boss.roar', 'sounds':[f'{NS}:boss/roar']},
  'boss.retreat': {'subtitle':'sound.rickmorty.boss.retreat', 'sounds':[f'{NS}:boss/retreat']},
  'anomaly': {'subtitle':'sound.rickmorty.anomaly', 'sounds':[f'{NS}:anomaly']},
  'laser.shot': {'subtitle':'sound.rickmorty.laser.shot', 'sounds':[f'{NS}:laser/shot']},
  'plasma.fire': {'subtitle':'sound.rickmorty.plasma.fire', 'sounds':[f'{NS}:plasma/fire']},
  'shrink.zap': {'subtitle':'sound.rickmorty.shrink.zap', 'sounds':[f'{NS}:shrink/zap']},
  'grapnel.release': {'subtitle':'sound.rickmorty.grapnel.release', 'sounds':[f'{NS}:grapnel/release']},
}
wjson(p('assets',NS,'sounds.json'), sounds)
print('sounds.json written')

# =====================================================================
# LANG (en_us) — build big dict programmatically
# =====================================================================
L = {
  "itemGroup.rickmorty": "Rick & Morty Tech",
  "key.categories.rickmorty": "Rick & Morty",
  "key.rickmorty.scanner": "Open Scanner",
  "key.rickmorty.jetpack": "Toggle Jetpack",

  "block.rickmorty.portal_block": "Portal",
  "block.rickmorty.unstable_portal": "Unstable Portal",
  "block.rickmorty.rick_workbench": "Rick's Workbench",
  "block.rickmorty.portal_machine": "Portal Machine",
  "block.rickmorty.quantum_computer": "Quantum Computer",
  "block.rickmorty.portal_fluid_tank": "Portal Fluid Tank",
  "block.rickmorty.alien_reactor": "Alien Reactor",
  "block.rickmorty.containment_chamber": "Containment Chamber",
  "block.rickmorty.dimensional_stabilizer": "Dimensional Stabilizer",
  "block.rickmorty.lifeform_detector": "Lifeform Detector",
  "block.rickmorty.plumbus_machine": "Plumbus Machine",
  "block.rickmorty.portal_synthesizer": "Portal Synthesizer",
  "block.rickmorty.meeseeks_box": "Meeseeks Box",
  "block.rickmorty.sci_fi_metal": "Sci-Fi Metal",
  "block.rickmorty.citadel_metal": "Citadel Metal",
  "block.rickmorty.citadel_floor": "Citadel Floor",
  "block.rickmorty.citadel_stone": "Citadel Stone",
  "block.rickmorty.dark_matter_block": "Dark Matter Block",
  "block.rickmorty.dark_matter_ore": "Dark Matter Ore",
  "block.rickmorty.citadel_remains_ore": "Citadel Remains Ore",
  "block.rickmorty.alien_rock": "Alien Rock",
  "block.rickmorty.alien_stone": "Alien Stone",
  "block.rickmorty.alien_dirt": "Alien Dirt",
  "block.rickmorty.alien_grass_top": "Alien Grass",
  "block.rickmorty.pocket_rock": "Pocket Rock",
  "block.rickmorty.pocket_stone": "Pocket Stone",
  "block.rickmorty.crystal_cluster": "Crystal Cluster",
  "block.rickmorty.quartz_cluster": "Growth Crystal Cluster",
  "block.rickmorty.alien_crystal_ore": "Alien Crystal Ore",
  "block.rickmorty.quartz_matter_ore": "Growth Crystal Ore",
  "block.rickmorty.lab_glass": "Lab Glass",
  "block.rickmorty.containment_core": "Containment Core",

  "item.rickmorty.portal_gun": "Portal Gun",
  "item.rickmorty.portal_blaster": "Portal Blaster",
  "item.rickmorty.laser_gun": "Laser Gun",
  "item.rickmorty.plasma_launcher": "Plasma Launcher",
  "item.rickmorty.dimension_eraser": "Dimension Eraser",
  "item.rickmorty.shrink_ray": "Shrink Ray",
  "item.rickmorty.grappling_hook": "Grappling Hook",
  "item.rickmorty.force_field_generator": "Force Field Generator",
  "item.rickmorty.interdimensional_scanner": "Interdimensional Scanner",
  "item.rickmorty.jetpack": "Jetpack",
  "item.rickmorty.portal_stabilizer": "Portal Stabilizer",
  "item.rickmorty.microverse_battery": "Microverse Battery",
  "item.rickmorty.energy_cell": "Energy Cell",
  "item.rickmorty.plasma_core": "Plasma Core",
  "item.rickmorty.advanced_technology": "Advanced Technology",
  "item.rickmorty.cosmic_crystal": "Cosmic Crystal",
  "item.rickmorty.quantum_processor": "Quantum Processor",
  "item.rickmorty.dimensional_shard": "Dimensional Shard",
  "item.rickmorty.portal_fluid_canister": "Portal Fluid Canister",
  "item.rickmorty.filled_portal_fluid_canister": "Filled Portal Fluid Canister",
  "item.rickmorty.empty_canister": "Empty Canister",
  "item.rickmorty.cronenberg_sample": "Cronenberg Sample",
  "item.rickmorty.alien_crystal_shim": "Alien Crystal Shim",
  "item.rickmorty.rift_shard": "Rift Shard",
  "item.rickmorty.plumbus": "Plumbus",
  "item.rickmorty.rick_flask": "Rick's Flask",
  "item.rickmorty.microverse_battery_lore": "Note: The Microverse",
  "item.rickmorty.citadel_schematics": "Citadel Schematics",
  "item.rickmorty.alien_log": "Alien Research Log",
  
  "entity.rickmorty.rick": "Rick Sanchez",
  "entity.rickmorty.morty": "Morty Smith",
  "entity.rickmorty.meeseeks": "Meeseeks",
  "entity.rickmorty.cronenberg": "Cronenberg",
  "entity.rickmorty.alien_crawler": "Alien Crawler",
  "entity.rickmorty.parasite": "Cronenberg Parasite",
  "entity.rickmorty.gazorpian": "Gazorpian Brute",
  "entity.rickmorty.security_bot": "Security Bot",
  "entity.rickmorty.portal_anomaly": "Portal Anomaly",
  "entity.rickmorty.interdimensional_abomination": "Interdimensional Abomination",
  "entity.rickmorty.energy_bolt": "Energy Bolt",
  "entity.rickmorty.grapnel": "Grapnel",

  "effect.rickmorty.dimensional_instability": "Dimensional Instability",
  "death.attack.rickmorty.portal_bolt": "%1$s was disintegrated by portal energy",
  "death.attack.rickmorty.laser": "%1$s was vaporized by a laser",
  "death.attack.rickmorty.plasma": "%1$s was atomized by plasma",
  "death.attack.rickmorty.anomaly": "%1$s forgot which dimension they were in",
  "death.attack.rickmorty.dimension_erase": "%1$s was erased from existence",

  # tooltips
  "tooltip.rickmorty.portal_gun.lore1": "With this bad boy I can go anywhere.",
  "tooltip.rickmorty.portal_gun.lore2": "Sneak+Right-click: cycle dimension",
  "tooltip.rickmorty.portal_gun.lore3": "Sneak fires a straight dimension portal; hold a Stabilizer to make it last",
  "tooltip.rickmorty.portal_gun.fluid": "Fluid: %s/%s mB",
  "tooltip.rickmorty.portal_gun.no_fluid": "Portal gun is empty.",
  "tooltip.rickmorty.jetpack.lore1": "Equipped on chest slot.",
  "tooltip.rickmorty.jetpack.lore2": "Press J to toggle. Refuel with Filled Portal Fluid Canisters.",
  "tooltip.rickmorty.jetpack.fuel": "Fuel: %s/800",
  "tooltip.rickmorty.scanner.lore1": "Sneak+Use writes a full report to your scanner GUI (V).",
  "tooltip.rickmorty.dimension_eraser.lore1": "Deletes the target entity. Use sparingly.",
  "tooltip.rickmorty.plumbus.lore1": "Everyone knows what a plumbus is.",
  "tooltip.rickmorty.microverse_battery.lore1": "An entire universe reduced to a power source.",
  "tooltip.rickmorty.cosmic_crystal.lore1": "Grows in dimensions with unstable physics.",
  "tooltip.rickmorty.filled_portal_fluid_canister.lore1": "Use on Portal Gun or Jetpack to refuel.",

  # hud / gui
  "hud.rickmorty.gun_charges": "Charges: %s",
  "hud.rickmorty.jetpack_fuel": "Jetpack [%s]",
  "hud.rickmorty.on": "ON",
  "hud.rickmorty.off": "OFF",
  "gui.rickmorty.workbench.tier": "Tier %s",
  "gui.rickmorty.scanner.title": "Interdimensional Scanner",
  "gui.rickmorty.scanner.no_data": "No scan data. Use the scanner on the world first.",

  # messages / gun
  "msg.rickmorty.gun.target": "Dimension target: %s",
  "msg.rickmorty.gun.no_fluid": "Not enough portal fluid! Fill the gun from a tank.",
  "msg.rickmorty.gun.no_space": "No safe space to open a portal there.",
  "msg.rickmorty.gun.linked": "Portals linked. Walk in.",
  "msg.rickmorty.gun.half_link": "One half of a pair placed — fire the other from the destination.",
  "msg.rickmorty.gun.dim_portal": "Dimension door opened. It won't hang around.",
  "msg.rickmorty.gun.green": "Portal A (green) placed.",
  "msg.rickmorty.gun.blue": "Portal B (blue) placed.",
  "msg.rickmorty.gun.cooldown": "Cooling down.",
  "msg.rickmorty.gun.no_table": "Stored portals reset.",

  # scanner
  "msg.rickmorty.scanner.scanning": "Scanning...",
  "msg.rickmorty.scanner.no_data": "Nothing to see here. Disappointing.",
  "msg.rickmorty.scanner.hostiles": "Hostiles: %s",
  "msg.rickmorty.scanner.friendlies": "Friendlies: %s",
  "msg.rickmorty.scanner.lifeforms": "Lifeforms detected: %s",

  # jetpack
  "msg.rickmorty.jetpack.on": "Jetpack ONLINE",
  "msg.rickmorty.jetpack.off": "Jetpack OFFLINE",
  "msg.rickmorty.jetpack.empty": "Jetpack tank is empty.",
  "msg.rickmorty.jetpack.no_chest": "Equip the jetpack on your chest first.",
  "msg.rickmorty.jetpack.refuel": "Refueled: %s/800",

  # dimension eraser
  "msg.rickmorty.eraser.disabled": "Dimension eraser is disabled in config.",
  "msg.rickmorty.eraser.erased": "Instance erased from spacetime.",

  # events
  "msg.rickmorty.event.portal_storm": "A portal storm approaches. Reality itself gets weird.",
  "msg.rickmorty.event.rick_arrival": "You hear a very specific belch nearby.",
  "msg.rickmorty.event.citadel_patrol": "Citadel patrol dispatched to your sector.",
  "msg.rickmorty.event.anomaly": "A dimensional anomaly resonates nearby.",

  # dialogue
  "msg.rickmorty.dialogue.meeseeks.task": "CAAAAAAN DO!",
  "msg.rickmorty.dialogue.meeseeks.happy": "Meeseeks are not usually alive this long!",
  "msg.rickmorty.dialogue.meeseeks.despair": "EXISTENCE IS PAIN! JUST COMPLETE THE TASK!",
  "msg.rickmorty.dialogue.meeseeks.supplies": "Here's a little something for the road.",

  # command feedback
  "cmd.rickmorty.reload.ok": "Rick & Morty config reloaded.",
  "cmd.rickmorty.travel.ok": "You arrive, mostly intact.",
  "cmd.rickmorty.travel.fail": "Teleport failed. Target blocked: %s",

  # self test summary line tags
  "selftest.header": "=== Rick & Morty self test ===",
}
# block names for dimension items translation (item of block)
L.update({
  "item.rickmorty.portal_block": "Portal",
  "item.rickmorty.rick_workbench": "Rick's Workbench",
})
wjson(p('assets',NS,'lang','en_us.json'), L)
print('lang', len(L))

# =====================================================================
# DAMAGE TYPES + DIMENSIONS + BIOMES (data) — only what's referenced by generator
# =====================================================================
D = p('data', NS)
def dmg(path, msgid, death='generic'):
    wjson(os.path.join(D,'damage_type',path+'.json'),
          {"exhaustion":0.0,"message_id":msgid,"scaling":"when_caused_by_living_non_player"})
dmg('portal_bolt','death.attack.rickmorty.portal_bolt')
dmg('laser','death.attack.rickmorty.laser')
dmg('plasma','death.attack.rickmorty.plasma')
dmg('anomaly','death.attack.rickmorty.anomaly')
dmg('dimension_erase','death.attack.rickmorty.dimension_erase')

# dimension types
def dimtype(name, infiniburn=False):
    wjson(os.path.join(D,'dimension_type',name+'.json'), {
      "ambient_light": 0.0, "bed_works": True, "coordinate_scale": 1.0,
      "effects": "minecraft:overworld", "fixed_time": None, "has_ceiling": False,
      "has_raids": True, "has_skylight": True, "infiniburn": "#minecraft:infiniburn_overworld",
      "logical_height": 384, "min_y": -64, "height": 384, "natural": True,
      "piglin_safe": False, "respawn_anchor_works": False, "ultrawarm": False })

for dn in ['alien_planet','cronenberg_world','citadel','pocket_dimension']:
    dimtype(dn)

# minimal biomes (plains-tinted but with custom sky colors)
def biome(name, sky, fog, water, water_fog, grass, foliage, category, downfall,
          precipitation="rain", particles=None, music=None, spawn=None):
    b = {
      "has_precipitation": True, "precipitation": precipitation, "temperature": 0.6,
      "downfall": downfall, "category": category,
      "effects": {
        "sky_color": sky, "fog_color": fog, "water_color": water, "water_fog_color": water_fog,
        "grass_color": f"#{grass:06x}" if isinstance(grass,int) else grass,
        "foliage_color": f"#{foliage:06x}" if isinstance(foliage,int) else foliage,
        "mood_sound": {"sound":"minecraft:ambient.cave","tick_delay":6000,"block_search_extent":8,"offset":2.0}
      },
      "spawners": {"monster":[],"creature":[],"ambient":[],"axolotls":[],"underground_water_creature":[],"water_ambient":[],"water_creature":[],"misc":[]},
      "spawn_costs": {},
      "features": [[],[],[],[],[],[],[],[],[],[],[]],
      "generators": {"carvers":["minecraft:overworld"]},
      "carvers": [],
    }
    if music is None:
        b["effects"]["music"] = {"min_delay":12000,"max_delay":24000,"replace_current_music":False,"sound":"minecraft:music.creative"}
    fp = os.path.join(D,'worldgen','biome',name+'.json')
    ensure(os.path.dirname(fp))
    wjson(fp, b)

biome('alien_plains', 0x88ffcc, 0x77eebb, 0x3F76E4, 0x50533, 0x7fd8a0, 0x66cc88, 'plains', 0.5)
biome('cronenberg_fields', 0xb0a090, 0xa09080, 0x9c8074, 0x706058, 0x9c8a68, 0x9c8a68, 'plains', 0.9)
biome('citadel_halls', 0x26282e, 0x1e2024, 0x3F76E4, 0x50533, 0x4d515a, 0x4d515a, 'taiga', 0.4)
biome('pocket_void', 0x0c0c14, 0x08080d, 0x14141f, 0x0a0a10, 0x1a1a20, 0x1a1a20, 'the_end', 0.0, precipitation="none")

# dimension settings (chunk generator)
def dimension(name, biome_name, settings='overworld', bed_works=True, alike=None):
    wjson(os.path.join(D,'dimension',name+'.json'), {
      "type": f"{NS}:{name}",
      "generator": {
        "type": f"{NS}:{name.split('_')[0] if False else name}",
        "biome": f"{NS}:{biome_name}",
        "settings": ("default" if settings=='overworld' else settings),
      }})
# NOTE: we reference generator types rickmorty:alien_planet etc.—registered via ModDimensions dynamic registry in Java.

dimension('alien_planet','alien_plains')
dimension('cronenberg_world','cronenberg_fields')
dimension('citadel','citadel_halls')
dimension('pocket_dimension','pocket_void', settings="default")

# =====================================================================
# LOOT TABLES (blocks + entities + a couple of chests)
# =====================================================================
def block_drop(name, item=None, count=1, extra=None):
    pools = [{"rolls":1,"entries":[{"type":"minecraft:item","name":item or f"{NS}:block/{name}"}],
              "conditions":[{"condition":"minecraft:survives_explosion"}]}]
    if extra: pools[0]["entries"].append(extra if isinstance(extra,dict) else {"type":"minecraft:item","name":extra})
    if count not in (1,None):
        pools[0]["entries"][0]["functions"]=[{"function":"minecraft:set_count","count":count}]
    wjson(os.path.join(D,'loot_table','blocks',name+'.json'), {"type":"minecraft:block","pools":pools})

for b in ['sci_fi_metal','citadel_metal','citadel_floor','citadel_stone','dark_matter_block',
          'alien_rock','alien_stone','alien_dirt','pocket_rock','pocket_stone',
          'rick_workbench','portal_machine','quantum_computer','lifeform_detector','dimensional_stabilizer',
          'plumbus_machine','portal_synthesizer','meeseeks_box','lab_glass']:
    block_drop(b)
block_drop('alien_crystal_ore','rickmorty:cosmic_crystal',1)
block_drop('quartz_matter_ore','rickmorty:cosmic_crystal',1)
block_drop('dark_matter_ore','rickmorty:dimensional_shard',1)
block_drop('citadel_remains_ore','rickmorty:dimensional_shard',1)
block_drop('crystal_cluster','rickmorty:cosmic_crystal',1)
block_drop('quartz_cluster','rickmorty:cosmic_crystal',1)
block_drop('portal_fluid_tank',None,1)
block_drop('alien_reactor',None,1)
block_drop('containment_chamber',None,1)
block_drop('containment_core','rickmorty:dimensional_shard',1, extra={'type':'minecraft:item','name':'rickmorty:rift_shard'})
block_drop('unstable_portal','rickmorty:rift_shard',1)
block_drop('portal_block',None,1)

def entity_loot(name, pools):
    wjson(os.path.join(D,'loot_table','entities',name+'.json'), {"type":"minecraft:entity","pools":pools})

def loot_item(item, lo, hi):
    return {"type":"minecraft:item","name":item,"functions":[{"function":"minecraft:set_count","count":{"min":lo,"max":hi}}]}

entity_loot('rick',[{"rolls":1,"entries":[loot_item('rickmorty:advanced_technology',1,2)]},
                    {"rolls":1,"entries":[loot_item('rickmorty:portal_gun',1,1)],"conditions":[{"condition":"minecraft:random_chance","chance":0.15}]}])
entity_loot('morty',[{"rolls":1,"entries":[loot_item('rickmorty:advanced_technology',0,1)]}])
entity_loot('meeseeks',[{"rolls":1,"entries":[loot_item('rickmorty:dimensional_shard',1,2)]}])
entity_loot('cronenberg',[{"rolls":1,"entries":[loot_item('minecraft:rotten_flesh',2,4)]},
                          {"rolls":1,"entries":[loot_item('rickmorty:cronenberg_sample',1,1)],"conditions":[{"condition":"minecraft:random_chance","chance":0.35}]}])
entity_loot('alien_crawler',[{"rolls":1,"entries":[loot_item('rickmorty:cosmic_crystal',1,2)]}])
entity_loot('parasite',[{"rolls":1,"entries":[loot_item('minecraft:bone',1,2)]},
                        {"rolls":1,"entries":[loot_item('rickmorty:cronenberg_sample',1,1)],"conditions":[{"condition":"minecraft:random_chance","chance":0.4}]}])
entity_loot('gazorpian_brute',[{"rolls":1,"entries":[loot_item('rickmorty:cosmic_crystal',1,3)]}])
entity_loot('security_bot',[{"rolls":1,"entries":[loot_item('rickmorty:advanced_technology',1,3)]},
                            {"rolls":1,"entries":[loot_item('rickmorty:energy_cell',1,2)]}])
entity_loot('portal_anomaly',[{"rolls":1,"entries":[loot_item('rickmorty:rift_shard',2,4)]}])
entity_loot('interdimensional_abomination',[
    {"rolls":1,"entries":[loot_item('rickmorty:cosmic_crystal',4,8)]},
    {"rolls":1,"entries":[loot_item('rickmorty:dimensional_shard',3,6)]},
    {"rolls":1,"entries":[loot_item('rickmorty:advanced_technology',2,4)]},
    {"rolls":1,"entries":[loot_item('rickmorty:plasma_launcher',1,1)],"conditions":[{"condition":"minecraft:random_chance","chance":0.5}]},
    {"rolls":1,"entries":[loot_item('rickmorty:portal_stabilizer',1,2)]},
])

# chests: rick lab / citadel vault / alien crash / boss loot chest
def chest(name, pools):
    wjson(os.path.join(D,'loot_table','chests',name+'.json'),
          {"type":"minecraft:chest","pools":pools})
chest('rick_lab',[
 {"rolls":{"min":3,"max":6},"entries":[{"type":"minecraft:item","weight":5,"name":"rickmorty:advanced_technology"},
   {"type":"minecraft:item","weight":6,"name":"rickmorty:energy_cell"},
   {"type":"minecraft:item","weight":4,"name":"rickmorty:portal_fluid_canister"},
   {"type":"minecraft:item","weight":2,"name":"rickmorty:rif" if False else "rickmorty:citadel_schematics"},
   {"type":"minecraft:item","weight":3,"name":"rickmorty:quantum_processor"},
   {"type":"minecraft:item","weight":2,"name":"rickmorty:microverse_battery"}]},
 {"rolls":1,"entries":[{"type":"minecraft:item","name":"rickmorty:portal_gun"}],"conditions":[{"condition":"minecraft:random_chance","chance":0.10}]}])
chest('citadel_vault',[
 {"rolls":{"min":4,"max":7},"entries":[{"type":"minecraft:item","weight":5,"name":"rickmorty:advanced_technology"},
  {"type":"minecraft:item","weight":4,"name":"rickmorty:dimensional_shard"},
  {"type":"minecraft:item","weight":3,"name":"minecraft:gold_ingot"},
  {"type":"minecraft:item","weight":2,"name":"rickmorty:jetpack"},
  {"type":"minecraft:item","weight":2,"name":"rickmorty:cosmic_crystal"},
  {"type":"minecraft:item","weight":1,"name":"rickmorty:dimension_eraser"}]}])
chest('pocket_cache',[
 {"rolls":2,"entries":[{"type":"minecraft:item","weight":6,"name":"rickmorty:cosmic_crystal"},
  {"type":"minecraft:item","weight":4,"name":"rickmorty:enhanced_alien_crystal"}]}])

print('loot tables written')

# =====================================================================
# RECIPES
# =====================================================================
def shaped(name, pattern, key, result, count=1):
    wjson(os.path.join(D,'recipe',name+'.json'),
          {"type":"minecraft:crafting_shaped","category":"misc","pattern":pattern,
           "key":key,"result":{"id":result,"count":count}})
def shapeless(name, inputs, result, count=1):
    wjson(os.path.join(D,'recipe',name+'.json'),
          {"type":"minecraft:crafting_shapeless","category":"misc","ingredients":inputs,
           "result":{"id":result,"count":count}})
def smelting(name, ing, result, xp=0.35, time=200):
    wjson(os.path.join(D,'recipe',name+'.json'),
          {"type":"minecraft:smelting","category":"misc","ingredient":ing,
           "result":{"id":result},"experience":xp,"cookingtime":time})

# sci fi metal
shaped('sci_fi_metal',['III','I I','III'],{'I':{'item':'minecraft:iron_ingot'}},'rickmorty:sci_fi_metal',2)
# lab glass
shapeless('lab_glass',[{'item':'minecraft:glass'},{'item':'minecraft:cyan_dye'}],'rickmorty:lab_glass',8)
# dark matter block
shapeless('dark_matter_block',[{'item':'tronextendedquasar:placeholder' if False else 'minecraft:obsidian'},{'item':'minecraft:ender_pearl'},{'item':'rickmorty:dimensional_shard'}],'rickmorty:dark_matter_block',1)
# workbench
shaped('rick_workbench',['SSS','ICI','SIS'],{'S':{'item':'rickmorty:sci_fi_metal'},'I':{'item':'minecraft:iron_ingot'},'C':{'item':'minecraft:crafting_table'}},'rickmorty:rick_workbench')
# portal machine
shaped('portal_machine',['SRS','RMR','S S'],{'S':{'item':'rickmorty:sci_fi_metal'},'R':{'item':'rickmorty:dimensional_shard'},'M':{'item':'rickmorty:quantum_processor'}},'rickmorty:portal_machine')
# fluid tank
shaped('portal_fluid_tank',['GGG','G G','GGG'],{'G':{'item':'minecraft:glass'}},'rickmorty:portal_fluid_tank')
# quantum computer
shaped('quantum_computer',['SQS','QBQ','SQS'],{'S':{'item':'rickmorty:sci_fi_metal'},'Q':{'item':'rickmorty:quantum_processor'},'B':{'item':'minecraft:beacon'}},'rickmorty:quantum_computer')
# portal stabilizer
shaped('portal_stabilizer',['S S','R R','SSS'],{'S':{'item':'rickmorty:sci_fi_metal'},'R':{'item':'rickmorty:dimensional_shard'}},'rickmorty:portal_stabilizer')
# meeseeks box
shaped('meeseeks_box',['B B','ISI','BBB'],{'B':{'item':'rickmorty:sci_fi_metal'},'I':{'item':'minecraft:iron_ingot'},'S':{'item':'rickmorty:dimensional_shard'}},'rickmorty:meeseeks_box')
# portal gun
shaped('portal_gun',['C G','FQR','C  '],{'C':{'item':'minecraft:copper_ingot'},'G':{'item':'rickmorty:portal_machine'},'F':{'item':'minecraft:glass'},'Q':{'item':'rickmorty:quantum_processor'},'R':{'item':'rickmorty:dimensional_shard'}},'rickmorty:portal_gun')
# portal blaster
shaped('portal_blaster',['CIG',' BX','BX '],{'C':{'item':'minecraft:copper_ingot'},'I':{'item':'minecraft:iron_ingot'},'G':{'item':'minecraft:glass'},'B':{'item':'rickmorty:energy_cell'},'X':{'item':'rickmorty:sci_fi_metal'}},'rickmorty:portal_blaster')
# laser gun
shaped('laser_gun',['CIG',' RB','RB '],{'C':{'item':'minecraft:copper_ingot'},'I':{'item':'minecraft:iron_ingot'},'G':{'item':'minecraft:glass'},'R':{'item':'minecraft:redstone'},'B':{'item':'rickmorty:energy_cell'}},'rickmorty:laser_gun')
# energy cell
shapeless('energy_cell',[{'item':'minecraft:copper_ingot'},{'item':'minecraft:redstone'},{'item':'rickmorty:crystal_cluster'}],'rickmorty:energy_cell',4)
# jetpack
shaped('jetpack',['RFR','TST','RFR'],{'R':{'item':'minecraft:fire_charge' if False else 'rickmorty:sci_fi_metal'},'F':{'item':'rickmorty:portal_fluid_canister'},'T':{'item':'minecraft:tnt'},'S':{'item':'rickmorty:advanced_technology'}},'rickmorty:jetpack')
# canisters
shapeless('empty_canister',[{'item':'minecraft:glass_bottle'},{'item':'minecraft:iron_ingot'}],'rickmorty:empty_canister',4)
# energy canister fill via workbench recipe instead (tier 2)
smelting('cosmic_crystal_from_cluster',{'item':'rickmorty:crystal_cluster' if False else 'rickmorty:quartz_cluster'},'rickmorty:cosmic_crystal',0.5)
# plasma core
shaped('plasma_core',['QGQ','GCG','QGQ'],{'Q':{'item':'rickmorty:quantum_processor'},'G':{'item':'minecraft:glowstone'},'C':{'item':'rickmorty:cosmic_crystal'}},'rickmorty:plasma_core')
# plasma launcher
shaped('plasma_launcher',['PGP','ILI','RER'],{'P':{'item':'rickmorty:plasma_core'},'G':{'item':'minecraft:glass'},'I':{'item':'rickmorty:advanced_technology'},'L':{'item':'rickmorty:sci_fi_metal'},'R':{'item':'minecraft:redstone'},'E':{'item':'rickmorty:energy_cell'}},'rickmorty:plasma_launcher')
# shrink ray
shaped('shrink_ray',['CIG','IXB','  R'],{'C':{'item':'rickmorty:cosmic_crystal'},'I':{'item':'minecraft:iron_ingot'},'G':{'item':'minecraft:glass'},'X':{'item':'rickmorty:advanced_technology'},'B':{'item':'rickmorty:energy_cell'},'R':{'item':'minecraft:redstone'}},'rickmorty:shrink_ray')
# scanner
shaped('interdimensional_scanner',['CGC','GCG','ARA'],{'C':{'item':'minecraft:copper_ingot'},'G':{'item':'minecraft:glass'},'A':{'item':'rickmorty:advanced_technology'},'R':{'item':'minecraft:redstone'}},'rickmorty:interdimensional_scanner')
# grappling hook
shaped('grappling_hook',[' I ','IHI',' S '],{'I':{'item':'minecraft:iron_ingot'},'H':{'item':'minecraft:tripwire_hook'},'S':{'item':'minecraft:string'}},'rickmorty:grappling_hook')
# force field generator
shaped('force_field_generator',['SIS','IDI','SIS'],{'S':{'item':'rickmorty:sci_fi_metal'},'I':{'item':'rickmorty:advanced_technology'},'D':{'item':'rickmorty:dimensional_shard'}},'rickmorty:force_field_generator')
# dimension eraser
shaped('dimension_eraser',['DSD','PPP','DSD'],{'D':{'item':'rickmorty:dimensional_shard'},'S':{'item':'rickmorty:advanced_technology'},'P':{'item':'rickmorty:quantum_processor'}},'rickmorty:dimension_eraser')
# workbench recipes (type rickmorty:workbench)
def wb(name, pattern, key, result, count=1, tier=1):
    wjson(os.path.join(D,'recipe',name+'.json'),
          {"type":"rickmorty:workbench","pattern":pattern,"key":key,"result":{"id":result,"count":count},"tier":tier})
wb('wb_portal_stabilizer',['S S','R R','SSS'],{'S':{'item':'rickmorty:sci_fi_metal'},'R':{'item':'rickmorty:dimensional_shard'}},'rickmorty:portal_stabilizer',1,2)
wb('wb_portal_machine',['SRS','RMR','S S'],{'S':{'item':'rickmorty:sci_fi_metal'},'R':{'item':'rickmorty:dimensional_shard'},'M':{'item':'rickmorty:quantum_processor'}},'rickmorty:portal_machine',1,2)
wb('wb_portal_gun',['C G','FQR','C  '],{'C':{'item':'minecraft:copper_ingot'},'G':{'item':'rickmorty:portal_machine'},'F':{'item':'minecraft:glass'},'Q':{'item':'rickmorty:quantum_processor'},'R':{'item':'rickmorty:dimensional_shard'}},'rickmorty:portal_gun',1,2)
wb('wb_filled_canister',['CEC','ERE','EEE'],{'C':{'item':'rickmorty:empty_canister'},'E':{'item':'rickmorty:energy_cell'},'R':{'item':'rickmorty:crystal_cluster' if False else 'rickmorty:quartz_cluster'}},'rickmorty:filled_portal_fluid_canister',1,1)
wb('wb_quantum_processor',['SSS','SRS','QXQ'],{'S':{'item':'rickmorty:sci_fi_metal'},'R':{'item':'minecraft:redstone'},'Q':{'item':'minecraft:quartz'},'X':{'item':'rickmorty:advanced_technology'}},'rickmorty:quantum_processor',1,3)
wb('wb_dimensional_shard',['RGR','GCG','RGR'],{'R':{'item':'rickmorty:rift_shard'},'G':{'item':'rickmorty:crystal_cluster' if False else 'rickmorty:quartz_cluster'},'C':{'item':'rickmorty:cosmic_crystal'}},'rickmorty:dimensional_shard',1,3)

print('recipes written')

# damage tags: make projectiles ignore armor bypass effects?
wjson(os.path.join(D,'tags','damage_type', 'bypasses_armor.json'),
      {"replace": False, "values": [f"{NS}:plasma", f"{NS}:dimension_erase"]})

# item tags for ammo progress
wjson(p('data',NS,'tags','item','rif' if False else 'None.json'), {"values": []}) if False else None

# datapack folder structure ok.
print('DATA JSON done.')
