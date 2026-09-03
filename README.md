# Effect Master

Minecraft 1.20.1 / Forge 向けの MOD。ステータス効果ごとに振る舞いを選べます。
他の MOD が追加した効果も対象です。

| 設定 | 動作 |
| --- | --- |
| 無効 (Disabled) | 効果の付与を拒否し、既に付いていれば取り除く |
| デフォルト (Default) | バニラどおり |
| 永続 (Persistent) | 全プレイヤーに常時付与する |

## 使い方

`/effectconfig` で設定画面が開きます。

設定はワールド単位で共有され、変更できるのは **owner** だけです。

| 環境 | 変更できる人 |
| --- | --- |
| シングルプレイ | ワールドのホスト（チートの許可設定に関係なく常に可） |
| LAN 公開 | ホスト、および op 済みのプレイヤー |
| 専用サーバー | op 済みのプレイヤー（権限レベル 2 以上） |

owner 以外のプレイヤーも画面は開けますが、閲覧専用になります。

保存先は `config/effect_master_effects.json` です。バニラから変えた効果だけが書き出されます。

```json
{
  "minecraft:night_vision": "PERSISTENT",
  "minecraft:poison": "DISABLED"
}
```

## 必要な MOD

- Minecraft 1.20.1 / Forge 47.2.0 以降
- [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge) 4.11.0 以降
- [Cloth Config](https://github.com/shedaniel/cloth-config) 11.1.106 以降

## ビルド

```
./gradlew build
```

`build/libs/effect_master-<version>.jar` が生成されます。
