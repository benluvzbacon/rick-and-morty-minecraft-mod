#!/usr/bin/env python3
"""Aligns generated resources with the REAL registered ids in ModBlocks/ModItems/ModEntities.
Removes bogus ids, creates missing textures/models/lang/recipes/loot. Deterministic."""
import json, os, shutil

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
R = os.path.join(REPO, 'src', 'main', 'resources')
NS = 'rickmorty'
def p(*a): return os.path.join(R, *a)
def osj(*a): return os.path.join(*a)

# ---------- real ids ----------
BLOCKS = ['sci_fi_metal','citadel_metal','lab_glass','alien_rock','pocket_rock','crystal_cluster',
 'alien_crystal_ore','pocket_crystal_ore','portal_energy_block','dark_matter_block',
 'rick_workbench','portal_machine','portal_fluid_tank','alien_reactor','plumbus_machine',
 'meeseeks_box','quantum_computer','interdimensional_storage','portal_block','unstable_portal','containment_core']
ITEMS = ['portal_gun','portal_blaster','laser_gun','plasma_launcher','dimension_eraser','shrink_ray',
 'grappling_hook','force_field_generator','interdimensional_scanner','jetpack','portal_stabilizer',
 'microverse_battery' if False else '']  # placeholder
ITEMS = [x for x in ITEMS if x]
ITEMS += ['energy_cell','plasma_cell','filled_portal_fluid_canister','portal_fluid_canister',
 'alien_crystal','pocket_crystal','interdimensional_crystal','chitin','mutant_tissue','cronenberg_stew',
 'alien_alloy_ingot','dark_matter','dark_matter_component','advanced_circuitry','quantum_circuit',
 'portal_gun_frame','robot_plating','simple_wafer','rift_shard','plumbus','rick_flask',
 'rick_spawn_egg','morty_spawn_egg','meeseeks_spawn_egg','cronenberg_spawn_egg','alien_crawler_spawn_egg',
 'parasite_spawn_egg','gazorpian_spawn_egg','security_bot_spawn_egg','portal_anomaly_spawn_egg']

def wjson(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path,'w') as f: json.dump(obj, f, indent=2)

def mv_texture(kind, src, dst):
    a = p('assets',NS,'textures',kind, src + '.png')
    b = p('assets',NS,'textures',kind, dst + '.png')
    if os.path.exists(a) and not os.path.exists(b):
        shutil.copy(a, b)

def rm(path):
    if os.path.exists(path): os.remove(path)

# ---------- textures: derive + prune ----------
# item textures mapping: new_id -> art to reuse
mv_texture('item','advanced_technology','advanced_circuitry')
mv_texture('item','quantum_processor','quantum_circuit')
mv_texture('item','plasma_core','plasma_cell')
mv_texture('item','cosmic_crystal','interdimensional_crystal')
mv_texture('item','alien_crystal_shim','alien_crystal')
mv_texture('item','cosmic_crystal','pocket_crystal')
mv_texture('item','dimensional_shard','rift_shard' ) if False else None
mv_texture('item','cronenberg_sample','mutant_tissue')
mv_texture('item','cronenberg_sample','cronenberg_stew')
mv_texture('item','advanced_technology','simple_wafer')
mv_texture('item','advanced_technology','robot_plating')
mv_texture('item','advanced_technology','dark_matter_component')
mv_texture('item','cosmic_crystal','dark_matter')
mv_texture('item','advanced_technology','alien_alloy_ingot')
mv_texture('item','advanced_technology','chitin')
mv_texture('item','portal_gun','portal_gun_frame')
# delete bogus item textures & models
BOGUS_ITEMS = ['advanced_technology','cosmic_crystal','quantum_processor','dimensional_shard',
 'empty_canister','cronenberg_sample','alien_crystal_shim','microverse_battery','microverse_battery_lore',
 'citadel_schematics','alien_log','portal_fluid_canister_shim' if False else 'none_xyz']
for b in [x for x in BOGUS_ITEMS if x not in ('alien_crystal',)]:
    rm(p('assets',NS,'textures','item',b+'.png'))
    rm(p('assets',NS,'models','item',b+'.json'))

# block texture renames
mv_texture('block','quartz_matter_ore','pocket_crystal_ore')
mv_texture('block','quartz_cluster','crystal_cluster')
mv_texture('block','containment_core','portal_energy_block')
mv_texture('block','meeseeks_box_top','interdimensional_storage')

