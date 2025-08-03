# Terimakasih sudah menggunakan jasa kami, saya selaku author pikrew mengucapkan terimakasih lagi.
# Saya harap plugin ini tidak diperjual belikan tanpa seizin author nya, yaitu saya.
🎮 Dungeon Mechanism GUI Configuration System
📋 Overview
Sistem GUI yang memungkinkan admin untuk mengkonfigurasi plugin DungeonMechanism secara real-time melalui interface yang user-friendly, tanpa perlu edit file config.yml secara manual.
🚀 Commands
Main Command

/dconfig - Membuka menu utama GUI configuration
Aliases: /dungeonconfig, /dcfg
Permission: dungeonmechanism.admin

Sub Commands
/dconfig mainBuka menu utama
/dconfig doorBuka menu Door Room configuration
/dconfig healBuka menu Heal Area configuration
/dconfig trapBuka menu Trap configuration
/dconfig respawnBuka menu Death Respawn configuration
/dconfig reloadReload plugin tanpa restart server
/dconfig helpTampilkan bantuan command

🎯 GUI Features
🏠 Main Menu
Menu utama yang menampilkan overview semua konfigurasi dengan akses cepat ke setiap section:

Door Room Mechanism - Konfigurasi mekanisme pintu dungeon
Heal Area Mechanism - Konfigurasi area penyembuhan
Trap Mechanism - Konfigurasi jebakan
Death Respawn System - Konfigurasi sistem respawn
Save & Reload - Simpan dan reload plugin
View All Config - Lihat semua konfigurasi dalam chat

🚪 Door Room Configuration

Region Name - Nama region WorldGuard
Radius - Radius area yang hilang saat trigger
Trigger Block - Material block yang memicu mekanisme
Restore Delay - Waktu pengembalian dalam detik

❤️ Heal Area Configuration

Heal Block - Material block yang memberikan heal
Heal Amount - Jumlah HP yang di-heal (dalam hearts)

⚡ Trap Configuration

Trap Block - Material block pemicu trap
Trap Region - Region WorldGuard untuk trap
Duration - Lama tidak bisa bergerak (detik)
Particle - Efek partikel visual

💀 Death Respawn Configuration

Dungeon World - Nama world dungeon
Respawn Duration - Waktu delay respawn
Spawn Coordinates - Koordinat X, Y, Z spawn point
Set Current Location - Set spawn ke lokasi player saat ini

🔧 Input System
Chat Input
Ketika mengklik item konfigurasi, player akan diminta input melalui chat:

Ketik value baru yang diinginkan
Ketik cancel untuk membatalkan
Sistem akan memvalidasi input secara otomatis

Input Types

String - Text bebas (region names, world names, etc.)
Integer - Angka bulat (radius, duration, etc.)
Double - Angka decimal (coordinates, heal amount)
Material - Nama material Minecraft (validasi otomatis)

✨ Key Features
🎨 Visual Interface

Colorful UI - Interface dengan warna dan emoji yang menarik
Clear Categories - Setiap section memiliki warna berbeda
Real-time Values - Menampilkan nilai current configuration
Intuitive Navigation - Navigasi yang mudah dengan tombol back

🛡️ Validation & Safety

Input Validation - Validasi otomatis untuk semua tipe data
Material Checking - Validasi material Minecraft yang valid
Permission System - Hanya admin yang bisa akses
Error Handling - Pesan error yang jelas dan informatif

🔄 Real-time Updates

Live Preview - Nilai konfigurasi terupdate langsung di GUI
Hot Reload - Reload plugin tanpa restart server
Instant Feedback - Notifikasi sukses/error langsung

💾 Persistent Storage

Auto Save - Otomatis simpan ke config.yml
Backup Safe - Tidak merusak konfigurasi existing
Format Preservation - Mempertahankan format dan comment

🎮 Usage Example

Buka GUI: /dconfig
Pilih Category: Klik pada "Door Room Mechanism"
Edit Setting: Klik pada "Radius"
Input Value: Ketik 3 di chat
Confirm: Sistem akan konfirmasi perubahan
Save: Klik "Save & Reload" di main menu

🔒 Permissions
Admin Permissions
yamldungeonmechanism.admin:
- Akses penuh ke GUI configuration
- Dapat mengubah semua settings
- Dapat reload plugin

dungeon.reload:
- Dapat reload plugin
- Termasuk dalam dungeonmechanism.admin

User Permissions
yamldungeonmechanism.use:
- Menggunakan fitur basic plugin
- Default untuk semua player

🚀 Benefits
For Administrators

No File Editing - Tidak perlu edit config.yml manual
Real-time Changes - Perubahan langsung tanpa restart
User Friendly - Interface yang mudah dipahami
Error Prevention - Validasi input mencegah error config

For Server Management

Live Configuration - Adjust settings saat server running
Quick Testing - Test konfigurasi berbeda dengan cepat
Backup Safety - Tidak risiko corrupt config file
Remote Management - Bisa diakses in-game dari mana saja

🎯 Advanced Features
Location Setting

Current Location Button - Set spawn ke lokasi player saat ini
Coordinate Display - Menampilkan koordinat current dengan jelas
Multi-world Support - Support untuk multiple world

Validation System

Material Validation - Cek apakah material valid di Minecraft
Range Checking - Validasi range nilai (tidak negatif untuk radius, etc.)
Type Safety - Konversi tipe data yang aman

User Experience

Tab Completion - Auto-complete untuk commands
Help System - Built-in help dan usage information
Intuitive Flow - Alur penggunaan yang natural dan mudah

📝 Configuration Structure
Supported Config Keys
yaml# Door Room Mechanism
region: "dungeon"
radius: 1
trigger-block: "CHISELED_STONE_BRICKS"
restore-delay: 6

# Heal Area Mechanism
heal-block: "LAPIS_BLOCK"
heal-amount: 1.0

# Trap Mechanism
trap:
block: "STONECUTTER"
region: "dg1"
duration: 5
particle: "SNOWFLAKE"

# Death Respawn System
dungeon_world: "DUNGEON"
respawn_duration: 10
dungeon_spawn:
x: 183
y: 4
z: 16
Semua key di atas dapat diubah melalui GUI tanpa perlu edit file manual!