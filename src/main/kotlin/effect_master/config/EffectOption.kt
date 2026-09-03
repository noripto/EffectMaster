package effect_master.config

import net.minecraft.network.chat.Component

/**
 * 1 つのステータス効果に対して選べる振る舞い。
 *
 * この設定はワールド（サーバー）単位で共有される。変更できるのは owner だけで、
 * 判定は [effect_master.EffectMaster.OWNER_PERMISSION_LEVEL] による。
 */
enum class EffectOption(translationKey: String) {
    /** 効果の付与を拒否し、既に付いていれば取り除く。 */
    DISABLED("effect_master.option.disabled"),

    /** バニラどおり。MOD は何もしない。 */
    DEFAULT("effect_master.option.default"),

    /** 全プレイヤーに常時付与し続ける。 */
    PERSISTENT("effect_master.option.persistent");

    val displayName: Component = Component.translatable(translationKey)

    companion object {
        /** 保存済みの文字列から復元する。未知の値は null。 */
        fun byName(name: String): EffectOption? = entries.firstOrNull { it.name == name }
    }
}
