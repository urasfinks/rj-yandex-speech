package ru.jamsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.jamsys.component.Broker;
import ru.jamsys.component.YandexSpeech;

@SpringBootApplication
public class App {

    public static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(App.class, args);
        System.out.println("Hello World!");
        //YandexSpeech bean = context.getBean(YandexSpeech.class);
        //bean.synthesize("Привет, Маша!", "2.wav");
    }
}
