# SplitApp — Proje Bağlamı

> Son güncelleme: 3. Aşama başlangıcı (branch: `spring-boot-migration`)
>
> Bu dosya projeye yeni bakan bir geliştiricinin sistemi hızlıca anlaması için yazıldı.
> Sadece **kodda gerçekten var olan** şeyler "tamamlandı" olarak işaretlenir.

## 1. Proje nedir?

SplitApp, Splitwise benzeri bir harcama paylaşma uygulamasıdır: arkadaş grupları ortak harcamaları
kaydeder, uygulama kimin kime ne kadar borçlu olduğunu hesaplar.

Proje iki parçadan oluşacak:

| Parça | Teknoloji | Durum |
|---|---|---|
| Backend (REST API) | Kotlin + Spring Boot | Geliştiriliyor |
| Android uygulaması | Kotlin (Jetpack Compose planlanıyor) | Henüz başlanmadı |

Projenin ikinci amacı, geliştiricinin (Ceyda) backend geliştirmeyi öğrenmesi ve junior backend
pozisyonlarına hazırlanmasıdır. Öğrenme notları `LEARNING_NOTES.md` dosyasındadır.

## 2. Proje geçmişi

1. Backend ilk olarak **Ktor** ile yazıldı (kayıt, giriş, `/me`, JWT, bcrypt, Flyway).
   Bu sürüm `ktor-final` git etiketinde ve `main` branch'inde saklanıyor. **Ktor ile geliştirme yapılmayacak**,
   sadece eski sürüm olarak duruyor.
2. Backend, sektörde daha yaygın olduğu için **Spring Boot**'a taşınıyor. Geçiş `spring-boot-migration`
   branch'inde aşama aşama yapılıyor. Temel endpoint'ler (kayıt, giriş, `/me`) çalıştığında bu branch
   pull request ile `main`'e birleştirilecek.

GitHub: https://github.com/ceydagezer/splitapp

## 3. Teknolojiler ve sürümler

| Teknoloji | Sürüm | Ne için |
|---|---|---|
| Kotlin | 2.3.21 | Programlama dili |
| Java (JDK) | 21 | Kotlin kodu JVM üzerinde çalışır |
| Spring Boot | 4.1.1 | Uygulama çatısı (framework) |
| Spring Framework | 7.0.9 | Spring Boot'un altındaki çekirdek |
| Spring Web MVC + Tomcat | 7.0.9 / 11.0.24 | HTTP sunucusu ve `@RestController` |
| Jackson (+ Kotlin modülü) | 3.1.5 | JSON ↔ Kotlin nesnesi dönüşümü |
| Spring Data JPA | 4.1.1 | Repository arayüzlerinden otomatik sorgu üretimi |
| Hibernate | 7.4.5 | JPA uygulaması (ORM) |
| Flyway | 12.4.0 | Veritabanı migration'ları |
| HikariCP | 7.0.2 | Veritabanı bağlantı havuzu |
| PostgreSQL | 17 (Docker) | Veritabanı |
| PostgreSQL JDBC sürücüsü | 42.7.13 | Java ↔ PostgreSQL bağlantısı |
| Gradle (Kotlin DSL) | 9.3.1 | Derleme aracı |
| JUnit | 6.0.3 | Testler |

Sürümlerin çoğu elle yazılmaz; `io.spring.dependency-management` plugin'i Spring Boot 4.1.1 ile birlikte
test edilmiş sürümleri seçer. Elle yazılanlar sadece Kotlin plugin'leri ve Spring Boot plugin'idir
(`backend/build.gradle.kts`).

**Neden Spring Boot 4.1.1?** Spring Boot 3.5'in ücretsiz (OSS) desteği 30 Haziran 2026'da bitti.
4.1.x'in ücretsiz desteği 31 Temmuz 2027'ye kadar sürüyor. Yeni bir proje için desteklenen sürüm seçildi.

**Planlanan ama henüz eklenmeyen:** Spring Security + OAuth2 Resource Server (JWT için), Bean Validation.

## 4. Klasör yapısı

