#import <Flutter/Flutter.h>
#import "WechatOpenSDK/WXApi.h"
#import "WWKApi.h"
#import <TencentOpenAPI/TencentOAuth.h>
#import <TencentOpenAPI/QQApiInterface.h>

@interface BkTencentSharePlugin : NSObject <FlutterPlugin, WXApiDelegate, TencentSessionDelegate, WWKApiDelegate>
@end
