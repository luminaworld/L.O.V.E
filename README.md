# Luminaris: Operation Vanilla Extraction Core Engine (L.O.V.E)

[![Paper 1.21](https://img.shields.io/badge/Paper-1.21-00d2ff.svg)](https://papermc.io)
[![Folia Supported](https://img.shields.io/badge/Folia-Supported-5a9eec.svg)](https://github.com/PaperMC/Folia)
[![Kotlin 2.3](https://img.shields.io/badge/Kotlin-2.3-7f52ff.svg)](https://kotlinlang.org)
[![Java 25](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net)

แกนหลักระบบเกมเพลย์แนว **Extraction Shooter / Looter Extraction** สำหรับเซิร์ฟเวอร์ Minecraft (Paper / Folia 1.21+) ขับเคลื่อนด้วยภาษา Kotlin ที่ออกแบบมาเพื่อประสิทธิภาพสูงสุด และมีความยืดหยุ่นในการตั้งค่าผ่านระบบ Web Configuration ในตัว

---

## 🌟 ฟีเจอร์หลัก (Key Features)

- ⏳ **ระบบสกัดหลบหนี (Extraction Matrix)**: โซนหลบหนีพร้อมเวลานับถอยหลัง, ตรวจจับดาเมจขัดขวางการหลบหนี, อนุภาคเอฟเฟกต์ และเสียงแจ้งเตือน
- 🗺️ **โลกแมตช์จำลอง (Ephemeral Instance Worlds)**: ระบบโคลนแมพจากโฟลเดอร์ต้นฉบับ (`templates_worlds`) ไปเปิดห้องเล่น และลบทำความสะอาดอัตโนมัติเมื่อจบรอบ
- 📦 **ระบบค้นหากล่องดรอป (Interactive Drop Box System)**:
  - สุ่มไอเทมตามระดับความหายาก (Common, Uncommon, Rare, Epic, Legendary)
  - แอนิเมชันเปิดสำรวจทีละช่องพร้อมระบบจำลองการค้นหาและเสียงประกอบ
  - รองรับชนิดบล็อกคอนเทนเนอร์หลากหลายชนิด
- 💡 **ระบบเรืองแสงและแนวสายตา (Container Glow & Line-of-Sight)**:
  - ขอบเรืองแสงรอบกล่องดรอปตามระยะที่กำหนด
  - ระบบ Raycast LOS ป้องกันการมองเห็นกล่องทะลุกำแพง
- 🎒 **กระเป๋านิรภัย (Safe Cases)**: ช่องเก็บของพิเศษที่ไอเทมไม่สูญหายเมื่อตาย พร้อมระบบป้องกันการซ้อนกระเป๋า (Anti-Nesting)
- 💀 **ระบบกล่องศพ (Player Corpse Containers)**: เก็บของของผู้เล่นลงกล่องเมื่อเสียชีวิต พร้อมระบบขยายกล่องอัตโนมัติหากไอเทมล้นช่อง
- 🌐 **ระบบตั้งค่าผ่านเว็บในตัว (Embedded Web Config Server)**:
  - เข้าจัดการคอนฟิกผ่านเว็บเบราว์เซอร์ได้ทันที
  - เพิ่ม/ลบ/คัดลอก (CRUD) ไอเทม, กล่องดรอป, กระเป๋านิรภัย, และภาษาไทย/อังกฤษ
  - โหมด Dark Theme เรียบหรูด้วย Tailwind CSS + SVG Icons รองรับการใช้งานเต็มหน้าจอ

---

## 📋 ความต้องการของระบบ (Requirements)

- **Java Runtime Environment (JRE)**: เวอร์ชัน **25** หรือใหม่กว่า (แนะนำ Eclipse Temurin หรือ GraalVM)
- **Minecraft Server**: **Paper**, **Purpur**, หรือ **Folia** เวอร์ชัน **1.21+**

---

## 🛠️ วิธีการ Clone และ Build ปลั๊กอิน (Clone & Build Guide)

### 1. ติดตั้ง Git และ Java 25
ตรวจสอบว่าเครื่องของคุณติดตั้ง Git และ Java 25 เรียบร้อยแล้ว:
```bash
git --version
java -version
```

### 2. Clone Repository
ดาวน์โหลดซอร์สโค้ดลงมายังเครื่องของคุณ:
```bash
git clone https://github.com/luminaworld/L.O.V.E.git
cd L.O.V.E
```

### 3. Build ด้วย Gradle Wrapper
ใช้คำสั่ง Gradle Wrapper เพื่อคอมไพล์และสร้างไฟล์ปลั๊กอิน (Shadow JAR):

**บน Linux / macOS:**
```bash
./gradlew shadowJar
```

**บน Windows (Command Prompt / PowerShell):**
```cmd
gradlew.bat shadowJar
```

*(หากเป็นการ Build ครั้งแรก Gradle จะดาวน์โหลดเครื่องมือและ Dependencies ที่จำเป็นให้อัตโนมัติ)*

### 4. นำไฟล์ JAR ไปใช้งาน
เมื่อ Build สำเร็จ ไฟล์ปลั๊กอินจะอยู่ที่:
```text
build/libs/LuminaExtractionCore-1.0.0-SNAPSHOT.jar
```
คัดลอกไฟล์ `.jar` ดังกล่าวไปวางในโฟลเดอร์ `plugins/` ของเซิร์ฟเวอร์ Paper/Folia แล้วเริ่มรันเซิร์ฟเวอร์

---

## ⚙️ การตั้งค่าและการใช้งานระบบ Web Config

หลังจากเปิดเซิร์ฟเวอร์ ปลั๊กอินจะสร้างโฟลเดอร์ `plugins/LuminaExtractionCore/` และเปิด Web Server อัตโนมัติ (สามารถเปิด/ปิดและตั้งพอร์ตได้ใน `config.yml`)

- **URL ค่าเริ่มต้น**: `http://<IP-เครื่องเซิร์ฟเวอร์>:8080/`
- **คำสั่งเปิด/ปิด/รีโหลดเว็บในเกม**:
  ```text
  /ext web status   - ตรวจสอบสถานะและพอร์ตของ Web Config Server
  /ext web reload   - รีสตาร์ทเว็บเซิร์ฟเวอร์และดึง Asset หน้าเว็บเวอร์ชันล่าสุด
  /ext web open     - รับลิงก์และโทเค็นสำหรับเข้าใช้งาน Web UI
  ```

---

## 📜 คำสั่งหลักและสิทธิ์การใช้งาน (Commands & Permissions)

| คำสั่ง | สิทธิ์ (Permission) | คำอธิบาย |
| :--- | :--- | :--- |
| `/ext admin` | `luminaris.admin` | เมนูและการจัดการระบบสำหรับแอดมิน |
| `/ext editor` | `luminaris.editor` | โหมดแก้ไขจุดเกิดและกล่องคอนเทนเนอร์ในแผนที่ |
| `/ext web` | `luminaris.admin` | ควบคุมระบบ Web Configuration Server |
| `/ext reload` | `luminaris.admin` | รีโหลดคอนฟิกทั้งหมดของปลั๊กอิน |

---

## 📄 License & Credits

พัฒนาโดยทีมงาน **LuminarisTeam**  
สงวนลิขสิทธิ์สำหรับโปรเจกต์ Luminaris: Operation Vanilla Extraction Core Engine (L.O.V.E)
