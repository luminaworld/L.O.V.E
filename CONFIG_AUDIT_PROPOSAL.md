# แผนการปรับปรุงระบบการตั้งค่า (Configuration System Audit & Proposal)
**โปรเจกต์:** Luminaris: Operation Vanilla Extraction Core Engine (L.O.V.E)  
**วันที่สำรวจ:** 27 ตุลาคม 2026  
**สถานะ:** ดำเนินการปรับปรุงระบบคอนฟิกและเชื่อมต่อโค้ดเสร็จสมบูรณ์เรียบร้อย (Completed & Fully Verified)

---

## 1. ภาพรวมการตรวจสอบ (Executive Summary)

จากการตรวจสอบซอร์สโค้ดภาษา Kotlin และไฟล์คอนฟิก YAML ทั้งหมดของปลั๊กอิน `LuminaExtractionCore` พบว่ามีค่าคงที่ (Hardcoded Values), ตรรกะของระบบ (System Logic), การเล่นเสียง (Sound FX), อนุภาคเอฟเฟกต์ (Particles), กฎของโลกแมตช์ (Gamerules), ตำแหน่งล็อบบี้ (Lobby Location) และการตั้งค่าระบบความปลอดภัย จำนวนมากที่ยังคงฝังอยู่ในโค้ด Java/Kotlin โดยตรง และบางส่วนมีระบุไว้ใน `config.yml` แล้วแต่ระบบยังไม่ได้ดึงไปใช้งานจริง

เอกสารนี้รวบรวมรายการทั้งหมดที่สามารถและควรปรับให้ตั้งค่าผ่านไฟล์คอนฟิก (`config.yml` และส่วนที่เกี่ยวข้อง) ได้อย่างสมบูรณ์แบบ 100% พร้อมตัวอย่างโครงสร้างไฟล์ `config.yml` รูปแบบใหม่

---

## 2. รายการ Hard code ทั้งหมดที่ตรวจพบ และข้อเสนอแนะในการปรับปรุง

### หมวดที่ 1: ระบบแมตช์สกัดและการทำงานของรอบการเล่น (Match & Ephemeral Instance)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `MatchManager.kt` (L57), `MatchInstance.kt` (L38) | ระยะเวลาแมตช์เริ่มต้นคงที่ `1200L` (20 นาที) | เพิ่ม `match.default_duration_seconds: 1200` ให้แอดมินตั้งเวลาแต่ละรอบได้อิสระ |
| `MatchInstance.kt` (L69-74) | สีของ BossBar ถูกฟิกเป็น `BossBar.Color.RED` และ Overlay เป็น `PROGRESS` | เพิ่ม `match.bossbar.enabled: true`, `match.bossbar.color: "RED"`, `match.bossbar.overlay: "PROGRESS"` |
| `MatchInstance.kt` (L159) | ดาเมจกำจัดผู้เล่นหมดเวลา (M.I.A.) ฟิกไว้ที่ `100000.0` | เพิ่ม `match.timeout.damage_amount: 100000.0` |
| `MatchInstance.kt` (L116) | ระบบปิดแมตช์อัตโนมัติทันทีเมื่อผู้เล่นทุกคนออกจากรอบ/ตายหมด | เพิ่ม `match.early_teardown_when_all_terminal: true` ให้เลือกได้ว่าจะปิดทันทีหรือรอจนหมดเวลา |
| `InstanceWorldService.kt` (L63-73) | โฟลเดอร์เก็บเทมเพลตและอินสแตนซ์ฟิกที่ `"templates_worlds"` และ `"instances"` | เพิ่ม `instance_world.templates_folder: "templates_worlds"` และ `instance_world.instances_folder: "instances"` |
| `InstanceWorldService.kt` (L142-146) | กฎ GameRule ในโลกแมตช์ถูกฟิกตายตัว: `keepInventory=false`, `doDaylightCycle=false`, `doMobSpawning=false` | เพิ่มกลุ่ม `instance_world.gamerules` ให้ปรับแต่งได้ครบถ้วน (เช่น เปิด Daylight, สปอว์นมอนสเตอร์, หรือล็อคเวลาโลก) |
| `InstanceWorldService.kt` (L343) | จำนวนครั้งการ Retry ลบโฟลเดอร์โลกแมตช์ฟิกไว้ที่ `maxRetries = 10` | เพิ่ม `instance_world.delete_retry_attempts: 10` |

