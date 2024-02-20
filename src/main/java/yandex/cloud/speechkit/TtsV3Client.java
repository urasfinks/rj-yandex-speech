package yandex.cloud.speechkit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import speechkit.common.v3.Common;
import syandex.cloud.api.ai.tts.v3.Tts;
import yandex.cloud.api.ai.tts.v3.SynthesizerGrpc;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

public class TtsV3Client {

    private final SynthesizerGrpc.SynthesizerStub client;
    private final ManagedChannel channel;

    public TtsV3Client(String host, int port, String apiKey) {
        channel = ManagedChannelBuilder.forAddress(host, port).build();
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Api-Key " + apiKey);
        var requestId = UUID.randomUUID().toString();
        headers.put(Metadata.Key.of("x-client-request-id", Metadata.ASCII_STRING_MARSHALLER), requestId);
        client = SynthesizerGrpc.newStub(channel).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    public boolean synthesize(String text, File output, int timeOutMillis, Map<String, Object> defSettings) throws UnsupportedAudioFileException, IOException {
        boolean result = false;
        Tts.UtteranceSynthesisRequest request = Tts.UtteranceSynthesisRequest
                .newBuilder()
                .setText(text)
                .setOutputAudioSpec(Common.AudioFormatOptions
                        .newBuilder()
                        .setContainerAudio(Common.ContainerAudio
                                .newBuilder()
                                .setContainerAudioType(Common.ContainerAudio.ContainerAudioType.WAV)
                                .build()))
                .addHints(Tts.Hints.newBuilder().setSpeed((double) defSettings.get("speed")))
                .addHints(Tts.Hints.newBuilder().setVoice((String) defSettings.get("voice")))
                .addHints(Tts.Hints.newBuilder().setRole((String) defSettings.get("role")))
                .setLoudnessNormalizationType(Tts.UtteranceSynthesisRequest.LoudnessNormalizationType.LUFS)
                .build();

        TtsStreamObserver observer = new TtsStreamObserver(new Exchanger<>());
        client.utteranceSynthesis(request, observer);
        byte[] bytes = observer.awaitResult(1000);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes));
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, output);
        try {
            result = observer.exchanger.exchange(false, timeOutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void disconnect() {
        channel.shutdownNow();
    }
}
