# Emoji Şarj · Pompi

Flutter arayüzü + Android yerel animasyon hizmetleri. Android 8.0 / API 26 ve üzeri.

## Gerçekte ne yapar?

- Kullanıcı izin verip Başlat'a bastığında kalıcı bir ön plan hizmeti başlar.
- Kablo takılı, ekran açık ve kilit çözülmüşse sağ üstte küçük sarı karakter ve kırmızı pompa gösterilir. Küçük görünümde ikinci pil, yüzde veya sahte status bar yoktur.
- Pompalama, terleme, bezle terini silme ve %100 kutlama pozları aynı şeffaf sprite atlasını kullanır. Poz geçişlerine küçük hareketler eşlik eder; bu dört pozlu bir animasyondur.
- Sağ/üst boşluk ve boyut ayarları cihazda saklanır. Dokunmalar alttaki uygulamaya geçer. Bazı güvenli uygulamalar Android üzerinden kaplamayı gizleyebilir.
- Kablo çıkarılınca, ekran kapanınca veya kilitlenince küçük karakter kaldırılır. Hizmet yeni şarj bağlantısını bekler; uygulamadaki veya bildirimdeki Durdur düğmesi hizmeti bitirir. Bildirim izni reddedilirse bildirim çekmecesindeki kontrol görünmeyebilir; uygulama içindeki Durdur kullanılabilir.
- Büyük sarı karakter + neon yeşil batarya, Android **Ekran koruyucu / Screen saver** (`DreamService`) olarak sunulur. Sistem ayarından Pompi'yi seçip şarj sırasında başlatmayı etkinleştirin. Üreticinin desteklemesi ve ekran koruyucu zamanlaması gerekir. Gerçek pil yüzdesi kullanılır, kablo çıkarılınca kapanır. Hafif konum kaydırması ve düşük parlaklık kullanılır.
- Tam ekranı dene düğmesi %64 temsili verili önizlemedir; dokununca çıkar. Bu ekran gerçek kilit ekranı değildir.

## Android sınırları

Standart Android uygulaması sistemin sağ üst pil simgesini değiştiremez veya yanına özel status bar öğesi yerleştiremez. `TYPE_APPLICATION_OVERLAY` sistem status bar'ının **altında** kalır. Bu sürüm küçük karakteri en yakın izinli alana, sağ üstte status bar'ın hemen altına yerleştirir. Üretici ve ekran çentiğine göre konum ayarlanabilir.

Sıradan bir uygulama güvenli kilit ekranının yerini her cihazda otomatik alamaz. Bu uygulama telefonu kendiliğinden uyandırmaz, kilidi kaldırmaz ve alarm/arama amaçlı tam ekran bildirim izinlerini kullanmaz. DreamService desteklenmeyen telefonlarda büyük animasyon elle önizlenebilir. Başka uygulamalardaki hareketsizliği izlemek için erişilebilirlik veya kullanım geçmişi izni istenmez; cihaz boşta durumunu Android yönetir.

Başlatma yalnızca görünür uygulamadan yapılır; yeniden başlatma/boot otomasyonu yoktur. Android hizmeti sonlandırırsa uygulamayı açıp yeniden Başlat'a basmak gerekebilir. `specialUse` ön plan hizmeti gerekçesi manifestte tanımlıdır; Play Store yayını için ilgili kullanımın Google tarafından ayrıca değerlendirilmesi gerekir.

Görsel referanstaki ana sarı karakter uygulanmıştır. Tavşan/kedi/panda/robot, tema seçimi ve tahmini dolum süresi bu sürümde yoktur. Kalan süreye ilişkin uydurma veri gösterilmez.

## Derleme

Flutter **3.35.7**, Dart **3.9.2**, Java **17**, Android SDK/NDK (Flutter'ın belirlediği sürümler). Önceki prototipin varsayılan Android uygulama kimliği `com.example.emoji_sarj` korunur; Kotlin ad alanı `com.pompi.emoji_sarj` olur.

```sh
flutter pub get --enforce-lockfile
dart format --output=none --set-exit-if-changed lib test
flutter analyze
flutter test
flutter build apk --debug
cd android
./gradlew testDebugUnitTest lintDebug
```

Android proje kaynakları ve Gradle wrapper depodadır. CI, `flutter create` çalıştırmaz; özel Android hizmetleri yeniden üretilmez. `.github/workflows/android.yaml` ana dala veya `pompi/**` dallarına gönderimde, pull request'te ve elle başlatmada aynı kontrolleri çalıştırır. Başarılı çalışmanın `emoji-sarj-debug-apk` artifact'inde kurulumluk debug APK bulunur. Debug APK geliştirme/test içindir; imzalı mağaza sürümü değildir.

## Cihaz kabul kontrolü

1. Ekran üstünde gösterme iznini verin, dönüp Başlat'a basın. Bildirim iznini de verin.
2. Şarjı bağlayın, başka uygulamaya geçin. Sağ üstte yalnızca küçük karakter/pompa olmalı; telefonun kendi pil simgesi korunmalı.
3. Konum ve boyutu farklı çentik/yönlerde deneyin. Kaplama dokunmaları engellememeli.
4. Kabloyu çıkarın, yeniden takın; kilitleyin, açın. Gizlenme/görünme durumlarını kontrol edin.
5. Ekran koruyucu ayarından Pompi + şarj sırasında seçin. Android'in desteklediği boşta zamanlamasını bekleyin. Kabloyu çıkarınca kapanmalı; uyandırınca güvenli kilit davranışı korunmalı.
6. %100'de kutlama, %100 fakat kablo yokken gizlenme, bildirimden Durdur, uygulamadan Durdur ve izin iptalini deneyin.
7. Samsung/Xiaomi/Pixel gibi en az iki üreticide ve Android 13/14/15+ üzerinde kontrol edin. Üretici arka plan kısıtlarını ayrıca değerlendirin.

Flutter widget testleri izin/başlat/durdur akışını, ikinci pil bulunmamasını ve küçük ekran taşmalarını kontrol eder. Kotlin testi bir şarj oturumundaki ekran kapatma, kilitleme, kilidi açma, büyük sahneye geçiş ve kablo çıkarma durumlarını kapsar. Bunlar fiziksel telefon testinin yerine geçmez.

## Görsel varlık

`assets/pompi-atlas.png`: ekli referansın sarı karakterine göre yerleşik ImageGen aracıyla üretilen dört pozlu, 2×2 şeffaf atlas. Üst sol pompa yukarı, üst sağ pompa aşağı/terleme, alt sol ter silme, alt sağ kutlama. İstem `docs/asset-prompt.txt` içinde saklanır. Flutter ve Android aynı atlası kullanır. Ağ veya hesap gerekmez; üretim uygulamasında INTERNET izni yoktur. Flutter'ın debug/profile geliştirme manifestleri geliştirme bağlantıları için INTERNET izni içerir.

Kaynaklar: [Android overlay](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY), [DreamService](https://developer.android.com/reference/android/service/dreams/DreamService), [Ön plan hizmeti türleri](https://developer.android.com/develop/background-work/services/fgs/service-types).

