package ru.jamsys.component;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.jamsys.AbstractCoreComponent;
import ru.jamsys.Util;
import yandex.cloud.speechkit.TtsV3Client;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
@Lazy
public class YandexSpeech extends AbstractCoreComponent {

    public boolean synthesize(String text, String path, Map<String, Object> settings) {
        TtsV3Client client = null;
        boolean result = false;
        try {
            Map<String, Object> defSettings = new HashMap<>();
            defSettings.put("speed", 1.0);
            defSettings.put("voice", "marina");
            defSettings.put("role", "neutral");

            if (settings != null) {
                Util.overflow(defSettings, settings);
            }
            String apikey = "AQVN3EGt9Bm9DEaalAFhflDIVX4A4KUN4CeJVSAx";
            client = new TtsV3Client("tts.api.cloud.yandex.net", 443, apikey);
            Util.logConsole("Start");
            result = client.synthesize(text, new File(path), 5000, defSettings);
            Util.logConsole("Stop " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client != null) {
            client.disconnect();
        }
        return result;
    }

    public boolean synthesize(String text, String path) {
        return synthesize(text, path, null);
    }

    @Override
    public void flushStatistic() {

    }
}
