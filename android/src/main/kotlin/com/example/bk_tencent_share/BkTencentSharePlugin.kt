package com.example.bk_tencent_share

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.NonNull
import com.tencent.connect.common.Constants
import com.tencent.connect.share.QQShare
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.tencent.wework.api.IWWAPI
import com.tencent.wework.api.WWAPIFactory
import com.tencent.wework.api.model.WWMediaImage
import com.tencent.wework.api.model.WWMediaLink
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.*
import java.net.URL


/** BkTencentSharePlugin */
class BkTencentSharePlugin: FlutterPlugin, MethodCallHandler {
  private var APPID = ""
  private var AGENTID = ""
  private val result: Result? = null
  private var context: Context? = null
  private var registrar: Registrar? = null
  private var api: IWXAPI? = null
  private var iwwapi: IWWAPI? = null
  private var bitmap: Bitmap? = null
  private lateinit var channel : MethodChannel
  private var flutterTencent: Tencent? = null
  val BK_DEVOPS_TITLE = "蓝盾DevOps平台"


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    print("onAttachedToEngine");
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "bk_tencent_share")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  fun registerWith(_registrar: Registrar) {
    registrar = _registrar
    print("registerWith");
    val intentFilter = IntentFilter()
    intentFilter.addAction("sendResp")
    context?.registerReceiver(createReceiver(), intentFilter)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "register") {
      val appid: String = call.argument<Any>("appId") as String
      val qqAppId: String = call.argument<Any>("qqAppId") as String
      val wxWorkAppId: String = call.argument<Any>("wxWorkAppId") as String
      val wxWorkCorpId: String = call.argument<Any>("wxWorkCorpId") as String
      val wxWorkAgentId: String = call.argument<Any>("wxWorkAgentId") as String



      api = WXAPIFactory.createWXAPI(context, appid, true)
      flutterTencent = Tencent.createInstance(qqAppId, context);
      api?.registerApp(appid);

      this.AGENTID = wxWorkAgentId;
      this.APPID = wxWorkCorpId;
      iwwapi = WWAPIFactory.createWWAPI(context)

      iwwapi?.registerApp(wxWorkAppId);

      result.success("success");
    } else if (call.method == "isWechatInstalled") {
      if (api == null) {
        result.success("false")
      } else {
        val isInstall = api?.isWXAppInstalled()
        result.success(isInstall.toString());
      }
    } else if (call.method == "isQQInstalled") {
      if (api == null) {
        result.success(false)
      } else {
        val isInstall = flutterTencent?.isQQInstalled(context);
        val resultStr: String = isInstall.toString();
        result.success(resultStr);
      }
    } else if (call.method == "isWeworkInstalled") {
      if (api == null) {
        result.success(false)
      } else {
        val isInstall = flutterTencent?.isQQInstalled(context);
        val resultStr: String = isInstall.toString();
        result.success(resultStr);
      }
    } else if (call.method == "shareToWechat") {
      val kind = call.argument<String>("kind")
      val coverUrl = call.argument<String>("previewImageUrl")
      val request = SendMessageToWX.Req()
      request.scene = SendMessageToWX.Req.WXSceneSession
      val wxmessage: WXMediaMessage = WXMediaMessage()

      when (kind) {
        "image" -> {
          val imgObj = WXImageObject()
          imgObj.setImagePath(coverUrl)
          wxmessage.mediaObject = imgObj
          //网络图片或者本地图片
          object : Thread() {
            override fun run() {

              request.transaction = System.currentTimeMillis().toString()
              request.message = wxmessage
              api?.sendReq(request)
            }
          }.start()
        }
        "webpage" -> {
          val webpageObject = WXWebpageObject()
          webpageObject.webpageUrl = call.argument<Any>("url").toString()

          if (bitmap != null) {
            val thumbBitmap = Bitmap.createScaledBitmap(bitmap!!, 100, 100, true)
            wxmessage?.thumbData = convertBitmapToByteArray(thumbBitmap, true)
          }


          wxmessage?.mediaObject = webpageObject
          wxmessage?.title = call.argument<Any>("title").toString()
          wxmessage?.description = call.argument<Any>("description").toString()
          //网络图片或者本地图片
          object : Thread() {
            override fun run() {

              bitmap = GetBitmap(coverUrl)
              if (bitmap == null) {
                Toast.makeText(context, "图片路径错误", Toast.LENGTH_SHORT).show()
                return
              }
              val imageObject = WXImageObject(bitmap)
              wxmessage?.mediaObject = imageObject
              request.transaction = System.currentTimeMillis().toString()
              request.message = wxmessage
              api?.sendReq(request)
            }
          }.start()
        }
      }
    } else if (call.method == "shareToQQ") {
      val listener = OneListener()
      val kind = call.argument<String>("kind")
      val qqParams = Bundle()
      print("BkTencentShare" + "arguments:" + call.arguments)

      when (kind) {
        "image" -> {
          val imageUri: String? = call.argument("previewImageUrl");

          qqParams.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)

          qqParams.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUri)
          qqParams.putString(QQShare.SHARE_TO_QQ_APP_NAME, context?.packageName)

        }
        "webpage" -> {

          qqParams.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
          qqParams.putString(QQShare.SHARE_TO_QQ_TITLE, call.argument("title"))
          qqParams.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, call.argument("previewImageUrl"))
          qqParams.putString(QQShare.SHARE_TO_QQ_SUMMARY, call.argument("description"))
          qqParams.putString(QQShare.SHARE_TO_QQ_TARGET_URL, call.argument("url"))
        }
      }

      print("BkTencentShare" +  "params:$qqParams")
      Handler(Looper.getMainLooper()).post(Runnable { flutterTencent?.shareToQQ(registrar!!.activity(), qqParams, listener) })
    } else if (call.method == "shareToWework") {
      val kind = call.argument<String>("kind")
      val coverUrl = call.argument<String>("previewImageUrl")
      val request = SendMessageToWX.Req()
      request.scene = SendMessageToWX.Req.WXSceneSession
      val wxmessage: WXMediaMessage = WXMediaMessage()

      if (bitmap != null) {
        val thumbBitmap = Bitmap.createScaledBitmap(bitmap!!, 100, 100, true)
        wxmessage?.thumbData = convertBitmapToByteArray(thumbBitmap, true)
      }

      when (kind) {
        "image" -> {
          val img = WWMediaImage()
          img.fileName = call.argument<String>("fileName");
          img.filePath = call.argument<String>("previewImageUrl");
          img.appPkg = context?.packageName;
          img.appName = context?.packageName;
          img.appId = APPID
          img.agentId = AGENTID
          iwwapi!!.sendMessage(img)
        }
        "webpage" -> {
          val link = WWMediaLink()
          link.thumbUrl = call.argument<String>("previewImageUrl");
          link.webpageUrl = call.argument<String>("url");
          link.title = call.argument<String>("title");
          link.description = call.argument<String>("description");
          link.appPkg = context?.packageName;
          link.appName = context?.packageName;
          link.appId = APPID
          link.agentId = AGENTID
          iwwapi!!.sendMessage(link)
        }
      }
    } else {
      result.notImplemented()
    }
  }
  private fun shareToQQ(call: MethodCall, listener: OneListener) {
    val params = Bundle()
    print("BkTencentShare" + "arguments:" + call.arguments)
    val qqParams = Bundle()
    qqParams.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
    qqParams.putString(QQShare.SHARE_TO_QQ_TITLE, call.argument("title"))
    qqParams.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, call.argument("previewImageUrl"))
    qqParams.putString(QQShare.SHARE_TO_QQ_SUMMARY, call.argument("description"))
    qqParams.putString(QQShare.SHARE_TO_QQ_TARGET_URL, call.argument("url"))

    print("FlutterQqPlugin" +  "params:$params")
    Handler(Looper.getMainLooper()).post(Runnable { flutterTencent?.shareToQQ(registrar!!.activity(), params, listener) })
  }

  private  fun shareImageToQQ(call: MethodCall, listener: OneListener) {
    val imageUri: String? = call.argument("previewImageUrl");
    val filePath: String = Environment.getDownloadCacheDirectory().getPath().toString() + "/qrcode.png"
    print("sdPath" + filePath);
    val bmp = GetBitmap(imageUri);

    bitmapToFile(filePath, bmp, 90)

    val qqParams = Bundle()

    qqParams.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
    qqParams.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, filePath)
    qqParams.putString(QQShare.SHARE_TO_QQ_TITLE, BK_DEVOPS_TITLE)
    qqParams.putString(QQShare.SHARE_TO_QQ_SUMMARY, call.argument("description"))
  }

  /**
   * bitmap保存为file
   */
  fun bitmapToFile(filePath: String?,
                   bitmap: Bitmap?, quality: Int) {
    if (bitmap != null) {
      try {
        val file = File(filePath)
        if (file.exists()) {
          return
        }
        val bos = BufferedOutputStream(
                FileOutputStream(filePath))
        bitmap.compress(CompressFormat.PNG, quality, bos)
        bos.flush()
        bos.close()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  fun GetBitmap(url: String?): Bitmap? {
    var bitmap: Bitmap? = null
    var `in`: InputStream? = null
    var out: BufferedOutputStream? = null
    return try {
      `in` = BufferedInputStream(URL(url).openStream(), 1024)
      val dataStream = ByteArrayOutputStream()
      out = BufferedOutputStream(dataStream, 1024)
      copy(`in`, out)
      out.flush()
      val data = dataStream.toByteArray()
      bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
      bitmap
    } catch (e: IOException) {
      e.printStackTrace()
      null
    }
  }

  fun convertBitmapToByteArray(bitmap: Bitmap, needRecycle: Boolean): ByteArray? {
    val output = ByteArrayOutputStream()
    bitmap.compress(CompressFormat.PNG, 100, output)
    if (needRecycle) {
      bitmap.recycle()
    }
    val result = output.toByteArray()
    try {
      output.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return result
  }

  @Throws(IOException::class)
  private fun copy(`in`: InputStream, out: OutputStream) {
    val b = ByteArray(1024)
    var read: Int
    while (`in`.read(b).also { read = it } != -1) {
      out.write(b, 0, read)
    }
  }

  private fun createReceiver(): BroadcastReceiver? {
    return object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent) {
        println(intent.getStringExtra("type"))
        if (intent.getStringExtra("type") == "SendAuthResp") {
          result?.success(intent.getStringExtra("code"))
        } else if (intent.getStringExtra("type") == "PayResp") {
          result?.success(intent.getStringExtra("code"))
        } else if (intent.getStringExtra("type") == "ShareResp") {
          println(intent.getStringExtra("code"))
          result?.success(intent.getStringExtra("code"))
        }
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private class OneListener : IUiListener, PluginRegistry.ActivityResultListener {
    private var result: Result? = null
    fun setResult(result: Result?) {
      this.result = result
    }

    override fun onComplete(response: Any?) {
      var re: HashMap<String, Any> = HashMap<String, Any>()

      re.put("Code", 0);
      re.put("Message", response.toString())
      result!!.success(re)
    }

    override fun onError(uiError: UiError) {
      print("bkTencentShare: " + "errorCode:" + uiError.errorCode.toString() + ";errorMessage:" + uiError.errorMessage)
      var re: HashMap<String, Any> = HashMap<String, Any>()
      re.put("Code", 1)
      re.put("Message", "errorCode:" + uiError.errorCode.toString() + ";errorMessage:" + uiError.errorMessage)
      result!!.success(re)
    }

    override fun onCancel() {
      print("bkTencentShare error:cancel")
      var re: HashMap<String, Any> = HashMap<String, Any>()
      re.put("Code", 2)
      re.put("Message", "cancel")
      result!!.success(re)
    }

    override fun onWarning(p0: Int) {
//      TODO("Not yet implemented")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
      if (requestCode == Constants.REQUEST_QQ_SHARE) {
        Tencent.onActivityResultData(requestCode, resultCode, data, this)
        return true
      }
      return false
    }
  }
}
