/**
 * Luminaris Extraction Core • Web Configuration Application
 * Tailwind CSS + Full-Width Responsive Design + Thai Primary Language + SVG Icons
 * Full Support for Adding & Deleting Items, Categories, Safe Cases, and Translation Keys.
 */

(function () {
  'use strict';

  // --- SVG Icons Mapping (Zero Emojis) ---
  const SVG_ICONS = {
    general: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>`,
    lobby: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>`,
    extraction: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polyline></svg>`,
    match: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>`,
    instance_world: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon><line x1="8" y1="2" x2="8" y2="18"></line><line x1="16" y1="6" x2="16" y2="22"></line></svg>`,
    corpse: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="12" r="1"></circle><circle cx="15" cy="12" r="1"></circle><path d="M8 20v2h8v-2"></path><path d="M12.5 17l-.5-1-.5 1h1z"></path><path d="M16 20a6 6 0 1 0-8 0"></path></svg>`,
    container_highlight: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>`,
    drop_box: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>`,
    safe_cases_settings: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>`,
    spawn: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><circle cx="12" cy="12" r="6"></circle><circle cx="12" cy="12" r="2"></circle></svg>`,
    economy: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"></circle><path d="M14.8 9A2 2 0 0 0 13 8h-2a2 2 0 1 0 0 4h2a2 2 0 1 1 0 4h-2a2 2 0 0 1-1.8-1"></path><path d="M12 6v2m0 8v2"></path></svg>`,
    editor: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="13.5" cy="6.5" r=".5"></circle><circle cx="17.5" cy="10.5" r=".5"></circle><circle cx="8.5" cy="7.5" r=".5"></circle><circle cx="6.5" cy="12.5" r=".5"></circle><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"></path></svg>`,
    web_config: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>`,
    box: `<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg>`,
    plus: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>`,
    trash: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>`,
    copy: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>`,
    check: `<svg class="w-4 h-4 text-emerald-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>`,
    alert: `<svg class="w-4 h-4 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>`
  };

  // --- Configuration Schema in Thai ---
  const CONFIG_SCHEMA = {
    general: {
      title: 'ภาษาและระบบทั่วไป',
      desc: 'การตั้งค่าภาษาหลักของปลั๊กอิน (รองรับ th ภาษาไทย และ en ภาษาอังกฤษ)',
      fields: [
        { key: 'language', label: 'ภาษาหลักเริ่มต้น', desc: 'เลือกภาษาที่ต้องการให้ระบบนำมาแสดงผลในแชทและเมนู', type: 'select', options: ['th', 'en'] }
      ]
    },
    lobby: {
      title: 'จุดส่งตัวกลับล็อบบี้ (Lobby & Evacuation)',
      desc: 'พิกัดตำแหน่งที่ผู้เล่นจะถูกส่งตัวกลับ เมื่อสกัดหลบหนีสำเร็จหรือแมตช์สิ้นสุด',
      fields: [
        { key: 'lobby.world', label: 'ชื่อโลกของล็อบบี้', desc: 'เช่น world หรือ lobby', type: 'text', default: 'world' },
        { key: 'lobby.x', label: 'พิกัด X', desc: 'ตำแหน่งแนวนอน X', type: 'number', step: 0.5, default: 0.5 },
        { key: 'lobby.y', label: 'พิกัด Y (ความสูง)', desc: 'ระดับความสูง Y', type: 'number', step: 0.5, default: 64.0 },
        { key: 'lobby.z', label: 'พิกัด Z', desc: 'ตำแหน่งแนวลึก Z', type: 'number', step: 0.5, default: 0.5 },
        { key: 'lobby.yaw', label: 'มุมมองสายตา (Yaw)', desc: 'มุมมองสายตาแนวนอน 0 ถึง 360', type: 'number', step: 1.0, default: 0.0 },
        { key: 'lobby.pitch', label: 'มุมมองสายตา (Pitch)', desc: 'มุมมองสายตาก้ม-เงย (-90 ถึง 90)', type: 'number', step: 1.0, default: 0.0 }
      ]
    },
    extraction: {
      title: 'ระบบการสกัดหลบหนี (Extraction Matrix)',
      desc: 'ควบคุมเวลานับถอยหลังในโซนสกัด และการยกเลิกเมื่อผู้เล่นถูกโจมตี',
      fields: [
        { key: 'extraction.default_countdown_seconds', label: 'เวลานับถอยหลังเริ่มต้น (วินาที)', desc: 'ระยะเวลาที่ต้องยืนในโซนจนกว่าจะสกัดสำเร็จ', type: 'number', min: 1, max: 300, default: 10 },
        { key: 'extraction.interrupt_on_damage', label: 'ยกเลิกการสกัดเมื่อโดนดาเมจ', desc: 'หากเปิดไว้ เมื่อผู้เล่นได้รับดาเมจเวลาสกัดจะถูกยกเลิกทันที', type: 'boolean', default: true },
        { key: 'extraction.damage_threshold', label: 'ดาเมจขั้นต่ำที่ทำให้การสกัดหลุด', desc: '0.0 = โดนดาเมจใดๆ ก็ยกเลิกทันที', type: 'number', step: 0.1, default: 0.0 },
        { key: 'extraction.sounds.success', label: 'เสียงเมื่อสกัดสำเร็จ', desc: 'Bukkit Sound Name เช่น UI_TOAST_CHALLENGE_COMPLETE', type: 'text', default: 'UI_TOAST_CHALLENGE_COMPLETE' },
        { key: 'extraction.sounds.countdown_tick', label: 'เสียงจังหวะนับถอยหลังแต่ละวินาที', desc: 'Bukkit Sound Name เช่น UI_BUTTON_CLICK', type: 'text', default: 'UI_BUTTON_CLICK' }
      ]
    },
    match: {
      title: 'ระบบรอบการเล่นและแมตช์ (Match Instances)',
      desc: 'กำหนดระยะเวลาการเล่นในแมตช์, แถบ BossBar และบทลงโทษเมื่อหมดเวลา (M.I.A.)',
      fields: [
        { key: 'match.default_duration_seconds', label: 'ระยะเวลาการเล่นต่อแมตช์ (วินาที)', desc: '1200 วินาที = 20 นาที', type: 'number', min: 60, max: 86400, default: 1200 },
        { key: 'match.early_teardown_when_all_terminal', label: 'ปิดโลกแมตช์ทันทีเมื่อผู้เล่นออกครบ', desc: 'ประหยัด RAM/CPU โดยไม่ต้องรอจนหมดเวลาหากผู้เล่นตายหรือสกัดหมดแล้ว', type: 'boolean', default: true },
        { key: 'match.bossbar.enabled', label: 'แสดงแถบเวลา BossBar ด้านบนจอ', desc: 'เปิด/ปิดแถบนับถอยหลังเวลาแมตช์สำหรับผู้เล่นในรอบ', type: 'boolean', default: true },
        { key: 'match.bossbar.color', label: 'สีของแถบ BossBar', desc: 'สีแถบแสดงผล', type: 'select', options: ['RED', 'BLUE', 'GREEN', 'YELLOW', 'PURPLE', 'WHITE', 'PINK'] },
        { key: 'match.bossbar.overlay', label: 'รูปแบบช่องของ BossBar', desc: 'จำนวนขีดช่องบนแถบเวลา', type: 'select', options: ['PROGRESS', 'NOTCHED_6', 'NOTCHED_10', 'NOTCHED_12', 'NOTCHED_20'] },
        { key: 'match.timeout.damage_amount', label: 'ดาเมจเมื่อหมดเวลาแมตช์ (M.I.A.)', desc: 'ดาเมจที่จะสร้างแก่ผู้เล่นที่ไม่สามารถสกัดได้ทันเวลา (100000.0 = กำจัดทันที)', type: 'number', step: 100.0, default: 100000.0 }
      ]
    },
    instance_world: {
      title: 'การจัดการโลกจำลองแมตช์ (Ephemeral Instance Worlds)',
      desc: 'ควบคุมการโคลนและลบโลกแมตช์ชั่วคราวอัตโนมัติ พร้อมตั้งค่ากฎ Gamerules',
      fields: [
        { key: 'instance_world.templates_folder', label: 'ชื่อโฟลเดอร์แผนที่ต้นฉบับ (Templates)', desc: 'โฟลเดอร์เก็บโลกต้นฉบับสำหรับนำมาโคลน', type: 'text', default: 'templates_worlds' },
        { key: 'instance_world.instances_folder', label: 'ชื่อโฟลเดอร์โลกแมตช์ (Instances)', desc: 'โฟลเดอร์สำหรับวางโลกแมตช์ที่กำลังเล่น', type: 'text', default: 'instances' },
        { key: 'instance_world.delete_retry_attempts', label: 'จำนวนครั้งสูงสุดในการลองลบโฟลเดอร์ซ้ำ', desc: 'แก้ปัญหา File Lock บน Windows/Linux เมื่อปิดแมตช์ (แนะนำ 10 ครั้ง)', type: 'number', min: 1, max: 50, default: 10 },
        { key: 'instance_world.gamerules.keep_inventory', label: 'กฎ: เก็บของไว้กับตัวเมื่อตาย (Keep Inventory)', desc: 'false = ดรอปของลงกล่องศพตามกฎสกัดหลบหนี', type: 'boolean', default: false },
        { key: 'instance_world.gamerules.do_daylight_cycle', label: 'กฎ: ให้เวลากลางวัน-กลางคืนเดินตามปกติ', desc: 'เปิด/ปิดการเดินของเวลาในโลกแมตช์', type: 'boolean', default: false },
        { key: 'instance_world.gamerules.do_mob_spawning', label: 'กฎ: สปอว์นมอนสเตอร์ตามธรรมชาติ', desc: 'เปิด/ปิดมอนสเตอร์สปอว์นเองตามธรรมชาติ', type: 'boolean', default: false },
        { key: 'instance_world.gamerules.do_weather_cycle', label: 'กฎ: สภาพอากาศ ฝนและพายุ', desc: 'เปิด/ปิดวงจรฝนตกฟ้าผ่า', type: 'boolean', default: false },
        { key: 'instance_world.gamerules.fixed_time', label: 'กำหนดเวลาคงที่ของโลกแมตช์ (Ticks)', desc: '6000 = เที่ยงวันสว่างชัดเจน, 18000 = กลางคืนมืดสนิท', type: 'number', min: 0, max: 24000, default: 6000 }
      ]
    },
    corpse: {
      title: 'ระบบกล่องศพผู้เล่น (Player Corpse Containers)',
      desc: 'กำหนดบล็อกที่ใช้เก็บของเมื่อผู้เล่นเสียชีวิตในสนามรบ',
      fields: [
        { key: 'corpse.enabled', label: 'เปิดใช้งานระบบกล่องศพ', desc: 'true = วางกล่องเก็บของที่จุดตาย, false = ของกระจายลงพื้น', type: 'boolean', default: true },
        { key: 'corpse.material', label: 'ชนิดบล็อกสำหรับกล่องศพหลัก', desc: 'CHEST, BARREL, หรือ TRAPPED_CHEST', type: 'select', options: ['CHEST', 'BARREL', 'TRAPPED_CHEST'] },
        { key: 'corpse.overflow_material', label: 'ชนิดบล็อกสำหรับกล่องส่วนขยาย', desc: 'วางเพิ่มอัตโนมัติหากไอเทมของผู้เล่นเกิน 27 ช่อง', type: 'select', options: ['CHEST', 'BARREL', 'TRAPPED_CHEST'] },
        { key: 'corpse.overflow_action', label: 'การจัดการหากไอเทมเกิน 2 กล่อง', desc: 'DROP = ดรอปไอเทมส่วนเกินลงพื้น, VOID = ลบทิ้ง', type: 'select', options: ['DROP', 'VOID'] }
      ]
    },
    container_highlight: {
      title: 'ระบบขอบเรืองแสงกล่องดรอป (Container Glow & LOS)',
      desc: 'แสดงเส้นขอบเรืองแสงรอบกล่องดรอป พร้อมระบบตรวจจับแนวสายตาป้องกันการมองทะลุกำแพง',
      fields: [
        { key: 'container_highlight.enabled', label: 'เปิดใช้งานเส้นขอบเรืองแสงรอบกล่อง', desc: 'เปิด/ปิดระบบเรืองแสงทั้งหมด', type: 'boolean', default: true },
        { key: 'container_highlight.max_distance', label: 'ระยะห่างสูงสุดที่จะแสดงผล (บล็อก)', desc: 'ระยะตรวจจับผู้เล่น (เช่น 16 บล็อก)', type: 'number', step: 1.0, min: 2.0, max: 64.0, default: 16.0 },
        { key: 'container_highlight.check_interval_ticks', label: 'ความถี่ในการตรวจแนวสายตา (Ticks)', desc: '20 Ticks = 1 วินาที (แนะนำ 4-10 Ticks)', type: 'number', min: 1, max: 40, default: 6 },
        { key: 'container_highlight.mode', label: 'รูปแบบ Entity ที่ใช้สร้างแสง', desc: 'BLOCK_DISPLAY (แนะนำ: ไม่มี Hitbox บังคลิก) หรือ SHULKER', type: 'select', options: ['BLOCK_DISPLAY', 'SHULKER'] },
        { key: 'container_highlight.fullbright', label: 'บังคับความสว่างสูงสุดของโมเดลกล่อง (Fullbright)', desc: 'สว่างตลอดเวลาแม้ในที่มืดสนิท', type: 'boolean', default: false },
        { key: 'container_highlight.glow_color', label: 'สีของเส้นขอบเรืองแสงรอบกล่อง', desc: 'Scoreboard Glow Color', type: 'select', options: ['WHITE', 'GOLD', 'AQUA', 'RED', 'GREEN', 'YELLOW', 'LIGHT_PURPLE', 'DARK_PURPLE', 'DARK_AQUA'] },
        { key: 'container_highlight.team_name', label: 'ชื่อ Scoreboard Team สำหรับเรืองแสง', desc: 'ชื่อทีมสำหรับระบบภายใน', type: 'text', default: 'lumina_glow' },
        { key: 'container_highlight.worlds.match', label: 'เปิดใช้งานในโลกแมตช์สกัดหลบหนี', desc: 'เปิดในห้องเล่นจริง', type: 'boolean', default: true },
        { key: 'container_highlight.worlds.template', label: 'เปิดใช้งานในโลกเทมเพลตสำหรับแก้ไข', desc: 'เปิดในโหมดแอดมินสร้างแมพ', type: 'boolean', default: true }
      ]
    },
    drop_box: {
      title: 'ระบบค้นหากล่องดรอปและเสียงประกอบ (Drop Box Search)',
      desc: 'กลไกการสุ่มค้นหาไอเทมทีละช่อง และเสียงเอฟเฟกต์ระหว่างค้นหา',
      fields: [
        { key: 'drop_box.default_search_time_min', label: 'เวลาค้นหากล่องขั้นต่ำ (วินาที)', desc: 'ช่วงเริ่มค้นหากล่อง', type: 'number', min: 0, max: 30, default: 2 },
        { key: 'drop_box.default_search_time_max', label: 'เวลาค้นหากล่องสูงสุด (วินาที)', desc: 'ช่วงค้นหากล่องสูงสุด', type: 'number', min: 1, max: 60, default: 4 },
        { key: 'drop_box.default_covered_material', label: 'บล็อกที่ใช้ปิดทุกช่องก่อนเริ่มค้นหา', desc: 'เช่น BLACK_STAINED_GLASS_PANE', type: 'text', default: 'BLACK_STAINED_GLASS_PANE' },
        { key: 'drop_box.default_searching_material', label: 'บล็อกที่แสดงระหว่างค้นหาทีละช่อง', desc: 'เช่น STONE', type: 'text', default: 'STONE' },
        { key: 'drop_box.default_search_sound', label: 'เสียงจังหวะค้นหาทีละช่อง', desc: 'เช่น BLOCK_STONE_HIT', type: 'text', default: 'BLOCK_STONE_HIT' },
        { key: 'drop_box.default_reveal_sound', label: 'เสียงเริ่มต้นเมื่อเปิดพบไอเทม', desc: 'เช่น ENTITY_ITEM_PICKUP', type: 'text', default: 'ENTITY_ITEM_PICKUP' },
        { key: 'drop_box.rarity_search_times.COMMON', label: 'เวลาค้นหาไอเทม Common (วินาที)', desc: 'ไอเทมทั่วไป', type: 'number', min: 1, default: 1 },
        { key: 'drop_box.rarity_search_times.UNCOMMON', label: 'เวลาค้นหาไอเทม Uncommon (วินาที)', desc: 'ไอเทมคุณภาพปานกลาง', type: 'number', min: 1, default: 2 },
        { key: 'drop_box.rarity_search_times.RARE', label: 'เวลาค้นหาไอเทม Rare (วินาที)', desc: 'ไอเทมหายาก', type: 'number', min: 1, default: 3 },
        { key: 'drop_box.rarity_search_times.EPIC', label: 'เวลาค้นหาไอเทม Epic (วินาที)', desc: 'ไอเทมมหากาพย์', type: 'number', min: 1, default: 4 },
        { key: 'drop_box.rarity_search_times.LEGENDARY', label: 'เวลาค้นหาไอเทม Legendary (วินาที)', desc: 'ไอเทมตำนาน', type: 'number', min: 1, default: 5 },
        { key: 'drop_box.sounds.complete', label: 'เสียงเมื่อค้นหากล่องครบทุกช่อง', desc: 'เช่น ENTITY_EXPERIENCE_ORB_PICKUP', type: 'text', default: 'ENTITY_EXPERIENCE_ORB_PICKUP' },
        { key: 'drop_box.sounds.take_item', label: 'เสียงเมื่อหยิบไอเทมเข้ากระเป๋า', desc: 'เช่น ENTITY_ITEM_PICKUP', type: 'text', default: 'ENTITY_ITEM_PICKUP' },
        { key: 'drop_box.sounds.click_searching_error', label: 'เสียงแจ้งเตือนเมื่อคลิกช่องที่ยังหาไม่เสร็จ', desc: 'เช่น UI_BUTTON_CLICK', type: 'text', default: 'UI_BUTTON_CLICK' }
      ]
    },
    safe_cases_settings: {
      title: 'ระบบกระเป๋านิรภัยสากล (Safe Cases Global)',
      desc: 'การป้องกันการซ้อนกระเป๋า และวัสดุบล็อกช่องที่ยังไม่ได้ปลดล็อค',
      fields: [
        { key: 'safe_cases_settings.locked_slot_material', label: 'ไอเทมปิดช่องที่ยังไม่ได้ปลดล็อค', desc: 'เช่น GRAY_STAINED_GLASS_PANE', type: 'text', default: 'GRAY_STAINED_GLASS_PANE' },
        { key: 'safe_cases_settings.anti_nesting', label: 'ป้องกันการใส่กระเป๋าซ้อนในกระเป๋าอื่น (Anti-Nesting)', desc: 'บล็อกการซ้อนกระเป๋าทุกกรณีเพื่อป้องกันการปั๊มช่องเก็บของ', type: 'boolean', default: true }
      ]
    },
    spawn: {
      title: 'การกระจายจุดเกิดผู้เล่น (Spawn Allocator)',
      desc: 'อัลกอริทึมกระจายจุดเกิดแบบ Anti-Clumping',
      fields: [
        { key: 'spawn.allow_spawn_reuse', label: 'วนจุดเกิดซ้ำหากผู้เล่นมากกว่าจุดเกิดที่มี', desc: 'ช่วยให้เล่นได้แม้จุดเกิดมีน้อยกว่าจำนวนผู้เล่น', type: 'boolean', default: true }
      ]
    },
    economy: {
      title: 'เศรษฐกิจและเกณฑ์ราคาไอเทม (Economy & Prices)',
      desc: 'เกณฑ์ราคาสำหรับจัดเกรดความหายากอัตโนมัติ และราคาเริ่มต้นเมื่อสร้างไอเทมใหม่',
      fields: [
        { key: 'economy.rarity_price_thresholds.LEGENDARY', label: 'เกณฑ์ราคาขั้นต่ำ: Legendary', desc: 'เช่น 2500.0', type: 'number', step: 100.0, default: 2500.0 },
        { key: 'economy.rarity_price_thresholds.EPIC', label: 'เกณฑ์ราคาขั้นต่ำ: Epic', desc: 'เช่น 1000.0', type: 'number', step: 50.0, default: 1000.0 },
        { key: 'economy.rarity_price_thresholds.RARE', label: 'เกณฑ์ราคาขั้นต่ำ: Rare', desc: 'เช่น 400.0', type: 'number', step: 25.0, default: 400.0 },
        { key: 'economy.rarity_price_thresholds.UNCOMMON', label: 'เกณฑ์ราคาขั้นต่ำ: Uncommon', desc: 'เช่น 100.0', type: 'number', step: 10.0, default: 100.0 },
        { key: 'economy.default_prices.COMMON', label: 'ราคาเริ่มต้นไอเทมใหม่: Common', desc: 'ราคาเริ่มต้น', type: 'number', default: 50.0 },
        { key: 'economy.default_prices.UNCOMMON', label: 'ราคาเริ่มต้นไอเทมใหม่: Uncommon', desc: 'ราคาเริ่มต้น', type: 'number', default: 150.0 },
        { key: 'economy.default_prices.RARE', label: 'ราคาเริ่มต้นไอเทมใหม่: Rare', desc: 'ราคาเริ่มต้น', type: 'number', default: 500.0 },
        { key: 'economy.default_prices.EPIC', label: 'ราคาเริ่มต้นไอเทมใหม่: Epic', desc: 'ราคาเริ่มต้น', type: 'number', default: 1500.0 },
        { key: 'economy.default_prices.LEGENDARY', label: 'ราคาเริ่มต้นไอเทมใหม่: Legendary', desc: 'ราคาเริ่มต้น', type: 'number', default: 4000.0 }
      ]
    },
    editor: {
      title: 'เอฟเฟกต์เครื่องมือและเมนูแก้ไข (Editor & Visuals)',
      desc: 'อนุภาคเอฟเฟกต์และเสียงของเครื่องมือมาร์กเกอร์จุดวางกล่องในเกม',
      fields: [
        { key: 'editor.marker_place_particle', label: 'อนุภาคเมื่อวางมาร์กเกอร์สำเร็จ', desc: 'เช่น HAPPY_VILLAGER หรือ VILLAGER_HAPPY', type: 'text', default: 'HAPPY_VILLAGER' },
        { key: 'editor.marker_place_particle_count', label: 'จำนวนเม็ดอนุภาค', desc: 'เช่น 25', type: 'number', min: 1, max: 100, default: 25 },
        { key: 'editor.marker_place_sound', label: 'เสียงเมื่อวางมาร์กเกอร์', desc: 'เช่น BLOCK_CHEST_LOCKED', type: 'text', default: 'BLOCK_CHEST_LOCKED' },
        { key: 'editor.marker_break_sound', label: 'เสียงเมื่อทุบมาร์กเกอร์', desc: 'เช่น BLOCK_CHEST_CLOSE', type: 'text', default: 'BLOCK_CHEST_CLOSE' },
        { key: 'editor.gui_border_material', label: 'สีกระจกขอบหน้าต่างเมนูแก้ไขทั้งหมด', desc: 'เช่น GRAY_STAINED_GLASS_PANE', type: 'text', default: 'GRAY_STAINED_GLASS_PANE' }
      ]
    },
    web_config: {
      title: 'เว็บเซิร์ฟเวอร์ตั้งค่า (Web Config Server)',
      desc: 'ควบคุมการเปิด/ปิด, หมายเลขพอร์ต และรหัสผ่านความปลอดภัยของหน้าเว็บนี้',
      fields: [
        { key: 'web_config.enabled', label: 'เปิดใช้งานเว็บตั้งค่า', desc: 'เปิดรันเว็บเซิร์ฟเวอร์ทันทีเมื่อเริ่มปลั๊กอิน', type: 'boolean', default: true },
        { key: 'web_config.port', label: 'พอร์ตที่ใช้รันเว็บ (Port)', desc: 'เช่น 8080, 8888, 3000 (หากเปลี่ยนพอร์ต ระบบจะรีสตาร์ตเว็บอัตโนมัติ)', type: 'number', min: 1024, max: 65535, default: 8080 },
        { key: 'web_config.bind_address', label: 'IP Address สำหรับการดักฟัง (Bind Address)', desc: '0.0.0.0 = เข้าได้จากภายนอก, 127.0.0.1 = เข้าได้เฉพาะเครื่องโฮสต์', type: 'text', default: '0.0.0.0' },
        { key: 'web_config.auth_token', label: 'รหัสผ่านหรือ Security Token', desc: 'หากระบุไว้ ผู้ใช้จะต้องใส่ Token ก่อนเข้าจัดการ (เว้นว่างไว้หากไม่ต้องการรหัสผ่าน)', type: 'text', default: '' }
      ]
    }
  };

  // --- Application State ---
  const state = {
    activeFile: 'config.yml',
    activeCategory: 'general',
    viewMode: 'form',
    rawYaml: '',
    originalRawYaml: '',
    parsedData: {},
    isDirty: false,
    filesList: [],
    authToken: localStorage.getItem('lumina_auth_token') || '',
    searchQuery: ''
  };

  // --- DOM Elements ---
  const elements = {
    serverStatusBadge: document.getElementById('serverStatusBadge'),
    statusPlatformText: document.getElementById('statusPlatformText'),
    globalSearchInput: document.getElementById('globalSearchInput'),
    searchClearBtn: document.getElementById('searchClearBtn'),
    dirtyIndicator: document.getElementById('dirtyIndicator'),
    discardBtn: document.getElementById('discardBtn'),
    inGameReloadBtn: document.getElementById('inGameReloadBtn'),
    saveBtn: document.getElementById('saveBtn'),
    authModalBtn: document.getElementById('authModalBtn'),
    authModal: document.getElementById('authModal'),
    closeAuthModalBtn: document.getElementById('closeAuthModalBtn'),
    authTokenInput: document.getElementById('authTokenInput'),
    saveAuthTokenBtn: document.getElementById('saveAuthTokenBtn'),
    clearAuthTokenBtn: document.getElementById('clearAuthTokenBtn'),
    entryModal: document.getElementById('entryModal'),
    entryModalTitle: document.getElementById('entryModalTitle'),
    entryModalBody: document.getElementById('entryModalBody'),
    closeEntryModalBtn: document.getElementById('closeEntryModalBtn'),
    cancelEntryModalBtn: document.getElementById('cancelEntryModalBtn'),
    confirmEntryModalBtn: document.getElementById('confirmEntryModalBtn'),
    currentFileDisplay: document.getElementById('currentFileDisplay'),
    currentCategoryDisplay: document.getElementById('currentCategoryDisplay'),
    viewModeFormBtn: document.getElementById('viewModeFormBtn'),
    viewModeRawBtn: document.getElementById('viewModeRawBtn'),
    formViewPanel: document.getElementById('formViewPanel'),
    rawViewPanel: document.getElementById('rawViewPanel'),
    categoryTitle: document.getElementById('categoryTitle'),
    categoryDesc: document.getElementById('categoryDesc'),
    formFieldsContainer: document.getElementById('formFieldsContainer'),
    rawFileSelect: document.getElementById('rawFileSelect'),
    rawMetaDisplay: document.getElementById('rawMetaDisplay'),
    syntaxStatus: document.getElementById('syntaxStatus'),
    rawFormatBtn: document.getElementById('rawFormatBtn'),
    rawLineNumbers: document.getElementById('rawLineNumbers'),
    rawYamlTextarea: document.getElementById('rawYamlTextarea'),
    dynamicFilesSection: document.getElementById('dynamicFilesSection'),
    dynamicFilesNavList: document.getElementById('dynamicFilesNavList'),
    createNewFileBtn: document.getElementById('createNewFileBtn'),
    footerVersionText: document.getElementById('footerVersionText'),
    toastContainer: document.getElementById('toastContainer'),
    confirmModal: document.getElementById('confirmModal'),
    confirmModalTitle: document.getElementById('confirmModalTitle'),
    confirmModalDesc: document.getElementById('confirmModalDesc'),
    cancelConfirmModalBtn: document.getElementById('cancelConfirmModalBtn'),
    okConfirmModalBtn: document.getElementById('okConfirmModalBtn'),
    createFileModal: document.getElementById('createFileModal'),
    closeCreateFileModalBtn: document.getElementById('closeCreateFileModalBtn'),
    cancelCreateFileModalBtn: document.getElementById('cancelCreateFileModalBtn'),
    confirmCreateFileModalBtn: document.getElementById('confirmCreateFileModalBtn'),
    newConfigFileNameInput: document.getElementById('newConfigFileNameInput')
  };

  // --- API Client ---

  function getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (state.authToken) {
      headers['Authorization'] = 'Bearer ' + state.authToken;
      headers['X-Auth-Token'] = state.authToken;
    }
    return headers;
  }

  async function apiFetch(endpoint, options = {}) {
    options.headers = Object.assign({}, getHeaders(), options.headers || {});
    try {
      const response = await fetch(endpoint, options);
      if (response.status === 401) {
        showToast('จำเป็นต้องใส่โทเค็นความปลอดภัย (Auth Token)', 'error');
        openAuthModal();
      }
      const data = await response.json();
      return data;
    } catch (err) {
      console.error('API Error:', err);
      showToast('การเชื่อมต่อขัดข้อง: ' + err.message, 'error');
      return { success: false, error: err.message };
    }
  }

  async function loadStatus() {
    const res = await apiFetch('/api/status');
    if (res && res.success) {
      elements.statusPlatformText.textContent = `${res.platform} • v${res.version}`;
      elements.footerVersionText.textContent = `v${res.version}`;
      if (res.authRequired && !state.authToken) {
        openAuthModal();
      }
    } else {
      elements.statusPlatformText.textContent = 'ออฟไลน์ (Offline)';
      elements.serverStatusBadge.querySelector('.status-dot').className = 'status-dot w-2 h-2 rounded-full bg-red-500';
    }
  }

  async function loadFilesList() {
    const res = await apiFetch('/api/files');
    if (res && res.success && Array.isArray(res.files)) {
      state.filesList = res.files;
      renderFilesDropdown();
      renderDynamicNavList();
    }
  }

  async function loadConfigFile(filePath) {
    if (state.isDirty) {
      if (!confirm('คุณมีการแก้ไขที่ยังไม่ได้บันทึก ต้องการยกเลิกและสลับไฟล์หรือไม่?')) {
        return false;
      }
    }

    elements.formFieldsContainer.innerHTML = `
      <div class="flex items-center justify-center py-20 text-zinc-500 text-sm">
        กำลังโหลดไฟล์ ${escapeHtml(filePath)}...
      </div>
    `;

    const res = await apiFetch(`/api/config?file=${encodeURIComponent(filePath)}`);
    if (res && res.success) {
      state.activeFile = filePath;
      state.rawYaml = res.raw || '';
      state.originalRawYaml = res.raw || '';
      state.parsedData = res.parsed || {};
      setDirty(false);

      elements.currentFileDisplay.textContent = filePath;
      elements.rawYamlTextarea.value = state.rawYaml;
      elements.rawFileSelect.value = filePath;
      updateLineNumbers();
      updateRawMeta();

      renderCurrentView();
      return true;
    } else {
      showToast('ไม่สามารถโหลดไฟล์ได้: ' + (res.error || 'ข้อผิดพลาดไม่ทราบสาเหตุ'), 'error');
      return false;
    }
  }

  async function saveConfigFile() {
    elements.saveBtn.disabled = true;
    elements.saveBtn.innerHTML = `
      <svg class="w-3.5 h-3.5 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12a9 9 0 1 1-6.219-8.56"></path>
      </svg>
      <span>กำลังบันทึก...</span>
    `;

    if (state.viewMode === 'raw') {
      state.rawYaml = elements.rawYamlTextarea.value;
    }

    const res = await apiFetch(`/api/config?file=${encodeURIComponent(state.activeFile)}`, {
      method: 'POST',
      body: JSON.stringify({ content: state.rawYaml })
    });

    elements.saveBtn.disabled = false;
    elements.saveBtn.innerHTML = `
      <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
        <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
        <polyline points="17 21 17 13 7 13 7 21"></polyline>
        <polyline points="7 3 7 8 15 8"></polyline>
      </svg>
      <span>บันทึกและใช้งาน</span>
    `;

    if (res && res.success) {
      state.originalRawYaml = state.rawYaml;
      setDirty(false);
      showToast('บันทึกและรีโหลดการตั้งค่าในเกมเรียบร้อยแล้ว!', 'success');
      const reloadRes = await apiFetch(`/api/config?file=${encodeURIComponent(state.activeFile)}`);
      if (reloadRes && reloadRes.success) {
        state.parsedData = reloadRes.parsed || {};
        if (state.viewMode === 'form') {
          renderCurrentView();
        }
      }
    } else {
      showToast('บันทึกไม่สำเร็จ: ' + (res.error || 'ไวยากรณ์ไม่ถูกต้อง'), 'error');
    }
  }

  async function reloadInGame() {
    elements.inGameReloadBtn.disabled = true;
    showToast('กำลังสั่งรีโหลดระบบทั้งหมดในเซิร์ฟเวอร์ Minecraft...', 'warning');

    const res = await apiFetch('/api/reload', { method: 'POST' });
    elements.inGameReloadBtn.disabled = false;

    if (res && res.success) {
      showToast('รีโหลดการตั้งค่าทั้งหมดในเกมสำเร็จแล้ว!', 'success');
    } else {
      showToast('เกิดข้อผิดพลาดในการรีโหลด: ' + (res.error || 'Server error'), 'error');
    }
  }

  // --- Rendering UI ---

  function setDirty(isDirty) {
    state.isDirty = isDirty;
    elements.dirtyIndicator.style.display = isDirty ? 'flex' : 'none';
  }

  function renderFilesDropdown() {
    elements.rawFileSelect.innerHTML = '';
    state.filesList.forEach(file => {
      const opt = document.createElement('option');
      opt.value = file.path;
      opt.textContent = `${file.path} (${file.category})`;
      elements.rawFileSelect.appendChild(opt);
    });
    elements.rawFileSelect.value = state.activeFile;
  }

  function renderDynamicNavList() {
    const knownPaths = new Set([
      'config.yml', 'items.yml', 'categories.yml', 'safe_cases.yml', 'lang/en.yml', 'lang/th.yml'
    ]);

    const otherFiles = state.filesList.filter(f => !knownPaths.has(f.path));
    elements.dynamicFilesSection.classList.remove('hidden');
    elements.dynamicFilesNavList.innerHTML = '';

    if (otherFiles.length > 0) {
      otherFiles.forEach(f => {
        const li = document.createElement('li');
        li.className = 'group flex items-center justify-between rounded-lg hover:bg-zinc-800/80 pr-2';
        li.innerHTML = `
          <a href="#file-${f.path}" class="nav-item flex-1 flex items-center justify-between px-3 py-2 text-xs font-medium text-zinc-400 group-hover:text-white transition" data-file="${f.path}">
            <div class="flex items-center gap-2.5 truncate mr-2">
              ${SVG_ICONS.box}
              <span class="truncate">${escapeHtml(f.name)}</span>
            </div>
            <span class="font-mono text-[10px] text-zinc-500 flex-shrink-0">${escapeHtml(f.path)}</span>
          </a>
          <button class="delete-file-btn p-1.5 rounded-lg text-zinc-600 hover:text-red-400 hover:bg-red-500/10 opacity-0 group-hover:opacity-100 transition flex-shrink-0 cursor-pointer" title="ลบไฟล์นี้">
            ${SVG_ICONS.trash}
          </button>
        `;

        li.querySelector('.delete-file-btn').addEventListener('click', (e) => {
          e.stopPropagation();
          showConfirmModal(
            `ลบไฟล์ "${f.path}"?`,
            `คุณแน่ใจหรือไม่ว่าต้องการลบไฟล์คอนฟิก "${f.path}" ออกจากเซิร์ฟเวอร์? การลบนี้จะมีผลทันทีและไม่สามารถกู้คืนได้`,
            async () => {
              const delRes = await apiFetch(`/api/config?file=${encodeURIComponent(f.path)}`, { method: 'DELETE' });
              if (delRes && delRes.success) {
                showToast(`ลบไฟล์ "${f.path}" สำเร็จแล้ว`, 'success');
                if (state.activeFile === f.path) {
                  await loadConfigFile('config.yml');
                }
                await loadFilesList();
              } else {
                showToast('ไม่สามารถลบไฟล์ได้: ' + (delRes.error || 'Server error'), 'error');
              }
            }
          );
        });

        elements.dynamicFilesNavList.appendChild(li);
      });
    } else {
      const emptyLi = document.createElement('li');
      emptyLi.className = 'px-3 py-1 text-[11px] text-zinc-600 italic';
      emptyLi.textContent = 'ไม่มีไฟล์คอนฟิกเพิ่มเติม';
      elements.dynamicFilesNavList.appendChild(emptyLi);
    }
  }

  function renderCurrentView() {
    if (state.viewMode === 'raw') {
      elements.formViewPanel.classList.add('hidden');
      elements.rawViewPanel.classList.remove('hidden');
      elements.rawYamlTextarea.value = state.rawYaml;
      updateLineNumbers();
      updateRawMeta();
      return;
    }

    elements.formViewPanel.classList.remove('hidden');
    elements.rawViewPanel.classList.add('hidden');

    if (state.activeFile === 'config.yml') {
      renderConfigYamlForm();
    } else if (state.activeFile === 'items.yml') {
      renderItemsRegistryView();
    } else if (state.activeFile === 'categories.yml') {
      renderCategoriesRegistryView();
    } else if (state.activeFile === 'safe_cases.yml') {
      renderSafeCasesRegistryView();
    } else if (state.activeFile.startsWith('lang/')) {
      renderLocalizationForm();
    } else {
      renderGenericOrRegistryForm();
    }
  }

  /**
   * Renders full-width grid of cards for config.yml
   */
  function renderConfigYamlForm() {
    const catKey = state.activeCategory;
    const catSchema = CONFIG_SCHEMA[catKey];

    if (!catSchema) {
      renderAllConfigCategories();
      return;
    }

    elements.categoryTitle.textContent = catSchema.title;
    elements.categoryDesc.textContent = catSchema.desc;
    elements.currentCategoryDisplay.textContent = catSchema.title;

    elements.formFieldsContainer.innerHTML = '';

    const grid = document.createElement('div');
    grid.className = 'w-full grid grid-cols-1 xl:grid-cols-2 gap-6';

    const card = document.createElement('div');
    card.className = 'w-full bg-zinc-900/90 border border-zinc-800/80 rounded-2xl p-6 shadow-xl space-y-4';

    const header = document.createElement('div');
    header.className = 'flex items-center justify-between pb-3 border-b border-zinc-800/80';
    header.innerHTML = `
      <div class="flex items-center gap-2.5">
        <span class="text-zinc-400">${SVG_ICONS[catKey] || SVG_ICONS.box}</span>
        <h3 class="text-sm font-bold text-white">${catSchema.title}</h3>
      </div>
      <span class="text-[11px] text-zinc-500 font-mono">${catSchema.fields.length} รายการ</span>
    `;
    card.appendChild(header);

    const fieldsWrapper = document.createElement('div');
    fieldsWrapper.className = 'space-y-4';

    catSchema.fields.forEach(field => {
      fieldsWrapper.appendChild(createSettingRow(field));
    });

    card.appendChild(fieldsWrapper);
    grid.appendChild(card);
    elements.formFieldsContainer.appendChild(grid);
  }

  function renderAllConfigCategories() {
    elements.categoryTitle.textContent = 'ภาพรวมไฟล์การตั้งค่าหลัก (Master Configuration)';
    elements.categoryDesc.textContent = 'แสดงหมวดหมู่ทั้งหมดในไฟล์ config.yml';
    elements.currentCategoryDisplay.textContent = 'ภาพรวมทั้งหมด';

    elements.formFieldsContainer.innerHTML = '';

    const grid = document.createElement('div');
    grid.className = 'w-full grid grid-cols-1 xl:grid-cols-2 2xl:grid-cols-3 gap-6';

    Object.keys(CONFIG_SCHEMA).forEach(catKey => {
      const schema = CONFIG_SCHEMA[catKey];
      const card = document.createElement('div');
      card.className = 'w-full bg-zinc-900/90 border border-zinc-800/80 rounded-2xl p-5 shadow-xl space-y-3';

      const header = document.createElement('div');
      header.className = 'flex items-center justify-between pb-3 border-b border-zinc-800/80';
      header.innerHTML = `
        <div class="flex items-center gap-2.5">
          <span class="text-zinc-400">${SVG_ICONS[catKey] || SVG_ICONS.box}</span>
          <h3 class="text-xs font-bold text-white">${schema.title}</h3>
        </div>
      `;
      card.appendChild(header);

      const fieldsWrapper = document.createElement('div');
      fieldsWrapper.className = 'space-y-3';
      schema.fields.forEach(field => {
        fieldsWrapper.appendChild(createSettingRow(field));
      });

      card.appendChild(fieldsWrapper);
      grid.appendChild(card);
    });

    elements.formFieldsContainer.appendChild(grid);
  }

  function createSettingRow(field) {
    const row = document.createElement('div');
    row.className = 'setting-row flex items-center justify-between py-2 border-b border-zinc-800/60 last:border-b-0 gap-4';
    row.dataset.key = field.key;

    const currentValue = getNestedValue(state.parsedData, field.key);
    const value = (currentValue !== undefined && currentValue !== null) ? currentValue : (field.default !== undefined ? field.default : '');

    row.innerHTML = `
      <div class="flex-1 min-w-0 pr-2">
        <div class="flex items-center gap-2 mb-0.5">
          <span class="text-xs font-semibold text-zinc-200">${escapeHtml(field.label)}</span>
          <span class="font-mono text-[10px] text-zinc-500 truncate max-w-[200px]" title="${field.key}">${escapeHtml(field.key)}</span>
        </div>
        ${field.desc ? `<div class="text-[11px] text-zinc-400 leading-tight">${escapeHtml(field.desc)}</div>` : ''}
      </div>
      <div class="setting-control flex-shrink-0 flex justify-end"></div>
    `;

    const ctrlContainer = row.querySelector('.setting-control');

    if (field.type === 'boolean') {
      const isChecked = Boolean(value);
      ctrlContainer.innerHTML = `
        <label class="switch">
          <input type="checkbox" ${isChecked ? 'checked' : ''} data-key="${field.key}">
          <span class="slider"></span>
        </label>
      `;
      ctrlContainer.querySelector('input').addEventListener('change', (e) => {
        updateSettingValue(field.key, e.target.checked);
      });
    } else if (field.type === 'select') {
      const select = document.createElement('select');
      select.className = 'bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs px-2.5 py-1.5 text-zinc-200 outline-none focus:border-white transition cursor-pointer min-w-[120px]';
      field.options.forEach(opt => {
        const optionEl = document.createElement('option');
        optionEl.value = opt;
        optionEl.textContent = opt;
        if (String(opt) === String(value)) optionEl.selected = true;
        select.appendChild(optionEl);
      });
      select.addEventListener('change', (e) => {
        updateSettingValue(field.key, e.target.value);
      });
      ctrlContainer.appendChild(select);
    } else if (field.type === 'number') {
      const input = document.createElement('input');
      input.type = 'number';
      input.className = 'bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono px-2.5 py-1.5 text-zinc-200 outline-none focus:border-white transition w-28 text-right';
      input.value = value;
      if (field.step) input.step = field.step;
      if (field.min !== undefined) input.min = field.min;
      if (field.max !== undefined) input.max = field.max;
      input.addEventListener('input', (e) => {
        const val = parseFloat(e.target.value);
        updateSettingValue(field.key, isNaN(val) ? 0 : val);
      });
      ctrlContainer.appendChild(input);
    } else {
      const input = document.createElement('input');
      input.type = 'text';
      input.className = 'bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs px-2.5 py-1.5 text-zinc-200 outline-none focus:border-white transition min-w-[160px]';
      input.value = value;
      input.addEventListener('input', (e) => {
        updateSettingValue(field.key, e.target.value);
      });
      ctrlContainer.appendChild(input);
    }

    return row;
  }

  /**
   * Dedicated View for items.yml with Add, Duplicate, Delete, and Full Edit capabilities
   */
  let itemSearchQuery = '';
  let itemRarityFilter = 'ALL';

  function renderItemsRegistryView() {
    elements.categoryTitle.textContent = 'ฐานข้อมูลไอเทมการสกัด (items.yml)';
    elements.categoryDesc.textContent = 'รายการไอเทมทั้งหมดที่สามารถสุ่มพบได้ในกล่องดรอป พร้อมมูลค่าความหายากและโอกาสออก';
    elements.currentCategoryDisplay.textContent = 'จัดการไอเทม';

    elements.formFieldsContainer.innerHTML = '';

    const itemsData = state.parsedData.items || {};
    let itemKeys = Object.keys(itemsData);

    // Filter items based on local search and rarity
    if (itemRarityFilter !== 'ALL') {
      itemKeys = itemKeys.filter(k => {
        const item = itemsData[k] || {};
        const r = (item.rarity || item.tier || 'COMMON').toUpperCase();
        return r === itemRarityFilter;
      });
    }

    if (itemSearchQuery.trim()) {
      const q = itemSearchQuery.trim().toLowerCase();
      itemKeys = itemKeys.filter(k => {
        const item = itemsData[k] || {};
        const name = (item.display_name || '').toLowerCase();
        const mat = (item.material || '').toLowerCase();
        return k.toLowerCase().includes(q) || name.includes(q) || mat.includes(q);
      });
    }

    // Action Header (Filters + Add Button)
    const actionHeader = document.createElement('div');
    actionHeader.className = 'w-full flex flex-wrap items-center justify-between gap-3 pb-4 mb-2';
    actionHeader.innerHTML = `
      <div class="flex items-center gap-3">
        <div class="text-xs text-zinc-400">
          ไอเทมทั้งหมด <span class="font-bold text-white">${Object.keys(itemsData).length}</span> รายการ
          ${itemKeys.length !== Object.keys(itemsData).length ? `<span class="text-zinc-500">(แสดงผล ${itemKeys.length})</span>` : ''}
        </div>
        <div class="flex items-center gap-2">
          <input type="text" id="itemLocalSearchInput" placeholder="ค้นหาไอเทมหรือ Material..." value="${escapeHtml(itemSearchQuery)}"
                 class="h-7 px-2.5 bg-zinc-950 border border-zinc-800 rounded-md text-xs text-zinc-200 placeholder-zinc-500 outline-none focus:border-zinc-500 w-44">
          <select id="itemRarityFilterSelect" class="h-7 px-2 bg-zinc-950 border border-zinc-800 rounded-md text-xs text-zinc-300 outline-none focus:border-zinc-500 cursor-pointer">
            <option value="ALL" ${itemRarityFilter === 'ALL' ? 'selected' : ''}>ทุกระดับความหายาก</option>
            <option value="COMMON" ${itemRarityFilter === 'COMMON' ? 'selected' : ''}>COMMON</option>
            <option value="UNCOMMON" ${itemRarityFilter === 'UNCOMMON' ? 'selected' : ''}>UNCOMMON</option>
            <option value="RARE" ${itemRarityFilter === 'RARE' ? 'selected' : ''}>RARE</option>
            <option value="EPIC" ${itemRarityFilter === 'EPIC' ? 'selected' : ''}>EPIC</option>
            <option value="LEGENDARY" ${itemRarityFilter === 'LEGENDARY' ? 'selected' : ''}>LEGENDARY</option>
          </select>
        </div>
      </div>
      <button id="addNewItemBtn" class="px-3.5 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-medium text-xs flex items-center gap-1.5 shadow-sm transition active:scale-95 cursor-pointer">
        ${SVG_ICONS.plus}
        <span>เพิ่มไอเทมใหม่</span>
      </button>
    `;
    elements.formFieldsContainer.appendChild(actionHeader);

    actionHeader.querySelector('#addNewItemBtn').addEventListener('click', () => openAddItemModal());
    actionHeader.querySelector('#itemLocalSearchInput').addEventListener('input', (e) => {
      itemSearchQuery = e.target.value;
      renderItemsRegistryView();
    });
    actionHeader.querySelector('#itemRarityFilterSelect').addEventListener('change', (e) => {
      itemRarityFilter = e.target.value;
      renderItemsRegistryView();
    });

    if (itemKeys.length === 0) {
      const emptyBox = document.createElement('div');
      emptyBox.className = 'bg-zinc-900 border border-zinc-800 rounded-2xl p-12 text-center text-zinc-500';
      emptyBox.innerHTML = `ไม่พบไอเทมที่ตรงตามเงื่อนไข กดปุ่ม "เพิ่มไอเทมใหม่" ด้านบนเพื่อเริ่มสร้าง`;
      elements.formFieldsContainer.appendChild(emptyBox);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'w-full grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-6';

    itemKeys.forEach(itemId => {
      const item = itemsData[itemId] || {};
      const card = document.createElement('div');
      card.className = 'bg-zinc-900/90 border border-zinc-800 hover:border-zinc-700/80 rounded-2xl p-5 shadow-xl transition space-y-4';

      const rarity = (item.rarity || item.tier || 'COMMON').toUpperCase();
      const rarityBadgeColor = {
        COMMON: 'bg-zinc-800 text-zinc-300 border-zinc-700',
        UNCOMMON: 'bg-emerald-950/60 text-emerald-400 border-emerald-800',
        RARE: 'bg-blue-950/60 text-blue-400 border-blue-800',
        EPIC: 'bg-purple-950/60 text-purple-400 border-purple-800',
        LEGENDARY: 'bg-amber-950/60 text-amber-400 border-amber-800'
      }[rarity] || 'bg-zinc-800 text-zinc-300 border-zinc-700';

      const allowedContStr = Array.isArray(item.allowed_containers)
        ? item.allowed_containers.join(', ')
        : (item.allowed_containers || '');

      const loreStr = Array.isArray(item.lore)
        ? item.lore.join('\n')
        : (item.lore || '');

      card.innerHTML = `
        <div class="flex items-center justify-between pb-3 border-b border-zinc-800">
          <div class="flex items-center gap-2 min-w-0">
            <span class="p-1.5 rounded-lg bg-zinc-800 text-indigo-400 flex-shrink-0">${SVG_ICONS.box}</span>
            <div class="min-w-0">
              <div class="font-bold text-xs text-white truncate" title="${itemId}">${escapeHtml(itemId)}</div>
              <div class="text-[10px] font-mono text-zinc-500 truncate">${escapeHtml(item.material || 'STONE')}</div>
            </div>
          </div>
          <div class="flex items-center gap-1.5 flex-shrink-0">
            <span class="text-[10px] font-bold px-2 py-0.5 rounded-full border ${rarityBadgeColor}">
              ${escapeHtml(rarity)}
            </span>
            <button class="duplicate-item-btn p-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-400 hover:text-white border border-zinc-700/60 transition cursor-pointer" title="คัดลอกไอเทมนี้">
              ${SVG_ICONS.copy}
            </button>
            <button class="delete-item-btn p-1.5 rounded-lg bg-zinc-800 hover:bg-red-500/20 text-zinc-500 hover:text-red-400 border border-zinc-700/60 hover:border-red-500/40 transition cursor-pointer" title="ลบไอเทมนี้">
              ${SVG_ICONS.trash}
            </button>
          </div>
        </div>

        <div class="space-y-3">
          <div>
            <label class="text-[11px] font-medium text-zinc-400 block mb-1">ชื่อไอเทม (Display Name):</label>
            <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition item-display-name" value="${escapeHtml(item.display_name || itemId)}">
          </div>

          <div class="grid grid-cols-3 gap-2">
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">ราคา ($):</label>
              <input type="number" step="10" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition item-price" value="${item.price || item.value || 0}">
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">น้ำหนักดรอป:</label>
              <input type="number" step="1" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition item-weight" value="${item.weight || 10}">
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">เวลาค้นหา (s):</label>
              <input type="number" min="0" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition item-search-time" value="${item.search_time || 2}">
            </div>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">ชนิดบล็อก (Material):</label>
              <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition item-material" value="${escapeHtml(item.material || 'STONE')}">
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">ระดับความหายาก (Rarity):</label>
              <select class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition item-rarity cursor-pointer">
                ${['COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY'].map(r => `<option value="${r}" ${r === rarity ? 'selected' : ''}>${r}</option>`).join('')}
              </select>
            </div>
          </div>

          <div>
            <label class="text-[11px] font-medium text-zinc-400 block mb-1">กล่องที่อนุญาตให้ดรอป (คั่นด้วยจุลภาค):</label>
            <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition item-containers" value="${escapeHtml(allowedContStr)}" placeholder="เช่น normal_crate, airdrop">
          </div>

          <div>
            <label class="text-[11px] font-medium text-zinc-400 block mb-1">คำอธิบายใต้ไอเทม (Lore):</label>
            <textarea rows="2" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition item-lore leading-relaxed resize-y" placeholder="1 บรรทัดต่อ 1 ข้อความ">${escapeHtml(loreStr)}</textarea>
          </div>
        </div>
      `;

      card.querySelector('.item-display-name').addEventListener('input', (e) => {
        updateSettingValue(`items.${itemId}.display_name`, e.target.value);
      });
      card.querySelector('.item-price').addEventListener('input', (e) => {
        const val = parseFloat(e.target.value) || 0;
        updateSettingValue(`items.${itemId}.price`, val);
        updateSettingValue(`items.${itemId}.value`, val);
      });
      card.querySelector('.item-weight').addEventListener('input', (e) => {
        updateSettingValue(`items.${itemId}.weight`, parseFloat(e.target.value) || 1);
      });
      card.querySelector('.item-search-time').addEventListener('input', (e) => {
        updateSettingValue(`items.${itemId}.search_time`, parseInt(e.target.value, 10) || 1);
      });
      card.querySelector('.item-material').addEventListener('input', (e) => {
        updateSettingValue(`items.${itemId}.material`, e.target.value.toUpperCase().trim());
      });
      card.querySelector('.item-rarity').addEventListener('change', (e) => {
        updateSettingValue(`items.${itemId}.rarity`, e.target.value);
        updateSettingValue(`items.${itemId}.tier`, e.target.value);
      });
      card.querySelector('.item-containers').addEventListener('input', (e) => {
        const arr = e.target.value.split(',').map(s => s.trim()).filter(Boolean);
        updateSettingValue(`items.${itemId}.allowed_containers`, arr);
      });
      card.querySelector('.item-lore').addEventListener('input', (e) => {
        const lines = e.target.value.split('\n').map(l => l.trim()).filter(Boolean);
        updateSettingValue(`items.${itemId}.lore`, lines);
      });

      // Duplicate item button
      card.querySelector('.duplicate-item-btn').addEventListener('click', () => {
        openAddItemModal(item, itemId + '_copy');
      });

      // Delete item button
      card.querySelector('.delete-item-btn').addEventListener('click', () => {
        showConfirmModal(
          `ลบไอเทม "${itemId}"?`,
          `คุณแน่ใจหรือไม่ว่าต้องการลบไอเทม "${itemId}" ออกจากระบบ? การเปลี่ยนแปลงจะมีผลเมื่อกด "บันทึกและใช้งาน"`,
          () => {
            delete state.parsedData.items[itemId];
            state.rawYaml = stringifyYaml(state.parsedData);
            elements.rawYamlTextarea.value = state.rawYaml;
            setDirty(true);
            renderItemsRegistryView();
            showToast(`ลบไอเทม "${itemId}" เรียบร้อยแล้ว (กดบันทึกเพื่อนำไปใช้งาน)`, 'success');
          }
        );
      });

      grid.appendChild(card);
    });

    elements.formFieldsContainer.appendChild(grid);
  }

  /**
   * Dedicated View for categories.yml with Add, Duplicate, Delete, and Rarity Chances
   */
  function renderCategoriesRegistryView() {
    elements.categoryTitle.textContent = 'หมวดหมู่กล่องคอนเทนเนอร์ (categories.yml)';
    elements.categoryDesc.textContent = 'กำหนดอัตราการสุ่มดรอป จำนวนชิ้น และเวลาค้นหาสำหรับกล่องแต่ละประเภท';
    elements.currentCategoryDisplay.textContent = 'หมวดหมู่กล่องดรอป';

    elements.formFieldsContainer.innerHTML = '';

    // Action Header (Add Button)
    const actionHeader = document.createElement('div');
    actionHeader.className = 'w-full flex items-center justify-between pb-4 mb-2';
    actionHeader.innerHTML = `
      <div class="text-xs text-zinc-400">
        มีหมวดหมู่ทั้งหมด <span class="font-bold text-white">${Object.keys(state.parsedData.categories || {}).length}</span> หมวดหมู่
      </div>
      <button id="addNewCategoryBtn" class="px-3.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-xs flex items-center gap-1.5 shadow-sm transition active:scale-95 cursor-pointer">
        ${SVG_ICONS.plus}
        <span>เพิ่มหมวดหมู่ใหม่</span>
      </button>
    `;
    elements.formFieldsContainer.appendChild(actionHeader);

    actionHeader.querySelector('#addNewCategoryBtn').addEventListener('click', () => openAddCategoryModal());

    const catData = state.parsedData.categories || {};
    const catKeys = Object.keys(catData);

    if (catKeys.length === 0) {
      const emptyBox = document.createElement('div');
      emptyBox.className = 'bg-zinc-900 border border-zinc-800 rounded-2xl p-12 text-center text-zinc-500';
      emptyBox.innerHTML = `ยังไม่มีหมวดหมู่กล่อง กดปุ่ม "เพิ่มหมวดหมู่ใหม่" ด้านบนเพื่อเริ่มสร้าง`;
      elements.formFieldsContainer.appendChild(emptyBox);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'w-full grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-6';

    catKeys.forEach(catId => {
      const cat = catData[catId] || {};
      const card = document.createElement('div');
      card.className = 'bg-zinc-900/90 border border-zinc-800 hover:border-zinc-700/80 rounded-2xl p-5 shadow-xl transition space-y-4';

      card.innerHTML = `
        <div class="flex items-center justify-between pb-3 border-b border-zinc-800">
          <div class="flex items-center gap-2 min-w-0">
            <span class="p-1.5 rounded-lg bg-zinc-800 text-emerald-400 flex-shrink-0">${SVG_ICONS.box}</span>
            <div class="min-w-0">
              <div class="font-bold text-xs text-white truncate" title="${catId}">${escapeHtml(catId)}</div>
              <div class="text-[10px] text-zinc-400 font-mono truncate">${escapeHtml(cat.name || catId)}</div>
            </div>
          </div>
          <div class="flex items-center gap-1.5 flex-shrink-0">
            <span class="w-3.5 h-3.5 rounded-full border border-zinc-700 shadow-sm" style="background-color: ${cat.color || '#AAAAAA'}"></span>
            <button class="duplicate-cat-btn p-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-400 hover:text-white border border-zinc-700/60 transition cursor-pointer" title="คัดลอกหมวดหมู่นี้">
              ${SVG_ICONS.copy}
            </button>
            <button class="delete-cat-btn p-1.5 rounded-lg bg-zinc-800 hover:bg-red-500/20 text-zinc-500 hover:text-red-400 border border-zinc-700/60 hover:border-red-500/40 transition cursor-pointer" title="ลบหมวดหมู่นี้">
              ${SVG_ICONS.trash}
            </button>
          </div>
        </div>

        <div class="space-y-3">
          <div>
            <label class="text-[11px] font-medium text-zinc-400 block mb-1">ชื่อกล่องที่แสดงผล (Display Name):</label>
            <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition cat-display-name" value="${escapeHtml(cat.display_name || cat.name || catId)}">
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">สีประจำกล่อง (Color Hex):</label>
              <div class="flex items-center gap-2">
                <input type="color" class="w-8 h-8 rounded bg-zinc-950 border border-zinc-800 outline-none cursor-pointer cat-color-picker" value="${cat.color || '#AAAAAA'}">
                <input type="text" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white cat-color-text" value="${escapeHtml(cat.color || '#AAAAAA')}">
              </div>
            </div>
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="text-[11px] font-medium text-zinc-400 block mb-1">จำนวน Min:</label>
                <input type="number" min="1" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white cat-min-rolls" value="${cat.min_rolls || 1}">
              </div>
              <div>
                <label class="text-[11px] font-medium text-zinc-400 block mb-1">จำนวน Max:</label>
                <input type="number" min="1" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white cat-max-rolls" value="${cat.max_rolls || 4}">
              </div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">เวลาค้นหาต่ำสุด (s):</label>
              <input type="number" min="0" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition cat-search-min" value="${cat.search_time_min || 2}">
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">เวลาค้นหาสูงสุด (s):</label>
              <input type="number" min="1" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition cat-search-max" value="${cat.search_time_max || 4}">
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">บล็อกปิดกล่อง (Covered):</label>
              <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition cat-covered" value="${escapeHtml(cat.covered_material || 'BLACK_STAINED_GLASS_PANE')}">
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">บล็อกค้นหา (Searching):</label>
              <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition cat-searching" value="${escapeHtml(cat.searching_material || 'STONE')}">
            </div>
          </div>

          <div class="pt-2 border-t border-zinc-800/80">
            <label class="text-[11px] font-semibold text-zinc-300 block mb-1.5">โอกาสออกของระดับความหายาก (Rarity Chances %):</label>
            <div class="grid grid-cols-5 gap-1.5 text-center">
              <div>
                <span class="text-[10px] text-zinc-400 block mb-0.5 font-bold">COM</span>
                <input type="number" step="1" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white cat-chance-common" value="${(cat.rarity_chances && cat.rarity_chances.COMMON !== undefined) ? cat.rarity_chances.COMMON : 0}">
              </div>
              <div>
                <span class="text-[10px] text-emerald-400 block mb-0.5 font-bold">UNC</span>
                <input type="number" step="1" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white cat-chance-uncommon" value="${(cat.rarity_chances && cat.rarity_chances.UNCOMMON !== undefined) ? cat.rarity_chances.UNCOMMON : 0}">
              </div>
              <div>
                <span class="text-[10px] text-blue-400 block mb-0.5 font-bold">RARE</span>
                <input type="number" step="1" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white cat-chance-rare" value="${(cat.rarity_chances && cat.rarity_chances.RARE !== undefined) ? cat.rarity_chances.RARE : 0}">
              </div>
              <div>
                <span class="text-[10px] text-purple-400 block mb-0.5 font-bold">EPIC</span>
                <input type="number" step="1" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white cat-chance-epic" value="${(cat.rarity_chances && cat.rarity_chances.EPIC !== undefined) ? cat.rarity_chances.EPIC : 0}">
              </div>
              <div>
                <span class="text-[10px] text-amber-400 block mb-0.5 font-bold">LEG</span>
                <input type="number" step="1" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white cat-chance-legendary" value="${(cat.rarity_chances && cat.rarity_chances.LEGENDARY !== undefined) ? cat.rarity_chances.LEGENDARY : 0}">
              </div>
            </div>
          </div>
        </div>
      `;

      card.querySelector('.cat-display-name').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.display_name`, e.target.value);
      });
      card.querySelector('.cat-color-picker').addEventListener('input', (e) => {
        card.querySelector('.cat-color-text').value = e.target.value;
        updateSettingValue(`categories.${catId}.color`, e.target.value);
      });
      card.querySelector('.cat-color-text').addEventListener('input', (e) => {
        card.querySelector('.cat-color-picker').value = e.target.value;
        updateSettingValue(`categories.${catId}.color`, e.target.value);
      });
      card.querySelector('.cat-min-rolls').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.min_rolls`, parseInt(e.target.value, 10) || 1);
      });
      card.querySelector('.cat-max-rolls').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.max_rolls`, parseInt(e.target.value, 10) || 1);
      });
      card.querySelector('.cat-search-min').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.search_time_min`, parseInt(e.target.value, 10) || 1);
      });
      card.querySelector('.cat-search-max').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.search_time_max`, parseInt(e.target.value, 10) || 1);
      });
      card.querySelector('.cat-covered').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.covered_material`, e.target.value.toUpperCase().trim());
      });
      card.querySelector('.cat-searching').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.searching_material`, e.target.value.toUpperCase().trim());
      });
      card.querySelector('.cat-chance-common').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.COMMON`, parseFloat(e.target.value) || 0);
      });
      card.querySelector('.cat-chance-uncommon').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.UNCOMMON`, parseFloat(e.target.value) || 0);
      });
      card.querySelector('.cat-chance-rare').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.RARE`, parseFloat(e.target.value) || 0);
      });
      card.querySelector('.cat-chance-epic').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.EPIC`, parseFloat(e.target.value) || 0);
      });
      card.querySelector('.cat-chance-legendary').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.LEGENDARY`, parseFloat(e.target.value) || 0);
      });

      // Duplicate category button
      card.querySelector('.duplicate-cat-btn').addEventListener('click', () => {
        openAddCategoryModal(cat, catId + '_copy');
      });

      // Delete category button
      card.querySelector('.delete-cat-btn').addEventListener('click', () => {
        showConfirmModal(
          `ลบหมวดหมู่ "${catId}"?`,
          `คุณแน่ใจหรือไม่ว่าต้องการลบหมวดหมู่กล่อง "${catId}"? การเปลี่ยนแปลงจะมีผลเมื่อกด "บันทึกและใช้งาน"`,
          () => {
            delete state.parsedData.categories[catId];
            state.rawYaml = stringifyYaml(state.parsedData);
            elements.rawYamlTextarea.value = state.rawYaml;
            setDirty(true);
            renderCategoriesRegistryView();
            showToast(`ลบหมวดหมู่ "${catId}" เรียบร้อยแล้ว (กดบันทึกเพื่อนำไปใช้งาน)`, 'success');
          }
        );
      });

      grid.appendChild(card);
    });

    elements.formFieldsContainer.appendChild(grid);
  }

  /**
   * Dedicated View for safe_cases.yml with Add, Duplicate, Delete, and Full Edit capabilities
   */
  function renderSafeCasesRegistryView() {
    elements.categoryTitle.textContent = 'กระเป๋านิรภัย (safe_cases.yml)';
    elements.categoryDesc.textContent = 'กระเป๋าเก็บของติดตัวที่จะไม่หล่นเมื่อเสียชีวิตในสนามรบ';
    elements.currentCategoryDisplay.textContent = 'กระเป๋านิรภัย';

    elements.formFieldsContainer.innerHTML = '';

    // Action Header (Add Button)
    const actionHeader = document.createElement('div');
    actionHeader.className = 'w-full flex items-center justify-between pb-4 mb-2';
    actionHeader.innerHTML = `
      <div class="text-xs text-zinc-400">
        มีกระเป๋านิรภัยทั้งหมด <span class="font-bold text-white">${Object.keys(state.parsedData.safe_cases || {}).length}</span> ใบ
      </div>
      <button id="addNewCaseBtn" class="px-3.5 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 text-white font-medium text-xs flex items-center gap-1.5 shadow-sm transition active:scale-95 cursor-pointer">
        ${SVG_ICONS.plus}
        <span>เพิ่มกระเป๋านิรภัยใหม่</span>
      </button>
    `;
    elements.formFieldsContainer.appendChild(actionHeader);

    actionHeader.querySelector('#addNewCaseBtn').addEventListener('click', () => openAddSafeCaseModal());

    const caseData = state.parsedData.safe_cases || {};
    const caseKeys = Object.keys(caseData);

    if (caseKeys.length === 0) {
      const emptyBox = document.createElement('div');
      emptyBox.className = 'bg-zinc-900 border border-zinc-800 rounded-2xl p-12 text-center text-zinc-500';
      emptyBox.innerHTML = `ยังไม่มีกระเป๋านิรภัย กดปุ่ม "เพิ่มกระเป๋านิรภัยใหม่" ด้านบนเพื่อเริ่มสร้าง`;
      elements.formFieldsContainer.appendChild(emptyBox);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'w-full grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-6';

    caseKeys.forEach(caseId => {
      const c = caseData[caseId] || {};
      const card = document.createElement('div');
      card.className = 'bg-zinc-900/90 border border-zinc-800 hover:border-zinc-700/80 rounded-2xl p-5 shadow-xl transition space-y-4';

      card.innerHTML = `
        <div class="flex items-center justify-between pb-3 border-b border-zinc-800">
          <div class="flex items-center gap-2 min-w-0">
            <span class="p-1.5 rounded-lg bg-zinc-800 text-sky-400 flex-shrink-0">${SVG_ICONS.safe_cases_settings}</span>
            <div class="min-w-0">
              <div class="font-bold text-xs text-white truncate" title="${caseId}">${escapeHtml(caseId)}</div>
              <div class="text-[10px] text-zinc-500 font-mono truncate">${escapeHtml(c.material || 'BUNDLE')}</div>
            </div>
          </div>
          <div class="flex items-center gap-1.5 flex-shrink-0">
            <span class="text-[10px] font-semibold px-2 py-0.5 rounded bg-zinc-800 border border-zinc-700 text-zinc-300">
              ${c.capacity || 9} ช่อง
            </span>
            <button class="duplicate-case-btn p-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-400 hover:text-white border border-zinc-700/60 transition cursor-pointer" title="คัดลอกกระเป๋านี้">
              ${SVG_ICONS.copy}
            </button>
            <button class="delete-case-btn p-1.5 rounded-lg bg-zinc-800 hover:bg-red-500/20 text-zinc-500 hover:text-red-400 border border-zinc-700/60 hover:border-red-500/40 transition cursor-pointer" title="ลบกระเป๋านี้">
              ${SVG_ICONS.trash}
            </button>
          </div>
        </div>

        <div class="space-y-3">
          <div>
            <label class="text-[11px] font-medium text-zinc-400 block mb-1">ชื่อกระเป๋า (Title / Display Name):</label>
            <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition case-display-name" value="${escapeHtml(c.display_name || c.title || caseId)}">
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">ความจุช่อง (Capacity):</label>
              <input type="number" min="1" max="54" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition case-capacity" value="${c.capacity || 9}">
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">ไอเทม (Material):</label>
              <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition case-material" value="${escapeHtml(c.material || 'BUNDLE')}">
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3 items-center">
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">CustomModelData:</label>
              <input type="number" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition case-cmd" value="${c.custom_model_data || 0}">
            </div>
            <div class="pt-4">
              <label class="flex items-center gap-2 cursor-pointer select-none">
                <input type="checkbox" class="case-keeps rounded bg-zinc-950 border-zinc-700 text-sky-500 focus:ring-0 cursor-pointer" ${c.keeps_on_death !== false ? 'checked' : ''}>
                <span class="text-xs text-zinc-300 font-medium">ของไม่ตกเมื่อตาย</span>
              </label>
            </div>
          </div>
        </div>
      `;

      card.querySelector('.case-display-name').addEventListener('input', (e) => {
        updateSettingValue(`safe_cases.${caseId}.display_name`, e.target.value);
        updateSettingValue(`safe_cases.${caseId}.title`, e.target.value);
      });
      card.querySelector('.case-capacity').addEventListener('input', (e) => {
        updateSettingValue(`safe_cases.${caseId}.capacity`, parseInt(e.target.value, 10) || 9);
      });
      card.querySelector('.case-material').addEventListener('input', (e) => {
        updateSettingValue(`safe_cases.${caseId}.material`, e.target.value.toUpperCase().trim());
      });
      card.querySelector('.case-cmd').addEventListener('input', (e) => {
        updateSettingValue(`safe_cases.${caseId}.custom_model_data`, parseInt(e.target.value, 10) || 0);
      });
      card.querySelector('.case-keeps').addEventListener('change', (e) => {
        updateSettingValue(`safe_cases.${caseId}.keeps_on_death`, e.target.checked);
      });

      // Duplicate case button
      card.querySelector('.duplicate-case-btn').addEventListener('click', () => {
        openAddSafeCaseModal(c, caseId + '_copy');
      });

      // Delete case button
      card.querySelector('.delete-case-btn').addEventListener('click', () => {
        showConfirmModal(
          `ลบกระเป๋านิรภัย "${caseId}"?`,
          `คุณแน่ใจหรือไม่ว่าต้องการลบกระเป๋านิรภัย "${caseId}"? การเปลี่ยนแปลงจะมีผลเมื่อกด "บันทึกและใช้งาน"`,
          () => {
            delete state.parsedData.safe_cases[caseId];
            state.rawYaml = stringifyYaml(state.parsedData);
            elements.rawYamlTextarea.value = state.rawYaml;
            setDirty(true);
            renderSafeCasesRegistryView();
            showToast(`ลบกระเป๋านิรภัย "${caseId}" เรียบร้อยแล้ว (กดบันทึกเพื่อนำไปใช้งาน)`, 'success');
          }
        );
      });

      grid.appendChild(card);
    });

    elements.formFieldsContainer.appendChild(grid);
  }

  /**
   * Dedicated View for Localization (lang/th.yml and lang/en.yml) with Add, Delete, and Search capabilities
   */
  let langKeySearchQuery = '';

  function renderLocalizationForm() {
    const isThai = state.activeFile.endsWith('th.yml');
    elements.categoryTitle.textContent = isThai ? 'ภาษาไทย (lang/th.yml)' : 'ภาษาอังกฤษ (lang/en.yml)';
    elements.categoryDesc.textContent = 'แก้ไขข้อความแจ้งเตือน คำนำหน้า และข้อความในเมนูทั้งหมด รองรับ MiniMessage (<color:#HEX>, <gradient:...>)';
    elements.currentCategoryDisplay.textContent = 'ข้อความภาษา';

    elements.formFieldsContainer.innerHTML = '';

    const flatKeys = flattenObject(state.parsedData);
    let keyList = Object.keys(flatKeys);

    if (langKeySearchQuery.trim()) {
      const q = langKeySearchQuery.trim().toLowerCase();
      keyList = keyList.filter(k => k.toLowerCase().includes(q) || String(flatKeys[k]).toLowerCase().includes(q));
    }

    // Action Header (Search + Add Button)
    const actionHeader = document.createElement('div');
    actionHeader.className = 'w-full flex flex-wrap items-center justify-between gap-3 pb-4 mb-2';
    actionHeader.innerHTML = `
      <div class="flex items-center gap-3">
        <div class="text-xs text-zinc-400">
          ข้อความทั้งหมด <span class="font-bold text-white">${Object.keys(flatKeys).length}</span> รายการ
          ${keyList.length !== Object.keys(flatKeys).length ? `<span class="text-zinc-500">(แสดงผล ${keyList.length})</span>` : ''}
        </div>
        <input type="text" id="langLocalSearchInput" placeholder="ค้นหาคีย์ข้อความหรือเนื้อหา..." value="${escapeHtml(langKeySearchQuery)}"
               class="h-7 px-2.5 bg-zinc-950 border border-zinc-800 rounded-md text-xs text-zinc-200 placeholder-zinc-500 outline-none focus:border-zinc-500 w-52">
      </div>
      <button id="addNewKeyBtn" class="px-3.5 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-500 text-white font-medium text-xs flex items-center gap-1.5 shadow-sm transition active:scale-95 cursor-pointer">
        ${SVG_ICONS.plus}
        <span>เพิ่มข้อความใหม่</span>
      </button>
    `;
    elements.formFieldsContainer.appendChild(actionHeader);

    actionHeader.querySelector('#addNewKeyBtn').addEventListener('click', openAddKeyModal);
    actionHeader.querySelector('#langLocalSearchInput').addEventListener('input', (e) => {
      langKeySearchQuery = e.target.value;
      renderLocalizationForm();
    });

    const container = document.createElement('div');
    container.className = 'w-full bg-zinc-900/90 border border-zinc-800 rounded-2xl shadow-xl overflow-hidden';

    const table = document.createElement('table');
    table.className = 'w-full border-collapse text-left';
    table.innerHTML = `
      <thead>
        <tr class="bg-zinc-850 border-b border-zinc-800 text-[11px] font-semibold text-zinc-400 uppercase tracking-wider">
          <th class="py-3 px-5 w-1/3">คีย์ข้อความ (Translation Key)</th>
          <th class="py-3 px-5">ข้อความที่แสดงผล (Message Content)</th>
          <th class="py-3 px-4 w-12 text-center">ลบ</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-zinc-800/60"></tbody>
    `;

    const tbody = table.querySelector('tbody');

    if (keyList.length === 0) {
      tbody.innerHTML = '<tr><td colspan="3" class="py-12 text-center text-zinc-500 text-xs">ไม่พบรายการข้อความที่ตรงกับการค้นหา</td></tr>';
    } else {
      keyList.forEach(k => {
        const val = flatKeys[k];
        const tr = document.createElement('tr');
        tr.className = 'hover:bg-zinc-800/40 transition';
        tr.dataset.key = k;
        tr.innerHTML = `
          <td class="py-2.5 px-5 align-middle">
            <span class="font-mono text-xs text-zinc-300 select-all">${escapeHtml(k)}</span>
          </td>
          <td class="py-2.5 px-5 align-middle">
            <input type="text" class="w-full px-3 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition lang-input" value="${escapeHtml(String(val))}" data-key="${k}">
          </td>
          <td class="py-2.5 px-4 align-middle text-center">
            <button class="delete-key-btn p-1.5 rounded-lg bg-zinc-800 hover:bg-red-500/20 text-zinc-500 hover:text-red-400 border border-zinc-700/60 hover:border-red-500/40 transition cursor-pointer" title="ลบข้อความนี้">
              ${SVG_ICONS.trash}
            </button>
          </td>
        `;

        tr.querySelector('.lang-input').addEventListener('input', (e) => {
          updateSettingValue(k, e.target.value);
        });

        tr.querySelector('.delete-key-btn').addEventListener('click', () => {
          showConfirmModal(
            `ลบคีย์ "${k}"?`,
            `คุณแน่ใจหรือไม่ว่าต้องการลบข้อความ "${k}" ออกจากไฟล์ภาษานี้?`,
            () => {
              deleteNestedKey(state.parsedData, k);
              state.rawYaml = stringifyYaml(state.parsedData);
              elements.rawYamlTextarea.value = state.rawYaml;
              setDirty(true);
              renderLocalizationForm();
              showToast(`ลบคีย์ "${k}" เรียบร้อยแล้ว`, 'success');
            }
          );
        });

        tbody.appendChild(tr);
      });
    }

    container.appendChild(table);
    elements.formFieldsContainer.appendChild(container);
  }

  /**
   * Generic YAML Form View for custom files with Add & Delete Key support
   */
  function renderGenericOrRegistryForm() {
    elements.categoryTitle.textContent = `ไฟล์การตั้งค่า (${escapeHtml(state.activeFile)})`;
    elements.categoryDesc.textContent = 'แก้ไขค่าพารามิเตอร์ทั้งหมดในไฟล์ หรือสลับไปแท็บ Raw YAML เพื่อแก้ไขโครงสร้างแบบอิสระ';
    elements.currentCategoryDisplay.textContent = state.activeFile;

    elements.formFieldsContainer.innerHTML = '';

    // Action Header (Add Key Button)
    const actionHeader = document.createElement('div');
    actionHeader.className = 'w-full flex items-center justify-between pb-4 mb-2';
    actionHeader.innerHTML = `
      <div class="text-xs text-zinc-400">
        โครงสร้างไฟล์ <span class="font-mono text-zinc-200">${escapeHtml(state.activeFile)}</span>
      </div>
      <button id="addGenericKeyBtn" class="px-3.5 py-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-white font-medium text-xs flex items-center gap-1.5 border border-zinc-700 shadow-sm transition active:scale-95 cursor-pointer">
        ${SVG_ICONS.plus}
        <span>เพิ่มคีย์การตั้งค่าใหม่</span>
      </button>
    `;
    elements.formFieldsContainer.appendChild(actionHeader);
    actionHeader.querySelector('#addGenericKeyBtn').addEventListener('click', openAddKeyModal);

    const flat = flattenObject(state.parsedData);
    const keys = Object.keys(flat);

    if (keys.length === 0) {
      const emptyBox = document.createElement('div');
      emptyBox.className = 'bg-zinc-900 border border-zinc-800 rounded-2xl p-12 text-center text-zinc-500';
      emptyBox.innerHTML = `ไฟล์นี้ยังไม่มีข้อมูลการตั้งค่า กดปุ่ม "เพิ่มคีย์การตั้งค่าใหม่" ด้านบน หรือสลับไปแท็บแก้ไขโค้ด YAML โดยตรง`;
      elements.formFieldsContainer.appendChild(emptyBox);
      return;
    }

    const card = document.createElement('div');
    card.className = 'w-full bg-zinc-900/90 border border-zinc-800 rounded-2xl shadow-xl overflow-hidden';
    const table = document.createElement('table');
    table.className = 'w-full border-collapse text-left';
    table.innerHTML = `
      <thead>
        <tr class="bg-zinc-850 border-b border-zinc-800 text-[11px] font-semibold text-zinc-400 uppercase tracking-wider">
          <th class="py-3 px-5 w-1/3">เส้นทางคีย์ (Configuration Key Path)</th>
          <th class="py-3 px-5">ค่าที่ตั้งไว้ (Value)</th>
          <th class="py-3 px-4 w-12 text-center">ลบ</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-zinc-800/60"></tbody>
    `;
    const tbody = table.querySelector('tbody');

    keys.forEach(k => {
      const val = flat[k];
      const tr = document.createElement('tr');
      tr.className = 'hover:bg-zinc-800/40 transition';
      tr.innerHTML = `
        <td class="py-2.5 px-5 align-middle">
          <span class="font-mono text-xs text-zinc-300 select-all">${escapeHtml(k)}</span>
        </td>
        <td class="py-2.5 px-5 align-middle">
          <input type="text" class="w-full px-3 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition generic-val-input" value="${escapeHtml(String(val))}">
        </td>
        <td class="py-2.5 px-4 align-middle text-center">
          <button class="delete-generic-key-btn p-1.5 rounded-lg bg-zinc-800 hover:bg-red-500/20 text-zinc-500 hover:text-red-400 border border-zinc-700/60 hover:border-red-500/40 transition cursor-pointer" title="ลบคีย์นี้">
            ${SVG_ICONS.trash}
          </button>
        </td>
      `;

      tr.querySelector('.generic-val-input').addEventListener('input', (e) => {
        let v = e.target.value;
        if (v === 'true') v = true;
        else if (v === 'false') v = false;
        else if (!isNaN(Number(v)) && v.trim() !== '') v = Number(v);
        updateSettingValue(k, v);
      });

      tr.querySelector('.delete-generic-key-btn').addEventListener('click', () => {
        showConfirmModal(
          `ลบคีย์ "${k}"?`,
          `คุณแน่ใจหรือไม่ว่าต้องการลบการตั้งค่า "${k}" ออกจากไฟล์ "${state.activeFile}"?`,
          () => {
            deleteNestedKey(state.parsedData, k);
            state.rawYaml = stringifyYaml(state.parsedData);
            elements.rawYamlTextarea.value = state.rawYaml;
            setDirty(true);
            renderGenericOrRegistryForm();
            showToast(`ลบคีย์ "${k}" เรียบร้อยแล้ว`, 'success');
          }
        );
      });

      tbody.appendChild(tr);
    });

    card.appendChild(table);
    elements.formFieldsContainer.appendChild(card);
  }

  // --- Setting Update & YAML Sync Logic ---

  function updateSettingValue(path, newValue) {
    setNestedValue(state.parsedData, path, newValue);
    state.rawYaml = updateYamlStringValue(state.rawYaml, path, newValue);
    elements.rawYamlTextarea.value = state.rawYaml;
    updateLineNumbers();
    updateRawMeta();
    setDirty(true);
  }

  function updateYamlStringValue(yamlText, path, newValue) {
    const keys = path.split('.');
    const lines = yamlText.split('\n');

    let currentIndent = 0;
    let keyIndex = 0;
    let targetLineIndex = -1;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const trimmed = line.trim();

      if (!trimmed || trimmed.startsWith('#')) continue;

      const leadingSpaces = line.search(/\S/);
      const colonIdx = line.indexOf(':');
      if (colonIdx === -1) continue;

      const lineKey = line.substring(leadingSpaces, colonIdx).trim().replace(/^['"]|['"]$/g, '');

      if (lineKey === keys[keyIndex] && (keyIndex === 0 || leadingSpaces > currentIndent)) {
        if (keyIndex === keys.length - 1) {
          targetLineIndex = i;
          break;
        } else {
          keyIndex++;
          currentIndent = leadingSpaces;
        }
      }
    }

    if (targetLineIndex !== -1) {
      const oldLine = lines[targetLineIndex];
      const colonIdx = oldLine.indexOf(':');
      const prefix = oldLine.substring(0, colonIdx + 1);

      let inlineComment = '';
      const commentIdx = oldLine.indexOf('#', colonIdx);
      if (commentIdx !== -1) {
        inlineComment = ' ' + oldLine.substring(commentIdx);
      }

      let formattedVal = newValue;
      if (typeof newValue === 'string') {
        if (newValue.includes(':') || newValue.includes('#') || newValue.startsWith('@') || newValue.startsWith('"')) {
          formattedVal = `"${newValue.replace(/"/g, '\\"')}"`;
        } else if (newValue === '' || /^\s*$/.test(newValue)) {
          formattedVal = '""';
        }
      } else if (typeof newValue === 'boolean') {
        formattedVal = newValue ? 'true' : 'false';
      }

      lines[targetLineIndex] = `${prefix} ${formattedVal}${inlineComment}`;
      return lines.join('\n');
    }

    // Fallback: If not found in original lines, regenerate YAML
    return stringifyYaml(state.parsedData);
  }

  /**
   * Safe JavaScript to YAML Stringifier
   */
  function stringifyYaml(val, indent = 0) {
    const pad = '  '.repeat(indent);
    if (val === null || val === undefined) return 'null\n';

    if (Array.isArray(val)) {
      if (val.length === 0) return '[]\n';
      let res = '\n';
      for (const item of val) {
        if (typeof item === 'object' && item !== null) {
          res += `${pad}- ` + stringifyYaml(item, indent + 1).trimStart();
        } else {
          res += `${pad}- ${formatYamlScalar(item)}\n`;
        }
      }
      return res;
    }

    if (typeof val === 'object') {
      let res = indent > 0 ? '\n' : '';
      const keys = Object.keys(val);
      for (const key of keys) {
        const v = val[key];
        if (typeof v === 'object' && v !== null) {
          res += `${pad}${key}:${stringifyYaml(v, indent + 1)}`;
        } else {
          res += `${pad}${key}: ${formatYamlScalar(v)}\n`;
        }
      }
      return res;
    }

    return formatYamlScalar(val) + '\n';
  }

  function formatYamlScalar(val) {
    if (val === null || val === undefined) return 'null';
    if (typeof val === 'boolean') return val ? 'true' : 'false';
    if (typeof val === 'number') return String(val);
    const s = String(val);
    if (s === '' || s.includes(':') || s.includes('#') || s.includes('\n') || s.includes('"') || s.includes('<') || s.includes('{') || s.includes('[') || s.startsWith('@') || s.startsWith('&') || s.startsWith('*')) {
      return `"${s.replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`;
    }
    return s;
  }

  function deleteNestedKey(obj, path) {
    const parts = path.split('.');
    let cur = obj;
    for (let i = 0; i < parts.length - 1; i++) {
      if (!cur[parts[i]]) return;
      cur = cur[parts[i]];
    }
    delete cur[parts[parts.length - 1]];
  }

  // --- Modals for Adding New Entries ---

  function openAddItemModal(preset = null, defaultId = '') {
    const isClone = Boolean(preset);
    elements.entryModalTitle.innerHTML = `
      <span class="p-1.5 rounded-lg bg-indigo-600/20 text-indigo-400">${SVG_ICONS.box}</span>
      <span>${isClone ? 'คัดลอกและสร้างไอเทมใหม่ (Clone Item)' : 'เพิ่มไอเทมใหม่ (Add New Item)'}</span>
    `;

    const defRarity = preset ? (preset.rarity || preset.tier || 'COMMON').toUpperCase() : 'RARE';
    const defContainers = preset && Array.isArray(preset.allowed_containers)
      ? preset.allowed_containers.join(', ')
      : (preset && preset.allowed_containers ? preset.allowed_containers : 'common, rare, epic');
    const defLore = preset && Array.isArray(preset.lore)
      ? preset.lore.join('\n')
      : (preset && preset.lore ? preset.lore : '');

    elements.entryModalBody.innerHTML = `
      <div class="space-y-3">
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">รหัสไอเทม (Item ID):</label>
          <input type="text" id="m_item_id" value="${escapeHtml(defaultId)}" placeholder="เช่น combat_knife, gold_coin" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          <span class="text-[10px] text-zinc-500">ใช้ภาษาอังกฤษตัวพิมพ์เล็ก ห้ามเว้นวรรค (ใช้ _ แทน)</span>
        </div>
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">ชื่อไอเทม (Display Name):</label>
          <input type="text" id="m_item_name" value="${escapeHtml(preset ? (preset.display_name || '') : '')}" placeholder="เช่น <gradient:#00FFA3:#00B8FF><bold>Tactical Knife</bold></gradient>" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition">
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ชนิดบล็อก (Material):</label>
            <input type="text" id="m_item_mat" value="${escapeHtml(preset ? (preset.material || 'IRON_SWORD') : 'IRON_SWORD')}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ระดับความหายาก (Rarity):</label>
            <select id="m_item_rarity" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition cursor-pointer">
              ${['COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY'].map(r => `<option value="${r}" ${r === defRarity ? 'selected' : ''}>${r}</option>`).join('')}
            </select>
          </div>
        </div>
        <div class="grid grid-cols-3 gap-2">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ราคา ($):</label>
            <input type="number" id="m_item_price" value="${preset ? (preset.price || preset.value || 100) : 250}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">น้ำหนักดรอป:</label>
            <input type="number" id="m_item_weight" value="${preset ? (preset.weight || 10) : 20}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">เวลาค้นหา (s):</label>
            <input type="number" id="m_item_time" value="${preset ? (preset.search_time || 2) : 2}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
        </div>
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">กล่องที่อนุญาตให้สุ่มเกิด (คั่นด้วยจุลภาค):</label>
          <input type="text" id="m_item_containers" value="${escapeHtml(defContainers)}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
        </div>
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">คำอธิบายใต้ไอเทม (Lore - 1 บรรทัดต่อ 1 ข้อความ):</label>
          <textarea id="m_item_lore" rows="2" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition leading-relaxed resize-y">${escapeHtml(defLore)}</textarea>
        </div>
      </div>
    `;

    openEntryModal(() => {
      const id = document.getElementById('m_item_id').value.trim().toLowerCase().replace(/\s+/g, '_');
      if (!id) {
        showToast('กรุณาระบุรหัสไอเทม (Item ID)', 'error');
        return false;
      }
      if (!state.parsedData.items) state.parsedData.items = {};
      if (state.parsedData.items[id]) {
        showToast(`รหัสไอเทม "${id}" มีอยู่ในระบบแล้ว`, 'error');
        return false;
      }

      const containersStr = document.getElementById('m_item_containers').value;
      const containers = containersStr.split(',').map(s => s.trim()).filter(Boolean);
      const loreText = document.getElementById('m_item_lore').value.trim();
      const loreLines = loreText ? loreText.split('\n').map(l => l.trim()).filter(Boolean) : [
        `<color:#AAAAAA>Item ID: ${id}</color>`,
        `<color:#FFFF55>Extraction Value: </color><color:#FFAA00>$${document.getElementById('m_item_price').value || 100}</color>`
      ];

      const newItem = {
        material: document.getElementById('m_item_mat').value.trim().toUpperCase() || 'IRON_INGOT',
        display_name: document.getElementById('m_item_name').value.trim() || id,
        rarity: document.getElementById('m_item_rarity').value,
        tier: document.getElementById('m_item_rarity').value,
        price: parseFloat(document.getElementById('m_item_price').value) || 100,
        value: parseFloat(document.getElementById('m_item_price').value) || 100,
        weight: parseFloat(document.getElementById('m_item_weight').value) || 10,
        search_time: parseInt(document.getElementById('m_item_time').value, 10) || 2,
        reveal_sound: preset && preset.reveal_sound ? preset.reveal_sound : 'ENTITY_ITEM_PICKUP',
        allowed_containers: containers,
        lore: loreLines
      };

      state.parsedData.items[id] = newItem;
      state.rawYaml = stringifyYaml(state.parsedData);
      elements.rawYamlTextarea.value = state.rawYaml;
      setDirty(true);
      renderItemsRegistryView();
      showToast(`สร้างไอเทม "${id}" สำเร็จแล้ว! (อย่าลืมกดบันทึกและใช้งาน)`, 'success');
      return true;
    });
  }

  function openAddCategoryModal(preset = null, defaultId = '') {
    const isClone = Boolean(preset);
    elements.entryModalTitle.innerHTML = `
      <span class="p-1.5 rounded-lg bg-emerald-600/20 text-emerald-400">${SVG_ICONS.box}</span>
      <span>${isClone ? 'คัดลอกและสร้างหมวดหมู่กล่อง (Clone Category)' : 'เพิ่มหมวดหมู่กล่องใหม่ (Add New Category)'}</span>
    `;

    const chances = (preset && preset.rarity_chances) || { COMMON: 50, UNCOMMON: 35, RARE: 15, EPIC: 0, LEGENDARY: 0 };

    elements.entryModalBody.innerHTML = `
      <div class="space-y-3">
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">รหัสหมวดหมู่ (Category ID):</label>
          <input type="text" id="m_cat_id" value="${escapeHtml(defaultId)}" placeholder="เช่น supply_drop, weapon_case" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
        </div>
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">ชื่อกล่องที่แสดงผล (Display Name):</label>
          <input type="text" id="m_cat_name" value="${escapeHtml(preset ? (preset.display_name || preset.name || '') : '')}" placeholder="เช่น <gradient:#00FFA3:#00B8FF><bold>กล่องอาวุธ (Weapon Crate)</bold></gradient>" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition">
        </div>
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">สีประจำกล่อง (Hex Color):</label>
          <input type="color" id="m_cat_color" value="${preset ? (preset.color || '#00FFA3') : '#00FFA3'}" class="w-full h-8 bg-zinc-950 border border-zinc-700 rounded-lg outline-none cursor-pointer">
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">จำนวนดรอปต่ำสุด (Min Rolls):</label>
            <input type="number" id="m_cat_min_rolls" value="${preset ? (preset.min_rolls || 2) : 2}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">จำนวนดรอปสูงสุด (Max Rolls):</label>
            <input type="number" id="m_cat_max_rolls" value="${preset ? (preset.max_rolls || 4) : 4}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">เวลาค้นหาต่ำสุด (วินาที):</label>
            <input type="number" id="m_cat_search_min" value="${preset ? (preset.search_time_min || 2) : 2}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">เวลาค้นหาสูงสุด (วินาที):</label>
            <input type="number" id="m_cat_search_max" value="${preset ? (preset.search_time_max || 4) : 4}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">บล็อกปิดกล่อง (Covered):</label>
            <input type="text" id="m_cat_covered" value="${escapeHtml(preset ? (preset.covered_material || 'BLACK_STAINED_GLASS_PANE') : 'BLACK_STAINED_GLASS_PANE')}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">บล็อกค้นหา (Searching):</label>
            <input type="text" id="m_cat_searching" value="${escapeHtml(preset ? (preset.searching_material || 'STONE') : 'STONE')}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
        </div>
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">โอกาสออกของระดับความหายาก (Rarity Chances %):</label>
          <div class="grid grid-cols-5 gap-1.5 text-center">
            <div>
              <span class="text-[10px] text-zinc-400 block mb-0.5 font-bold">COM</span>
              <input type="number" id="m_cat_c_com" value="${chances.COMMON || 0}" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-emerald-400 block mb-0.5 font-bold">UNC</span>
              <input type="number" id="m_cat_c_unc" value="${chances.UNCOMMON || 0}" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-blue-400 block mb-0.5 font-bold">RARE</span>
              <input type="number" id="m_cat_c_rare" value="${chances.RARE || 0}" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-purple-400 block mb-0.5 font-bold">EPIC</span>
              <input type="number" id="m_cat_c_epic" value="${chances.EPIC || 0}" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-amber-400 block mb-0.5 font-bold">LEG</span>
              <input type="number" id="m_cat_c_leg" value="${chances.LEGENDARY || 0}" class="w-full px-1.5 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white">
            </div>
          </div>
        </div>
      </div>
    `;

    openEntryModal(() => {
      const id = document.getElementById('m_cat_id').value.trim().toLowerCase().replace(/\s+/g, '_');
      if (!id) {
        showToast('กรุณาระบุรหัสหมวดหมู่ (Category ID)', 'error');
        return false;
      }
      if (!state.parsedData.categories) state.parsedData.categories = {};
      if (state.parsedData.categories[id]) {
        showToast(`รหัสหมวดหมู่ "${id}" มีอยู่ในระบบแล้ว`, 'error');
        return false;
      }

      const newCat = {
        display_name: document.getElementById('m_cat_name').value.trim() || id,
        name: id,
        color: document.getElementById('m_cat_color').value,
        min_rolls: parseInt(document.getElementById('m_cat_min_rolls').value, 10) || 2,
        max_rolls: parseInt(document.getElementById('m_cat_max_rolls').value, 10) || 4,
        search_time_min: parseInt(document.getElementById('m_cat_search_min').value, 10) || 2,
        search_time_max: parseInt(document.getElementById('m_cat_search_max').value, 10) || 4,
        covered_material: document.getElementById('m_cat_covered').value.trim().toUpperCase() || 'BLACK_STAINED_GLASS_PANE',
        searching_material: document.getElementById('m_cat_searching').value.trim().toUpperCase() || 'STONE',
        search_sound: preset && preset.search_sound ? preset.search_sound : 'BLOCK_STONE_HIT',
        reveal_sound: preset && preset.reveal_sound ? preset.reveal_sound : 'ENTITY_ITEM_PICKUP',
        rarity_chances: {
          COMMON: parseFloat(document.getElementById('m_cat_c_com').value) || 0,
          UNCOMMON: parseFloat(document.getElementById('m_cat_c_unc').value) || 0,
          RARE: parseFloat(document.getElementById('m_cat_c_rare').value) || 0,
          EPIC: parseFloat(document.getElementById('m_cat_c_epic').value) || 0,
          LEGENDARY: parseFloat(document.getElementById('m_cat_c_leg').value) || 0
        }
      };

      state.parsedData.categories[id] = newCat;
      state.rawYaml = stringifyYaml(state.parsedData);
      elements.rawYamlTextarea.value = state.rawYaml;
      setDirty(true);
      renderCategoriesRegistryView();
      showToast(`สร้างหมวดหมู่ "${id}" สำเร็จแล้ว!`, 'success');
      return true;
    });
  }

  function openAddSafeCaseModal(preset = null, defaultId = '') {
    const isClone = Boolean(preset);
    elements.entryModalTitle.innerHTML = `
      <span class="p-1.5 rounded-lg bg-sky-600/20 text-sky-400">${SVG_ICONS.safe_cases_settings}</span>
      <span>${isClone ? 'คัดลอกและสร้างกระเป๋านิรภัย (Clone Safe Case)' : 'เพิ่มกระเป๋านิรภัยใหม่ (Add Safe Case)'}</span>
    `;

    elements.entryModalBody.innerHTML = `
      <div class="space-y-3">
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">รหัสกระเป๋า (Case ID):</label>
          <input type="text" id="m_case_id" value="${escapeHtml(defaultId)}" placeholder="เช่น gamma_case, theta_case" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
        </div>
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">ชื่อกระเป๋า (Display Name):</label>
          <input type="text" id="m_case_name" value="${escapeHtml(preset ? (preset.display_name || preset.title || '') : '')}" placeholder="เช่น <gradient:#FFAA00:#FF5500><bold>Gamma Safe Case</bold></gradient>" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition">
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ความจุช่อง (Capacity):</label>
            <input type="number" id="m_case_cap" value="${preset ? (preset.capacity || 9) : 9}" min="1" max="54" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ไอเทม (Material):</label>
            <input type="text" id="m_case_mat" value="${escapeHtml(preset ? (preset.material || 'BUNDLE') : 'BUNDLE')}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
        </div>
        <div class="grid grid-cols-2 gap-3 items-center">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">CustomModelData:</label>
            <input type="number" id="m_case_cmd" value="${preset ? (preset.custom_model_data || 0) : 0}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          </div>
          <div class="pt-4">
            <label class="flex items-center gap-2 cursor-pointer select-none">
              <input type="checkbox" id="m_case_keeps" class="rounded bg-zinc-950 border-zinc-700 text-sky-500 focus:ring-0 cursor-pointer" ${preset && preset.keeps_on_death === false ? '' : 'checked'}>
              <span class="text-xs text-zinc-300 font-medium">ของไม่ตกเมื่อตาย</span>
            </label>
          </div>
        </div>
      </div>
    `;

    openEntryModal(() => {
      const id = document.getElementById('m_case_id').value.trim().toLowerCase().replace(/\s+/g, '_');
      if (!id) {
        showToast('กรุณาระบุรหัสกระเป๋า (Case ID)', 'error');
        return false;
      }
      if (!state.parsedData.safe_cases) state.parsedData.safe_cases = {};
      if (state.parsedData.safe_cases[id]) {
        showToast(`รหัสกระเป๋า "${id}" มีอยู่ในระบบแล้ว`, 'error');
        return false;
      }

      state.parsedData.safe_cases[id] = {
        display_name: document.getElementById('m_case_name').value.trim() || id,
        title: document.getElementById('m_case_name').value.trim() || id,
        material: document.getElementById('m_case_mat').value.trim().toUpperCase() || 'BUNDLE',
        capacity: parseInt(document.getElementById('m_case_cap').value, 10) || 9,
        custom_model_data: parseInt(document.getElementById('m_case_cmd').value, 10) || 0,
        keeps_on_death: document.getElementById('m_case_keeps').checked
      };

      state.rawYaml = stringifyYaml(state.parsedData);
      elements.rawYamlTextarea.value = state.rawYaml;
      setDirty(true);
      renderSafeCasesRegistryView();
      showToast(`สร้างกระเป๋านิรภัย "${id}" สำเร็จแล้ว!`, 'success');
      return true;
    });
  }

  function openAddKeyModal() {
    elements.entryModalTitle.innerHTML = `
      <span class="p-1.5 rounded-lg bg-amber-600/20 text-amber-400">${SVG_ICONS.box}</span>
      <span>เพิ่มคีย์การตั้งค่าใหม่ (Add Configuration Key)</span>
    `;

    elements.entryModalBody.innerHTML = `
      <div class="space-y-3">
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">เส้นทางคีย์ (Key Path):</label>
          <input type="text" id="m_key_path" placeholder="เช่น custom.alert_message หรือ settings.timeout" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
          <span class="text-[10px] text-zinc-500">ใช้จุด (.) คั่นลำดับชั้นของ YAML เช่น section.child_key</span>
        </div>
        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">ค่าที่ตั้งไว้ (Value):</label>
          <input type="text" id="m_key_val" placeholder="เช่น ข้อความ, 100, true" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs text-zinc-100 outline-none focus:border-white transition">
        </div>
      </div>
    `;

    openEntryModal(() => {
      const key = document.getElementById('m_key_path').value.trim();
      let val = document.getElementById('m_key_val').value;
      if (!key) {
        showToast('กรุณาระบุเส้นทางคีย์', 'error');
        return false;
      }
      if (val === 'true') val = true;
      else if (val === 'false') val = false;
      else if (!isNaN(Number(val)) && val.trim() !== '') val = Number(val);

      setNestedValue(state.parsedData, key, val);
      state.rawYaml = stringifyYaml(state.parsedData);
      elements.rawYamlTextarea.value = state.rawYaml;
      setDirty(true);
      renderCurrentView();
      showToast(`เพิ่มคีย์ "${key}" เรียบร้อยแล้ว!`, 'success');
      return true;
    });
  }

  let activeModalConfirmCallback = null;

  function openEntryModal(onConfirm) {
    activeModalConfirmCallback = onConfirm;
    elements.entryModal.classList.remove('hidden');
    elements.entryModal.classList.add('flex');
    setTimeout(() => {
      const firstInput = elements.entryModalBody.querySelector('input');
      if (firstInput) firstInput.focus();
    }, 50);
  }

  function closeEntryModal() {
    elements.entryModal.classList.add('hidden');
    elements.entryModal.classList.remove('flex');
    activeModalConfirmCallback = null;
  }

  // --- Styled Confirmation Modal ---

  let activeConfirmCallback = null;

  function showConfirmModal(title, desc, onConfirm) {
    activeConfirmCallback = onConfirm;
    elements.confirmModalTitle.textContent = title || 'ยืนยันการลบข้อมูล';
    elements.confirmModalDesc.textContent = desc || 'คุณแน่ใจหรือไม่ว่าต้องการลบรายการนี้? การดำเนินการนี้ไม่สามารถย้อนกลับได้';
    elements.confirmModal.classList.remove('hidden');
    elements.confirmModal.classList.add('flex');
  }

  function closeConfirmModal() {
    elements.confirmModal.classList.add('hidden');
    elements.confirmModal.classList.remove('flex');
    activeConfirmCallback = null;
  }

  // --- Create File Modal ---

  function openCreateFileModal() {
    elements.newConfigFileNameInput.value = '';
    elements.createFileModal.classList.remove('hidden');
    elements.createFileModal.classList.add('flex');
    setTimeout(() => elements.newConfigFileNameInput.focus(), 50);
  }

  function closeCreateFileModal() {
    elements.createFileModal.classList.add('hidden');
    elements.createFileModal.classList.remove('flex');
  }

  async function handleCreateNewFile() {
    const rawName = (elements.newConfigFileNameInput.value || '').trim();
    if (!rawName) {
      showToast('กรุณาระบุชื่อไฟล์ที่ต้องการสร้าง', 'error');
      return;
    }
    let filename = rawName;
    if (!filename.endsWith('.yml') && !filename.endsWith('.yaml')) {
      filename += '.yml';
    }
    filename = filename.replace(/[^a-zA-Z0-9_\-\.\/]/g, '_');

    try {
      const res = await fetch(`/api/config?file=${encodeURIComponent(filename)}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-yaml',
          'Authorization': state.authToken ? `Bearer ${state.authToken}` : '',
          'X-Auth-Token': state.authToken || ''
        },
        body: `# คอนฟิกแบบกำหนดเอง: ${filename}\n`
      });
      const data = await res.json();
      if (res.ok && data.success) {
        showToast(`สร้างไฟล์ ${filename} เรียบร้อยแล้ว`, 'success');
        closeCreateFileModal();
        await loadFilesList();
        await loadConfigFile(filename);
      } else {
        showToast(data.message || 'เกิดข้อผิดพลาดในการสร้างไฟล์', 'error');
      }
    } catch (err) {
      showToast('ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้: ' + err.message, 'error');
    }
  }

  // --- Line Numbers & Syntax Validation ---

  function updateLineNumbers() {
    const linesCount = (elements.rawYamlTextarea.value.match(/\n/g) || []).length + 1;
    let numStr = '';
    for (let i = 1; i <= linesCount; i++) {
      numStr += i + '\n';
    }
    elements.rawLineNumbers.textContent = numStr;
  }

  function updateRawMeta() {
    const text = elements.rawYamlTextarea.value;
    const linesCount = (text.match(/\n/g) || []).length + 1;
    const sizeKb = (new Blob([text]).size / 1024).toFixed(1);
    elements.rawMetaDisplay.textContent = `${linesCount} บรรทัด • ${sizeKb} KB`;
  }

  function validateYamlSyntax(text) {
    const lines = text.split('\n');
    for (let i = 0; i < lines.length; i++) {
      if (lines[i].startsWith('\t')) {
        return { valid: false, error: `พบ Tab ที่บรรทัด ${i + 1} (YAML ไม่อนุญาตให้ใช้ Tab ต้องใช้ Space เท่านั้น)` };
      }
    }
    return { valid: true };
  }

  // --- Search Filtering ---

  function applySearch(query) {
    state.searchQuery = query.trim().toLowerCase();
    elements.searchClearBtn.classList.toggle('hidden', !state.searchQuery);

    if (!state.searchQuery) {
      document.querySelectorAll('.setting-row, tbody tr, .grid > div').forEach(el => {
        el.style.display = '';
      });
      return;
    }

    if (state.activeFile === 'config.yml' && state.viewMode === 'form') {
      renderAllConfigCategories();
    }

    document.querySelectorAll('.setting-row').forEach(row => {
      const text = (row.textContent || '').toLowerCase();
      row.style.display = text.includes(state.searchQuery) ? 'flex' : 'none';
    });

    document.querySelectorAll('tbody tr').forEach(tr => {
      const key = (tr.dataset.key || '').toLowerCase();
      const input = tr.querySelector('input');
      const val = input ? input.value.toLowerCase() : '';
      tr.style.display = (key.includes(state.searchQuery) || val.includes(state.searchQuery)) ? '' : 'none';
    });

    document.querySelectorAll('.grid > div').forEach(card => {
      const text = (card.textContent || '').toLowerCase();
      card.style.display = text.includes(state.searchQuery) ? 'block' : 'none';
    });
  }

  // --- Auth Modal & Security ---

  function openAuthModal() {
    elements.authTokenInput.value = state.authToken;
    elements.authModal.classList.remove('hidden');
    elements.authModal.classList.add('flex');
  }

  function closeAuthModal() {
    elements.authModal.classList.add('hidden');
    elements.authModal.classList.remove('flex');
  }

  // --- Toast Notifications ---

  function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const iconSvg = type === 'success' ? SVG_ICONS.check : type === 'error' ? SVG_ICONS.alert : SVG_ICONS.general;

    toast.innerHTML = `
      <span class="flex-shrink-0">${iconSvg}</span>
      <span class="flex-1 font-medium text-xs leading-relaxed">${escapeHtml(message)}</span>
    `;

    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(-10px)';
      setTimeout(() => toast.remove(), 250);
    }, 4000);
  }

  // --- Utilities ---

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function getNestedValue(obj, path) {
    if (!obj) return undefined;
    const parts = path.split('.');
    let cur = obj;
    for (const part of parts) {
      if (cur === null || cur === undefined) return undefined;
      cur = cur[part];
    }
    return cur;
  }

  function setNestedValue(obj, path, value) {
    const parts = path.split('.');
    let cur = obj;
    for (let i = 0; i < parts.length - 1; i++) {
      const part = parts[i];
      if (!cur[part] || typeof cur[part] !== 'object') {
        cur[part] = {};
      }
      cur = cur[part];
    }
    cur[parts[parts.length - 1]] = value;
  }

  function flattenObject(ob, prefix = '') {
    const toReturn = {};
    for (const i in ob) {
      if (!ob.hasOwnProperty(i)) continue;
      const key = prefix ? prefix + '.' + i : i;
      if (typeof ob[i] === 'object' && ob[i] !== null && !Array.isArray(ob[i])) {
        const flatObject = flattenObject(ob[i], key);
        for (const x in flatObject) {
          if (!flatObject.hasOwnProperty(x)) continue;
          toReturn[x] = flatObject[x];
        }
      } else {
        toReturn[key] = ob[i];
      }
    }
    return toReturn;
  }

  // --- Event Listeners Initialization ---

  function setupEventListeners() {
    elements.saveBtn.addEventListener('click', () => saveConfigFile());
    elements.inGameReloadBtn.addEventListener('click', () => reloadInGame());
    elements.discardBtn.addEventListener('click', () => {
      if (state.isDirty && confirm('ยกเลิกการแก้ไขทั้งหมดและโหลดใหม่จากดิสก์หรือไม่?')) {
        loadConfigFile(state.activeFile);
        showToast('ยกเลิกการแก้ไขเรียบร้อยแล้ว', 'info');
      }
    });

    elements.viewModeFormBtn.addEventListener('click', () => {
      elements.viewModeFormBtn.className = 'flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-medium text-white bg-zinc-800 shadow-sm transition';
      elements.viewModeRawBtn.className = 'flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-medium text-zinc-400 hover:text-white transition';
      state.viewMode = 'form';
      renderCurrentView();
    });

    elements.viewModeRawBtn.addEventListener('click', () => {
      elements.viewModeRawBtn.className = 'flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-medium text-white bg-zinc-800 shadow-sm transition';
      elements.viewModeFormBtn.className = 'flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-medium text-zinc-400 hover:text-white transition';
      state.viewMode = 'raw';
      renderCurrentView();
    });

    elements.rawYamlTextarea.addEventListener('input', () => {
      state.rawYaml = elements.rawYamlTextarea.value;
      updateLineNumbers();
      updateRawMeta();
      setDirty(true);

      const check = validateYamlSyntax(state.rawYaml);
      const dot = elements.syntaxStatus.querySelector('.syntax-dot');
      const text = elements.syntaxStatus.querySelector('span:last-child');
      if (check.valid) {
        dot.className = 'syntax-dot w-2 h-2 rounded-full bg-emerald-500';
        text.textContent = 'ไวยากรณ์ถูกต้อง (Valid)';
      } else {
        dot.className = 'syntax-dot w-2 h-2 rounded-full bg-red-500';
        text.textContent = check.error;
      }
    });

    elements.rawYamlTextarea.addEventListener('scroll', () => {
      elements.rawLineNumbers.scrollTop = elements.rawYamlTextarea.scrollTop;
    });

    elements.rawYamlTextarea.addEventListener('keydown', (e) => {
      if (e.key === 'Tab') {
        e.preventDefault();
        const start = elements.rawYamlTextarea.selectionStart;
        const end = elements.rawYamlTextarea.selectionEnd;
        elements.rawYamlTextarea.value = elements.rawYamlTextarea.value.substring(0, start) + '  ' + elements.rawYamlTextarea.value.substring(end);
        elements.rawYamlTextarea.selectionStart = elements.rawYamlTextarea.selectionEnd = start + 2;
        elements.rawYamlTextarea.dispatchEvent(new Event('input'));
      }
    });

    elements.rawFormatBtn.addEventListener('click', () => {
      const check = validateYamlSyntax(elements.rawYamlTextarea.value);
      if (check.valid) {
        showToast('ไวยากรณ์ YAML ถูกต้องเรียบร้อย', 'success');
      } else {
        showToast(check.error, 'error');
      }
    });

    elements.rawFileSelect.addEventListener('change', (e) => {
      loadConfigFile(e.target.value);
    });

    elements.globalSearchInput.addEventListener('input', (e) => {
      applySearch(e.target.value);
    });

    elements.searchClearBtn.addEventListener('click', () => {
      elements.globalSearchInput.value = '';
      applySearch('');
      elements.globalSearchInput.focus();
    });

    window.addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
        e.preventDefault();
        saveConfigFile();
      }
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        elements.globalSearchInput.focus();
        elements.globalSearchInput.select();
      }
    });

    // Sidebar navigation click
    document.querySelector('aside').addEventListener('click', (e) => {
      const navItem = e.target.closest('.nav-item');
      if (!navItem) return;

      e.preventDefault();

      if (navItem.id === 'navItemRawEditor') {
        document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
        navItem.classList.add('active');
        elements.viewModeRawBtn.click();
        return;
      }

      const targetFile = navItem.dataset.file;
      const targetCategory = navItem.dataset.category;

      document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
      navItem.classList.add('active');

      if (targetFile && targetFile !== state.activeFile) {
        loadConfigFile(targetFile).then(success => {
          if (success && targetCategory) {
            state.activeCategory = targetCategory;
            renderCurrentView();
          }
        });
      } else if (targetCategory) {
        state.activeCategory = targetCategory;
        if (state.viewMode === 'raw') {
          elements.viewModeFormBtn.click();
        } else {
          renderCurrentView();
        }
      }
    });

    // Entry Modal
    elements.closeEntryModalBtn.addEventListener('click', closeEntryModal);
    elements.cancelEntryModalBtn.addEventListener('click', closeEntryModal);
    elements.confirmEntryModalBtn.addEventListener('click', () => {
      if (activeModalConfirmCallback) {
        const success = activeModalConfirmCallback();
        if (success) closeEntryModal();
      }
    });

    // Confirm Modal
    elements.cancelConfirmModalBtn.addEventListener('click', closeConfirmModal);
    elements.okConfirmModalBtn.addEventListener('click', () => {
      if (activeConfirmCallback) {
        activeConfirmCallback();
      }
      closeConfirmModal();
    });
    elements.confirmModal.addEventListener('click', (e) => {
      if (e.target === elements.confirmModal) closeConfirmModal();
    });

    // Create File Modal
    if (elements.createNewFileBtn) {
      elements.createNewFileBtn.addEventListener('click', openCreateFileModal);
    }
    elements.closeCreateFileModalBtn.addEventListener('click', closeCreateFileModal);
    elements.cancelCreateFileModalBtn.addEventListener('click', closeCreateFileModal);
    elements.confirmCreateFileModalBtn.addEventListener('click', handleCreateNewFile);
    elements.createFileModal.addEventListener('click', (e) => {
      if (e.target === elements.createFileModal) closeCreateFileModal();
    });
    elements.newConfigFileNameInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        handleCreateNewFile();
      }
    });

    // Auth Modal
    elements.authModalBtn.addEventListener('click', openAuthModal);
    elements.closeAuthModalBtn.addEventListener('click', closeAuthModal);
    elements.saveAuthTokenBtn.addEventListener('click', () => {
      state.authToken = elements.authTokenInput.value.trim();
      if (state.authToken) {
        localStorage.setItem('lumina_auth_token', state.authToken);
        showToast('บันทึกโทเค็นความปลอดภัยแล้ว', 'success');
      } else {
        localStorage.removeItem('lumina_auth_token');
        showToast('ล้างโทเค็นแล้ว', 'info');
      }
      closeAuthModal();
      loadFilesList();
      loadConfigFile(state.activeFile);
    });

    elements.clearAuthTokenBtn.addEventListener('click', () => {
      state.authToken = '';
      localStorage.removeItem('lumina_auth_token');
      elements.authTokenInput.value = '';
      showToast('ล้างโทเค็นความปลอดภัยแล้ว', 'info');
      closeAuthModal();
    });

    elements.authModal.addEventListener('click', (e) => {
      if (e.target === elements.authModal) closeAuthModal();
    });

    elements.entryModal.addEventListener('click', (e) => {
      if (e.target === elements.entryModal) closeEntryModal();
    });

    window.addEventListener('beforeunload', (e) => {
      if (state.isDirty) {
        e.preventDefault();
        e.returnValue = '';
      }
    });
  }

  // --- Bootstrap App ---
  async function init() {
    setupEventListeners();
    await loadStatus();
    await loadFilesList();
    await loadConfigFile('config.yml');
  }

  document.addEventListener('DOMContentLoaded', init);
})();
