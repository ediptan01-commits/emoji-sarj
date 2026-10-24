import 'dart:async';
import 'package:battery_plus/battery_plus.dart';
import 'package:flutter/material.dart';

void main() => runApp(const EmojiSarjApp());

class EmojiSarjApp extends StatelessWidget {
  const EmojiSarjApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'Emoji Şarj',
        theme: ThemeData.dark(useMaterial3: true),
        home: const ChargingPage(),
      );
}

class ChargingPage extends StatefulWidget {
  const ChargingPage({super.key});
  @override
  State<ChargingPage> createState() => _ChargingPageState();
}

class _ChargingPageState extends State<ChargingPage> {
  final Battery battery = Battery();
  StreamSubscription<BatteryState>? subscription;
  Timer? timer;
  int level = 0;
  int tick = 0;
  bool charging = false;
  int selected = 0;
  final names = ['Pompi Emoji', 'Mırmır Kedi', 'Fındık Fare', 'Zıpzıp Tavşan'];
  final faces = ['😎', '🐱', '🐭', '🐰'];

  @override
  void initState() {
    super.initState();
    refresh();
    subscription = battery.onBatteryStateChanged.listen((_) => refresh());
    timer = Timer.periodic(const Duration(milliseconds: 500), (_) {
      if (!mounted) return;
      setState(() => tick++);
      if (tick % 10 == 0) refresh();
    });
  }

  Future<void> refresh() async {
    try {
      final newLevel = await battery.batteryLevel;
      final state = await battery.batteryState;
      if (!mounted) return;
      setState(() {
        level = newLevel;
        charging = state == BatteryState.charging || state == BatteryState.full;
      });
    } catch (_) {
      // Some devices may not provide battery information.
    }
  }

  @override
  void dispose() {
    subscription?.cancel();
    timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final phase = tick % 20;
    final wiping = phase >= 14 && phase < 18;
    final sweating = phase >= 9 && phase < 14;
    final pumping = charging && level < 100 && !wiping;
    final offset = pumping && tick.isEven ? 16.0 : 0.0;
    return Scaffold(
      backgroundColor: const Color(0xFF0C1025),
      appBar: AppBar(title: const Text('⚡ Emoji Şarj'), backgroundColor: Colors.transparent),
      body: SafeArea(
        child: Column(children: [
          const SizedBox(height: 12),
          Text(charging ? 'Şarj bağlantısı aktif' : 'Şarj kablosunu bağla',
              style: const TextStyle(fontSize: 19)),
          const SizedBox(height: 12),
          Text('%$level', style: const TextStyle(fontSize: 58, fontWeight: FontWeight.bold)),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 38),
            child: LinearProgressIndicator(
              value: level / 100,
              minHeight: 18,
              borderRadius: BorderRadius.circular(16),
              backgroundColor: Colors.white12,
              color: level < 20 ? Colors.redAccent : Colors.lightGreenAccent,
            ),
          ),
          const Spacer(),
          AnimatedContainer(
            duration: const Duration(milliseconds: 250),
            transform: Matrix4.translationValues(0, offset, 0),
            child: Text(faces[selected], style: const TextStyle(fontSize: 116)),
          ),
          const SizedBox(height: 12),
          Text(
            charging
                ? (level == 100
                    ? '🎉 Batarya doldu!'
                    : wiping
                        ? '🧻 Terimi siliyorum...'
                        : sweating
                            ? '💦 Çok yoruldum!'
                            : '💪 Pompala, pompala!')
                : '🔌 Şarja takınca pompalayacağım!',
            style: const TextStyle(fontSize: 21, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 10),
          Text(pumping ? (tick.isEven ? '🪠 ⬇️' : '🪠 ⬆️') : '🪠',
              style: const TextStyle(fontSize: 58)),
          const Spacer(),
          const Text('Karakter seç', style: TextStyle(fontSize: 18)),
          SizedBox(
            height: 112,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: names.length,
              itemBuilder: (context, i) => Padding(
                padding: const EdgeInsets.all(6),
                child: ChoiceChip(
                  selected: selected == i,
                  onSelected: (_) => setState(() => selected = i),
                  label: Column(mainAxisSize: MainAxisSize.min, children: [
                    Text(faces[i], style: const TextStyle(fontSize: 32)),
                    Text(names[i], style: const TextStyle(fontSize: 11)),
                  ]),
                ),
              ),
            ),
          ),
          const Padding(
            padding: EdgeInsets.all(12),
            child: Text(
              'İlk prototip: uygulama açıkken çalışır. Özgün karakter çizimleri ve kilit ekranı desteği sonraki aşamada.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white54, fontSize: 12),
            ),
          ),
        ]),
      ),
    );
  }
}
