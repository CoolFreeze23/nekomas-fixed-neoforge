#!/usr/bin/env python3
"""Writes assets/nekomasfixed/lang/pt_br.json from en_us.json.

Names follow Minecraft's own Brazilian Portuguese wording, read from the vanilla pt_br.json passed as the first
argument (it comes with the game), so "Tapete", "Bote", "Vidraça" or "Bancada" match what players see in vanilla.
Prints every English key without a translation, and exits non-zero if there is one.
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, 'src/main/resources/assets/nekomasfixed/lang')
EN = json.load(open(os.path.join(LANG, 'en_us.json'), encoding='utf8'))
V = json.load(open(sys.argv[1] if len(sys.argv) > 1 else 'D:/Ports/vanilla_pt_br.json', encoding='utf8'))

VANILLA = 'white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black'.split()
EXTRA = {'amber': 'Âmbar', 'aqua': 'Água-Marinha', 'indigo': 'Índigo', 'maroon': 'Bordô'}
COLORS = VANILLA + list(EXTRA)

FEM = {c: V[f'block.minecraft.{c}_wool'].removeprefix('Lã ') for c in VANILLA} | EXTRA
MASC = {c: V[f'block.minecraft.{c}_carpet'].removeprefix('Tapete ') for c in VANILLA} | EXTRA


def plural(word):
    """Colour adjectives that agree in number; colours named after things stay as they are."""
    if word in ('Laranja', 'Magenta', 'Rosa', 'Verde-Limão', 'Ciano', 'Cinza', 'Âmbar', 'Água-Marinha', 'Índigo', 'Bordô'):
        return word
    if word == 'Azul':
        return 'Azuis'
    if word == 'Marrom':
        return 'Marrons'
    if word.endswith('-claro') or word.endswith('-clara'):
        return word + 's'
    return word + 's'


PL = {c: plural(MASC[c]) for c in COLORS}

WOODS = {
    'oak': ('Carvalho', 'Tronco'), 'spruce': ('Pinheiro', 'Tronco'), 'birch': ('Bétula', 'Tronco'), 'jungle': ('Selva', 'Tronco'),
    'acacia': ('Acácia', 'Tronco'), 'dark_oak': ('Carvalho Escuro', 'Tronco'), 'mangrove': ('Mangue', 'Tronco'),
    'cherry': ('Cerejeira', 'Tronco'), 'baobab': ('Baobá', 'Tronco'),
}
for wood in WOODS:
    if wood != 'baobab':
        WOODS[wood] = (V[f'block.minecraft.{wood}_log'].removeprefix('Tronco de '), 'Tronco')

T = {}

# Colour families. English keys decide which of these are used, so the table can hold more than the mod registers.
for c in COLORS:
    fem, masc, pl = FEM[c], MASC[c], PL[c]
    for ns in ('nekomasfixed', 'minecraft'):
        T[f'block.{ns}.{c}_wool'] = f'Lã {fem}'
        T[f'block.{ns}.{c}_carpet'] = f'Tapete {masc}'
        T[f'block.{ns}.{c}_stained_glass'] = f'Vidro {masc}'
        T[f'block.{ns}.{c}_stained_glass_pane'] = f'Vidraça {fem}'
        T[f'block.{ns}.{c}_candle'] = f'Vela {fem}'
        T[f'block.{ns}.{c}_candle_cake'] = f'Bolo com Vela {fem}'
        T[f'block.{ns}.{c}_bed'] = f'Cama {fem}'
        T[f'block.{ns}.{c}_shulker_box'] = f'Caixa de Shulker {fem}'
        T[f'block.{ns}.{c}_terracotta'] = f'Terracota {fem}'
        T[f'block.{ns}.{c}_glazed_terracotta'] = f'Terracota Vidrada {fem}'
        T[f'block.{ns}.{c}_concrete'] = f'Concreto {masc}'
        T[f'block.{ns}.{c}_concrete_powder'] = f'Cimento {masc}'
    T[f'item.nekomasfixed.{c}_dye'] = f'Corante {masc}'
    T[f'block.nekomasfixed.{c}_bricks'] = f'Tijolos {pl}'
    T[f'block.nekomasfixed.{c}_brick_slab'] = f'Laje de Tijolos {pl}'
    T[f'block.nekomasfixed.{c}_brick_stairs'] = f'Escadas de Tijolos {pl}'
    T[f'block.nekomasfixed.{c}_brick_wall'] = f'Muro de Tijolos {pl}'
    T[f'block.nekomasfixed.{c}_spotted_wool'] = f'Lã Malhada {fem}'
    T[f'block.nekomasfixed.{c}_spotted_carpet'] = f'Tapete Malhado {masc}'
    T[f'block.nekomasfixed.{c}_spotted_wool_slab'] = f'Laje de Lã Malhada {fem}'
    T[f'block.nekomasfixed.{c}_spotted_wool_stairs'] = f'Escadas de Lã Malhada {fem}'
    T[f'block.nekomasfixed.{c}_wool_slab'] = f'Laje de Lã {fem}'
    T[f'block.nekomasfixed.{c}_wool_stairs'] = f'Escadas de Lã {fem}'
    T[f'block.nekomasfixed.{c}_concrete_slab'] = f'Laje de Concreto {masc}'
    T[f'block.nekomasfixed.{c}_concrete_stairs'] = f'Escadas de Concreto {masc}'
    T[f'item.nekomasfixed.{c}_dyed_brush'] = f'Pincel {masc}'
    T[f'item.nekomasfixed.{c}_prism'] = f'Prisma {masc}'
    T[f'glint.nekomasfixed.{c}'] = masc
    # colours given to firework stars as plain RGB
    T[f'item.minecraft.firework_star.{c}'] = masc

# Boats. Vanilla's word is "Bote"; bamboo floats on a "Jangada".
for wood, (name, _) in WOODS.items():
    for size, word in (('big', 'Grande'), ('huge', 'Enorme')):
        for ns in ('item', 'entity'):
            T[f'{ns}.nekomasfixed.{size}_{wood}_boat'] = f'Bote {word} de {name}'
for size, word in (('big', 'Grande'), ('huge', 'Enorme')):
    for ns in ('item', 'entity'):
        T[f'{ns}.nekomasfixed.{size}_bamboo_boat'] = f'Jangada {word} de Bambu'

# Hollow logs, under both id spellings upstream has used.
for wood, (name, _) in WOODS.items():
    T[f'block.nekomasfixed.hollow_{wood}_log'] = f'Tronco Oco de {name}'
    T[f'block.nekomasfixed.hollow_stripped_{wood}_log'] = f'Tronco Oco de {name} Descascado'
    T[f'block.nekomasfixed.stripped_hollow_{wood}_log'] = f'Tronco Oco de {name} Descascado'
    T[f'block.nekomasfixed.stripped_{wood}_hollow_log'] = f'Tronco Oco de {name} Descascado'
for prefix_bamboo in ('hollow_stripped_bamboo_block', 'stripped_hollow_bamboo_block', 'stripped_bamboo_hollow_block'):
    T[f'block.nekomasfixed.{prefix_bamboo}'] = 'Bloco de Bambu Oco Descascado'
T['block.nekomasfixed.hollow_bamboo_block'] = 'Bloco de Bambu Oco'
for stem, word in (('crimson', 'Carmesim'), ('warped', 'Distorcido')):
    T[f'block.nekomasfixed.hollow_{stem}_stem'] = f'Caule {word} Oco'
    for key in (f'hollow_stripped_{stem}_stem', f'stripped_hollow_{stem}_stem', f'stripped_{stem}_hollow_stem'):
        T[f'block.nekomasfixed.{key}'] = f'Caule {word} Oco Descascado'

# Flower crowns take vanilla's flower names.
FLOWERS = {
    'torchflower': 'torchflower', 'blue_orchid': 'blue_orchid', 'wither_rose': 'wither_rose', 'cornflower': 'cornflower',
    'lily_of_the_valley': 'lily_of_the_valley', 'orange_tulip': 'orange_tulip', 'pink_tulip': 'pink_tulip', 'allium': 'allium',
    'red_tulip': 'red_tulip', 'poppy': 'poppy', 'azure_bluet': 'azure_bluet', 'white_tulip': 'white_tulip',
    'oxeye_daisy': 'oxeye_daisy', 'dandelion': 'dandelion',
}
for key, flower in FLOWERS.items():
    T[f'item.nekomasfixed.{key}_flower_crown'] = 'Coroa de ' + V[f'block.minecraft.{flower}']

# Froglights: same word as vanilla's "Anfibrilho".
for key, word in {
    'clear': 'Cristalino', 'cloudy': 'Nublado', 'cascading': 'Cascata', 'cloudburst': 'Tempestuoso', 'chamoisee': 'Camurça',
    'sanguine': 'Sanguíneo', 'vermilion': 'Vermelhão', 'mandarin': 'Tangerina', 'lemon': 'Limão', 'kiwi': 'Kiwi',
    'seafoam': 'Espuma-do-Mar', 'teal': 'Azul-Petróleo', 'cerulean': 'Cerúleo', 'navy': 'Azul-Marinho', 'lavender': 'Lavanda',
    'thulian': 'Túlio', 'sakura': 'Sakura',
}.items():
    T[f'block.nekomasfixed.{key}_froglight'] = f'Anfibrilho {word}'

egg = V['item.minecraft.husk_spawn_egg'].removesuffix(V['entity.minecraft.husk']).rstrip()  # "Ovo Gerador de"
for key, name in {
    'moobloom': 'Moobloom', 'derelict': 'Abandonado', 'drenched': 'Encharcado', 'rime': 'Geado', 'suspicious_spider': 'Aranha Suspeita',
    'wildfire': 'Fogaréu', 'termite': 'Cupim',
}.items():
    T[f'item.nekomasfixed.{key}_spawn_egg'] = f'{egg} {name}'
    T[f'entity.nekomasfixed.{key}'] = name

for material, word in {'wooden': 'Madeira', 'stone': 'Pedra', 'copper': 'Cobre', 'iron': 'Ferro', 'golden': 'Ouro', 'diamond': 'Diamante', 'netherite': 'Netherita'}.items():
    T[f'item.nekomasfixed.{material}_sickle'] = f'Foice de {word}'
    T[f'item.nekomasfixed.{material}_crown'] = f'Coroa de {word}'
    T[f'item.nekomasfixed.goat_horn_{material}_helmet'] = f'Capacete de {word} com Chifres de Cabra'

T.update({
    'itemgroup.nekomasfixed': "Nekoma's Fixed: Ideias",
    'itemgroup.nekomasfixedcolours': "Nekoma's Fixed: Cores",

    'block.nekomasfixed.glow_torch': 'Tocha Brilhante',
    'block.nekomasfixed.glow_wall_torch': 'Tocha Brilhante de Parede',
    'block.nekomasfixed.glow_ink_torch': 'Tocha Brilhante',
    'block.nekomasfixed.rope': 'Corda',

    'block.nekomasfixed.baobab_log': 'Tronco de Baobá',
    'block.nekomasfixed.baobab_wood': 'Madeira de Baobá',
    'block.nekomasfixed.stripped_baobab_log': 'Tronco de Baobá Descascado',
    'block.nekomasfixed.stripped_baobab_wood': 'Madeira de Baobá Descascada',
    'block.nekomasfixed.baobab_planks': 'Tábuas de Baobá',
    'block.nekomasfixed.baobab_stairs': 'Escadas de Baobá',
    'block.nekomasfixed.baobab_slab': 'Laje de Baobá',
    'block.nekomasfixed.baobab_fence': 'Cerca de Baobá',
    'block.nekomasfixed.baobab_fence_gate': 'Portão de Baobá',
    'block.nekomasfixed.baobab_door': 'Porta de Baobá',
    'block.nekomasfixed.baobab_trapdoor': 'Alçapão de Baobá',
    'block.nekomasfixed.baobab_pressure_plate': 'Placa de Pressão de Baobá',
    'block.nekomasfixed.baobab_button': 'Botão de Baobá',
    'block.nekomasfixed.baobab_sign': 'Placa de Baobá',
    'block.nekomasfixed.baobab_wall_sign': 'Placa de Baobá de Parede',
    'block.nekomasfixed.baobab_hanging_sign': 'Placa Suspensa de Baobá',
    'block.nekomasfixed.baobab_wall_hanging_sign': 'Placa Suspensa de Baobá de Parede',
    'block.nekomasfixed.baobab_leaves': 'Folhas de Baobá',
    'block.nekomasfixed.baobab_fruit': 'Fruto de Baobá',
    'item.nekomasfixed.baobab_fruit': 'Fruto de Baobá',
    'block.nekomasfixed.baobab_sapling': 'Muda de Baobá',
    'item.nekomasfixed.baobab_seeds': 'Sementes de Baobá',
    'item.nekomasfixed.baobab_boat': 'Bote de Baobá',
    'entity.nekomasfixed.baobab_boat': 'Bote de Baobá',
    'item.nekomasfixed.baobab_chest_boat': 'Bote de Baobá com Baú',
    'entity.nekomasfixed.baobab_chest_boat': 'Bote de Baobá com Baú',
    'block.nekomasfixed.baobab_shelf': 'Prateleira de Baobá',

    'block.nekomasfixed.clam': 'Marisco',
    'block.nekomasfixed.clam_blue': 'Marisco Azul',
    'block.nekomasfixed.clam_pink': 'Marisco Rosa',
    'block.nekomasfixed.clam_purple': 'Marisco Roxo',
    'block.nekomasfixed.pearl_block': 'Bloco de Pérola',
    'item.nekomasfixed.pearl': 'Pérola',
    'block.nekomasfixed.geyser': 'Gêiser',
    'block.nekomasfixed.glistering_melon': 'Melancia Reluzente',
    'block.nekomasfixed.enderman_head': 'Cabeça de Enderman',
    'item.nekomasfixed.redstone_striker': 'Acendedor de Redstone',
    'block.nekomasfixed.redstone_striker': 'Acendedor de Redstone',
    'item.nekomasfixed.target_dummy': 'Boneco de Treino',
    'entity.nekomasfixed.target_dummy': 'Boneco de Treino',
    'item.nekomasfixed.anchor': 'Âncora',
    'block.nekomasfixed.horn': 'Chifre de Cabra',

    'block.nekomasfixed.sweetberry_cake': 'Bolo de Bagas Doces',
    'block.nekomasfixed.pan_cake': 'Panqueca',
    'block.nekomasfixed.glowberry_cake': 'Bolo de Bagas Brilhantes',
    'block.nekomasfixed.apple_cake': 'Bolo de Maçã',
    'block.nekomasfixed.vanilla_cake': 'Bolo de Baunilha',
    'block.nekomasfixed.cookie_cake': 'Bolo de Biscoito',
    'block.nekomasfixed.chocolate_cake': 'Bolo de Chocolate',
    'block.nekomasfixed.beetroot_cake': 'Bolo de Beterraba',

    'advancements.husbandry.ancient_dyes.title': 'Mestre das Cores Antigas!',
    'advancements.husbandry.ancient_dyes.description': 'Tenha todos os Corantes Antigos no inventário',
    'advancements.husbandry.all_froglights.title': 'Sobrecarga de Luz!',
    'advancements.husbandry.all_froglights.description': 'Tenha os 20 anfibrilhos no inventário',

    'item.nekomasfixed.turtle_chestplate': 'Carapaça de Tartaruga',
    'item.nekomasfixed.turtle_leggings': 'Joelheiras de Tartaruga',
    'item.nekomasfixed.turtle_boots': 'Nadadeiras de Tartaruga',
    'item.nekomasfixed.slingshot': 'Estilingue',

    'component.nekomasfixed.combo': 'Dano em sequência: %s',
    'component.nekomasfixed.dual_wield': 'Dá para usar outra na mão secundária',
    'component.nekomasfixed.duel_wield': 'Dá para usar outra na mão secundária',
    'component.nekomasfixed.storedtime': 'Hora registrada: %s',
    'enchantment.nekomasfixed.leeching': 'Sanguessuga',
    'enchantment.nekomasfixed.shatter': 'Estilhaçamento',
    'enchantment.nekomasfixed.dismount': 'Derrubada',
    'effect.nekomasfixed.lightning': 'Relâmpago',
    'effect.nekomasfixed.spicy': 'Ardência',
    'item.minecraft.potion.effect.lightning': 'Poção de Relâmpago',
    'item.minecraft.splash_potion.effect.lightning': 'Poção Arremessável de Relâmpago',
    'item.minecraft.lingering_potion.effect.lightning': 'Poção Prolongada de Relâmpago',
    'item.minecraft.tipped_arrow.effect.lightning': 'Flecha de Relâmpago',

    'block.nekomasfixed.ghost_pepper_shrub': 'Pé de Pimenta-Fantasma',
    'item.nekomasfixed.ghost_pepper': 'Pimenta-Fantasma',

    'container.nautilus': 'Contém: %s%s%s%s',
    'block.nekomasfixed.nautilus_block': 'Bloco de Náutilo',
    'block.nekomasfixed.zombie_nautilus_block': 'Bloco de Náutilo Zumbificado',
    'block.nekomasfixed.coral_nautilus_block': 'Bloco de Náutilo de Coral',

    'block.nekomasfixed.honey_cauldron': 'Caldeirão de Mel',
    'block.nekomasfixed.magma_cauldron': 'Caldeirão de Magma',
    'block.nekomasfixed.slime_cauldron': 'Caldeirão de Slime',
    'block.nekomasfixed.ice_cauldron': 'Caldeirão de Gelo',

    'block.nekomasfixed.pyrotechnics_table': 'Bancada de Pirotecnia',
    'item.nekomasfixed.pyrotechnics_table': 'Bancada de Pirotecnia',
    'entity.minecraft.villager.nekomasfixed.pyrotechnist': 'Pirotécnico',
    'container.nekomasfixed.pyrotechnics': 'Pirotecnia',
    'container.nekomasfixed.pyrotechnics.none': 'Sem efeito',
    'container.nekomasfixed.pyrotechnics.large_ball': 'Esfera grande',
    'container.nekomasfixed.pyrotechnics.star': 'Estrela',
    'container.nekomasfixed.pyrotechnics.creeper': 'Creeper',
    'container.nekomasfixed.pyrotechnics.burst': 'Estouro',
    'container.nekomasfixed.pyrotechnics.twinkle': 'Cintilação',
    'container.nekomasfixed.pyrotechnics.trail': 'Rastro',

    'subtitles.nekomasfixed.item.phantom_membrane.use': 'Membrana de Espectro estica',
    'subtitles.nekomasfixed.entity.clear_item_frame.add_item': 'Moldura de Item Transparente preenchida',
    'subtitles.nekomasfixed.entity.clear_item_frame.break': 'Moldura de Item Transparente retirada',
    'subtitles.nekomasfixed.entity.clear_item_frame.place': 'Moldura de Item Transparente colocada',
    'subtitles.nekomasfixed.entity.clear_item_frame.remove_item': 'Moldura de Item Transparente esvaziada',
    'subtitles.nekomasfixed.entity.clear_item_frame.rotate_item': 'Moldura de Item Transparente gira',
    'item.nekomasfixed.clear_item_frame': 'Moldura de Item Transparente',

    'item.nekomasfixed.smithing_template.glowing': '+Brilho',
    'item.nekomasfixed.smithing_template.hidden': '+Invisível',
    'item.nekomasfixed.smithing_template.echoing_effect': '+Efeito de Eco:',
    'echoing_effect.nekomasfixed.glowing': 'Cintilar',
    'echoing_effect.nekomasfixed.hidden': 'Sumir',
    'echoing_effect.nekomasfixed.material': 'Pigmento (%s)',
    'block.minecraft.banner.nekomasfixed.hide_background': '+Fundo Transparente',
    'block.minecraft.banner.nekomasfixed.glowing': '+Padrões Brilhantes',

    'advancements.nekomasfixed.husbandry.make_sheep_glow_at_night.title': 'Contar carneirinhos ficou mais fácil!',
    'advancements.nekomasfixed.husbandry.make_sheep_glow_at_night.description': 'Use uma Bolsa de Tinta Brilhante em uma ovelha à noite',
    'advancements.nekomasfixed.adventure.make_decorated_pot_glow.title': 'Vaso de Halloween',
    'advancements.nekomasfixed.adventure.make_decorated_pot_glow.description': 'Faça um Vaso Decorado brilhar por inteiro',
    'advancements.nekomasfixed.adventure.make_trim_glow.title': 'Pigmento fluorescente',
    'advancements.nekomasfixed.adventure.make_trim_glow.description': 'Combine 2 Bolsas de Tinta Brilhante com uma armadura enfeitada em uma Bancada de Ferraria',
    'advancements.nekomasfixed.adventure.make_echoing_trim.title': 'Marcas vivas',
    'advancements.nekomasfixed.adventure.make_echoing_trim.description': 'Combine um Fragmento de Eco com uma armadura enfeitada e uma Membrana de Espectro, uma Bolsa de Tinta Brilhante ou qualquer material de enfeite em uma Bancada de Ferraria',
    'advancements.nekomasfixed.adventure.make_wall_decoration_dissappear.title': 'Grafite',
    'advancements.nekomasfixed.adventure.make_wall_decoration_dissappear.description': 'Faça uma Placa ou um Estandarte sumir de uma parede',

    'credits.button.credits': 'Créditos do Mod',
    'title.nekomasfixed.config': 'Configurações',

    'tipped_arrow_custom.contains': 'Contém',
    'tipped_arrow_custom.potion_content': '%s',
    'item.nekomasfixed.tipped_arrow_custom': 'Flecha com Efeito',
    'item.nekomasfixed.quiver': 'Aljava',

    'block.nekomasfixed.kiln': 'Forno de Cerâmica',
    'container.nekomasfixed.kiln': 'Forno de Cerâmica',
    'gui.recipebook.toggleRecipes.kilnable': 'Queimáveis',
    'container.nekomasfixed.fletching': 'Arco e Flecha',

    'block.nekomasfixed.sulfur_torch': 'Tocha de Enxofre',
    'block.nekomasfixed.sulfur_wall_torch': 'Tocha de Enxofre de Parede',
    'block.nekomasfixed.sulfur_campfire': 'Fogueira de Enxofre',
    'block.nekomasfixed.sulfur_lantern': 'Lampião de Enxofre',
    'block.nekomasfixed.sulfur_fire': 'Fogo de Enxofre',
    'block.nekomasfixed.gold_chain': 'Corrente de Ouro',

    'terracotta_decorated_pot.made_of': 'Feito de: ',
    'terracotta_decorated_pot.front': 'Frente: ',
    'terracotta_decorated_pot.back': 'Trás: ',
    'terracotta_decorated_pot.left': 'Esquerda: ',
    'terracotta_decorated_pot.right': 'Direita: ',
    'block.nekomasfixed.terracotta_decorated_pot': 'Vaso Decorado',

    'block.nekomasfixed.corrupted_beacon': 'Sinalizador Corrompido',
    'item.nekomasfixed.corrupted_beacon': 'Sinalizador Corrompido',

    'item.nekomasfixed.goat_horn_turtle_helmet': 'Casco de Tartaruga com Chifres de Cabra',
    'item.nekomasfixed.nether_heart': 'Coração do Nether',
    'item.nekomasfixed.smithing_template.ingredients_shield_trident': 'Escudos e Tridentes',
    'item.nekomasfixed.wildfire_trident': 'Tridente do Fogaréu',
    'entity.nekomasfixed.wildfire_trident': 'Tridente do Fogaréu',
    'item.nekomasfixed.wildfire_shield': 'Escudo do Fogaréu',
    'item.nekomasfixed.jewel_armor_trim_smithing_template.new': V['item.minecraft.smithing_template'],
    'item.nekomasfixed.jewel_armor_trim_smithing_template': 'Enfeite de Joias',
    'trim_pattern.nekomasfixed:jewel': 'Enfeite de Joias',
    'item.nekomasfixed.crown_smithing_template': 'Molde de Ferraria de Coroa',
    'item.nekomasfixed.helmets': 'Capacetes',

    'block.nekomasfixed.termite_hive': 'Cupinzeiro',
    'block.nekomasfixed.termite_block': 'Bloco de Cupim',
    'item.nekomasfixed.boat_upgrade_template': 'Molde de Melhoria de Bote',
    'item.nekomasfixed.planks': 'Tábuas de Madeira',
    'item.nekomasfixed.boat': 'Botes',
    'item.nekomasfixed.special_stew': 'Ensopado Especial',

    'entity.nekomasfixed.fake_boat': 'Bote Falso',
    'entity.nekomasfixed.fire_bomb': 'Bomba de Fogo',
    'entity.nekomasfixed.slingshot_projectile': 'Projétil de Estilingue',
    'entity.nekomasfixed.slowness_snowball': 'Bola de Neve de Lentidão',

    'tooltip_component.nekomasfixed.glint': 'Brilho: ',
    'glint.nekomasfixed.water': 'Água',
    'glint.nekomasfixed.fire': 'Fogo',
    'glint.nekomasfixed.ender': 'Ender',
    'glint.nekomasfixed.rainbow': 'Arco-íris',
    'glint.nekomasfixed.frostfire': 'Fogo Gélido',
    'glint.nekomasfixed.jungle': 'Selva',
    'glint.nekomasfixed.void': 'Vazio',
    'glint.nekomasfixed.sky': 'Céu',
    'glint.nekomasfixed.ancient': 'Antigo',
    'glint.nekomasfixed.sculk': 'Sculk',
    'item.nekomasfixed.water_prism': 'Prisma de Água',
    'item.nekomasfixed.fire_prism': 'Prisma de Fogo',
    'item.nekomasfixed.ender_prism': 'Prisma do Ender',
    'item.nekomasfixed.rainbow_prism': 'Prisma Arco-íris',
    'item.nekomasfixed.frostfire_prism': 'Prisma de Fogo Gélido',
    'item.nekomasfixed.jungle_prism': 'Prisma da Selva',
    'item.nekomasfixed.void_prism': 'Prisma do Vazio',
    'item.nekomasfixed.sky_prism': 'Prisma do Céu',
    'item.nekomasfixed.ancient_prism': 'Prisma Antigo',
    'item.nekomasfixed.sculk_prism': 'Prisma de Sculk',

    'failed.entity_spawn': 'Esta criatura está desativada neste mundo!',

    'config.main_title.worldgen': 'Opções de Geração de Mundo',
    'config.main_title.nether': 'Opções do Nether',
    'config.main_title.entity': 'Opções de Criaturas',
    'config.main_title.item': 'Opções de Itens',
    'config.main_title.blocks': 'Opções de Blocos',
    'config.main_title.credits': 'Créditos',
    'config.in_game': 'Opções do Mod',
    'config.go_to.worldgen': 'Geração de Mundo...',
    'config.go_to.nether': 'Nether...',
    'config.go_to.entity': 'Criaturas...',
    'config.go_to.item': 'Itens...',
    'config.go_to.blocks': 'Blocos...',
    'config.separator.terrain': '=== Terreno ===',
    'config.separator.nether': '=== Melhorias no Nether ===',
    'config.separator.villagers': '=== Aldeões ===',
    'config.separator.spawn': '=== Geração de Criaturas ===',
    'config.separator.recipe': '=== Receitas ===',
    'config.separator.developers': '=== Desenvolvedores ===',
    'config.separator.asset_providers': '=== Arte ===',
    'config.separator.interaction': '=== Interação com Blocos ===',
    'config.separator.testers': '=== Testadores ===',
    'config.separator.translators': '=== Tradutores ===',
    'config.separator.cannot_be_changed': ' NÃO dá para mudar depois de criar o mundo!',
    'config.sub_category.cake': 'Bolos',
    'config.slider.cake_layers': 'Camadas de bolo',
    'config.slider.desc.cake_layers': 'Define quantas camadas de bolo podem ser empilhadas.',
    'config.nekoma': "Nekoma's",
    'config.fixed': 'Fixed',
    'nekoma': "Nekoma's",
    'fixed': 'Fixed',
    'config.worldgen.button.gen_geyser': 'Gerar gêiseres',
    'config.worldgen.desc.gen_geyser': 'Gera gêiseres no Nether',
    'config.worldgen.button.gen_mound': 'Gerar cupinzeiros',
    'config.worldgen.desc.gen_mound': 'Gera cupinzeiros nas savanas',
    'config.other.button.enable_dyed_brushes': 'Manter pincéis coloridos',
    'config.other.desc.enable_dyed_brushes': 'Mantém a receita dos pincéis coloridos',
    'config.entity.button.spawn_sus_spider': 'Gerar Aranhas Suspeitas',
    'config.entity.desc.spawn_sus_spider': 'Gera aranhas com efeitos de Ensopado Suspeito',
    'config.entity.button.spawn_mooblooms': 'Gerar Mooblooms',
    'config.entity.desc.spawn_mooblooms': 'Gera mooblooms',
    'config.entity.button.spawn_termite': 'Gerar Cupins',
    'config.entity.desc.spawn_termite': 'Gera cupins nos cupinzeiros',
    'config.entity.button.spawn_drenched': 'Gerar Encharcados',
    'config.entity.desc.spawn_drenched': 'Gera encharcados nos oceanos',
    'config.entity.button.spawn_rime': 'Gerar Geados',
    'config.entity.desc.spawn_rime': 'Gera geados nos biomas frios',
    'config.entity.button.spawn_derelict': 'Gerar Abandonados',
    'config.entity.desc.spawn_derelict': 'Gera abandonados nas selvas',
    'config.entity.button.spawn_wildfire': 'Gerar Fogaréu',
    'config.entity.desc.spawn_wildfire': 'Gera o Fogaréu na sala do chefe das Fortalezas do Nether',
    'config.entity.button.enable_messy_beds': 'Camas bagunçadas',
    'config.entity.desc.enable_messy_beds': 'Permite que as camas fiquem bagunçadas',
    'config.nether.button.do_food_rotting': 'Comida apodrece',
    'config.nether.desc.do_food_rotting': 'Toda comida, menos as douradas, apodrece aos poucos no Nether',
    'config.nether.button.magma_block_changes': 'Mudanças no Bloco de Magma',
    'config.nether.desc.magma_block_changes': 'Bloco de Magma só sai inteiro com Toque Suave',
    'config.blocks.button.enable_stacked_cakes': 'Bolos empilhados',
    'config.blocks.desc.enable_stacked_cakes': 'Permite empilhar vários bolos',
    'config.blocks.button.enderman_head_activates': 'Cabeça de Enderman ativa',
    'config.blocks.desc.enderman_head_activates': 'Olhar para uma Cabeça de Enderman a energiza',

    'advancements.nekomasfixed.root.description': 'Acompanhe seu progresso no mod!',
    'advancements.nekomasfixed.turtle_master.title': 'Mestre das Tartarugas!',
    'advancements.nekomasfixed.turtle_master.description': 'Junte uma armadura completa de tartaruga',
    'advancements.nekomasfixed.rainbow_spotted_wool.title': 'Arco-íris Malhado!',
    'advancements.nekomasfixed.rainbow_spotted_wool.description': 'Junte as 20 cores de lã malhada',
    'advancements.nekomasfixed.ship_of_thesus.title': 'Navio de Teseu!',
    'advancements.nekomasfixed.ship_of_thesus.description': 'Fabrique um bote enorme',
    'advancements.nekomasfixed.i_am_the_ruler.title': 'Aqui Quem Manda Sou Eu!',
    'advancements.nekomasfixed.i_am_the_ruler.description': 'Enfeite uma coroa com o Enfeite de Joias',
    'advancements.nekomasfixed.i_am_on_fire_now.title': 'Agora Quem Pegou Fogo Fui Eu!',
    'advancements.nekomasfixed.i_am_on_fire_now.description': 'Enfeite uma armadura completa de netherita com o Enfeite de Joias',
    'advancements.nekomasfixed.slice_&_dice.title': 'Picadinho!',
    'advancements.nekomasfixed.slice_&_dice.description': 'Consiga uma foice',
    'advancements.nekomasfixed.spooky.title': 'Assombrado!',
    'advancements.nekomasfixed.spooky.description': 'Consiga uma Coroa de Rosa-do-Wither',
    'advancements.nekomasfixed.a_bow_but_worse.title': 'Um arco, só que pior',
    'advancements.nekomasfixed.a_bow_but_worse.description': 'Consiga um estilingue',
    'advancements.nekomasfixed.hollow_knight.title': 'Cavaleiro Oco',
    'advancements.nekomasfixed.hollow_knight.description': 'Junte todos os troncos ocos',
    'advancements.nekomasfixed.flower_power.title': 'Poder das Flores',
    'advancements.nekomasfixed.flower_power.description': 'Junte todas as coroas de flores',
    'advancements.nekomasfixed.prismaniac.title': 'Prismaníaco',
    'advancements.nekomasfixed.prismaniac.description': 'Junte todas as cores de prisma',
})

out = {}
missing = []
for key, english in EN.items():
    if key in T:
        out[key] = T[key]
    else:
        missing.append(f'{key} = {english}')

# %s placeholders must survive translation
for key, text in out.items():
    if re.findall(r'%\d*\$?[sd]', EN[key]) != re.findall(r'%\d*\$?[sd]', text):
        missing.append(f'PLACEHOLDER MISMATCH {key}: {EN[key]!r} -> {text!r}')

with open(os.path.join(LANG, 'pt_br.json'), 'w', encoding='utf8', newline='\n') as handle:
    json.dump(out, handle, indent=2, ensure_ascii=False)
    handle.write('\n')

# Vanilla's English lang entries from this mod get their Brazilian counterpart next to them.
mc_en = os.path.join(ROOT, 'src/main/resources/assets/minecraft/lang/en_us.json')
if os.path.exists(mc_en):
    mc = {}
    for key in json.load(open(mc_en, encoding='utf8')):
        if key in ('block.minecraft.clock', 'block.minecraft.wall_clock'):
            mc[key] = V['item.minecraft.clock']
        elif key in T:
            mc[key] = T[key]
        else:
            missing.append(f'minecraft namespace: {key}')
    with open(os.path.join(ROOT, 'src/main/resources/assets/minecraft/lang/pt_br.json'), 'w', encoding='utf8', newline='\n') as handle:
        json.dump(mc, handle, indent=2, ensure_ascii=False)
        handle.write('\n')

print(f'{len(out)} of {len(EN)} keys translated')
for line in missing:
    print('MISSING', line)
sys.exit(1 if missing else 0)
