import re, sys, os
COLORS = ['LIGHT_BLUE','LIGHT_GRAY','WHITE','ORANGE','MAGENTA','YELLOW','LIME','PINK','GRAY','CYAN','PURPLE','BLUE','BROWN','GREEN','RED','BLACK']
CAMEL = {c: (c.lower().split('_')[0] + ''.join(w.capitalize() for w in c.lower().split('_')[1:])) for c in COLORS}
ITEM_COLL = {'WOOL':'WOOL','TERRACOTTA':'DYED_TERRACOTTA','CARPET':'CARPET','STAINED_GLASS':'STAINED_GLASS','STAINED_GLASS_PANE':'STAINED_GLASS_PANE','SHULKER_BOX':'DYED_SHULKER_BOX','GLAZED_TERRACOTTA':'GLAZED_TERRACOTTA','CONCRETE':'CONCRETE','CONCRETE_POWDER':'CONCRETE_POWDER','HARNESS':'HARNESS','BUNDLE':'DYED_BUNDLE','DYE':'DYE','BED':'BED','BANNER':'BANNER','CANDLE':'DYED_CANDLE'}
BLOCK_COLL = dict(ITEM_COLL); BLOCK_COLL.pop('HARNESS'); BLOCK_COLL.pop('BUNDLE'); BLOCK_COLL.pop('DYE'); BLOCK_COLL['WALL_BANNER']='WALL_BANNER'; BLOCK_COLL['CANDLE_CAKE']='DYED_CANDLE_CAKE'
def color_sub(m):
    owner, color, kind = m.group(1), m.group(2), m.group(3)
    table = ITEM_COLL if owner == 'Items' else BLOCK_COLL
    if kind not in table: return m.group(0)
    return f'{owner}.{table[kind]}.{CAMEL[color]}()'
COLOR_RE = r'\b(Items|Blocks)\.(' + '|'.join(COLORS) + r')_(' + '|'.join(sorted(set(ITEM_COLL)|set(BLOCK_COLL), key=len, reverse=True)) + r')\b'
RULES = [
    (r'\bMobEffects\.MOVEMENT_SLOWDOWN\b', 'MobEffects.SLOWNESS'),
    (r'\bMobEffects\.MOVEMENT_SPEED\b', 'MobEffects.SPEED'),
    (r'\bMobEffects\.DIG_SPEED\b', 'MobEffects.HASTE'),
    (r'\bMobEffects\.DIG_SLOWDOWN\b', 'MobEffects.MINING_FATIGUE'),
    (r'\bMobEffects\.DAMAGE_BOOST\b', 'MobEffects.STRENGTH'),
    (r'\bMobEffects\.HEAL\b', 'MobEffects.INSTANT_HEALTH'),
    (r'\bMobEffects\.HARM\b', 'MobEffects.INSTANT_DAMAGE'),
    (r'\bMobEffects\.JUMP\b', 'MobEffects.JUMP_BOOST'),
    (r'\bMobEffects\.CONFUSION\b', 'MobEffects.NAUSEA'),
    (r'\bMobEffects\.DAMAGE_RESISTANCE\b', 'MobEffects.RESISTANCE'),
    (r'\bSoundEvents\.PIG_HURT\b', 'legacyPigHurtSound()'),
    (r'\bMobEffectInstance\.load\(', 'legacyLoadEffect('),
    (r'\.direction\.normal\b', '.direction.unitVec3i'),
    (r'\bDirection\.getNearest\(', 'Direction.getApproximateNearest('),
    (r'\bRenderShape\.ENTITYBLOCK_ANIMATED\b', 'RenderShape.INVISIBLE'),
    (r'\bhasImpulse\b', 'needsSync'),
    (r'\bMaterial\(\s*(?:TextureAtlas\.LOCATION_BLOCKS|InventoryMenu\.BLOCK_ATLAS),\s*([^()]*(?:\([^()]*\))?[^()]*)\)\.sprite\(\)', r'com.atsuishio.portlib.render.LegacySprites.block(\1)'),
    (r'\bSheets\.cutoutBlockSheet\(\)', 'net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)'),
    (r'\bSheets\.translucentCullBlockSheet\(\)', 'net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)'),
    (r'\bSheets\.solidBlockSheet\(\)', 'net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)'),
]
for root in sys.argv[1:]:
    for dp, dn, fn in os.walk(root):
        for f in fn:
            if not f.endswith('.kt'): continue
            p = os.path.join(dp, f); s = open(p).read(); o = s
            for a, b in RULES: s = re.sub(a, b, s)
            s = re.sub(COLOR_RE, color_sub, s)
            if s != o: open(p, 'w').write(s)