```
SplitApp/
├── PROJECT_CONTEXT.md        ← bu dosya
├── LEARNING_NOTES.md         ← öğrenme notları
├── README.md                 ← GitHub'da görünen tanıtım (İngilizce): çalıştırma, testler, API durumu
├── docker-compose.yml        ← sadece PostgreSQL'i çalıştırır
├── .gitignore / .gitattributes
└── backend/
    ├── build.gradle.kts      ← bağımlılıklar ve plugin'ler
    ├── settings.gradle.kts   ← proje adı ve Maven Central deposu
    └── src/
        ├── main/
        │   ├── kotlin/com/splitapp/
        │   │   ├── SplitAppApplication.kt   ← giriş noktası (@SpringBootApplication)
        │   │   ├── common/
        │   │   │   └── ErrorResponse.kt     ← hata cevabı DTO'su {"error": "..."}
        │   │   ├── health/HealthController.kt
        │   │   └── user/
        │   │       ├── User.kt              ← @Entity (users tablosu)
        │   │       └── UserRepository.kt    ← JpaRepository
        │   └── resources/
        │       ├── application.yml          ← ayarlar
        │       ├── logback.xml              ← log formatı
        │       └── db/migration/V1__create_users.sql
        └── test/
            ├── kotlin/com/splitapp/
            │   ├── SplitAppApplicationTests.kt
            │   ├── health/HealthControllerTest.kt
            │   └── user/UserRepositoryTest.kt
            └── resources/application-test.yml   ← testler splitapp_test DB'sini kullanır
```

### Paketleme kararı: özelliğe göre paketler
Kod `controller/`, `service/` gibi katman klasörlerine değil, **özelliklere** (`user`, `auth`, ileride
`group`, `expense`) ayrılır. Her özellik paketinin içinde kendi Controller, Service, Repository, Entity ve
DTO'ları bulunur. Böylece bir özellikle ilgili her şey tek yerde durur ve proje büyüdükçe gezinmek kolaylaşır.

## 5. Mimari

Katmanlı mimari kullanılıyor. Bir isteğin yolu:

```
HTTP isteği
  → [Security filtresi]   JWT kontrolü                (4. Aşama'da eklenecek)
  → Controller            HTTP'yi karşılar, JSON ↔ DTO
  → Service               iş kuralları                (5. Aşama'da eklenecek)
  → Repository            veritabanı sorguları
  → Entity ↔ PostgreSQL
```

| Katman | Sorumluluk | Şu an var olanlar |
|---|---|---|
| Controller | HTTP isteği/cevabı | `HealthController` |
| Service | İş mantığı, transaction | — |
| Repository | Veritabanı erişimi | `UserRepository` |
| Entity | Tablo satırının Kotlin karşılığı | `User` |
| DTO | API'ye giren/çıkan veri şekli | `ErrorResponse` (henüz hiçbir yerde kullanılmıyor; `GlobalExceptionHandler` ile bağlanacak) |

Kurallar:
- Entity'ler **asla** doğrudan JSON olarak dışarı gönderilmez (örn. `User` şifre hash'i içerir). Dışarıya DTO gider.
- Controller veritabanıyla doğrudan konuşmaz; Service üzerinden gider.
- Bağımlılıklar constructor üzerinden enjekte edilir (constructor injection).

## 6. Veritabanı

- PostgreSQL 17, `docker compose up -d` ile Docker'da çalışır. Veri `splitapp_pgdata` volume'ünde kalıcıdır.
- İki veritabanı var:
  - `splitapp` → geliştirme (uygulama bunu kullanır)
  - `splitapp_test` → testler (testler bunu kullanır, her test sonunda rollback yapılır)
- Şemayı **Flyway** yönetir (`backend/src/main/resources/db/migration`). Hibernate tabloları değiştirmez,
  sadece entity'lerin tabloyla uyumunu kontrol eder (`spring.jpa.hibernate.ddl-auto: validate`).
- Kural: Uygulanmış bir migration dosyası **asla değiştirilmez**; değişiklik için yeni `V2__...` dosyası eklenir.

### Tablolar

**`users`** (`V1__create_users.sql`)

