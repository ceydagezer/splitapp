# SplitApp — Öğrenme Notları

> Bu dosya, SplitApp'i geliştirirken öğrendiğim kavramların notlarıdır.
> Her bölüm: **ne yaptık → neden yaptık → kavram → mülakat notları**.
> Bir kavram bir kez açıklanır; sonraki bölümler ona atıf yapar.

## İçindekiler

- [Bölüm 0 — Başlangıç: IDE sorunu ve projeyi güvenceye alma](#bölüm-0--başlangıç-ide-sorunu-ve-projeyi-güvenceye-alma)
- [Bölüm 1 — Spring Boot iskeleti](#bölüm-1--spring-boot-iskeleti)
- [Bölüm 2 — Veritabanı katmanı: JPA ve Flyway](#bölüm-2--veritabanı-katmanı-jpa-ve-flyway)
- [Bölüm 3 — Hata yönetimi ve DTO'lar](#bölüm-3--hata-yönetimi-ve-dtolar)
- [Mülakat soruları özeti](#mülakat-soruları-özeti)

---

## Bölüm 0 — Başlangıç: IDE sorunu ve projeyi güvenceye alma

### 0.1 "Unsupported binary format" hatası

**Ne oldu?** IntelliJ'de `Application.kt` dosyasında `install`, `routing` gibi her şey kırmızı göründü
("Unresolved reference"). Ama aynı kod terminalde `./gradlew compileKotlin` ile sorunsuz derlendi.

**Neden?** Kotlin derleyicisi her `.class` dosyasına **metadata** (hangi Kotlin sürümüyle derlendiği,
nullable bilgisi vb.) yazar. IDE'nin Kotlin eklentisi, kendisinden çok daha yeni bir Kotlin sürümüyle
derlenmiş kütüphanelerin metadata'sını okuyamaz. IntelliJ 2024.1, Kotlin 2.2 ile derlenmiş kütüphaneleri
okuyamadığı için onları "yok" sandı.

**Ders:** IDE'deki kırmızı çizgi her zaman koddaki hata demek değildir. Gerçek derleyici Gradle'dır.
Şüphede kalınca terminalde `./gradlew build` çalıştırılır.

### 0.2 JAVA_HOME

Gradle'ın hangi JDK ile çalışacağını bulması için `JAVA_HOME` ortam değişkeni kullanılır. Sistemdeki
`java` komutu bozuk bir kısayolu gösteriyordu, bu yüzden `./gradlew` çalışmıyordu. `JAVA_HOME`'u kurulu
JDK 21'e yönlendirmek sorunu çözdü.

### 0.3 Git ile güvenli çalışma

**Ne yaptık?** Projede hiç commit yoktu, yani bir dosya silinse geri getirilemezdi. Spring Boot geçişinden
önce:

```bash
git commit ...                              # Ktor kodunun fotoğrafını çektik
git tag -a ktor-final -m "..."             # bu fotoğrafa kalıcı bir isim verdik
git switch -c spring-boot-migration         # yeni işi ayrı bir dalda yapıyoruz
```

| Kavram | Ne demek | Benzetme |
|---|---|---|
| **Commit** | Projenin o anki halinin kaydı | Oyundaki "kayıt noktası" |
| **Tag** | Bir commit'e verilen kalıcı isim | Kayıt noktasına yapıştırılan etiket |
| **Branch** | Ana koddan ayrılan çalışma kolu | Asıl belgenin kopyası üzerinde çalışmak |
| **Remote (`origin`)** | Kodun uzaktaki kopyası (GitHub) | Bulut yedeği |
| **Push** | Yerel commit'leri GitHub'a gönderme | Yedeği buluta yükleme |

**Neden branch?** `main` her zaman çalışan bir sürümü göstermeli. Yarım kalmış geçiş `main`'i bozmasın
diye iş `spring-boot-migration` dalında yapılıyor; bitince **pull request** ile birleştirilecek.

**Commit geçmişini yeniden yazmak:** Commit mesajlarındaki bir satırı silmek için commit'leri yeniden
oluşturduk. Bu, commit kimliklerini (hash) değiştirir. **Kural:** Geçmiş sadece henüz başkasıyla
paylaşılmamışsa (push edilmemişse) yeniden yazılır. Push edilmiş geçmişi değiştirmek, aynı repoyla
çalışan herkesin geçmişini bozar.

**Commit mesajı alışkanlığı:** İlk satır kısa bir özet (≈50 karakter, emir kipinde: "Add ...", "Fix ..."),
boş satır, sonra madde madde ne ve neden.

**Mülakat notları**
- *merge* ile *rebase* farkı nedir? (merge geçmişi korur ve birleştirme commit'i ekler; rebase commit'leri
  yeni bir tabanın üzerine yeniden yazar.)
- Push edilmiş bir commit'i neden yeniden yazmamalıyız?
- `.gitignore`'a neler girer? (derleme çıktıları `build/`, IDE dosyaları `.idea/`, sırlar `.env`.)

---

## Bölüm 1 — Spring Boot iskeleti

**Ne yaptık?** Ktor bağımlılıklarını kaldırıp yerine Spring Boot koyduk; giriş noktası
(`SplitAppApplication.kt`), ayar dosyası (`application.yml`) ve `GET /health` endpoint'ini yazdık.

### 1.1 Framework ve Spring Boot

**Framework (çatı)**, uygulamanın iskeletini hazır veren ve senin kodunu *kendisi çağıran* yapıdır.
Kütüphaneyi sen çağırırsın; framework seni çağırır. Buna **Inversion of Control (IoC — kontrolün tersine
çevrilmesi)** denir.

**Spring Boot**, Spring Framework'ün üzerine kurulu, ayarları otomatik yapan katmandır:

- **Starter**: Bir özellik için gereken kütüphaneleri tek pakette toplar.
  `spring-boot-starter-webmvc` = Tomcat sunucusu + Spring MVC + Jackson (JSON).
- **Auto-configuration (otomatik yapılandırma)**: Spring Boot classpath'e bakar. "Tomcat var → web sunucusu
  başlat", "PostgreSQL sürücüsü ve `datasource.url` var → bağlantı havuzu kur" gibi kararları kendisi verir.
- **Dependency management**: `io.spring.dependency-management` plugin'i sayesinde bağımlılıkları sürüm
  yazmadan ekleriz; Spring Boot birbiriyle uyumlu sürümleri seçer.

```kotlin
@SpringBootApplication          // = bu paketi ve alt paketlerini tara + otomatik yapılandırmayı aç
class SplitAppApplication

fun main(args: Array<String>) {
    runApplication<SplitAppApplication>(*args)   // Spring'i başlat
}
```

`@SpringBootApplication` sınıfın bulunduğu paketten (`com.splitapp`) **aşağısını** tarar. Bu yüzden bu
sınıf her zaman en üst pakette durur.

### 1.2 Bean, Spring Container ve Dependency Injection

- **Spring Container (ApplicationContext)**: Uygulama açılırken nesneleri oluşturan ve birbirine bağlayan yapı.
- **Bean**: Spring'in oluşturup yönettiği nesne. `@RestController`, `@Service`, `@Repository`,
  `@Component`, `@Configuration` ile işaretlenen sınıflardan otomatik oluşur. Varsayılan olarak her
  bean'den **bir tane** oluşturulur (*singleton*).
- **Dependency Injection (DI)**: Bir sınıf ihtiyacı olan nesneleri kendisi oluşturmaz, constructor'da ister;
  Spring verir.

Ktor sürümünde bağlantıları elle kuruyorduk:

```kotlin
// Elle bağlama (eski Ktor kodu)
val userRepository = UserRepository(dataSource)
authRoutes(userRepository, jwtConfig)
```

Spring'de sadece ihtiyacı beyan ederiz:

```kotlin
@Service
class AuthService(private val userRepository: UserRepository)   // Spring UserRepository bean'ini verir
```

**Neden DI?**
1. Nesneler arası bağlantıyı tek bir yer (Spring) yönetir; 50 sınıflık projede elle bağlamak çok zordur.
2. **Test edilebilirlik**: Testte gerçek repository yerine sahte (mock) bir tane verebiliriz.
3. **Gevşek bağlılık**: Sınıf, bağımlılığının nasıl oluşturulduğunu bilmek zorunda değildir.

**Constructor injection** (constructor'dan isteme) önerilen yöntemdir: bağımlılıklar `val` (değişmez) olur,
eksik bağımlılıkla nesne oluşturulamaz ve testte elle nesne kurmak kolaydır. Alternatif olan field injection
(`@Autowired lateinit var ...`) uygulama kodunda önerilmez.

### 1.3 Controller

Controller, API'nin **ön kapısıdır**: HTTP isteğini alır, cevabı döner. İş kuralı içermez.

```kotlin
@RestController                       // dönen değeri JSON'a çevir
class HealthController {
    @GetMapping("/health")            // GET /health gelince bu fonksiyonu çağır
    fun health() = mapOf("status" to "ok")
}
```

`@RestController` = `@Controller` + `@ResponseBody`. Dönen Kotlin nesnesini **Jackson** JSON'a çevirir.
Diğer eşlemeler: `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@RequestMapping` (sınıf seviyesinde ortak yol).

### 1.4 Kotlin + Spring için gereken iki plugin

- **`kotlin("plugin.spring")`**: Kotlin sınıfları varsayılan olarak `final`'dır (alt sınıfı yazılamaz).
  Spring ise `@Transactional` gibi özellikler için sınıflarımızın çalışma anında **alt sınıfını (proxy)**
  oluşturur. Bu plugin Spring annotation'ı taşıyan sınıfları otomatik `open` yapar.
- **`-Xjsr305=strict`**: Spring'in Java kodundaki `@Nullable` işaretlerini Kotlin'in `String?` / `String`
  sistemine çevirir; null hataları derleme anında yakalanır.

### 1.5 application.yml

Spring Boot'un ayar dosyası. `${PORT:8080}` sözdizimi: "`PORT` ortam değişkeni varsa onu kullan, yoksa 8080".
Aynı kod hem kendi bilgisayarımızda hem sunucuda değiştirilmeden çalışır; sunucuya özel değerler (şifreler)
ortam değişkeniyle verilir. Bu yaklaşım **Twelve-Factor App** prensiplerinden "config"tir: ayarlar koddan ayrı tutulur.

### 1.6 İlk testler

| Annotation | Ne başlatır | Hız | Ne zaman |
|---|---|---|---|
| `@SpringBootTest` | Uygulamanın tamamı | Yavaş | Tüm parçalar birbirine bağlanabiliyor mu? |
| `@WebMvcTest(X::class)` | Sadece web katmanı (controller + JSON) | Hızlı | Controller'ın HTTP davranışı |

**MockMvc**: Gerçek bir port açmadan controller'a sahte HTTP isteği gönderir.

```kotlin
mockMvc.get("/health").andExpect {
    status { isOk() }                          // HTTP 200
    jsonPath("$.status") { value("ok") }       // JSON'daki "status" alanı "ok"
}
```

`jsonPath("$.status")`: `$` JSON'un kökü, `.status` içindeki alan.

**Mülakat notları**
- Spring ile Spring Boot arasındaki fark nedir? (Boot = otomatik yapılandırma + starter'lar + gömülü sunucu.)
- IoC ve DI nedir, farkları nelerdir? (IoC genel prensip, DI onu uygulama yöntemi.)
- Bean scope'ları nelerdir? (singleton varsayılan; prototype, request, session.)
- Neden constructor injection, field injection'a tercih edilir?
- `@Component`, `@Service`, `@Repository`, `@Controller` farkı? (Hepsi bean yapar; isimleri katmanı belirtir.
  `@Repository` ayrıca veritabanı hatalarını Spring'in `DataAccessException` türlerine çevirir.)

---

## Bölüm 2 — Veritabanı katmanı: JPA ve Flyway

**Ne yaptık?** Uygulamayı Docker'daki PostgreSQL'e bağladık, `users` tablosunun Kotlin karşılığı olan `User`
entity'sini ve `UserRepository`'yi yazdık, testler için ayrı `splitapp_test` veritabanı açtık.

### 2.1 Problem: nesne ↔ tablo çevirisi

Kodda nesneler, veritabanında tablolar var. Ktor sürümünde çeviriyi elle yapıyorduk: SQL yaz, `?`
parametrelerini doldur, sonuç satırlarını sütun sütun oku. Her tablo ve sorgu için aynı tekrar eden kod.

### 2.2 JDBC, JPA, Hibernate, Spring Data JPA — kim kimdir?

```
Spring Data JPA   → Repository interface'inden sorgu kodunu üretir      (en üst, en az kod)
      ↓ kullanır
JPA (Jakarta)     → "Nesneler tablolara nasıl eşlenir" kural kitabı (@Entity, @Id...)
      ↓ uygular
Hibernate         → Kuralları uygulayan kütüphane, SQL'i üretir
      ↓ kullanır
JDBC              → Java'nın veritabanına SQL gönderme standardı (Ktor'da doğrudan bunu kullanıyorduk)
      ↓
PostgreSQL sürücüsü → PostgreSQL ile konuşan JDBC uygulaması
```

Bu nesne-tablo eşleme yaklaşımına **ORM (Object-Relational Mapping)** denir.

### 2.3 Entity

Entity, bir tablonun Kotlin karşılığıdır: **bir nesne = bir satır**.

```kotlin
@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, length = 100) var name: String,
    @Column(nullable = false, unique = true) var email: String,
    @Column(name = "password_hash", nullable = false) var passwordHash: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // id'yi PostgreSQL üretir
    var id: Long? = null                                    // kaydedilene kadar null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}
```

Önemli noktalar:
- **Neden `data class` değil, normal `class`?** Hibernate entity'leri kendi yöntemleriyle karşılaştırır ve
  tembel yükleme (lazy loading) için proxy'ler oluşturur. `data class`'ın otomatik ürettiği `equals`/`hashCode`
  (tüm alanlara bakan) ve `copy` bunlarla çakışabilir. Entity'ler için normal `class` önerilir.
- **`id` neden `Long?`?** Nesneyi oluşturduğumuz an henüz veritabanında yok; numarayı PostgreSQL kayıt
  anında verir (`GENERATED ALWAYS AS IDENTITY`). `save()` sonrasında dolar.
- **`@Column(name = "password_hash")`**: Kotlin camelCase, SQL snake_case kullanır; eşleşmeyi açıkça yazdık.
- **`Instant`**: Zaman dilimi bağımsız bir an. PostgreSQL'deki `TIMESTAMPTZ` ile eşleşir.
- **Entity dışarı gönderilmez**: İçinde şifre hash'i var. API'ye DTO gider (Bölüm 3).

**İki Kotlin plugin'i daha:**
- **`kotlin("plugin.jpa")`**: JPA her entity'nin **boş constructor**'ı olmasını ister (veritabanından okurken
  önce boş nesne oluşturup alanları sonra doldurur). Kotlin sınıflarında bu yoktur; plugin derleme anında ekler.
- **`allOpen { annotation("jakarta.persistence.Entity") ... }`**: Hibernate de entity'lerin alt sınıfını
  (proxy) oluşturur, bu yüzden `open` olmaları gerekir.

### 2.4 Repository

Repository, bir entity için veritabanıyla konuşan **tek yerdir**.

```kotlin
interface UserRepository : JpaRepository<User, Long> {   // <entity tipi, id tipi>
    fun findByEmail(email: String): User?                  // bulunamazsa null
    fun existsByEmail(email: String): Boolean
}
```

- Sadece **interface** yazarız; gerçek kodunu uygulama açılırken **Spring Data JPA** üretir ve bean olarak kaydeder.
- `JpaRepository` hazır olarak verir: `save`, `saveAndFlush`, `findById`, `findAll`, `count`, `deleteById`...
- **Derived query (isimden türetilen sorgu)**: Fonksiyon ismi SQL'e çevrilir.
  `findByEmail` → `SELECT ... WHERE email = ?`, `existsByEmail` → var mı yok mu.
  Daha karmaşık sorgular için `@Query("...")` ile elle JPQL/SQL yazılabilir.
- Parametreler her zaman güvenli şekilde (prepared statement) gönderilir → **SQL injection** koruması otomatik.
- Kotlin'de `findById` `Optional` döner; Kotlin için `findByIdOrNull(id)` uzantısı daha doğaldır.

Ktor'daki 75 satırlık elle yazılmış `UserRepository` → 4 satır.

### 2.5 Flyway: veritabanının sürüm kontrolü

Git kodun geçmişini tutar; **Flyway veritabanı şemasının** geçmişini tutar.

- `db/migration` klasöründeki `V1__...sql`, `V2__...sql` dosyalarını sırayla **bir kez** çalıştırır.
- Hangilerini çalıştırdığını ve dosyanın **checksum**'ını `flyway_schema_history` tablosuna yazar.
- Uygulanmış bir dosya sonradan değiştirilirse checksum tutmaz ve Flyway uygulamayı durdurur.
  Bu yüzden kural: **eski migration'a dokunma, yeni dosya ekle.**

### 2.6 Flyway + Hibernate iş bölümü: `ddl-auto`

| `ddl-auto` | Hibernate ne yapar | Nerede |
|---|---|---|
| `create` / `create-drop` | Tabloları silip yeniden oluşturur | Sadece deneme projeleri |
| `update` | Eksik sütun/tabloyu eklemeye çalışır | Önerilmez (kontrolsüz değişiklik) |
| **`validate`** | Sadece entity ↔ tablo uyumunu kontrol eder | **Bizim seçimimiz** |
| `none` | Hiçbir şey | — |

Şemayı **Flyway değiştirir**, Hibernate sadece **kontrol eder**. Entity'de tabloda olmayan bir alan
yazarsak uygulama açılışta hata verir; sorun canlıya çıkmadan yakalanır.

### 2.7 Bağlantı havuzu (HikariCP)

Veritabanına her bağlanmak (TCP + kimlik doğrulama) pahalıdır. **Connection pool**, birkaç bağlantıyı açık
tutar ve istekler bunları ödünç alıp geri verir. Spring Boot, `spring.datasource.*` ayarlarını görünce
HikariCP'yi otomatik kurar (Ktor'da `DatabaseFactory.kt` ile elle kuruyorduk).

### 2.8 `open-in-view: false`

Spring Boot varsayılan olarak veritabanı oturumunu HTTP cevabı yazılana kadar açık tutar (Open Session in
View). Bu, controller'da fark edilmeden ek sorgular çalışmasına ve bağlantıların uzun süre meşgul kalmasına
yol açabilir. Kapattık: veritabanı işi Service katmanında bitmeli.

### 2.9 Testler: `@DataJpaTest`, profil, transaction ve rollback

```kotlin
@DataJpaTest                                        // sadece JPA + repository + Flyway başlar
@AutoConfigureTestDatabase(replace = Replace.NONE)  // bellek içi DB yerine gerçek PostgreSQL kullan
@ActiveProfiles("test")                             // application-test.yml'i de yükle → splitapp_test
class UserRepositoryTest(@Autowired private val userRepository: UserRepository)
```

- **Profil (profile)**: Ortama göre ayar seti. `application-test.yml` sadece `test` profili aktifken yüklenir
  ve yalnızca içindeki değerleri (DB adresi) değiştirir.
- **Transaction (işlem)**: Birden fazla veritabanı işleminin "ya hepsi ya hiçbiri" şeklinde çalışması.
  Banka havalesi benzetmesi: para bir hesaptan düşüp diğerine geçmeli; arada hata olursa ikisi de geri alınır.
- **Rollback**: Transaction'daki değişiklikleri geri almak. `@DataJpaTest` her testi transaction içinde
  çalıştırıp sonunda rollback yapar → her test temiz veritabanıyla başlar (testler sonrası kullanıcı sayısı 0 çıktı).
- **`save` ve `saveAndFlush` farkı**: Hibernate değişiklikleri biriktirip uygun anda veritabanına gönderir
  (**flush**). `saveAndFlush` hemen gönderir. Aynı email testinde hatanın tam o satırda oluşması için kullandık.
- **Veritabanı kısıtı (constraint)**: `email ... UNIQUE` sayesinde kod kontrol etmeyi unutsa bile veritabanı
  kopyayı reddeder; Spring bunu `DataIntegrityViolationException`'a çevirir. İş kuralları mümkün olduğunca
  hem kodda hem veritabanında korunur.

### 2.10 Test piramidi (bizdeki karşılığı)

```
        /  Uçtan uca  \        → az sayıda, yavaş (6. Aşama: Postman senaryosu)
       / Entegrasyon   \       → @SpringBootTest, @DataJpaTest, @WebMvcTest
      /   Unit test     \      → çok sayıda, hızlı (5. Aşama: Service testleri)
```

**Mülakat notları**
- ORM nedir, avantajları ve dezavantajları? (Az tekrar kodu ve tip güvenliği / üretilen SQL'i gözden kaçırma,
  N+1 sorunu riski.)
- JPA ile Hibernate arasındaki fark? (JPA standart/spesifikasyon, Hibernate uygulaması.)
- `ddl-auto` değerleri ve canlı ortamda neden `validate`/`none` kullanılır?
- Migration aracı neden kullanılır? Uygulanmış migration neden değiştirilmez?
- Transaction nedir? ACID ne demek? (Atomicity, Consistency, Isolation, Durability.)
- Connection pool neden gerekir?
- SQL injection nedir, nasıl önlenir? (Parametreli sorgular / prepared statement.)
- Entity için neden `data class` önerilmez?
- `@DataJpaTest` ile `@SpringBootTest` farkı?

---

## Bölüm 3 — Hata yönetimi ve DTO'lar

**Amaç:** API'nin döndüğü her hata aynı şekle sahip olsun: `{"error": "mesaj"}`. Ktor sürümündeki bu
sözleşme aynen korunuyor; böylece Android uygulaması tüm hataları tek bir kodla okuyabilecek.

### 3.1 DTO (Data Transfer Object)

**DTO**, sadece veri taşıyan, iş mantığı içermeyen sınıftır. API'ye **giren** (istek gövdesi) ve API'den
**çıkan** (cevap) verinin şeklini belirler.

İlk DTO'muz (`common/ErrorResponse.kt`):

```kotlin
data class ErrorResponse(val error: String)
// Jackson bunu şuna çevirir → {"error": "Email is not valid"}
```

JSON'daki alan adı, Kotlin'deki property adından gelir (`error`). Property adını değiştirmek API
sözleşmesini değiştirir; istemciler bozulur. Bu yüzden DTO'lardaki isimler bilinçli seçilir.

**Entity ile DTO neden ayrı?**

| | Entity (`User`) | DTO (`UserResponse`, `ErrorResponse`...) |
|---|---|---|
| Neyin şekli | Veritabanı tablosu | API'nin JSON'u |
| Kim değiştirir | Veritabanı ihtiyaçları (migration) | API sözleşmesi |
| Hassas alan | İçerebilir (`passwordHash`) | Asla içermez |
| Kotlin tipi | Normal `class` (bkz. 2.3) | `data class` |

Ayırmanın üç faydası:
1. **Güvenlik**: Entity'yi doğrudan döndürseydik `passwordHash` JSON'a sızardı. DTO'da o alan hiç yoktur.
2. **Bağımsız değişim**: Tabloya yeni bir sütun eklemek API'yi kendiliğinden değiştirmez (ve tersi).
3. **İhtiyaca göre şekil**: Aynı entity'den farklı amaçlar için farklı DTO'lar üretilebilir
   (örn. liste için kısa, detay için uzun).

**Neden DTO için `data class`?** Kotlin `data class` otomatik olarak `equals`, `hashCode`, `toString` ve
`copy` üretir. DTO'lar sadece değer taşıdığı için bunlar tam istediğimiz şeydir: iki `ErrorResponse("x")`
eşit sayılır, testlerde karşılaştırmak kolaylaşır. (Entity'de bunun neden sorun olduğu: 2.3.)

`val` kullandık: DTO oluşturulduktan sonra değişmez (**immutable**). Değişmeyen nesneler hata yapma
ihtimalini azaltır.

### 3.2 Ders: Test hatası her zaman kod hatası değildir

`ErrorResponse.kt` eklendikten sonra 7 testten 6'sı başarısız oldu. Sebep koddaki bir hata değildi:
bilgisayar yeniden başlatıldığı için **Docker Desktop kapanmıştı**, testler PostgreSQL'e bağlanamadı.

Hatayı bulma yöntemi:
1. **Hangi testler kırıldı, hangisi geçti?** Sadece veritabanı kullanan testler kırıldı; veritabanı
   kullanmayan `HealthControllerTest` geçti → şüphe veritabanına yöneldi.
2. **Hata zincirinin en altındaki `Caused by` satırını oku.** Üstteki satırlar genelde sonuçtur, en alttaki
   asıl sebeptir: `Connection to localhost:5432 refused`.
3. **Sebebi doğrula**: `docker ps` → Docker çalışmıyor.

Docker'ı başlatıp `docker compose up -d` ile container'ı açınca testler geçti. Veriler kaybolmadı, çünkü
volume'de (`splitapp_pgdata`) saklanıyor.

**Alışkanlık:** Geliştirmeye başlamadan önce `docker compose up -d` çalıştır.

---

## Mülakat soruları özeti

Kısa tekrar listesi (detaylar ilgili bölümde):

| Konu | Soru | Bölüm |
|---|---|---|
| Git | merge vs rebase; push edilmiş geçmiş neden yeniden yazılmaz | 0.3 |
| Spring | Spring vs Spring Boot; auto-configuration | 1.1 |
| Spring | IoC, DI, bean, scope; constructor injection | 1.2 |
| Spring | Stereotype annotation'lar (`@Service`, `@Repository`...) | 1.2 |
| Test | `@SpringBootTest` vs `@WebMvcTest` vs `@DataJpaTest`; MockMvc | 1.6, 2.9 |
| Veritabanı | ORM, JPA vs Hibernate, derived query | 2.2–2.4 |
| Veritabanı | Migration, Flyway, `ddl-auto` | 2.5–2.6 |
| Veritabanı | Transaction, ACID, rollback, connection pool | 2.7, 2.9 |
| Güvenlik | SQL injection | 2.4 |
| API tasarımı | DTO nedir, entity neden doğrudan döndürülmez | 3.1 |
| Kotlin | `data class` nedir, DTO'da neden kullanılır, entity'de neden kullanılmaz | 2.3, 3.1 |
| Hata ayıklama | Stack trace nasıl okunur (`Caused by`) | 3.2 |
