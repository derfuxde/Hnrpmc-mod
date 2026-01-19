# Hnrpmc - SimpleClans & Essentials

Ein umfassendes Minecraft-Plugin-System für **NeoForge**, das ein tiefgreifendes Clan-System mit essentiellen Utility-Funktionen wie Teleportation und Claims kombiniert.

## ✨ Features

* 🛡️ **Advanced Clan System:** Erstelle Clans, verwalte Ränge und kommuniziere in internen Chats.
* 🗺️ **Claim System:** Schütze dein Land vor fremden Zugriffen.
* 🏠 **Home & TPA:** Intuitives Teleportations-System für Spieler und Clan-Mitglieder.
* 📊 **Dynamic Placeholders:** Umfangreiche Platzhalter-Unterstützung für Scoreboards und Chats.
* ⚙️ **Bedingte Logik:** Intelligente Berechtigungsabfragen und bedingte Platzhalter.

---

## 🛡️ Clan-System

Verwalte deine Gemeinschaft mit einem robusten Befehlssystem.

### Basis-Befehle
| Befehl | Beschreibung |
| :--- | :--- |
| `/clan` | Öffnet das Hauptmenü oder zeigt die Clan-Info. |
| `/clan create <Tag> <Name>` | Gründet einen neuen Clan. |
| `/clan invite <Spieler>` | Lädt einen Spieler in deinen Clan ein. |
| `/clan resign` | Verlasse deinen aktuellen Clan. |

### Management & Kommunikation
* **Ränge:** Erstelle eigene Ränge mit spezifischen Permissions (`/clan rank`).
* **BB (Bulletin Board):** Hinterlasse Nachrichten für deine Mitglieder (`/clan bb`).
* **Clan-Chat:** Nutze `/clan chat` für private Gespräche oder `/clan ally` für Verbündete.
* **Moderation:** Kicke Mitglieder, lösche Clans oder ändere Tags (für Leader).

---

## 🏠 Home & Teleport
Optimiert für das Zusammenspiel im Clan.

* `/sethome [Name]` / `/home [Name]` - Verwalte deine persönlichen Punkte.
* **Clan-Home:** Setze einen gemeinsamen Treffpunkt für alle Clan-Mitglieder.
* **Regroup:** Leader können Clan-Mitglieder zu sich rufen (Home-Regroup).
* **TPA:** Sende Teleport-Anfragen an Freunde oder Clan-Kollegen.

---

## 📍 Platzhalter (Placeholders)
Das Plugin bietet verschiedene Platzhalter zur Integration in andere Systeme:

* `%clan_name%`: Name des Clans.
* `%clan_tag%`: Das Kürzel des Clans.
* `%clan_rank%`: Der aktuelle Rang des Spielers.
* **Bedingte Platzhalter:** Zeigt Informationen nur an, wenn der Spieler tatsächlich in einem Clan ist (verhindert leere Klammern im Chat).

---

## 🛠️ Installation
1. Lade die neueste `.jar` Datei herunter.
2. Schiebe sie in deinen `mods`-Ordner (NeoForge Server).
3. Starte den Server, um die Konfigurationsdateien in `/world/serverconfig/simpleclans` zu generieren.

---

## 🏗️ Entwicklung & API
Hnrpmc bietet Events für Entwickler an, um das System zu erweitern:
* `TagChangeEvent`: Wird gefeuert, wenn ein Clan-Tag geändert wird.
* `PlayerRankUpdateEvent`: Überwacht Rang-Änderungen.
* `HomeRegroupEvent`: Ideal für Logging oder Begrenzungen.
