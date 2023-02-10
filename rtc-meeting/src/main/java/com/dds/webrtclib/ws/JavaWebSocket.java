package com.dds.webrtclib.ws;

import android.annotation.SuppressLint;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public class JavaWebSocket implements IWebSocket {

    private final static String TAG = "zrzr";

    private WebSocketClient mWebSocketClient;

    private ISignalingEvents events;

    private boolean isOpen; //是否连接成功过

    private String connectId;

    public JavaWebSocket(ISignalingEvents events) {
        this.events = events;
    }

    @Override
    public void connect(String wss) {
        URI uri;
        try {
            uri = new URI(wss);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        if (mWebSocketClient == null) {
            mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isOpen = true;
                    events.onWebSocketOpen();
                }

                @Override
                public void onMessage(String message) {
                    isOpen = true;
                    Log.e(TAG, "onMessage = "+ message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.e(TAG, "onClose:" + reason);
                    if (events != null) {
                        events.onWebSocketOpenFailed(reason);
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, ex.toString());
                    if (events != null) {
                        events.onWebSocketOpenFailed(ex.toString());
                    }
                }
            };
        }
        if (wss.startsWith("wss")) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                if (sslContext != null) {
                    sslContext.init(null, new TrustManager[]{new TrustManagerTest()}, new SecureRandom());
                }

                SSLSocketFactory factory = null;
                if (sslContext != null) {
                    factory = sslContext.getSocketFactory();
                }

                if (factory != null) {
                    mWebSocketClient.setSocket(factory.createSocket());
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mWebSocketClient.connect();
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }

    }


    //============================需要发送的=====================================
    @Override
    public void joinRoom(String room) {
        //心跳
        Map<String, Object> map = new HashMap<>();
        map.put("type", "HEART");
        map.put("clientType", "ANDROID");
        map.put("id", "111");
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.e(TAG, "send heart-->" + jsonString);
        mWebSocketClient.send(jsonString);

        //发起预通话配置
        Map<String, Object> map1 = new HashMap<>();
        map1.put("type", "START");
        map1.put("id", "111");
        JSONObject object1 = new JSONObject(map1);
        final String jsonString1 = object1.toString();
        Log.e(TAG, "send pre-->" + jsonString1);
        mWebSocketClient.send(jsonString1);
    }

    public void sendAnswer(String socketId, String sdp) {
        Map<String, Object> childMap1 = new HashMap();
        childMap1.put("type", "answer");
        childMap1.put("sdp", sdp);
        HashMap<String, Object> childMap2 = new HashMap();
        childMap2.put("socketId", socketId);
        childMap2.put("sdp", childMap1);
        HashMap<String, Object> map = new HashMap();
        map.put("eventName", "__answer");
        map.put("data", childMap2);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        Log.e(TAG, "send answer-->" + jsonString);
        mWebSocketClient.send(jsonString);
    }


    public void sendOffer(String socketId, String sdp) {
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("type", "offer");
        childMap.put("sdp", sdp);

        Map<String, Object> map = new HashMap<>();
        map.put("type", "OFFER");
        map.put("offerId", "111");
        map.put("answerId", "111");
        map.put("description", childMap);

        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.e(TAG, "send offer-->" + jsonString);

        mWebSocketClient.send(jsonString);
    }

    public void sendIceCandidate(String socketId, IceCandidate iceCandidate) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "ICE");
        map.put("fromId", "111");
        map.put("fromClientType", "ANDROID");
        map.put("toId", "111");
        map.put("candidate", iceCandidate);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.e(TAG, "send ice-->" + jsonString);

        mWebSocketClient.send(jsonString);
    }
    //============================需要发送的=====================================


    //============================需要接收的=====================================
    @Override
    public void handleMessage(String message) {
//        Map map = JSON.parseObject(message, Map.class);
//        String eventName = (String) map.get("eventName");
//        if (eventName == null) return;
//        if (eventName.equals("_peers")) {
//            handleJoinToRoom(map);
//        }
//        if (eventName.equals("_new_peer")) {
//            handleRemoteInRoom(map);
//        }
//        if (eventName.equals("_ice_candidate")) {
//            handleRemoteCandidate(map);
//        }
//        if (eventName.equals("_remove_peer")) {
//            handleRemoteOutRoom(map);
//        }
//        if (eventName.equals("_offer")) {
//            handleOffer(map);
//        }
//        if (eventName.equals("_answer")) {
//            handleAnswer(map);
//        }

        try {
            //这个是解析你的回调数据
            JSONObject jsonObject = JSON.parseObject(message);

            //发起预通话配置回调
            if (jsonObject.getInteger("connectStatus") != null) {
                //连接状态 connectStatus 0-分配成功 1-需要排队
                int connectStatus = jsonObject.getIntValue("connectStatus");
                connectId = jsonObject.getString("connectId");
                String offerId = jsonObject.getString("offerId");
                if(connectStatus == 0) {
                    //预通话配置 分配成功 发起正式通话
                    String answerId = jsonObject.getString("answerId");
                    sendCall(answerId, offerId);
                }else{
//                    handleJoinToRoom(connectId, offerId);
                }
            }

            //发起正式通话回调
            if (jsonObject.getString("callStatus") != null) {
                String callStatus = jsonObject.getString("callStatus");
                String offerId = jsonObject.getString("offerId");
                Log.e("zrzr", "callStatus = " + callStatus);
                //接通
                if("ANSWER".equals(callStatus)) {
                    handleJoinToRoom(connectId, offerId);
                }
            }

            //发送offer 接收到answer
            if(jsonObject.getString("type") != null
                    && "ANSWER".equals(jsonObject.getString("type"))) {
                Map map = JSON.parseObject(message, Map.class);
                Map desc = (Map) map.get("description");
                String sdp = (String) desc.get("sdp");

                Log.e("aaaaa", "answer answer = " + message);

                events.onReceiverAnswer(connectId, sdp);
            }

            if(jsonObject.getString("type") != null
                    && "ICE".equals(jsonObject.getString("type"))) {

                Map map = JSON.parseObject(message, Map.class);
                IceCandidate iceCandidate = jsonObject.getObject("candidate", IceCandidate.class);

                events.onRemoteIceCandidate(connectId, iceCandidate);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发起正式通话
     * @param answerId
     * @param offerId
     */
    private void sendCall(String answerId, String offerId) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "CALL");
        map.put("offerId", offerId);
        map.put("answerId", answerId);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.e(TAG, "send call-->" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    private void handleJoinToRoom(String connectId, String offerId) {
        Log.e("zrzr", "handleJoinToRoom");
        ArrayList<String> connections = new ArrayList<>();
        connections.add(connectId);
        events.onJoinToRoom(connections, offerId);
    }

    // 自己进入房间
    private void handleJoinToRoom(Map map) {
        Map data = (Map) map.get("data");
        JSONArray arr;
        if (data != null) {
            arr = (JSONArray) data.get("connections");
            String js = JSONObject.toJSONString(arr, SerializerFeature.WriteClassName);
            ArrayList<String> connections = (ArrayList<String>) JSONObject.parseArray(js, String.class);
            String myId = (String) data.get("you");
            events.onJoinToRoom(connections, myId);
        }

    }

    // 自己已经在房间，有人进来
    private void handleRemoteInRoom(Map map) {
        Map data = (Map) map.get("data");
        String socketId;
        if (data != null) {
            socketId = (String) data.get("socketId");
            events.onRemoteJoinToRoom(socketId);
        }

    }

    // 处理交换信息
    private void handleRemoteCandidate(Map map) {
        Map data = (Map) map.get("data");
        String socketId;
        if (data != null) {
            socketId = (String) data.get("socketId");
            String sdpMid = (String) data.get("id");
            sdpMid = (null == sdpMid) ? "video" : sdpMid;
            int label = (int) Double.parseDouble(String.valueOf(data.get("label")));
            String candidate = (String) data.get("candidate");
            IceCandidate iceCandidate = new IceCandidate(sdpMid, label, candidate);
            events.onRemoteIceCandidate(socketId, iceCandidate);
        }


    }

    // 有人离开了房间
    private void handleRemoteOutRoom(Map map) {
        Map data = (Map) map.get("data");
        String socketId;
        if (data != null) {
            socketId = (String) data.get("socketId");
            events.onRemoteOutRoom(socketId);
        }

    }

    // 处理Offer
    private void handleOffer(Map map) {
        Map data = (Map) map.get("data");
        Map sdpDic;
        if (data != null) {
            sdpDic = (Map) data.get("sdp");
            String socketId = (String) data.get("socketId");
            String sdp = (String) sdpDic.get("sdp");
            events.onReceiveOffer(socketId, sdp);
        }

    }

    // 处理Answer
    private void handleAnswer(Map map) {
        Map data = (Map) map.get("data");
        Map sdpDic;
        if (data != null) {
            sdpDic = (Map) data.get("sdp");
            String socketId = (String) data.get("socketId");
            String sdp = (String) sdpDic.get("sdp");
            events.onReceiverAnswer(socketId, sdp);
        }

    }
    //============================需要接收的=====================================


    // 忽略证书
    public static class TrustManagerTest implements X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }


}