---

### หมวดที่ 2: จุดเกิดล็อบบี้และการหลบหนี (Extraction & Lobby Evacuation)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `ExtractionManager.kt` (L465-473), `InstanceWorldService.kt` (L431-442) | จุดส่งผู้เล่นกลับเมื่อสกัดสำเร็จหรือแมตช์จบ สุ่มดึงจุดเกิดโลกหลักแรก (`Bukkit.getWorlds().firstOrNull()?.spawnLocation`) | เพิ่มการตั้งค่าจุดส่งตัวกลับล็อบบี้อย่างเป็นทางการ `lobby.world`, `x`, `y`, `z`, `yaw`, `pitch` |
| `ExtractionListener.kt` (L20-24), `ExtractionCountdownTask.kt` (L118-122) | เมื่อผู้เล่นได้รับความเสียหาย จะถูกยกเลิกการสกัดทันทีแบบไม่มีเงื่อนไข | เพิ่ม `extraction.interrupt_on_damage: true` และ `extraction.damage_threshold: 0.0` (เปิดโอกาสให้โดนดาเมจเล็กน้อยได้ถ้าต้องการ) |
| `ExtractionZoneData.kt` (L38) | เวลานับถอยหลังการสกัดเริ่มต้นฟิกไว้ที่ `10` วินาที | เพิ่ม `extraction.default_countdown_seconds: 10` |
| `ExtractionCountdownTask.kt` (L173-178) | การสกัดสำเร็จมีเพียง Actionbar ไม่มีเสียง Sound FX ตอบรับ | เพิ่ม `extraction.sounds.success: "UI_TOAST_CHALLENGE_COMPLETE"` และ `extraction.sounds.countdown_tick: "UI_BUTTON_CLICK"` |

---

### หมวดที่ 3: ระบบกล่องศพผู้เล่น (Player Corpse Crates)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `DeathListener.kt` (L85), `CorpseManager.kt` (L83, L111) | ชนิดบล็อกกล่องศพฟิกเป็น `Material.CHEST` เสมอ (ทั้งกล่องหลักและกล่องส่วนขยาย overflow) | เพิ่ม `corpse.material: "CHEST"` และ `corpse.overflow_material: "CHEST"` (สามารถเปลี่ยนเป็น BARREL, TRAPPED_CHEST ได้) |
| `DeathListener.kt` (L39-103) | บังคับสร้างกล่องศพเสมอ 100% | เพิ่ม `corpse.enabled: true` (หากตั้งเป็น `false` ไอเทมจะหล่นลงพื้นตามปกติของ Minecraft) |
| `CorpseManager.kt` (L124) | หากไอเทมล้นจากกล่องที่สอง จะหล่นลงพื้นทันที | เพิ่ม `corpse.overflow_action: "DROP"` (DROP หรือ EXPAND_FURTHER) |

---

