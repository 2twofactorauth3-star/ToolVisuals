from pathlib import Path
import json
import re
from project import ROOT,OUT

REPLACEMENTS=[
    ('GuiGraphicsExtractor','GuiGraphics'),
    ('net.minecraft.client.gui.Hud','net.minecraft.client.gui.Gui'),
    ('AvatarModel','PlayerModel'),
    ('InputConstants.Type.KEYBOARD','InputConstants.Type.KEYSYM'),
    ('net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper','net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper'),
    ('KeyMappingHelper.registerKeyMapping','KeyBindingHelper.registerKeyBinding'),
    ('extractRenderState','render'),
    ('extractWidgetRenderState','renderWidget'),
    ('extractBackground','renderBackground'),
    ('extractPanorama','renderPanorama'),
    ('extractBlurredBackground','renderBlurredBackground'),
    ('extractContents','renderContents'),
    ('extractImage','renderImage'),
    ('.gui.screen()', '.screen'),
    ('.gui.overlay()', '.getOverlay()'),
    ('.gui.setScreen(', '.setScreen('),
    ('.gui.hud', '.gui'),
    ('ContainerInput','ClickType'),
    ('ClientTooltipComponentCallback','TooltipComponentCallback'),
    ('ClientCommands','ClientCommandManager'),
    ('com.mojang.authlib.services.ProfileResult','com.mojang.authlib.yggdrasil.ProfileResult'),
    ('com.mojang.renderpearl.api.pipeline','com.mojang.blaze3d.pipeline'),
    ('PayloadTypeRegistry.serverboundPlay()', 'PayloadTypeRegistry.playC2S()'),
    ('PayloadTypeRegistry.clientboundPlay()', 'PayloadTypeRegistry.playS2C()'),
    ('.mainCamera()', '.getMainCamera()'),
    ('.outline(', '.renderOutline('),
    ('handleContainerInput','handleInventoryMouseClick'),
    ('PlayerFaceExtractor','PlayerFaceRenderer'),
    ('net.minecraft.client.renderer.state.level.*','net.minecraft.client.renderer.state.*'),
    ('ExperienceBar','ExperienceBarRenderer'),
    ('LocatorBar','LocatorBarRenderer'),
    ('ContextualBar','ContextualBarRenderer'),
    ('.gameRenderState().levelRenderState', '.getLevelRenderState()'),
    ('.linesTranslucentNoDepthWrite()', '.linesTranslucent()'),
    ('.entityTranslucentCull(', '.entityTranslucent('),
    ('RenderPipelines.GUI_TEXT_GRAYSCALE','RenderPipelines.GUI_TEXT'),
    ('addGuiElement','submitGuiElement'),
    ('extractExperienceLevel','renderExperienceLevel'),
    ('net.minecraft.client.renderer.state.gui.pip','net.minecraft.client.gui.render.state.pip'),
    ('BlockAndLightGetter','BlockAndTintGetter'),
]

def key_matches(text):
    matches=list(re.finditer(r'((?:\w+\.)*\w+(?:\[[^\]]+\])?)\.matches\(',text))
    for match in reversed(matches):
        start=match.end();end=start;depth=1
        while end<len(text) and depth:
            depth+=(text[end]=='(')-(text[end]==')');end+=1
        arguments=text[start:end-1]
        if arguments.startswith('InputConstants.') or arguments.endswith('.getDefaultKey()'):
            text=text[:match.start()]+'ru.he3hauka.toolvisuals.client.compatibility.input.VersionInput.matches('+match[1]+','+arguments+')'+text[end:]
    return text


def legacy_pose(text):
    receivers=[r'\b\w+\.pose\(\)']
    receivers.extend(r'\b'+re.escape(name) for name in re.findall(r'var\s+(\w+)\s*=\s*\w+\.pose\(\)',text))
    expression='(?:'+'|'.join(receivers)+')'
    calls=list(re.finditer('('+expression+r')\.(translate|scale|rotate|transformPosition)\(',text))
    for match in reversed(calls):
        index=match.end();end=index;depth=1;arguments=[];start=index
        while end<len(text) and depth:
            char=text[end]
            if char=='(':depth+=1
            elif char==')':depth-=1
            elif char==',' and depth==1:arguments.append(text[start:end]);start=end+1
            end+=1
        arguments.append(text[start:end-1]);receiver,method=match.groups()
        if method in ['translate','scale'] and len(arguments)==2:
            replacement=receiver+'.'+method+'('+','.join(arguments)+(','+'0' if method=='translate' else ',1')+')'
        elif method=='rotate' and len(arguments)==1:
            replacement=receiver+'.mulPose(com.mojang.math.Axis.ZP.rotation('+arguments[0]+'))'
        elif method=='transformPosition' and len(arguments)==3:
            replacement='ru.he3hauka.toolvisuals.client.compatibility.gui.VersionGui.transform('+receiver+','+','.join(arguments)+')'
        else:continue
        text=text[:match.start()]+replacement+text[end:]
    text=re.sub('('+expression+r')\.(m[01][01])\(\)',r'\1.last().pose().\2()',text)
    return text

