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
    edit: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>`,
    check: `<svg class="w-4 h-4 text-emerald-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>`,
    alert: `<svg class="w-4 h-4 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>`,
    zap: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>`,
    shield: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`,
    alert_triangle: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
    backpack: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 10a4 4 0 0 1 4-4h8a4 4 0 0 1 4 4v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2Z"/><path d="M9 6V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/><path d="M8 21v-5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v5"/><line x1="8" y1="10" x2="16" y2="10"/></svg>`,
    scale: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m16 16 3-8 3 8c-.87.65-1.92 1-3 1s-2.13-.35-3-1Z"/><path d="m2 16 3-8 3 8c-.87.65-1.92 1-3 1s-2.13-.35-3-1Z"/><path d="M7 21h10"/><path d="M12 3v18"/><path d="M3 7h2c2 0 5-1 7-2 2 1 5 2 7 2h2"/></svg>`,
    clock: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`,
    palette: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="13.5" cy="6.5" r=".5" fill="currentColor"/><circle cx="17.5" cy="10.5" r=".5" fill="currentColor"/><circle cx="8.5" cy="7.5" r=".5" fill="currentColor"/><circle cx="6.5" cy="12.5" r=".5" fill="currentColor"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.93 0 1.65-.75 1.65-1.69 0-.44-.18-.84-.44-1.13-.29-.29-.44-.65-.44-1.12a1.64 1.64 0 0 1 1.67-1.67h2c3.05 0 5.56-2.51 5.56-5.56C21.44 6.3 17.2 2 12 2z"/></svg>`,
    crate: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/></svg>`,
    coins: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="8" cy="8" r="6"/><path d="M18.09 10.37A6 6 0 1 1 10.34 18"/><path d="M7 6h1v4"/><path d="m16.71 13.88.7.71-2.82 2.82"/></svg>`,
    file_code: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><polyline points="10 13 8 15 10 17"/><polyline points="14 13 16 15 14 17"/></svg>`,
    crown: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m2 4 3 12h14l3-12-6 7-4-7-4 7-6-7zm3 16h14"/></svg>`,
    medal: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="6"/><path d="M15.477 12.89 17 22l-5-3-5 3 1.523-9.11"/></svg>`,
    toolbox: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7h-3a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"/><path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="12" y1="12" x2="12" y2="12.01"/><path d="M2 13h20"/></svg>`,
    lock: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`,
    parachute: `<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21.5 10a10 10 0 0 0-19 0h19z"/><path d="M12 10v12"/><path d="m4.5 10 7.5 12"/><path d="m19.5 10-7.5 12"/></svg>`
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
    mobileMenuBtn: document.getElementById('mobileMenuBtn'),
    closeSidebarBtn: document.getElementById('closeSidebarBtn'),
    sidebarNav: document.getElementById('sidebarNav'),
    sidebarBackdrop: document.getElementById('sidebarBackdrop'),
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
    entryModalTabs: document.getElementById('entryModalTabs'),
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
    const isBool = field.type === 'boolean';
    row.className = isBool
      ? 'setting-row flex items-center justify-between py-2.5 sm:py-2 border-b border-zinc-800/60 last:border-b-0 gap-3'
      : 'setting-row flex flex-col sm:flex-row sm:items-center justify-between py-2.5 sm:py-2 border-b border-zinc-800/60 last:border-b-0 gap-2 sm:gap-4';
    row.dataset.key = field.key;

    const currentValue = getNestedValue(state.parsedData, field.key);
    const value = (currentValue !== undefined && currentValue !== null) ? currentValue : (field.default !== undefined ? field.default : '');

    row.innerHTML = `
      <div class="flex-1 min-w-0 pr-0 sm:pr-2">
        <div class="flex flex-wrap items-center gap-1.5 sm:gap-2 mb-0.5">
          <span class="text-xs font-semibold text-zinc-200">${escapeHtml(field.label)}</span>
          <span class="font-mono text-[10px] text-zinc-500 truncate max-w-[200px]" title="${field.key}">${escapeHtml(field.key)}</span>
        </div>
        ${field.desc ? `<div class="text-[11px] text-zinc-400 leading-tight">${escapeHtml(field.desc)}</div>` : ''}
      </div>
      <div class="setting-control flex-shrink-0 flex items-center ${isBool ? 'justify-end' : 'justify-start sm:justify-end w-full sm:w-auto'}"></div>
    `;

    const ctrlContainer = row.querySelector('.setting-control');

    if (field.type === 'boolean') {
      const isChecked = Boolean(value);
      ctrlContainer.innerHTML = `
        <label class="switch" title="เปิด/ปิด: ${escapeHtml(field.label)}">
          <input type="checkbox" ${isChecked ? 'checked' : ''} data-key="${field.key}" aria-label="${escapeHtml(field.label)}">
          <span class="slider"></span>
        </label>
      `;
      ctrlContainer.querySelector('input').addEventListener('change', (e) => {
        updateSettingValue(field.key, e.target.checked);
      });
    } else if (field.type === 'select') {
      const select = document.createElement('select');
      select.className = 'w-full sm:w-auto bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs px-2.5 py-2 sm:py-1.5 text-zinc-200 outline-none focus:border-indigo-500 transition cursor-pointer min-w-0 sm:min-w-[140px] touch-target';
      select.setAttribute('aria-label', field.label);
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
      input.inputMode = 'decimal';
      input.className = 'w-full sm:w-32 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono px-2.5 py-2 sm:py-1.5 text-zinc-200 outline-none focus:border-indigo-500 transition text-left sm:text-right touch-target';
      input.setAttribute('aria-label', field.label);
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
      input.className = 'w-full sm:w-auto bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs px-2.5 py-2 sm:py-1.5 text-zinc-200 outline-none focus:border-indigo-500 transition min-w-0 sm:min-w-[180px] touch-target';
      input.setAttribute('aria-label', field.label);
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
   * Fully powered by SVG icons with 0% emojis.
   */
  const RARITY_CHANCE_PRESETS = [
    {
      id: 'basic',
      name: 'กล่องทั่วไป',
      icon: SVG_ICONS.crate,
      desc: 'เน้นของพื้นฐาน F/D (S 0%)',
      chances: { F: 70.0, D: 25.0, C: 5.0, B: 0.0, A: 0.0, S: 0.0 }
    },
    {
      id: 'supply',
      name: 'กล่องเสบียง',
      icon: SVG_ICONS.toolbox,
      desc: 'สมดุลระดับกลาง F/D/C (S 0%)',
      chances: { F: 45.0, D: 35.0, C: 15.0, B: 5.0, A: 0.0, S: 0.0 }
    },
    {
      id: 'military',
      name: 'กล่องทหาร',
      icon: SVG_ICONS.medal,
      desc: 'ยุทโธปกรณ์ระดับกลาง-สูง D/C/B/A (S 0%)',
      chances: { F: 20.0, D: 35.0, C: 25.0, B: 15.0, A: 5.0, S: 0.0 }
    },
    {
      id: 'safe',
      name: 'ตู้เซฟ (S 5%)',
      icon: SVG_ICONS.lock,
      desc: 'ของมีค่า C/B/A พร้อมโอกาสแดง S 5%',
      chances: { F: 10.0, D: 20.0, C: 35.0, B: 20.0, A: 10.0, S: 5.0 }
    },
    {
      id: 'airdrop',
      name: 'แอร์ดรอป (S 8%)',
      icon: SVG_ICONS.parachute,
      desc: 'กล่องบินเสบียงพิเศษ แดง S 8%',
      chances: { F: 5.0, D: 15.0, C: 25.0, B: 30.0, A: 17.0, S: 8.0 }
    },
    {
      id: 'boss',
      name: 'กล่องบอส (S 10%)',
      icon: SVG_ICONS.crown,
      desc: 'ระดับสูงสุด แดง S 10% เต็มพิกัด',
      chances: { F: 0.0, D: 10.0, C: 20.0, B: 35.0, A: 25.0, S: 10.0 }
    }
  ];

  /**
   * 6 Standard Rarity Rank Colors
   */
  const RARITY_COLORS = {
    F: '#9CA3AF', // Slate / Gray (Common)
    D: '#10B981', // Emerald Green (Uncommon)
    C: '#06B6D4', // Cyan / Light Blue (Rare)
    B: '#A855F7', // Purple / Amethyst (Epic)
    A: '#F59E0B', // Amber / Gold (Legendary)
    S: '#EF4444'  // Rose / Crimson Red (Mythic)
  };

  const RARITY_COLOR_LIST = [
    { rank: 'F', hex: '#9CA3AF', name: 'แรงค์ F (Common)', label: 'เทา' },
    { rank: 'D', hex: '#10B981', name: 'แรงค์ D (Uncommon)', label: 'เขียว' },
    { rank: 'C', hex: '#06B6D4', name: 'แรงค์ C (Rare)', label: 'ฟ้า' },
    { rank: 'B', hex: '#A855F7', name: 'แรงค์ B (Epic)', label: 'ม่วง' },
    { rank: 'A', hex: '#F59E0B', name: 'แรงค์ A (Legendary)', label: 'ทอง' },
    { rank: 'S', hex: '#EF4444', name: 'แรงค์ S (Mythic)', label: 'แดง' }
  ];

  /**
   * Smart Auto-Color helper for Item & Box Display Names.
   * Updates or wraps the text with the selected rank's color code while preserving user customization.
   */
  function applyColorToDisplayName(currentText, hexColor) {
    if (!currentText || !currentText.trim()) {
      return `<color:${hexColor}>New Entry</color>`;
    }
    let s = currentText.trim();
    // If enclosed in <color:#HEX>...</color>
    if (/^<color:#[0-9a-fA-F]{6}>(.*?)<\/color>$/i.test(s)) {
      return s.replace(/^<color:#[0-9a-fA-F]{6}>(.*?)<\/color>$/i, `<color:${hexColor}>$1</color>`);
    }
    // If starts with <color:#HEX> without closing tag
    if (/^<color:#[0-9a-fA-F]{6}>(.*)$/i.test(s)) {
      return s.replace(/^<color:#[0-9a-fA-F]{6}>(.*)$/i, `<color:${hexColor}>$1</color>`);
    }
    // If starts with <#HEX>
    if (/^<#[0-9a-fA-F]{6}>(.*)$/i.test(s)) {
      return s.replace(/^<#[0-9a-fA-F]{6}>(.*?)(?:<\/#?[0-9a-fA-F]{6}>)?$/i, `<color:${hexColor}>$1</color>`);
    }
    // If starts with legacy &c or §c
    if (/^[&§][0-9a-fA-F](.*)$/i.test(s)) {
      return s.replace(/^[&§][0-9a-fA-F](.*)$/i, `<color:${hexColor}>$1</color>`);
    }
    // If starts with gradient and user explicitly applied a single rank color
    if (/^<gradient:[^>]+>(.*?)<\/gradient>$/i.test(s)) {
      return s.replace(/^<gradient:[^>]+>(.*?)<\/gradient>$/i, `<color:${hexColor}>$1</color>`);
    }
    // Plain text: wrap with color tag
    return `<color:${hexColor}>${s}</color>`;
  }

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
   * Universal Minecraft Color & Formatting Parser
   * Accurately parses and renders MiniMessage, legacy (&, §), Spigot hex (&#HEX, {#HEX}, &x&r...),
   * gradients, and named colors into authentic Minecraft styled HTML with text shadows.
   */
  function formatMiniMessagePreview(raw) {
    if (!raw) return '<span class="text-zinc-500 italic">ไม่มีข้อความ</span>';
    let s = String(raw);

    // 1. Normalize Section sign (§) to ampersand (&)
    s = s.replace(/§/g, '&');

    // 2. Spigot Hex format: &#RRGGBB or {#RRGGBB} -> <color:#RRGGBB>
    s = s.replace(/&#([0-9a-fA-F]{6})/g, '<color:#$1>');
    s = s.replace(/\{#([0-9a-fA-F]{6})\}/g, '<color:#$1>');

    // 3. Spigot legacy character hex: &x&r&r&g&g&b&b -> <color:#rrggbb>
    s = s.replace(/&x&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])/gi, '<color:#$1$2$3$4$5$6>');

    // 4. Standard Legacy color codes
    const legacyMap = {
      '0': '#000000', '1': '#0000AA', '2': '#00AA00', '3': '#00AAAA',
      '4': '#AA0000', '5': '#AA00AA', '6': '#FFAA00', '7': '#AAAAAA',
      '8': '#555555', '9': '#5555FF', 'a': '#55FF55', 'b': '#55FFFF',
      'c': '#FF5555', 'd': '#FF55FF', 'e': '#FFFF55', 'f': '#FFFFFF'
    };
    for (const [code, hexVal] of Object.entries(legacyMap)) {
      s = s.replace(new RegExp(`&${code}`, 'gi'), `<color:${hexVal}>`);
    }

    // Legacy formatting codes
    s = s.replace(/&l/gi, '<b>');
    s = s.replace(/&o/gi, '<i>');
    s = s.replace(/&n/gi, '<u>');
    s = s.replace(/&m/gi, '<s>');
    s = s.replace(/&r/gi, '<reset>');

    // 5. MiniMessage short hex: <#RRGGBB> -> <color:#RRGGBB>
    s = s.replace(/<#([0-9a-fA-F]{6})>/gi, '<color:#$1>');
    s = s.replace(/<\/#?([0-9a-fA-F]{6})>/gi, '</color>');
    s = s.replace(/<\/color:#[0-9a-fA-F]{6}>/gi, '</color>');

    // 6. MiniMessage Standard Named Colors
    const namedColors = {
      black: '#000000', dark_blue: '#0000AA', dark_green: '#00AA00', dark_aqua: '#00AAAA',
      dark_red: '#AA0000', dark_purple: '#AA00AA', gold: '#FFAA00', gray: '#AAAAAA',
      dark_gray: '#555555', blue: '#5555FF', green: '#55FF55', aqua: '#55FFFF',
      red: '#FF5555', light_purple: '#FF55FF', yellow: '#FFFF55', white: '#FFFFFF'
    };
    for (const [name, hexVal] of Object.entries(namedColors)) {
      s = s.replace(new RegExp(`<${name}>`, 'gi'), `<color:${hexVal}>`);
      s = s.replace(new RegExp(`</${name}>`, 'gi'), '</color>');
    }

    // MiniMessage tags normalization
    s = s.replace(/<bold>/gi, '<b>');
    s = s.replace(/<\/bold>/gi, '</b>');
    s = s.replace(/<italic>/gi, '<i>');
    s = s.replace(/<\/italic>/gi, '</i>');
    s = s.replace(/<underlined>/gi, '<u>');
    s = s.replace(/<\/underlined>/gi, '</u>');
    s = s.replace(/<strikethrough>/gi, '<s>');
    s = s.replace(/<\/strikethrough>/gi, '</s>');

    // 7. Gradients: <gradient:#HEX1:#HEX2:...>text</gradient>
    s = s.replace(/<gradient:([#0-9a-fA-F:]+)>(.*?)(?:<\/gradient>|$)/gi, (m, colorsPart, content) => {
      const cols = colorsPart.split(':').filter(Boolean).map(c => c.startsWith('#') ? c : `#${c}`);
      if (cols.length === 1) cols.push(cols[0]);
      const gradCss = `linear-gradient(90deg, ${cols.join(', ')})`;
      return `<span style="background: ${gradCss}; -webkit-background-clip: text; -webkit-text-fill-color: transparent; display: inline-block; font-weight: bold;">${content}</span>`;
    });

    // 8. Token stream parser for colors & formatting
    const tokens = s.split(/(<[^>]+>)/g);
    const out = [];
    let curColor = null;
    let bold = false;
    let italic = false;
    let underlined = false;
    let strikethrough = false;

    tokens.forEach(tok => {
      if (!tok) return;

      const mColor = tok.match(/^<color:(#[0-9a-fA-F]{6})>$/i);
      if (mColor) {
        curColor = mColor[1];
        return;
      }
      if (/^<\/color>$/i.test(tok)) {
        curColor = null;
        return;
      }
      if (/^<b>$/i.test(tok)) { bold = true; return; }
      if (/^<\/b>$/i.test(tok)) { bold = false; return; }
      if (/^<i>$/i.test(tok)) { italic = true; return; }
      if (/^<\/i>$/i.test(tok)) { italic = false; return; }
      if (/^<u>$/i.test(tok)) { underlined = true; return; }
      if (/^<\/u>$/i.test(tok)) { underlined = false; return; }
      if (/^<s>$/i.test(tok)) { strikethrough = true; return; }
      if (/^<\/s>$/i.test(tok)) { strikethrough = false; return; }
      if (/^<(?:reset|r)>$/i.test(tok)) {
        curColor = null;
        bold = false;
        italic = false;
        underlined = false;
        strikethrough = false;
        return;
      }

      // If token is already an HTML span from gradient
      if (tok.startsWith('<span') || tok.startsWith('</span>')) {
        out.push(tok);
        return;
      }

      const esc = escapeHtml(tok);
      const styles = [];
      if (curColor) styles.push(`color: ${curColor}`);
      if (bold) styles.push('font-weight: 700');
      if (italic) styles.push('font-style: italic');
      const decorations = [];
      if (underlined) decorations.push('underline');
      if (strikethrough) decorations.push('line-through');
      if (decorations.length > 0) styles.push(`text-decoration: ${decorations.join(' ')}`);

      if (styles.length > 0) {
        out.push(`<span style="${styles.join('; ')}">${esc}</span>`);
      } else {
        out.push(esc);
      }
    });

    return `<span class="mc-text-wrapper">${out.join('')}</span>`;
  }

  /**
   * Sets up interactive categorized tabs inside the unified entry modal
   */
  function setupModalTabs(tabConfigs, defaultTab = 0) {
    if (!elements.entryModalTabs) return;
    elements.entryModalTabs.innerHTML = '';
    elements.entryModalTabs.classList.remove('hidden');

    tabConfigs.forEach((tab, index) => {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = `modal-tab-btn px-4 py-2 text-xs font-semibold whitespace-nowrap flex items-center gap-2 cursor-pointer transition ${index === defaultTab ? 'active text-white' : 'text-zinc-400 hover:text-zinc-200'}`;
      btn.innerHTML = `${tab.icon || ''}<span>${tab.label}</span>`;
      btn.addEventListener('click', () => {
        elements.entryModalTabs.querySelectorAll('.modal-tab-btn').forEach(b => {
          b.classList.remove('active', 'text-white');
          b.classList.add('text-zinc-400');
        });
        btn.classList.add('active', 'text-white');
        btn.classList.remove('text-zinc-400');

        tabConfigs.forEach((t, i) => {
          const sec = document.getElementById(t.targetId || t.id);
          if (sec) {
            if (i === index) sec.classList.remove('hidden');
            else sec.classList.add('hidden');
          }
        });
      });
      elements.entryModalTabs.appendChild(btn);
    });

    // Initial activation
    tabConfigs.forEach((t, i) => {
      const sec = document.getElementById(t.targetId || t.id);
      if (sec) {
        if (i === defaultTab) sec.classList.remove('hidden');
        else sec.classList.add('hidden');
      }
    });
  }

  /**
   * Renders an interactive Minecraft Hex Color Picker component
   * into a target container element with 6 Rarity Colors as primary swatches.
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

        <!-- 6 Primary Rarity Rank Colors -->
        <div>
          <div class="flex items-center justify-between mb-1.5 px-0.5">
            <span class="text-[10px] font-semibold text-zinc-300 flex items-center gap-1.5">
              ${SVG_ICONS.palette}
              <span>สีประจำระดับความหายาก (6 สีหลัก):</span>
            </span>
            <span class="text-[9px] text-zinc-500">คลิกเลือกสีตามระดับ</span>
          </div>
          <div class="grid grid-cols-6 gap-1.5 mb-2.5">
            ${RARITY_COLOR_LIST.map(r => {
              const isMatch = r.hex.toUpperCase() === currentHex.toUpperCase();
              return `
                <button type="button" data-hex="${r.hex}" class="rarity-cp-swatch mc-cp-swatch relative h-7 rounded-lg border transition transform hover:scale-105 active:scale-95 cursor-pointer flex flex-col items-center justify-center p-1 font-mono font-bold text-[10px] shadow-sm ${isMatch ? 'ring-2 ring-white scale-105 border-white' : 'border-zinc-700/80'}" style="background-color: ${r.hex}; color: ${isLightColor(r.hex) ? '#000000' : '#FFFFFF'};" title="${r.name} (${r.hex})">
                  <span>${r.rank}</span>
                </button>
              `;
            }).join('')}
          </div>
        </div>

        <!-- 16 Minecraft Colors Grid -->
        <div>
          <div class="flex items-center justify-between mb-1.5 px-0.5">
            <span class="text-[10px] font-medium text-zinc-400 flex items-center gap-1">
              ${SVG_ICONS.palette}
              <span>สีมาตรฐาน Minecraft (16 สี):</span>
            </span>
            <span class="text-[9px] text-zinc-500">คลิกเลือกสีทันที</span>
          </div>
          <div class="grid grid-cols-8 gap-1.5 mc-cp-palette">
            ${MINECRAFT_COLORS.map(c => {
              const isSelected = c.hex.toUpperCase() === currentHex.toUpperCase();
              return `
                <button type="button" data-hex="${c.hex}" data-code="${c.code}" class="mc-cp-swatch relative h-6 rounded border border-zinc-700/80 transition transform hover:scale-110 active:scale-95 cursor-pointer flex items-center justify-center ${isSelected ? 'ring-2 ring-indigo-400 ring-offset-1 ring-offset-zinc-950 scale-105' : 'hover:border-zinc-400'}" style="background-color: ${c.hex};" title="§${c.code} ${c.name} (${c.nameTh}) ${c.hex}">
                  <span class="mc-cp-check ${isSelected ? 'block' : 'hidden'} text-xs font-bold leading-none">
                    ${SVG_ICONS.check}
                  </span>
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
      const rarityMatch = RARITY_COLOR_LIST.find(r => r.hex.toUpperCase() === currentHex.toUpperCase());

      if (badgeEl) {
        if (rarityMatch) {
          badgeEl.innerHTML = `<span class="font-bold text-white">${rarityMatch.rank}</span> <span class="text-zinc-300">${rarityMatch.label}</span>`;
          badgeEl.className = 'mc-cp-badge px-2.5 py-1 rounded bg-zinc-900 border border-indigo-500/60 text-[10px] font-mono whitespace-nowrap min-w-[76px] text-center shadow-sm';
        } else if (mcMatch) {
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
          swatch.classList.add('ring-2', 'ring-white', 'ring-offset-1', 'ring-offset-zinc-950', 'scale-105');
          swatch.classList.remove('hover:border-zinc-400');
          if (check) check.classList.remove('hidden');
        } else {
          swatch.classList.remove('ring-2', 'ring-white', 'ring-offset-1', 'ring-offset-zinc-950', 'scale-105');
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
   * Dedicated View for items.yml with Modern Showcase Cards, Filters, and Structured Modal Editor
   */
  let itemSearchQuery = '';
  let itemRarityFilter = 'ALL';
  let itemContainerFilter = 'ALL';
  let itemSortBy = 'DEFAULT';

  function renderItemsRegistryView() {
    elements.categoryTitle.textContent = 'ฐานข้อมูลไอเทมการสกัด (items.yml)';
    elements.categoryDesc.textContent = 'รายการไอเทมทั้งหมดที่สามารถสุ่มพบได้ในกล่องดรอป พร้อมมูลค่าความหายากและโอกาสออก';
    elements.currentCategoryDisplay.textContent = 'จัดการไอเทม';

    elements.formFieldsContainer.innerHTML = '';

    const itemsData = state.parsedData.items || {};
    let itemKeys = Object.keys(itemsData);
    const totalCount = itemKeys.length;

    // Collect all available categories for container filter
    const knownContainers = new Set();
    itemKeys.forEach(k => {
      const it = itemsData[k] || {};
      const conts = Array.isArray(it.allowed_containers) ? it.allowed_containers : (it.allowed_containers ? [it.allowed_containers] : []);
      conts.forEach(c => knownContainers.add(c));
    });

    // Count rarity distribution
    const rarityCounts = { F: 0, D: 0, C: 0, B: 0, A: 0, S: 0 };
    itemKeys.forEach(k => {
      const r = normalizeRarity((itemsData[k] || {}).rarity || (itemsData[k] || {}).tier || 'F');
      if (rarityCounts[r] !== undefined) rarityCounts[r]++;
    });

    // Filter items based on local search, rarity, and container
    if (itemRarityFilter !== 'ALL') {
      itemKeys = itemKeys.filter(k => {
        const item = itemsData[k] || {};
        const r = normalizeRarity(item.rarity || item.tier || 'F');
        return r === itemRarityFilter;
      });
    }

    if (itemContainerFilter !== 'ALL') {
      itemKeys = itemKeys.filter(k => {
        const item = itemsData[k] || {};
        const conts = Array.isArray(item.allowed_containers) ? item.allowed_containers : (item.allowed_containers ? [item.allowed_containers] : []);
        return conts.includes(itemContainerFilter);
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

    // Sort items
    if (itemSortBy === 'PRICE_DESC') {
      itemKeys.sort((a, b) => (parseFloat(itemsData[b].price) || 0) - (parseFloat(itemsData[a].price) || 0));
    } else if (itemSortBy === 'PRICE_ASC') {
      itemKeys.sort((a, b) => (parseFloat(itemsData[a].price) || 0) - (parseFloat(itemsData[b].price) || 0));
    } else if (itemSortBy === 'WEIGHT_DESC') {
      itemKeys.sort((a, b) => (parseFloat(itemsData[b].weight) || 0) - (parseFloat(itemsData[a].weight) || 0));
    } else if (itemSortBy === 'RARITY_DESC') {
      const rankOrder = { S: 6, A: 5, B: 4, C: 3, D: 2, F: 1 };
      itemKeys.sort((a, b) => (rankOrder[normalizeRarity(itemsData[b].rarity)] || 0) - (rankOrder[normalizeRarity(itemsData[a].rarity)] || 0));
    } else if (itemSortBy === 'NAME_ASC') {
      itemKeys.sort((a, b) => (itemsData[a].display_name || a).localeCompare(itemsData[b].display_name || b));
    }

    // Top Filter & Action Bar
    const controlPanel = document.createElement('div');
    controlPanel.className = 'w-full space-y-3 pb-4 mb-3 border-b border-zinc-800/80';
    controlPanel.innerHTML = `
      <div class="flex flex-wrap items-center justify-between gap-3">
        <!-- Search & Filters -->
        <div class="flex flex-wrap items-center gap-2 flex-1 min-w-0">
          <div class="relative flex-1 min-w-[200px] sm:max-w-xs">
            <svg class="w-3.5 h-3.5 text-zinc-500 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input type="text" id="itemLocalSearchInput" placeholder="ค้นหาไอเทมหรือ Material..." value="${escapeHtml(itemSearchQuery)}"
                   class="w-full h-9 pl-9 pr-3 bg-zinc-950 border border-zinc-800 rounded-lg text-xs text-zinc-200 placeholder-zinc-500 outline-none focus:border-indigo-500 transition">
          </div>

          <select id="itemRarityFilterSelect" class="h-9 px-3 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-medium text-zinc-300 outline-none focus:border-indigo-500 cursor-pointer">
            <option value="ALL" ${itemRarityFilter === 'ALL' ? 'selected' : ''}>ทุกระดับความหายาก</option>
            ${RARITY_RANKS.map(r => `<option value="${r}" ${itemRarityFilter === r ? 'selected' : ''}>แรงค์ ${r} (${rarityCounts[r] || 0})</option>`).join('')}
          </select>

          <select id="itemContainerFilterSelect" class="h-9 px-3 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-medium text-zinc-300 outline-none focus:border-indigo-500 cursor-pointer">
            <option value="ALL" ${itemContainerFilter === 'ALL' ? 'selected' : ''}>ทุกกล่องดรอป</option>
            ${Array.from(knownContainers).map(c => `<option value="${escapeHtml(c)}" ${itemContainerFilter === c ? 'selected' : ''}>กล่อง: ${escapeHtml(c)}</option>`).join('')}
          </select>

          <select id="itemSortSelect" class="h-9 px-3 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-medium text-zinc-300 outline-none focus:border-indigo-500 cursor-pointer">
            <option value="DEFAULT" ${itemSortBy === 'DEFAULT' ? 'selected' : ''}>เรียงลำดับ: ค่าเริ่มต้น</option>
            <option value="RARITY_DESC" ${itemSortBy === 'RARITY_DESC' ? 'selected' : ''}>ความหายาก (S -> F)</option>
            <option value="PRICE_DESC" ${itemSortBy === 'PRICE_DESC' ? 'selected' : ''}>ราคาสูง -> ต่ำ</option>
            <option value="PRICE_ASC" ${itemSortBy === 'PRICE_ASC' ? 'selected' : ''}>ราคาต่ำ -> สูง</option>
            <option value="WEIGHT_DESC" ${itemSortBy === 'WEIGHT_DESC' ? 'selected' : ''}>น้ำหนักดรอปมาก -> น้อย</option>
            <option value="NAME_ASC" ${itemSortBy === 'NAME_ASC' ? 'selected' : ''}>ชื่อไอเทม (ก-ฮ, A-Z)</option>
          </select>
        </div>

        <!-- Add Button -->
        <button id="addNewItemBtn" class="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-medium text-xs flex items-center justify-center gap-2 shadow-sm transition active:scale-95 cursor-pointer touch-target flex-shrink-0">
          ${SVG_ICONS.plus}
          <span>เพิ่มไอเทมใหม่</span>
        </button>
      </div>

      <!-- Quick Rarity Pills Bar -->
      <div class="flex items-center gap-1.5 overflow-x-auto pb-1 no-scrollbar text-xs">
        <button class="rarity-pill px-2.5 py-1 rounded-full text-[11px] font-medium border transition cursor-pointer ${itemRarityFilter === 'ALL' ? 'bg-indigo-600 border-indigo-500 text-white shadow-sm' : 'bg-zinc-900 border-zinc-800 text-zinc-400 hover:text-white'}" data-rank="ALL">
          ทั้งหมด (${totalCount})
        </button>
        ${RARITY_RANKS.map(r => {
          const isActive = itemRarityFilter === r;
          const style = RARITY_BADGE_STYLES[r] || RARITY_BADGE_STYLES.F;
          return `
            <button class="rarity-pill px-2.5 py-1 rounded-full text-[11px] font-medium border transition cursor-pointer flex items-center gap-1.5 ${isActive ? 'ring-2 ring-indigo-500 ' + style : style + ' opacity-75 hover:opacity-100'}" data-rank="${r}">
              <span>แรงค์ ${r}</span>
              <span class="text-[10px] font-mono px-1 rounded bg-black/30">${rarityCounts[r] || 0}</span>
            </button>
          `;
        }).join('')}
      </div>
    `;
    elements.formFieldsContainer.appendChild(controlPanel);

    // Event listeners for filters
    controlPanel.querySelector('#addNewItemBtn').addEventListener('click', () => openItemEditorModal());
    controlPanel.querySelector('#itemLocalSearchInput').addEventListener('input', (e) => {
      itemSearchQuery = e.target.value;
      renderItemsRegistryView();
    });
    controlPanel.querySelector('#itemRarityFilterSelect').addEventListener('change', (e) => {
      itemRarityFilter = e.target.value;
      renderItemsRegistryView();
    });
    controlPanel.querySelector('#itemContainerFilterSelect').addEventListener('change', (e) => {
      itemContainerFilter = e.target.value;
      renderItemsRegistryView();
    });
    controlPanel.querySelector('#itemSortSelect').addEventListener('change', (e) => {
      itemSortBy = e.target.value;
      renderItemsRegistryView();
    });
    controlPanel.querySelectorAll('.rarity-pill').forEach(btn => {
      btn.addEventListener('click', () => {
        itemRarityFilter = btn.dataset.rank;
        renderItemsRegistryView();
      });
    });

    if (itemKeys.length === 0) {
      const emptyBox = document.createElement('div');
      emptyBox.className = 'bg-zinc-900/60 border border-zinc-800/80 rounded-2xl p-12 text-center text-zinc-500';
      emptyBox.innerHTML = `
        <div class="w-12 h-12 mx-auto rounded-full bg-zinc-800 flex items-center justify-center text-zinc-500 mb-3">${SVG_ICONS.box}</div>
        <p class="text-sm font-semibold text-zinc-300 mb-1">ไม่พบไอเทมที่ตรงตามเงื่อนไข</p>
        <p class="text-xs text-zinc-500 mb-4">ลองปรับเงื่อนไขการค้นหาหรือกดปุ่มด้านล่างเพื่อสร้างไอเทมใหม่</p>
        <button id="emptyAddNewItemBtn" class="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-medium text-xs inline-flex items-center gap-1.5 shadow-sm transition">
          ${SVG_ICONS.plus} <span>เพิ่มไอเทมใหม่</span>
        </button>
      `;
      emptyBox.querySelector('#emptyAddNewItemBtn').addEventListener('click', () => openItemEditorModal());
      elements.formFieldsContainer.appendChild(emptyBox);
      return;
    }

    // Grid of Showcase Cards
    const grid = document.createElement('div');
    grid.className = 'w-full grid grid-cols-1 md:grid-cols-2 2xl:grid-cols-3 gap-4 sm:gap-5';

    itemKeys.forEach(itemId => {
      const item = itemsData[itemId] || {};
      const card = document.createElement('div');
      card.className = 'showcase-card rounded-2xl bg-zinc-900/90 border border-zinc-800/80 hover:border-zinc-700 p-4 sm:p-5 flex flex-col justify-between shadow-xl space-y-3.5';

      const rarity = normalizeRarity(item.rarity || item.tier || 'F');
      const rarityBadgeColor = RARITY_BADGE_STYLES[rarity] || RARITY_BADGE_STYLES.F;
      const priceFormatted = Number(item.price || item.value || 0).toLocaleString();

      const containers = Array.isArray(item.allowed_containers)
        ? item.allowed_containers
        : (item.allowed_containers ? [item.allowed_containers] : ['normal_crate']);

      const loreLines = Array.isArray(item.lore)
        ? item.lore
        : (item.lore ? String(item.lore).split('\n') : []);

      card.innerHTML = `
        <div>
          <!-- Header: Slot Icon + Title + Rarity Badge -->
          <div class="flex items-start justify-between gap-3 pb-3 border-b border-zinc-800/80">
            <div class="flex items-center gap-3 min-w-0">
              <div class="w-12 h-12 rounded-xl border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1.5 flex-shrink-0">
                <img class="w-8 h-8 mc-icon object-contain" src="${getMinecraftItemTexture(item.material || 'STONE')}" alt="${escapeHtml(item.material || 'STONE')}" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/block/${(item.material||'stone').toLowerCase()}.png';">
              </div>
              <div class="min-w-0">
                <div class="text-sm font-bold text-white truncate leading-tight">${formatMiniMessagePreview(item.display_name || itemId)}</div>
                <div class="flex items-center gap-1.5 mt-1">
                  <span class="font-mono text-[10px] text-zinc-400 bg-zinc-950 px-1.5 py-0.5 rounded border border-zinc-800 truncate" title="${itemId}">${escapeHtml(itemId)}</span>
                  <span class="text-[10px] font-mono text-zinc-500 truncate">${escapeHtml(item.material || 'STONE')}</span>
                </div>
              </div>
            </div>
            <div class="flex flex-col items-end gap-1 flex-shrink-0">
              <span class="text-[10px] font-bold px-2 py-0.5 rounded-full border ${rarityBadgeColor}">
                แรงค์ ${escapeHtml(rarity)}
              </span>
              <span class="text-xs font-bold font-mono text-emerald-400 bg-emerald-950/40 px-2 py-0.5 rounded border border-emerald-800/60">
                $${priceFormatted}
              </span>
            </div>
          </div>

          <!-- Body: Stats and Container Pills -->
          <div class="pt-3 space-y-2.5">
            <!-- Stats Row -->
            <div class="grid grid-cols-2 gap-2 text-xs">
              <div class="p-2 bg-zinc-950/60 border border-zinc-800/60 rounded-lg flex items-center justify-between">
                <span class="text-[11px] text-zinc-400 flex items-center gap-1.5">${SVG_ICONS.scale}<span>น้ำหนักดรอป:</span></span>
                <span class="font-mono font-semibold text-zinc-200">${item.weight || 10}</span>
              </div>
              <div class="p-2 bg-zinc-950/60 border border-zinc-800/60 rounded-lg flex items-center justify-between">
                <span class="text-[11px] text-zinc-400 flex items-center gap-1.5">${SVG_ICONS.clock}<span>เวลาค้นหา:</span></span>
                <span class="font-mono font-semibold text-zinc-200">${item.search_time || 2}s</span>
              </div>
            </div>

            <!-- Allowed Containers -->
            <div>
              <span class="text-[10px] font-semibold text-zinc-500 uppercase tracking-wider block mb-1">กล่องที่อนุญาตให้ดรอป (${containers.length}):</span>
              <div class="flex flex-wrap items-center gap-1">
                ${containers.slice(0, 4).map(c => `
                  <span class="text-[10px] px-2 py-0.5 rounded-md bg-zinc-800/80 border border-zinc-700/60 text-zinc-300 font-mono flex items-center gap-1">
                    <span class="text-zinc-500">${SVG_ICONS.crate}</span><span>${escapeHtml(c)}</span>
                  </span>
                `).join('')}
                ${containers.length > 4 ? `<span class="text-[10px] text-zinc-500 font-medium">+${containers.length - 4} กล่อง</span>` : ''}
              </div>
            </div>

            <!-- Lore preview (if present) -->
            ${loreLines.length > 0 ? `
              <div class="p-2 bg-zinc-950/80 border border-zinc-800/60 rounded-lg text-[11px] text-zinc-300 font-sans space-y-0.5">
                ${loreLines.slice(0, 2).map(l => `<div>${formatMiniMessagePreview(l)}</div>`).join('')}
                ${loreLines.length > 2 ? `<div class="text-[10px] text-zinc-500 italic">+${loreLines.length - 2} บรรทัด...</div>` : ''}
              </div>
            ` : ''}
          </div>
        </div>

        <!-- Action Footer -->
        <div class="pt-3 border-t border-zinc-800/80 flex items-center justify-between gap-2">
          <button class="edit-item-btn flex-1 py-1.5 px-3 rounded-lg bg-indigo-600/10 hover:bg-indigo-600 text-indigo-300 hover:text-white border border-indigo-500/30 hover:border-indigo-600 text-xs font-semibold flex items-center justify-center gap-1.5 transition cursor-pointer touch-target">
            ${SVG_ICONS.edit}
            <span>แก้ไขไอเทม</span>
          </button>
          <button class="duplicate-item-btn p-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-400 hover:text-white border border-zinc-700/60 transition cursor-pointer touch-target" title="คัดลอกไอเทมนี้">
            ${SVG_ICONS.copy}
          </button>
          <button class="delete-item-btn p-2 rounded-lg bg-zinc-800 hover:bg-red-500/20 text-zinc-500 hover:text-red-400 border border-zinc-700/60 hover:border-red-500/40 transition cursor-pointer touch-target" title="ลบไอเทมนี้">
            ${SVG_ICONS.trash}
          </button>
        </div>
      `;

      // Event bindings
      card.querySelector('.edit-item-btn').addEventListener('click', (e) => {
        e.stopPropagation();
        openItemEditorModal(itemId);
      });

      card.querySelector('.duplicate-item-btn').addEventListener('click', (e) => {
        e.stopPropagation();
        openItemEditorModal(null, item, itemId + '_copy');
      });

      card.querySelector('.delete-item-btn').addEventListener('click', (e) => {
        e.stopPropagation();
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

      // Clicking anywhere on the card opens editor
      card.addEventListener('click', () => openItemEditorModal(itemId));

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

    const catData = state.parsedData.categories || {};
    const catKeys = Object.keys(catData);

    // Modern Action Header / Control Bar
    const controlBar = document.createElement('div');
    controlBar.className = 'w-full flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-4 mb-2 border-b border-zinc-800/80';
    controlBar.innerHTML = `
      <div class="flex items-center gap-3">
        <div class="p-2 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
          ${SVG_ICONS.box}
        </div>
        <div>
          <div class="text-sm font-bold text-white flex items-center gap-2">
            <span>หมวดหมู่กล่องดรอปทั้งหมด</span>
            <span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">${catKeys.length} หมวดหมู่</span>
          </div>
          <div class="text-xs text-zinc-400">คลิกที่การ์ดเพื่อเปิดหน้าต่างแก้ไขข้อมูลอย่างละเอียด</div>
        </div>
      </div>
      <button id="addNewCategoryBtn" class="px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs flex items-center justify-center gap-2 shadow-lg shadow-emerald-950/40 transition active:scale-95 cursor-pointer w-full sm:w-auto touch-target">
        ${SVG_ICONS.plus}
        <span>เพิ่มหมวดหมู่กล่องใหม่</span>
      </button>
    `;
    elements.formFieldsContainer.appendChild(controlBar);

    controlBar.querySelector('#addNewCategoryBtn').addEventListener('click', () => openCategoryEditorModal());

    if (catKeys.length === 0) {
      const emptyBox = document.createElement('div');
      emptyBox.className = 'bg-zinc-900 border border-zinc-800 rounded-2xl p-12 text-center text-zinc-500';
      emptyBox.innerHTML = `
        <div class="w-12 h-12 mx-auto mb-3 text-zinc-600">${SVG_ICONS.box}</div>
        <div class="text-sm font-medium text-zinc-400 mb-1">ยังไม่มีหมวดหมู่กล่องดรอป</div>
        <div class="text-xs text-zinc-500 mb-4">กดปุ่ม "เพิ่มหมวดหมู่กล่องใหม่" ด้านบนเพื่อเริ่มกำหนดค่า</div>
      `;
      elements.formFieldsContainer.appendChild(emptyBox);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'w-full grid grid-cols-1 md:grid-cols-2 2xl:grid-cols-3 gap-5';

    catKeys.forEach(catId => {
      const cat = catData[catId] || {};
      const catColor = cat.color || '#10B981';
      const chances = cat.rarity_chances || {};

      // Normalize rarity values
      const fVal = chances.F !== undefined ? chances.F : (chances.COMMON !== undefined ? chances.COMMON : 0);
      const dVal = chances.D !== undefined ? chances.D : (chances.UNCOMMON !== undefined ? chances.UNCOMMON : 0);
      const cVal = chances.C !== undefined ? chances.C : (chances.RARE !== undefined ? chances.RARE : 0);
      const bVal = chances.B !== undefined ? chances.B : (chances.EPIC !== undefined ? chances.EPIC : 0);
      const aVal = chances.A !== undefined ? chances.A : (chances.LEGENDARY !== undefined ? chances.LEGENDARY : 0);
      const sVal = chances.S !== undefined ? chances.S : (chances.MYTHIC !== undefined ? chances.MYTHIC : 0);
      const totalChance = fVal + dVal + cVal + bVal + aVal + sVal;

      const card = document.createElement('div');
      card.className = 'showcase-card group bg-zinc-900/90 hover:bg-zinc-900 border border-zinc-800 hover:border-emerald-500/50 rounded-2xl p-5 shadow-lg transition-all flex flex-col justify-between cursor-pointer';

      card.innerHTML = `
        <div class="space-y-4">
          <!-- Card Header -->
          <div class="flex items-start justify-between gap-3 pb-3 border-b border-zinc-800/80">
            <div class="flex items-center gap-3 min-w-0">
              <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 border border-zinc-700/80 shadow-inner" style="background-color: ${catColor}15; border-color: ${catColor}40; color: ${catColor};">
                ${SVG_ICONS.box}
              </div>
              <div class="min-w-0">
                <div class="text-sm font-bold text-white truncate group-hover:text-emerald-300 transition-colors">
                  ${formatMiniMessagePreview(cat.display_name || cat.name || catId)}
                </div>
                <div class="flex items-center gap-2 mt-0.5">
                  <span class="font-mono text-[11px] text-zinc-400 bg-zinc-950 px-2 py-0.5 rounded border border-zinc-800 truncate">${escapeHtml(catId)}</span>
                  <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" style="background-color: ${catColor}" title="สีกล่อง: ${catColor}"></span>
                </div>
              </div>
            </div>

            <!-- Card Actions -->
            <div class="flex items-center gap-1 flex-shrink-0" onclick="event.stopPropagation();">
              <button class="edit-cat-btn p-1.5 rounded-lg bg-zinc-800/80 hover:bg-emerald-600/20 text-zinc-400 hover:text-emerald-300 border border-zinc-700/60 hover:border-emerald-500/40 transition cursor-pointer" title="แก้ไขหมวดหมู่นี้">
                ${SVG_ICONS.edit}
              </button>
              <button class="duplicate-cat-btn p-1.5 rounded-lg bg-zinc-800/80 hover:bg-zinc-700 text-zinc-400 hover:text-white border border-zinc-700/60 transition cursor-pointer" title="คัดลอกหมวดหมู่นี้">
                ${SVG_ICONS.copy}
              </button>
              <button class="delete-cat-btn p-1.5 rounded-lg bg-zinc-800/80 hover:bg-red-500/20 text-zinc-500 hover:text-red-400 border border-zinc-700/60 hover:border-red-500/40 transition cursor-pointer" title="ลบหมวดหมู่นี้">
                ${SVG_ICONS.trash}
              </button>
            </div>
          </div>

          <!-- Rarity Distribution Progress Bar -->
          <div class="space-y-1.5">
            <div class="flex items-center justify-between text-[11px]">
              <span class="text-zinc-400 font-medium">สัดส่วนเรทดรอป (Rarity Chances):</span>
              <span class="font-mono font-bold ${Math.abs(totalChance - 100) < 0.01 ? 'text-emerald-400' : 'text-amber-400'}">
                รวม ${totalChance.toFixed(1).replace(/\\.0$/, '')}%
              </span>
            </div>
            <div class="rarity-progress-bar">
              ${fVal > 0 ? `<div class="rarity-bar-seg rarity-bar-f" style="width: ${fVal}%" title="F: ${fVal}%"></div>` : ''}
              ${dVal > 0 ? `<div class="rarity-bar-seg rarity-bar-d" style="width: ${dVal}%" title="D: ${dVal}%"></div>` : ''}
              ${cVal > 0 ? `<div class="rarity-bar-seg rarity-bar-c" style="width: ${cVal}%" title="C: ${cVal}%"></div>` : ''}
              ${bVal > 0 ? `<div class="rarity-bar-seg rarity-bar-b" style="width: ${bVal}%" title="B: ${bVal}%"></div>` : ''}
              ${aVal > 0 ? `<div class="rarity-bar-seg rarity-bar-a" style="width: ${aVal}%" title="A: ${aVal}%"></div>` : ''}
              ${sVal > 0 ? `<div class="rarity-bar-seg rarity-bar-s" style="width: ${sVal}%" title="S: ${sVal}%"></div>` : ''}
            </div>
            <!-- Rarity Percent Legend Pills -->
            <div class="flex items-center justify-between gap-1 pt-1 text-[10px] font-mono">
              <span class="px-1.5 py-0.5 rounded bg-zinc-800/80 text-zinc-300 border border-zinc-700">F: ${fVal}%</span>
              <span class="px-1.5 py-0.5 rounded bg-emerald-950/60 text-emerald-400 border border-emerald-800/60">D: ${dVal}%</span>
              <span class="px-1.5 py-0.5 rounded bg-cyan-950/60 text-cyan-400 border border-cyan-800/60">C: ${cVal}%</span>
              <span class="px-1.5 py-0.5 rounded bg-purple-950/60 text-purple-400 border border-purple-800/60">B: ${bVal}%</span>
              <span class="px-1.5 py-0.5 rounded bg-amber-950/60 text-amber-400 border border-amber-800/60">A: ${aVal}%</span>
              <span class="px-1.5 py-0.5 rounded bg-rose-950/60 text-rose-400 border border-rose-800/60">S: ${sVal}%</span>
            </div>
          </div>

          <!-- Gameplay Details Grid -->
          <div class="grid grid-cols-2 gap-2 text-xs bg-zinc-950/60 p-3 rounded-xl border border-zinc-800/60">
            <div class="flex items-center justify-between">
              <span class="text-zinc-500">จำนวนดรอป:</span>
              <span class="font-mono font-semibold text-zinc-200">${cat.min_rolls || 1} - ${cat.max_rolls || 4} ชิ้น</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-zinc-500">เวลาค้นหา:</span>
              <span class="font-mono font-semibold text-zinc-200">${cat.search_time_min || 2} - ${cat.search_time_max || 4}s</span>
            </div>
            <div class="flex items-center gap-1.5 pt-1 border-t border-zinc-800/50">
              <span class="text-zinc-500">บล็อกปิด:</span>
              <span class="font-mono text-[11px] text-zinc-300 truncate" title="${cat.covered_material || 'BLACK_STAINED_GLASS_PANE'}">${escapeHtml(cat.covered_material || 'GLASS_PANE')}</span>
            </div>
            <div class="flex items-center gap-1.5 pt-1 border-t border-zinc-800/50">
              <span class="text-zinc-500">บล็อกค้นหา:</span>
              <span class="font-mono text-[11px] text-zinc-300 truncate" title="${cat.searching_material || 'STONE'}">${escapeHtml(cat.searching_material || 'STONE')}</span>
            </div>
          </div>
        </div>

        <!-- Footer Edit Button -->
        <div class="pt-4 mt-2 border-t border-zinc-800/80 flex items-center justify-between">
          <span class="text-[11px] text-zinc-500">คลิกเพื่อแก้ไข</span>
          <button class="px-3 py-1.5 rounded-lg bg-zinc-800 group-hover:bg-emerald-600/20 text-zinc-300 group-hover:text-emerald-300 border border-zinc-700/60 group-hover:border-emerald-500/40 text-xs font-medium flex items-center gap-1.5 transition">
            ${SVG_ICONS.edit}
            <span>แก้ไขกล่องดรอป</span>
          </button>
        </div>
      `;

      // Event Listeners
      card.addEventListener('click', () => openCategoryEditorModal(catId));

      card.querySelector('.edit-cat-btn').addEventListener('click', () => {
        openCategoryEditorModal(catId);
      });

      card.querySelector('.duplicate-cat-btn').addEventListener('click', () => {
        openCategoryEditorModal(null, cat, catId + '_copy');
      });

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

    const caseData = state.parsedData.safe_cases || {};
    const caseKeys = Object.keys(caseData);

    // Modern Action Header / Control Bar
    const controlBar = document.createElement('div');
    controlBar.className = 'w-full flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-4 mb-2 border-b border-zinc-800/80';
    controlBar.innerHTML = `
      <div class="flex items-center gap-3">
        <div class="p-2 rounded-xl bg-sky-500/10 border border-sky-500/20 text-sky-400">
          ${SVG_ICONS.safe_cases_settings}
        </div>
        <div>
          <div class="text-sm font-bold text-white flex items-center gap-2">
            <span>กระเป๋านิรภัยทั้งหมด</span>
            <span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-sky-500/10 text-sky-400 border border-sky-500/20">${caseKeys.length} แบบ</span>
          </div>
          <div class="text-xs text-zinc-400">กระเป๋าเก็บไอเทมติดตัวที่ไม่สูญหายเมื่อเสียชีวิต คลิกที่การ์ดเพื่อแก้ไข</div>
        </div>
      </div>
      <button id="addNewCaseBtn" class="px-4 py-2.5 rounded-xl bg-sky-600 hover:bg-sky-500 text-white font-semibold text-xs flex items-center justify-center gap-2 shadow-lg shadow-sky-950/40 transition active:scale-95 cursor-pointer w-full sm:w-auto touch-target">
        ${SVG_ICONS.plus}
        <span>เพิ่มกระเป๋านิรภัยใหม่</span>
      </button>
    `;
    elements.formFieldsContainer.appendChild(controlBar);

    controlBar.querySelector('#addNewCaseBtn').addEventListener('click', () => openSafeCaseEditorModal());

    if (caseKeys.length === 0) {
      const emptyBox = document.createElement('div');
      emptyBox.className = 'bg-zinc-900 border border-zinc-800 rounded-2xl p-12 text-center text-zinc-500';
      emptyBox.innerHTML = `
        <div class="w-12 h-12 mx-auto mb-3 text-zinc-600">${SVG_ICONS.safe_cases_settings}</div>
        <div class="text-sm font-medium text-zinc-400 mb-1">ยังไม่มีกระเป๋านิรภัยในระบบ</div>
        <div class="text-xs text-zinc-500 mb-4">กดปุ่ม "เพิ่มกระเป๋านิรภัยใหม่" ด้านบนเพื่อเริ่มสร้าง</div>
      `;
      elements.formFieldsContainer.appendChild(emptyBox);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'w-full grid grid-cols-1 md:grid-cols-2 2xl:grid-cols-3 gap-5';

    caseKeys.forEach(caseId => {
      const c = caseData[caseId] || {};
      const capacity = parseInt(c.capacity, 10) || 9;
      const keepsOnDeath = c.keeps_on_death !== false;
      const mat = (c.material || 'BUNDLE').toUpperCase().trim();
      const cmd = parseInt(c.custom_model_data, 10) || 0;

      // Generate mini inventory visual grid (shows up to max 18 or 27 slots representation)
      const maxSlotsToShow = Math.min(Math.max(capacity, 9), 27);
      let slotsHtml = '';
      for (let i = 0; i < maxSlotsToShow; i++) {
        const isActive = i < capacity;
        slotsHtml += `<div class="slot-cell ${isActive ? 'active-slot' : 'opacity-20'}" title="ช่องที่ ${i + 1}${isActive ? ' (ใช้งานได้)' : ''}"></div>`;
      }

      const card = document.createElement('div');
      card.className = 'showcase-card group bg-zinc-900/90 hover:bg-zinc-900 border border-zinc-800 hover:border-sky-500/50 rounded-2xl p-5 shadow-lg transition-all flex flex-col justify-between cursor-pointer';

      card.innerHTML = `
        <div class="space-y-4">
          <!-- Card Header -->
          <div class="flex items-start justify-between gap-3 pb-3 border-b border-zinc-800/80">
            <div class="flex items-center gap-3 min-w-0">
              <div class="w-12 h-12 rounded-xl border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1.5 flex-shrink-0 group-hover:border-sky-500/60 transition-colors shadow-inner">
                <img class="w-8 h-8 mc-icon object-contain group-hover:scale-110 transition-transform" src="${getMinecraftItemTexture(mat)}" alt="${escapeHtml(mat)}" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/item/bundle.png';">
              </div>
              <div class="min-w-0">
                <div class="text-sm font-bold text-white truncate group-hover:text-sky-300 transition-colors">
                  ${formatMiniMessagePreview(c.display_name || c.title || caseId)}
                </div>
                <div class="flex items-center gap-2 mt-0.5 flex-wrap">
                  <span class="font-mono text-[11px] text-zinc-400 bg-zinc-950 px-2 py-0.5 rounded border border-zinc-800 truncate">${escapeHtml(caseId)}</span>
                  <span class="font-mono text-[10px] text-sky-400 bg-sky-950/50 px-1.5 py-0.5 rounded border border-sky-800/40">${escapeHtml(mat)}</span>
                  ${cmd > 0 ? `<span class="text-[10px] font-mono text-zinc-400 bg-zinc-800 px-1.5 py-0.5 rounded">CMD: ${cmd}</span>` : ''}
                </div>
              </div>
            </div>

            <!-- Card Actions -->
            <div class="flex items-center gap-1 flex-shrink-0" onclick="event.stopPropagation();">
              <button class="edit-case-btn p-1.5 rounded-lg bg-zinc-800/80 hover:bg-sky-600/20 text-zinc-400 hover:text-sky-300 border border-zinc-700/60 hover:border-sky-500/40 transition cursor-pointer" title="แก้ไขกระเป๋านี้">
                ${SVG_ICONS.edit}
              </button>
              <button class="duplicate-case-btn p-1.5 rounded-lg bg-zinc-800/80 hover:bg-zinc-700 text-zinc-400 hover:text-white border border-zinc-700/60 transition cursor-pointer" title="คัดลอกกระเป๋านี้">
                ${SVG_ICONS.copy}
              </button>
              <button class="delete-case-btn p-1.5 rounded-lg bg-zinc-800/80 hover:bg-red-500/20 text-zinc-500 hover:text-red-400 border border-zinc-700/60 hover:border-red-500/40 transition cursor-pointer" title="ลบกระเป๋านี้">
                ${SVG_ICONS.trash}
              </button>
            </div>
          </div>

          <!-- Capacity & Perks Badges -->
          <div class="flex items-center justify-between gap-2 flex-wrap">
            <div class="flex items-center gap-2">
              <span class="text-xs font-bold px-2.5 py-1 rounded-lg bg-sky-950/70 border border-sky-800/60 text-sky-300 flex items-center gap-1.5">
                ${SVG_ICONS.backpack}
                <span>ความจุ ${capacity} ช่อง</span>
              </span>
              <span class="text-[11px] font-medium px-2 py-0.5 rounded-md flex items-center gap-1.5 ${keepsOnDeath ? 'bg-emerald-950/60 border border-emerald-800/60 text-emerald-400' : 'bg-red-950/60 border border-red-800/60 text-red-400'}">
                ${keepsOnDeath ? `${SVG_ICONS.shield}<span>ของไม่ตกเมื่อตาย</span>` : `${SVG_ICONS.alert_triangle}<span>ของตกเมื่อตาย</span>`}
              </span>
            </div>
          </div>

          <!-- Mini Inventory Visual Grid Preview -->
          <div class="p-3 bg-zinc-950/70 rounded-xl border border-zinc-800/70 space-y-1.5">
            <div class="flex items-center justify-between text-[11px] text-zinc-400">
              <span class="font-medium">ขนาดกระเป๋าในเกม:</span>
              <span class="font-mono text-zinc-500 text-[10px]">${capacity} / ${capacity > 9 ? (Math.ceil(capacity / 9) * 9) : 9} ช่อง</span>
            </div>
            <div class="grid grid-cols-9 gap-1.5 p-2 bg-zinc-900/80 rounded-lg border border-zinc-800/80 justify-center">
              ${slotsHtml}
            </div>
          </div>
        </div>

        <!-- Footer Edit Button -->
        <div class="pt-4 mt-2 border-t border-zinc-800/80 flex items-center justify-between">
          <span class="text-[11px] text-zinc-500">คลิกเพื่อแก้ไข</span>
          <button class="px-3 py-1.5 rounded-lg bg-zinc-800 group-hover:bg-sky-600/20 text-zinc-300 group-hover:text-sky-300 border border-zinc-700/60 group-hover:border-sky-500/40 text-xs font-medium flex items-center gap-1.5 transition">
            ${SVG_ICONS.edit}
            <span>แก้ไขกระเป๋านิรภัย</span>
          </button>
        </div>
      `;

      // Event Listeners
      card.addEventListener('click', () => openSafeCaseEditorModal(caseId));

      card.querySelector('.edit-case-btn').addEventListener('click', () => {
        openSafeCaseEditorModal(caseId);
      });

      card.querySelector('.duplicate-case-btn').addEventListener('click', () => {
        openSafeCaseEditorModal(null, c, caseId + '_copy');
      });

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
      <div class="flex flex-wrap items-center gap-2.5 sm:gap-3 w-full sm:w-auto">
        <div class="text-xs text-zinc-400 w-full sm:w-auto">
          ข้อความทั้งหมด <span class="font-bold text-white">${Object.keys(flatKeys).length}</span> รายการ
          ${keyList.length !== Object.keys(flatKeys).length ? `<span class="text-zinc-500">(แสดงผล ${keyList.length})</span>` : ''}
        </div>
        <input type="text" id="langLocalSearchInput" placeholder="ค้นหาคีย์ข้อความหรือเนื้อหา..." value="${escapeHtml(langKeySearchQuery)}"
               class="h-8 px-2.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs text-zinc-200 placeholder-zinc-500 outline-none focus:border-indigo-500 w-full sm:w-56">
      </div>
      <button id="addNewKeyBtn" class="px-3.5 py-2 rounded-lg bg-amber-600 hover:bg-amber-500 text-white font-medium text-xs flex items-center justify-center gap-1.5 shadow-sm transition active:scale-95 cursor-pointer w-full sm:w-auto touch-target">
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
    container.className = 'w-full bg-zinc-900/90 border border-zinc-800 rounded-2xl shadow-xl overflow-x-auto touch-scroll';

    const table = document.createElement('table');
    table.className = 'w-full min-w-[550px] border-collapse text-left';
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
    actionHeader.className = 'w-full flex flex-wrap items-center justify-between gap-3 pb-4 mb-2';
    actionHeader.innerHTML = `
      <div class="text-xs text-zinc-400">
        โครงสร้างไฟล์ <span class="font-mono text-zinc-200">${escapeHtml(state.activeFile)}</span>
      </div>
      <button id="addGenericKeyBtn" class="px-3.5 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-white font-medium text-xs flex items-center justify-center gap-1.5 border border-zinc-700 shadow-sm transition active:scale-95 cursor-pointer w-full sm:w-auto touch-target">
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
    card.className = 'w-full bg-zinc-900/90 border border-zinc-800 rounded-2xl shadow-xl overflow-x-auto touch-scroll';
    const table = document.createElement('table');
    table.className = 'w-full min-w-[550px] border-collapse text-left';
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

  // --- Categorized Editor Modals with Multi-Tab Navigation & Live Preview ---

  /**
   * Comprehensive Multi-Tab Modal Editor for Items (items.yml)
   * Supports both Creating (itemId = null) and Editing (itemId = string)
   */
  function openItemEditorModal(itemId = null, preset = null, defaultId = '') {
    const isEditing = Boolean(itemId && state.parsedData.items && state.parsedData.items[itemId]);
    const itemData = isEditing ? state.parsedData.items[itemId] : (preset || {});
    const targetId = isEditing ? itemId : defaultId;

    // Setup Header Title
    elements.entryModalTitle.innerHTML = `
      <div class="flex items-center gap-2.5">
        <span class="p-2 rounded-xl ${isEditing ? 'bg-indigo-600/20 text-indigo-400' : 'bg-emerald-600/20 text-emerald-400'}">
          ${isEditing ? SVG_ICONS.edit : SVG_ICONS.plus}
        </span>
        <div>
          <div class="text-sm font-bold text-white">${isEditing ? `แก้ไขไอเทม: ${escapeHtml(itemId)}` : (preset ? 'คัดลอกและสร้างไอเทมใหม่' : 'สร้างไอเทมใหม่')}</div>
          <div class="text-[11px] text-zinc-400">${isEditing ? 'ปรับแต่งคุณสมบัติ ราคา และการแสดงผลของไอเทม' : 'กำหนดค่าไอเทมใหม่ลงในระบบ'}</div>
        </div>
      </div>
    `;

    // Setup Categorized Tabs
    setupModalTabs([
      { id: 'tab-item-general', label: 'ข้อมูลและโมเดล', icon: SVG_ICONS.box },
      { id: 'tab-item-stats', label: 'ราคาและสถิติ', icon: SVG_ICONS.coins },
      { id: 'tab-item-containers', label: 'กล่องที่พบได้', icon: SVG_ICONS.crate },
      { id: 'tab-item-lore', label: 'คำอธิบาย (Lore)', icon: SVG_ICONS.file_code }
    ], 'tab-item-general');

    const curMaterial = (itemData.material || 'IRON_SWORD').toUpperCase().trim();
    const curRarity = normalizeRarity(itemData.rarity || itemData.tier || 'C');
    const curName = itemData.display_name || itemData.name || '';
    const curPrice = itemData.price !== undefined ? itemData.price : (itemData.value !== undefined ? itemData.value : 100);
    const curWeight = itemData.weight !== undefined ? itemData.weight : 10;
    const curSearchTime = itemData.search_time !== undefined ? itemData.search_time : 2;
    const curCmd = itemData.custom_model_data || 0;
    const curSound = itemData.reveal_sound || 'ENTITY_ITEM_PICKUP';

    let selectedContainers = Array.isArray(itemData.allowed_containers)
      ? [...itemData.allowed_containers]
      : (itemData.allowed_containers ? [itemData.allowed_containers] : ['normal_crate', 'airdrop']);

    let loreText = Array.isArray(itemData.lore)
      ? itemData.lore.join('\n')
      : (itemData.lore || '');

    // Render Modal Body with Tab Contents
    elements.entryModalBody.innerHTML = `
      <div class="space-y-4">
        <!-- Live Preview Banner (Sticky at top of modal) -->
        <div class="p-3 bg-zinc-950/90 border border-zinc-800 rounded-xl flex items-center justify-between gap-3 shadow-inner">
          <div class="flex items-center gap-3 min-w-0">
            <button type="button" id="m_item_slot_picker_btn" class="w-12 h-12 rounded-xl border border-zinc-700/80 item-slot-frame flex items-center justify-center p-2 flex-shrink-0 cursor-pointer group hover:border-indigo-500 transition shadow-inner" title="คลิกเพื่อเลือกไอเทมในเกม">
              <img id="m_item_preview_icon" src="${getMinecraftItemTexture(curMaterial)}" class="w-8 h-8 mc-icon object-contain group-hover:scale-110 transition-transform" alt="${escapeHtml(curMaterial)}" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/block/${curMaterial.toLowerCase()}.png';">
            </button>
            <div class="min-w-0">
              <div id="m_item_preview_name" class="text-sm font-bold text-white truncate">
                ${formatMiniMessagePreview(curName || curMaterial)}
              </div>
              <div class="flex items-center gap-2 mt-0.5">
                <span id="m_item_preview_mat" class="font-mono text-[11px] text-zinc-400 truncate">${escapeHtml(curMaterial)}</span>
                <span id="m_item_preview_rank" class="text-[10px] font-bold px-1.5 py-0.2 rounded border rarity-badge-${curRarity}">แรงค์ ${curRarity}</span>
              </div>
            </div>
          </div>
          <button type="button" id="m_item_pick_all_btn" class="px-3 py-1.5 rounded-lg bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 hover:text-white text-xs font-semibold flex items-center gap-1.5 border border-indigo-500/30 transition active:scale-95 cursor-pointer flex-shrink-0">
            ${SVG_ICONS.box}
            <span>เลือกไอเทม</span>
          </button>
        </div>

        <!-- TAB 1: General & Appearance -->
        <div id="tab-item-general" class="modal-tab-content space-y-4 active">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">รหัสไอเทม (Item ID):</label>
              <input type="text" id="m_item_id_input" value="${escapeHtml(targetId)}" placeholder="เช่น combat_knife, gold_coin" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-indigo-500 transition ${isEditing ? 'opacity-60 cursor-not-allowed bg-zinc-900' : ''}" ${isEditing ? 'disabled' : ''}>
              <span class="text-[10px] text-zinc-500 mt-1 block">${isEditing ? 'รหัสไอเทมคงที่ ไม่สามารถแก้ไขได้' : 'ตัวพิมพ์เล็ก ห้ามเว้นวรรค (ใช้ _ แทน)'}</span>
            </div>
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">ชนิดไอเทม (Minecraft Material):</label>
              <div class="flex items-center gap-1.5">
                <input type="text" id="m_item_mat_input" value="${escapeHtml(curMaterial)}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-indigo-500 transition">
                <button type="button" id="m_item_mat_sub_btn" class="px-2.5 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 transition cursor-pointer flex-shrink-0">
                  เลือก
                </button>
              </div>
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-1.5 flex-wrap gap-1">
              <label class="text-xs font-semibold text-zinc-300">ชื่อไอเทม (Display Name - รองรับสีทุกรูปแบบ):</label>
              <!-- Quick Color Swatches Bar -->
              <div class="flex items-center gap-1.5 p-1 bg-zinc-950/80 rounded-lg border border-zinc-800" id="m_item_quick_colors" title="คลิกเพื่อใส่แท็กสีด่วนตามระดับความหายาก">
                <span class="text-[10px] text-zinc-500 font-medium px-1">สีด่วน:</span>
                ${RARITY_COLOR_LIST.map(rc => `
                  <button type="button" data-hex="${rc.hex}" class="quick-color-btn" style="background-color: ${rc.hex};" title="ใส่สี ${rc.name} (${rc.hex})"></button>
                `).join('')}
              </div>
            </div>
            <input type="text" id="m_item_name_input" value="${escapeHtml(curName)}" placeholder="เช่น Tactical Knife หรือ <color:#EF4444>Tactical Knife</color>" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs text-zinc-100 outline-none focus:border-indigo-500 transition font-mono">
            <span class="text-[10px] text-zinc-500 mt-1 block">รองรับทุกรูปแบบ (&, §, &#HEX, &lt;color:#HEX&gt;, &lt;gradient&gt;) หรือคลิกเลือกระดับด้านล่างเพื่อใส่สีอัตโนมัติ</span>
          </div>

          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-2">ระดับความหายาก (Rarity Rank):</label>
            <div class="grid grid-cols-6 gap-2" id="m_item_rarity_group">
              ${RARITY_RANKS.map(r => `
                <button type="button" data-rank="${r}" class="m-rarity-btn py-2 px-1 rounded-xl text-xs font-bold font-mono transition cursor-pointer flex flex-col items-center gap-0.5 border ${r === curRarity ? 'active ring-2 ring-white ' + getRarityButtonClass(r) : 'bg-zinc-950 border-zinc-800 text-zinc-400 hover:text-white hover:border-zinc-700'}">
                  <span class="text-xs">${r}</span>
                  <span class="text-[9px] opacity-70">${getRarityLabel(r)}</span>
                </button>
              `).join('')}
            </div>
            <input type="hidden" id="m_item_rarity_input" value="${curRarity}">
          </div>

          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">CustomModelData (CMD):</label>
            <input type="number" id="m_item_cmd_input" value="${curCmd}" placeholder="0" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-indigo-500 transition">
            <span class="text-[10px] text-zinc-500 mt-1 block">ระบุตัวเลขโมเดล 3D จาก Resource Pack (0 หากใช้ไอเทมมาตรฐาน)</span>
          </div>
        </div>

        <!-- TAB 2: Stats & Economy -->
        <div id="tab-item-stats" class="modal-tab-content space-y-4">
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">ราคาขาย ($):</label>
              <div class="relative">
                <span class="absolute left-3 top-2 text-zinc-500 text-xs">$</span>
                <input type="number" id="m_item_price_input" value="${curPrice}" class="w-full pl-6 pr-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-indigo-500 transition">
              </div>
              <span class="text-[10px] text-zinc-500 mt-1 block">มูลค่าในการขายหรือแลกเปลี่ยน</span>
            </div>
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">น้ำหนักดรอป (Weight):</label>
              <input type="number" step="0.5" id="m_item_weight_input" value="${curWeight}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-indigo-500 transition">
              <span class="text-[10px] text-zinc-500 mt-1 block">ยิ่งเยอะโอกาสออกยิ่งสูง</span>
            </div>
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">เวลาค้นหา (Search Time):</label>
              <div class="relative">
                <input type="number" id="m_item_time_input" value="${curSearchTime}" class="w-full pr-8 pl-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-indigo-500 transition">
                <span class="absolute right-3 top-2 text-zinc-500 text-xs">วินาที</span>
              </div>
              <span class="text-[10px] text-zinc-500 mt-1 block">เวลาที่ใช้คุ้ยหาในกล่อง</span>
            </div>
          </div>

          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">เสียงเมื่อเปิดเจอไอเทม (Reveal Sound):</label>
            <input type="text" id="m_item_sound_input" value="${escapeHtml(curSound)}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-indigo-500 transition">
            <span class="text-[10px] text-zinc-500 mt-1 block">เช่น ENTITY_ITEM_PICKUP, BLOCK_CHEST_OPEN</span>
          </div>
        </div>

        <!-- TAB 3: Allowed Containers -->
        <div id="tab-item-containers" class="modal-tab-content space-y-3">
          <div class="flex items-center justify-between">
            <label class="text-xs font-semibold text-zinc-300">เลือกกล่องที่ไอเทมชิ้นนี้สามารถสุ่มเจอได้:</label>
          </div>
          <div id="m_item_containers_slot" class="pt-1"></div>
        </div>

        <!-- TAB 4: Lore & Tooltip -->
        <div id="tab-item-lore" class="modal-tab-content space-y-4">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ข้อความคำอธิบาย (Lore - 1 บรรทัดต่อ 1 บรรทัดข้อความ):</label>
            <textarea id="m_item_lore_input" rows="4" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-indigo-500 transition leading-relaxed resize-y" placeholder="<color:#777777>Tactical equipment</color>&#10;<color:#FFFF55>Extraction Value: </color><color:#FFAA00>$250</color>">${escapeHtml(loreText)}</textarea>
          </div>

          <!-- Real-Time Lore Live Preview Box -->
          <div class="space-y-1.5">
            <div class="text-[11px] font-semibold text-zinc-400">ภาพจำลองคำอธิบายในเกม (Live Lore Preview):</div>
            <div id="m_item_lore_preview" class="p-3 bg-zinc-950/90 border border-purple-900/40 rounded-xl font-mono text-xs space-y-1 min-h-[60px] shadow-inner">
              <!-- Rendered via JS -->
            </div>
          </div>
        </div>
      </div>
    `;

    // Helpers for Rarity Buttons
    function getRarityButtonClass(r) {
      switch (r) {
        case 'F': return 'bg-zinc-800 text-zinc-200 border-zinc-600';
        case 'D': return 'bg-emerald-950/70 text-emerald-300 border-emerald-600';
        case 'C': return 'bg-cyan-950/70 text-cyan-300 border-cyan-600';
        case 'B': return 'bg-purple-950/70 text-purple-300 border-purple-600';
        case 'A': return 'bg-amber-950/70 text-amber-300 border-amber-600';
        case 'S': return 'bg-rose-950/70 text-rose-300 border-rose-600';
        default: return 'bg-zinc-800 text-zinc-200 border-zinc-600';
      }
    }

    function getRarityLabel(r) {
      switch (r) {
        case 'F': return 'Common';
        case 'D': return 'Uncommon';
        case 'C': return 'Rare';
        case 'B': return 'Epic';
        case 'A': return 'Legendary';
        case 'S': return 'Mythic';
        default: return '';
      }
    }

    // Mount Interactive Container Selector
    const contSlot = elements.entryModalBody.querySelector('#m_item_containers_slot');
    if (contSlot) {
      renderContainerChipSelector(contSlot, selectedContainers, (updated) => {
        selectedContainers = updated;
      });
    }

    // Wire Rarity Group
    const rarityBtns = elements.entryModalBody.querySelectorAll('.m-rarity-btn');
    const rarityInput = elements.entryModalBody.querySelector('#m_item_rarity_input');
    const previewRankBadge = elements.entryModalBody.querySelector('#m_item_preview_rank');

    rarityBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        const rank = btn.getAttribute('data-rank');
        rarityInput.value = rank;
        rarityBtns.forEach(b => {
          b.className = `m-rarity-btn py-2 px-1 rounded-xl text-xs font-bold font-mono transition cursor-pointer flex flex-col items-center gap-0.5 border ${b.getAttribute('data-rank') === rank ? 'active ring-2 ring-white ' + getRarityButtonClass(rank) : 'bg-zinc-950 border-zinc-800 text-zinc-400 hover:text-white hover:border-zinc-700'}`;
        });
        if (previewRankBadge) {
          previewRankBadge.textContent = `แรงค์ ${rank}`;
          previewRankBadge.className = `text-[10px] font-bold px-1.5 py-0.2 rounded border rarity-badge-${rank}`;
        }
        // Smart Auto-Color: When selecting rank, update Display Name color to rank's color
        const rankColor = RARITY_COLORS[rank] || '#AAAAAA';
        nameInput.value = applyColorToDisplayName(nameInput.value, rankColor);
        refreshNamePreview();
      });
    });

    // Quick Color Swatches Listener
    elements.entryModalBody.querySelectorAll('#m_item_quick_colors .quick-color-btn').forEach(qcBtn => {
      qcBtn.addEventListener('click', () => {
        const hex = qcBtn.getAttribute('data-hex');
        if (hex) {
          nameInput.value = applyColorToDisplayName(nameInput.value, hex);
          refreshNamePreview();
        }
      });
    });

    // Material Selection & Live Preview
    const previewIcon = elements.entryModalBody.querySelector('#m_item_preview_icon');
    const previewName = elements.entryModalBody.querySelector('#m_item_preview_name');
    const previewMat = elements.entryModalBody.querySelector('#m_item_preview_mat');
    const matInput = elements.entryModalBody.querySelector('#m_item_mat_input');
    const nameInput = elements.entryModalBody.querySelector('#m_item_name_input');
    const idInput = elements.entryModalBody.querySelector('#m_item_id_input');

    const updateLiveMaterial = (newMat, readableName) => {
      newMat = newMat.toUpperCase().trim();
      matInput.value = newMat;
      if (previewMat) previewMat.textContent = newMat;
      if (previewIcon) {
        previewIcon.src = getMinecraftItemTexture(newMat);
        previewIcon.alt = newMat;
      }
      if (!nameInput.value.trim() && readableName) {
        nameInput.value = `<gradient:#00FFA3:#00B8FF><bold>${readableName}</bold></gradient>`;
        refreshNamePreview();
      }
      if (!isEditing && !idInput.value.trim()) {
        idInput.value = newMat.toLowerCase();
      }
    };

    const refreshNamePreview = () => {
      const raw = nameInput.value.trim() || matInput.value.trim() || 'New Item';
      if (previewName) {
        previewName.innerHTML = formatMiniMessagePreview(raw);
      }
    };

    nameInput.addEventListener('input', refreshNamePreview);
    matInput.addEventListener('input', (e) => {
      updateLiveMaterial(e.target.value);
    });

    const triggerItemPicker = () => {
      openMaterialPicker({
        currentMaterial: matInput.value || 'IRON_SWORD',
        title: 'เลือกไอเทมสำหรับเกม',
        onSelect: (selectedMat, itemObj) => {
          updateLiveMaterial(selectedMat, itemObj ? itemObj.readable : null);
        }
      });
    };

    elements.entryModalBody.querySelector('#m_item_slot_picker_btn').addEventListener('click', triggerItemPicker);
    elements.entryModalBody.querySelector('#m_item_pick_all_btn').addEventListener('click', triggerItemPicker);
    elements.entryModalBody.querySelector('#m_item_mat_sub_btn').addEventListener('click', triggerItemPicker);

    // Live Lore Preview Listener
    const loreInput = elements.entryModalBody.querySelector('#m_item_lore_input');
    const lorePreview = elements.entryModalBody.querySelector('#m_item_lore_preview');

    const refreshLorePreview = () => {
      const text = loreInput.value.trim();
      if (!text) {
        lorePreview.innerHTML = '<span class="text-zinc-600 italic">ไม่มีข้อความคำอธิบาย</span>';
        return;
      }
      const lines = text.split('\n');
      lorePreview.innerHTML = lines.map(line => `<div>${formatMiniMessagePreview(line)}</div>`).join('');
    };

    loreInput.addEventListener('input', refreshLorePreview);
    refreshLorePreview();

    // Confirm & Save
    openEntryModal(() => {
      const finalId = idInput.value.trim().toLowerCase().replace(/\s+/g, '_');
      if (!finalId) {
        showToast('กรุณาระบุรหัสไอเทม (Item ID)', 'error');
        return false;
      }

      if (!isEditing && state.parsedData.items && state.parsedData.items[finalId]) {
        showToast(`รหัสไอเทม "${finalId}" มีอยู่ในระบบแล้ว`, 'error');
        return false;
      }

      if (!state.parsedData.items) state.parsedData.items = {};

      const containers = (selectedContainers && selectedContainers.length > 0)
        ? [...selectedContainers]
        : ['normal_crate'];

      const rawLore = loreInput.value.trim();
      const loreLines = rawLore ? rawLore.split('\n').map(l => l.trim()).filter(Boolean) : [];

      const rank = rarityInput.value || 'C';
      const priceVal = parseFloat(elements.entryModalBody.querySelector('#m_item_price_input').value) || 100;

      const updatedItem = {
        material: matInput.value.trim().toUpperCase() || 'IRON_SWORD',
        display_name: nameInput.value.trim() || finalId,
        rarity: rank,
        tier: rank,
        price: priceVal,
        value: priceVal,
        weight: parseFloat(elements.entryModalBody.querySelector('#m_item_weight_input').value) || 10,
        search_time: parseInt(elements.entryModalBody.querySelector('#m_item_time_input').value, 10) || 2,
        custom_model_data: parseInt(elements.entryModalBody.querySelector('#m_item_cmd_input').value, 10) || 0,
        reveal_sound: elements.entryModalBody.querySelector('#m_item_sound_input').value.trim() || 'ENTITY_ITEM_PICKUP',
        allowed_containers: containers,
        lore: loreLines
      };

      state.parsedData.items[finalId] = updatedItem;
      state.rawYaml = stringifyYaml(state.parsedData);
      elements.rawYamlTextarea.value = state.rawYaml;
      setDirty(true);
      renderItemsRegistryView();
      showToast(`${isEditing ? `อัปเดตไอเทม "${finalId}"` : `สร้างไอเทม "${finalId}"`} เรียบร้อยแล้ว (กดบันทึกเพื่อนำไปใช้งาน)`, 'success');
      return true;
    }, isEditing ? 'บันทึกการแก้ไขไอเทม' : 'สร้างไอเทมใหม่');
  }

  /**
   * Comprehensive Multi-Tab Modal Editor for Drop Categories (categories.yml)
   * Supports both Creating (catId = null) and Editing (catId = string)
   */
  function openCategoryEditorModal(catId = null, preset = null, defaultId = '') {
    const isEditing = Boolean(catId && state.parsedData.categories && state.parsedData.categories[catId]);
    const catData = isEditing ? state.parsedData.categories[catId] : (preset || {});
    const targetId = isEditing ? catId : defaultId;

    // Setup Header Title
    elements.entryModalTitle.innerHTML = `
      <div class="flex items-center gap-2.5">
        <span class="p-2 rounded-xl ${isEditing ? 'bg-emerald-600/20 text-emerald-400' : 'bg-emerald-600/20 text-emerald-400'}">
          ${isEditing ? SVG_ICONS.edit : SVG_ICONS.plus}
        </span>
        <div>
          <div class="text-sm font-bold text-white">${isEditing ? `แก้ไขหมวดหมู่กล่อง: ${escapeHtml(catId)}` : (preset ? 'คัดลอกและสร้างหมวดหมู่กล่อง' : 'สร้างหมวดหมู่กล่องใหม่')}</div>
          <div class="text-[11px] text-zinc-400">กำหนดสีประจำกล่อง บล็อกโมเดล อัตราการดรอป และเวลาค้นหา</div>
        </div>
      </div>
    `;

    // Setup Categorized Tabs
    setupModalTabs([
      { id: 'tab-cat-general', label: 'ข้อมูลทั่วไปและบล็อก', icon: SVG_ICONS.box },
      { id: 'tab-cat-rolls', label: 'จำนวนและเวลาค้นหา', icon: SVG_ICONS.crate },
      { id: 'tab-cat-chances', label: 'โอกาสดรอป (Chances)', icon: SVG_ICONS.coins }
    ], 'tab-cat-general');

    const curName = catData.display_name || catData.name || '';
    let curColor = catData.color || '#10B981';
    const curMinRolls = catData.min_rolls || 1;
    const curMaxRolls = catData.max_rolls || 4;
    const curSearchMin = catData.search_time_min || 2;
    const curSearchMax = catData.search_time_max || 4;
    const curCovered = (catData.covered_material || 'BLACK_STAINED_GLASS_PANE').toUpperCase().trim();
    const curSearching = (catData.searching_material || 'STONE').toUpperCase().trim();
    const curSearchSound = catData.search_sound || 'BLOCK_STONE_HIT';
    const curRevealSound = catData.reveal_sound || 'ENTITY_ITEM_PICKUP';

    const rawChances = catData.rarity_chances || {};
    const initChances = {
      F: rawChances.F !== undefined ? rawChances.F : (rawChances.COMMON !== undefined ? rawChances.COMMON : 60),
      D: rawChances.D !== undefined ? rawChances.D : (rawChances.UNCOMMON !== undefined ? rawChances.UNCOMMON : 25),
      C: rawChances.C !== undefined ? rawChances.C : (rawChances.RARE !== undefined ? rawChances.RARE : 10),
      B: rawChances.B !== undefined ? rawChances.B : (rawChances.EPIC !== undefined ? rawChances.EPIC : 4),
      A: rawChances.A !== undefined ? rawChances.A : (rawChances.LEGENDARY !== undefined ? rawChances.LEGENDARY : 1),
      S: rawChances.S !== undefined ? rawChances.S : (rawChances.MYTHIC !== undefined ? rawChances.MYTHIC : 0)
    };

    // Render Modal Body with Tab Contents
    elements.entryModalBody.innerHTML = `
      <div class="space-y-4">
        <!-- Live Preview Header Banner -->
        <div class="p-3 bg-zinc-950/90 border border-zinc-800 rounded-xl flex items-center justify-between gap-3 shadow-inner">
          <div class="flex items-center gap-3 min-w-0">
            <div id="m_cat_preview_icon" class="w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 border shadow-inner" style="background-color: ${curColor}15; border-color: ${curColor}40; color: ${curColor};">
              ${SVG_ICONS.box}
            </div>
            <div class="min-w-0">
              <div id="m_cat_preview_name" class="text-sm font-bold text-white truncate">
                ${formatMiniMessagePreview(curName || targetId || 'New Category')}
              </div>
              <div class="flex items-center gap-2 mt-0.5">
                <span id="m_cat_preview_id" class="font-mono text-[11px] text-zinc-400 truncate">${escapeHtml(targetId || 'category_id')}</span>
                <span id="m_cat_preview_dot" class="w-2.5 h-2.5 rounded-full flex-shrink-0" style="background-color: ${curColor}"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- TAB 1: General & Blocks -->
        <div id="tab-cat-general" class="modal-tab-content space-y-4 active">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">รหัสหมวดหมู่ (Category ID):</label>
              <input type="text" id="m_cat_id_input" value="${escapeHtml(targetId)}" placeholder="เช่น supply_drop, weapon_crate" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition ${isEditing ? 'opacity-60 cursor-not-allowed bg-zinc-900' : ''}" ${isEditing ? 'disabled' : ''}>
              <span class="text-[10px] text-zinc-500 mt-1 block">${isEditing ? 'รหัสหมวดหมู่คงที่ ไม่สามารถแก้ไขได้' : 'ตัวพิมพ์เล็ก ห้ามเว้นวรรค (ใช้ _ แทน)'}</span>
            </div>
            <div>
              <div class="flex items-center justify-between mb-1.5 flex-wrap gap-1">
                <label class="text-xs font-semibold text-zinc-300">ชื่อกล่องที่แสดงผล (Display Name - รองรับสีทุกรูปแบบ):</label>
                <!-- Quick Color Swatches Bar -->
                <div class="flex items-center gap-1.5 p-1 bg-zinc-950/80 rounded-lg border border-zinc-800" id="m_cat_quick_colors" title="คลิกเพื่อใส่แท็กสีด่วนตามระดับความหายาก">
                  <span class="text-[10px] text-zinc-500 font-medium px-1">สีด่วน:</span>
                  ${RARITY_COLOR_LIST.map(rc => `
                    <button type="button" data-hex="${rc.hex}" class="quick-color-btn" style="background-color: ${rc.hex};" title="ใส่สี ${rc.name} (${rc.hex})"></button>
                  `).join('')}
                </div>
              </div>
              <input type="text" id="m_cat_name_input" value="${escapeHtml(curName)}" placeholder="เช่น กล่องอาวุธ หรือ <color:#EF4444>กล่องอาวุธ</color>" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs text-zinc-100 outline-none focus:border-emerald-500 transition font-mono">
              <span class="text-[10px] text-zinc-500 mt-1 block">รองรับทุกรูปแบบ (&, §, &#HEX, &lt;color:#HEX&gt;, &lt;gradient&gt;) หรือคลิกเลือกสีประจำระดับ</span>
            </div>
          </div>

          <!-- Color Picker -->
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">สีประจำกล่อง (Color Hex & Minecraft Colors):</label>
            <div id="m_cat_color_slot"></div>
          </div>

          <!-- Blocks -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">บล็อกปิดกล่อง (Covered Material):</label>
              <div class="flex items-center gap-1.5">
                <div class="w-8 h-8 rounded-lg border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1 flex-shrink-0">
                  <img id="m_cat_cov_preview" class="w-6 h-6 mc-icon object-contain" src="${getMinecraftItemTexture(curCovered)}" alt="">
                </div>
                <input type="text" id="m_cat_cov_input" value="${escapeHtml(curCovered)}" class="w-full px-2.5 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition">
                <button type="button" id="m_cat_cov_btn" class="px-2.5 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 transition cursor-pointer flex-shrink-0">
                  เลือก
                </button>
              </div>
            </div>
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">บล็อกค้นหา (Searching Material):</label>
              <div class="flex items-center gap-1.5">
                <div class="w-8 h-8 rounded-lg border border-zinc-700/80 item-slot-frame flex items-center justify-center p-1 flex-shrink-0">
                  <img id="m_cat_src_preview" class="w-6 h-6 mc-icon object-contain" src="${getMinecraftItemTexture(curSearching)}" alt="">
                </div>
                <input type="text" id="m_cat_src_input" value="${escapeHtml(curSearching)}" class="w-full px-2.5 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition">
                <button type="button" id="m_cat_src_btn" class="px-2.5 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 transition cursor-pointer flex-shrink-0">
                  เลือก
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- TAB 2: Rolls & Times -->
        <div id="tab-cat-rolls" class="modal-tab-content space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">จำนวนสุ่มดรอปต่ำสุด (Min Rolls):</label>
              <input type="number" min="1" id="m_cat_min_rolls" value="${curMinRolls}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition">
            </div>
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">จำนวนสุ่มดรอปสูงสุด (Max Rolls):</label>
              <input type="number" min="1" id="m_cat_max_rolls" value="${curMaxRolls}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition">
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">เวลาค้นหาต่ำสุด (วินาที):</label>
              <input type="number" min="0" id="m_cat_search_min" value="${curSearchMin}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition">
            </div>
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">เวลาค้นหาสูงสุด (วินาที):</label>
              <input type="number" min="1" id="m_cat_search_max" value="${curSearchMax}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition">
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2 border-t border-zinc-800/80">
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">เสียงระหว่างค้นหา (Search Sound):</label>
              <input type="text" id="m_cat_snd_search" value="${escapeHtml(curSearchSound)}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition">
            </div>
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">เสียงเมื่อเปิดกล่องสำเร็จ (Reveal Sound):</label>
              <input type="text" id="m_cat_snd_reveal" value="${escapeHtml(curRevealSound)}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-emerald-500 transition">
            </div>
          </div>
        </div>

        <!-- TAB 3: Chances & Balance -->
        <div id="tab-cat-chances" class="modal-tab-content space-y-4">
          <div class="flex items-center justify-between gap-2 flex-wrap">
            <div class="flex items-center gap-2">
              <label class="text-xs font-semibold text-zinc-300">สัดส่วนโอกาสออกไอเทมแต่ละระดับ (Rarity Chances %):</label>
              <span id="m_cat_chances_total" class="text-[10px] font-mono px-2 py-0.5 rounded-full border transition font-bold bg-zinc-800 text-zinc-400 border-zinc-700">
                รวม 100%
              </span>
            </div>
            <button type="button" id="m_cat_auto_balance_btn" class="px-2.5 py-1 rounded-lg bg-emerald-950/80 hover:bg-emerald-900 border border-emerald-700/60 text-emerald-300 text-xs font-semibold flex items-center gap-1.5 transition cursor-pointer active:scale-95 shadow-sm">
              ${SVG_ICONS.zap}
              <span>ปรับสัดส่วนให้ครบ 100%</span>
            </button>
          </div>

          <!-- Live Stacked Rarity Progress Bar -->
          <div class="space-y-1 bg-zinc-950 p-3 rounded-xl border border-zinc-800/80">
            <div class="rarity-progress-bar" id="m_cat_progress_bar">
              <!-- Dynamically populated -->
            </div>
          </div>

          <!-- Presets Bar -->
          <div class="flex flex-wrap items-center gap-1.5 p-2 bg-zinc-950/80 border border-zinc-800/80 rounded-xl">
            <span class="text-[10px] text-zinc-500 font-semibold px-1">พรีเซ็ทเรท:</span>
            ${RARITY_CHANCE_PRESETS.map(p => `
              <button type="button" data-preset="${p.id}" class="m-cat-preset-btn px-2.5 py-1 rounded-lg text-xs font-medium bg-zinc-900 hover:bg-zinc-800 text-zinc-300 hover:text-white border border-zinc-800 hover:border-zinc-700 transition cursor-pointer active:scale-95 flex items-center gap-1.5" title="${p.name} - ${p.desc}">
                <span>${p.icon}</span>
                <span>${p.name}</span>
              </button>
            `).join('')}
          </div>

          <!-- 6 Rarity Inputs -->
          <div class="grid grid-cols-3 sm:grid-cols-6 gap-2 text-center">
            <div class="bg-zinc-950 p-2 rounded-xl border border-zinc-800">
              <span class="text-xs text-zinc-400 block mb-1 font-bold">F (Common)</span>
              <input type="number" step="0.5" id="m_chance_f" value="${initChances.F}" class="w-full px-2 py-1.5 bg-zinc-900 border border-zinc-700 rounded-lg text-xs font-mono text-zinc-100 text-center outline-none focus:border-emerald-500">
            </div>
            <div class="bg-zinc-950 p-2 rounded-xl border border-emerald-900/40">
              <span class="text-xs text-emerald-400 block mb-1 font-bold">D (Uncommon)</span>
              <input type="number" step="0.5" id="m_chance_d" value="${initChances.D}" class="w-full px-2 py-1.5 bg-zinc-900 border border-emerald-800/60 rounded-lg text-xs font-mono text-emerald-300 text-center outline-none focus:border-emerald-500">
            </div>
            <div class="bg-zinc-950 p-2 rounded-xl border border-cyan-900/40">
              <span class="text-xs text-cyan-400 block mb-1 font-bold">C (Rare)</span>
              <input type="number" step="0.5" id="m_chance_c" value="${initChances.C}" class="w-full px-2 py-1.5 bg-zinc-900 border border-cyan-800/60 rounded-lg text-xs font-mono text-cyan-300 text-center outline-none focus:border-cyan-500">
            </div>
            <div class="bg-zinc-950 p-2 rounded-xl border border-purple-900/40">
              <span class="text-xs text-purple-400 block mb-1 font-bold">B (Epic)</span>
              <input type="number" step="0.5" id="m_chance_b" value="${initChances.B}" class="w-full px-2 py-1.5 bg-zinc-900 border border-purple-800/60 rounded-lg text-xs font-mono text-purple-300 text-center outline-none focus:border-purple-500">
            </div>
            <div class="bg-zinc-950 p-2 rounded-xl border border-amber-900/40">
              <span class="text-xs text-amber-400 block mb-1 font-bold">A (Legendary)</span>
              <input type="number" step="0.5" id="m_chance_a" value="${initChances.A}" class="w-full px-2 py-1.5 bg-zinc-900 border border-amber-800/60 rounded-lg text-xs font-mono text-amber-300 text-center outline-none focus:border-amber-500">
            </div>
            <div class="bg-zinc-950 p-2 rounded-xl border border-rose-900/40">
              <span class="text-xs text-rose-400 block mb-1 font-bold">S (Mythic)</span>
              <input type="number" step="0.5" id="m_chance_s" value="${initChances.S}" class="w-full px-2 py-1.5 bg-zinc-900 border border-rose-800/60 rounded-lg text-xs font-mono text-rose-300 text-center outline-none focus:border-rose-500">
            </div>
          </div>
        </div>
      </div>
    `;

    // Mount Color Picker & Live Category Theme Sync
    const colorSlot = elements.entryModalBody.querySelector('#m_cat_color_slot');
    const previewDot = elements.entryModalBody.querySelector('#m_cat_preview_dot');
    const previewIconBox = elements.entryModalBody.querySelector('#m_cat_preview_icon');
    let catColorPicker = null;

    const updateCategoryThemeColor = (newHex) => {
      curColor = newHex;
      if (previewDot) previewDot.style.backgroundColor = newHex;
      if (previewIconBox) {
        previewIconBox.style.backgroundColor = `${newHex}15`;
        previewIconBox.style.borderColor = `${newHex}40`;
        previewIconBox.style.color = newHex;
      }
    };

    if (colorSlot) {
      catColorPicker = renderMinecraftColorPicker(colorSlot, curColor, (newHex) => {
        updateCategoryThemeColor(newHex);
        nameInput.value = applyColorToDisplayName(nameInput.value, newHex);
        refreshCategoryNamePreview();
      });
    }

    // Quick Color Buttons Listener for Category Display Name
    elements.entryModalBody.querySelectorAll('#m_cat_quick_colors .quick-color-btn').forEach(qcBtn => {
      qcBtn.addEventListener('click', () => {
        const hex = qcBtn.getAttribute('data-hex');
        if (hex) {
          if (catColorPicker) catColorPicker.setColor(hex);
          updateCategoryThemeColor(hex);
          nameInput.value = applyColorToDisplayName(nameInput.value, hex);
          refreshCategoryNamePreview();
        }
      });
    });

    // Live Title Preview
    const nameInput = elements.entryModalBody.querySelector('#m_cat_name_input');
    const previewName = elements.entryModalBody.querySelector('#m_cat_preview_name');
    const refreshCategoryNamePreview = () => {
      if (previewName) {
        previewName.innerHTML = formatMiniMessagePreview(nameInput.value.trim() || 'New Category');
      }
    };
    nameInput.addEventListener('input', refreshCategoryNamePreview);

    // Material Pickers for Covered & Searching
    const covInput = elements.entryModalBody.querySelector('#m_cat_cov_input');
    const covPreview = elements.entryModalBody.querySelector('#m_cat_cov_preview');
    const srcInput = elements.entryModalBody.querySelector('#m_cat_src_input');
    const srcPreview = elements.entryModalBody.querySelector('#m_cat_src_preview');

    const updateCoveredMat = (m) => {
      m = m.toUpperCase().trim();
      covInput.value = m;
      if (covPreview) covPreview.src = getMinecraftItemTexture(m);
    };

    const updateSearchingMat = (m) => {
      m = m.toUpperCase().trim();
      srcInput.value = m;
      if (srcPreview) srcPreview.src = getMinecraftItemTexture(m);
    };

    elements.entryModalBody.querySelector('#m_cat_cov_btn').addEventListener('click', () => {
      openMaterialPicker({
        currentMaterial: covInput.value || 'BLACK_STAINED_GLASS_PANE',
        title: 'เลือกบล็อกปิดกล่อง (Covered)',
        filterType: 'BLOCKS',
        onSelect: (selectedMat) => updateCoveredMat(selectedMat)
      });
    });

    elements.entryModalBody.querySelector('#m_cat_src_btn').addEventListener('click', () => {
      openMaterialPicker({
        currentMaterial: srcInput.value || 'STONE',
        title: 'เลือกบล็อกค้นหา (Searching)',
        filterType: 'BLOCKS',
        onSelect: (selectedMat) => updateSearchingMat(selectedMat)
      });
    });

    covInput.addEventListener('input', (e) => updateCoveredMat(e.target.value));
    srcInput.addEventListener('input', (e) => updateSearchingMat(e.target.value));

    // Chances & Progress Bar Management
    const inF = elements.entryModalBody.querySelector('#m_chance_f');
    const inD = elements.entryModalBody.querySelector('#m_chance_d');
    const inC = elements.entryModalBody.querySelector('#m_chance_c');
    const inB = elements.entryModalBody.querySelector('#m_chance_b');
    const inA = elements.entryModalBody.querySelector('#m_chance_a');
    const inS = elements.entryModalBody.querySelector('#m_chance_s');
    const totalBadge = elements.entryModalBody.querySelector('#m_cat_chances_total');
    const progressBar = elements.entryModalBody.querySelector('#m_cat_progress_bar');

    const refreshModalChancesUI = () => {
      const f = parseFloat(inF.value) || 0;
      const d = parseFloat(inD.value) || 0;
      const c = parseFloat(inC.value) || 0;
      const b = parseFloat(inB.value) || 0;
      const a = parseFloat(inA.value) || 0;
      const s = parseFloat(inS.value) || 0;
      const sum = f + d + c + b + a + s;

      updateRarityChancesBadge(totalBadge, sum);

      // Render Progress Bar Segments
      progressBar.innerHTML = `
        ${f > 0 ? `<div class="rarity-bar-seg rarity-bar-f" style="width: ${f}%" title="F: ${f}%"></div>` : ''}
        ${d > 0 ? `<div class="rarity-bar-seg rarity-bar-d" style="width: ${d}%" title="D: ${d}%"></div>` : ''}
        ${c > 0 ? `<div class="rarity-bar-seg rarity-bar-c" style="width: ${c}%" title="C: ${c}%"></div>` : ''}
        ${b > 0 ? `<div class="rarity-bar-seg rarity-bar-b" style="width: ${b}%" title="B: ${b}%"></div>` : ''}
        ${a > 0 ? `<div class="rarity-bar-seg rarity-bar-a" style="width: ${a}%" title="A: ${a}%"></div>` : ''}
        ${s > 0 ? `<div class="rarity-bar-seg rarity-bar-s" style="width: ${s}%" title="S: ${s}%"></div>` : ''}
      `;
    };

    const setModalChancesValues = (ch) => {
      inF.value = ch.F;
      inD.value = ch.D;
      inC.value = ch.C;
      inB.value = ch.B;
      inA.value = ch.A;
      inS.value = ch.S;
      refreshModalChancesUI();
    };

    [inF, inD, inC, inB, inA, inS].forEach(inp => inp.addEventListener('input', refreshModalChancesUI));

    elements.entryModalBody.querySelectorAll('.m-cat-preset-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const pid = btn.getAttribute('data-preset');
        const p = RARITY_CHANCE_PRESETS.find(x => x.id === pid);
        if (p) {
          setModalChancesValues(p.chances);
          const topRank = ['S', 'A', 'B', 'C', 'D', 'F'].find(r => p.chances[r] > 0) || 'F';
          const rankColor = RARITY_COLORS[topRank] || '#10B981';
          if (catColorPicker) catColorPicker.setColor(rankColor);
          updateCategoryThemeColor(rankColor);
          nameInput.value = applyColorToDisplayName(nameInput.value, rankColor);
          refreshCategoryNamePreview();
          showToast(`ใช้พรีเซ็ท "${p.name}" พร้อมปรับสีเป็น ${topRank} (${rankColor}) แล้ว`, 'info');
        }
      });
    });

    elements.entryModalBody.querySelector('#m_cat_auto_balance_btn').addEventListener('click', () => {
      const cur = {
        F: parseFloat(inF.value) || 0,
        D: parseFloat(inD.value) || 0,
        C: parseFloat(inC.value) || 0,
        B: parseFloat(inB.value) || 0,
        A: parseFloat(inA.value) || 0,
        S: parseFloat(inS.value) || 0
      };
      setModalChancesValues(balanceChancesTo100(cur));
      showToast('ปรับสัดส่วนเรทดรอปให้ครบ 100% เรียบร้อยแล้ว', 'success');
    });

    refreshModalChancesUI();

    // Confirm & Save
    openEntryModal(() => {
      const finalId = elements.entryModalBody.querySelector('#m_cat_id_input').value.trim().toLowerCase().replace(/\s+/g, '_');
      if (!finalId) {
        showToast('กรุณาระบุรหัสหมวดหมู่ (Category ID)', 'error');
        return false;
      }

      if (!isEditing && state.parsedData.categories && state.parsedData.categories[finalId]) {
        showToast(`รหัสหมวดหมู่ "${finalId}" มีอยู่ในระบบแล้ว`, 'error');
        return false;
      }

      if (!state.parsedData.categories) state.parsedData.categories = {};

      const balancedChances = balanceChancesTo100({
        F: parseFloat(inF.value) || 0,
        D: parseFloat(inD.value) || 0,
        C: parseFloat(inC.value) || 0,
        B: parseFloat(inB.value) || 0,
        A: parseFloat(inA.value) || 0,
        S: parseFloat(inS.value) || 0
      });

      const updatedCat = {
        display_name: nameInput.value.trim() || finalId,
        name: finalId,
        color: curColor || '#10B981',
        min_rolls: parseInt(elements.entryModalBody.querySelector('#m_cat_min_rolls').value, 10) || 1,
        max_rolls: parseInt(elements.entryModalBody.querySelector('#m_cat_max_rolls').value, 10) || 4,
        search_time_min: parseInt(elements.entryModalBody.querySelector('#m_cat_search_min').value, 10) || 2,
        search_time_max: parseInt(elements.entryModalBody.querySelector('#m_cat_search_max').value, 10) || 4,
        covered_material: covInput.value.trim().toUpperCase() || 'BLACK_STAINED_GLASS_PANE',
        searching_material: srcInput.value.trim().toUpperCase() || 'STONE',
        search_sound: elements.entryModalBody.querySelector('#m_cat_snd_search').value.trim() || 'BLOCK_STONE_HIT',
        reveal_sound: elements.entryModalBody.querySelector('#m_cat_snd_reveal').value.trim() || 'ENTITY_ITEM_PICKUP',
        rarity_chances: balancedChances
      };

      state.parsedData.categories[finalId] = updatedCat;

      // Update category in cachedCategoryList
      const cachedIdx = cachedCategoryList.findIndex(c => c.id === finalId);
      const cleanTitle = updatedCat.display_name.replace(/<[^>]+>/g, '').trim() || finalId;
      if (cachedIdx >= 0) {
        cachedCategoryList[cachedIdx] = { id: finalId, displayName: cleanTitle, color: updatedCat.color };
      } else {
        cachedCategoryList.push({ id: finalId, displayName: cleanTitle, color: updatedCat.color });
      }

      state.rawYaml = stringifyYaml(state.parsedData);
      elements.rawYamlTextarea.value = state.rawYaml;
      setDirty(true);
      renderCategoriesRegistryView();
      showToast(`${isEditing ? `อัปเดตหมวดหมู่ "${finalId}"` : `สร้างหมวดหมู่ "${finalId}"`} เรียบร้อยแล้ว (กดบันทึกเพื่อนำไปใช้งาน)`, 'success');
      return true;
    }, isEditing ? 'บันทึกการแก้ไขกล่อง' : 'สร้างหมวดหมู่ใหม่');
  }

  /**
   * Comprehensive Multi-Tab Modal Editor for Safe Cases (safe_cases.yml)
   * Supports both Creating (caseId = null) and Editing (caseId = string)
   */
  function openSafeCaseEditorModal(caseId = null, preset = null, defaultId = '') {
    const isEditing = Boolean(caseId && state.parsedData.safe_cases && state.parsedData.safe_cases[caseId]);
    const caseData = isEditing ? state.parsedData.safe_cases[caseId] : (preset || {});
    const targetId = isEditing ? caseId : defaultId;

    // Setup Header Title
    elements.entryModalTitle.innerHTML = `
      <div class="flex items-center gap-2.5">
        <span class="p-2 rounded-xl ${isEditing ? 'bg-sky-600/20 text-sky-400' : 'bg-sky-600/20 text-sky-400'}">
          ${isEditing ? SVG_ICONS.edit : SVG_ICONS.plus}
        </span>
        <div>
          <div class="text-sm font-bold text-white">${isEditing ? `แก้ไขกระเป๋านิรภัย: ${escapeHtml(caseId)}` : (preset ? 'คัดลอกและสร้างกระเป๋านิรภัย' : 'สร้างกระเป๋านิรภัยใหม่')}</div>
          <div class="text-[11px] text-zinc-400">กำหนดไอคอน ความจุช่องเก็บของ และสิทธิประโยชน์เมื่อเสียชีวิต</div>
        </div>
      </div>
    `;

    // Setup Categorized Tabs
    setupModalTabs([
      { id: 'tab-case-info', label: 'ข้อมูลกระเป๋าและไอคอน', icon: SVG_ICONS.safe_cases_settings },
      { id: 'tab-case-capacity', label: 'ความจุและสิทธิประโยชน์', icon: SVG_ICONS.box }
    ], 'tab-case-info');

    const curName = caseData.display_name || caseData.title || '';
    const curMaterial = (caseData.material || 'BUNDLE').toUpperCase().trim();
    let curCapacity = parseInt(caseData.capacity, 10) || 9;
    const curCmd = parseInt(caseData.custom_model_data, 10) || 0;
    const curKeeps = caseData.keeps_on_death !== false;

    // Render Modal Body with Tab Contents
    elements.entryModalBody.innerHTML = `
      <div class="space-y-4">
        <!-- Live Preview Banner -->
        <div class="p-3 bg-zinc-950/90 border border-zinc-800 rounded-xl flex items-center justify-between gap-3 shadow-inner">
          <div class="flex items-center gap-3 min-w-0">
            <button type="button" id="m_case_slot_picker_btn" class="w-12 h-12 rounded-xl border border-zinc-700/80 item-slot-frame flex items-center justify-center p-2 flex-shrink-0 cursor-pointer group hover:border-sky-500 transition shadow-inner" title="คลิกเพื่อเลือกไอเทม">
              <img id="m_case_preview_icon" src="${getMinecraftItemTexture(curMaterial)}" class="w-8 h-8 mc-icon object-contain group-hover:scale-110 transition-transform" alt="${escapeHtml(curMaterial)}" onerror="this.onerror=null; this.src='https://assets.mcasset.cloud/1.21/assets/minecraft/textures/item/bundle.png';">
            </button>
            <div class="min-w-0">
              <div id="m_case_preview_name" class="text-sm font-bold text-white truncate">
                ${formatMiniMessagePreview(curName || curMaterial)}
              </div>
              <div class="flex items-center gap-2 mt-0.5">
                <span id="m_case_preview_mat" class="font-mono text-[11px] text-zinc-400 truncate">${escapeHtml(curMaterial)}</span>
                <span id="m_case_preview_cap" class="text-[10px] font-bold px-2 py-0.5 rounded bg-sky-950/80 text-sky-400 border border-sky-800/60">${curCapacity} ช่อง</span>
              </div>
            </div>
          </div>
          <button type="button" id="m_case_pick_mat_btn" class="px-3 py-1.5 rounded-lg bg-sky-600/20 hover:bg-sky-600/30 text-sky-300 hover:text-white text-xs font-semibold flex items-center gap-1.5 border border-sky-500/30 transition active:scale-95 cursor-pointer flex-shrink-0">
            ${SVG_ICONS.box}
            <span>เลือกไอเทม</span>
          </button>
        </div>

        <!-- TAB 1: Case Info & Icon -->
        <div id="tab-case-info" class="modal-tab-content space-y-4 active">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">รหัสกระเป๋า (Case ID):</label>
              <input type="text" id="m_case_id_input" value="${escapeHtml(targetId)}" placeholder="เช่น alpha_case, gamma_case" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-sky-500 transition ${isEditing ? 'opacity-60 cursor-not-allowed bg-zinc-900' : ''}" ${isEditing ? 'disabled' : ''}>
              <span class="text-[10px] text-zinc-500 mt-1 block">${isEditing ? 'รหัสกระเป๋าคงที่ ไม่สามารถแก้ไขได้' : 'ตัวพิมพ์เล็ก ห้ามเว้นวรรค (ใช้ _ แทน)'}</span>
            </div>
            <div>
              <label class="text-xs font-semibold text-zinc-300 block mb-1">ไอเทมในเกม (Material):</label>
              <div class="flex items-center gap-1.5">
                <input type="text" id="m_case_mat_input" value="${escapeHtml(curMaterial)}" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-sky-500 transition">
                <button type="button" id="m_case_mat_sub_btn" class="px-2.5 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium border border-zinc-700 transition cursor-pointer flex-shrink-0">
                  เลือก
                </button>
              </div>
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-1.5 flex-wrap gap-1">
              <label class="text-xs font-semibold text-zinc-300">ชื่อกระเป๋า (Display Name / Title - รองรับสีทุกรูปแบบ):</label>
              <!-- Quick Color Swatches Bar -->
              <div class="flex items-center gap-1.5 p-1 bg-zinc-950/80 rounded-lg border border-zinc-800" id="m_case_quick_colors" title="คลิกเพื่อใส่แท็กสีด่วนตามระดับความหายาก">
                <span class="text-[10px] text-zinc-500 font-medium px-1">สีด่วน:</span>
                ${RARITY_COLOR_LIST.map(rc => `
                  <button type="button" data-hex="${rc.hex}" class="quick-color-btn" style="background-color: ${rc.hex};" title="ใส่สี ${rc.name} (${rc.hex})"></button>
                `).join('')}
              </div>
            </div>
            <input type="text" id="m_case_name_input" value="${escapeHtml(curName)}" placeholder="เช่น <gradient:#FFAA00:#FF5500><bold>Gamma Safe Case</bold></gradient> หรือ <color:#EF4444>Safe Case</color>" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs text-zinc-100 outline-none focus:border-sky-500 transition font-mono">
            <span class="text-[10px] text-zinc-500 mt-1 block">รองรับทุกรูปแบบ (&, §, &#HEX, &lt;color:#HEX&gt;, &lt;gradient&gt;) หรือคลิกเลือกสีด่วนด้านบน</span>
          </div>

          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">CustomModelData (CMD):</label>
            <input type="number" id="m_case_cmd_input" value="${curCmd}" placeholder="0" class="w-full px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono text-zinc-100 outline-none focus:border-sky-500 transition">
            <span class="text-[10px] text-zinc-500 mt-1 block">ระบุตัวเลขโมเดล 3D จาก Resource Pack (0 หากใช้ไอเทมมาตรฐาน)</span>
          </div>
        </div>

        <!-- TAB 2: Capacity & Perks -->
        <div id="tab-case-capacity" class="modal-tab-content space-y-4">
          <div>
            <label class="text-xs font-semibold text-zinc-300 block mb-1">ความจุช่อง (Capacity):</label>
            <div class="flex items-center gap-3">
              <input type="number" id="m_case_cap_input" min="1" max="54" value="${curCapacity}" class="w-32 px-3 py-2 bg-zinc-950 border border-zinc-700/80 rounded-lg text-xs font-mono font-bold text-zinc-100 outline-none focus:border-sky-500 transition">
              <span class="text-xs text-zinc-400">ช่อง (สูงสุด 54 ช่อง)</span>
            </div>
          </div>

          <!-- Quick Capacity Presets -->
          <div>
            <span class="text-[11px] font-semibold text-zinc-400 block mb-1.5">ขนาดกระเป๋ายอดนิยม (Quick Presets):</span>
            <div class="flex flex-wrap items-center gap-1.5">
              ${[
                { label: '4 ช่อง (2x2 Alpha)', val: 4 },
                { label: '9 ช่อง (3x3 Gamma)', val: 9 },
                { label: '12 ช่อง (2x6 Kappa)', val: 12 },
                { label: '16 ช่อง (4x4 Theta)', val: 16 },
                { label: '27 ช่อง (3x9 Omega)', val: 27 },
                { label: '36 ช่อง (4x9 Titan)', val: 36 },
                { label: '54 ช่อง (6x9 Vault)', val: 54 }
              ].map(p => `
                <button type="button" data-cap="${p.val}" class="m-case-preset-btn px-2.5 py-1.5 rounded-lg text-xs font-mono font-medium bg-zinc-950 hover:bg-zinc-800 text-zinc-300 hover:text-white border border-zinc-800 hover:border-zinc-700 transition cursor-pointer active:scale-95">
                  ${p.label}
                </button>
              `).join('')}
            </div>
          </div>

          <!-- Interactive Visual Slot Grid -->
          <div class="p-3.5 bg-zinc-950 rounded-xl border border-zinc-800/80 space-y-2">
            <div class="flex items-center justify-between text-xs text-zinc-400">
              <span class="font-semibold">ภาพจำลองช่องเก็บของในเกม:</span>
              <span id="m_case_slot_count_badge" class="font-mono text-sky-400 text-[11px] font-bold">${curCapacity} ช่อง</span>
            </div>
            <div id="m_case_visual_grid" class="grid grid-cols-9 gap-1.5 p-2 bg-zinc-900/80 rounded-lg border border-zinc-800/80 justify-center">
              <!-- Rendered via JS -->
            </div>
          </div>

          <!-- Keeps on Death Checkbox Switch -->
          <div class="p-3 bg-zinc-950 rounded-xl border border-zinc-800/80 flex items-center justify-between gap-3">
            <div>
              <div class="text-xs font-semibold text-white">ของไม่ตกเมื่อเสียชีวิต (Keeps on Death)</div>
              <div class="text-[11px] text-zinc-400">เมื่อผู้เล่นตาย ไอเทมในกระเป๋านี้จะไม่สูญหายหรือตกที่พื้น</div>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" id="m_case_keeps_input" class="sr-only peer" ${curKeeps ? 'checked' : ''}>
              <div class="w-11 h-6 bg-zinc-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-zinc-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-sky-600"></div>
            </label>
          </div>
        </div>
      </div>
    `;

    // Live Title & Material Listeners
    const nameInput = elements.entryModalBody.querySelector('#m_case_name_input');
    const previewName = elements.entryModalBody.querySelector('#m_case_preview_name');
    nameInput.addEventListener('input', () => {
      if (previewName) {
        previewName.innerHTML = formatMiniMessagePreview(nameInput.value.trim() || 'New Safe Case');
      }
    });

    // Quick Color Swatches Listener for Safe Case
    elements.entryModalBody.querySelectorAll('#m_case_quick_colors .quick-color-btn').forEach(qcBtn => {
      qcBtn.addEventListener('click', () => {
        const hex = qcBtn.getAttribute('data-hex');
        if (hex) {
          nameInput.value = applyColorToDisplayName(nameInput.value, hex);
          if (previewName) {
            previewName.innerHTML = formatMiniMessagePreview(nameInput.value.trim() || 'New Safe Case');
          }
        }
      });
    });

    const matInput = elements.entryModalBody.querySelector('#m_case_mat_input');
    const previewMat = elements.entryModalBody.querySelector('#m_case_preview_mat');
    const previewIcon = elements.entryModalBody.querySelector('#m_case_preview_icon');

    const updateCaseMaterial = (m) => {
      m = m.toUpperCase().trim();
      matInput.value = m;
      if (previewMat) previewMat.textContent = m;
      if (previewIcon) {
        previewIcon.src = getMinecraftItemTexture(m);
        previewIcon.alt = m;
      }
    };

    const triggerCasePicker = () => {
      openMaterialPicker({
        currentMaterial: matInput.value || 'BUNDLE',
        title: 'เลือกไอเทมสำหรับกระเป๋านิรภัย',
        onSelect: (selectedMat) => updateCaseMaterial(selectedMat)
      });
    };

    elements.entryModalBody.querySelector('#m_case_slot_picker_btn').addEventListener('click', triggerCasePicker);
    elements.entryModalBody.querySelector('#m_case_pick_mat_btn').addEventListener('click', triggerCasePicker);
    elements.entryModalBody.querySelector('#m_case_mat_sub_btn').addEventListener('click', triggerCasePicker);
    matInput.addEventListener('input', (e) => updateCaseMaterial(e.target.value));

    // Dynamic Visual Grid
    const capInput = elements.entryModalBody.querySelector('#m_case_cap_input');
    const visualGrid = elements.entryModalBody.querySelector('#m_case_visual_grid');
    const previewCapBadge = elements.entryModalBody.querySelector('#m_case_preview_cap');
    const countBadge = elements.entryModalBody.querySelector('#m_case_slot_count_badge');

    const refreshVisualGrid = () => {
      const cap = Math.min(Math.max(parseInt(capInput.value, 10) || 1, 1), 54);
      if (previewCapBadge) previewCapBadge.textContent = `${cap} ช่อง`;
      if (countBadge) countBadge.textContent = `${cap} ช่อง`;

      const maxSlotsToShow = Math.min(Math.max(cap, 9), 27);
      let slotsHtml = '';
      for (let i = 0; i < maxSlotsToShow; i++) {
        const isActive = i < cap;
        slotsHtml += `<div class="slot-cell ${isActive ? 'active-slot' : 'opacity-20'}" title="ช่องที่ ${i + 1}${isActive ? ' (ใช้งานได้)' : ''}"></div>`;
      }
      visualGrid.innerHTML = slotsHtml;
    };

    capInput.addEventListener('input', refreshVisualGrid);

    elements.entryModalBody.querySelectorAll('.m-case-preset-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        capInput.value = btn.getAttribute('data-cap');
        refreshVisualGrid();
      });
    });

    refreshVisualGrid();

    // Confirm & Save
    openEntryModal(() => {
      const finalId = elements.entryModalBody.querySelector('#m_case_id_input').value.trim().toLowerCase().replace(/\s+/g, '_');
      if (!finalId) {
        showToast('กรุณาระบุรหัสกระเป๋า (Case ID)', 'error');
        return false;
      }

      if (!isEditing && state.parsedData.safe_cases && state.parsedData.safe_cases[finalId]) {
        showToast(`รหัสกระเป๋า "${finalId}" มีอยู่ในระบบแล้ว`, 'error');
        return false;
      }

      if (!state.parsedData.safe_cases) state.parsedData.safe_cases = {};

      const updatedCase = {
        display_name: nameInput.value.trim() || finalId,
        title: nameInput.value.trim() || finalId,
        material: matInput.value.trim().toUpperCase() || 'BUNDLE',
        capacity: parseInt(capInput.value, 10) || 9,
        custom_model_data: parseInt(elements.entryModalBody.querySelector('#m_case_cmd_input').value, 10) || 0,
        keeps_on_death: elements.entryModalBody.querySelector('#m_case_keeps_input').checked
      };

      state.parsedData.safe_cases[finalId] = updatedCase;
      state.rawYaml = stringifyYaml(state.parsedData);
      elements.rawYamlTextarea.value = state.rawYaml;
      setDirty(true);
      renderSafeCasesRegistryView();
      showToast(`${isEditing ? `อัปเดตกระเป๋า "${finalId}"` : `สร้างกระเป๋า "${finalId}"`} เรียบร้อยแล้ว (กดบันทึกเพื่อนำไปใช้งาน)`, 'success');
      return true;
    }, isEditing ? 'บันทึกการแก้ไขกระเป๋า' : 'สร้างกระเป๋านิรภัยใหม่');
  }

  // Backward compatibility shims for add buttons/clones
  function openAddItemModal(preset = null, defaultId = '') {
    return openItemEditorModal(null, preset, defaultId);
  }

  function openAddCategoryModal(preset = null, defaultId = '') {
    return openCategoryEditorModal(null, preset, defaultId);
  }

  function openAddSafeCaseModal(preset = null, defaultId = '') {
    return openSafeCaseEditorModal(null, preset, defaultId);
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

  function openEntryModal(onConfirm, confirmBtnText = 'เพิ่มรายการ') {
    activeModalConfirmCallback = onConfirm;
    if (elements.confirmEntryModalBtn) {
      elements.confirmEntryModalBtn.textContent = confirmBtnText;
    }
    elements.entryModal.classList.remove('hidden');
    elements.entryModal.classList.add('flex');
    setTimeout(() => {
      const firstInput = elements.entryModalBody.querySelector('input:not([disabled])');
      if (firstInput) firstInput.focus();
    }, 50);
  }

  function closeEntryModal() {
    elements.entryModal.classList.add('hidden');
    elements.entryModal.classList.remove('flex');
    if (elements.entryModalTabs) {
      elements.entryModalTabs.innerHTML = '';
      elements.entryModalTabs.classList.add('hidden');
    }
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

  // --- Mobile Sidebar Controls ---
  function openMobileSidebar() {
    if (!elements.sidebarNav) return;
    elements.sidebarNav.classList.remove('-translate-x-full');
    if (elements.sidebarBackdrop) {
      elements.sidebarBackdrop.classList.remove('hidden');
    }
  }

  function closeMobileSidebar() {
    if (!elements.sidebarNav) return;
    elements.sidebarNav.classList.add('-translate-x-full');
    if (elements.sidebarBackdrop) {
      elements.sidebarBackdrop.classList.add('hidden');
    }
  }

  // --- Event Listeners Initialization ---

  function setupEventListeners() {
    // Mobile Drawer Controls
    if (elements.mobileMenuBtn) {
      elements.mobileMenuBtn.addEventListener('click', openMobileSidebar);
    }
    if (elements.closeSidebarBtn) {
      elements.closeSidebarBtn.addEventListener('click', closeMobileSidebar);
    }
    if (elements.sidebarBackdrop) {
      elements.sidebarBackdrop.addEventListener('click', closeMobileSidebar);
    }

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
      if (e.key === 'Escape') {
        if (elements.sidebarNav && !elements.sidebarNav.classList.contains('-translate-x-full') && window.innerWidth < 1024) {
          closeMobileSidebar();
        }
      }
    });

    // Sidebar navigation click
    document.querySelector('aside').addEventListener('click', (e) => {
      const navItem = e.target.closest('.nav-item');
      if (!navItem) return;

      e.preventDefault();

      if (window.innerWidth < 1024) {
        closeMobileSidebar();
      }

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