# delete bogus block textures/models/blockstates
BOGUS_BLOCKS = ['quartz_cluster','quartz_matter_ore','alien_stone','alien_dirt','alien_grass_top',
 'citadel_stone','citadel_floor','pocket_stone','containment_chamber','lifeform_detector',
 'dimensional_stabilizer','portal_synthesizer','dark_matter_ore','citadel_remains_ore',
 'portal_fluid_tank_frame','portal_fluid']
for b in BOGUS_BLOCKS:
    rm(p('assets',NS,'textures','block',b+'.png'))
    rm(p('assets',NS,'models','block',b+'.json'))
    rm(p('assets',NS,'blockstates',b+'.json'))
    rm(p('assets',NS,'models','item',b+'.json'))
    rm(p('data',NS,'loot_table','blocks',b+'.json'))
    rm(p('data',NS,'recipe',b+'.json'))

# keep tank frame/fluid textures (used in models), restore if deleted:
# they are referenced by portal_fluid_tank_*.json models -> regenerate simple strips if missing
import random, struct, zlib
random.seed(99)
def write_png(path, img):
    def chunk(t,data):
        c=t+data
        return struct.pack('>I',len(data))+c+struct.pack('>I',zlib.crc32(c)&0xffffffff)
    h=len(img); w=len(img[0])
    raw=b''.join(b'\x00'+b''.join(bytes(bytearray(int(v) for v in px)) for px in row) for row in img)
    with open(path,'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b''))

def solid16(c, amt=8):
    img=[[c for _ in range(16)] for _ in range(16)]
    for y in range(16):
        for x in range(16):
            n=random.randint(-amt,amt)
            img[y][x]=(max(0,min(255,c[0]+n)),max(0,min(255,c[1]+n)),max(0,min(255,c[2]+n)),c[3])
    return img

# tank parts already exist? keep if present; if deleted path guard:
tf = p('assets',NS,'textures','block','portal_fluid_tank_frame.png')
if not os.path.exists(tf):
    os.makedirs(os.path.dirname(tf),exist_ok=True)
    img=[[(0,0,0,0)]*16 for _ in range(16)]
    for i in range(16):
        img[0][i]=(110,116,130,255); img[15][i]=(110,116,130,255); img[i][0]=(110,116,130,255); img[i][15]=(110,116,130,255)
    write_png(tf,img)
tf2 = p('assets',NS,'textures','block','portal_fluid.png')
if not os.path.exists(tf2):
    write_png(tf2, solid16((40,220,110,235),20))

# ---------- models & blockstates for real blocks ----------
simple_cube = ['sci_fi_metal','citadel_metal','lab_glass','alien_rock','pocket_rock',
 'alien_crystal_ore','pocket_crystal_ore','portal_energy_block','dark_matter_block',
 'plumbus_machine','quantum_computer','interdimensional_storage','containment_core']
for n in simple_cube:
    wjson(p('assets',NS,'models','block',n+'.json'),
          {"parent":"minecraft:block/cube_all","textures":{"all":f"{NS}:block/{n}"}})
    wjson(p('assets',NS,'blockstates',n+'.json'), {"variants":{"":{"model":f"{NS}:block/{n}"}}})
    wjson(p('assets',NS,'models','item',n+'.json'), {"parent":f"{NS}:block/{n}"})

# crystal cluster cross
wjson(p('assets',NS,'models','block','crystal_cluster.json'),
      {"parent":"minecraft:block/cross","textures":{"cross":f"{NS}:block/crystal_cluster"}})
wjson(p('assets',NS,'blockstates','crystal_cluster.json'), {"variants":{"":{"model":f"{NS}:block/crystal_cluster"}}})
wjson(p('assets',NS,'models','item','crystal_cluster.json'),
      {"parent":"minecraft:item/generated","textures":{"layer0":f"{NS}:block/crystal_cluster"}})

# item models for all real items (skip if exists)
for n in ITEMS:
    m = p('assets',NS,'models','item',n+'.json')
    if not os.path.exists(m):
        wjson(m, {"parent":"minecraft:item/generated","textures":{"layer0":f"{NS}:item/{n}"}})

# every real item texture must exist -> final guard: synthesize placeholder-art from generic chip
for n in ITEMS:
    t = p('assets',NS,'textures','item',n+'.png')
    if not os.path.exists(t):
        base = solid16((120,140,180,255),18)
        hs = hash(n) & 0xffffff
        r=(hs>>16)&255; g=(hs>>8)&255; bb=hs&255
        for y in range(3,13):
            for x in range(3,13):
                base[y][x]=(64+((r*x+y*3)%160),64+((g*y+x*2)%160),64+((bb*(x+y))%160),255)
        write_png(t, base); print('filler texture', n)

# ---------- loot tables (real ids) ----------
def block_loot(name, drop, count=1):
    pools=[{"rolls":1,"entries":[{"type":"minecraft:item","name":drop}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]
    if count!=1: pools[0]["entries"][0]["functions"]=[{"function":"minecraft:set_count","count":{"min":1,"max":count}}]
    wjson(p('data',NS,'loot_table','blocks',name+'.json'), {"type":"minecraft:block","pools":pools})

for b in ['sci_fi_metal','citadel_metal','alien_rock','pocket_rock','dark_matter_block',
 'rick_workbench','portal_machine','quantum_computer','plumbus_machine','meeseeks_box',
 'interdimensional_storage','portal_fluid_tank','alien_reactor','containment_core','lab_glass','portal_energy_block']:
    block_loot(b, f'{NS}:{b}')
block_loot('alien_crystal_ore', f'{NS}:alien_crystal')
block_loot('pocket_crystal_ore', f'{NS}:pocket_crystal')
block_loot('crystal_cluster', f'{NS}:alien_crystal')
block_loot('unstable_portal', f'{NS}:rift_shard')
block_loot('portal_block', 'minecraft:air')

def ent_loot(name, pools):
    wjson(p('data',NS,'loot_table','entities',name+'.json'), {"type":"minecraft:entity","pools":pools})
def L(item, lo, hi):
    return {"type":"minecraft:item","name":item,"functions":[{"function":"minecraft:set_count","count":{"min":lo,"max":hi}}]}
ent_loot('rick',[{"rolls":1,"entries":[L(f'{NS}:advanced_circuitry',1,2)]},
 {"rolls":1,"entries":[L(f'{NS}:portal_gun',1,1)],"conditions":[{"condition":"minecraft:random_chance","chance":0.1}]}])
ent_loot('morty',[{"rolls":1,"entries":[L(f'{NS}:advanced_circuitry',0,1)]}])
ent_loot('meeseeks',[{"rolls":1,"entries":[L(f'{NS}:rift_shard',1,2)]}])
ent_loot('cronenberg_mutant',[{"rolls":1,"entries":[L('minecraft:rotten_flesh',2,4)]},
 {"rolls":1,"entries":[L(f'{NS}:mutant_tissue',1,1)],"conditions":[{"condition":"minecraft:random_chance","chance":0.35}]}])
ent_loot('alien_crawler',[{"rolls":1,"entries":[L(f'{NS}:alien_crystal',1,2)]},
 {"rolls":1,"entries":[L(f'{NS}:chitin',0,2)]}])
ent_loot('parasite',[{"rolls":1,"entries":[L('minecraft:bone',1,2)]},
 {"rolls":1,"entries":[L(f'{NS}:mutant_tissue',1,1)],"conditions":[{"condition":"minecraft:random_chance","chance":0.4}]}])
ent_loot('gazorpian_brute',[{"rolls":1,"entries":[L(f'{NS}:alien_crystal',1,3)]}])
ent_loot('security_bot',[{"rolls":1,"entries":[L(f'{NS}:advanced_circuitry',1,3)]},
 {"rolls":1,"entries":[L(f'{NS}:energy_cell',1,2)]},{"rolls":1,"entries":[L(f'{NS}:robot_plating',0,2)]}])
ent_loot('portal_anomaly',[{"rolls":1,"entries":[L(f'{NS}:rift_shard',2,4)]}])
ent_loot('interdimensional_abomination',[
 {"rolls":1,"entries":[L(f'{NS}:pocket_crystal',3,6)]},
 {"rolls":1,"entries":[L(f'{NS}:alien_crystal',3,6)]},
 {"rolls":1,"entries":[L(f'{NS}:rift_shard',4,8)]},
 {"rolls":1,"entries":[L(f'{NS}:dark_matter',2,4)]},
 {"rolls":1,"entries":[L(f'{NS}:plasma_launcher',1,1)],"conditions":[{"condition":"minecraft:random_chance","chance":0.5}]}])

chestid = p('data',NS,'loot_table','chests')
wjson(os.path.join(chestid,'rick_lab.json'),{"type":"minecraft:chest","pools":[
 {"rolls":{"min":3,"max":6},"entries":[
  {"type":"minecraft:item","weight":5,"name":f"{NS}:advanced_circuitry"},
  {"type":"minecraft:item","weight":6,"name":f"{NS}:energy_cell"},
  {"type":"minecraft:item","weight":4,"name":f"{NS}:portal_fluid_canister"},
  {"type":"minecraft:item","weight":3,"name":f"{NS}:quantum_circuit"},
  {"type":"minecraft:item","weight":2,"name":f"{NS}:portal_gun_frame"},
  {"type":"minecraft:item","weight":2,"name":f"{NS}:alien_alloy_ingot"}]},
 {"rolls":1,"entries":[{"type":"minecraft:item","name":f"{NS}:portal_gun"}],
  "conditions":[{"condition":"minecraft:random_chance","chance":0.1}]}]})
wjson(os.path.join(chestid,'citadel_vault.json'),{"type":"minecraft:chest","pools":[
 {"rolls":{"min":4,"max":7},"entries":[{"type":"minecraft:item","weight":5,"name":f"{NS}:advanced_circuitry"},
  {"type":"minecraft:item","weight":4,"name":f"{NS}:dark_matter"},
  {"type":"minecraft:item","weight":3,"name":"minecraft:gold_ingot"},
  {"type":"minecraft:item","weight":2,"name":f"{NS}:jetpack"},
  {"type":"minecraft:item","weight":2,"name":f"{NS}:pocket_crystal"},
  {"type":"minecraft:item","weight":1,"name":f"{NS}:dimension_eraser"}]}]})
wjson(os.path.join(chestid,'pocket_cache.json'),{"type":"minecraft:chest","pools":[
 {"rolls":2,"entries":[{"type":"minecraft:item","weight":6,"name":f"{NS}:pocket_crystal"},
  {"type":"minecraft:item","weight":4,"name":f"{NS}:interdimensional_crystal"},
  {"type":"minecraft:item","weight":3,"name":f"{NS}:dark_matter"}]}]})
wjson(os.path.join(chestid,'abomination_aux.json'),{"type":"minecraft:chest","pools":[
 {"rolls":3,"entries":[{"type":"minecraft:item","weight":4,"name":f"{NS}:plasma_cell"},
  {"type":"minecraft:item","weight":3,"name":f"{NS}:filled_portal_fluid_canister"}]}]})
# clean stray bad chest tables
for f in os.listdir(chestid):
    if f not in ('rick_lab.json','citadel_vault.json','pocket_cache.json','abomination_aux.json'):
        rm(os.path.join(chestid,f)); print('pruned chest', f)

# ---------- recipes (real ids) ----------
rd = p('data',NS,'recipe')
for f in list(os.listdir(rd)): rm(os.path.join(rd,f))
def shaped(name, pattern, key, result, count=1):
    wjson(os.path.join(rd,name+'.json'), {"type":"minecraft:crafting_shaped","category":"misc",
        "pattern":pattern,"key":key,"result":{"id":result,"count":count}})
def shapeless(name, ings, result, count=1):
    wjson(os.path.join(rd,name+'.json'), {"type":"minecraft:crafting_shapeless","category":"misc",
        "ingredients":ings,"result":{"id":result,"count":count}})
def smelt(name, ing, result, xp=0.4):
    wjson(os.path.join(rd,name+'.json'), {"type":"minecraft:smelting","category":"misc",
        "ingredient":ing,"result":{"id":result},"experience":xp,"cookingtime":200})
def wb(name, pattern, key, result, count=1, tier=1):
    wjson(os.path.join(rd,name+'.json'), {"type":"rickmorty:workbench","pattern":pattern,
        "key":key,"result":{"id":result,"count":count},"tier":tier})

I='minecraft:iron_ingot'; C='minecraft:copper_ingot'; RDT='minecraft:redstone'
AC=f'{NS}:alien_crystal'; PC=f'{NS}:pocket_crystal'; IC=f'{NS}:interdimensional_crystal'
ADV=f'{NS}:advanced_circuitry'; QC=f'{NS}:quantum_circuit'; PL=f'{NS}:robot_plating'
EC=f'{NS}:energy_cell'; PCELL=f'{NS}:plasma_cell'; SF=f'{NS}:sci_fi_metal'; DM=f'{NS}:dark_matter'
AA=f'{NS}:alien_alloy_ingot'; RS=f'{NS}:rift_shard'; PGF=f'{NS}:portal_gun_frame'; SW=f'{NS}:simple_wafer'

shaped('sci_fi_metal',['IAI','ACA','IAI'],{'I':{'item':I},'A':{'item':C},'C':{'item':'minecraft:cobblestone'}},f'{NS}:sci_fi_metal',4)
shapeless('lab_glass',[{'item':'minecraft:glass'},{'item':'minecraft:cyan_dye'}],f'{NS}:lab_glass',8)
shapeless('simple_wafer',[{'item':I},{'item':RDT}],f'{NS}:simple_wafer',4)
shaped('robot_plating',['IA','AI','  '],{'I':{'item':I},'A':{'item':AA}},f'{NS}:robot_plating',4)
shaped('advanced_circuitry',['WWW','RQR','WWW'],{'W':{'item':SW},'R':{'item':RDT},'Q':{'item':'minecraft:quartz'}},f'{NS}:advanced_circuitry',2)
shaped('quantum_circuit',['EAE','AQA','EAE'],{'E':{'item':'minecraft:ender_pearl'},'A':{'item':ADV},'Q':{'item':QC if False else 'minecraft:quartz'}},f'{NS}:quantum_circuit')
shaped('energy_cell',['CRC','RGR','CRC'],{'C':{'item':C},'R':{'item':RDT},'G':{'item':AC}},f'{NS}:energy_cell',2)
shaped('plasma_cell',[' E ','GCG',' E '],{'E':{'item':EC},'G':{'item':'minecraft:glowstone_dust'},'C':{'item':AC}},f'{NS}:plasma_cell',2)
shapeless('alien_alloy_ingot',[{'item':I},{'item':AC},{'item':C}],f'{NS}:alien_alloy_ingot',2)
shaped('dark_matter_block',['DDD','DDD','DDD'],{'D':{'item':DM}},f'{NS}:dark_matter_block')
shapeless('dark_matter',[{'item':f'{NS}:dark_matter_block'}],DM,9)
shapeless('dark_matter_component',[{'item':DM},{'item':AA},{'item':ADV}],f'{NS}:dark_matter_component',2)
shapeless('interdimensional_crystal',[{'item':AC},{'item':PC},{'item':'minecraft:ender_pearl'}],IC)
shaped('crystal_cluster',['A A',' A ','A A'],{'A':{'item':AC}},f'{NS}:crystal_cluster')
shapeless('cronenberg_stew',[{'item':'minecraft:bowl'},{'item':'minecraft:rotten_flesh'},{'item':f'{NS}:mutant_tissue'}],f'{NS}:cronenberg_stew')
shaped('portal_gun_frame',['PPP','GGP','   '],{'P':{'item':PL},'G':{'item':'minecraft:glass'}},PGF)
shaped('portal_machine',['SAS','AEA','SAS'],{'S':{'item':SF},'A':{'item':ADV},'E':{'item':EC}},f'{NS}:portal_machine')
shaped('portal_fluid_tank',['GGG','G G','SSS'],{'G':{'item':'minecraft:glass'},'S':{'item':SF}},f'{NS}:portal_fluid_tank')
shaped('portal_fluid_canister',[' G ','GBG',' S '],{'G':{'item':'minecraft:glass'},'B':{'item':'minecraft:iron_nugget'},'S':{'item':SF}},f'{NS}:portal_fluid_canister',2)
shaped('interdimensional_scanner',['CGC','GAG','SRS'],{'C':{'item':C},'G':{'item':'minecraft:glass'},'A':{'item':ADV},'S':{'item':SF},'R':{'item':RDT}},f'{NS}:interdimensional_scanner')
shaped('grappling_hook',[' I ','IHI',' S '],{'I':{'item':I},'H':{'item':'minecraft:tripwire_hook'},'S':{'item':'minecraft:string'}},f'{NS}:grappling_hook')
shaped('magnet_hook_filler_disabled' if False else 'laser_gun',['CGC','AEE','S S '],
  {'C':{'item':C},'G':{'item':'minecraft:glass' if False else 'minecraft:redstone'},'A':{'item':ADV},'E':{'item':EC},'S':{'item':SF}},
  f'{NS}:laser_gun')
shaped('portal_blaster',['PG ','CAG','SP '],{'P':{'item':PL},'G':{'item':'minecraft:glass'},'C':{'item':PGF},'A':{'item':ADV},'S':{'item':SF}},f'{NS}:portal_blaster')
shaped('rick_flask',[' G ','GAG','GGG'],{'G':{'item':'minecraft:glass'},'A':{'item':f'{NS}:filled_portal_fluid_canister'}},f'{NS}:rick_flask')
smelt('alien_alloy_ingot_from_scrap',{'item':PL},f'{NS}:alien_alloy_ingot')
smelt('energy_cell_from_crystal',{'item':AC},EC,0.6)

wb('wb_portal_gun',['FGC','AQA','C R'],{'F':{'item':PGF},'G':{'item':'minecraft:glass'},'C':{'item':QC},'A':{'item':IC},'Q':{'item':QC},'R':{'item':EC}},f'{NS}:portal_gun',1,2)
wb('wb_portal_stabilizer',['ASA','RER','SSS'],{'A':{'item':AA},'S':{'item':SF},'R':{'item':RS},'E':{'item':EC}},f'{NS}:portal_stabilizer',1,2)
wb('wb_portal_machine',['SQS','ICI','SQS'],{'S':{'item':SF},'Q':{'item':QC},'I':{'item':AA},'C':{'item':IC}},f'{NS}:portal_machine',1,2)
wb('wb_quantum_computer',['SQS','QAQ','SQS'],{'S':{'item':SF},'Q':{'item':QC},'A':{'item':'minecraft:beacon'}},f'{NS}:quantum_computer',1,2)
wb('wb_meeseeks_box',['SBS','ARA','SSS'],{'S':{'item':SF},'B':{'item':'minecraft:light_blue_dye'},'A':{'item':ADV},'R':{'item':RS}},f'{NS}:meeseeks_box',1,1)
wb('wb_filled_canister',['A A','CBC','EEE'],{'A':{'item':AC},'C':{'item':f'{NS}:portal_fluid_canister'},'B':{'item':EC},'E':{'item':AA}},f'{NS}:filled_portal_fluid_canister',1,1)
wb('wb_jetpack',['PTP','FEF','PFP'],{'P':{'item':PL},'T':{'item':f'{NS}:filled_portal_fluid_canister'},'F':{'item':SF},'E':{'item':QC}},f'{NS}:jetpack',1,3)
wb('wb_shrink_ray',['CGC','AQE','S S'],{'C':{'item':IC},'G':{'item':'minecraft:glass'},'A':{'item':PGF},'Q':{'item':QC},'E':{'item':EC},'S':{'item':SF}},f'{NS}:shrink_ray',1,2)
wb('wb_plasma_launcher',['PPP','AQA','SES'],{'P':{'item':PCELL},'A':{'item':PGF},'Q':{'item':QC},'S':{'item':SF},'E':{'item':EC}},f'{NS}:plasma_launcher',1,3)
wb('wb_force_field',['SAS','IDI','SAS'],{'S':{'item':SF},'A':{'item':AA},'I':{'item':IC},'D':{'item':ADV}},f'{NS}:force_field_generator',1,2)
wb('wb_dimension_eraser',['IDI','DMD','IQI'],{'I':{'item':IC},'D':{'item':f'{NS}:dark_matter_component'},'M':{'item':DM},'Q':{'item':QC}},f'{NS}:dimension_eraser',1,3)
wb('wb_interdimensional_crystal',['APG','PEP','GPA'],{'A':{'item':AC},'P':{'item':PC},'G':{'item':'minecraft:glowstone_dust'},'E':{'item':RS}},IC,2,3)

# kill stale bad recipe files
for f in list(os.listdir(rd)):
    if f in ('quantum_computer.json','plasma_core.json'): rm(os.path.join(rd,f))

# ---------- lang ----------
L = {
 "itemGroup.rickmorty":"Rick & Morty Tech",
 "key.categories.rickmorty":"Rick & Morty",
 "key.rickmorty.scanner":"Open Scanner",
 "key.rickmorty.jetpack":"Toggle Jetpack",
}
BLOCKS_NICE = {'portal_block':'Portal','unstable_portal':'Unstable Portal','rick_workbench':"Rick's Workbench",
 'portal_machine':'Portal Machine','portal_fluid_tank':'Portal Fluid Tank','meeseeks_box':'Meeseeks Box',
 'quantum_computer':'Quantum Computer','sci_fi_metal':'Sci-Fi Metal','citadel_metal':'Citadel Metal',
 'dark_matter_block':'Dark Matter Block','alien_rock':'Alien Rock','pocket_rock':'Pocket Rock',
 'crystal_cluster':'Crystal Cluster','alien_crystal_ore':'Alien Crystal Ore','pocket_crystal_ore':'Pocket Crystal Ore',
 'portal_energy_block':'Portal Energy Block','alien_reactor':'Alien Reactor','plumbus_machine':'Plumbus Machine',
 'interdimensional_storage':'Interdimensional Storage','lab_glass':'Lab Glass','containment_core':'Containment Core'}
for k,v in BLOCKS_NICE.items(): L[f'block.rickmorty.{k}']=v
ENTITIES = {'rick':'Rick Sanchez','morty':'Morty Smith','meeseeks':'Meeseeks','cronenberg_mutant':'Cronenberg',
 'alien_crawler':'Alien Crawler','parasite':'Cronenberg Parasite','gazorpian_brute':'Gazorpian Brute',
 'security_bot':'Security Bot','portal_anomaly':'Portal Anomaly','interdimensional_abomination':'Interdimensional Abomination',
 'energy_bolt':'Energy Bolt','grapnel':'Grapnel'}
for k,v in ENTITIES.items(): L[f'entity.rickmorty.{k}']=v
ITEMS_NICE = {'portal_gun':'Portal Gun','portal_blaster':'Portal Blaster','laser_gun':'Laser Gun',
 'plasma_launcher':'Plasma Launcher','dimension_eraser':'Dimension Eraser','shrink_ray':'Shrink Ray',
 'grappling_hook':'Grappling Hook','force_field_generator':'Force Field Generator',
 'interdimensional_scanner':'Interdimensional Scanner','jetpack':'Jetpack','portal_stabilizer':'Portal Stabilizer',
 'energy_cell':'Energy Cell','plasma_cell':'Plasma Cell','filled_portal_fluid_canister':'Filled Portal Fluid Canister',
 'portal_fluid_canister':'Portal Fluid Canister','alien_crystal':'Alien Crystal','pocket_crystal':'Pocket Crystal',
 'interdimensional_crystal':'Interdimensional Crystal','chitin':'Chitin','mutant_tissue':'Mutant Tissue',
 'cronenberg_stew':'Cronenberg Stew','alien_alloy_ingot':'Alien Alloy Ingot','dark_matter':'Dark Matter',
 'dark_matter_component':'Dark Matter Component','advanced_circuitry':'Advanced Circuitry',
 'quantum_circuit':'Quantum Circuit','portal_gun_frame':'Portal Gun Frame','robot_plating':'Robot Plating',
 'simple_wafer':'Simple Wafer','rift_shard':'Rift Shard','plumbus':'Plumbus','rick_flask':"Rick's Flask"}
for k,v in ITEMS_NICE.items(): L[f'item.rickmorty.{k}']=v
for k,v in ENTITIES.items():
    L[f'item.rickmorty.{k}_spawn_egg']=v+' Spawn Egg'
# lore lines
L.update({
 'item.rickmorty.portal_gun.lore.1':'A one-of-a-kind interdimensional transit device.',
 'item.rickmorty.portal_gun.lore.2':"Sneak+Right-click air: cycle target dimension.",
 'item.rickmorty.portal_gun.lore.3':"Hold a Stabilizer for cheaper, longer-lived portals.",
 'item.rickmorty.alien_crystal.lore.1':'Crystallized exotic matter from alien strata.',
 'item.rickmorty.alien_crystal.lore.2':'Primary material for mid-tech devices.',
 'item.rickmorty.pocket_crystal.lore.1':'Hyper-dense crystal from a pocket dimension.',
 'item.rickmorty.pocket_crystal.lore.2':'Do not lick. Do not store near clocks.',
 'item.rickmorty.interdimensional_crystal.lore.1':'Two crystals, one shared destiny.',
 'item.rickmorty.plumbus.lore.1':'Everyone knows what a plumbus is.',
 'item.rickmorty.rift_shard.lore.1':'Torn from an unstable portal. Still warm.',
 'item.rickmorty.dimension_eraser.lore.1':'Removes things from the observed universe.',
 'item.rickmorty.dimension_eraser.lore.2':'Config-gated, for obvious reasons.',
 'item.rickmorty.jetpack.lore.1':'Equip on the chest slot. Press J to toggle.',
 'item.rickmorty.jetpack.lore.2':'Right-click with hand: refuel (+400 / canister).',
 'item.rickmorty.interdimensional_scanner.lore.1':'Right-click for a quick census.',
 'item.rickmorty.interdimensional_scanner.lore.2':'A full report lands in the scanner screen (V).',
 'item.rickmorty.rick_flask.lore.1':'Liquid courage of questionable legality.',
 'item.rickmorty.cronenberg_stew.lore.1':'It was the humans who needed to be fixed.',
 'item.rickmorty.dark_matter.lore.1':'Condensed dimension-stuff. Handle gently.',
 'item.rickmorty.mutant_tissue.lore.1':'Sample of transformed genetic matter.',
 'item.rickmorty.shrink_ray.lore.1':'For when problems should literally look smaller.',
 'item.rickmorty.force_field_generator.lore.1':'Personal safety dome, no refunds.',
 'item.rickmorty.portal_blaster.lore.1':'Standard issue sidearm.',
 'item.rickmorty.laser_gun.lore.1':'Burns through energy cells for extra punch.',
 'item.rickmorty.plasma_launcher.lore.1':'Fires slow, hits like a collapsing star. Recoil!',
 'item.rickmorty.grappling_hook.lore.1':'Swing physics, physics of a different solar system.',
 'item.rickmorty.portal_stabilizer.lore.1':'Hold while firing to stabilize the portal.',
 # hud/gui
 'hud.rickmorty.gun_charges':'Charges: %s','hud.rickmorty.jetpack_fuel':'Jetpack [%s]',
 'hud.rickmorty.on':'ON','hud.rickmorty.off':'OFF',
 'gui.rickmorty.workbench.tier':'Tier %s','gui.rickmorty.scanner.title':'Interdimensional Scanner',
 'gui.rickmorty.scanner.no_data':'No scan data. Use the scanner in the world first.',
 # messages
 'msg.rickmorty.gun.target':'Dimension target: %s','msg.rickmorty.gun.no_fluid':'Portal gun is empty.',
 'msg.rickmorty.gun.no_space':'No room for a portal there.',
 'msg.rickmorty.gun.linked':'Portals linked. Step through.',
 'msg.rickmorty.gun.half_link':'First half placed. Complete the link elsewhere.',
 'msg.rickmorty.gun.dim_portal':'Dimension door opened. It will not linger.',
 'msg.rickmorty.gun.green':'Portal A (green) placed.','msg.rickmorty.gun.blue':'Portal B (blue) placed.',
 'msg.rickmorty.jetpack.on':'Jetpack ONLINE','msg.rickmorty.jetpack.off':'Jetpack OFFLINE',
 'msg.rickmorty.jetpack.empty':'Jetpack tank is empty.',
 'msg.rickmorty.jetpack.no_chest':'Equip the jetpack on your chest first.',
 'msg.rickmorty.jetpack.refuel':'Refueled: %s/800',
 'msg.rickmorty.eraser.disabled':'Dimension eraser is disabled in config.',
 'msg.rickmorty.eraser.erased':'Instance erased from spacetime.',
 'msg.rickmorty.event.portal_storm':'A portal storm approaches. Reality gets weird.',
 'msg.rickmorty.event.rick_arrival':'You hear a very specific belch nearby.',
 'msg.rickmorty.event.citadel_patrol':'Citadel patrol dispatched to your sector.',
 'msg.rickmorty.event.anomaly':'A dimensional anomaly resonates nearby.',
 # sound subtitles
 'sound.rickmorty.portal.teleport':'Portal whoosh','sound.rickmorty.portal.open':'Portal opens',
 'sound.rickmorty.portal.close':'Portal closes','sound.rickmorty.portal.storm':'Portal storm rumbles',
 'sound.rickmorty.gun.zap':'Portal gun zap','sound.rickmorty.gun.charge':'Charging up',
 'sound.rickmorty.gun.fail':'Gun fizzles','sound.rickmorty.workbench.craft':'Workbench hums',
 'sound.rickmorty.boss.roar':'Abomination roars','sound.rickmorty.boss.retreat':'Abomination retreats',
 'sound.rickmorty.anomaly':'Reality groans','sound.rickmorty.laser.shot':'Laser fires',
 'sound.rickmorty.plasma.fire':'Plasma launches','sound.rickmorty.shrink.zap':'Shrink ray fires',
 'sound.rickmorty.grapnel.release':'Grapnel releases',
})
wjson(p('assets',NS,'lang','en_us.json'), L)
print('lang', len(L))

# ---------- dimensions: fix biome names ----------
for dim, bio, preset in [('alien_planet','alien_fields','alien'),('cronenberg_world','cronenberg_wastes','cronenberg'),
     ('citadel','citadel_plaza','citadel'),('pocket_dimension','pocket_void','pocket')]:
    wjson(p('data',NS,'dimension',dim+'.json'), {
        "type": f"{NS}:{dim}",
        "generator": {"type": f"{NS}:rm_generator", "biome": f"{NS}:{bio}", "preset": preset}})
for old in ['alien_plains','cronenberg_fields','citadel_halls']:
    rm(p('data',NS,'worldgen','biome',old+'.json'))
print('dimensions fixed')
print('FIX-UP DONE')
