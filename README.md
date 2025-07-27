# 📦 DatabaseAPI

## 🎯 Description

**DatabaseAPI** est un plugin Spigot pour Paper 1.21.4 qui fournit une interface centralisée pour accéder à une base de données SQLite. Il permet aux autres plugins de réutiliser la même connexion SQLite sans dupliquer la logique de gestion de base de données.

---

## 🗂️ Structure du plugin

| Fichier | Description |
|--------|-------------|
| `main.java` | Classe principale du plugin. Initialise la base de données et offre des méthodes d'accès globales. |
| `DatabaseManager.java` | Gère la connexion SQLite, l'initialisation du fichier `database.db`, et les opérations SQL de base. |

---

## 🚀 Installation

1. Compile le plugin et place `DatabaseAPI.jar` dans le dossier `/plugins` de ton serveur.
2. Démarre le serveur. Le fichier SQLite `database.db` sera automatiquement créé dans `/plugins/DatabaseAPI/`.

---

## 🔗 Utilisation dans un autre plugin

### 1. Ajouter la dépendance

Dans le `plugin.yml` de ton plugin :


depend: [DatabaseAPI]

### 2. Accéder à la base de données

@Override
public void onEnable() {
Plugin plugin = Bukkit.getPluginManager().getPlugin("DatabaseAPI");

    if (plugin instanceof main dbPlugin && plugin.isEnabled()) {
        Connection connection = dbPlugin.getDatabaseManager().getConnection();

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS your_table (uuid TEXT PRIMARY KEY, value DOUBLE)");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    } else {
        getLogger().severe("❌ DatabaseAPI introuvable !");
        Bukkit.getPluginManager().disablePlugin(this);
    }
}

---

## ✅ Bonnes pratiques

1. Ne ferme jamais la connexion retournée par getConnection() (elle est gérée automatiquement).

2. Utilise toujours getConnection() depuis l’API au lieu d’en ouvrir une nouvelle.

3. Gère tes propres tables (ne touche pas à celles d’un autre plugin).

```yaml