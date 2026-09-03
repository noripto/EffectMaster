package effect_master.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import effect_master.EffectMaster
import net.minecraft.resources.ResourceLocation
import java.nio.file.Files
import java.nio.file.Path

/**
 * 設定の JSON 入出力。`config/effect_master_effects.json` に
 * `{"minecraft:speed": "PERSISTENT"}` の形で保存する。
 *
 * [EffectOption.DEFAULT] は書き出さない。何も指定していない状態と同じ意味なので、
 * ファイルには「バニラから変えた効果」だけが残る。
 */
internal object EffectSettingsFile {
    private val GSON = GsonBuilder().setPrettyPrinting().create()

    /** 壊れた行は読み飛ばす。読めなければ空の設定を返す。 */
    fun load(path: Path): Map<ResourceLocation, EffectOption> {
        if (!Files.exists(path)) {
            EffectMaster.LOGGER.debug("No settings file at {}, starting from defaults", path)
            return emptyMap()
        }

        val root = try {
            Files.newBufferedReader(path).use { JsonParser.parseReader(it) }
        } catch (e: Exception) {
            EffectMaster.LOGGER.error("Failed to read {}, falling back to defaults", path, e)
            return emptyMap()
        }

        if (!root.isJsonObject) {
            EffectMaster.LOGGER.error("{} is not a JSON object, falling back to defaults", path)
            return emptyMap()
        }

        val settings = mutableMapOf<ResourceLocation, EffectOption>()
        for ((key, value) in root.asJsonObject.entrySet()) {
            val id = ResourceLocation.tryParse(key)
            if (id == null) {
                EffectMaster.LOGGER.warn("Skipping malformed effect id '{}' in {}", key, path)
                continue
            }
            val option = value.takeIf { it.isJsonPrimitive }?.asString?.let(EffectOption::byName)
            if (option == null) {
                EffectMaster.LOGGER.warn("Skipping unknown option '{}' for {} in {}", value, key, path)
                continue
            }
            if (option != EffectOption.DEFAULT) settings[id] = option
        }
        return settings
    }

    fun save(path: Path, settings: Map<ResourceLocation, EffectOption>) {
        val json = JsonObject()
        settings.entries
            .filter { it.value != EffectOption.DEFAULT }
            .sortedBy { it.key.toString() }
            .forEach { (id, option) -> json.addProperty(id.toString(), option.name) }

        try {
            Files.createDirectories(path.parent)
            Files.newBufferedWriter(path).use { GSON.toJson(json, it) }
        } catch (e: Exception) {
            EffectMaster.LOGGER.error("Failed to write {}", path, e)
        }
    }
}
