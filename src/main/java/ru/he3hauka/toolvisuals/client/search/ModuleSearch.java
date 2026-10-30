package ru.he3hauka.toolvisuals.client.search;

import java.util.*;

import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

public final class ModuleSearch {
    private record Term(String text,int weight){}
    private record Document(VisualModule module,List<Term> terms,Set<String> words){}
    private record Match(VisualModule module,int score){}
    private static final List<Document> DOCUMENTS=Arrays.stream(VisualModule.values()).map(ModuleSearch::document).toList();
    private ModuleSearch(){}
    public static List<VisualModule> find(String query) {
        var variants=SearchText.variants(query.length()>80?query.substring(0,80):query);
        if(variants.isEmpty())return List.of(VisualModule.values());
        return DOCUMENTS.stream().map(doc->new Match(doc.module,variants.stream().mapToInt(value->score(doc,value)).max().orElse(0))).filter(match->match.score>0).sorted(Comparator.comparingInt(Match::score).reversed().thenComparing(match->match.module.title)).map(Match::module).toList();
    }
    private static Document document(VisualModule module) {
        var terms=new ArrayList<Term>();add(terms,module.title,120);add(terms,module.id,110);add(terms,aliases(module),105);
        for(var language:UiLanguage.values()) {
            add(terms,UiI18n.text(language,"toolvisuals.module."+module.id),50);
            for(var setting:VisualSetting.values())if(setting.module==module)add(terms,UiI18n.text(language,"toolvisuals.setting."+setting.id),35);
            if(module==VisualModule.NO_RENDER)for(var option:RenderOption.values())add(terms,UiI18n.text(language,"toolvisuals.option."+option.id),30);
        }
        var words=new HashSet<String>();for(var term:terms)words.addAll(List.of(term.text.split(" ")));
        words.add(SearchText.normalize(module.title).replace(" ",""));words.add(module.id.replace("_",""));
        return new Document(module,List.copyOf(terms),Set.copyOf(words));
    }
    private static void add(List<Term> terms,String value,int weight){String normalized=SearchText.normalize(value);terms.add(new Term(normalized,weight));String latin=SearchText.transliterate(normalized);if(!latin.equals(normalized))terms.add(new Term(latin,weight));}
    private static int score(Document document,String query) {
        String compact=query.replace(" ","");int best=0;
        for(var term:document.terms) {
            String joined=term.text.replace(" ","");
            if(joined.equals(compact))best=Math.max(best,term.weight+300);
            else if(joined.startsWith(compact))best=Math.max(best,term.weight+240);
            else if(joined.contains(compact))best=Math.max(best,term.weight+170);
        }
        String[] tokens=query.split(" ");if(tokens.length>8)return best;
        int total=0;
        for(String token:tokens) {
            int found=0;
            for(String word:document.words) {
                if(word.equals(token))found=Math.max(found,100);
                else if(word.startsWith(token))found=Math.max(found,80);
                else if(token.length()>=3&&word.contains(token))found=Math.max(found,65);
                else if((token.length()>=4||tokens.length>1&&token.length()>=3)&&Math.abs(word.length()-token.length())<=2){int distance=SearchText.distance(token,word);if(distance<=1||token.length()>=7&&distance<=2)found=Math.max(found,55-distance*12);}
            }
            if(found==0)return best;
            total+=found;
        }
        return Math.max(best,total/tokens.length);
    }
    private static String aliases(VisualModule module) {
        return switch(module) {
            case MUSIC_HUD -> "музыка музыкальный плеер треки песни спотифай яндекс ютуб пауза переключить music hud media player spotify yandex youtube music";
            case SHULKER_TOOLTIP -> "шалкер шалкеры шалкертултип подсказка содержимое превью контейнер shulker tooltip box contents preview inventory";
            case MINI_GAMES -> "миниигры мини игры энгрибердс энгри бёрдс крестики нолики четыре в ряд играть с друзьями minigames angry birds tic tac toe connect four games";
            case DROP_PROTECT -> "дроппротект защита выбрасывание выброс потеря редкие ценные предметы подтверждение drop protect confirmation valuable items";
            case SESSION_STATS -> "статистика сессия время игры смерти блоки расстояние session stats playtime deaths distance blocks";
            case TAPE_MOUSE -> "тейпмаус тейпмауз мышь удержание зажать автокликер ферма лкм пкм tape mouse hold click farm afk";
            case DAMAGE_VIGNETTE -> "виньетка урон кровь края экрана красный низкое здоровье hp damage health hurt vignette screen edges";
            case MOTION_BLUR->"моушенблюр размытие движение motion blur frame smoothing";
            case BLOCK_HIGHLIGHT->"блок оверлей подсветка обводка выделение блоков block overlay selection outline";
            case REPLAY->"повторы реплей запись откат просмотр запись мира replay recording playback";
            case TOOL_PRESENTS->"подарки подарочек поиск маршрут спавн хэллоуин навигация координаты gift presents spawn route halloween";
            case APPLE_SKIN->"апплскин яблокоскин еда голод насыщение истощение здоровье food hunger saturation exhaustion appleskin";
            case KEYBINDS->"кейбинды бинды клавиши команды макросы keybind macro bind keyboard commands";
            case VOICE_CHAT->"войс голос чат микрофон разговаривать voice microphone proximity simple voice chat";
            case CRYSTAL_OPTIMIZER->"кристалл кристаллы оптимайзер оптимизация задержка crystal optimizer latency marlow cpvp";
            case NO_RENDER->"норендер скрыть убрать отключить дождь туман огонь частицы render hide fog rain fire";
            case WORLD_CUSTOMIZER->"мир время небо погода world sky time weather";
            case FULL_BRIGHT->"фуллбрайт яркость свет темнота brightness gamma light";
            case WORLD_PARTICLES->"частицы мира снег пыль ambient particles snow";
            case WAYPOINTS->"точки метки координаты waypoint marker";
            case OPTIMIZATION->"оптимизация фпс лаги производительность fps performance optimization";
            case AUTO_SPRINT->"автоспринт бег спринт sprint running";
            case CHANGE_HAND,CUSTOM_HAND->"рука руки hand viewmodel";
            case COMMAND_FIX->"команды раскладка исправление command keyboard layout";
            case ELYTRA_SWAP->"элитры свап нагрудник elytra chestplate swap";
            case FAKE_PLAYER->"фейк игрок манекен fake dummy";
            case FAST_EXP->"фастэксп фастексп опыт пузырьки exp experience bottles";
            case ITEM_SCROLLER->"скроллер перенос предметы shift inventory scroll";
            case PVP_SAVE->"пвпсейв бой выход защита combat logout";
            case AUCTION_HELPER->"аукцион перепродажа реселл auction resell";
            case JUMP_CIRCLES->"круги прыжок jump circles";
            case TRAILS->"следы шлейф trails";
            case CHINA_HAT->"шляпа китайская hat";
            case ZOOM->"зум приближение лупа zoom";
            case NAME_PROTECT->"неймпротект ник имя защита privacy name";
            case NO_HURT_CAM->"нохурткам камера тряска качание hurt shake bobbing";
            case PROJECTILE_PREDICTION->"траектория полет стрелы жемчуг prediction trajectory pearl";
            case ITEM_PHYSIC->"физика предметы лежат item physics";
            case TOTEM_POP_COUNTER->"тотем счетчик срабатывания totem counter";
            case SOUND_CUSTOMIZER->"звуки удар клики sound hit audio";
            case NO_SWING_ANIMATION->"взмах удар рука анимация swing attack animation";
            case CHUNK_ANIMATOR->"чанки загрузка плавность chunks animation";
            case MINI_ITEMS->"мини предметы размер items scale";
            case FREE_LOOK->"фрилук свободная камера третье лицо freelook camera";
            case NO_FLUID->"вода лава мыло прозрачность water lava fluid";
            case FAST_SHADERS->"шейдеры оптимизация shaders performance";
            case ARMOR_COLOR->"цвет броня прочность armor colour durability";
            case HIT_COLOR->"цвет удар урон hit color";
            case CUSTOM_HITBOXES->"хитбоксы контур линии цвет hitboxes outlines";
            case HIT_PARTICLES,HIT_BUBBLES->"удар частицы попадание hit particles bubbles";
            case ASPECT_RATIO->"разрешение растяжение экран aspect ratio resolution";
            case TARGET_ESP,TARGET_HUD->"цель таргет противник здоровье target enemy health";
            case HEALTH_INDICATOR->"зако здоровье хп индикатор удар здоровье противника zako health indicator hit hp";
            case CROSSHAIR->"прицел кроссхеир точка crosshair reticle aim pricel pritsel";
            case BETTER_HUD->"плавность чат таб интерфейс smooth hud chat tab";
            case INFO_HUD->"информация фпс координаты список модули скорость бпс info fps coordinates speed bps";
            case ARMOR_HUD->"броня армор прочность armor durability";
            case EXTRA_TAB->"экстратаб таб игроки список players list tab";
            case INVENTORY_HUD->"инвентарь инвентори хотбар inventory hotbar";
            case KEYSTROKES->"клавиши нажатия кнопки keys keystrokes mouse";
            case POTION_HUD->"эффекты зелья поты время potion effects";
            case COOLDOWN_HUD->"кулдауны задержки перезарядка cooldown timer";
            case CLIENT_LIST->"клиентлист список клиентов игроки визуалы toolvisuals online clients list";
            case FRIENDS_LIST->"друзья френдс онлайн friends online";
            case CHAT_TABS->"вкладки чат фильтры сообщения chat filter";
            case DURABILITY_ALERT->"поломка прочность предупреждения durability alert";
            case ITEM_INFO->"подсказки предметы прочность еда item tooltip food";
            case BOSS_BAR_CUSTOMIZER->"босс полоска боссбар boss bar";
            case STREAMER_MODE->"стример приватность скрыть ник айпи streamer privacy";
            case RESOURCE_PACK_SPOOFER->"ресурспак спуфер рп скачивание resource pack spoof";
            case EXTRA_ANIMATIONS->"анимации еда питьё карты лестница ползание лодка лошадь not enough animations third person eating climbing crawl row";
            case DEATH_ASCENSION->"смерть душа небеса вознесение death ascension soul angel heaven";
            case ANIMATIONS->"анимации эмоции танцы сальто emote dance animations";
            case COSMETICS->"косметика крылья рога корона нимб ушки спина голова cosmetics wings horns crown halo ears";
            case CAPES->"плащ плащи накидка мантия рисовать cape capes cloak editor";
            case CLIENT_BADGE->"значок бейдж логотип клиент ник badge logo client nametag";
        };
    }
}
