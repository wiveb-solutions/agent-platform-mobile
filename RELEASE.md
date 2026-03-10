# Processus de Release - Agent Platform Mobile

Ce document décrit le processus complet de release pour l'application mobile Kotlin Multiplatform.

## 📋 Prérequis

### 1. Secrets GitHub

Avant de pouvoir déployer, configurez les secrets suivants dans votre repository GitHub:

#### Pour Android (obligatoire):
```
KEYSTORE_BASE64    = Contenu du fichier keystore.jks encodé en base64
KEYSTORE_PASSWORD  = Mot de passe du keystore
KEY_ALIAS          = Alias de la clé de signature
KEY_PASSWORD       = Mot de passe de la clé
```

#### Pour iOS (optionnel, pour déploiement App Store):
```
APPLE_ID           = Votre identifiant Apple Developer
APPLE_TEAM_ID      = Votre Team ID Apple
APP_STORE_CONNECT_API_KEY_ID
APP_STORE_CONNECT_API_ISSUER_ID
APP_STORE_CONNECT_API_KEY
```

### 2. Création du Keystore Android

```bash
keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
```

Encodage en base64 pour GitHub:
```bash
base64 -i keystore.jks
```

## 🔄 Pipeline CI/CD

### Déclencheurs automatiques

| Événement | Action |
|-----------|--------|
| Push sur `feat/*` ou `fix/*` | Tests + Build Debug APK |
| Push sur `main` ou `master` | Tests + Build Debug + Build Release (APK + AAB) |
| Pull Request vers `main` | Tests + Build Debug APK |

### Workflows disponibles

#### Android
- `.github/workflows/android-build.yml`
  - Tests unitaires
  - Build Debug APK
  - Build Release APK (signé)
  - Build Release AAB (Android App Bundle)

#### iOS
- `.github/workflows/ios-build.yml`
  - Build iOS Simulator (x64)
  - Build iOS Device (arm64)

## 🚀 Processus de Release

### 1. Développement

```bash
# Créer une nouvelle branche feature
git checkout -b feat/AGENT-XX-description

# Développer et tester localement
./gradlew :composeApp:assembleDebug

# Commit et push
git add .
git commit -m "feat: description"
git push origin feat/AGENT-XX-description
```

### 2. Pull Request

1. Créer une PR vers `main`
2. Le CI va automatiquement:
   - Exécuter les tests unitaires
   - Builder le Debug APK
   - Générer les rapports de tests

3. Vérifier que:
   - ✅ Tests passent
   - ✅ Build réussi
   - ✅ Artéfacts disponibles

### 3. Merge sur Main

Une fois la PR mergée sur `main`:

1. Le CI va automatiquement:
   - Exécuter tous les tests
   - Builder le Debug APK
   - Builder le Release APK (signé)
   - Builder le Release AAB

2. Récupérer les artéfacts:
   - Aller dans GitHub → Actions → Dernier run
   - Télécharger `release-apk` ou `release-aab`

### 4. Déploiement Play Store

#### Option A: Déploiement manuel
1. Télécharger l'AAB depuis les artéfacts GitHub
2. Se connecter à [Google Play Console](https://play.google.com/console)
3. Créer une nouvelle version de production
4. Uploader l'AAB
5. Soumettre pour review

#### Option B: Déploiement automatique (Fastlane)
Configurer Fastlane pour le déploiement automatique (voir section Fastlane ci-dessous).

### 5. Déploiement App Store

#### Option A: Déploiement manuel via Xcode
1. Ouvrir le projet iOS dans Xcode
2. Sélectionner le scheme
3. Product → Archive
4. Distribute → App Store Connect
5. Suivre les étapes d'upload

#### Option B: Déploiement automatique (Fastlane)
Configurer Fastlane pour le déploiement automatique (voir section Fastlane ci-dessous).

## 🔧 Fastlane (Optionnel)

### Installation

```bash
# Installer Fastlane
sudo gem install fastlane

# Dans le projet
cd agent-platform-mobile
fastlane init
```

### Configuration Fastlane

Créer `fastlane/README.md` et `fastlane/Appfile`:

```ruby
# fastlane/Appfile
appfile_identifier("com.wiveb.agentplatform")
```

Créer `fastlane/Fastfile`:

```ruby
# fastlane/Fastfile
default_platform(:android)

platform :android do
  desc "Deploy a new version to the Google Play"
  lane :deploy do
    gradle(task: 'clean')
    gradle(task: 'assembleRelease')
    
    supply(
     aab: "composeApp/build/outputs/bundle/release/com.wiveb.agentplatform-release.aab",
      track: 'production'
    )
  end
  
  lane :beta do
    gradle(task: 'clean')
    gradle(task: 'assembleRelease')
    
    supply(
      aab: "composeApp/build/outputs/bundle/release/com.wiveb.agentplatform-release.aab",
      track: 'beta'
    )
  end
end

platform :ios do
  desc "Deploy to App Store"
  lane :deploy do
    match(
      type: "appstore"
    )
    
    gym(
      scheme: "composeApp",
      export_method: "app-store"
    )
    
    upload_to_app_store(
      skip_upload_symbols: true,
      skip_upload_pre_release: true,
      skip_upload_metadata: true
    )
  end
end
```

## 📦 Versioning

### Schéma de version

```
MAJOR.MINOR.PATCH
```

- **MAJOR**: Changements incompatibles
- **MINOR**: Nouvelles fonctionnalités rétrocompatibles
- **PATCH**: Corrections de bugs rétrocompatibles

### Mise à jour de version

Dans `composeApp/build.gradle.kts`:

```kotlin
defaultConfig {
    applicationId = "com.wiveb.agentplatform"
    minSdk = 26
    targetSdk = 35
    versionCode = 2  // Incrémenté à chaque release
    versionName = "1.1.0"  // MAJOR.MINOR.PATCH
}
```

## 🐛 Dépannage

### Tests échouent
```bash
# Vérifier les logs de tests
./gradlew :composeApp:testDebugUnitTest --info

# Voir les rapports HTML
open composeApp/build/reports/tests/testDebugUnitTest/index.html
```

### Build échoue
```bash
# Clean et rebuild
./gradlew clean
./gradlew :composeApp:assembleDebug --info

# Vérifier la configuration Gradle
./gradlew dependencies
```

### Signature échoue
```bash
# Vérifier que les secrets sont configurés
# GitHub → Settings → Secrets and variables → Actions

# Vérifier le keystore localement
keytool -list -v -keystore keystore.jks
```

## 📊 Monitoring

### Vérifier le statut du CI

1. GitHub → Actions
2. Sélectionner le workflow
3. Voir les derniers runs

### Artéfacts disponibles

| Artéfact | Durée de rétention | Contenu |
|----------|-------------------|---------|
| debug-apk | 14 jours | APK debug |
| release-apk | 30 jours | APK release signé |
| release-aab | 30 jours | AAB pour Play Store |
| test-reports | 14 jours | Rapports de tests |
| ios-framework | 14 jours | Framework iOS |

## 📝 Checklist de Release

- [ ] Tests unitaires passent localement
- [ ] Tests CI passent sur la PR
- [ ] Version mise à jour dans `build.gradle.kts`
- [ ] CHANGELOG.md mis à jour
- [ ] PR mergée sur main
- [ ] Build release réussi sur CI
- [ ] Artéfacts téléchargés et testés
- [ ] Déployé sur Play Store / App Store
- [ ] Notification envoyée aux utilisateurs