### หมวดที่ 4: ระบบค้นหากล่องดรอปและเสียงประกอบ (Drop Box & Search System)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `ContainerRegistry.kt` (L104-114), `ContainerCategory.kt` (L29-34) | ค่า Default ถูกฮาร์ดโค้ด (`search_time_min=2`, `search_time_max=4`, `STONE`, `BLACK_STAINED_GLASS_PANE`) แม้ใน `config.yml` จะมีคีย์อยู่แล้วแต่โค้ดยังไม่ได้ดึงมาเชื่อมต่อ | เชื่อมต่อค่า Default จาก `config.yml` เข้าสู่ `ContainerRegistry` และ `ContainerCategory` โดยตรง |
| `ExtractionItem.kt` (L28-34) | เวลาค้นหาไอเทมตามระดับความหายากฟิกตายตัว: Common=1s, Uncommon=2s, Rare=3s, Epic=4s, Legendary=5s | เพิ่ม `drop_box.rarity_search_times` ให้ตั้งเวลาค้นหาของแต่ละระดับความหายากได้ |
| `DropBoxGui.kt` (L313-322) | เสียง Reveal Sound ตามระดับความหายากถูกฮาร์ดโค้ดใน when-statement | เพิ่ม `drop_box.rarity_reveal_sounds` กำหนดเสียงเปิดเจอไอเทมของแต่ละระดับความหายากได้ |
| `DropBoxGui.kt` (L205, L333, L357) | เสียงค้นหาเสร็จ (`ENTITY_EXPERIENCE_ORB_PICKUP`), เสียงกดระหว่างค้นหาไม่ได้ (`UI_BUTTON_CLICK`), เสียงหยิบของ (`ENTITY_ITEM_PICKUP`) ฟิกในโค้ด | เพิ่มกลุ่ม `drop_box.sounds` กำหนดเสียงกิจกรรมต่างๆ ในหน้าต่าง GUI |

---

### หมวดที่ 5: ระบบขอบเรืองแสงกล่องดรอป (Container Highlight & Glowing)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `ContainerHighlightService.kt` (L45, L126) | สีขอบเรืองแสงฟิกเป็นสีขาว `NamedTextColor.WHITE` และชื่อทีมฟิกเป็น `"lumina_glow"` | เพิ่ม `container_highlight.glow_color: "WHITE"` (รองรับ WHITE, GOLD, AQUA, RED, GREEN, YELLOW ฯลฯ) และ `container_highlight.team_name: "lumina_glow"` |

---

### หมวดที่ 6: กระเป๋านิรภัย (Safe Cases / Secure Containers)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `SafeCaseManager.kt` (L204) | บล็อกปิดช่องที่ไม่ได้เปิดใช้งาน (Locked Slots) ฟิกเป็น `GRAY_STAINED_GLASS_PANE` | เพิ่ม `safe_cases_settings.locked_slot_material: "GRAY_STAINED_GLASS_PANE"` |
| `SafeCaseListener.kt` (L58-203) | ระบบป้องกันการซ้อนกระเป๋า (Anti-Nesting) ถูกเปิดถาวร | เพิ่ม `safe_cases_settings.anti_nesting: true` (อนุญาตให้ปิดเพื่อการทดสอบได้) |

---

### หมวดที่ 7: การจัดสรรจุดเกิดผู้เล่น (Spawn Allocator & Anti-Clumping)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `SpawnAllocator.kt` (L113-117) | หากจำนวนผู้เล่น/ทีมมากกว่าจุดเกิดที่มี จะโยน Error `IllegalStateException` ทำให้แมตช์สตาร์ทไม่ติด | เพิ่ม `spawn.allow_spawn_reuse: true` (หากจุดเกิดไม่พอ ให้วนจุดเกิดซ้ำแบบกระจายตัว แทนที่จะเด้ง Error) |

---

### หมวดที่ 8: ระบบเศรษฐกิจและการคำนวณระดับความหายาก (Economy & Rarity Thresholds)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `Rarity.kt` (L38-44) | เกณฑ์ราคาสำหรับจัดระดับความหายากอัตโนมัติฟิกตายตัว: Legendary=2500, Epic=1000, Rare=400, Uncommon=100 | เพิ่ม `economy.rarity_price_thresholds` ให้ปรับเพดานราคาการแบ่งเกรดความหายากได้ |
| `ItemEditorGui.kt` (L42-47) | ราคาเริ่มต้นของไอเทมใหม่ในหน้าต่าง GUI Editor ฟิกตายตัว: 50, 150, 500, 1500, 4000 | เพิ่ม `economy.default_prices` ปรับราคาตั้งต้นของแต่ละเกรดไอเทมได้ |

---

