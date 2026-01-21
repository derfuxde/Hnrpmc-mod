# Hnrpmc Mod

${\color{red}Dieser \space Mod \space befindet \space sich \space noch \space in \space der \space Beta \space Version}$

Hnrpmc ist ein leistungsstarkes Minecraft-Mod-System für **NeoForge**, das ein tiefgreifendes Clan-Management mit essenziellen Survival-Features wie Claims, Homes und Teleportation vereint.

---

## ✨ Hauptfeatures

* 🛡️ **Clan-System:** Gründung, Ränge, Bulletin Boards und interne Chats.
* 🗺️ **Claim-System:** Schütze deine Gebiete und verwalte Berechtigungen für Spieler und ganze Clans. 
* 🏠 **Home-System:** Erstelle mehrere Homes, verwalte sie und besuche (bei Erlaubnis) andere Spieler.
* 🔄 **TPA-System:** Intuitives Anfragen-System für Teleportationen (TPA/TPAHere).
* 📊 **Placeholders:** Dynamische Platzhalter für Clan-Tags, Ränge und Statistiken.

---

## 🛡️ Clan-System
Verwalte deine Community effizient.

| Befehl | Beschreibung |
| :--- | :--- |
| `/clan create <Tag> <Name>` | Gründet einen neuen Clan. |
| `/clan invite <Spieler>` | Lädt ein neues Mitglied ein. |
| `/clan rank` | Erstellt und verwaltet Ränge sowie Permissions innerhalb des Clans. |
| `/clan bb <Nachricht>` | Schreibt eine Nachricht an das Clan-Bulletin-Board. |
| `/clan chat` | Wechselt in den Clan-Chat-Kanal. |
| `/clan ally` | Kommuniziere mit befreundeten Clans. |
| `/clan resign` | Verlässt den aktuellen Clan (erfordert Bestätigung). |

Der Clan-Tag wird im chat angezeigt
Standart: [Clan-Tag] <Spielername> Nachricht

---

## 🗺️ Claim-System
Sichere deine Basis gegen Griefing.

* **`/claim`**: Claim einen bereich
* **Berechtigungen**: Nutze `/claim perm <add/remove> <Claim> <Permission>`, um generelle Rechte zu vergeben.
     * *Info:* GUI kommt bald
* Nutze `/claim playerperms <add/remove> <Claim> <Player/Clan> <Permission>`, um gezielt Rechte zu vergeben. 
    * *Besonderheit:* Du kannst Rechte nicht nur an Einzelspieler, sondern direkt an ganze Clans vergeben!
* **Visualisierung:** Claims nutzen Markierungen, um Grenzen im Spiel anzuzeigen.

---

## 🏠 Home & Teleportation
Flexibles Reisen für dich und deine Freunde.

### Homes
- `/sethome <Name>`: Erstellt einen neuen Home-Punkt.
- `/home <Name>`: Teleportiert dich zu deinem Home.
- `/delhome <Name>`: Löscht einen Home-Punkt.
- `/homes [Spieler]`: Listet deine Homes oder (mit Permission) die eines anderen Spielers auf.

### TPA (Teleport Requests)
- `/tpa <Spieler>`: Sende eine Teleport-Anfrage.
- `/tpahere <Spieler>`: Bitte einen Spieler, sich zu dir zu teleportieren.
- `/tpaccept`: Nimmt die letzte Anfrage an.
- `/tpdeny`: Lehnt die Anfrage ab.

---

## ⚙️ Technische Details & Placeholders
Das Plugin ist hochgradig konfigurierbar und bietet Integrationen:

* **Placeholders:**
    | Placeholder | Beschreibung |
    | :--- | :--- |
    | `%playtime%` | Formatierte Spielzeit das Spielers |
    | `%playername%` | Spielername |
    | `%player_allow_flight%` | Ist der Spieler erlaubt zu fliegen |
    | `%player_armor_helmet_name%` | Helm name wenn Spieler einen auf hat |
    | `%player_armor_helmet_durability%` | Helm haltbarkeit wenn Spieler einen auf hat |
    | `%player_armor_chestplate_name%` | Brustplatten name wenn Spieler eine an hat |
    | `%player_armor_chestplate_durability%` | Brustplatten haltbarkeit wenn Spieler eine an hat |
    | `%player_armor_leggings_name%` | Hose name wenn Spieler eine an hat |
    | `%player_armor_leggings_durability%` | Hose haltbarkeit wenn Spieler eine an hat |
    | `%player_armor_boots_name%` |  Schuhe name wenn Spieler welche an hat |
    | `%player_armor_boots_durability%` | Schuh haltbarkeit wenn Spieler eine an hat |
    | `%player_health%` | Spieler leben |
    | `%clan_name%` | Clanname wenn Spieler in einem |
    | `%clan_tag%` | Clantag wenn Spieler in einem |
    | `%clan_color%` | Clan Farbe wenn Spieler in einem |
    | `%clan_rank_name%` | Rang name wenn vorhanden |
    | `%clan_rank_id%` | Rang id wenn vorhanden |
    | `%isinclan%` | ist Spieler in einem Clan gibt `yes` oder `no` |
    | `%clan_members%` | Die Clan Mitglieder Anzahl |
    | `%clan_onlinemembers%` | Die Clan mitglieder Anzahl die online sind |
    | `%server_players%` | Die Anzahl an Spieler auf dem Server |
    | `%player_ping_colored%` | Farbiger Spieler ping |
    | `%player_ping%` | Spieler ping |
    | `%server_maxplayers%` | Die Maximale Anzahl an Spielern auf dem Server |
  
                  **Mehr: bald**
  
    * Plazhalter können mit `/caim placeholder <Spieler> <Nachricht>`
    * Bedingte Platzhalter, die sich automatisch ausblenden, wenn man in keinem Clan ist.
* **Events für Entwickler:**
    * `TagChangeEvent`: Reagiere auf Namensänderungen.
    * `HomeRegroupEvent`: Logge oder beeinflusse Clan-Teleportationen.

---

## 🛠️ Installation
1. Lade die `.jar` Datei in den `mods`-Ordner deines NeoForge-Servers.
2. Starte den Server einmal, um die Configs zu generieren.
3. Konfiguriere die Berechtigungen in deinem Permission-Mod (z.B. [LuckPerms](https://modrinth.com/plugin/luckperms)).
