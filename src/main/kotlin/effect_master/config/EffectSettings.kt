package effect_master.config

import effect_master.EffectMaster
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.registries.ForgeRegistries
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * サーバー側が持つ唯一の設定。クライアントはこれを直接触らず、
 * 画面を開くときに送られてくるスナップショット（[effect_master.client.ClientEffectSettings]）を見る。
 *
 * [EffectOption.DEFAULT] の効果はマップに載せない。「載っていない = DEFAULT」で統一する。
 */
object EffectSettings {
    private const val FILE_NAME = "effect_master_effects.json"

    private val overrides = ConcurrentHashMap<ResourceLocation, EffectOption>()

    private val path: Path
        get() = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME)

    fun load() {
        overrides.clear()
        overrides.putAll(EffectSettingsFile.load(path))
        EffectMaster.LOGGER.info("Loaded {} effect override(s)", overrides.size)
    }

    fun save() {
        EffectSettingsFile.save(path, overrides.toMap())
    }

    operator fun get(id: ResourceLocation): EffectOption = overrides[id] ?: EffectOption.DEFAULT

    operator fun get(effect: MobEffect): EffectOption {
        val id = ForgeRegistries.MOB_EFFECTS.getKey(effect) ?: return EffectOption.DEFAULT
        return get(id)
    }

    /** 値が変わったときだけ true。呼び出し側はこれを見て、変更時の後始末をするか決める。 */
    fun set(id: ResourceLocation, option: EffectOption): Boolean {
        val previous = get(id)
        if (previous == option) return false
        if (option == EffectOption.DEFAULT) overrides.remove(id) else overrides[id] = option
        return true
    }

    /** 画面へ送る用のコピー。 */
    fun snapshot(): Map<ResourceLocation, EffectOption> = overrides.toMap()

    /** 何も指定されていなければ true。tick 処理を丸ごと飛ばすための早期判定に使う。 */
    fun isEmpty(): Boolean = overrides.isEmpty()

    /** 常時付与すべき効果。レジストリから消えた id は無視する。 */
    fun persistentEffects(): List<MobEffect> = overrides.entries
        .filter { it.value == EffectOption.PERSISTENT }
        .mapNotNull { ForgeRegistries.MOB_EFFECTS.getValue(it.key) }

    fun isDisabled(effect: MobEffect): Boolean = get(effect) == EffectOption.DISABLED
}