def rename_symbol(text,before,after):
    return re.sub(r'"(?:\\.|[^"\\])*"|\b'+re.escape(before)+r'\b',lambda match:match[0] if match[0].startswith('"') else after,text)

def translate(text,version):
    text=text.replace('@MINECRAFT@',version)
    relocations=json.loads((ROOT/'versions'/version/'relocations.json').read_text(encoding='utf-8'))
    for before,after in relocations.items():text=text.replace(before.replace('/','.'),after.replace('/','.')).replace(before,after)
    text=text.replace('net.minecraft.client.model.player.AvatarModel','net.minecraft.client.model.player.PlayerModel' if version=='1.21.11' else 'net.minecraft.client.model.PlayerModel')
    if version=='1.21.4':
        text=text.replace('net.minecraft.client.input.', 'ru.he3hauka.toolvisuals.client.compatibility.input.')
        text=text.replace('net.minecraft.world.entity.Player','net.minecraft.world.entity.player.Player')
        text=text.replace('net.minecraft.core.ResourceLocation', 'net.minecraft.resources.ResourceLocation')
        text=re.sub(r'(?<![\w.])([\w]+)\.getGuiScale\(\)',r'(int)\1.getGuiScale()',text)
        text=re.sub(r'(\w+\.getWindow\(\))\.getGuiScale\(\)',r'(int)\1.getGuiScale()',text)
        text=text.replace('net.minecraft.client.gui.components.*;', 'net.minecraft.client.gui.components.*;\nimport ru.he3hauka.toolvisuals.client.compatibility.gui.LegacyEditBox;')
        text=re.sub(r'\b(\w+)\.nextStratum\(\);',r'\1.flush();',text)


        text=text.replace('net.minecraft.client.renderer.state.level.LevelRenderState','ru.he3hauka.toolvisuals.client.compatibility.render.SceneState')
        text=text.replace('net.minecraft.client.renderer.state.level.CameraRenderState','ru.he3hauka.toolvisuals.client.compatibility.render.CameraState')
        text=text.replace('net.minecraft.client.renderer.state.level.*','ru.he3hauka.toolvisuals.client.compatibility.render.*')
        text=text.replace('net.minecraft.client.renderer.SubmitNodeCollector','ru.he3hauka.toolvisuals.client.compatibility.render.GeometryCollector')
        text=re.sub(r'\bLevelRenderState\b','SceneState',text).replace('CameraRenderState','CameraState')
        text=re.sub(r'\bSubmitNodeCollector\b','GeometryCollector',text)
        text=text.replace('net.minecraft.client.renderer.rendertype.RenderTypes','net.minecraft.client.renderer.RenderType')
        text=re.sub(r'\bRenderTypes\b','RenderType',text)
        text=text.replace('.linesTranslucentNoDepthWrite()', '.lines()').replace('.linesTranslucent()', '.lines()')
        text=text.replace('net.minecraft.client.model.player.PlayerModel','net.minecraft.client.model.PlayerModel')
        text=text.replace('ClientAsset.Texture','ResourceLocation').replace('net.minecraft.core.ResourceLocation','net.minecraft.resources.ResourceLocation')
        text=text.replace('version.name()', 'version.getName()').replace('version.protocolVersion()', 'version.getProtocolVersion()')
        text=re.sub(r'((?:state\.)?item)\.getModelBoundingBox\(\)',r'ru.he3hauka.toolvisuals.client.items.render.ItemGeometry.bounds(\1)',text)
        text=text.replace('var mode=client.getWindow().getVideoMode();', 'int refresh=client.getWindow().getRefreshRate();')
        text=text.replace('mode!=null&&mode.getRefreshRate()>0', 'refresh>0').replace('Math.round(mode.getRefreshRate())','refresh')

        text=re.sub(r'\bAvatarRenderer\b','PlayerRenderer',text)
        text=rename_symbol(text,'Avatar','Player')
        text=text.replace('net.minecraft.world.entity.Avatar','net.minecraft.world.entity.player.Player').replace('net.minecraft.world.entity.Player','net.minecraft.world.entity.player.Player')
        text=text.replace('net.minecraft.client.renderer.rendertype.RenderType','net.minecraft.client.renderer.RenderType')
        text=text.replace('RenderPipelines.GUI_TEXTURED','ru.he3hauka.toolvisuals.client.compatibility.render.LegacyGuiPipelines.GUI_TEXTURED')
        if text.count('RenderPipelines')==1:text=text.replace('import net.minecraft.client.renderer.RenderPipelines;','')
        if '.compatibility.gui;' not in text and '@Mixin' not in text:
            text=re.sub(r'\bEditBox\b','LegacyEditBox',text).replace('net.minecraft.client.gui.components.LegacyEditBox','ru.he3hauka.toolvisuals.client.compatibility.gui.LegacyEditBox')
        text=text.replace('InputConstants.getKey(event)', 'InputConstants.getKey(event.key(),event.scancode())')
        text=text.replace('com.mojang.blaze3d.platform.cursor.CursorTypes','ru.he3hauka.toolvisuals.client.compatibility.gui.VersionGui.Cursor').replace('CursorTypes.','Cursor.')
        text=re.sub(r'\b(\w+)\.requestCursor\(([^;]+)\)',r'ru.he3hauka.toolvisuals.client.compatibility.gui.VersionGui.cursor(\2)',text)
        text=text.replace('super(() -> label, pixels)', 'super(pixels)').replace('super(()->id.toString(),pixels)', 'super(pixels)')
        text=text.replace('sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);','setFilter(true,false);').replace('sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);','setFilter(true,false);')
        text=text.replace('import com.mojang.renderpearl.api.textures.FilterMode;','')
        text=re.sub(r'new DynamicTexture\(\(\)\s*->\s*id.toString\(\),\s*(\w+)\)',r'new DynamicTexture(\1)',text)
        text=text.replace('net.minecraft.core.ClientAsset.Texture','net.minecraft.resources.ResourceLocation').replace('import net.minecraft.core.ClientAsset;','')
        text=text.replace('ClientAsset.Texture','ResourceLocation').replace('net.minecraft.core.ResourceLocation','net.minecraft.resources.ResourceLocation')
        text=text.replace('version.name()', 'version.getName()').replace('version.protocolVersion()', 'version.getProtocolVersion()')
        text=re.sub(r'((?:state\.)?item)\.getModelBoundingBox\(\)',r'ru.he3hauka.toolvisuals.client.items.render.ItemGeometry.bounds(\1)',text)
        text=text.replace('var mode=client.getWindow().getVideoMode();', 'int refresh=client.getWindow().getRefreshRate();')
        text=text.replace('mode!=null&&mode.getRefreshRate()>0', 'refresh>0').replace('Math.round(mode.getRefreshRate())','refresh')

        text=text.replace('state.isSolidRender(level,pos)', 'state.isSolidRender()').replace('adjacent.isSolidRender(level,neighbor)','adjacent.isSolidRender()')
        text=text.replace('client.levelExtractor.', 'client.levelRenderer.')
        text=text.replace('avatar.skin.elytra()', 'avatar.skin.elytraTexture()')
        text=text.replace('g.pose().identity()', 'g.pose().setIdentity()')
        text=text.replace('client.gameRenderer.getLevelRenderState().cameraRenderState', 'ru.he3hauka.toolvisuals.client.compatibility.render.VersionCamera.SCENE.cameraRenderState')
        if 'PlayerRenderer.class)' in text:
            text=text.replace('import net.minecraft.client.player.AbstractClientPlayer;', 'import net.minecraft.client.player.AbstractClientPlayer;')
            text=re.sub(r'\bPlayer (player|entity)\b',r'AbstractClientPlayer \1',text)
            text=text.replace('Lnet/minecraft/world/entity/Player;', 'Lnet/minecraft/client/player/AbstractClientPlayer;')

        text=re.sub(r'KeyMapping.Category category = KeyMapping.Category.register\([\s\S]*?\);', 'String category="key.categories.toolvisuals";',text)
        text=text.replace('import io.netty.channel.ChannelFutureListener;','import net.minecraft.network.PacketSendListener;').replace('ChannelFutureListener listener', 'PacketSendListener listener')
        text=text.replace('listener.operationComplete(embedded.newSucceededFuture())', 'listener.onSuccess()')
        text=text.replace('Lio/netty/channel/ChannelFutureListener;', 'Lnet/minecraft/network/PacketSendListener;')
        text=text.replace('packet.containerId()', 'packet.getContainerId()')

        text=text.replace('.getProfile().name()', '.getProfile().getName()').replace('.getProfile().id()', '.getProfile().getId()')
        text=text.replace('client.player.isWithinBlockInteractionRange(hit.getBlockPos(), 0)', 'client.player.canInteractWithBlock(hit.getBlockPos(), 0)')
        text=text.replace('sound.getResourceLocation()', 'sound.getLocation()')
        text=text.replace('inventory.getSelectedSlot()', 'inventory.selected').replace('.getInventory().getSelectedSlot()', '.getInventory().selected')
        text=text.replace('inventory.getSelectedItem()', 'inventory.getSelected()').replace('.getInventory().getSelectedItem()', '.getInventory().getSelected()')
        text=text.replace('actor.getTicksUsingItem(partial)', '(actor.getTicksUsingItem()+partial)')
        text=text.replace('var right=state.rightHandItemStack;var leftStack=state.leftHandItemStack;', 'var right=actor.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);var leftStack=actor.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND);')
        text=re.sub(r'armorPreview.shadowPieces.clear\(\);', '',text)
        for before,after in [('RunCommand','runCommand'),('SuggestCommand','suggestCommand'),('CopyToClipboard','copy'),('OpenUrl','url')]:
            text=text.replace('new ClickEvent.'+before+'(', 'ru.he3hauka.toolvisuals.client.compatibility.gui.ChatEvents.'+after+'(')
        text=text.replace('new HoverEvent.ShowText(', 'ru.he3hauka.toolvisuals.client.compatibility.gui.ChatEvents.hover(')
        text=text.replace('.matchesMouse(event)', '.matchesMouse(event.button())')
        text=text.replace('Minecraft.getInstance().getWindow().getGuiScale()', '(int)Minecraft.getInstance().getWindow().getGuiScale()')
        text=text.replace('client.getWindow().isFocused()', 'client.isWindowActive()')
        text=text.replace('Minecraft.getInstance().getWindow().handle()', 'Minecraft.getInstance().getWindow().getWindow()')
        text=text.replace('original.renderOutline()', 'original.outline()')
        text=text.replace('new User(name, offlineId(name), "", Optional.empty(), Optional.empty())', 'new User(name, offlineId(name), "", Optional.empty(), Optional.empty(),User.Type.LEGACY)')
        text=text.replace('new FontDescription.Resource(ResourceLocation.parse("toolvisuals:badge"))', 'ResourceLocation.parse("toolvisuals:badge")')
        for before,after in [('SPEED','MOVEMENT_SPEED'),('STRENGTH','DAMAGE_BOOST'),('NAUSEA','CONFUSION')]:text=text.replace('MobEffects.'+before,'MobEffects.'+after)
        text=text.replace('net.caffeinemc.mods.sodium.client.gui.SodiumOptions','net.caffeinemc.mods.sodium.client.gui.SodiumGameOptions').replace('SodiumOptions.writeToDisk','SodiumGameOptions.writeToDisk')

        text=text.replace('var mode=client.getWindow().getActiveVideoMode();', 'var mode=client.getWindow().getVideoMode();')
        text=text.replace('limiter.getThrottleReason().name()', '"NATIVE"')

        text=re.sub(r'\.createLookup\((\w+),true\)',r'.lookupInsecure(\1)',text)
        text=text.replace('import net.minecraft.client.gui.TextAlignment;', '')
        text=re.sub(r'state\.shadowPieces\.clear\(\);\s*','',text)
        text=text.replace('state.leashStates', 'state.leashState').replace('armorPreview.leashStates','armorPreview.leashState')
        text=re.sub(r'(?:state|armorPreview)\.outlineColor\s*=\s*(?:EntityRenderState.NO_OUTLINE|0);\s*', '',text)
        text=re.sub(r'\b(g|graphics|extractor)\.entity\(',r'ru.he3hauka.toolvisuals.client.compatibility.gui.EntityPreviews.draw(\1,',text)
        text=text.replace('var state = renderer.createRenderState(target, 1);','var state = renderer.createRenderState(target, 1);ru.he3hauka.toolvisuals.client.compatibility.gui.EntityPreviews.register(state,renderer);')
        if '@Mixin(PlayerRenderer.class)' in text:
            text=text.replace('import net.minecraft.world.entity.player.Player;','import net.minecraft.client.player.AbstractClientPlayer;')
            text=re.sub(r'\bPlayer player\b','AbstractClientPlayer player',text).replace('Lnet/minecraft/world/entity/player/Player;', 'Lnet/minecraft/client/player/AbstractClientPlayer;').replace('Lnet/minecraft/world/entity/Avatar;', 'Lnet/minecraft/client/player/AbstractClientPlayer;')

        text=text.replace('ru.he3hauka.toolvisuals.client.effects.render.EffectPipeline.initialize();', 'ru.he3hauka.toolvisuals.client.effects.render.EffectPipeline.initialize();ru.he3hauka.toolvisuals.client.compatibility.render.LegacyWorld.initialize();')
        text=text.replace('Gui.getMobEffectSprite(', 'Minecraft.getInstance().getMobEffectTextures().get(').replace('Hud.getMobEffectSprite(', 'Minecraft.getInstance().getMobEffectTextures().get(')
        text=text.replace('Identifier icon','net.minecraft.client.renderer.texture.TextureAtlasSprite icon')
        text=text.replace('g.blitSprite(RenderPipelines.GUI_TEXTURED, effect.icon,', 'ru.he3hauka.toolvisuals.client.compatibility.gui.VersionGui.potionIcon(g,effect.icon,')

        if '.compatibility.gui;' not in text and '@Mixin' not in text:
            text=text.replace('new AbstractSliderButton(', 'new ru.he3hauka.toolvisuals.client.compatibility.gui.LegacySlider(').replace('extends AbstractSliderButton {', 'extends ru.he3hauka.toolvisuals.client.compatibility.gui.LegacySlider {')
        text=re.sub(r'KeyMapping.Category category = KeyMapping.Category.register\(\s*ResourceLocation.fromNamespaceAndPath\(ToolVisuals.MOD_ID, "controls"\)\);', 'String category="key.categories.toolvisuals";',text)

        text=re.sub(r'\b(\w+)\.textRenderer\(\)\.accept\(TextAlignment.CENTER,',r'ru.he3hauka.toolvisuals.client.compatibility.gui.VersionGui.centered(\1,',text)
        text=text.replace('ConnectionIdentity.launcherProfile(minecraft).name()', 'ConnectionIdentity.launcherProfile(minecraft).getName()')
        text=text.replace('.dimension().identifier()', '.dimension().location()')
        text=text.replace('ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC','ItemStack.OPTIONAL_STREAM_CODEC')
        text=text.replace('import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;','')
        text=re.sub(r'\s*RenderPipelinesAccessor.toolvisuals\$register\(UiPipelines.(?:TEXT|RASTER_TEXT)\);','',text)
        text=re.sub(r'KeyMapping.Category category = KeyMapping.Category.register\(\s*ResourceLocation.fromNamespaceAndPath\("toolvisuals", "main"\)\);', 'String category = "key.categories.toolvisuals";',text)
        text=text.replace('import net.minecraft.world.item.component.TooltipDisplay;', '')
        text=text.replace('var display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);','boolean hidden=stack.has(DataComponents.HIDE_TOOLTIP)||stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP);')
        text=text.replace('display.hideTooltip() || !display.shows(DataComponents.CONTAINER)','hidden')
        text=text.replace('Minecraft.getInstance().hasShiftDown()','net.minecraft.client.gui.screens.Screen.hasShiftDown()')

        text=text.replace('launcher.name()', 'launcher.getName()').replace('launcher.id()', 'launcher.getId()').replace('profile.name()', 'profile.getName()').replace('profile.id()', 'profile.getId()')
        text=text.replace('new Font(new TabFontProvider(((FontAccessor)(Object)original).toolvisuals$fontProvider(), raster))','new Font(new TabFontProvider(((FontAccessor)(Object)original).toolvisuals$fontProvider(), raster),false)')

        text=text.replace('import net.minecraft.network.chat.FontDescription;', '')
        text=text.replace('FontDescription face','ResourceLocation face').replace('new FontDescription.Resource(ResourceLocation.fromNamespaceAndPath("toolvisuals", path))','ResourceLocation.fromNamespaceAndPath("toolvisuals", path)')
        text=text.replace('original).toolvisuals$fontProvider(), raster))','original).toolvisuals$fontProvider(), raster),false)')
        text=text.replace('UiShapeBatch.begin();','').replace('UiShapeBatch.end();','')
        text=re.sub(r'\b(\w+)\.matches\((event|e)\)',r'\1.matches(\2.key(),\2.scancode())',text)
        text=re.sub(r'(?<![\w.])PlayerSkin skin,','net.minecraft.client.resources.PlayerSkin skin,',text).replace('import net.minecraft.world.entity.player.PlayerSkin;','import net.minecraft.client.resources.PlayerSkin;')
        text=text.replace('new ClientAsset.ResourceTexture(id,id)','id')
        text=text.replace('.cape().texturePath()', '.capeTexture()').replace('.body().texturePath()', '.texture()').replace('old.cape()', 'old.capeTexture()').replace('state.skin.cape()', 'state.skin.capeTexture()').replace('old.elytra()', 'old.elytraTexture()')
        text=text.replace('new PlayerSkin(old.body(),', 'new PlayerSkin(old.texture(),old.textureUrl(),')
        text=text.replace('.getGameProfile().name()', '.getGameProfile().getName()').replace('.getGameProfile().id()', '.getGameProfile().getId()')

        if '.compatibility.gui;' not in text and '@Mixin' not in text:
            text=text.replace('extends Screen {','extends ru.he3hauka.toolvisuals.client.compatibility.gui.LegacyScreen {')
            text=text.replace('extends EditBox {','extends ru.he3hauka.toolvisuals.client.compatibility.gui.LegacyEditBox {')
            text=text.replace('extends Button {','extends ru.he3hauka.toolvisuals.client.compatibility.gui.LegacyButton {')
        text=legacy_pose(text)
        text=text.replace('.pushMatrix()', '.pushPose()').replace('.popMatrix()', '.popPose()')
        text=text.replace('client.getWindow().handle()', 'client.getWindow().getWindow()').replace('window.handle()', 'window.getWindow()')
    if version=='1.21.4':
        text=text.replace('Identifier','ResourceLocation').replace('net.minecraft.world.entity.player.PlayerSkin','net.minecraft.client.resources.PlayerSkin').replace('AvatarRenderState','PlayerRenderState')
    gui_mixin='@Mixin' not in text or bool(re.search(r'@Mixin\([^)]*(?:net\.minecraft\.client\.gui\.|\b\w*Screen\.class|\bServerSelectionList\.)',text)) or any(re.search(r'@Mixin\([^)]*\b'+re.escape(name)+r'\.class',text) and re.search(r'import net\.minecraft\.client\.gui\.[^;]*\b'+re.escape(name)+r';',text) for name in re.findall(r'import [^;]+\.(\w+);',text))
    for before,after in REPLACEMENTS:
        if before=='extractRenderState' and not gui_mixin:continue
        text=re.sub(r'\b'+re.escape(before)+r'\b',after,text) if before.isidentifier() else text.replace(before,after)
    if gui_mixin and 'net.minecraft.client.gui.' in text:
        text=re.sub(r'\bextract([A-Z]\w*)',r'render\1',text)
    text=re.sub(r'\b(g|graphics|extractor)\.text\(',r'\1.drawString(',text)
    for before,after in [('entity','submitEntityRenderState'),('item','renderItem'),('itemDecorations','renderItemDecorations')]:
        text=re.sub(r'\b(g|graphics|extractor)\.'+before+r'\(',r'\1.'+after+'(',text)
    text=re.sub(r'\.rotate\((Axis\.\w+),([^;]+?)\);',r'.mulPose(\1.rotation(\2));',text)
    text=re.sub(r'\bHud\b','Gui',text)
    text=re.sub(r'(\b\w+)\.gui\.isHidden\(\)',r'\1.options.hideGui',text)
    text=re.sub(r'InputConstants\.isKeyDown\(([^()]*(?:\([^()]*\)[^()]*)?)\)',r'ru.he3hauka.toolvisuals.client.compatibility.input.VersionInput.down(\1)',text)
    text=text.replace('import org.lwjgl.sdl.SDLKeyboard;','')
    text=text.replace('(SDLKeyboard.SDL_GetModState() & InputConstants.MOD_SHIFT) != 0','ru.he3hauka.toolvisuals.client.compatibility.input.VersionInput.shift()')
    text=text.replace('Blaze3D.openFile(', 'net.minecraft.Util.getPlatform().openFile(').replace('com.mojang.blaze3d.net.minecraft.Util','net.minecraft.Util')
    text=text.replace('Blaze3D.openPath(', 'ru.he3hauka.toolvisuals.client.compatibility.files.UserFolders.open(').replace('com.mojang.blaze3d.ru.he3hauka','ru.he3hauka')
    text=text.replace('Blaze3D.openUri(', 'net.minecraft.Util.getPlatform().openUri(').replace('com.mojang.blaze3d.net.minecraft.Util','net.minecraft.Util')
    text=text.replace('.nonEmptyItemCopyStream()', '.nonEmptyItemsStream()')
    text=text.replace('container.size()', 'container.getSlots()')
    text=text.replace('charged.items()', 'charged.getItems()')
    text=text.replace('.entityIds()', '.getEntityIds()')
    text=text.replace('.onlineMode()', '.getConnection().isEncrypted()')
    text=text.replace('client.gui.toastManager()', 'client.getToastManager()')
    text=text.replace('import net.minecraft.client.GuiMessageSource;','').replace('import net.minecraft.client.multiplayer.chat.GuiMessageSource;','')
    text=text.replace('message.source()!=GuiMessageSource.PLAYER','message.signature()==null')
    text=text.replace('.setVisibleMessageFilter(ChatTabs::visible)', '.rescaleChat()')
    text=text.replace('.addClientSystemMessage(', '.addMessage(')
    text=text.replace('gui.screen()', 'Minecraft.getInstance().screen')
    text=text.replace('String downloads = org.lwjgl.sdl.SDLFileSystem.SDL_GetUserFolder(org.lwjgl.sdl.SDLFileSystem.SDL_FOLDER_DOWNLOADS);','String downloads = ru.he3hauka.toolvisuals.client.compatibility.files.UserFolders.downloads().toString();')
    text=text.replace('pack.openMetadata()', 'pack.open()')
    text=text.replace('prepare8xTextOutline', 'drawInBatch8xOutline')
    text=text.replace('PlayerFaceRenderer.render(', 'PlayerFaceRenderer.draw(')
    text=text.replace('container.nonEmptyItemsStream()', 'container.nonEmptyStream()').replace('container.getSlots()', '(int)container.stream().count()')
    text=re.sub(r'\.rotateDegrees\((Axis\.\w+),([^;]+?)\);',r'.mulPose(\1.rotationDegrees(\2));',text)
    text=re.sub(r'(\w+)\.isSwinging\(\)',r'\1.swinging',text)
    text=re.sub(r'(\w+)\.scoreText\s*=\s*null;',r'ru.he3hauka.toolvisuals.client.compatibility.render.VersionState.clearScore(\1);',text)
    text=re.sub(r'(\w+)\.scoreText\s*!=\s*null',r'ru.he3hauka.toolvisuals.client.compatibility.render.VersionState.hasScore(\1)',text)
    text=re.sub(r'(state\.lightCoords,\s*)camera\)',r'\1state.distanceToCameraSq,camera)',text)
    text=re.sub(r'(?:\w+\.)?cameraRenderState\.isFirstPerson|camera\.isFirstPerson',r'Minecraft.getInstance().options.getCameraType().isFirstPerson()',text)
    text=text.replace('camera.projectionMatrix','ru.he3hauka.toolvisuals.client.compatibility.render.VersionCamera.projection()')
    text=text.replace('camera.viewRotationMatrix','ru.he3hauka.toolvisuals.client.compatibility.render.VersionCamera.view()')
    text=text.replace('camera.hudFov','ru.he3hauka.toolvisuals.client.compatibility.render.VersionCamera.handFov()')
    text=text.replace('camera.cullFrustum','ru.he3hauka.toolvisuals.client.compatibility.render.VersionCamera.frustum()')
    text=text.replace('.withColorTargetState(ColorTargetState.DEFAULT)', '.withoutBlend()')
    text=text.replace('.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))', '.withBlend(BlendFunction.TRANSLUCENT)')
    text=re.sub(r'\.withDepthStencilState\(new DepthStencilState\(CompareOp.GREATER_THAN_OR_EQUAL,(true|false)\)\)',r'.withDepthTestFunction(com.mojang.blaze3d.platform.DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(\1)',text)
    text=text.replace('.withForcedSolidModelPhase()', '')
    text=text.replace('import de.maxhenkel.voicechat.api.internal.events.HudIconRenderEvent;','').replace('import de.maxhenkel.voicechat.api.internal.events.StartMicEvent;','')
    text=re.sub(r'^\s*events.registerEvent\((?:StartMicEvent|HudIconRenderEvent)\.class,.*?;\s*$', '',text,flags=re.MULTILINE)
    if version=='1.21.11':text=re.sub(r'(\w+)\.worldPartialTicks',r'Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)',text)
    if version=='1.21.11':
        text=text.replace('client.getWindow().isFocused()', 'client.isWindowActive()')
        text=text.replace('net.minecraft.Util', 'net.minecraft.util.Util')
        text=text.replace('client.levelExtractor.', 'client.levelRenderer.')
        text=text.replace('var mode=client.getWindow().getActiveVideoMode();', 'int refresh=client.getWindow().getRefreshRate();')
        text=text.replace('mode!=null&&mode.getRefreshRate()>0', 'refresh>0').replace('Math.round(mode.getRefreshRate())', 'refresh')
    if version=='1.21.4' and 'public final class VisualWorld {' in text:
        start=text.index('        if(current.strokes.size>0)')
        end=text.index('        Hat hat',start)
        text=text[:start]+'        ru.he3hauka.toolvisuals.client.compatibility.render.ScreenLines.draw(current.strokes,camera,collector);\n'+text[end:]
        text=text.replace('.setLineWidth(width)','')
    if version=='1.21.4' and 'public final class DeathAscension {' in text:
        text=text.replace('    private DeathAscension(){}','    private DeathAscension(){}\n    public static boolean hideBody(int entity){return ToolVisualsClient.CONFIG.active(DEATH_ASCENSION)&&ToolVisualsClient.CONFIG.flag(DEATH_HIDE_BODY)&&TIMELINE.containsEntity(entity);}')
    if version=='1.21.4':
        text=text.replace('.getMainCamera().position()', '.getMainCamera().getPosition()')
        text=text.replace('var mode=client.getWindow().getVideoMode();','int refresh=client.getWindow().getRefreshRate();')
        text=text.replace('renderBlurredBackground(graphics)', 'renderBlurredBackground()')
        text=text.replace('original.renderOutline()', 'original.outline()')
        text=text.replace('client.gameRenderer.getLevelRenderState().cameraRenderState', 'ru.he3hauka.toolvisuals.client.compatibility.render.VersionCamera.SCENE.cameraRenderState')
        text=text.replace('sound.getResourceLocation()', 'sound.getLocation()')
        text=text.replace('target.onPress(null)', 'target.onPress()')
        text=text.replace('entity.setLastHurtByPlayer(player,40)', 'entity.setLastHurtByPlayer(player)')
        text=text.replace('.isWithinBlockInteractionRange(position, 0)', '.canInteractWithBlock(position, 0)')
        text=re.sub(r'new FontDescription.Resource\((ResourceLocation\.(?:parse|fromNamespaceAndPath)\([^;]+?)\)\)',r'\1)',text)
        text=text.replace('world.getEntity(crystal.getUUID())', '((ru.he3hauka.toolvisuals.mixin.world.LevelEntitiesAccessor)world).toolvisuals$entities().get(crystal.getUUID())')
        for name,method in [('SuggestCommand','suggestCommand'),('RunCommand','runCommand'),('CopyToClipboard','copy'),('OpenUrl','url')]:
            text=text.replace('new net.minecraft.network.chat.ClickEvent.'+name+'(', 'ru.he3hauka.toolvisuals.client.compatibility.gui.ChatEvents.'+method+'(')
        text=text.replace('new net.minecraft.network.chat.HoverEvent.ShowText(', 'ru.he3hauka.toolvisuals.client.compatibility.gui.ChatEvents.hover(')

    for name in ['net.minecraft.Util','net.minecraft.util.Util']:
        text=text.replace(name+'.getPlatform().openFile(', 'ru.he3hauka.toolvisuals.client.compatibility.files.UserFolders.open(')
    text=text.replace('KeyMappingHelper','KeyBindingHelper')
    return key_matches(text)

