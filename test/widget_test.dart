import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:emoji_sarj/main.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  bool enabled = false;
  bool allowed = true;
  final calls = <String>[];
  setUp(() {
    enabled = false;
    allowed = true;
    calls.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(bridge, (call) async {
      calls.add(call.method);
      if (call.method == 'start') enabled = true;
      if (call.method == 'stop') enabled = false;
      if (call.method == 'status') {
        return {
          'enabled': enabled,
          'allowed': allowed,
          'plugged': true,
          'right': 8,
          'top': 0,
          'size': 48
        };
      }
      return null;
    });
  });
  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(bridge, null);
  });
  testWidgets('starts and stops explicitly, no duplicate battery gauge',
      (tester) async {
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
  testWidgets('permission is requested before starting overlay',
      (tester) async {
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
  testWidgets('small screen scrolls to full screen actions without overflow',
      (tester) async {
    tester.view.physicalSize = const Size(320, 568);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(const EmojiSarjApp());
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(find.text('Tam ekranı dene →'), 250);
    await tester.tap(find.text('Tam ekranı dene →'));
    await tester.pumpAndSettle();
    expect(calls, contains('preview'));
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  });
}
