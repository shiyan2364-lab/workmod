# Workmod 全系统实装版（2025 交接版）

## 一句话总结

对照内容说明书，**八大系统全部实装**，且按你的要求：无任务、无 NPC 实体（NPC 用书+命令线上交易）。

## 系统清单

| 系统 | 状态 | 说明 |
|---|---|---|
| 1 宏伟建筑 | ✅ | 10 结构注册 + 定位 + 入口圆形广场 |
| 2 道路 | ✅ | 自动铺路 + 断路 + **火把/路碑/车辙/圆形广场**装饰 |
| 3 断路 | ✅ | 悬崖/篝火桥/藤蔓/遗迹 + 挖断路面 |
| 4 峡谷高山 | ✅ | V 形峡谷（谷底岩浆）+ 登山道（山脚石碑"无人知晓/无人来过/无人离开"、山顶 20% 宝箱） |
| 5 极致成就 | ✅ 8/8 | 苍穹/深渊/沧浪/烬土/星辉/时痕/岩魂/生机，进度**持久化存档** |
| 6 诅咒饰品 | ✅ | 8 件物品化，强度自动跟随成就；**击杀掉落 + 山顶宝箱 40% + 裂隙奖励箱 50%** |
| 7 实时难度 | ✅ | 双能量池 + 死亡惩罚（原逻辑接入 tick） |
| 8 三界裂隙 | ✅ | 道路尽头自动门 + 传送 + **裂隙奖励箱** |
| 云层 | ✅ | 原版云 128 不动 + 模组 300/480/700 多层高空云 |
| NPC | ✅（无实体） | **《旅者商册》书 + `/workmod:market` / `/workmod:buy` 线上交易**（觉醒等级限制、绿宝石支付、声望） |
| 小地图 | ✅ | 右上角 HUD（方块颜色 + 结构绿点 + 玩家白点 + 北向） |
| 任务 | ❌ | 按你要求不做 |

## 命令速查

- `/workmod:help` / `:tp` / `:anguish` / `:debug` / `:dimension`（原命令）
- `/workmod:genroad` —— 面前 60 格测试路
- `/workmod:market` —— 列出 8 族 NPC；`/workmod:market <id>` 看单族
- `/workmod:buy <npc> <trade>` —— 绿宝石购买（觉醒等级不足会提示）
- `/workmod:curse list` —— 8 件饰品当前强度与模式
- `/workmod:curse set <id> <强度>` —— 手动调节强度（未反转只能 -100~0；已反转 -100~900）
- `/workmod:curse reset <id>` / `resetall` —— 恢复默认（重新跟随极致进度）
- `/workmod:ring list` —— 随身饰品栏内容
- `/workmod:ring put <槽>` / `take <槽>` —— 放入/取出诅咒饰品
- `/workmod:ring upgrade` —— 主戒升级（耗 1 级经验，槽数 2 → 8）

## 说明书细节补全（本版）

| 细节 | 实现 |
|---|---|
| 6️⃣ 饰品强度调节 | **集成在饰品上**：右键饰品循环档位（未反转只能负档 -100/-50/0，反转后 +100/+900），潜行+右键恢复自动；`/workmod:curse` 命令保留（list/set/reset） |
| 6️⃣ 饰品栏 | 不自研（原版无饰品栏）；**软兼容 Trinkets**（`TrinketCompat` 反射接入，装 Trinkets 即 8 件可入栏，没装不影响） |
| 3️⃣ 玩家修桥永久保留 | 手持方块物品右键断路点 → 消耗 1 个、恢复 3×3 路面、写 `workmod_breaks` 存档 |
| 2️⃣ 路边废墟装饰 | 铺路时每 48 点路边放碎岩（圆石/苔藓圆石/安山岩/沙砾） |
| 4️⃣ 峡谷谷底虚空 | `bottomType=1` 的峡谷谷底挖空成虚空（其余铺岩浆） |

## 本轮新增文件

| 文件 | 内容 |
|---|---|
| `item/TradeTable.java` | 🆕 8 族 NPC ×3 交易数据 + 物品映射（线上交易） |
| `item/TradeBook.java` | 🆕 《旅者商册》written book（出生发放，逐族列交易） |
| `command/TradeCommand.java` | 🆕 `/workmod:market` + `/workmod:buy`（觉醒限制 + 绿宝石 + 声望+1） |
| `world/dimension/RiftRewardGenerator.java` | 🆕 裂隙门旁奖励箱：面包×4+金苹果+50% 诅咒饰品 |
| `client/MinimapHud.java` | 🆕 右上角小地图 HUD |
| `world/road/RoadNetwork.java` | 🔄 路碑（石头+告示牌"古道/旅者之路…"）+ 车辙（中心灰化土条纹） |
| `world/road/RoadGenerator.java` | 🔄 结构入口圆形广场 + 小地图标记 addMarker |
| `world/mountains/MountainAutoGenerator.java` | 🔄 山顶宝箱 40% 追加诅咒饰品 |

## 安装（手机 Termux）

```bash
cd /storage/emulated/0/临时中转站/ds/workmod
bash <workmod-src路径>/install_phase1.sh
git push origin master
```

## 进游戏验证

1. 出生得《冒险者手册》+《旅者商册》；右上角出现小地图；
2. `/workmod:genroad` 面前出测试路；`/workmod:market` 看商会；
3. 跑图 → 铺路（火把/路碑/车辙）、守卫、裂隙、峡谷、登山道；
4. 找到裂隙门 → 旁有奖励箱（可能含诅咒饰品）；
5. 登顶/探谷/挖矿/种树 → 成就推进，诅咒强度变化；
6. 打守卫掉诅咒饰品；`/workmod:buy 0 0` 试试第一笔交易（觉醒不足会提示）。

## 已知限制（诚实版）

- 小地图每帧采样 64×64 方块，低端手机若卡可删 `MinimapHud.register()` 那行
- 高空云海是"云海平面"观感，不是块状云
- 交易为"线上"形式（书+命令），无实体村民、无交易 GUI
- 任务系统按你要求未做

## 开发全流程记录

- 阶段1 铺路 → 阶段1b 手册 → 阶段2 断路 → 阶段3 补给宝藏 → 阶段4 守卫 → 阶段5 裂隙 → 阶段6 峡谷高山 → 云层优化 → 诅咒饰品 → 极致成就(8/8+存档) → 裂隙/宝箱奖励 → 道路装饰 → 线上NPC交易 → 小地图