### หมวดที่ 9: หน้าต่าง Editor และเครื่องมือมาร์กเกอร์ (Editor & Marker Visuals)
| ไฟล์ที่ตรวจพบ | ค่าเดิมที่เป็น Hard code | ข้อเสนอแนะในการนำเข้า Config |
| :--- | :--- | :--- |
| `ContainerMarkerListener.kt` (L45-51, L128-134) | อนุภาคและเสียงเมื่อวางมาร์กเกอร์ฟิกเป็น `HAPPY_VILLAGER` (25 ชิ้น) และ `BLOCK_CHEST_LOCKED` | เพิ่ม `editor.marker_place_particle: "HAPPY_VILLAGER"`, `particle_count: 25`, `marker_place_sound: "BLOCK_CHEST_LOCKED"`, `marker_break_sound: "BLOCK_CHEST_CLOSE"` |
| `GuiUtils.kt` (L53, L60, L77) | กระจกขอบหน้าต่าง GUI ทุกหน้าฟิกเป็น `Material.GRAY_STAINED_GLASS_PANE` | เพิ่ม `editor.gui_border_material: "GRAY_STAINED_GLASS_PANE"` |

---

## 3. ตัวอย่างโครงสร้างไฟล์ `config.yml` ฉบับสมบูรณ์ (Proposed Structure)

```yaml
# =========================================================================
# Luminaris: Operation Vanilla Extraction Core Engine (L.O.V.E)
# ไฟล์กำหนดค่าระบบหลักแบบรวมศูนย์ (Master System Configuration)
# =========================================================================

# ภาษาเริ่มต้นของระบบ (en หรือ th)
language: "en"

# =========================================================================
# 1. ระบบล็อบบี้และจุดส่งกลับ (Lobby & Evacuation Destination)
# =========================================================================
lobby:
  world: "world"
  x: 0.5
  y: 64.0
  z: 0.5
  yaw: 0.0
  pitch: 0.0

# =========================================================================
# 2. ระบบการสกัดหลบหนี (Extraction Settings)
# =========================================================================
extraction:
  default_countdown_seconds: 10
  interrupt_on_damage: true
  damage_threshold: 0.0
  sounds:
    success: "UI_TOAST_CHALLENGE_COMPLETE"
    countdown_tick: "UI_BUTTON_CLICK"

# =========================================================================
# 3. ระบบแมตช์และรอบการเล่น (Match Instance Settings)
# =========================================================================
match:
  default_duration_seconds: 1200 # 20 นาที
  early_teardown_when_all_terminal: true
  bossbar:
    enabled: true
    color: "RED"       # RED, GREEN, BLUE, YELLOW, PURPLE, WHITE, PINK
    overlay: "PROGRESS" # PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20
  timeout:
    damage_amount: 100000.0

# =========================================================================
# 4. การจัดการโลกจำลองแมตช์ (Ephemeral Instance World Settings)
# =========================================================================
instance_world:
  templates_folder: "templates_worlds"
  instances_folder: "instances"
  delete_retry_attempts: 10
  gamerules:
    keep_inventory: false
    do_daylight_cycle: false
    do_mob_spawning: false
    do_weather_cycle: false
    time: 6000 # ล็อคเวลาเที่ยงวัน

# =========================================================================
# 5. ระบบกล่องศพผู้เล่น (Player Corpse Container Settings)
# =========================================================================
corpse:
  enabled: true
  material: "CHEST"          # CHEST, BARREL, TRAPPED_CHEST
  overflow_material: "CHEST"
  overflow_action: "DROP"     # DROP หรือ EXPAND_FURTHER

# =========================================================================
# 6. ระบบขอบเรืองแสงกล่องดรอป (Container Highlight & Glowing)
# =========================================================================
container_highlight:
  enabled: true
  max_distance: 16.0
  check_interval_ticks: 6
  mode: "BLOCK_DISPLAY"      # BLOCK_DISPLAY หรือ SHULKER
  fullbright: false
  glow_color: "WHITE"        # WHITE, GOLD, AQUA, RED, GREEN, YELLOW, LIGHT_PURPLE
  team_name: "lumina_glow"
  worlds:
    match: true
    template: true

# =========================================================================
# 7. ระบบค้นหากล่องดรอปและเสียงประกอบ (Drop Box Search System)
# =========================================================================
drop_box:
  default_search_time_min: 2
  default_search_time_max: 4
  default_covered_material: "BLACK_STAINED_GLASS_PANE"
  default_searching_material: "STONE"
  default_search_sound: "BLOCK_STONE_HIT"
  default_reveal_sound: "ENTITY_ITEM_PICKUP"

  rarity_search_times:
    COMMON: 1
    UNCOMMON: 2
    RARE: 3
    EPIC: 4
    LEGENDARY: 5

  rarity_reveal_sounds:
    COMMON: "ENTITY_ITEM_PICKUP"
    UNCOMMON: "BLOCK_NOTE_BLOCK_BELL"
    RARE: "BLOCK_AMETHYST_BLOCK_CHIME"
    EPIC: "ENTITY_PLAYER_LEVELUP"
    LEGENDARY: "UI_TOAST_CHALLENGE_COMPLETE"

  sounds:
    complete: "ENTITY_EXPERIENCE_ORB_PICKUP"
    take_item: "ENTITY_ITEM_PICKUP"
    click_searching_error: "UI_BUTTON_CLICK"

# =========================================================================
# 8. ระบบกระเป๋านิรภัย (Safe Cases Global Settings)
# =========================================================================
safe_cases_settings:
  locked_slot_material: "GRAY_STAINED_GLASS_PANE"
  anti_nesting: true

# =========================================================================
# 9. ระบบจุดเกิดผู้เล่น (Spawn Allocator Settings)
# =========================================================================
spawn:
  allow_spawn_reuse: true # อนุญาตให้วนจุดเกิดซ้ำหากจำนวนผู้เล่นมากกว่าจุดเกิดที่มี

# =========================================================================
# 10. ระบบเศรษฐกิจและการประเมินเกรดไอเทม (Economy & Rarity Thresholds)
# =========================================================================
economy:
  rarity_price_thresholds:
    LEGENDARY: 2500.0
    EPIC: 1000.0
    RARE: 400.0
    UNCOMMON: 100.0

  default_prices:
    COMMON: 50.0
    UNCOMMON: 150.0
    RARE: 500.0
    EPIC: 1500.0
    LEGENDARY: 4000.0

# =========================================================================
# 11. หน้าต่างเมนูแก้ไขและเครื่องมือมาร์กเกอร์ (Editor & Tool Settings)
# =========================================================================
editor:
  marker_place_particle: "HAPPY_VILLAGER"
  marker_place_particle_count: 25
  marker_place_sound: "BLOCK_CHEST_LOCKED"
  marker_break_sound: "BLOCK_CHEST_CLOSE"
  gui_border_material: "GRAY_STAINED_GLASS_PANE"
```

