package io.wispforest.jello.api.dye.registry;

import io.wispforest.jello.api.dye.DyeColorant;
import io.wispforest.jello.main.common.Jello;
import io.wispforest.jello.api.util.ColorUtil;
import io.wispforest.jello.api.util.MessageUtil;
import io.wispforest.jello.main.common.data.tags.JelloTags;
import com.google.gson.*;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.block.MapColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryEntry;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.ApiStatus;

import java.io.InputStreamReader;
import java.util.*;

public class DyeColorantJsonTest {

    private static final Gson BIG_GSON = new GsonBuilder().setPrettyPrinting().create();

    public static DyeColorant getRandomlyRegisteredDyeColor(){
        boolean nonVanillaDyeColor = false;
        RegistryEntry<DyeColorant> dyeColor = DyeColorantRegistry.DYE_COLOR.getRandom(new Random()).get();

        while(!nonVanillaDyeColor){
            if(!dyeColor.isIn(JelloTags.DyeColor.VANILLA_DYES)){
                nonVanillaDyeColor = true;
            }else{
                dyeColor = DyeColorantRegistry.DYE_COLOR.getRandom(new Random()).get();
            }
        }

        return dyeColor.value();
    }

    @ApiStatus.Internal public static final OwoItemSettings BASE_BLOCK_ITEM_SETTINGS = new OwoItemSettings().group(ItemGroup.MISC).tab(2);
    @ApiStatus.Internal public static final OwoItemSettings BASE_ITEM_SETTINGS = new OwoItemSettings().group(ItemGroup.MISC).tab(3);

    public static void gatherDyesFromJson(){
        MessageUtil messager = new MessageUtil("JsonToRegistry");

        try {
            JsonArray names = JsonHelper.getArray(BIG_GSON.fromJson(new InputStreamReader(DyeColorantRegistry.class.getClassLoader().getResourceAsStream("assets/jello/other/colorDatabase.json")), JsonObject.class), "colors");

            for (var i = 0; i < names.size(); i++) {
                JsonObject currentObject = names.get(i).getAsJsonObject();

                Identifier colorIdentifier = new Identifier(Jello.MODID, currentObject.get("identifierSafeName").getAsString());
                int colorValue = Integer.parseInt(currentObject.get("hexValue").getAsString(), 16);

                if(DyeColorantRegistry.DYE_COLOR.containsId(colorIdentifier)){
                    //continue;
                    colorIdentifier = new Identifier(Jello.MODID, currentObject.get("identifierSafeName").getAsString() + "_2");
                }

                if(DyeColorantRegistry.DYE_COLOR.containsId(new Identifier(currentObject.get("identifierSafeName").getAsString()))){
                    continue;
                }

                DyeColorant currentDyeColor = DyeColorantRegistry.registryDyeColor(colorIdentifier, MapColor.CLEAR, colorValue);
                DyeColorantRegistry.createDyedVariants(currentDyeColor, BASE_ITEM_SETTINGS, BASE_BLOCK_ITEM_SETTINGS, false);
            }

            DyeColorantRegistry.registerModidModelRedirect(Jello.MODID);

            messager.stopTimerPrint("It seems that the registry filling took ");
            messager.infoMessage("Total amount of registered dyes from json are " + DyeColorantRegistry.DYE_COLOR.size());
        }catch (JsonSyntaxException | JsonIOException e) {
            messager.failMessage("Something has gone with the json to Dye Registry method!");
            e.printStackTrace();
        }
    }

