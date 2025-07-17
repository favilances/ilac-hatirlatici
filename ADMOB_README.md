# Google AdMob Entegrasyonu

## ✅ Production Reklamları Aktif!

Uygulamanızda artık **gerçek AdMob reklamları** çalışıyor ve gelir elde edebilirsiniz.

## Mevcut Durum

### 🎯 Production Reklamları (Şu An Aktif)
Uygulamanızda şu an gerçek reklam anahtarlarınız kullanılıyor:
- **App ID**: `ca-app-pub-7696062209589998~8118721394`
- **Banner Unit ID**: `ca-app-pub-7696062209589998/2397866570`

### 🔄 Test Reklamlarına Geri Dönüş (Geliştirme İçin)
Geliştirme yaparken test reklamlarını kullanmak için:
- `activity_main.xml` dosyasında `app:adUnitId="@string/admob_test_banner_unit_id"` olarak değiştirin
- Bu Google'ın sağladığı test banner ID'sidir: `ca-app-pub-3940256099942544/6300978111`

## Kullanılan Reklam Birimleri

- **App ID**: `ca-app-pub-7696062209589998~8118721394`
- **Banner Unit ID**: `ca-app-pub-7696062209589998/2397866570`
- **Test Banner Unit ID**: `ca-app-pub-3940256099942544/6300978111`

## Reklam Konumu

Reklamlar ana ekranın en alt kısmında, navigation bar'ın altında görüntülenir.

## Production Reklamları Hakkında

### ⏱️ Yükleme Süresi
- **Test reklamları**: Anında yüklenir
- **Production reklamları**: 2-5 saniye sürebilir
- **İlk yükleme**: Daha uzun sürebilir

### 🎯 Reklam Görünürlüğü
- Production reklamları her zaman mevcut olmayabilir
- Coğrafi konuma göre değişiklik gösterebilir
- Belirli saatlerde daha fazla reklam mevcut olabilir

### 💰 Gelir Tracking
- AdMob konsolundan gelir takibi yapabilirsiniz
- Reklam tıklama ve görüntülenme istatistikleri
- Günlük rapor güncellemeleri

## Test Etme

1. Uygulamayı çalıştırın
2. Ana ekranın altında banner reklam görünecek
3. **Production'da gerçek reklamlar** görünecektir
4. "Reklam Yükleniyor..." yazısı görebilirsiniz

## Sorun Giderme

- **Reklamlar yüklenmiyorsa**: İnternet bağlantısını kontrol edin
- **Logcat kontrolü**: "AdMob" filtresini kullanın
- **Production reklamları**: Test reklamlarından daha az güvenilir
- **Bölgesel farklılıklar**: Bazı bölgelerde daha az reklam mevcut

## ⚠️ Kritik Politika Uyarısı

🚨 **ASLA KENDİ REKLAMLARINIZI TIKLAYMAYIN!**
- Bu AdMob hesabınızın **kalıcı kapatılmasına** neden olur
- Geliştirme yaparken **test reklamlarını** kullanın
- Arkadaşlarınızdan da tıklamamalarını isteyin 