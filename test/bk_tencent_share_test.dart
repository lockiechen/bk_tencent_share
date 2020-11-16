import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:bk_tencent_share/bk_tencent_share.dart';

void main() {
  const MethodChannel channel = MethodChannel('bk_tencent_share');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await BkTencentShare.platformVersion, '42');
  });
}
