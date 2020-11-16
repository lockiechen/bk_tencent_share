#import "BkTencentSharePlugin.h"


@implementation BkTencentSharePlugin{
    FlutterResult result;
    NSObject<FlutterPluginRegistrar> *_registrar;
}

- (instancetype)init {
  self = [super init];
  [[NSNotificationCenter defaultCenter]
    addObserver:self
    selector:@selector(handleOpenURL:)
    name:@"WXWork"
    object:nil
  ];
//    [[NSNotificationCenter defaultCenter]
//      addObserver:self
//      selector:@selector(openURL:)
//      name:@"open"
//      object:nil
//    ];
//
//    [[NSNotificationCenter defaultCenter]
//      addObserver:self
//      selector:@selector(continueUserActivity:)
//      name:@"user"
//      object:nil
//    ];
  return self;
}


//- (void)dealloc {
//  [[NSNotificationCenter defaultCenter] removeObserver:self];
//}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*) registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"bk_tencent_share"
            binaryMessenger:[registrar messenger]];
  BkTencentSharePlugin* instance = [[BkTencentSharePlugin alloc] init];
//    _registrar = registrar;
    
  [registrar addMethodCallDelegate:instance channel:channel];
  [registrar addApplicationDelegate:instance];
    
    [WXApi startLogByLevel:WXLogLevelDetail logBlock:^(NSString *log) {
        NSLog(@"WeChatSDK: %@", log);
    }];
    
    
    
}



//- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url {
//    return  [WXApi handleOpenURL:url delegate:self];
//}
//
//- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
//    return [WXApi handleOpenURL:url delegate:self];
//}
//
//- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void(^)(NSArray<id<UIUserActivityRestoring>> * __nullable restorableObjects))restorationHandler
//{
//    return [WXApi handleOpenUniversalLink:userActivity delegate:self];
//}
//
//- (void) scene:(UIScene *)scene continueUserActivity:(NSUserActivity *)userActivity {
//    [WXApi handleOpenUniversalLink:userActivity delegate:self];
//}



- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  NSDictionary *arguments = [call arguments];
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }
  // Register app to Wechat with appid
  else if ([@"register" isEqualToString:call.method]) {
      NSLog(@"UL: %@", arguments[@"universalLink"]);
      [WXApi registerApp: arguments[@"appId"] universalLink: arguments[@"universalLink"]];
      [[TencentOAuth alloc] initWithAppId:arguments[@"qqAppId"] andDelegate:self];
      [WWKApi registerApp:arguments[@"wxWorkAppId"] corpId:arguments[@"wxWorkCorpId"] agentId:arguments[@"wxWorkAgentId"]];

    result(@"true");
  } else if ([@"isWechatInstalled" isEqualToString:call.method]) {
    BOOL installed = [WXApi isWXAppInstalled];
    result([NSString stringWithFormat:@"%@", installed ? @"true" : @"false"]);
  } else if ([@"isQQInstalled" isEqualToString:call.method]) {
    BOOL isInstall = [QQApiInterface isQQInstalled];
    result([NSString stringWithFormat:@"%@", isInstall ? @"true" : @"false"]);
  } else if ([@"isWeworkInstalled" isEqualToString:call.method]) {
    BOOL isInstall = [WWKApi isAppInstalled];
    result([NSString stringWithFormat:@"%@", isInstall ? @"true" : @"false"]);
  } else if ([@"shareToWechat" isEqualToString:call.method]) {
    NSString* kind = arguments[@"kind"];
    // Wechat Message Request.
    SendMessageToWXReq *request = [[SendMessageToWXReq alloc] init];
    // Defaults request is not bText.
    request.bText = NO;
    request.scene = WXSceneSession;
    WXMediaMessage *message = [WXMediaMessage message];
    if ([@"webpage" isEqualToString:kind]) {
      
      message.title = arguments[@"title"];
      message.description = arguments[@"description"];

      [message setThumbImage:[UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString: arguments[@"previewImageUrl"]]]]];
      WXWebpageObject * webpageObject = [WXWebpageObject object];
      webpageObject.webpageUrl = arguments[@"url"];
      message.mediaObject = webpageObject;
    } else if ([@"image" isEqualToString:kind]) {
      WXImageObject *imageObject = [WXImageObject object];
        imageObject.imageData = [NSData dataWithContentsOfFile:arguments[@"previewImageUrl"]];
      message.mediaObject = imageObject;
    }

    request.message = message;

      [WXApi sendReq:request completion:^(BOOL success) {
          NSLog(@"SHARE COMPLETE");
          result([NSString stringWithFormat:@"%@", success ? @"true" : @"false"]);
      }];
  } else if ([@"shareToQQ" isEqualToString:call.method]) {
      NSString* kind = arguments[@"kind"];
      QQApiNewsObject *message = nil;
      if ([@"webpage" isEqualToString:kind]) {
        message = [QQApiNewsObject
                  objectWithURL:[NSURL URLWithString:arguments[@"url"]]
                  title:arguments[@"title"]
                  description:arguments[@"description"]
                  previewImageURL:[NSURL URLWithString:arguments[@"previewImageUrl"]]];
      } else if ([@"image" isEqualToString:kind]) {
          NSData *imgData = [NSData dataWithContentsOfFile:arguments[@"previewImageUrl"]];
          QQApiImageObject *imgObj = [QQApiImageObject objectWithData:imgData
                                                     previewImageData:imgData
                                                     title:arguments[@"title"]
                                                     description :arguments[@"description"]];
          message = imgObj;
      }
    
    SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:message];
    //将内容分享到qq
    QQApiSendResultCode sent = [QQApiInterface sendReq:req];
  
    result(@"qq success");
  }  else if ([@"shareToWework" isEqualToString:call.method]) {
      NSString* kind = arguments[@"kind"];
      WWKSendMessageReq *req = [[WWKSendMessageReq alloc] init];
      
      if ([@"webpage" isEqualToString:kind]) {
          NSData *previewImageData = [NSData dataWithContentsOfURL:[NSURL URLWithString:arguments[@"previewImageUrl"]]];
          
          WWKMessageLinkAttachment *attachment = [[WWKMessageLinkAttachment alloc] init];
          attachment.title = arguments[@"title"];
          attachment.summary = arguments[@"description"];
          attachment.url = arguments[@"url"];
          attachment.icon = previewImageData;
          req.attachment = attachment;
      } else if ([@"image" isEqualToString:kind]) {

          WWKMessageImageAttachment *attachment = [[WWKMessageImageAttachment alloc] init];
          attachment.filename = arguments[@"fileName"];
          attachment.path = arguments[@"previewImageUrl"];
          NSLog(@"%@", attachment.path);
          req.attachment = attachment;
          
      }
    
      [WWKApi sendReq:req];
  
    result(@"wework success");
  } else if ([@"shareToWework" isEqualToString:call.method]) {
    //
  } else {
    result(FlutterMethodNotImplemented);
  }
}
//
-(BOOL)handleOpenURL:(NSNotification *)notification {
  NSString * urlString =  [notification userInfo][@"url"];
  NSURL * url = [NSURL URLWithString:urlString];
    return [WWKApi handleOpenURL:url delegate:self];
}
//
//-(void) onReq:(BaseReq*)reqonReq {
//    NSLog(@"plugin onReq");
//}
//
//
//-(void) onResp:(BaseResp*)resp {
//    NSLog(@"plugin onResp");
//  if([resp isKindOfClass:[SendMessageToWXResp class]]) {
//
//    result([NSString stringWithFormat:@"%d",resp.errCode]);
//  }
//}

- (void)onResp:(WWKBaseResp *)resp {
    NSLog(@"%s", "onWWK reponse");
}
// onReq是企业微信终端向第三方程序发起请求，要求第三方程序响应。第三方程序响应完后必须调用sendRsp返回。在调用sendRsp返回时，会切回到企业微信终端程序界面。
-(void) onReq:(WWKBaseReq*)req {
}


@end
