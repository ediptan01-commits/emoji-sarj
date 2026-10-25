# HONOR Magic7 Pro / MagicOS 10 şarj teması

**Durum: yapısal kontrolleri geçen deneysel `.hnt`; fiziksel telefonda denenmedi.**
Bu, APK'dan ayrı bir HONOR tema modülüdür. Ekran koruyucusunun işlevsiz
açma düğmesini düzeltmez veya açmaz. Kullanıcının görüntüsündeki ayar,
fotoğraf/albüm ekran koruyucusudur; normal bir uygulama sistemin güvenli
ekran koruyucu seçimini zorla değiştiremez.

MagicOS 10+ için HONOR'un resmi tema kılavuzu şarj animasyonu özelleştirmesini
belgeler. Bu yol kısa sistem şarj efektini hedefler. Sürekli açık bir kilit
ekranı/AOD veya şarj boyunca çalışan ekran koruyucu değildir. Belgelenen efekt
süresi yaklaşık 6 saniyedir; tetiklenme, bölge ve tema kabulü cihazda doğrulanmalıdır.

## İçerik

- Aynı özgün sarı Pompi atlası; pompalama ve ter silme pozları.
- Kodla çizilen özgün renkli halka; hortum halkaya bağlanır.
- 1080×2160 RGBA32, 30 fps, 180 kare / 6 saniye.
- HONOR Theme Editor 2.5.0'ın **AlphaVideoMakeTool** programıyla üretilen
  H.264 renk videosu + `mp4s` alfa veri izi. Sıradan şeffaf olmayan MP4 değildir.
- `charging/charging_config.xml` şeması resmi 8.1 şablonundan doğrulanmıştır;
  açıklama sürümü şablondaki 8.0 ailesinde tutulur. MagicOS 10 diye uydurma
  bir `10.0` açıklama sürümü verilmez.
- Gerçek yüzde ve şarj modu, yerel `progress_text` / `percentage_text` /
  `charging_prompt_texts` alanlarında sistem tarafından çizilir. Videoda
  sabit yüzde, saat, bildirim veya ikinci pil yoktur.
- Yalnızca `charging`, `preview` ve `description.xml`. Kilit ekranı,
  duvar kâğıdı, uygulama ikonları ve diğer depo kaynakları pakete alınmaz.

Önizleme kapağındaki **94%** temsili yerleşimdir; telefondan alınmış sonuç
değildir. Kullanıcının banka bildirimi içeren ekran görüntüsü hiçbir varlığa
veya pakete eklenmemiştir. Halka da o görüntüden kesilmemiştir.

## Üretim

Python 3.12+ ve `requirements.txt` içindeki Pillow sürümü gerekir.
Resmi editör/encoder kullanıcı tarafından HONOR'dan ayrıca edinilir;
editör, DLL veya üretici kaynak kodu bu depoda dağıtılmaz.

```powershell
python -m pip install -r honor-theme/requirements.txt
python honor-theme/build_frames.py --output build/honor-frames
python honor-theme/build_theme.py --frames-root build/honor-frames --encoder 'C:/HONOR/AlphaVideoMakeTool.exe' --output build/honor-theme
```

Encoder yanındaki `ffprobe.exe` de bulunmalıdır. İstek, resmi editörün
kullandığı tek Base64 JSON argümanı biçimindedir. HNT bir ZIP'tir;
`charging` klasör olarak kalır. Resmi editörün yalnız `com.*` ve `icons`
modüllerini iç ZIP'e dönüştüren dışa aktarma davranışı esas alınmıştır.

İşlem kare sayısı/biçimi, iki video izi, 30 fps, iki izde 180 örnek,
6 saniye süre, XML parse/referanslar, ZIP CRC ve 95 MB boyut sınırını
denetler. `honor-dogrulama.json` araç ve paket SHA256 değerlerini içerir.
Bunlar HONOR editörünün tam görsel doğrulamasının, mağaza onayının veya
fiziksel telefon testinin yerine geçmez. Encoder'ın örnek tablosu hakkında
verdiği tanı satırları günlüğe saklanır; çıkış kodu 0 ve video decode
kontrolü ayrıca doğrulanmıştır.

## Telefonda deneme

Resmi, belgelenen yöntem: HONOR Theme Editor 2.5.0'da `.hnt` dosyasını
içe alın; şarj modülünü kontrol edin; **Sync / Sync to phone** ile bağlı
telefona gönderin. Telefonda **Temalar → Ben → İndirilenlerim** altında
Pompi temasını açıp uygulayın. Dil/bölgeye göre menü isimleri değişebilir.
Bilgisayar bağlantısı isterse telefonda USB hata ayıklama iznini kendiniz
onaylayın. Editörün tüm varsayılan modüllerini dışa aktarmayın; yalnızca
şarj modülü ve önizlemeyi seçin.

Telefonun Temalar uygulamasında yerel dosya içe alma seçeneği varsa `.hnt`
oradan da denenebilir. Bu seçeneğin bu bölgesel sürümde bulunduğu
doğrulanmadı; dosyayı indirmek tek başına temayı etkinleştirmez.

Temayı uyguladıktan sonra kilit ekranında kabloyu çıkarıp yeniden takın.
Yüzdenin gerçek pil düzeyiyle değiştiğini, karakterin/gerçek saatin
çakışmadığını, güvenli kilidin ve bildirimlerin korunduğunu kontrol edin.
Çalışmazsa eski temayı Temalar'dan yeniden uygulayın. Tema görünmüyor veya
şarj modülü yok sayılıyorsa uyumluluk doğrulanmamıştır; ekran koruyucu
düğmesini açmak bunu çözmez. Telefon testinden önce “kesin çalışıyor” denmez.

## Kaynaklar

- [HONOR Theme Editor kılavuzu](https://developer.honor.com/cn/docs/11106/guides/themetool)
- [Resmi indirme sayfası](https://developer.honor.com/cn/docs/11106/guides/download)
- [HONOR mobil tema biçimi](https://developer.honor.com/cn/docs/11106/guides/mobile)
- [Android DreamService](https://developer.android.com/reference/android/service/dreams/DreamService)

Resmi editör 2.5.0 yükleyicisi SHA256:
`BC7409A482E1599083475354C2B96363A920F11FFB993A95A63E8B2BC585D2AB`.
Kaynak resmi HONOR CDN'idir; yükleyicide Authenticode imzası bulunmadığından
yayıncı imzasının doğrulandığı iddia edilmez. Bu geliştirmede yükleyici
kurulmadı; arşivinden alınan dönüştürücü kullanıldı.
