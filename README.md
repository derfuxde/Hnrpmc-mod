# Hnrpmc Mod

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

---

## 🗺️ Claim-System
Sichere deine Basis gegen Griefing.

* **`/claim`**: Das Hauptmenü für Claims.
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
    * `%clan_tag%`, `%clan_name%`
    * Bedingte Platzhalter, die sich automatisch ausblenden, wenn man in keinem Clan ist.
* **Events für Entwickler:**
    * `TagChangeEvent`: Reagiere auf Namensänderungen.
    * `HomeRegroupEvent`: Logge oder beeinflusse Clan-Teleportationen.

---

## 🛠️ Installation
1. Lade die `.jar` Datei in den `mods`-Ordner deines NeoForge-Servers.
2. Starte den Server einmal, um die Configs zu generieren.
3. Konfiguriere die Berechtigungen in deinem Permission-Plugin (z.B. LuckPerms).

---

## 📝 Lizenz
Dieses Projekt ist unter der [MIT Lizenz](LICENSE) lizenziert.
