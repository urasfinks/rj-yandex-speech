package yandex.cloud.speechkit;

import io.grpc.stub.StreamObserver;
import syandex.cloud.api.ai.tts.v3.Tts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

class TtsStreamObserver implements StreamObserver<Tts.UtteranceSynthesisResponse> {

    final Exchanger<Boolean> exchanger;

    private ByteArrayOutputStream result = new ByteArrayOutputStream();
    private CountDownLatch count = new CountDownLatch(1);

    TtsStreamObserver(Exchanger<Boolean> exchanger) {
        this.exchanger = exchanger;
    }

    @Override
    public void onNext(Tts.UtteranceSynthesisResponse response) {
        if (response.hasAudioChunk()) {
            try {
                result.write(response.getAudioChunk().getData().toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onError(Throwable t) {
        try {
            exchanger.exchange(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {
        try {
            exchanger.exchange(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        count.countDown();
    }

    byte[] awaitResult(int timeoutMillis) {
        try {
            count.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result.toByteArray();
    }
}