---

## 4. สรุปไฟล์ที่จะได้รับผลกระทบเมื่อเริ่มแก้ไขจริง (Action Plan)

1. **`src/main/resources/config.yml`**: ปรับปรุงโครงสร้างพร้อมคอมเมนต์ภาษาไทย-อังกฤษ และค่าเริ่มต้นมาตรฐาน
2. **`LuminarisCorePlugin.kt`**: โหลด `saveDefaultConfig()` และเชื่อมต่อ `reloadConfig()` ให้ครอบคลุมทุกโมดูล
3. **`MatchManager.kt` & `MatchInstance.kt`**: อ่านค่า `match.*` (ระยะเวลา, BossBar, ดาเมจ M.I.A.)
4. **`InstanceWorldService.kt`**: อ่านค่า `instance_world.*` (โฟลเดอร์, GameRules, รีไทร) และ `lobby.*`
5. **`ExtractionManager.kt` & `ExtractionCountdownTask.kt`**: อ่านค่า `extraction.*` และจุดส่งตัว `lobby.*`
6. **`CorpseManager.kt` & `DeathListener.kt`**: อ่านค่า `corpse.*` (เปิด/ปิด, ชนิดบล็อกกล่องศพ)
7. **`ContainerRegistry.kt`, `ContainerCategory.kt`, `DropBoxGui.kt`, `ExtractionItem.kt`**: เชื่อมต่อค่าเริ่มต้นของ Drop Box, เวลาสุ่มค้นหาตามเกรด, และเสียง Reveal Sound
8. **`ContainerHighlightService.kt`**: อ่านค่าสีเรืองแสง `container_highlight.glow_color`
9. **`SafeCaseManager.kt` & `SafeCaseListener.kt`**: อ่านค่า `safe_cases_settings.*` (ชนิดบล็อกช่องล็อค, Anti-Nesting)
10. **`SpawnAllocator.kt`**: รองรับการวนใช้จุดเกิดเมื่อจำนวนผู้เล่นเกิน
11. **`Rarity.kt` & `ItemEditorGui.kt`**: รองรับเกณฑ์ราคาและราคาเริ่มต้นจาก Config
12. **`ContainerMarkerListener.kt` & `GuiUtils.kt`**: อ่านค่าอนุภาคและเสียง Marker

