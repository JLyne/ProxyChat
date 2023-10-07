/*
 * ProxyChat, a Velocity chat solution
 * Copyright (C) 2020 James Lyne
 *
 * Based on BungeeChat2 (https://github.com/AuraDevelopmentTeam/BungeeChat2)
 * Copyright (C) 2020 Aura Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.notnull.ProxyChat.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.api.filter.FilterManager;
import uk.co.notnull.ProxyChat.api.filter.ProxyChatFilter;
import uk.co.notnull.ProxyChat.command.EmojiCommand;
import uk.co.notnull.ProxyChat.emoji.CustomEmoji;
import uk.co.notnull.ProxyChat.emoji.Emoji;
import uk.co.notnull.ProxyChat.filter.EmojiFilter;
import uk.co.notnull.ProxyChat.filter.EmojiPostFilter;
import uk.co.notnull.ProxyChat.util.LoggerHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class EmojiModule extends Module {
	private final Pattern incompleteEmojiPattern = Pattern.compile("(.*):(\\w+)$"); //Pattern for matching an incomplete :emoji_name:
	private final Pattern emojiPattern = Pattern.compile(":(\\w+):"); //Pattern for matching all default unicode emoji
	private Pattern customCharacterPattern; //Pattern for matching all configured custom emoji

	// https://github.com/mathiasbynens/emoji-test-regex-pattern
	private final Pattern defaultCharacterPattern = Pattern.compile("([#*\\d]\\x{FE0F}?\\x{20E3}|[\\xA9\\xAE\\x{203C}\\x{2049}\\x{2122}\\x{2139}\\x{2194}-\\x{2199}\\x{21A9}\\x{21AA}\\x{231A}\\x{231B}\\x{2328}\\x{23CF}\\x{23ED}-\\x{23EF}\\x{23F1}\\x{23F2}\\x{23F8}-\\x{23FA}\\x{24C2}\\x{25AA}\\x{25AB}\\x{25B6}\\x{25C0}\\x{25FB}\\x{25FC}\\x{25FE}\\x{2600}-\\x{2604}\\x{260E}\\x{2611}\\x{2614}\\x{2615}\\x{2618}\\x{2620}\\x{2622}\\x{2623}\\x{2626}\\x{262A}\\x{262E}\\x{262F}\\x{2638}-\\x{263A}\\x{2640}\\x{2642}\\x{2648}-\\x{2653}\\x{265F}\\x{2660}\\x{2663}\\x{2665}\\x{2666}\\x{2668}\\x{267B}\\x{267E}\\x{267F}\\x{2692}\\x{2694}-\\x{2697}\\x{2699}\\x{269B}\\x{269C}\\x{26A0}\\x{26A7}\\x{26AA}\\x{26B0}\\x{26B1}\\x{26BD}\\x{26BE}\\x{26C4}\\x{26C8}\\x{26CF}\\x{26D1}\\x{26D3}\\x{26E9}\\x{26F0}-\\x{26F5}\\x{26F7}\\x{26F8}\\x{26FA}\\x{2702}\\x{2708}\\x{2709}\\x{270F}\\x{2712}\\x{2714}\\x{2716}\\x{271D}\\x{2721}\\x{2733}\\x{2734}\\x{2744}\\x{2747}\\x{2757}\\x{2763}\\x{27A1}\\x{2934}\\x{2935}\\x{2B05}-\\x{2B07}\\x{2B1B}\\x{2B1C}\\x{2B55}\\x{3030}\\x{303D}\\x{3297}\\x{3299}\\x{1F004}\\x{1F170}\\x{1F171}\\x{1F17E}\\x{1F17F}\\x{1F202}\\x{1F237}\\x{1F321}\\x{1F324}-\\x{1F32C}\\x{1F336}\\x{1F37D}\\x{1F396}\\x{1F397}\\x{1F399}-\\x{1F39B}\\x{1F39E}\\x{1F39F}\\x{1F3CD}\\x{1F3CE}\\x{1F3D4}-\\x{1F3DF}\\x{1F3F5}\\x{1F3F7}\\x{1F43F}\\x{1F4FD}\\x{1F549}\\x{1F54A}\\x{1F56F}\\x{1F570}\\x{1F573}\\x{1F576}-\\x{1F579}\\x{1F587}\\x{1F58A}-\\x{1F58D}\\x{1F5A5}\\x{1F5A8}\\x{1F5B1}\\x{1F5B2}\\x{1F5BC}\\x{1F5C2}-\\x{1F5C4}\\x{1F5D1}-\\x{1F5D3}\\x{1F5DC}-\\x{1F5DE}\\x{1F5E1}\\x{1F5E3}\\x{1F5E8}\\x{1F5EF}\\x{1F5F3}\\x{1F5FA}\\x{1F6CB}\\x{1F6CD}-\\x{1F6CF}\\x{1F6E0}-\\x{1F6E5}\\x{1F6E9}\\x{1F6F0}\\x{1F6F3}]\\x{FE0F}?|[\\x{261D}\\x{270C}\\x{270D}\\x{1F574}\\x{1F590}][\\x{FE0F}\\x{1F3FB}-\\x{1F3FF}]?|[\\x{26F9}\\x{1F3CB}\\x{1F3CC}\\x{1F575}][\\x{FE0F}\\x{1F3FB}-\\x{1F3FF}]?(?:\\x{200D}[\\x{2640}\\x{2642}]\\x{FE0F}?)?|[\\x{270A}\\x{270B}\\x{1F385}\\x{1F3C2}\\x{1F3C7}\\x{1F442}\\x{1F443}\\x{1F446}-\\x{1F450}\\x{1F466}\\x{1F467}\\x{1F46B}-\\x{1F46D}\\x{1F472}\\x{1F474}-\\x{1F476}\\x{1F478}\\x{1F47C}\\x{1F483}\\x{1F485}\\x{1F48F}\\x{1F491}\\x{1F4AA}\\x{1F57A}\\x{1F595}\\x{1F596}\\x{1F64C}\\x{1F64F}\\x{1F6C0}\\x{1F6CC}\\x{1F90C}\\x{1F90F}\\x{1F918}-\\x{1F91F}\\x{1F930}-\\x{1F934}\\x{1F936}\\x{1F977}\\x{1F9B5}\\x{1F9B6}\\x{1F9BB}\\x{1F9D2}\\x{1F9D3}\\x{1F9D5}\\x{1FAC3}-\\x{1FAC5}\\x{1FAF0}\\x{1FAF2}-\\x{1FAF6}][\\x{1F3FB}-\\x{1F3FF}]?|[\\x{1F3C3}\\x{1F3C4}\\x{1F3CA}\\x{1F46E}\\x{1F470}\\x{1F471}\\x{1F473}\\x{1F477}\\x{1F481}\\x{1F482}\\x{1F486}\\x{1F487}\\x{1F645}-\\x{1F647}\\x{1F64B}\\x{1F64D}\\x{1F64E}\\x{1F6A3}\\x{1F6B4}-\\x{1F6B6}\\x{1F926}\\x{1F935}\\x{1F937}-\\x{1F939}\\x{1F93D}\\x{1F93E}\\x{1F9B8}\\x{1F9B9}\\x{1F9CD}-\\x{1F9CF}\\x{1F9D4}\\x{1F9D6}-\\x{1F9DD}][\\x{1F3FB}-\\x{1F3FF}]?(?:\\x{200D}[\\x{2640}\\x{2642}]\\x{FE0F}?)?|[\\x{1F46F}\\x{1F9DE}\\x{1F9DF}](?:\\x{200D}[\\x{2640}\\x{2642}]\\x{FE0F}?)?|[\\x{23E9}-\\x{23EC}\\x{23F0}\\x{23F3}\\x{25FD}\\x{2693}\\x{26A1}\\x{26AB}\\x{26C5}\\x{26CE}\\x{26D4}\\x{26EA}\\x{26FD}\\x{2705}\\x{2728}\\x{274C}\\x{274E}\\x{2753}-\\x{2755}\\x{2795}-\\x{2797}\\x{27B0}\\x{27BF}\\x{2B50}\\x{1F0CF}\\x{1F18E}\\x{1F191}-\\x{1F19A}\\x{1F201}\\x{1F21A}\\x{1F22F}\\x{1F232}-\\x{1F236}\\x{1F238}-\\x{1F23A}\\x{1F250}\\x{1F251}\\x{1F300}-\\x{1F320}\\x{1F32D}-\\x{1F335}\\x{1F337}-\\x{1F37C}\\x{1F37E}-\\x{1F384}\\x{1F386}-\\x{1F393}\\x{1F3A0}-\\x{1F3C1}\\x{1F3C5}\\x{1F3C6}\\x{1F3C8}\\x{1F3C9}\\x{1F3CF}-\\x{1F3D3}\\x{1F3E0}-\\x{1F3F0}\\x{1F3F8}-\\x{1F407}\\x{1F409}-\\x{1F414}\\x{1F416}-\\x{1F43A}\\x{1F43C}-\\x{1F43E}\\x{1F440}\\x{1F444}\\x{1F445}\\x{1F451}-\\x{1F465}\\x{1F46A}\\x{1F479}-\\x{1F47B}\\x{1F47D}-\\x{1F480}\\x{1F484}\\x{1F488}-\\x{1F48E}\\x{1F490}\\x{1F492}-\\x{1F4A9}\\x{1F4AB}-\\x{1F4FC}\\x{1F4FF}-\\x{1F53D}\\x{1F54B}-\\x{1F54E}\\x{1F550}-\\x{1F567}\\x{1F5A4}\\x{1F5FB}-\\x{1F62D}\\x{1F62F}-\\x{1F634}\\x{1F637}-\\x{1F644}\\x{1F648}-\\x{1F64A}\\x{1F680}-\\x{1F6A2}\\x{1F6A4}-\\x{1F6B3}\\x{1F6B7}-\\x{1F6BF}\\x{1F6C1}-\\x{1F6C5}\\x{1F6D0}-\\x{1F6D2}\\x{1F6D5}-\\x{1F6D7}\\x{1F6DD}-\\x{1F6DF}\\x{1F6EB}\\x{1F6EC}\\x{1F6F4}-\\x{1F6FC}\\x{1F7E0}-\\x{1F7EB}\\x{1F7F0}\\x{1F90D}\\x{1F90E}\\x{1F910}-\\x{1F917}\\x{1F920}-\\x{1F925}\\x{1F927}-\\x{1F92F}\\x{1F93A}\\x{1F93F}-\\x{1F945}\\x{1F947}-\\x{1F976}\\x{1F978}-\\x{1F9B4}\\x{1F9B7}\\x{1F9BA}\\x{1F9BC}-\\x{1F9CC}\\x{1F9D0}\\x{1F9E0}-\\x{1F9FF}\\x{1FA70}-\\x{1FA74}\\x{1FA78}-\\x{1FA7C}\\x{1FA80}-\\x{1FA86}\\x{1FA90}-\\x{1FAAC}\\x{1FAB0}-\\x{1FABA}\\x{1FAC0}-\\x{1FAC2}\\x{1FAD0}-\\x{1FAD9}\\x{1FAE0}-\\x{1FAE7}]|\\x{2764}\\x{FE0F}?(?:\\x{200D}[\\x{1F525}\\x{1FA79}])?|\\x{1F1E6}[\\x{1F1E8}-\\x{1F1EC}\\x{1F1EE}\\x{1F1F1}\\x{1F1F2}\\x{1F1F4}\\x{1F1F6}-\\x{1F1FA}\\x{1F1FC}\\x{1F1FD}\\x{1F1FF}]|\\x{1F1E7}[\\x{1F1E6}\\x{1F1E7}\\x{1F1E9}-\\x{1F1EF}\\x{1F1F1}-\\x{1F1F4}\\x{1F1F6}-\\x{1F1F9}\\x{1F1FB}\\x{1F1FC}\\x{1F1FE}\\x{1F1FF}]|\\x{1F1E8}[\\x{1F1E6}\\x{1F1E8}\\x{1F1E9}\\x{1F1EB}-\\x{1F1EE}\\x{1F1F0}-\\x{1F1F5}\\x{1F1F7}\\x{1F1FA}-\\x{1F1FF}]|\\x{1F1E9}[\\x{1F1EA}\\x{1F1EC}\\x{1F1EF}\\x{1F1F0}\\x{1F1F2}\\x{1F1F4}\\x{1F1FF}]|\\x{1F1EA}[\\x{1F1E6}\\x{1F1E8}\\x{1F1EA}\\x{1F1EC}\\x{1F1ED}\\x{1F1F7}-\\x{1F1FA}]|\\x{1F1EB}[\\x{1F1EE}-\\x{1F1F0}\\x{1F1F2}\\x{1F1F4}\\x{1F1F7}]|\\x{1F1EC}[\\x{1F1E6}\\x{1F1E7}\\x{1F1E9}-\\x{1F1EE}\\x{1F1F1}-\\x{1F1F3}\\x{1F1F5}-\\x{1F1FA}\\x{1F1FC}\\x{1F1FE}]|\\x{1F1ED}[\\x{1F1F0}\\x{1F1F2}\\x{1F1F3}\\x{1F1F7}\\x{1F1F9}\\x{1F1FA}]|\\x{1F1EE}[\\x{1F1E8}-\\x{1F1EA}\\x{1F1F1}-\\x{1F1F4}\\x{1F1F6}-\\x{1F1F9}]|\\x{1F1EF}[\\x{1F1EA}\\x{1F1F2}\\x{1F1F4}\\x{1F1F5}]|\\x{1F1F0}[\\x{1F1EA}\\x{1F1EC}-\\x{1F1EE}\\x{1F1F2}\\x{1F1F3}\\x{1F1F5}\\x{1F1F7}\\x{1F1FC}\\x{1F1FE}\\x{1F1FF}]|\\x{1F1F1}[\\x{1F1E6}-\\x{1F1E8}\\x{1F1EE}\\x{1F1F0}\\x{1F1F7}-\\x{1F1FB}\\x{1F1FE}]|\\x{1F1F2}[\\x{1F1E6}\\x{1F1E8}-\\x{1F1ED}\\x{1F1F0}-\\x{1F1FF}]|\\x{1F1F3}[\\x{1F1E6}\\x{1F1E8}\\x{1F1EA}-\\x{1F1EC}\\x{1F1EE}\\x{1F1F1}\\x{1F1F4}\\x{1F1F5}\\x{1F1F7}\\x{1F1FA}\\x{1F1FF}]|\\x{1F1F4}\\x{1F1F2}|\\x{1F1F5}[\\x{1F1E6}\\x{1F1EA}-\\x{1F1ED}\\x{1F1F0}-\\x{1F1F3}\\x{1F1F7}-\\x{1F1F9}\\x{1F1FC}\\x{1F1FE}]|\\x{1F1F6}\\x{1F1E6}|\\x{1F1F7}[\\x{1F1EA}\\x{1F1F4}\\x{1F1F8}\\x{1F1FA}\\x{1F1FC}]|\\x{1F1F8}[\\x{1F1E6}-\\x{1F1EA}\\x{1F1EC}-\\x{1F1F4}\\x{1F1F7}-\\x{1F1F9}\\x{1F1FB}\\x{1F1FD}-\\x{1F1FF}]|\\x{1F1F9}[\\x{1F1E6}\\x{1F1E8}\\x{1F1E9}\\x{1F1EB}-\\x{1F1ED}\\x{1F1EF}-\\x{1F1F4}\\x{1F1F7}\\x{1F1F9}\\x{1F1FB}\\x{1F1FC}\\x{1F1FF}]|\\x{1F1FA}[\\x{1F1E6}\\x{1F1EC}\\x{1F1F2}\\x{1F1F3}\\x{1F1F8}\\x{1F1FE}\\x{1F1FF}]|\\x{1F1FB}[\\x{1F1E6}\\x{1F1E8}\\x{1F1EA}\\x{1F1EC}\\x{1F1EE}\\x{1F1F3}\\x{1F1FA}]|\\x{1F1FC}[\\x{1F1EB}\\x{1F1F8}]|\\x{1F1FD}\\x{1F1F0}|\\x{1F1FE}[\\x{1F1EA}\\x{1F1F9}]|\\x{1F1FF}[\\x{1F1E6}\\x{1F1F2}\\x{1F1FC}]|\\x{1F3F3}\\x{FE0F}?(?:\\x{200D}(?:\\x{26A7}\\x{FE0F}?|\\x{1F308}))?|\\x{1F3F4}(?:\\x{200D}\\x{2620}\\x{FE0F}?|\\x{E0067}\\x{E0062}(?:\\x{E0065}\\x{E006E}\\x{E0067}|\\x{E0073}\\x{E0063}\\x{E0074}|\\x{E0077}\\x{E006C}\\x{E0073})\\x{E007F})?|\\x{1F408}(?:\\x{200D}\\x{2B1B})?|\\x{1F415}(?:\\x{200D}\\x{1F9BA})?|\\x{1F43B}(?:\\x{200D}\\x{2744}\\x{FE0F}?)?|\\x{1F441}\\x{FE0F}?(?:\\x{200D}\\x{1F5E8}\\x{FE0F}?)?|\\x{1F468}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F468}\\x{1F469}]\\x{200D}(?:\\x{1F466}(?:\\x{200D}\\x{1F466})?|\\x{1F467}(?:\\x{200D}[\\x{1F466}\\x{1F467}])?)|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F468}|\\x{1F466}(?:\\x{200D}\\x{1F466})?|\\x{1F467}(?:\\x{200D}[\\x{1F466}\\x{1F467}])?)|\\x{1F3FB}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F468}[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F468}[\\x{1F3FC}-\\x{1F3FF}]))?|\\x{1F3FC}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F468}[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F468}[\\x{1F3FB}\\x{1F3FD}-\\x{1F3FF}]))?|\\x{1F3FD}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F468}[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F468}[\\x{1F3FB}\\x{1F3FC}\\x{1F3FE}\\x{1F3FF}]))?|\\x{1F3FE}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F468}[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F468}[\\x{1F3FB}-\\x{1F3FD}\\x{1F3FF}]))?|\\x{1F3FF}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F468}[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F468}[\\x{1F3FB}-\\x{1F3FE}]))?)?|\\x{1F469}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?[\\x{1F468}\\x{1F469}]|\\x{1F466}(?:\\x{200D}\\x{1F466})?|\\x{1F467}(?:\\x{200D}[\\x{1F466}\\x{1F467}])?|\\x{1F469}\\x{200D}(?:\\x{1F466}(?:\\x{200D}\\x{1F466})?|\\x{1F467}(?:\\x{200D}[\\x{1F466}\\x{1F467}])?))|\\x{1F3FB}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:[\\x{1F468}\\x{1F469}]|\\x{1F48B}\\x{200D}[\\x{1F468}\\x{1F469}])[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}[\\x{1F468}\\x{1F469}][\\x{1F3FC}-\\x{1F3FF}]))?|\\x{1F3FC}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:[\\x{1F468}\\x{1F469}]|\\x{1F48B}\\x{200D}[\\x{1F468}\\x{1F469}])[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}[\\x{1F468}\\x{1F469}][\\x{1F3FB}\\x{1F3FD}-\\x{1F3FF}]))?|\\x{1F3FD}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:[\\x{1F468}\\x{1F469}]|\\x{1F48B}\\x{200D}[\\x{1F468}\\x{1F469}])[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}[\\x{1F468}\\x{1F469}][\\x{1F3FB}\\x{1F3FC}\\x{1F3FE}\\x{1F3FF}]))?|\\x{1F3FE}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:[\\x{1F468}\\x{1F469}]|\\x{1F48B}\\x{200D}[\\x{1F468}\\x{1F469}])[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}[\\x{1F468}\\x{1F469}][\\x{1F3FB}-\\x{1F3FD}\\x{1F3FF}]))?|\\x{1F3FF}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:[\\x{1F468}\\x{1F469}]|\\x{1F48B}\\x{200D}[\\x{1F468}\\x{1F469}])[\\x{1F3FB}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}[\\x{1F468}\\x{1F469}][\\x{1F3FB}-\\x{1F3FE}]))?)?|\\x{1F62E}(?:\\x{200D}\\x{1F4A8})?|\\x{1F635}(?:\\x{200D}\\x{1F4AB})?|\\x{1F636}(?:\\x{200D}\\x{1F32B}\\x{FE0F}?)?|\\x{1F93C}(?:[\\x{1F3FB}-\\x{1F3FF}]|\\x{200D}[\\x{2640}\\x{2642}]\\x{FE0F}?)?|\\x{1F9D1}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F384}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{1F91D}\\x{200D}\\x{1F9D1})|\\x{1F3FB}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F384}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F9D1}[\\x{1F3FC}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F9D1}[\\x{1F3FB}-\\x{1F3FF}]))?|\\x{1F3FC}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F384}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F9D1}[\\x{1F3FB}\\x{1F3FD}-\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F9D1}[\\x{1F3FB}-\\x{1F3FF}]))?|\\x{1F3FD}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F384}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F9D1}[\\x{1F3FB}\\x{1F3FC}\\x{1F3FE}\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F9D1}[\\x{1F3FB}-\\x{1F3FF}]))?|\\x{1F3FE}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F384}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F9D1}[\\x{1F3FB}-\\x{1F3FD}\\x{1F3FF}]|\\x{1F91D}\\x{200D}\\x{1F9D1}[\\x{1F3FB}-\\x{1F3FF}]))?|\\x{1F3FF}(?:\\x{200D}(?:[\\x{2695}\\x{2696}\\x{2708}]\\x{FE0F}?|[\\x{1F33E}\\x{1F373}\\x{1F37C}\\x{1F384}\\x{1F393}\\x{1F3A4}\\x{1F3A8}\\x{1F3EB}\\x{1F3ED}\\x{1F4BB}\\x{1F4BC}\\x{1F527}\\x{1F52C}\\x{1F680}\\x{1F692}\\x{1F9AF}-\\x{1F9B3}\\x{1F9BC}\\x{1F9BD}]|\\x{2764}\\x{FE0F}?\\x{200D}(?:\\x{1F48B}\\x{200D})?\\x{1F9D1}[\\x{1F3FB}-\\x{1F3FE}]|\\x{1F91D}\\x{200D}\\x{1F9D1}[\\x{1F3FB}-\\x{1F3FF}]))?)?|\\x{1FAF1}(?:\\x{1F3FB}(?:\\x{200D}\\x{1FAF2}[\\x{1F3FC}-\\x{1F3FF}])?|\\x{1F3FC}(?:\\x{200D}\\x{1FAF2}[\\x{1F3FB}\\x{1F3FD}-\\x{1F3FF}])?|\\x{1F3FD}(?:\\x{200D}\\x{1FAF2}[\\x{1F3FB}\\x{1F3FC}\\x{1F3FE}\\x{1F3FF}])?|\\x{1F3FE}(?:\\x{200D}\\x{1FAF2}[\\x{1F3FB}-\\x{1F3FD}\\x{1F3FF}])?|\\x{1F3FF}(?:\\x{200D}\\x{1FAF2}[\\x{1F3FB}-\\x{1F3FE}])?)?)");

	private final EmojiFilter emojiFilter = new EmojiFilter(this);
	private final EmojiPostFilter emojiPostFilter = new EmojiPostFilter(this);

	private Map<String, Emoji> emoji; //All emoji mapped by all usable names
	private Map<String, Emoji> emojiByCharacter; //All emoji mapped by character
	private Map<String, Emoji> searchableDefaultEmoji; //All default unicode emoji considered searchable, mapped by all usable names
	private Map<String, CustomEmoji> customEmoji; //All defined custom emoji, mapped by all usable names

	private Component emojiList;
	private EmojiCommand emojiCommand;

	@Override
	public String getName() {
		return "Emoji";
	}

	@Override
	public void onEnable() {
		emoji = new HashMap<>();
		emojiByCharacter = new HashMap<>();

		searchableDefaultEmoji = new LinkedHashMap<>();
		customEmoji = new LinkedHashMap<>();

		parseEmojiMap();
		parseConfig();

		emojiCommand = new EmojiCommand(this);

		FilterManager.addPreParseFilter(getName(), emojiFilter);
		FilterManager.addPostParseFilter(getName(), emojiPostFilter);

		emojiCommand.register();
	}

	@Override
	public void onDisable() {
		emoji.clear();
		emojiByCharacter.clear();

		searchableDefaultEmoji.clear();
		customEmoji.clear();

		emojiCommand.unregister();
		emojiList = null;

		FilterManager.removePreParseFilter(getName());
		FilterManager.removePostParseFilter(getName());
	}

	/**
	 * Parse the custom emojis defined in the ProxyChat config
	 */
	private void parseConfig() {
		Map<String, Map<String, List<String>>> categories = (Map<String, Map<String, List<String>>>) getModuleSection()
				.getAnyRef("custom-emoji");

		StringBuilder characterRegex = new StringBuilder("[");
		Map<String, List<CustomEmoji>> customEmojiByCategory = new LinkedHashMap<>();

		categories.forEach((String category, Map<String, List<String>> emoji) -> {
			ArrayList<CustomEmoji> categoryEmojis = new ArrayList<>();

			emoji.forEach((String character, List<String> names) -> {
				characterRegex.append(character);
				CustomEmoji e = new CustomEmoji(character, names, category);

				names.forEach(name -> {
					this.emoji.put(name.toLowerCase(), e);
					this.customEmoji.put(name.toLowerCase(), e);
				});
				emojiByCharacter.put(character, e);
				categoryEmojis.add(e);
			});

			customEmojiByCategory.put(category, categoryEmojis);
		});

		characterRegex.append("]");

		if (characterRegex.length() > 2) {
			customCharacterPattern = Pattern.compile(characterRegex.toString());
		}

		buildEmojiListComponent(customEmojiByCategory);
	}

	/**
	 * Parse default emoji from discordEmojiMap.json
	 */
	private void parseEmojiMap() {
		Type emojiListType = new TypeToken<List<Emoji>>() {}.getType();
		final Gson gson = new GsonBuilder().registerTypeAdapter(emojiListType, new EmojiDeserializer()).create();
		List<Emoji> result;

		try (InputStream stream = Objects.requireNonNull(
				ProxyChat.getInstance().getClass().getClassLoader().getResourceAsStream("discordEmojiMap.json"))) {
			result = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), emojiListType);
		} catch (IOException | NullPointerException e) {
			LoggerHelper.warning("Failed to load emoji map. Standard emoji will not be available.");
			return;
		}

		result.forEach(e -> {
			e.getNames().forEach(n -> emoji.put(n, e));
			emojiByCharacter.put(e.getCharacter(), e);

			if(e.isSearchable()) {
				e.getNames().forEach(n -> searchableDefaultEmoji.put(n, e));
			}
		});
	}

	private void buildEmojiListComponent(Map<String, List<CustomEmoji>> customEmojiByCategory) {
		TextComponent.Builder list = Component.text().append(
						Component.text().content("Available Emoji")
								.color(NamedTextColor.YELLOW)
								.decoration(TextDecoration.UNDERLINED, TextDecoration.State.TRUE)
								.decoration(TextDecoration.BOLD, TextDecoration.State.TRUE))
				.append(Component.newline());

		customEmojiByCategory.forEach((String category, List<CustomEmoji> emojis) -> {
			if (emojis.isEmpty()) {
				return;
			}

			list.append(Component.text().content(category).color(NamedTextColor.BLUE));
			list.append(Component.newline());
			list.append(Component.space());

			emojis.forEach(emoji -> list.append(emoji.getComponent()).append(Component.space()));
			list.append(Component.newline());
		});

		emojiList = list.build();
	}

	public List<String> getEmojiSuggestions(SimpleCommand.Invocation invocation) {
		String lastWord = invocation.arguments()[invocation.arguments().length - 1].toLowerCase();
		Matcher matcher = incompleteEmojiPattern.matcher(lastWord);

		if (!matcher.find()) {
			return Collections.emptyList();
		}

		String prefix = matcher.group(1);

		if(matcher.group(2).length() < 2) {
			return Collections.emptyList();
		}

		return searchEmoji(matcher.group(2))
				.stream()
				.map(emoji -> prefix + emoji.getPrimaryNameWithColons())
				.collect(Collectors.toList());
	}

	public List<Emoji> searchEmoji(String search) {
		int limit = 10;

		List<Emoji> results = customEmoji.entrySet().stream()
				.filter(e -> e.getKey().contains(search))
				.map(Map.Entry::getValue).distinct().limit(limit)
				.collect(Collectors.toList());

		limit -= results.size();

		if(limit > 0) {
			results.addAll(searchableDefaultEmoji.entrySet().stream()
								   .filter(e -> e.getKey().contains(search))
								   .map(Map.Entry::getValue).distinct().limit(limit)
								   .collect(Collectors.toList()));
		}

		return results;
	}

	public Component getEmojiListComponent() {
		return emojiList;
	}

	public Pattern getEmojiPattern() {
		return emojiPattern;
	}

	/**
	 * Returns the pattern used for matching default emoji
	 * @return The pattern
	 */
	public Pattern getDefaultCharacterPattern() {
		return defaultCharacterPattern;
	}

	/**
	 * Returns the pattern used for matching defined custom emoji
	 * @return The pattern
	 */
	public Pattern getCustomCharacterPattern() {
		return customCharacterPattern;
	}

	public Optional<Emoji> getEmoji(String name) {
		return Optional.ofNullable(emoji.get(name));
	}

	public Optional<Emoji> getEmojiByCharacter(String character) {
		return Optional.ofNullable(emojiByCharacter.get(character));
	}

	public EmojiFilter getEmojiFilter() {
		return emojiFilter;
	}

	public EmojiPostFilter getEmojiPostFilter() {
		return emojiPostFilter;
	}

	public static class EmojiDeserializer implements JsonDeserializer<List<Emoji>> {

		@Override
		public List<Emoji> deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
			final Gson gson = new Gson();
			final JsonObject obj = je.getAsJsonObject();
			final JsonElement emojiElement = obj.get("emojiDefinitions");
			final JsonArray emojiArray = emojiElement.getAsJsonArray();
			final Pattern tonePattern = Pattern.compile(".*_tone\\d$");

			final List<Emoji> emoji = new ArrayList<>();

			for (Object object : emojiArray) {
				final JsonObject jsonObject = (JsonObject) object;
				String character = jsonObject.get("surrogates").getAsString();
				String primaryName = jsonObject.get("primaryName").getAsString();

				List<String> names = gson.fromJson(jsonObject.getAsJsonArray("names"),
												   new TypeToken<List<String>>() {}.getType());

				emoji.add(new Emoji(character, names, !tonePattern.matcher(primaryName).matches())); //Skin tones aren't searchable to reduce clutter
			}

			return emoji;
		}
	}

	public java.lang.String pattern() {
		return this.incompleteEmojiPattern.pattern();
	}

	public java.util.regex.Matcher matcher(final java.lang.CharSequence input) {
		return this.incompleteEmojiPattern.matcher(input);
	}

	public int flags() {
		return this.incompleteEmojiPattern.flags();
	}

	public java.lang.String[] split(final java.lang.CharSequence input, final int limit) {
		return this.incompleteEmojiPattern.split(input, limit);
	}

	public java.lang.String[] split(final java.lang.CharSequence input) {
		return this.incompleteEmojiPattern.split(input);
	}

	public java.util.function.Predicate<java.lang.String> asPredicate() {
		return this.incompleteEmojiPattern.asPredicate();
	}

	public java.util.function.Predicate<java.lang.String> asMatchPredicate() {
		return this.incompleteEmojiPattern.asMatchPredicate();
	}

	public java.util.stream.Stream<java.lang.String> splitAsStream(final java.lang.CharSequence input) {
		return this.incompleteEmojiPattern.splitAsStream(input);
	}
}