| Sütun | Tip | Kural |
|---|---|---|
| `id` | BIGINT | PK, `GENERATED ALWAYS AS IDENTITY` (PostgreSQL üretir) |
| `name` | VARCHAR(100) | NOT NULL |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE |
| `password_hash` | VARCHAR(255) | NOT NULL (bcrypt hash'i, düz şifre asla saklanmaz) |
| `created_at` | TIMESTAMPTZ | NOT NULL, varsayılan `now()` |

Planlanan tablolar (henüz yok): gruplar, grup üyeleri, harcamalar, harcama paylaşımları, ödemeler.

## 7. Ayarlar

`backend/src/main/resources/application.yml`. Her değer ortam değişkeniyle değiştirilebilir:

| Ayar | Ortam değişkeni | Varsayılan (sadece yerel geliştirme) |
|---|---|---|
| Port | `PORT` | `8080` |
| DB adresi | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/splitapp` |
| DB kullanıcı | `DATABASE_USER` | `splitapp` |
| DB şifre | `DATABASE_PASSWORD` | `splitapp_dev_password` |

Testler `@ActiveProfiles("test")` ile `application-test.yml` dosyasını da yükler; bu dosya sadece DB adresini
`splitapp_test` olarak değiştirir.

## 8. API durumu

| Metot | Yol | Auth | Spring Boot'ta durum |
|---|---|---|---|
| GET | `/health` | Hayır | ✅ Çalışıyor → `{"status":"ok"}` |
| POST | `/auth/register` | Hayır | ⏳ Henüz yok (Ktor'da vardı, 5. Aşama) |
| POST | `/auth/login` | Hayır | ⏳ Henüz yok (Ktor'da vardı, 5. Aşama) |
| GET | `/me` | Evet (JWT) | ⏳ Henüz yok (Ktor'da vardı, 5. Aşama) |

Ktor sürümündeki API sözleşmesi Spring Boot'ta **aynen korunacak** (yollar, JSON alanları, durum kodları,
hata formatı `{"error": "..."}`), böylece ileride yazılacak Android uygulaması etkilenmez.

## 9. Geçiş planı ve ilerleme

| # | Aşama | Durum |
|---|---|---|
| 0 | Mevcut Ktor kodunu git ile güvenceye alma (commit, `ktor-final` etiketi, branch) | ✅ |
| 1 | Spring Boot iskeleti, `/health`, ilk testler | ✅ |
| 2 | Veritabanı katmanı: JPA, Flyway, `User`, `UserRepository`, repository testleri | ✅ |
| 3 | Hata yönetimi (`{"error": ...}`) ve DTO'lar | 🔄 Devam ediyor — `ErrorResponse` ✅; exception sınıfları, `GlobalExceptionHandler`, testi, `UserResponse` ⏳ |
| 4 | Spring Security + JWT (OAuth2 Resource Server) | ⏳ |
| 5 | Kayıt, giriş, `/me` (Service + Controller + testler) | ⏳ |
| 6 | Temizlik: uçtan uca kontrol, README'nin son hali → `main`'e birleştirme (README Spring Boot'a göre güncellendi ve `logback.xml`'deki Ktor'dan kalan Netty ayarı kaldırıldı) | ⏳ |

Sonrasında: gruplar, harcamalar, bakiye hesaplama, borç kapatma.

## 10. Alınan teknik kararlar

| Karar | Neden |
|---|---|
| Spring Boot 4.1.1 | Ücretsiz destekli güncel sürüm (3.5'in OSS desteği bitti) |
| Kotlin 2.3.21 | start.spring.io'nun Spring Boot 4.1.1 için önerdiği sürüm |
| Flyway şemayı yönetir, Hibernate `validate` | Şema değişiklikleri SQL dosyalarında görünür ve kontrollü olur; Hibernate'in tabloları kendiliğinden değiştirip veri kaybettirme riski olmaz |
| `open-in-view: false` | Veritabanı bağlantısı HTTP cevabı yazılırken açık tutulmaz; sorgular Service katmanında biter |
| Testler için ayrı PostgreSQL veritabanı | H2 gibi bellek içi veritabanları PostgreSQL'den farklı davranabilir; gerçek veritabanıyla test daha güvenilir |
| JWT için OAuth2 Resource Server | Spring'in standart, bakımı yapılan JWT desteği; daha az elle yazılmış güvenlik kodu |
| Özelliğe göre paketleme | Proje büyüdükçe ilgili kodlar bir arada kalır |
| Bağımlılıkları aşama aşama ekleme | Her bağımlılığın neden gerektiği kullanıldığı anda görülür |
| `libs.versions.toml` kaldırıldı | Sürümleri Spring Boot yönettiği için gereksizdi; tek dosyada sade yapı |
| `.gitattributes` ile `gradlew` için LF | Windows'ta satır sonları değişirse `./gradlew` betiği bozulur |

## 11. Çalıştırma

```bash
docker compose up -d                   # PostgreSQL'i başlat (proje kök dizininde)
cd backend
./gradlew build                        # derle + testleri çalıştır
./gradlew bootRun                      # uygulamayı başlat → http://localhost:8080/health
```

Bilgisayar yeniden başlatıldıysa önce Docker Desktop açılmalı; aksi halde veritabanı kullanan testler
`Connection to localhost:5432 refused` hatasıyla kırılır. Testlerin çalışması için Docker'daki PostgreSQL açık olmalı ve `splitapp_test` veritabanı bulunmalı
(bir kez oluşturulur: `docker exec splitapp-db-1 createdb -U splitapp splitapp_test`).