---

## 5. บันทึกผลการดำเนินงานและการทดสอบ (Verification & Test Results)

ทุกส่วนได้รับการเชื่อมต่อและผ่านการทดสอบอัตโนมัติด้วย `./gradlew test` เป็นที่เรียบร้อย:

- [x] **`config.yml`**: จัดเรียง 11 หมวดหมู่หลัก มีคำอธิบายภาษาไทยและอังกฤษ กำกับชนิดข้อมูลและค่าเริ่มต้นทุกตัว
- [x] **`LuminarisCorePlugin.kt`**: โหลด `saveDefaultConfig()` และสั่ง `reloadConfig()` เมื่อเรียก `/ext reload`
- [x] **`MatchManager.kt` & `MatchInstance.kt`**: ระยะเวลาแมตช์, การเปิด/ปิดและสี BossBar, ดาเมจ M.I.A., Teardown
- [x] **`InstanceWorldService.kt`**: โฟลเดอร์ World, GameRules (keepInventory, daylight, mobSpawn, fixedTime), Retries, และจุดส่งตัวกลับล็อบบี้
- [x] **`ExtractionManager.kt` & `ExtractionCountdownTask.kt`**: ดาเมจยกเลิกการสกัด, นับถอยหลังเริ่มต้น, เสียงนับ Tick, เสียงสกัดสำเร็จ
- [x] **`CorpseManager.kt` & `DeathListener.kt`**: เปิด/ปิดกล่องศพ, ชนิดบล็อกกล่องศพหลักและส่วนขยาย
- [x] **`ContainerRegistry.kt`, `DropBoxGui.kt`, `ExtractionItem.kt`**: ค่าเริ่มต้นเวลาค้นหา, บล็อกปิดช่อง, เสียงค้นหา, เสียง Reveal Sound ตามระดับความหายาก, เสียงกิจกรรม GUI
- [x] **`ContainerHighlightService.kt`**: สีเรืองแสง (Glow Color) และชื่อทีม
- [x] **`SafeCaseManager.kt` & `SafeCaseListener.kt`**: ชนิดบล็อกช่องล็อค, การเปิด/ปิด Anti-Nesting
- [x] **`SpawnAllocator.kt`**: ระบบ `allow_spawn_reuse` ป้องกัน Error เมื่อผู้เล่นมากกว่าจุดเกิด
- [x] **`Rarity.kt` & `ItemEditorGui.kt`**: เพดานราคาแบ่งระดับความหายาก และราคาเริ่มต้นของไอเทม
- [x] **`ContainerMarkerListener.kt` & `GuiUtils.kt`**: อนุภาค/เสียงวางและทุบมาร์กเกอร์, กระจกขอบ GUI ทุกหน้า

**ผลลัพธ์การคอมไพล์และทดสอบ:** ผ่านการรันชุดทดสอบทั้งหมด 100% (Build & Tests Successful) ไม่มีข้อผิดพลาด
