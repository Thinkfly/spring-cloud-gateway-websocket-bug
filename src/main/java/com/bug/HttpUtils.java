package com.bug;

import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HttpUtils {

    public static String getHeader(ServerHttpRequest request, String key) {

        return getHeader(request.getHeaders(), key);

    }

    public static String getHeader(ServerRequest request, String key) {

        return getHeader(request.headers().asHttpHeaders(), key);

    }

    public static String getHeader(HttpHeaders httpHeaders, String key) {

        return getParam(httpHeaders, key);

    }

    public static String getParam(MultiValueMap<String, String> map, String key) {

        String result = null == map.get(key) ? null : map.get(key).get(0);

        return result;

    }

    public static String getStringRequestBody(ServerHttpRequest serverHttpRequest) {

        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();

        AtomicReference<String> bodyRef = new AtomicReference<>();

        body.subscribe(buffer -> {

            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());

            DataBufferUtils.release(buffer);

            bodyRef.set(charBuffer.toString());

        });

        //获取request body
        return bodyRef.get();

    }

    public static DataBuffer stringBuffer(String value) {

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);

        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);

        buffer.write(bytes);

        return buffer;

    }

    public static String getStringBody(List<? extends DataBuffer> dataBuffers) {

        byte[] total = new byte[0];

        for (DataBuffer dataBuffer : dataBuffers) {

            byte[] content = new byte[dataBuffer.readableByteCount()];

            dataBuffer.read(content);

            DataBufferUtils.release(dataBuffer);

            total = byteMerger(total, content);

        }

        String result = new String(total, Charset.forName("UTF-8"));

        return result;

    }

    public static String getStringBody(DataBuffer dataBuffer) {

        String result = null;

        byte[] content = new byte[dataBuffer.readableByteCount()];

        dataBuffer.read(content);

        DataBufferUtils.release(dataBuffer);

        result = new String(content, Charset.forName("UTF-8"));

//        log.info("getStringBody-result:{}", result);

        return result;

    }

    public static byte[] byteMerger(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length+bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

}
