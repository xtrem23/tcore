/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.templates.requests;

import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.log.LcLevel;

/**
 * Created by Gavriil Sitnikov on 13/11/2015.
 * Base class that is requesting data via HTTP using OkHttp library as executor and RxJava as interface.
 * Also it is parsing responded data to specific type.
 *
 * @param <T> Type of parsed object.
 */
public abstract class HttpRequest<T> {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    @NonNull
    private static Charset getCharset(@NonNull final ResponseBody responseBody) {
        final MediaType contentType = responseBody.contentType();
        return contentType == null ? DEFAULT_CHARSET : contentType.charset(DEFAULT_CHARSET);
    }

    @NonNull
    private static String requestBodyToString(@NonNull final Request request) throws IOException {
        final RequestBody body = request.newBuilder().build().body();
        if (body == null) {
            return "";
        }
        final Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }

    @NonNull
    private final Class<T> responseResultType;

    protected HttpRequest(@NonNull final Class<T> responseResultType) {
        this.responseResultType = responseResultType;
    }

    /**
     * Class of object to be parsed from response.
     *
     * @return Type of object.
     */
    @NonNull
    public Class<T> getResponseResultType() {
        return responseResultType;
    }

    /**
     * Base URL of request.
     *
     * @return URL.
     */
    @NonNull
    protected abstract String baseUrl();

    /**
     * Creates {@link OkHttpClient} object.
     * Could be override if you want to specify client.
     *
     * @return Exemplar of {@link OkHttpClient}.
     */
    @NonNull
    protected OkHttpClient createHttpClient() {
        return new OkHttpClient();
    }

    /**
     * Creates Request builder.
     * Could be override if you want to specify request building.
     *
     * @return Request builder;
     * @throws IOException Exception during request creation.
     */
    @NonNull
    protected Request.Builder createHttpRequest() throws IOException {
        final HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl()).newBuilder();
        final Map<String, String> headers = new HashMap<>();
        onConfigureHeaders(headers);
        onConfigureUrlParameters(urlBuilder);
        return new Request.Builder().url(urlBuilder.build()).headers(Headers.of(headers));
    }

    /**
     * Method to specify headers.
     * Override it to add new headers etc.
     *
     * @param headers Headers to be used in request.
     */
    protected void onConfigureHeaders(@NonNull final Map<String, String> headers) {
        // to be overridden. default does nothing
    }

    /**
     * Method to specify URL parameters.
     * Override it to add new URL parameters etc.
     *
     * @param urlBuilder URL builder to be used to build URL of request.
     */
    protected void onConfigureUrlParameters(@NonNull final HttpUrl.Builder urlBuilder) {
        // to be overridden. default does nothing
    }

    /**
     * Parses responded data to specific typed object.
     *
     * @param responseResultType Type of object to be parsed;
     * @param charset            Charset of responded data;
     * @param inputStream        Responded data;
     * @return Parsed object;
     * @throws IOException Exception during parsing.
     */
    @NonNull
    protected abstract T parse(@NonNull final Class<T> responseResultType, @NonNull final Charset charset, @NonNull final InputStream inputStream)
            throws IOException;

    @SuppressWarnings({"unchecked", "PMD.NPathComplexity"})
    //TODO: NPathComplexity
    @NonNull
    private T executeSyncInternal(@NonNull final RequestController requestController) throws IOException {
        final boolean shouldLog = Lc.getLogProcessor().getMinLogLevel().lessThan(LcLevel.INFO);
        if (shouldLog) {
            Lc.d("Url requested: %s\n%s", requestController.request.url(), requestBodyToString(requestController.request));
        }
        final Response response = requestController.call.execute();
        final ResponseBody responseBody = response.body();
        final Charset charset = getCharset(responseBody);
        final byte[] bytes = shouldLog ? response.body().bytes() : null;
        if (shouldLog) {
            Lc.d("Response for: %s has code %s and content: %s", requestController.request.url(), response.code(),
                    new String(bytes, charset));
        }
        if (getResponseResultType().equals(Response.class)) {
            return handleResponse((T) response);
        }
        final T result;
        try {
            result = parse(responseResultType, charset, bytes == null ? response.body().byteStream() : new ByteArrayInputStream(bytes));
        } catch (final RuntimeException throwable) {
            Lc.assertion("Runtime exception during response parsing " + requestController.request.url());
            throw new IOException(throwable);
        }
        return handleResponse(result);
    }

    /**
     * Synchronously executes request.
     *
     * @return Parsed result of request;
     * @throws IOException Exception during request.
     */
    @NonNull
    public T executeSync() throws IOException {
        return executeSyncInternal(new RequestController());
    }

    /**
     * Asynchronously executes request. Basically emits only one item as a result of request.
     * Could emit {@link IOException} as error.
     */
    @NonNull
    public Observable<T> execute() {
        return Observable
                .fromCallable(RequestController::new)
                .switchMap(requestController -> Observable
                        .fromCallable(() -> executeSyncInternal(requestController))
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .doOnDispose(requestController.call::cancel));
    }

    /**
     * Override this method to handle response (change or replace or throw exception if it's invalid or whatever).
     *
     * @param response Response to handle;
     * @return Handled response;
     * @throws IOException Exception during handle.
     */
    @NonNull
    protected T handleResponse(@NonNull final T response) throws IOException {
        return response;
    }

    private class RequestController {

        @NonNull
        private final Request request;
        @NonNull
        private final Call call;

        public RequestController() throws IOException {
            this.request = createHttpRequest().build();
            this.call = createHttpClient().newCall(this.request);
        }

    }

}