import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'pompi_sprite.dart';

void main() => runApp(const EmojiSarjApp());
const bridge = MethodChannel('emoji_sarj/control');

class EmojiSarjApp extends StatelessWidget {
  const EmojiSarjApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    title: 'Emoji Şarj',
    theme: ThemeData.dark(useMaterial3: true).copyWith(
      scaffoldBackgroundColor: const Color(0xff050d20),
      colorScheme: ColorScheme.fromSeed(
        seedColor: const Color(0xffb6f873),
        brightness: Brightness.dark,
      ),
    ),
    home: const ChargingPage(),
  );
}

class ChargingPage extends StatefulWidget {
  const ChargingPage({super.key});
  @override
  State<ChargingPage> createState() => _ChargingPageState();
}

class _ChargingPageState extends State<ChargingPage>
    with WidgetsBindingObserver {
  bool enabled = false, allowed = false, plugged = false;
  double right = 8, top = 0, size = 48;
  String? message;
  Timer? statusTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    refresh();
    statusTimer = Timer.periodic(const Duration(seconds: 2), (_) => refresh());
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    statusTimer?.cancel();
    if (state == AppLifecycleState.resumed) {
      refresh();
      statusTimer = Timer.periodic(
        const Duration(seconds: 2),
        (_) => refresh(),
      );
    }
  }

  Future<void> refresh() async {
    try {
      final data = await bridge.invokeMapMethod<String, dynamic>('status');
      if (!mounted || data == null) return;
      setState(() {
        enabled = data['enabled'] == true;
        allowed = data['allowed'] == true;
        plugged = data['plugged'] == true;
        right = (data['right'] as num).toDouble();
        top = (data['top'] as num).toDouble();
        size = (data['size'] as num).toDouble();
      });
    } on PlatformException catch (e) {
      if (mounted) setState(() => message = e.message);
    } on MissingPluginException {
      if (mounted) {
        setState(
          () => message = 'Bu özellikler Android telefonda kullanılabilir.',
        );
      }
    }
  }

  Future<void> command(String name, [Map<String, dynamic>? args]) async {
    try {
      await bridge.invokeMethod<void>(name, args);
      await refresh();
    } on PlatformException catch (e) {
      if (mounted) {
        setState(() => message = e.message ?? 'İşlem tamamlanamadı.');
      }
    } on MissingPluginException {
      if (mounted) setState(() => message = 'Android telefonda deneyin.');
    }
  }

  @override
  void dispose() {
    statusTimer?.cancel();
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  Widget panel(Widget child) => Container(
    padding: const EdgeInsets.all(22),
    decoration: BoxDecoration(
      color: const Color(0xff0d1b32),
      borderRadius: BorderRadius.circular(26),
    ),
    child: child,
  );
  @override
  Widget build(BuildContext context) => Scaffold(
    body: SafeArea(
      child: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          const Text(
            '⚡ EMOJİ ŞARJ',
            style: TextStyle(
              color: Colors.white,
              letterSpacing: 1,
              fontSize: 24,
            ),
          ),
          const SizedBox(height: 18),
          const Text(
            'Küçük dostun,\nbüyük enerjin.',
            style: TextStyle(
              fontSize: 32,
              height: 1.08,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 16),
          const Center(child: PompiSprite(size: 190)),
          const Text(
            'Pompi pompalar, terler, bir nefes alıp terini siler. Sen telefonunu kullanmaya devam et.',
            style: TextStyle(color: Colors.white70, height: 1.6),
          ),
          const SizedBox(height: 24),
          panel(
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const Icon(Icons.bubble_chart, color: Color(0xffb6f873)),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        enabled ? 'Pompi görevde' : 'Pompi hazır',
                        style: const TextStyle(
                          fontSize: 22,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  enabled
                      ? (plugged
                            ? 'Şarj bağlı. Ekran açık ve kilit yokken küçük karakter görünür.'
                            : 'Kablo bekleniyor. Küçük karakter şarj bağlanınca görünür.')
                      : 'Küçük karakteri diğer uygulamaların üzerinde göstermek için başlat.',
                  style: const TextStyle(color: Colors.white70, height: 1.5),
                ),
                const SizedBox(height: 18),
                FilledButton.icon(
                  onPressed: () => command(
                    enabled
                        ? 'stop'
                        : allowed
                        ? 'start'
                        : 'permission',
                  ),
                  icon: Icon(
                    enabled ? Icons.stop_circle_outlined : Icons.play_arrow,
                  ),
                  label: Text(
                    enabled
                        ? 'Pompi’yi durdur'
                        : allowed
                        ? 'Pompi’yi başlat'
                        : 'Üstte gösterme iznini aç',
                  ),
                ),
                if (!allowed)
                  const Text(
                    'İzni verdikten sonra buraya dön ve başlat.',
                    style: TextStyle(fontSize: 12, color: Colors.white60),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          panel(
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  '01  Küçük yol arkadaşı',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 10),
                const Text(
                  'İkinci pil yok. Yalnızca karakter ve pompası. Android sistem çubuğuna çizim izni vermez; karakter sağ üstte, çubuğun hemen altında durur. Konumu telefonuna göre ayarla.',
                  style: TextStyle(color: Colors.white70, height: 1.5),
                ),
                for (final item in [
                  ('Sağdan boşluk', right, 0.0, 160.0, 'right'),
                  ('Üstten ek boşluk', top, 0.0, 200.0, 'top'),
                  ('Karakter boyutu', size, 32.0, 80.0, 'size'),
                ]) ...[
                  const SizedBox(height: 12),
                  Text('${item.$1} · ${item.$2.round()} dp'),
                  Slider(
                    value: item.$2,
                    min: item.$3,
                    max: item.$4,
                    onChanged: (v) => setState(() {
                      if (item.$5 == 'right') right = v;
                      if (item.$5 == 'top') top = v;
                      if (item.$5 == 'size') size = v;
                    }),
                    onChangeEnd: (_) => command('position', {
                      'right': right.round(),
                      'top': top.round(),
                      'size': size.round(),
                    }),
                  ),
                ],
                const Text(
                  'Karakter dokunmaları yakalamaz. Kablo çıkarılınca veya ekran kilitlenince gizlenir.',
                  style: TextStyle(
                    color: Colors.white54,
                    fontSize: 12,
                    height: 1.5,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          panel(
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  '02  Dinlenirken büyük sahne',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 10),
                const Text(
                  'Büyük karakter ve batarya yalnızca tam ekranda görünür. Telefonun Ekran koruyucu ayarından Pompi’yi ve “şarj sırasında” seçeneğini seç. Desteklenen cihazlarda Android ekran boşta kalınca açar.',
                  style: TextStyle(color: Colors.white70, height: 1.5),
                ),
                const SizedBox(height: 14),
                OutlinedButton.icon(
                  onPressed: () => command('dreamSettings'),
                  icon: const Icon(Icons.bedtime_outlined),
                  label: const Text('Ekran koruyucuyu ayarla'),
                ),
                TextButton(
                  onPressed: () => command('preview'),
                  child: const Text('Tam ekranı dene →'),
                ),
                const Text(
                  'Her üretici desteklemez. Güvenli kilit ekranının yerine geçmez; kapalı ekranı zorla uyandırmaz. Önizleme temsili, ekran koruyucu gerçek pil verisini kullanır.',
                  style: TextStyle(
                    color: Colors.white54,
                    height: 1.5,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
          if (message != null)
            Padding(
              padding: const EdgeInsets.only(top: 16),
              child: Text(
                message!,
                style: const TextStyle(color: Colors.amberAccent),
              ),
            ),
          const SizedBox(height: 24),
          const Text(
            'Çizim: Pompi • Ağ erişimi ve hesap gerekmez.\nUygulamadan durdurabilirsin. Bildirim izni verirsen bildirimden de durdurabilirsin.',
            style: TextStyle(color: Colors.white38, fontSize: 12, height: 1.6),
          ),
        ],
      ),
    ),
  );
}
