import 'dart:async';

import 'package:flutter/services.dart';

class BkTencentShare {
  static const MethodChannel _channel = const MethodChannel('bk_tencent_share');

  static Future<dynamic> register(
    String appId,
    String universalLink,
    String qqAppId,
    String wxWorkAppId,
    String wxWorkCorpId,
    String wxWorkAgentId,
  ) async {
    final String result = await _channel.invokeMethod('register', {
      'appId': appId,
      'universalLink': universalLink,
      'qqAppId': qqAppId,
      'wxWorkAppId': wxWorkAppId,
      'wxWorkCorpId': wxWorkCorpId,
      'wxWorkAgentId': wxWorkAgentId,
    });
    print('registe result: $result');
    return result;
  }

  static Future<dynamic> shareToWechat(dynamic shareArgs) async {
    final String isWxInstall = await BkTencentShare.isWechatInstalled();
    if (isWxInstall == 'false') {
      return false;
    }
    final String result =
        await _channel.invokeMethod('shareToWechat', shareArgs);
    print('shareToWechat result: $result');
    return result == 'true';
  }

  static Future<dynamic> shareToQQ(dynamic shareArgs) async {
    final String result = await _channel.invokeMethod('shareToQQ', shareArgs);
    print('shareToQQ result: $result');
    return result;
  }

  static Future<dynamic> shareToWework(dynamic shareArgs) async {
    final String result =
        await _channel.invokeMethod('shareToWework', shareArgs);
    print('shareToWework result: $result');
    return result;
  }

  static Future<dynamic> isWechatInstalled() async {
    final String result = await _channel.invokeMethod('isWechatInstalled');
    print('isWechatInstalled result: $result');
    return result;
  }

  static Future<dynamic> isQQInstalled() async {
    final String result = await _channel.invokeMethod('isQQInstalled');
    print('isQQInstalled result: $result');
    return result;
  }

  static Future<dynamic> isWeworkInstalled() async {
    final String result = await _channel.invokeMethod('isWeworkInstalled');
    print('isWeworkInstalled result: $result');
    return result;
  }
}
