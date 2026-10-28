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
        { key: 'drop_box.rarity_search_times.F', label: 'เวลาค้นหาไอเทมแรงค์ F (วินาที)', desc: 'ไอเทมทั่วไป (สีเทา)', type: 'number', min: 1, default: 1 },
        { key: 'drop_box.rarity_search_times.D', label: 'เวลาค้นหาไอเทมแรงค์ D (วินาที)', desc: 'ไอเทมคุณภาพปานกลาง (สีเขียว)', type: 'number', min: 1, default: 2 },
        { key: 'drop_box.rarity_search_times.C', label: 'เวลาค้นหาไอเทมแรงค์ C (วินาที)', desc: 'ไอเทมหายาก (สีฟ้า)', type: 'number', min: 1, default: 3 },
        { key: 'drop_box.rarity_search_times.B', label: 'เวลาค้นหาไอเทมแรงค์ B (วินาที)', desc: 'ไอเทมมหากาพย์ (สีม่วง)', type: 'number', min: 1, default: 4 },
        { key: 'drop_box.rarity_search_times.A', label: 'เวลาค้นหาไอเทมแรงค์ A (วินาที)', desc: 'ไอเทมตำนาน (สีทอง)', type: 'number', min: 1, default: 5 },
        { key: 'drop_box.rarity_search_times.S', label: 'เวลาค้นหาไอเทมแรงค์ S (วินาที)', desc: 'ไอเทมระดับสูงสุด (สีแดง)', type: 'number', min: 1, default: 6 },
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
        { key: 'economy.rarity_price_thresholds.S', label: 'เกณฑ์ราคาขั้นต่ำ: แรงค์ S (สีแดง)', desc: 'เช่น 5000.0', type: 'number', step: 500.0, default: 5000.0 },
        { key: 'economy.rarity_price_thresholds.A', label: 'เกณฑ์ราคาขั้นต่ำ: แรงค์ A (สีทอง)', desc: 'เช่น 2500.0', type: 'number', step: 100.0, default: 2500.0 },
        { key: 'economy.rarity_price_thresholds.B', label: 'เกณฑ์ราคาขั้นต่ำ: แรงค์ B (สีม่วง)', desc: 'เช่น 1000.0', type: 'number', step: 50.0, default: 1000.0 },
        { key: 'economy.rarity_price_thresholds.C', label: 'เกณฑ์ราคาขั้นต่ำ: แรงค์ C (สีฟ้า)', desc: 'เช่น 400.0', type: 'number', step: 25.0, default: 400.0 },
        { key: 'economy.rarity_price_thresholds.D', label: 'เกณฑ์ราคาขั้นต่ำ: แรงค์ D (สีเขียว)', desc: 'เช่น 100.0', type: 'number', step: 10.0, default: 100.0 },
        { key: 'economy.default_prices.F', label: 'ราคาเริ่มต้นไอเทมใหม่: แรงค์ F', desc: 'ราคาเริ่มต้น', type: 'number', default: 50.0 },
        { key: 'economy.default_prices.D', label: 'ราคาเริ่มต้นไอเทมใหม่: แรงค์ D', desc: 'ราคาเริ่มต้น', type: 'number', default: 150.0 },
        { key: 'economy.default_prices.C', label: 'ราคาเริ่มต้นไอเทมใหม่: แรงค์ C', desc: 'ราคาเริ่มต้น', type: 'number', default: 500.0 },
        { key: 'economy.default_prices.B', label: 'ราคาเริ่มต้นไอเทมใหม่: แรงค์ B', desc: 'ราคาเริ่มต้น', type: 'number', default: 1200.0 },
        { key: 'economy.default_prices.A', label: 'ราคาเริ่มต้นไอเทมใหม่: แรงค์ A', desc: 'ราคาเริ่มต้น', type: 'number', default: 3000.0 },
        { key: 'economy.default_prices.S', label: 'ราคาเริ่มต้นไอเทมใหม่: แรงค์ S', desc: 'ราคาเริ่มต้น', type: 'number', default: 6000.0 }
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
    newConfigFileNameInput: document.getElementById('newConfigFileNameInput'),
    materialPickerModal: document.getElementById('materialPickerModal'),
    materialPickerTitle: document.getElementById('materialPickerTitle'),
    closeMaterialPickerBtn: document.getElementById('closeMaterialPickerBtn'),
    cancelMaterialPickerBtn: document.getElementById('cancelMaterialPickerBtn'),
    materialSearchInput: document.getElementById('materialSearchInput'),
    materialCategoryFilters: document.getElementById('materialCategoryFilters'),
    materialGridContainer: document.getElementById('materialGridContainer'),
    materialCountDisplay: document.getElementById('materialCountDisplay')
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
      if (state.activeFile === 'categories.yml') {
        preloadCategories();
      }
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

  // --- Minecraft Material & Texture Manager ---
  let mcItemsList = [];
  let mcTexturesMap = {};
  let mcMaterialsList = [];
  let isMcAssetsLoaded = false;
  let currentPickerCallback = null;
  let currentPickerFilter = 'ALL';
  let currentPickerQuery = '';
  let currentPickerSelected = '';
  let currentFilteredPickerItems = [];
  let currentPickerBatchIndex = 0;
  const PICKER_BATCH_SIZE = 120;

  async function loadMinecraftAssets() {
    try {
      // 1. Try local items_textures.json
      let data = null;
      try {
        const localRes = await fetch('items_textures.json');
        if (localRes.ok) {
          data = await localRes.json();
        }
      } catch (e) {
        console.warn('[MinecraftAssets] Local items_textures.json not found, falling back to CDN...', e);
      }

      // 2. Fallback to CDN
      if (!data || !data.items || data.items.length === 0) {
        try {
          const cdnRes = await fetch('https://unpkg.com/minecraft-textures/dist/textures/json/26.3.json');
          if (cdnRes.ok) {
            data = await cdnRes.json();
          }
        } catch (cdnErr) {
          console.warn('[MinecraftAssets] CDN textures fetch failed:', cdnErr);
        }
      }

      if (data && data.items) {
        mcItemsList = data.items;
        mcTexturesMap = {};
        for (let i = 0; i < mcItemsList.length; i++) {
          const item = mcItemsList[i];
          const rawId = item.id || '';
          const clean = rawId.replace(/^minecraft:/, '').toLowerCase();
          mcTexturesMap[clean] = item;
          mcTexturesMap['minecraft:' + clean] = item;
          mcTexturesMap[clean.toUpperCase()] = item;
        }
        isMcAssetsLoaded = true;
      }

      // 3. Query server for Bukkit materials list
      try {
        const matRes = await apiFetch('/api/materials');
        if (matRes && matRes.success && Array.isArray(matRes.materials)) {
          mcMaterialsList = matRes.materials;
          for (let i = 0; i < mcMaterialsList.length; i++) {
            const m = mcMaterialsList[i];
            const clean = m.name.toLowerCase();
            if (!mcTexturesMap[clean]) {
              const readable = m.name.toLowerCase().split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
              const newItem = {
                readable: readable,
                id: m.id || ('minecraft:' + clean),
                texture: `https://assets.mcasset.cloud/1.21/assets/minecraft/textures/item/${clean}.png`
              };
              mcItemsList.push(newItem);
              mcTexturesMap[clean] = newItem;
              mcTexturesMap['minecraft:' + clean] = newItem;
              mcTexturesMap[m.name] = newItem;
            }
          }
        }
      } catch (err) {
        console.warn('[MinecraftAssets] Failed to query /api/materials:', err);
      }
    } catch (err) {
      console.error('[MinecraftAssets] Failed to initialize Minecraft assets:', err);
    }
  }

  function getMinecraftItemTexture(matName) {
    if (!matName) return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="%2371717a" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>';
    const clean = matName.toLowerCase().trim().replace(/^minecraft:/, '');
    const entry = mcTexturesMap[clean] || mcTexturesMap['minecraft:' + clean] || mcTexturesMap[clean.toUpperCase()];
    if (entry && entry.texture) {
      return entry.texture;
    }
    return `https://assets.mcasset.cloud/1.21/assets/minecraft/textures/item/${clean}.png`;
  }

  function getMinecraftItemReadableName(matName) {
    if (!matName) return 'Unknown Item';
    const clean = matName.toLowerCase().trim().replace(/^minecraft:/, '');
    const entry = mcTexturesMap[clean] || mcTexturesMap['minecraft:' + clean] || mcTexturesMap[clean.toUpperCase()];
    if (entry && entry.readable) {
      return entry.readable;
    }
    return matName.toLowerCase().split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
  }

  const THAI_ITEM_ALIASES = [
    ['ทองแดง', ['copper']],
    ['โต๊ะคราฟต์', ['crafting_table']],
    ['โต๊ะคราฟ', ['crafting_table']],
    ['เนเธอไรต์', ['netherite']],
    ['เนเธอไรท์', ['netherite']],
    ['ลูกธนู', ['arrow']],
    ['หน้าไม้', ['crossbow']],
    ['คบเพลิง', ['torch']],
    ['แผ่นเสียง', ['music_disc', 'disc']],
    ['ดาบ', ['sword']],
    ['ขวาน', ['axe']],
    ['พลั่ว', ['shovel']],
    ['จอบ', ['hoe']],
    ['อีเตอร์', ['pickaxe']],
    ['ที่ขุด', ['pickaxe']],
    ['ธนู', ['bow', 'crossbow']],
    ['โล่', ['shield']],
    ['หมวก', ['helmet', 'cap']],
    ['เสื้อ', ['chestplate', 'tunic']],
    ['เกราะ', ['helmet', 'chestplate', 'leggings', 'boots', 'armor', 'shield']],
    ['กางเกง', ['leggings', 'pants']],
    ['รองเท้า', ['boots']],
    ['ปีก', ['elytra']],
    ['กล่อง', ['chest', 'shulker_box', 'barrel', 'box']],
    ['หีบ', ['chest', 'ender_chest', 'trapped_chest']],
    ['เตา', ['furnace', 'smoker', 'blast_furnace']],
    ['เพชร', ['diamond']],
    ['ทอง', ['gold', 'golden']],
    ['เหล็ก', ['iron']],
    ['หิน', ['stone', 'cobblestone', 'deepslate']],
    ['ไม้', ['wood', 'log', 'planks', 'plank']],
    ['แร่', ['ore', 'raw_']],
    ['แอปเปิ้ล', ['apple']],
    ['ขนมปัง', ['bread']],
    ['เนื้อ', ['beef', 'porkchop', 'mutton', 'chicken', 'rabbit']],
    ['ปลา', ['cod', 'salmon']],
    ['ยา', ['potion']],
    ['สมุด', ['book']],
    ['หนังสือ', ['book']],
    ['โคมไฟ', ['lantern', 'lamp']],
    ['ระเบิด', ['tnt']],
    ['เตียง', ['bed']],
    ['กระจก', ['glass']],
    ['มรกต', ['emerald']],
    ['เนเธอร์', ['nether']],
    ['ลาพิส', ['lapis']],
    ['เรดสโตน', ['redstone']],
    ['เข็มทิศ', ['compass']],
    ['นาฬิกา', ['clock']],
    ['แตร', ['goat_horn']],
    ['กระเป๋า', ['bundle']],
    ['ถุง', ['bundle']],
    ['กุญแจ', ['trial_key', 'key']],
    ['พลุ', ['firework']],
    ['ไข่', ['egg']],
    ['กระดูก', ['bone']],
    ['หอก', ['spear']],
    ['กระบอง', ['mace']],
    ['พรม', ['carpet']],
    ['ขนแกะ', ['wool']],
    ['คอนกรีต', ['concrete']],
    ['ดิน', ['dirt', 'mud']],
    ['ทราย', ['sand']]
  ];

  const MC_COLORS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black'];

  // Categorize Minecraft items exactly like Java Edition Creative Inventory tabs
  function categorizeMinecraftItem(item) {
    const id = (item.id || '').toLowerCase().replace(/^minecraft:/, '');

    // 1. SPAWN_EGGS (Mob spawn eggs)
    if (id.endsWith('_spawn_egg')) {
      return 'SPAWN_EGGS';
    }

    // 2. OPERATOR (Command blocks, Barrier, Light, Structure blocks)
    if (/^(command_block|chain_command_block|repeating_command_block|command_block_minecart|barrier|structure_block|structure_void|jigsaw|light|debug_stick)$/.test(id)) {
      return 'OPERATOR';
    }

    // 3. COLORED_BLOCKS (16 colors of wool, carpet, concrete, stained glass, beds, candles, banners, shulkers, cushions, harnesses)
    for (let i = 0; i < MC_COLORS.length; i++) {
      const col = MC_COLORS[i];
      if (id.startsWith(col + '_')) {
        const rest = id.slice(col.length + 1);
        if (/^(wool|carpet|terracotta|glazed_terracotta|concrete|stained_glass|bed|candle|banner|shulker_box|cushion|harness)/.test(rest)) {
          return 'COLORED_BLOCKS';
        }
      }
    }

    // 4. COMBAT (Swords, Spears, Mace, Trident, Bow, Crossbow, Arrows, Shields, Armor, Totem)
    if (/(_sword|_spear|mace|trident|bow|crossbow|arrow|shield|_helmet|_chestplate|_leggings|_boots|turtle_helmet|elytra|horse_armor|wolf_armor|nautilus_armor|totem_of_undying|wind_charge)/.test(id)) {
      if (!id.endsWith('_banner_pattern') && !id.endsWith('_template')) {
        return 'COMBAT';
      }
    }

    // 5. TOOLS_UTILITIES (Pickaxes, Shovels, Axes, Hoes, Utilities, Buckets, Rides, Music Discs, Bundles)
    if (/(_pickaxe|_shovel|_axe|_hoe|fishing_rod|flint_and_steel|shears|brush|lead|name_tag|compass|clock|spyglass|_bucket|saddle|minecart|_boat|carrot_on_a_stick|warped_fungus_on_a_stick|firework_rocket|music_disc_|goat_horn|bundle)/.test(id)) {
      if (!/furnace|crafting/.test(id)) {
        return 'TOOLS_UTILITIES';
      }
    }

    // 6. FOOD_DRINKS (Food, Drinks, Soups, Potions)
    if (/(apple|bread|porkchop|beef|chicken|mutton|rabbit|cod|salmon|tropical_fish|pufferfish|carrot|potato|beetroot|soup|stew|melon_slice|sweet_berries|glow_berries|chorus_fruit|cookie|cake|pie|honey_bottle|rotten_flesh|spider_eye|milk_bucket|potion|ominous_bottle)/.test(id)) {
      if (!/candle_cake|potted/.test(id)) {
        return 'FOOD_DRINKS';
      }
    }

    // 7. REDSTONE_BLOCKS (Redstone circuitry, doors, trapdoors, mechanisms, bulbs)
    if (/(redstone|repeater|comparator|target|lever|_button|_pressure_plate|lightning_rod|daylight_detector|sculk_sensor|tripwire|trapped_chest|tnt|note_block|piston|dispenser|dropper|hopper|observer|crafter|_door|_trapdoor|_fence_gate|_copper_bulb|copper_bulb)/.test(id)) {
      if (!/redstone_ore/.test(id)) {
        return 'REDSTONE_BLOCKS';
      }
    }

    // 8. FUNCTIONAL_BLOCKS (Workstations, Containers, Lighting, Signs, Utility blocks)
    if (/(torch|lantern|campfire|crafting_table|furnace|smoker|blast_furnace|brewing_stand|cauldron|anvil|smithing_table|grindstone|cartography_table|loom|stonecutter|fletching_table|enchanting_table|chest|barrel|shulker_box|decorated_pot|chiseled_bookshelf|bell|beacon|conduit|lodestone|respawn_anchor|jukebox|lectern|flower_pot|composter|_sign|spawner|vault|end_rod|froglight|sea_lantern)/.test(id)) {
      if (!/chestplate|horse_armor/.test(id)) {
        return 'FUNCTIONAL_BLOCKS';
      }
    }

    // 9. NATURAL_BLOCKS (Ores, Dirt, Stone, Trees, Foliage, Nether/End natural blocks)
    if (/(_ore|raw_iron_block|raw_copper_block|raw_gold_block|ancient_debris|amethyst_block|budding_amethyst|amethyst_cluster|_amethyst_bud|dirt|grass_block|mud|sand|gravel|ice|snow|moss|obsidian|netherrack|soul_sand|soul_soil|basalt|blackstone|end_stone|purpur|magma_block|_log|_wood|_stem|_hyphae|_leaves|_sapling|_fungus|_mushroom|cactus|sugar_cane|bamboo|vine|flower|rose|tulip|daisy|orchid|poppy|dandelion|blossom|roots|dripleaf|grass|fern|kelp|sculk|creaking_heart|coral|podzol|mycelium)/.test(id)) {
      return 'NATURAL_BLOCKS';
    }

    // 10. INGREDIENTS (Ingots, Nuggets, Gems, Drops, Dyes, Templates, Sherds)
    if (/(ingot|nugget|raw_|diamond|emerald|lapis_lazuli|quartz|amethyst_shard|netherite_scrap|stick|flint|string|feather|gunpowder|_seeds|wheat|bone|slime_ball|clay_ball|brick|resin_|paper|book|leather|hide|foot|glowstone_dust|blaze_rod|blaze_powder|magma_cream|ghast_tear|pearl|eye_of_ender|shell|membrane|scute|breeze_rod|heavy_core|trial_key|breath|fire_charge|nether_star|echo_shard|disc_fragment|_dye|_sherd|_template|_banner_pattern|copper_bulb)/.test(id)) {
      return 'INGREDIENTS';
    }

    // 11. BUILDING_BLOCKS (Default: Construction blocks, planks, stones, bricks, slabs, stairs, copper blocks)
    return 'BUILDING_BLOCKS';
  }

  function openMaterialPicker(options = {}) {
    const {
      currentMaterial = 'STONE',
      title = 'เลือกไอเทมหรือบล็อกในเกม Minecraft',
      onSelect = null,
      filterType = 'ALL'
    } = options;

    let normFilter = filterType;
    if (normFilter === 'BLOCKS') normFilter = 'BUILDING_BLOCKS';
    else if (normFilter === 'WEAPON_ARMOR') normFilter = 'COMBAT';
    else if (normFilter === 'TOOLS') normFilter = 'TOOLS_UTILITIES';
    else if (normFilter === 'CONSUMABLE') normFilter = 'FOOD_DRINKS';
    else if (normFilter === 'MATERIALS') normFilter = 'INGREDIENTS';
    else if (normFilter === 'CONTAINERS') normFilter = 'FUNCTIONAL_BLOCKS';

    currentPickerCallback = onSelect;
    currentPickerSelected = currentMaterial.toUpperCase().trim().replace(/^MINECRAFT:/, '');
    currentPickerFilter = normFilter;
    currentPickerQuery = '';

    if (elements.materialPickerTitle) {
      elements.materialPickerTitle.textContent = title;
    }
    if (elements.materialSearchInput) {
      elements.materialSearchInput.value = '';
    }
    if (elements.materialPickerModal) {
      elements.materialPickerModal.classList.remove('hidden');
      elements.materialPickerModal.classList.add('flex');
    }

    if (elements.materialCategoryFilters) {
      const filterBtns = elements.materialCategoryFilters.querySelectorAll('.mat-filter-btn');
      filterBtns.forEach(btn => {
        const cat = btn.getAttribute('data-cat');
        if (cat === currentPickerFilter) {
          btn.className = 'mat-filter-btn px-2.5 py-1.5 rounded-lg text-[11px] font-medium bg-indigo-600 text-white transition whitespace-nowrap cursor-pointer flex items-center gap-1.5 shadow-sm';
        } else {
          btn.className = 'mat-filter-btn px-2.5 py-1.5 rounded-lg text-[11px] font-medium bg-zinc-800 text-zinc-400 hover:text-zinc-200 transition whitespace-nowrap cursor-pointer flex items-center gap-1.5';
        }
      });
    }

    renderMaterialPickerGrid();
    setTimeout(() => {
      if (elements.materialSearchInput) elements.materialSearchInput.focus();
    }, 50);
  }

  function closeMaterialPicker() {
    if (elements.materialPickerModal) {
      elements.materialPickerModal.classList.add('hidden');
      elements.materialPickerModal.classList.remove('flex');
    }
    currentPickerCallback = null;
  }

  function renderMaterialPickerGrid() {
    if (!elements.materialGridContainer) return;
    elements.materialGridContainer.innerHTML = '';
    const query = currentPickerQuery.toLowerCase().trim();

    // Collect matched Thai alias English keywords
    const matchedEnKeywords = new Set();
    if (query) {
      let tempQ = query;
      for (let i = 0; i < THAI_ITEM_ALIASES.length; i++) {
        const [thWord, enList] = THAI_ITEM_ALIASES[i];
        if (tempQ.includes(thWord)) {
          enList.forEach(w => matchedEnKeywords.add(w));
          tempQ = tempQ.split(thWord).join('');
        }
      }
    }

    currentFilteredPickerItems = mcItemsList.filter(item => {
      const rawId = (item.id || '').toLowerCase();
      const cleanId = rawId.replace(/^minecraft:/, '');
      const readable = (item.readable || '').toLowerCase();

      if (currentPickerFilter !== 'ALL') {
        const cat = categorizeMinecraftItem(item);
        if (cat !== currentPickerFilter) return false;
      }

      if (query) {
        if (cleanId.includes(query) || readable.includes(query) || rawId.includes(query)) {
          return true;
        }
        if (matchedEnKeywords.size > 0) {
          for (const enWord of matchedEnKeywords) {
            if (cleanId.includes(enWord)) {
              if (enWord === 'bow' && cleanId.includes('bowl')) continue;
              return true;
            }
          }
        }
        return false;
      }
      return true;
    });

    currentPickerBatchIndex = 0;
    elements.materialGridContainer.scrollTop = 0;

    if (currentFilteredPickerItems.length === 0) {
      if (elements.materialCountDisplay) {
        elements.materialCountDisplay.innerHTML = `พบ <span class="font-bold text-rose-400">0</span> รายการ (จากทั้งหมด ${mcItemsList.length} รายการใน Paper 26.3)`;
      }
      elements.materialGridContainer.innerHTML = `
        <div class="col-span-full py-12 text-center text-zinc-500 text-xs">
          ไม่พบไอเทมที่ตรงกับคำค้นหา "${escapeHtml(query)}"
        </div>
      `;
      return;
    }

    renderNextPickerBatch();
  }

  function renderNextPickerBatch() {
    if (!elements.materialGridContainer) return;
    if (currentPickerBatchIndex >= currentFilteredPickerItems.length) return;

    // Remove any previous load indicator
    const existingIndicator = document.getElementById('pickerScrollIndicator');
    if (existingIndicator) existingIndicator.remove();

    const batch = currentFilteredPickerItems.slice(currentPickerBatchIndex, currentPickerBatchIndex + PICKER_BATCH_SIZE);
    currentPickerBatchIndex += batch.length;

    const fragment = document.createDocumentFragment();

    batch.forEach(item => {
      const rawId = item.id || '';
      const clean = rawId.replace(/^minecraft:/, '').toLowerCase();
      const bukkitMat = clean.toUpperCase();
      const isSelected = (currentPickerSelected === bukkitMat);

      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = `group flex flex-col items-center justify-center p-2 rounded-xl bg-zinc-900/90 border ${
        isSelected ? 'border-indigo-500 ring-2 ring-indigo-500/50 bg-indigo-950/20' : 'border-zinc-800 hover:border-indigo-500 hover:bg-zinc-850'
      } transition cursor-pointer text-center relative select-none`;
      btn.title = `${item.readable || clean} (${bukkitMat})`;

      const tex = item.texture || getMinecraftItemTexture(clean);

      btn.innerHTML = `
        <div class="w-10 h-10 flex items-center justify-center p-1 rounded-lg bg-zinc-950/80 border border-zinc-800/80 group-hover:border-indigo-500/40 transition">
          <img src="${tex}" class="w-8 h-8 mc-icon object-contain group-hover:scale-110 transition-transform" alt="${clean}" loading="lazy" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/block/${clean}.png';">
        </div>
        <span class="text-[10px] font-semibold text-zinc-300 group-hover:text-white mt-1.5 line-clamp-1 w-full truncate">${escapeHtml(item.readable || clean)}</span>
        <span class="text-[8px] font-mono text-zinc-500 group-hover:text-zinc-400 w-full truncate">${escapeHtml(bukkitMat)}</span>
      `;

      btn.addEventListener('click', () => {
        if (currentPickerCallback) {
          currentPickerCallback(bukkitMat, item);
        }
        closeMaterialPicker();
      });

      fragment.appendChild(btn);
    });

    elements.materialGridContainer.appendChild(fragment);

    // Indicator & count display
    if (elements.materialCountDisplay) {
      elements.materialCountDisplay.innerHTML = `แสดง <span class="font-bold text-white">${currentPickerBatchIndex}</span> / <span class="font-bold text-indigo-400">${currentFilteredPickerItems.length}</span> รายการ (จากทั้งหมด ${mcItemsList.length} รายการใน Paper 26.3)`;
    }

    if (currentPickerBatchIndex < currentFilteredPickerItems.length) {
      const moreInfo = document.createElement('div');
      moreInfo.id = 'pickerScrollIndicator';
      moreInfo.className = 'col-span-full py-3 text-center text-zinc-500 text-[11px] flex items-center justify-center gap-1.5';
      moreInfo.innerHTML = `<span>เลื่อนลงเพื่อดูไอเทมเพิ่มเติม... (${currentPickerBatchIndex}/${currentFilteredPickerItems.length})</span>`;
      elements.materialGridContainer.appendChild(moreInfo);
    } else if (currentFilteredPickerItems.length > PICKER_BATCH_SIZE) {
      const doneInfo = document.createElement('div');
      doneInfo.id = 'pickerScrollIndicator';
      doneInfo.className = 'col-span-full py-2.5 text-center text-zinc-600 text-[11px]';
      doneInfo.textContent = `แสดงครบทั้งหมด ${currentFilteredPickerItems.length} รายการแล้ว`;
      elements.materialGridContainer.appendChild(doneInfo);
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
          <a href="#file-${f.path}" class="nav-item flex-1 flex items-center justify-between px-3 py-2 text-xs font-medium text-zinc-400 group-hover:text-white transition min-w-0" data-file="${f.path}">
            <div class="flex items-center gap-2 truncate mr-2 min-w-0">
              ${SVG_ICONS.box}
              <span class="truncate">${escapeHtml(f.name)}</span>
            </div>
            <span class="font-mono text-[10px] text-zinc-500 truncate max-w-[85px] text-right flex-shrink-0" title="${escapeHtml(f.path)}">${escapeHtml(f.path)}</span>
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
   * Rarity ranks and styling helpers (F, D, C, B, A, S)
   */
  const RARITY_RANKS = ['F', 'D', 'C', 'B', 'A', 'S'];

  const RARITY_BADGE_STYLES = {
    F: 'bg-zinc-800 text-zinc-300 border-zinc-700',
    D: 'bg-emerald-950/60 text-emerald-400 border-emerald-800',
    C: 'bg-cyan-950/60 text-cyan-400 border-cyan-800',
    B: 'bg-purple-950/60 text-purple-400 border-purple-800',
    A: 'bg-amber-950/60 text-amber-400 border-amber-800',
    S: 'bg-rose-950/70 text-rose-300 border-rose-700/80 font-black shadow-sm ring-1 ring-rose-500/30'
  };

  function normalizeRarity(val) {
    if (!val) return 'F';
    const u = String(val).trim().toUpperCase();
    if (u === 'COMMON') return 'F';
    if (u === 'UNCOMMON') return 'D';
    if (u === 'RARE') return 'C';
    if (u === 'EPIC') return 'B';
    if (u === 'LEGENDARY') return 'A';
    if (u === 'MYTHIC') return 'S';
    if (RARITY_RANKS.includes(u)) return u;
    return 'F';
  }

  /**
   * Container Rarity Chance Presets (All guaranteed to sum to exactly 100.0%)
   * Red Rank S is carefully tuned to 0% for low tiers and strictly 5% - 10% for high-tier vaults/airdrops.
   */
  const RARITY_CHANCE_PRESETS = [
    {
      id: 'basic',
      name: 'กล่องทั่วไป',
      icon: '📦',
      desc: 'เน้นของพื้นฐาน F/D (S 0%)',
      chances: { F: 70.0, D: 25.0, C: 5.0, B: 0.0, A: 0.0, S: 0.0 }
    },
    {
      id: 'supply',
      name: 'กล่องเสบียง',
      icon: '🧰',
      desc: 'สมดุลระดับกลาง F/D/C (S 0%)',
      chances: { F: 45.0, D: 35.0, C: 15.0, B: 5.0, A: 0.0, S: 0.0 }
    },
    {
      id: 'military',
      name: 'กล่องทหาร',
      icon: '🎖️',
      desc: 'ยุทโธปกรณ์ระดับกลาง-สูง D/C/B/A (S 0%)',
      chances: { F: 20.0, D: 35.0, C: 25.0, B: 15.0, A: 5.0, S: 0.0 }
    },
    {
      id: 'safe',
      name: 'ตู้เซฟ (S 5%)',
      icon: '🔒',
      desc: 'ของมีค่า C/B/A พร้อมโอกาสแดง S 5%',
      chances: { F: 10.0, D: 20.0, C: 35.0, B: 20.0, A: 10.0, S: 5.0 }
    },
    {
      id: 'airdrop',
      name: 'แอร์ดรอป (S 8%)',
      icon: '🪂',
      desc: 'กล่องบินเสบียงพิเศษ แดง S 8%',
      chances: { F: 5.0, D: 15.0, C: 25.0, B: 30.0, A: 17.0, S: 8.0 }
    },
    {
      id: 'boss',
      name: 'กล่องบอส (S 10%)',
      icon: '👑',
      desc: 'ระดับสูงสุด แดง S 10% เต็มพิกัด',
      chances: { F: 0.0, D: 10.0, C: 20.0, B: 35.0, A: 25.0, S: 10.0 }
    }
  ];

  /**
   * Proportionally balances rarity drop chances to ensure total sum is strictly 100.0%.
   * Caps Red Rank S to maximum 10.0% if higher.
   */
  function balanceChancesTo100(chances) {
    const ranks = ['F', 'D', 'C', 'B', 'A', 'S'];
    const current = {};
    ranks.forEach(r => {
      current[r] = Math.max(0, parseFloat(chances[r]) || 0);
    });

    let sum = ranks.reduce((acc, r) => acc + current[r], 0);
    if (sum <= 0) {
      return { F: 70.0, D: 25.0, C: 5.0, B: 0.0, A: 0.0, S: 0.0 };
    }

    const balanced = {};
    let distributed = 0;
    ranks.forEach((r, idx) => {
      if (idx === ranks.length - 1) {
        balanced[r] = Math.max(0, parseFloat((100.0 - distributed).toFixed(1)));
      } else {
        const raw = (current[r] / sum) * 100.0;
        const rounded = Math.round(raw * 10.0) / 10.0;
        balanced[r] = rounded;
        distributed += rounded;
      }
    });

    // Check if S exceeds 10%
    if (balanced.S > 10.0) {
      const excess = parseFloat((balanced.S - 10.0).toFixed(1));
      balanced.S = 10.0;
      balanced.A = parseFloat((balanced.A + excess).toFixed(1));
    }

    return balanced;
  }

  function updateRarityChancesBadge(badgeEl, sum) {
    if (!badgeEl) return;
    const is100 = Math.abs(sum - 100.0) < 0.1;
    badgeEl.textContent = `รวม ${sum.toFixed(1)}%`;
    if (is100) {
      badgeEl.className = 'cat-total-badge text-[10px] font-mono px-2 py-0.5 rounded-full border transition font-bold bg-emerald-950/60 text-emerald-400 border-emerald-800';
    } else {
      badgeEl.className = 'cat-total-badge text-[10px] font-mono px-2 py-0.5 rounded-full border transition font-bold bg-amber-950/60 text-amber-400 border-amber-800';
    }
  }

  /**
   * 16 Standard Minecraft Colors Palette with Chat Codes and Hex values
   */
  const MINECRAFT_COLORS = [
    { code: '0', hex: '#000000', name: 'Black', nameTh: 'ดำ' },
    { code: '1', hex: '#0000AA', name: 'Dark Blue', nameTh: 'น้ำเงินเข้ม' },
    { code: '2', hex: '#00AA00', name: 'Dark Green', nameTh: 'เขียวเข้ม' },
    { code: '3', hex: '#00AAAA', name: 'Dark Aqua', nameTh: 'ฟ้าคราม' },
    { code: '4', hex: '#AA0000', name: 'Dark Red', nameTh: 'แดงเข้ม' },
    { code: '5', hex: '#AA00AA', name: 'Dark Purple', nameTh: 'ม่วงเข้ม' },
    { code: '6', hex: '#FFAA00', name: 'Gold', nameTh: 'ทอง' },
    { code: '7', hex: '#AAAAAA', name: 'Gray', nameTh: 'เทา' },
    { code: '8', hex: '#555555', name: 'Dark Gray', nameTh: 'เทาเข้ม' },
    { code: '9', hex: '#5555FF', name: 'Blue', nameTh: 'น้ำเงิน' },
    { code: 'a', hex: '#55FF55', name: 'Green', nameTh: 'เขียวสว่าง' },
    { code: 'b', hex: '#55FFFF', name: 'Aqua', nameTh: 'ฟ้าสว่าง' },
    { code: 'c', hex: '#FF5555', name: 'Red', nameTh: 'แดง' },
    { code: 'd', hex: '#FF55FF', name: 'Light Purple', nameTh: 'ชมพูม่วง' },
    { code: 'e', hex: '#FFFF55', name: 'Yellow', nameTh: 'เหลือง' },
    { code: 'f', hex: '#FFFFFF', name: 'White', nameTh: 'ขาว' }
  ];

  function normalizeHexColor(input) {
    if (!input || typeof input !== 'string') return '#AAAAAA';
    let str = input.trim();
    if (!str.startsWith('#')) str = '#' + str;
    if (/^#[0-9A-Fa-f]{3}$/.test(str)) {
      str = '#' + str[1] + str[1] + str[2] + str[2] + str[3] + str[3];
    }
    return str.toUpperCase();
  }

  function isLightColor(hex) {
    const clean = (hex || '').replace('#', '');
    if (clean.length !== 6) return false;
    const r = parseInt(clean.substring(0, 2), 16) || 0;
    const g = parseInt(clean.substring(2, 4), 16) || 0;
    const b = parseInt(clean.substring(4, 6), 16) || 0;
    return (r * 299 + g * 587 + b * 114) / 1000 > 140;
  }

  function findMinecraftColor(hex) {
    if (!hex) return null;
    const clean = normalizeHexColor(hex).toUpperCase();
    return MINECRAFT_COLORS.find(c => c.hex.toUpperCase() === clean) || null;
  }

  /**
   * Renders an interactive Minecraft Hex Color Picker component
   * into a target container element.
   */
  function renderMinecraftColorPicker(containerEl, initialHex, onChange) {
    if (!containerEl) return null;
    let currentHex = normalizeHexColor(initialHex || '#AAAAAA');

    containerEl.innerHTML = `
      <div class="mc-color-picker-widget bg-zinc-950/90 border border-zinc-800/80 rounded-lg p-2.5 space-y-2.5">
        <!-- Input Row -->
        <div class="flex items-center gap-2">
          <!-- Color Preview Box & Native Picker -->
          <div class="relative w-8 h-8 rounded-lg border border-zinc-700/80 shadow-inner flex-shrink-0 overflow-hidden cursor-pointer group" title="คลิกเพื่อเลือกสีอิสระผ่านวงล้อสี">
            <div class="mc-cp-preview w-full h-full transition" style="background-color: ${currentHex}"></div>
            <input type="color" class="mc-cp-native absolute inset-0 opacity-0 cursor-pointer w-full h-full" value="${/^#[0-9A-Fa-f]{6}$/.test(currentHex) ? currentHex : '#AAAAAA'}">
          </div>

          <!-- Hex Input with # prefix -->
          <div class="relative flex-1">
            <span class="absolute left-2.5 top-1/2 -translate-y-1/2 text-xs font-mono text-zinc-500 font-bold select-none">#</span>
            <input type="text" maxlength="7" class="mc-cp-text w-full pl-6 pr-2 py-1.5 bg-zinc-900 border border-zinc-700 rounded-md text-xs font-mono text-zinc-100 outline-none focus:border-white transition uppercase font-semibold" value="${currentHex.replace('#', '')}" placeholder="RRGGBB">
          </div>

          <!-- Matching Minecraft Color Tag -->
          <div class="mc-cp-badge px-2.5 py-1 rounded bg-zinc-900 border border-zinc-800 text-[10px] font-mono text-zinc-400 whitespace-nowrap min-w-[76px] text-center shadow-sm">
            -
          </div>
        </div>

        <!-- 16 Minecraft Colors Grid -->
        <div>
          <div class="flex items-center justify-between mb-1.5 px-0.5">
            <span class="text-[10px] font-medium text-zinc-400 flex items-center gap-1">
              <span>🎨 สีมาตรฐาน Minecraft (16 สี):</span>
            </span>
            <span class="text-[9px] text-zinc-500">คลิกเลือกสีทันที</span>
          </div>
          <div class="grid grid-cols-8 gap-1.5 mc-cp-palette">
            ${MINECRAFT_COLORS.map(c => {
              const isSelected = c.hex.toUpperCase() === currentHex.toUpperCase();
              const isLight = isLightColor(c.hex);
              const checkColor = isLight ? '#000000' : '#FFFFFF';
              return `
                <button type="button" data-hex="${c.hex}" data-code="${c.code}" class="mc-cp-swatch relative h-6 rounded border border-zinc-700/80 transition transform hover:scale-110 active:scale-95 cursor-pointer flex items-center justify-center ${isSelected ? 'ring-2 ring-indigo-400 ring-offset-1 ring-offset-zinc-950 scale-105' : 'hover:border-zinc-400'}" style="background-color: ${c.hex};" title="§${c.code} ${c.name} (${c.nameTh}) ${c.hex}">
                  <span class="mc-cp-check ${isSelected ? 'block' : 'hidden'}" style="color: ${checkColor}; font-size: 10px; font-weight: bold; line-height: 1;">✓</span>
                </button>
              `;
            }).join('')}
          </div>
        </div>
      </div>
    `;

    const previewEl = containerEl.querySelector('.mc-cp-preview');
    const nativeInput = containerEl.querySelector('.mc-cp-native');
    const textInput = containerEl.querySelector('.mc-cp-text');
    const badgeEl = containerEl.querySelector('.mc-cp-badge');
    const swatches = containerEl.querySelectorAll('.mc-cp-swatch');

    function updateUI(hex, notify = true) {
      currentHex = normalizeHexColor(hex);
      if (previewEl) previewEl.style.backgroundColor = currentHex;
      if (nativeInput && /^#[0-9A-Fa-f]{6}$/.test(currentHex)) {
        nativeInput.value = currentHex;
      }
      const rawHexClean = currentHex.replace('#', '');
      if (textInput && textInput.value.toUpperCase() !== rawHexClean) {
        textInput.value = rawHexClean;
      }

      // Update Minecraft badge
      const mcMatch = findMinecraftColor(currentHex);
      if (badgeEl) {
        if (mcMatch) {
          badgeEl.innerHTML = `<span class="font-bold text-amber-400">§${mcMatch.code}</span> <span class="text-zinc-200">${mcMatch.name}</span>`;
          badgeEl.className = 'mc-cp-badge px-2.5 py-1 rounded bg-zinc-900 border border-zinc-700 text-[10px] font-mono whitespace-nowrap min-w-[76px] text-center shadow-sm';
        } else {
          badgeEl.innerHTML = `<span class="text-zinc-400 font-mono">Hex Custom</span>`;
          badgeEl.className = 'mc-cp-badge px-2.5 py-1 rounded bg-zinc-900/60 border border-zinc-800 text-[10px] font-mono text-zinc-400 whitespace-nowrap min-w-[76px] text-center';
        }
      }

      // Update swatch rings and checks
      swatches.forEach(swatch => {
        const swatchHex = swatch.getAttribute('data-hex');
        const isMatch = swatchHex && swatchHex.toUpperCase() === currentHex.toUpperCase();
        const check = swatch.querySelector('.mc-cp-check');
        if (isMatch) {
          swatch.classList.add('ring-2', 'ring-indigo-400', 'ring-offset-1', 'ring-offset-zinc-950', 'scale-105');
          swatch.classList.remove('hover:border-zinc-400');
          if (check) check.classList.remove('hidden');
        } else {
          swatch.classList.remove('ring-2', 'ring-indigo-400', 'ring-offset-1', 'ring-offset-zinc-950', 'scale-105');
          swatch.classList.add('hover:border-zinc-400');
          if (check) check.classList.add('hidden');
        }
      });

      if (notify && typeof onChange === 'function') {
        onChange(currentHex);
      }
    }

    // Initialize UI
    updateUI(currentHex, false);

    // Event listeners
    if (nativeInput) {
      nativeInput.addEventListener('input', (e) => {
        updateUI(e.target.value.toUpperCase(), true);
      });
    }

    if (textInput) {
      textInput.addEventListener('input', (e) => {
        let val = e.target.value.trim();
        if (val.startsWith('#')) val = val.substring(1);
        val = val.replace(/[^0-9A-Fa-f]/g, '');
        e.target.value = val.toUpperCase();
        if (val.length === 6 || val.length === 3) {
          updateUI('#' + val, true);
        }
      });
      textInput.addEventListener('blur', (e) => {
        let val = e.target.value.trim();
        if (!val) val = 'AAAAAA';
        updateUI('#' + val, true);
      });
    }

    swatches.forEach(swatch => {
      swatch.addEventListener('click', (e) => {
        e.stopPropagation();
        const hex = swatch.getAttribute('data-hex');
        if (hex) {
          updateUI(hex, true);
        }
      });
    });

    return {
      setColor: (newHex) => updateUI(newHex, false),
      getColor: () => currentHex
    };
  }

  /**
   * Container Category Preloader & Cache for interactive container selector
   */
  let cachedCategoryList = [];

  async function preloadCategories() {
    try {
      const res = await apiFetch('/api/config?file=categories.yml');
      if (res && res.success && res.parsed && res.parsed.categories) {
        const cats = res.parsed.categories;
        cachedCategoryList = Object.keys(cats).map(id => {
          const cat = cats[id] || {};
          const rawName = cat.name || cat.display_name || id;
          const cleanName = rawName.replace(/<[^>]+>/g, '').trim();
          return {
            id: id,
            displayName: cleanName || id,
            color: cat.color || '#888888'
          };
        });
      }
    } catch (err) {
      console.warn('Could not preload categories.yml:', err);
    }
  }

  function getKnownContainerCategories(extraList = []) {
    const list = [...cachedCategoryList];
    const knownIds = new Set(list.map(c => c.id));

    // Fallback defaults if not loaded yet
    const defaults = [
      { id: 'normal_crate', displayName: 'กล่องธรรมดา (Normal Crate)', color: '#AAAAAA' },
      { id: 'airdrop', displayName: 'กล่องแอร์ดรอป (Supply Airdrop)', color: '#00D2FF' },
      { id: 'safe_box', displayName: 'กล่องตู้เซฟ (Armored Safe)', color: '#FFAA00' },
      { id: 'military_crate', displayName: 'กล่องทหาร (Military Crate)', color: '#55FF55' },
      { id: 'medical_crate', displayName: 'กล่องพยาบาล (Medical Crate)', color: '#FF5555' }
    ];
    if (list.length === 0) {
      defaults.forEach(d => {
        if (!knownIds.has(d.id)) {
          list.push(d);
          knownIds.add(d.id);
        }
      });
    }

    if (Array.isArray(extraList)) {
      extraList.forEach(id => {
        const trimmed = String(id).trim();
        if (trimmed && !knownIds.has(trimmed)) {
          list.push({ id: trimmed, displayName: trimmed, color: '#A1A1AA' });
          knownIds.add(trimmed);
        }
      });
    }

    return list;
  }

  /**
   * Interactive Container Tag / Chip Selector Component
   * Allows clicking to add/remove container drop targets without typing.
   */
  function renderContainerChipSelector(containerEl, currentList, onUpdate) {
    let selected = Array.isArray(currentList) ? [...currentList] : (currentList ? [currentList] : []);

    function render() {
      const allKnown = getKnownContainerCategories(selected);
      containerEl.innerHTML = `
        <div class="space-y-1.5">
          <div class="flex items-center justify-between">
            <label class="text-[11px] font-semibold text-zinc-300 flex items-center gap-1.5">
              <span>กล่องที่ไอเทมนี้จะดรอป:</span>
              <span class="text-[10px] font-mono px-1.5 py-0.5 rounded ${selected.length > 0 ? 'bg-indigo-950/80 text-indigo-300 border border-indigo-800' : 'bg-zinc-800 text-zinc-500'}">
                ${selected.length} กล่อง
              </span>
            </label>
            <div class="flex items-center gap-1.5 text-[10px]">
              <button type="button" class="btn-select-all px-2 py-0.5 rounded bg-zinc-800 hover:bg-zinc-700 text-zinc-300 hover:text-white transition cursor-pointer">
                เลือกทั้งหมด
              </button>
              <button type="button" class="btn-clear-all px-2 py-0.5 rounded bg-zinc-800 hover:bg-zinc-700 text-zinc-400 hover:text-red-400 transition cursor-pointer">
                ล้างทั้งหมด
              </button>
            </div>
          </div>
          <div class="flex flex-wrap gap-1.5 min-h-[34px] p-2 bg-zinc-950/90 border border-zinc-800/80 rounded-xl items-center">
            ${allKnown.map(cat => {
              const isSelected = selected.includes(cat.id);
              if (isSelected) {
                return `
                  <button type="button" data-cat-id="${escapeHtml(cat.id)}" class="chip-toggle px-2.5 py-1 rounded-lg text-[11px] font-medium flex items-center gap-1.5 border transition cursor-pointer active:scale-95 bg-indigo-950/80 border-indigo-500/70 text-indigo-200 shadow-sm hover:bg-indigo-900" title="คลิกเพื่อยกเลิกการดรอปในกล่องนี้">
                    <span class="w-1.5 h-1.5 rounded-full" style="background-color: ${cat.color || '#6366F1'}"></span>
                    <span class="font-mono">${escapeHtml(cat.id)}</span>
                    <svg class="w-3.5 h-3.5 text-indigo-400 hover:text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  </button>
                `;
              } else {
                return `
                  <button type="button" data-cat-id="${escapeHtml(cat.id)}" class="chip-toggle px-2.5 py-1 rounded-lg text-[11px] font-medium flex items-center gap-1.5 border transition cursor-pointer active:scale-95 bg-zinc-900/60 border-zinc-800 text-zinc-400 hover:border-zinc-700 hover:text-zinc-200 hover:bg-zinc-850" title="คลิกเพื่อเพิ่มการดรอปในกล่องนี้">
                    <span class="w-1.5 h-1.5 rounded-full opacity-50" style="background-color: ${cat.color || '#71717A'}"></span>
                    <span class="font-mono">${escapeHtml(cat.id)}</span>
                    <span class="text-zinc-500 text-xs font-bold">+</span>
                  </button>
                `;
              }
            }).join('')}
            <div class="inline-flex items-center gap-1 ml-auto">
              <input type="text" placeholder="+ กล่องอื่น" class="custom-cat-input h-6 px-2 bg-zinc-900 border border-zinc-800 rounded-md text-[10px] font-mono text-zinc-200 placeholder-zinc-500 outline-none focus:border-indigo-500 w-24">
              <button type="button" class="btn-add-custom h-6 px-2 rounded bg-zinc-800 hover:bg-zinc-700 text-zinc-300 hover:text-white text-[10px] font-medium transition cursor-pointer">
                + เพิ่ม
              </button>
            </div>
          </div>
        </div>
      `;

      containerEl.querySelectorAll('.chip-toggle').forEach(btn => {
        btn.addEventListener('click', () => {
          const cid = btn.getAttribute('data-cat-id');
          if (selected.includes(cid)) {
            selected = selected.filter(x => x !== cid);
          } else {
            selected.push(cid);
          }
          onUpdate(selected);
          render();
        });
      });

      const btnSelectAll = containerEl.querySelector('.btn-select-all');
      if (btnSelectAll) {
        btnSelectAll.addEventListener('click', () => {
          selected = allKnown.map(c => c.id);
          onUpdate(selected);
          render();
        });
      }

      const btnClearAll = containerEl.querySelector('.btn-clear-all');
      if (btnClearAll) {
        btnClearAll.addEventListener('click', () => {
          selected = [];
          onUpdate(selected);
          render();
        });
      }

      const customInput = containerEl.querySelector('.custom-cat-input');
      const btnAddCustom = containerEl.querySelector('.btn-add-custom');
      const handleAddCustom = () => {
        const val = customInput.value.trim().toLowerCase().replace(/\s+/g, '_');
        if (val && !selected.includes(val)) {
          selected.push(val);
          onUpdate(selected);
          render();
        }
      };
      if (btnAddCustom && customInput) {
        btnAddCustom.addEventListener('click', handleAddCustom);
        customInput.addEventListener('keydown', (e) => {
          if (e.key === 'Enter') {
            e.preventDefault();
            handleAddCustom();
          }
        });
      }
    }

    render();
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
        const r = normalizeRarity(item.rarity || item.tier || 'F');
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
          <select id="itemRarityFilterSelect" class="h-7 px-2 bg-zinc-950 border border-zinc-800 rounded-md text-xs font-medium text-zinc-300 outline-none focus:border-zinc-500 cursor-pointer">
            <option value="ALL" ${itemRarityFilter === 'ALL' ? 'selected' : ''}>ทุกระดับความหายาก</option>
            <option value="F" ${itemRarityFilter === 'F' ? 'selected' : ''}>แรงค์ F (สีเทา)</option>
            <option value="D" ${itemRarityFilter === 'D' ? 'selected' : ''}>แรงค์ D (สีเขียว)</option>
            <option value="C" ${itemRarityFilter === 'C' ? 'selected' : ''}>แรงค์ C (สีฟ้า)</option>
            <option value="B" ${itemRarityFilter === 'B' ? 'selected' : ''}>แรงค์ B (สีม่วง)</option>
            <option value="A" ${itemRarityFilter === 'A' ? 'selected' : ''}>แรงค์ A (สีทอง)</option>
            <option value="S" ${itemRarityFilter === 'S' ? 'selected' : ''}>แรงค์ S (สีแดง)</option>
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

      const rarity = normalizeRarity(item.rarity || item.tier || 'F');
      const rarityBadgeColor = RARITY_BADGE_STYLES[rarity] || RARITY_BADGE_STYLES.F;

      const loreStr = Array.isArray(item.lore)
        ? item.lore.join('\n')
        : (item.lore || '');

      card.innerHTML = `
        <div class="flex items-center justify-between pb-3 border-b border-zinc-800">
          <div class="flex items-center gap-3 min-w-0">
            <button type="button" class="w-12 h-12 rounded-xl border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1.5 transition select-card-mat-btn cursor-pointer flex-shrink-0 group" title="คลิกเพื่อเลือกไอเทมในเกม">
              <img class="w-8 h-8 mc-icon object-contain group-hover:scale-110 transition-transform item-card-icon-img" src="${getMinecraftItemTexture(item.material || 'STONE')}" alt="${escapeHtml(item.material || 'STONE')}" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/block/${(item.material||'stone').toLowerCase()}.png';">
            </button>
            <div class="min-w-0">
              <div class="font-bold text-xs text-white truncate" title="${itemId}">${escapeHtml(itemId)}</div>
              <div class="flex items-center gap-1.5 mt-0.5">
                <span class="text-[10px] font-mono text-zinc-400 bg-zinc-950 px-1.5 py-0.5 rounded border border-zinc-800 truncate item-card-mat-badge">${escapeHtml(item.material || 'STONE')}</span>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-1.5 flex-shrink-0">
            <span class="text-[10px] font-bold px-2 py-0.5 rounded-full border ${rarityBadgeColor}">
              แรงค์ ${escapeHtml(rarity)}
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
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">ชนิดไอเทม (Material ในเกม):</label>
              <div class="flex items-center gap-1.5">
                <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition item-material" value="${escapeHtml(item.material || 'STONE')}">
                <button type="button" class="px-2 py-1.5 rounded-lg bg-zinc-800 hover:bg-indigo-600 hover:text-white text-zinc-300 text-xs font-medium border border-zinc-700/60 pick-card-mat-btn transition cursor-pointer flex-shrink-0 flex items-center gap-1" title="เปิดหน้าต่างเลือกไอเทมในเกมทั้งหมด">
                  <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg>
                  <span>เลือก</span>
                </button>
              </div>
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">ระดับความหายาก (Rarity):</label>
              <select class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-bold text-zinc-100 outline-none focus:border-white transition item-rarity cursor-pointer">
                ${RARITY_RANKS.map(r => `<option value="${r}" ${r === rarity ? 'selected' : ''}>แรงค์ ${r}</option>`).join('')}
              </select>
            </div>
          </div>

          <!-- Interactive Container Dropdown / Chip Selector -->
          <div class="item-containers-slot pt-1"></div>

          <div>
            <label class="text-[11px] font-medium text-zinc-400 block mb-1">คำอธิบายใต้ไอเทม (Lore):</label>
            <textarea rows="2" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition item-lore leading-relaxed resize-y" placeholder="1 บรรทัดต่อ 1 ข้อความ">${escapeHtml(loreStr)}</textarea>
          </div>
        </div>
      `;

      // Mount interactive container chip selector
      const currentCont = Array.isArray(item.allowed_containers)
        ? [...item.allowed_containers]
        : (item.allowed_containers ? [item.allowed_containers] : []);

      const containersSlot = card.querySelector('.item-containers-slot');
      if (containersSlot) {
        renderContainerChipSelector(containersSlot, currentCont, (updatedList) => {
          item.allowed_containers = updatedList;
          updateSettingValue(`items.${itemId}.allowed_containers`, updatedList);
        });
      }

      const updateCardMaterial = (newMat) => {
        newMat = newMat.toUpperCase().trim();
        card.querySelector('.item-material').value = newMat;
        card.querySelector('.item-card-mat-badge').textContent = newMat;
        const iconImg = card.querySelector('.item-card-icon-img');
        if (iconImg) {
          iconImg.src = getMinecraftItemTexture(newMat);
          iconImg.alt = newMat;
        }
        updateSettingValue(`items.${itemId}.material`, newMat);
      };

      card.querySelector('.select-card-mat-btn').addEventListener('click', () => {
        openMaterialPicker({
          currentMaterial: card.querySelector('.item-material').value || 'STONE',
          title: `เลือกไอเทมใหม่สำหรับ [${itemId}]`,
          onSelect: (selectedMat) => updateCardMaterial(selectedMat)
        });
      });

      card.querySelector('.pick-card-mat-btn').addEventListener('click', () => {
        openMaterialPicker({
          currentMaterial: card.querySelector('.item-material').value || 'STONE',
          title: `เลือกไอเทมใหม่สำหรับ [${itemId}]`,
          onSelect: (selectedMat) => updateCardMaterial(selectedMat)
        });
      });

      card.querySelector('.item-material').addEventListener('input', (e) => {
        const val = e.target.value.toUpperCase().trim();
        card.querySelector('.item-card-mat-badge').textContent = val || 'STONE';
        const iconImg = card.querySelector('.item-card-icon-img');
        if (iconImg) {
          iconImg.src = getMinecraftItemTexture(val);
        }
        updateSettingValue(`items.${itemId}.material`, val);
      });
      card.querySelector('.item-rarity').addEventListener('change', (e) => {
        const newRarity = e.target.value;
        updateSettingValue(`items.${itemId}.rarity`, newRarity);
        updateSettingValue(`items.${itemId}.tier`, newRarity);
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
            <span class="w-3.5 h-3.5 rounded-full border border-zinc-700 shadow-sm cat-header-dot transition" style="background-color: ${cat.color || '#AAAAAA'}"></span>
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

          <div>
            <label class="text-[11px] font-medium text-zinc-400 block mb-1">สีประจำกล่อง (Color Hex & Minecraft Colors):</label>
            <div class="cat-color-picker-slot"></div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">จำนวน Min:</label>
              <input type="number" min="1" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white cat-min-rolls" value="${cat.min_rolls || 1}">
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">จำนวน Max:</label>
              <input type="number" min="1" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white cat-max-rolls" value="${cat.max_rolls || 4}">
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
              <div class="flex items-center gap-1.5">
                <div class="w-8 h-8 rounded-lg border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1 flex-shrink-0">
                  <img class="w-6 h-6 mc-icon object-contain cat-covered-icon" src="${getMinecraftItemTexture(cat.covered_material || 'BLACK_STAINED_GLASS_PANE')}" alt="" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/block/${(cat.covered_material||'black_stained_glass_pane').toLowerCase()}.png';">
                </div>
                <input type="text" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition cat-covered" value="${escapeHtml(cat.covered_material || 'BLACK_STAINED_GLASS_PANE')}">
                <button type="button" class="px-2 py-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 pick-cat-covered-btn transition cursor-pointer flex-shrink-0" title="เลือกบล็อก">
                  เลือก
                </button>
              </div>
            </div>
            <div>
              <label class="text-[11px] font-medium text-zinc-400 block mb-1">บล็อกค้นหา (Searching):</label>
              <div class="flex items-center gap-1.5">
                <div class="w-8 h-8 rounded-lg border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1 flex-shrink-0">
                  <img class="w-6 h-6 mc-icon object-contain cat-searching-icon" src="${getMinecraftItemTexture(cat.searching_material || 'STONE')}" alt="" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/block/${(cat.searching_material||'stone').toLowerCase()}.png';">
                </div>
                <input type="text" class="w-full px-2 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition cat-searching" value="${escapeHtml(cat.searching_material || 'STONE')}">
                <button type="button" class="px-2 py-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 pick-cat-searching-btn transition cursor-pointer flex-shrink-0" title="เลือกบล็อก">
                  เลือก
                </button>
              </div>
            </div>
          </div>

          <div class="pt-2.5 border-t border-zinc-800/80 space-y-2">
            <div class="flex items-center justify-between gap-2 flex-wrap">
              <div class="flex items-center gap-2">
                <label class="text-[11px] font-semibold text-zinc-300">โอกาสออกความหายาก (Rarity Chances %):</label>
                <span class="cat-total-badge text-[10px] font-mono px-2 py-0.5 rounded-full border transition font-bold bg-zinc-800 text-zinc-400 border-zinc-700">
                  รวม 0%
                </span>
              </div>
              <button type="button" class="btn-balance-cat px-2 py-0.5 rounded bg-indigo-950/80 hover:bg-indigo-900 border border-indigo-700/60 text-indigo-300 text-[10px] font-medium flex items-center gap-1 transition cursor-pointer active:scale-95" title="ปรับสัดส่วนทุกแรงค์ให้รวมได้ 100% พอดี">
                <span>⚡ ปรับให้ครบ 100%</span>
              </button>
            </div>

            <!-- Presets Bar -->
            <div class="flex flex-wrap items-center gap-1 p-1.5 bg-zinc-950/80 border border-zinc-800/80 rounded-lg">
              <span class="text-[10px] text-zinc-500 font-semibold px-1">พรีเซ็ทเรท:</span>
              ${RARITY_CHANCE_PRESETS.map(p => `
                <button type="button" data-preset="${p.id}" class="cat-preset-btn px-2 py-0.5 rounded text-[10px] font-medium bg-zinc-900 hover:bg-zinc-850 text-zinc-300 hover:text-white border border-zinc-800 hover:border-zinc-700 transition cursor-pointer active:scale-95 flex items-center gap-1" title="${p.name} - ${p.desc}">
                  <span>${p.icon}</span>
                  <span>${p.name}</span>
                </button>
              `).join('')}
            </div>

            <div class="grid grid-cols-6 gap-1 text-center">
              <div>
                <span class="text-[10px] text-zinc-400 block mb-0.5 font-bold">F</span>
                <input type="number" step="0.5" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white cat-chance-f" value="${(cat.rarity_chances && cat.rarity_chances.F !== undefined) ? cat.rarity_chances.F : ((cat.rarity_chances && cat.rarity_chances.COMMON !== undefined) ? cat.rarity_chances.COMMON : 0)}">
              </div>
              <div>
                <span class="text-[10px] text-emerald-400 block mb-0.5 font-bold">D</span>
                <input type="number" step="0.5" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-emerald-300 text-center outline-none focus:border-white cat-chance-d" value="${(cat.rarity_chances && cat.rarity_chances.D !== undefined) ? cat.rarity_chances.D : ((cat.rarity_chances && cat.rarity_chances.UNCOMMON !== undefined) ? cat.rarity_chances.UNCOMMON : 0)}">
              </div>
              <div>
                <span class="text-[10px] text-cyan-400 block mb-0.5 font-bold">C</span>
                <input type="number" step="0.5" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-cyan-300 text-center outline-none focus:border-white cat-chance-c" value="${(cat.rarity_chances && cat.rarity_chances.C !== undefined) ? cat.rarity_chances.C : ((cat.rarity_chances && cat.rarity_chances.RARE !== undefined) ? cat.rarity_chances.RARE : 0)}">
              </div>
              <div>
                <span class="text-[10px] text-purple-400 block mb-0.5 font-bold">B</span>
                <input type="number" step="0.5" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-purple-300 text-center outline-none focus:border-white cat-chance-b" value="${(cat.rarity_chances && cat.rarity_chances.B !== undefined) ? cat.rarity_chances.B : ((cat.rarity_chances && cat.rarity_chances.EPIC !== undefined) ? cat.rarity_chances.EPIC : 0)}">
              </div>
              <div>
                <span class="text-[10px] text-amber-400 block mb-0.5 font-bold">A</span>
                <input type="number" step="0.5" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-800 rounded text-xs font-mono text-amber-300 text-center outline-none focus:border-white cat-chance-a" value="${(cat.rarity_chances && cat.rarity_chances.A !== undefined) ? cat.rarity_chances.A : ((cat.rarity_chances && cat.rarity_chances.LEGENDARY !== undefined) ? cat.rarity_chances.LEGENDARY : 0)}">
              </div>
              <div>
                <span class="text-[10px] text-rose-400 block mb-0.5 font-bold">S</span>
                <input type="number" step="0.5" class="w-full px-1 py-1 bg-zinc-950 border border-rose-900/60 rounded text-xs font-mono text-rose-300 text-center outline-none focus:border-rose-500 cat-chance-s" value="${(cat.rarity_chances && cat.rarity_chances.S !== undefined) ? cat.rarity_chances.S : 0}">
              </div>
            </div>
          </div>
        </div>
      `;

      const inputF = card.querySelector('.cat-chance-f');
      const inputD = card.querySelector('.cat-chance-d');
      const inputC = card.querySelector('.cat-chance-c');
      const inputB = card.querySelector('.cat-chance-b');
      const inputA = card.querySelector('.cat-chance-a');
      const inputS = card.querySelector('.cat-chance-s');
      const totalBadge = card.querySelector('.cat-total-badge');

      const refreshCardTotal = () => {
        const sum = (parseFloat(inputF.value) || 0) +
                    (parseFloat(inputD.value) || 0) +
                    (parseFloat(inputC.value) || 0) +
                    (parseFloat(inputB.value) || 0) +
                    (parseFloat(inputA.value) || 0) +
                    (parseFloat(inputS.value) || 0);
        updateRarityChancesBadge(totalBadge, sum);
      };

      const setCardChances = (ch) => {
        inputF.value = ch.F;
        inputD.value = ch.D;
        inputC.value = ch.C;
        inputB.value = ch.B;
        inputA.value = ch.A;
        inputS.value = ch.S;
        updateSettingValue(`categories.${catId}.rarity_chances.F`, ch.F);
        updateSettingValue(`categories.${catId}.rarity_chances.D`, ch.D);
        updateSettingValue(`categories.${catId}.rarity_chances.C`, ch.C);
        updateSettingValue(`categories.${catId}.rarity_chances.B`, ch.B);
        updateSettingValue(`categories.${catId}.rarity_chances.A`, ch.A);
        updateSettingValue(`categories.${catId}.rarity_chances.S`, ch.S);
        refreshCardTotal();
      };

      // Preset click handlers
      card.querySelectorAll('.cat-preset-btn').forEach(btn => {
        btn.addEventListener('click', () => {
          const pid = btn.getAttribute('data-preset');
          const p = RARITY_CHANCE_PRESETS.find(x => x.id === pid);
          if (p) {
            setCardChances(p.chances);
            showToast(`ใช้พรีเซ็ท "${p.name}" ให้กล่อง "${catId}" แล้ว`, 'info');
          }
        });
      });

      // Auto-balance click handler
      card.querySelector('.btn-balance-cat').addEventListener('click', () => {
        const current = {
          F: parseFloat(inputF.value) || 0,
          D: parseFloat(inputD.value) || 0,
          C: parseFloat(inputC.value) || 0,
          B: parseFloat(inputB.value) || 0,
          A: parseFloat(inputA.value) || 0,
          S: parseFloat(inputS.value) || 0
        };
        const balanced = balanceChancesTo100(current);
        setCardChances(balanced);
        showToast(`ปรับสมดุลเรทกล่อง "${catId}" ให้ครบ 100% เรียบร้อยแล้ว`, 'success');
      });

      card.querySelector('.cat-display-name').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.display_name`, e.target.value);
      });
      const colorSlot = card.querySelector('.cat-color-picker-slot');
      const headerDot = card.querySelector('.cat-header-dot');
      if (colorSlot) {
        renderMinecraftColorPicker(colorSlot, cat.color || '#AAAAAA', (newHex) => {
          updateSettingValue(`categories.${catId}.color`, newHex);
          if (headerDot) headerDot.style.backgroundColor = newHex;
          const cached = cachedCategoryList.find(c => c.id === catId);
          if (cached) cached.color = newHex;
        });
      }
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
      card.querySelector('.pick-cat-covered-btn').addEventListener('click', () => {
        openMaterialPicker({
          currentMaterial: card.querySelector('.cat-covered').value || 'BLACK_STAINED_GLASS_PANE',
          title: `เลือกบล็อกปิดกล่อง (Covered) สำหรับ [${catId}]`,
          filterType: 'BLOCKS',
          onSelect: (selectedMat) => {
            card.querySelector('.cat-covered').value = selectedMat;
            const icon = card.querySelector('.cat-covered-icon');
            if (icon) icon.src = getMinecraftItemTexture(selectedMat);
            updateSettingValue(`categories.${catId}.covered_material`, selectedMat);
          }
        });
      });

      card.querySelector('.pick-cat-searching-btn').addEventListener('click', () => {
        openMaterialPicker({
          currentMaterial: card.querySelector('.cat-searching').value || 'STONE',
          title: `เลือกบล็อกค้นหา (Searching) สำหรับ [${catId}]`,
          filterType: 'BLOCKS',
          onSelect: (selectedMat) => {
            card.querySelector('.cat-searching').value = selectedMat;
            const icon = card.querySelector('.cat-searching-icon');
            if (icon) icon.src = getMinecraftItemTexture(selectedMat);
            updateSettingValue(`categories.${catId}.searching_material`, selectedMat);
          }
        });
      });

      card.querySelector('.cat-covered').addEventListener('input', (e) => {
        const val = e.target.value.toUpperCase().trim();
        const icon = card.querySelector('.cat-covered-icon');
        if (icon) icon.src = getMinecraftItemTexture(val);
        updateSettingValue(`categories.${catId}.covered_material`, val);
      });
      card.querySelector('.cat-searching').addEventListener('input', (e) => {
        const val = e.target.value.toUpperCase().trim();
        const icon = card.querySelector('.cat-searching-icon');
        if (icon) icon.src = getMinecraftItemTexture(val);
        updateSettingValue(`categories.${catId}.searching_material`, val);
      });
      card.querySelector('.cat-chance-f').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.F`, parseFloat(e.target.value) || 0);
        refreshCardTotal();
      });
      card.querySelector('.cat-chance-d').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.D`, parseFloat(e.target.value) || 0);
        refreshCardTotal();
      });
      card.querySelector('.cat-chance-c').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.C`, parseFloat(e.target.value) || 0);
        refreshCardTotal();
      });
      card.querySelector('.cat-chance-b').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.B`, parseFloat(e.target.value) || 0);
        refreshCardTotal();
      });
      card.querySelector('.cat-chance-a').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.A`, parseFloat(e.target.value) || 0);
        refreshCardTotal();
      });
      card.querySelector('.cat-chance-s').addEventListener('input', (e) => {
        updateSettingValue(`categories.${catId}.rarity_chances.S`, parseFloat(e.target.value) || 0);
        refreshCardTotal();
      });

      refreshCardTotal();

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
          <div class="flex items-center gap-3 min-w-0">
            <button type="button" class="w-12 h-12 rounded-xl border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1.5 transition select-case-mat-btn cursor-pointer flex-shrink-0 group" title="คลิกเพื่อเลือกไอเทม">
              <img class="w-8 h-8 mc-icon object-contain group-hover:scale-110 transition-transform case-icon-img" src="${getMinecraftItemTexture(c.material || 'BUNDLE')}" alt="${escapeHtml(c.material || 'BUNDLE')}" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/item/bundle.png';">
            </button>
            <div class="min-w-0">
              <div class="font-bold text-xs text-white truncate" title="${caseId}">${escapeHtml(caseId)}</div>
              <div class="text-[10px] text-zinc-400 font-mono truncate case-mat-badge">${escapeHtml(c.material || 'BUNDLE')}</div>
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
              <div class="flex items-center gap-1.5">
                <input type="text" class="w-full px-2.5 py-1.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition case-material" value="${escapeHtml(c.material || 'BUNDLE')}">
                <button type="button" class="px-2 py-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 pick-case-mat-btn transition cursor-pointer flex-shrink-0">
                  เลือก
                </button>
              </div>
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

      const updateCaseMaterial = (newMat) => {
        newMat = newMat.toUpperCase().trim();
        card.querySelector('.case-material').value = newMat;
        card.querySelector('.case-mat-badge').textContent = newMat;
        const iconImg = card.querySelector('.case-icon-img');
        if (iconImg) {
          iconImg.src = getMinecraftItemTexture(newMat);
          iconImg.alt = newMat;
        }
        updateSettingValue(`safe_cases.${caseId}.material`, newMat);
      };

      card.querySelector('.select-case-mat-btn').addEventListener('click', () => {
        openMaterialPicker({
          currentMaterial: card.querySelector('.case-material').value || 'BUNDLE',
          title: `เลือกไอเทมสำหรับกระเป๋า [${caseId}]`,
          onSelect: (selectedMat) => updateCaseMaterial(selectedMat)
        });
      });

      card.querySelector('.pick-case-mat-btn').addEventListener('click', () => {
        openMaterialPicker({
          currentMaterial: card.querySelector('.case-material').value || 'BUNDLE',
          title: `เลือกไอเทมสำหรับกระเป๋า [${caseId}]`,
          onSelect: (selectedMat) => updateCaseMaterial(selectedMat)
        });
      });

      card.querySelector('.case-display-name').addEventListener('input', (e) => {
        updateSettingValue(`safe_cases.${caseId}.display_name`, e.target.value);
        updateSettingValue(`safe_cases.${caseId}.title`, e.target.value);
      });
      card.querySelector('.case-capacity').addEventListener('input', (e) => {
        updateSettingValue(`safe_cases.${caseId}.capacity`, parseInt(e.target.value, 10) || 9);
      });
      card.querySelector('.case-material').addEventListener('input', (e) => {
        const val = e.target.value.toUpperCase().trim();
        card.querySelector('.case-mat-badge').textContent = val || 'NONE';
        const iconImg = card.querySelector('.case-icon-img');
        if (iconImg) iconImg.src = getMinecraftItemTexture(val);
        updateSettingValue(`safe_cases.${caseId}.material`, val);
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
      if (Array.isArray(newValue)) {
        if (newValue.length === 0) {
          formattedVal = '[]';
        } else {
          formattedVal = '[' + newValue.map(item => formatYamlScalar(item)).join(', ') + ']';
        }
      } else if (typeof newValue === 'string') {
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
   * Guarantees space after mapping key (e.g. key: []) preventing SnakeYAML lexer errors
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
      const keys = Object.keys(val);
      if (keys.length === 0) return '{}\n';
      let res = indent > 0 ? '\n' : '';
      for (const key of keys) {
        const v = val[key];
        if (Array.isArray(v)) {
          if (v.length === 0) {
            res += `${pad}${key}: []\n`;
          } else {
            const isPrimitiveArray = v.every(x => typeof x !== 'object' || x === null);
            if (isPrimitiveArray && v.length <= 8) {
              res += `${pad}${key}: [${v.map(formatYamlScalar).join(', ')}]\n`;
            } else {
              res += `${pad}${key}:${stringifyYaml(v, indent + 1)}`;
            }
          }
        } else if (typeof v === 'object' && v !== null) {
          if (Object.keys(v).length === 0) {
            res += `${pad}${key}: {}\n`;
          } else {
            res += `${pad}${key}:${stringifyYaml(v, indent + 1)}`;
          }
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

    const defMaterial = (preset ? (preset.material || 'IRON_SWORD') : 'IRON_SWORD').toUpperCase().trim();
    const defRarity = preset ? normalizeRarity(preset.rarity || preset.tier || 'F') : 'C';
    let modalSelectedContainers = preset && Array.isArray(preset.allowed_containers)
      ? [...preset.allowed_containers]
      : (preset && preset.allowed_containers ? [preset.allowed_containers] : ['normal_crate', 'airdrop']);
    const defLore = preset && Array.isArray(preset.lore)
      ? preset.lore.join('\n')
      : (preset && preset.lore ? preset.lore : '');

    const defTexture = getMinecraftItemTexture(defMaterial);
    const defReadable = getMinecraftItemReadableName(defMaterial);

    elements.entryModalBody.innerHTML = `
      <div class="space-y-4">
        <!-- Interactive Item Picker Banner -->
        <div class="p-3 bg-zinc-950 border border-zinc-800 rounded-xl flex items-center justify-between gap-3 shadow-inner">
          <div class="flex items-center gap-3 min-w-0">
            <button type="button" id="m_choose_item_slot_btn" class="w-12 h-12 rounded-xl border border-zinc-700 item-slot-frame flex items-center justify-center p-2 flex-shrink-0 cursor-pointer group hover:border-indigo-500 transition" title="คลิกเพื่อเลือกไอเทมในเกม">
              <img id="m_item_icon_img" src="${defTexture}" class="w-8 h-8 mc-icon object-contain group-hover:scale-110 transition-transform" alt="${escapeHtml(defMaterial)}" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/block/${defMaterial.toLowerCase()}.png';">
            </button>
            <div class="min-w-0">
              <div id="m_item_mat_display" class="text-xs font-bold font-mono text-white truncate">${escapeHtml(defMaterial)}</div>
              <div id="m_item_readable_display" class="text-[11px] text-zinc-400 truncate">${escapeHtml(defReadable)}</div>
            </div>
          </div>
          <button type="button" id="m_choose_item_all_btn" class="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm transition active:scale-95 cursor-pointer flex-shrink-0">
            ${SVG_ICONS.box}
            <span>เลือกไอเทมในเกมทั้งหมด</span>
          </button>
        </div>

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
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ชนิดไอเทม (Material ในเกม):</label>
            <div class="flex items-center gap-1.5">
              <input type="text" id="m_item_mat" value="${escapeHtml(defMaterial)}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
              <button type="button" id="m_pick_mat_btn" class="px-2.5 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 transition cursor-pointer flex-shrink-0">
                เลือก
              </button>
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ระดับความหายาก (Rarity):</label>
            <select id="m_item_rarity" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-bold text-zinc-100 outline-none focus:border-white transition cursor-pointer">
              ${RARITY_RANKS.map(r => `<option value="${r}" ${r === defRarity ? 'selected' : ''}>แรงค์ ${r}</option>`).join('')}
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

        <!-- Interactive Container Chip Selector -->
        <div id="m_containers_selector_slot" class="pt-1"></div>

        <div>
          <label class="text-xs font-semibold text-zinc-300 block mb-1">คำอธิบายใต้ไอเทม (Lore - 1 บรรทัดต่อ 1 ข้อความ):</label>
          <textarea id="m_item_lore" rows="2" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition leading-relaxed resize-y">${escapeHtml(defLore)}</textarea>
        </div>
      </div>
    `;

    // Mount interactive container selector into modal
    const contSlot = elements.entryModalBody.querySelector('#m_containers_selector_slot');
    if (contSlot) {
      renderContainerChipSelector(contSlot, modalSelectedContainers, (updated) => {
        modalSelectedContainers = updated;
      });
    }

    const setModalMaterial = (selectedMat, itemObj) => {
      selectedMat = selectedMat.toUpperCase().trim();
      const matInput = document.getElementById('m_item_mat');
      if (matInput) matInput.value = selectedMat;

      const matDisp = document.getElementById('m_item_mat_display');
      if (matDisp) matDisp.textContent = selectedMat;

      const readName = (itemObj && itemObj.readable) ? itemObj.readable : getMinecraftItemReadableName(selectedMat);
      const readDisp = document.getElementById('m_item_readable_display');
      if (readDisp) readDisp.textContent = readName;

      const iconImg = document.getElementById('m_item_icon_img');
      if (iconImg) {
        iconImg.src = getMinecraftItemTexture(selectedMat);
        iconImg.alt = selectedMat;
      }

      const idInput = document.getElementById('m_item_id');
      if (idInput && !idInput.value.trim()) {
        idInput.value = selectedMat.toLowerCase();
      }

      const nameInput = document.getElementById('m_item_name');
      if (nameInput && !nameInput.value.trim()) {
        nameInput.value = `<gradient:#00FFA3:#00B8FF><bold>${readName}</bold></gradient>`;
      }
    };

    const triggerPicker = () => {
      const currentVal = (document.getElementById('m_item_mat') || {}).value || 'IRON_SWORD';
      openMaterialPicker({
        currentMaterial: currentVal,
        title: 'เลือกไอเทมในเกมสำหรับไอเทมใหม่',
        onSelect: (selectedMat, itemObj) => setModalMaterial(selectedMat, itemObj)
      });
    };

    const slotBtn = document.getElementById('m_choose_item_slot_btn');
    if (slotBtn) slotBtn.addEventListener('click', triggerPicker);

    const allBtn = document.getElementById('m_choose_item_all_btn');
    if (allBtn) allBtn.addEventListener('click', triggerPicker);

    const pickSubBtn = document.getElementById('m_pick_mat_btn');
    if (pickSubBtn) pickSubBtn.addEventListener('click', triggerPicker);

    const matInput = document.getElementById('m_item_mat');
    if (matInput) {
      matInput.addEventListener('input', (e) => {
        const val = e.target.value.toUpperCase().trim();
        const matDisp = document.getElementById('m_item_mat_display');
        if (matDisp) matDisp.textContent = val || 'NONE';
        const readDisp = document.getElementById('m_item_readable_display');
        if (readDisp) readDisp.textContent = getMinecraftItemReadableName(val);
        const iconImg = document.getElementById('m_item_icon_img');
        if (iconImg) iconImg.src = getMinecraftItemTexture(val);
      });
    }

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

      const containers = (modalSelectedContainers && modalSelectedContainers.length > 0)
        ? [...modalSelectedContainers]
        : ['normal_crate'];
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

    const chances = (preset && preset.rarity_chances) || { F: 60, D: 25, C: 10, B: 4, A: 1, S: 0 };
    const getChance = (rank, oldKey) => {
      if (chances[rank] !== undefined) return chances[rank];
      if (oldKey && chances[oldKey] !== undefined) return chances[oldKey];
      return 0;
    };

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
          <label class="text-xs font-semibold text-zinc-300 block mb-1">สีประจำกล่อง (Color Hex & Minecraft Colors):</label>
          <div id="m_cat_color_slot"></div>
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
            <div class="flex items-center gap-1.5">
              <div class="w-8 h-8 rounded-lg border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1 flex-shrink-0">
                <img id="m_cat_cov_img" class="w-6 h-6 mc-icon object-contain" src="${getMinecraftItemTexture(preset ? (preset.covered_material || 'BLACK_STAINED_GLASS_PANE') : 'BLACK_STAINED_GLASS_PANE')}" alt="">
              </div>
              <input type="text" id="m_cat_covered" value="${escapeHtml(preset ? (preset.covered_material || 'BLACK_STAINED_GLASS_PANE') : 'BLACK_STAINED_GLASS_PANE')}" class="w-full px-2 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
              <button type="button" id="m_cat_cov_btn" class="px-2 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 transition cursor-pointer flex-shrink-0">เลือก</button>
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">บล็อกค้นหา (Searching):</label>
            <div class="flex items-center gap-1.5">
              <div class="w-8 h-8 rounded-lg border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1 flex-shrink-0">
                <img id="m_cat_src_img" class="w-6 h-6 mc-icon object-contain" src="${getMinecraftItemTexture(preset ? (preset.searching_material || 'STONE') : 'STONE')}" alt="">
              </div>
              <input type="text" id="m_cat_searching" value="${escapeHtml(preset ? (preset.searching_material || 'STONE') : 'STONE')}" class="w-full px-2 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
              <button type="button" id="m_cat_src_btn" class="px-2 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 transition cursor-pointer flex-shrink-0">เลือก</button>
            </div>
          </div>
        </div>
        <div>
          <div class="flex items-center justify-between gap-2 mb-1.5 flex-wrap">
            <div class="flex items-center gap-2">
              <label class="text-xs font-semibold text-zinc-300">โอกาสออกความหายาก (Rarity Chances %):</label>
              <span id="m_cat_total_badge" class="text-[10px] font-mono px-2 py-0.5 rounded-full border transition font-bold bg-zinc-800 text-zinc-400 border-zinc-700">
                รวม 0%
              </span>
            </div>
            <button type="button" id="m_cat_balance_btn" class="px-2 py-0.5 rounded bg-indigo-950/80 hover:bg-indigo-900 border border-indigo-700/60 text-indigo-300 text-[10px] font-medium flex items-center gap-1 transition cursor-pointer active:scale-95" title="ปรับสัดส่วนทุกแรงค์ให้รวมได้ 100% พอดี">
              <span>⚡ ปรับให้ครบ 100%</span>
            </button>
          </div>

          <!-- Presets Buttons Bar in Modal -->
          <div class="flex flex-wrap items-center gap-1 p-1.5 mb-2 bg-zinc-950/80 border border-zinc-800/80 rounded-lg">
            <span class="text-[10px] text-zinc-500 font-semibold px-1">พรีเซ็ท:</span>
            ${RARITY_CHANCE_PRESETS.map(p => `
              <button type="button" data-preset="${p.id}" class="m-cat-preset-btn px-2 py-0.5 rounded text-[10px] font-medium bg-zinc-900 hover:bg-zinc-850 text-zinc-300 hover:text-white border border-zinc-800 hover:border-zinc-700 transition cursor-pointer active:scale-95 flex items-center gap-1" title="${p.name} - ${p.desc}">
                <span>${p.icon}</span>
                <span>${p.name}</span>
              </button>
            `).join('')}
          </div>

          <div class="grid grid-cols-6 gap-1 text-center">
            <div>
              <span class="text-[10px] text-zinc-400 block mb-0.5 font-bold">F</span>
              <input type="number" step="0.5" id="m_cat_c_f" value="${getChance('F', 'COMMON')}" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-zinc-100 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-emerald-400 block mb-0.5 font-bold">D</span>
              <input type="number" step="0.5" id="m_cat_c_d" value="${getChance('D', 'UNCOMMON')}" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-emerald-300 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-cyan-400 block mb-0.5 font-bold">C</span>
              <input type="number" step="0.5" id="m_cat_c_c" value="${getChance('C', 'RARE')}" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-cyan-300 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-purple-400 block mb-0.5 font-bold">B</span>
              <input type="number" step="0.5" id="m_cat_c_b" value="${getChance('B', 'EPIC')}" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-purple-300 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-amber-400 block mb-0.5 font-bold">A</span>
              <input type="number" step="0.5" id="m_cat_c_a" value="${getChance('A', 'LEGENDARY')}" class="w-full px-1 py-1 bg-zinc-950 border border-zinc-700 rounded text-xs font-mono text-amber-300 text-center outline-none focus:border-white">
            </div>
            <div>
              <span class="text-[10px] text-rose-400 block mb-0.5 font-bold">S</span>
              <input type="number" step="0.5" id="m_cat_c_s" value="${getChance('S', 'MYTHIC')}" class="w-full px-1 py-1 bg-zinc-950 border border-rose-900/60 rounded text-xs font-mono text-rose-300 text-center outline-none focus:border-rose-500">
            </div>
          </div>
        </div>
      </div>
    `;

    let modalSelectedColor = preset ? (preset.color || '#00FFA3') : '#00FFA3';
    const modalColorSlot = document.getElementById('m_cat_color_slot');
    if (modalColorSlot) {
      renderMinecraftColorPicker(modalColorSlot, modalSelectedColor, (newHex) => {
        modalSelectedColor = newHex;
      });
    }

    const mInputF = document.getElementById('m_cat_c_f');
    const mInputD = document.getElementById('m_cat_c_d');
    const mInputC = document.getElementById('m_cat_c_c');
    const mInputB = document.getElementById('m_cat_c_b');
    const mInputA = document.getElementById('m_cat_c_a');
    const mInputS = document.getElementById('m_cat_c_s');
    const mTotalBadge = document.getElementById('m_cat_total_badge');

    const refreshModalTotal = () => {
      const sum = (parseFloat(mInputF.value) || 0) +
                  (parseFloat(mInputD.value) || 0) +
                  (parseFloat(mInputC.value) || 0) +
                  (parseFloat(mInputB.value) || 0) +
                  (parseFloat(mInputA.value) || 0) +
                  (parseFloat(mInputS.value) || 0);
      updateRarityChancesBadge(mTotalBadge, sum);
    };

    const setModalChances = (ch) => {
      mInputF.value = ch.F;
      mInputD.value = ch.D;
      mInputC.value = ch.C;
      mInputB.value = ch.B;
      mInputA.value = ch.A;
      mInputS.value = ch.S;
      refreshModalTotal();
    };

    elements.entryModalBody.querySelectorAll('.m-cat-preset-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const pid = btn.getAttribute('data-preset');
        const p = RARITY_CHANCE_PRESETS.find(x => x.id === pid);
        if (p) {
          setModalChances(p.chances);
          showToast(`ใช้พรีเซ็ท "${p.name}" แล้ว`, 'info');
        }
      });
    });

    const mBalanceBtn = document.getElementById('m_cat_balance_btn');
    if (mBalanceBtn) {
      mBalanceBtn.addEventListener('click', () => {
        const cur = {
          F: parseFloat(mInputF.value) || 0,
          D: parseFloat(mInputD.value) || 0,
          C: parseFloat(mInputC.value) || 0,
          B: parseFloat(mInputB.value) || 0,
          A: parseFloat(mInputA.value) || 0,
          S: parseFloat(mInputS.value) || 0
        };
        const balanced = balanceChancesTo100(cur);
        setModalChances(balanced);
        showToast('ปรับสมดุลเรทให้ครบ 100% เรียบร้อยแล้ว', 'success');
      });
    }

    [mInputF, mInputD, mInputC, mInputB, mInputA, mInputS].forEach(inp => {
      inp.addEventListener('input', refreshModalTotal);
    });
    refreshModalTotal();

    const covBtn = document.getElementById('m_cat_cov_btn');
    if (covBtn) {
      covBtn.addEventListener('click', () => {
        const cur = (document.getElementById('m_cat_covered') || {}).value || 'BLACK_STAINED_GLASS_PANE';
        openMaterialPicker({
          currentMaterial: cur,
          title: 'เลือกบล็อกปิดกล่อง (Covered)',
          filterType: 'BLOCKS',
          onSelect: (selectedMat) => {
            const input = document.getElementById('m_cat_covered');
            if (input) input.value = selectedMat;
            const img = document.getElementById('m_cat_cov_img');
            if (img) img.src = getMinecraftItemTexture(selectedMat);
          }
        });
      });
    }

    const srcBtn = document.getElementById('m_cat_src_btn');
    if (srcBtn) {
      srcBtn.addEventListener('click', () => {
        const cur = (document.getElementById('m_cat_searching') || {}).value || 'STONE';
        openMaterialPicker({
          currentMaterial: cur,
          title: 'เลือกบล็อกค้นหา (Searching)',
          filterType: 'BLOCKS',
          onSelect: (selectedMat) => {
            const input = document.getElementById('m_cat_searching');
            if (input) input.value = selectedMat;
            const img = document.getElementById('m_cat_src_img');
            if (img) img.src = getMinecraftItemTexture(selectedMat);
          }
        });
      });
    }

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
        color: modalSelectedColor || '#AAAAAA',
        min_rolls: parseInt(document.getElementById('m_cat_min_rolls').value, 10) || 2,
        max_rolls: parseInt(document.getElementById('m_cat_max_rolls').value, 10) || 4,
        search_time_min: parseInt(document.getElementById('m_cat_search_min').value, 10) || 2,
        search_time_max: parseInt(document.getElementById('m_cat_search_max').value, 10) || 4,
        covered_material: document.getElementById('m_cat_covered').value.trim().toUpperCase() || 'BLACK_STAINED_GLASS_PANE',
        searching_material: document.getElementById('m_cat_searching').value.trim().toUpperCase() || 'STONE',
        search_sound: preset && preset.search_sound ? preset.search_sound : 'BLOCK_STONE_HIT',
        reveal_sound: preset && preset.reveal_sound ? preset.reveal_sound : 'ENTITY_ITEM_PICKUP',
        rarity_chances: balanceChancesTo100({
          F: parseFloat(document.getElementById('m_cat_c_f').value) || 0,
          D: parseFloat(document.getElementById('m_cat_c_d').value) || 0,
          C: parseFloat(document.getElementById('m_cat_c_c').value) || 0,
          B: parseFloat(document.getElementById('m_cat_c_b').value) || 0,
          A: parseFloat(document.getElementById('m_cat_c_a').value) || 0,
          S: parseFloat(document.getElementById('m_cat_c_s').value) || 0
        })
      };

      state.parsedData.categories[id] = newCat;
      if (!cachedCategoryList.some(c => c.id === id)) {
        cachedCategoryList.push({
          id: id,
          displayName: newCat.display_name.replace(/<[^>]+>/g, '').trim() || id,
          color: newCat.color || '#AAAAAA'
        });
      }
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
            <div class="flex items-center gap-1.5">
              <div class="w-8 h-8 rounded-lg border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1 flex-shrink-0">
                <img id="m_case_mat_img" class="w-6 h-6 mc-icon object-contain" src="${getMinecraftItemTexture(preset ? (preset.material || 'BUNDLE') : 'BUNDLE')}" alt="">
              </div>
              <input type="text" id="m_case_mat" value="${escapeHtml(preset ? (preset.material || 'BUNDLE') : 'BUNDLE')}" class="w-full px-2 py-2 bg-zinc-950 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-white transition">
              <button type="button" id="m_case_mat_btn" class="px-2 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 transition cursor-pointer flex-shrink-0">เลือก</button>
            </div>
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

    const caseMatBtn = document.getElementById('m_case_mat_btn');
    if (caseMatBtn) {
      caseMatBtn.addEventListener('click', () => {
        const cur = (document.getElementById('m_case_mat') || {}).value || 'BUNDLE';
        openMaterialPicker({
          currentMaterial: cur,
          title: 'เลือกไอเทมสำหรับกระเป๋านิรภัย',
          onSelect: (selectedMat) => {
            const input = document.getElementById('m_case_mat');
            if (input) input.value = selectedMat;
            const img = document.getElementById('m_case_mat_img');
            if (img) img.src = getMinecraftItemTexture(selectedMat);
          }
        });
      });
    }

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

    // Confirm Modal (Close only via explicit buttons)
    elements.cancelConfirmModalBtn.addEventListener('click', closeConfirmModal);
    elements.okConfirmModalBtn.addEventListener('click', () => {
      if (activeConfirmCallback) {
        activeConfirmCallback();
      }
      closeConfirmModal();
    });

    // Create File Modal (Close only via explicit buttons)
    if (elements.createNewFileBtn) {
      elements.createNewFileBtn.addEventListener('click', openCreateFileModal);
    }
    elements.closeCreateFileModalBtn.addEventListener('click', closeCreateFileModal);
    elements.cancelCreateFileModalBtn.addEventListener('click', closeCreateFileModal);
    elements.confirmCreateFileModalBtn.addEventListener('click', handleCreateNewFile);
    elements.newConfigFileNameInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        handleCreateNewFile();
      }
    });

    // Auth Modal (Close only via explicit buttons)
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

    // Material Picker Modal (Close only via explicit buttons)
    if (elements.closeMaterialPickerBtn) {
      elements.closeMaterialPickerBtn.addEventListener('click', closeMaterialPicker);
    }
    if (elements.cancelMaterialPickerBtn) {
      elements.cancelMaterialPickerBtn.addEventListener('click', closeMaterialPicker);
    }
    if (elements.materialSearchInput) {
      elements.materialSearchInput.addEventListener('input', (e) => {
        currentPickerQuery = e.target.value;
        renderMaterialPickerGrid();
      });
    }
    if (elements.materialCategoryFilters) {
      elements.materialCategoryFilters.addEventListener('click', (e) => {
        const btn = e.target.closest('.mat-filter-btn');
        if (!btn) return;
        currentPickerFilter = btn.getAttribute('data-cat');
        elements.materialCategoryFilters.querySelectorAll('.mat-filter-btn').forEach(b => {
          if (b === btn) {
            b.className = 'mat-filter-btn px-2.5 py-1.5 rounded-lg text-[11px] font-medium bg-indigo-600 text-white transition whitespace-nowrap cursor-pointer flex items-center gap-1.5 shadow-sm';
          } else {
            b.className = 'mat-filter-btn px-2.5 py-1.5 rounded-lg text-[11px] font-medium bg-zinc-800 text-zinc-400 hover:text-zinc-200 transition whitespace-nowrap cursor-pointer flex items-center gap-1.5';
          }
        });
        renderMaterialPickerGrid();
      });
    }

    if (elements.materialGridContainer) {
      elements.materialGridContainer.addEventListener('scroll', () => {
        const { scrollTop, scrollHeight, clientHeight } = elements.materialGridContainer;
        if (scrollTop + clientHeight >= scrollHeight - 180) {
          renderNextPickerBatch();
        }
      });
    }

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
    await preloadCategories();
    await loadMinecraftAssets();
    await loadConfigFile('config.yml');
  }

  document.addEventListener('DOMContentLoaded', init);
})();