    //TODO: Keep??
//    public static void generateJsonFile() {
//        try {
//            var colorDataBaseFile = DyeColorRegistry.class.getClassLoader().getResourceAsStream("assets/jello/other/colorNames.json");
//
//            InputStreamReader inputFile = new InputStreamReader(colorDataBaseFile);
//
//            JsonObject infoFromJson = BIG_GSON.fromJson(inputFile, JsonObject.class);
//
//            JsonHelper.getArray(infoFromJson, "colors").forEach(jsonElement -> {
//                ((JsonObject)jsonElement).remove("r");
//                ((JsonObject)jsonElement).remove("g");
//                ((JsonObject)jsonElement).remove("b");
//                ((JsonObject)jsonElement).remove("h");
//                ((JsonObject)jsonElement).remove("s");
//                ((JsonObject)jsonElement).remove("l");
//
//                String hexValue = ((JsonObject)jsonElement).get("hex").getAsString();
//                String colorName = ((JsonObject)jsonElement).get("name").getAsString();
//
//                ((JsonObject)jsonElement).remove("hex");
//                ((JsonObject)jsonElement).remove("name");
//
//                ((JsonObject)jsonElement).addProperty("hexValue", hexValue);
//                ((JsonObject)jsonElement).addProperty("colorName", colorName);
//
//                ((JsonObject)jsonElement).addProperty("identifierSafeName", colorName.toLowerCase(Locale.ROOT).replace("\s", "_").replaceAll("[^a-z0-9\\/\\._\\-]", ""));
//            });
//
//            FileWriter saveFile = new FileWriter(FabricLoader.getInstance().getConfigDir().resolve("colorDatabase.json").toFile());
//
//            BIG_GSON.toJson(infoFromJson, saveFile);
//
//            saveFile.close();
//
//        } catch (JsonSyntaxException | JsonIOException | IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static MutablePair<Triple<String, String, String>, Boolean> ERROR_RETURN_VALUE(String color){
        return new MutablePair<>(new MutableTriple<>("000000", "Invalid Color: " + color, "_null"), false);
    }

    /**
     *  Note: Code was based off of/used from <a href="https://chir.ag/projects/ntc/ntc.js">ntc.js</a>, created by Chirag Mehta,
     *  under the <a href="http://creativecommons.org/licenses/by/2.5/">Creative Commons Licences</a>
     */
    private static Pair<Triple<String, String, String>, Boolean> getNearestValue(String color){
        color = color.toUpperCase();
        if(color.length() < 3 || color.length() > 7)
            return ERROR_RETURN_VALUE(color);
        if(color.length() % 3 == 0)
            color = "#" + color;
        if(color.length() == 4)
            color = "#" + color.substring(1, 1) + color.substring(1, 1) + color.substring(2, 1) + color.substring(2, 1) + color.substring(3, 1) + color.substring(3, 1);

        int hexColor = Integer.parseInt(color.replace("#", ""), 16);

        var rgb = new int[]{hexColor >> 16, (hexColor >> 8) & 0xFF, hexColor & 0xFF};
        var r = rgb[0];
        var g = rgb[1];
        var b = rgb[2];

        var hsl = ColorUtil.getHSLfromColor(hexColor);
        var h = hsl[0];
        var s = hsl[1];
        var l = hsl[2];

        float ndf1;
        float ndf2;
        float ndf;
        int cl = -1;
        float df = -1;

        try {
            var colorDataBaseFile = DyeColorantRegistry.class.getClassLoader().getResourceAsStream("assets/jello/other/colorDatabase.json");

            JsonArray names = JsonHelper.getArray(BIG_GSON.fromJson(new InputStreamReader(colorDataBaseFile), JsonObject.class), "colors");

            for (var i = 0; i < names.size(); i++) {
                JsonObject currentObject = names.get(i).getAsJsonObject();

                int closeHexColor = Integer.parseInt(currentObject.get("hexValue").getAsString(), 16);

                int[] rgbColorArray = new int[]{closeHexColor >> 16, (closeHexColor >> 8) & 0xFF, closeHexColor & 0xFF};

                float[] hslColorArray = ColorUtil.getHSLfromColor(closeHexColor);


                if (color == "#" + names.get(i).getAsJsonObject().get("hexValue")) //ntc.names[i][0])
                    return new MutablePair<>(new MutableTriple<>(currentObject.get("hexValue").getAsString(), currentObject.get("colorName").getAsString(), currentObject.get("identifierSafeName").getAsString()), true);//ntc.names[i][0], ntc.names[i][1], true);

                ndf1 = MathHelper.square(r - rgbColorArray[0]) + MathHelper.square(g - rgbColorArray[1]) +  MathHelper.square(b - rgbColorArray[2]);//ntc.names[i][2], 2) + Math.pow(g - ntc.names[i][3], 2) + Math.pow(b - ntc.names[i][4], 2);
                ndf2 = MathHelper.square(h -  hslColorArray[0]) + MathHelper.square(s - hslColorArray[1]) + MathHelper.square(l - hslColorArray[2]);//ntc.names[i][5], 2) + Math.pow(s - ntc.names[i][6], 2) + Math.pow(l - ntc.names[i][7], 2);
                ndf = ndf1 + ndf2 * 2;
                if (df < 0 || df > ndf) {
                    df = ndf;
                    cl = i;
                }
            }

            JsonObject finalColorObject = names.get(cl).getAsJsonObject();

            return (cl < 0 ? ERROR_RETURN_VALUE(color) : new MutablePair<>(new MutableTriple<>(finalColorObject.get("hexValue").getAsString(), finalColorObject.get("colorName").getAsString(), finalColorObject.get("identifierSafeName").getAsString()), false));//ntc.names[cl][0], ntc.names[cl][1], false));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (JsonIOException e) {
            e.printStackTrace();
        }

        return ERROR_RETURN_VALUE(color);
    }
}