def contents(version,tests=False):
    kind='test' if tests else 'main'
    target=OUT/version/('generated-tests' if tests else 'generated-sources')
    root=ROOT/'src'/kind/'java'
    excluded=ROOT/'versions'/version/'excludes.json'
    excludes=set(json.loads(excluded.read_text(encoding='utf-8'))) if excluded.exists() else set()
    local_excludes=ROOT/'versions'/version/'local-test-excludes.json'
    if tests and local_excludes.is_file():excludes.update(json.loads(local_excludes.read_text(encoding='utf-8')))
    paths={p.relative_to(root):translate(p.read_text(encoding='utf-8'),version) for p in root.rglob('*.java') if p.relative_to(root).as_posix() not in excludes}
    for overlay in [ROOT/'versions/common'/kind/'java',ROOT/'versions'/version/kind/'java']:
        for p in overlay.rglob('*.java') if overlay.exists() else []:paths[p.relative_to(overlay)]=translate(p.read_text(encoding='utf-8'),version)
    local_adapter=ROOT/'build-tools/multiversion/local_test_sources.py'
    if tests and local_adapter.is_file():
        import importlib.util
        spec=importlib.util.spec_from_file_location('toolvisuals_local_sources',local_adapter)
        adapter=importlib.util.module_from_spec(spec);spec.loader.exec_module(adapter)
        paths=adapter.adapt(paths,version)
    return paths

def generate(version,tests=False):
    target=OUT/version/('generated-tests' if tests else 'generated-sources')
    paths=contents(version,tests)
    if target.resolve().parent!= (OUT/version).resolve():raise RuntimeError('Invalid generated sources directory')
    for old in target.rglob('*.java') if target.exists() else []:
        if old.relative_to(target) not in paths:old.unlink()
    for path,source in paths.items():
        destination=target/path;destination.parent.mkdir(parents=True,exist_ok=True);destination.write_text(source,encoding='utf-8')
    return [target/p for p in sorted(paths)]
