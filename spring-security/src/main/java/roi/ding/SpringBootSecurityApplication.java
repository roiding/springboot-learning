package roi.ding;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication
public class SpringBootSecurityApplication {
	//验证码
	@Bean
	Producer verifyCode() {
		Properties properties = new Properties();
		properties.setProperty("kaptcha.image.width", "150");
		properties.setProperty("kaptcha.image.height", "50");
		properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
		properties.setProperty("kaptcha.textproducer.char.length", "4");
		Config config = new Config(properties);
		DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
		defaultKaptcha.setConfig(config);
		return defaultKaptcha;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSecurityApplication.class, args);
	}
}
