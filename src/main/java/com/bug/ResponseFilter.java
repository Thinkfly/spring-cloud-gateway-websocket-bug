package com.bug;

import com.alibaba.fastjson.JSONObject;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ResponseFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //检查是否需要包裹

        ServerHttpResponse httpResponse = exchange.getResponse();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(httpResponse) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                if (body instanceof Flux) {

                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                    Flux realBody = fluxBody.buffer().map(dataBuffers -> {

                        String wrapBody = null;

                        String stringBody = HttpUtils.getStringBody(dataBuffers);

                        MediaType mediaType = httpResponse.getHeaders().getContentType();

                        if (MediaType.APPLICATION_JSON_UTF8.equals(mediaType)) {

                            JSONObject resp = new JSONObject();

                            JSONObject jo = JSONObject.parseObject(stringBody);

                            resp.put("data", jo);

                            wrapBody = resp.toJSONString();

                        }

                        httpResponse.setStatusCode(HttpStatus.OK);

                        httpResponse.getHeaders().setContentLength(wrapBody.getBytes().length);

                        return httpResponse.bufferFactory().wrap(wrapBody.getBytes());

                    });

                    return super.writeWith(realBody);

                }

                return super.writeWith(body);
            }

        };

        //用门面代替返回
        return chain.filter(exchange.mutate().response(decoratedResponse).build());


    }

    @Override
    public int getOrder() {
        return -2;
    }
}
