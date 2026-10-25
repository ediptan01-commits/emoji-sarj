import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:emoji_sarj/main.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  bool enabled = false;
  bool allowed = true;
  String? chargingError;
  final calls = <String>[];
  setUp(() {
    enabled = false;
    allowed = true;
    chargingError = null;
    calls.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(bridge, (call) async {
          calls.add(call.method);
          if (call.method == 'chargingScreen' && chargingError != null) {
            throw PlatformException(code: 'unplugged', message: chargingError);
          }
          if (call.method == 'start') enabled = true;
          if (call.method == 'stop') enabled = false;
          if (call.method == 'status') {
            return {
              'enabled': enabled,
              'allowed': allowed,
              'plugged': true,
              'right': 8,
              'top': 0,
              'size': 48,
              'dreamEnabled': false,
              'dreamSelected': false,
            };
          }
          return null;
        });
  });
  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(bridge, null);
  });
  testWidgets('starts and stops explicitly, no duplicate battery gauge', (
    tester,
  ) async {
    await tester.pumpWidget(const EmojiSarjApp());
    await tester.pumpAndSettle();
    expect(find.byType(LinearProgressIndicator), findsNothing);
    await tester.ensureVisible(find.text('Pompi’yi başlat'));
    await tester.tap(find.text('Pompi’yi başlat'));
    await tester.pumpAndSettle();
    expect(calls, contains('start'));
    expect(find.text('Pompi görevde'), findsOneWidget);
    await tester.tap(find.text('Pompi’yi durdur'));
    await tester.pumpAndSettle();
    expect(calls, contains('stop'));
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('permission is requested before starting overlay', (
    tester,
  ) async {
    allowed = false;
    await tester.pumpWidget(const EmojiSarjApp());
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Üstte gösterme iznini aç'));
    await tester.tap(find.text('Üstte gösterme iznini aç'));
    await tester.pumpAndSettle();
    expect(calls, contains('permission'));
    expect(calls, isNot(contains('start')));
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('small screen scrolls to full screen actions without overflow', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(320, 568);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(const EmojiSarjApp());
    await tester.pumpAndSettle();
    final preview = find.text('Tam ekranı dene →').hitTestable();
    for (var i = 0; i < 20 && preview.evaluate().isEmpty; i++) {
      await tester.drag(find.byType(ListView), const Offset(0, -250));
      await tester.pumpAndSettle();
    }
    expect(preview, findsOneWidget);
    await tester.tap(preview);
    await tester.pumpAndSettle();
    expect(calls, contains('preview'));
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets(
    'live charging screen opens independently of disabled Android dream',
    (tester) async {
      await tester.pumpWidget(const EmojiSarjApp());
      await tester.pumpAndSettle();
      final live = find.text('Şarj ekranını şimdi aç').hitTestable();
      for (var i = 0; i < 20 && live.evaluate().isEmpty; i++) {
        await tester.drag(find.byType(ListView), const Offset(0, -250));
        await tester.pumpAndSettle();
      }
      expect(live, findsOneWidget);
      await tester.tap(live);
      await tester.pumpAndSettle();
      expect(calls, contains('chargingScreen'));
      expect(calls, isNot(contains('dreamSettings')));
      expect(
        find.textContaining('Android ekran koruyucusu kapalı'),
        findsOneWidget,
      );
      expect(tester.takeException(), isNull);
      await tester.pumpWidget(const SizedBox());
    },
  );
  testWidgets('charging failures are visible immediately in a snackbar', (
    tester,
  ) async {
    chargingError = 'Önce şarj kablosunu bağla.';
    await tester.pumpWidget(const EmojiSarjApp());
    await tester.pumpAndSettle();
    final live = find.text('Şarj ekranını şimdi aç').hitTestable();
    for (var i = 0; i < 20 && live.evaluate().isEmpty; i++) {
      await tester.drag(find.byType(ListView), const Offset(0, -250));
      await tester.pumpAndSettle();
    }
    await tester.tap(live);
    await tester.pumpAndSettle();
    expect(
      find.descendant(
        of: find.byType(SnackBar),
        matching: find.text(chargingError!),
      ),
      findsOneWidget,
    );
    await tester.pumpWidget(const SizedBox());
  });
}
