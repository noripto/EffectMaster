package effect_master.client

import effect_master.config.EffectOption
import effect_master.network.EffectMasterNetwork
import effect_master.network.UpdateEffectSettingsPacket
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.registries.ForgeRegistries

/**
 * Cloth Config で組み立てる設定画面。
 *
 * 状態は持たない。サーバーから届いたスナップショットを表示し、閉じるときに
 * 変わった分だけ送り返す。
 */
@OnlyIn(Dist.CLIENT)
object EffectConfigScreen {
    fun open(settings: Map<ResourceLocation, EffectOption>, editable: Boolean) {
        val minecraft = Minecraft.getInstance()
        minecraft.setScreen(build(minecraft.screen, settings, editable))
    }

    private fun build(
        parent: Screen?,
        settings: Map<ResourceLocation, EffectOption>,
        editable: Boolean,
    ): Screen {
        // 触った項目だけ貯めて、保存時にまとめて送る。
        val changes = mutableMapOf<ResourceLocation, EffectOption>()

        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("effect_master.config.title"))
            .setEditable(editable)
            .setSavingRunnable {
                if (changes.isNotEmpty()) {
                    EffectMasterNetwork.sendToServer(UpdateEffectSettingsPacket(changes.toMap()))
                    changes.clear()
                }
            }

        val category = builder.getOrCreateCategory(Component.translatable("effect_master.config.category.effects"))
        val entryBuilder = builder.entryBuilder()

        // レジストリ順は登録順で読みづらいので、表示名で並べる。
        ForgeRegistries.MOB_EFFECTS.entries
            .sortedBy { it.value.displayName.string }
            .forEach { (key, effect) ->
                val id = key.location()
                val current = settings[id] ?: EffectOption.DEFAULT

                category.addEntry(
                    entryBuilder.startEnumSelector(effect.displayName, EffectOption::class.java, current)
                        .setDefaultValue(EffectOption.DEFAULT)
                        .setEnumNameProvider { option -> (option as EffectOption).displayName }
                        .setTooltip(Component.literal(id.toString()))
                        .setSaveConsumer { selected ->
                            if (selected == current) changes.remove(id) else changes[id] = selected
                        }
                        .build()
                )
            }

        return builder.build()
    }
}
